package de.flattze.mapsync.commands;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;
import de.flattze.mapsync.extractor.MapExtractor;
import de.flattze.mapsync.renderer.CustomMapRenderer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapSyncCommand implements CommandExecutor {

    private final MapSyncPlugin plugin;

    public MapSyncCommand(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Verfügbare Befehle: upload | mymaps");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "upload" -> {
                plugin.getLogger().info("[MapSync] Starte Upload-Prozess...");

                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.FILLED_MAP) {
                    player.sendMessage("§cDu musst eine FILLED_MAP in der Hand halten!");
                    return true;
                }

                MapMeta meta = (MapMeta) item.getItemMeta();
                MapView view = meta.getMapView();
                if (view == null) {
                    player.sendMessage("§cKeine MapView gefunden!");
                    return true;
                }

                byte[] colors = null;

                // 1) Prüfe auf CustomMapRenderer
                for (MapRenderer renderer : view.getRenderers()) {
                    if (renderer instanceof CustomMapRenderer custom) {
                        colors = custom.getColors();
                        plugin.getLogger().info("[MapSync] CustomRenderer gefunden.");
                        break;
                    }
                }

                // 2) Fallback auf map_<id>.dat
                if (colors == null) {
                    plugin.getLogger().info("[MapSync] Kein CustomRenderer → Fallback auf .dat");
                    MapExtractor extractor = plugin.getMapExtractor();
                    colors = extractor.extractColors(view.getId(), player.getWorld());
                }

                if (colors == null || colors.length != 16384) {
                    player.sendMessage("§cFarben konnten nicht gelesen werden! Abbruch.");
                    return true;
                }

                // MapRecord bauen & speichern
                MapRecord record = new MapRecord(
                        view.getId(),
                        player.getUniqueId(),
                        player.getName(),
                        player.getWorld().getName(),
                        view.getScale().getValue(),
                        view.getCenterX(),
                        view.getCenterZ(),
                        true,
                        false,
                        colors
                );

                plugin.getDatabaseManager().uploadMap(record);
                player.sendMessage("§aKarte erfolgreich hochgeladen! ID: " + view.getId());
            }

            case "mymaps" -> {
                plugin.getGuiManager().openOwnedMaps(player, 1);
            }

            default -> player.sendMessage("Unbekannter Befehl.");
        }

        return true;
    }
}
