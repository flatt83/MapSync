package de.flattze.mapsync.commands;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class MapSyncCommand implements CommandExecutor {

    private final MapSyncPlugin plugin;

    public MapSyncCommand(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§7Verfügbare Befehle: /mapsync upload | gui | migrate | mymaps");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "upload" -> {
                player.sendMessage("§7Starte Upload für Karte in deiner Hand...");

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

                int mapId = view.getId();
                player.sendMessage("§7MapView ID: " + mapId);

                byte[] colors = plugin.getMapExtractor().extractColors(mapId, player.getWorld());
                player.sendMessage("§7Farbenlänge: " + colors.length);

                // Erstelle Record
                MapRecord record = new MapRecord(
                        mapId,
                        player.getUniqueId(),
                        player.getName(),
                        player.getWorld().getName(),
                        view.getScale().getValue(),
                        view.getCenterX(),
                        view.getCenterZ(),
                        view.isLocked(),
                        view.isTrackingPosition(),
                        colors
                );

                plugin.getDatabaseManager().uploadMap(record);
                player.sendMessage("§aUpload erfolgreich!");
            }

            case "gui" -> plugin.getGuiManager().openPlayerMapGUI(player);
            case "mymaps" -> {
                plugin.getLogger().info("Rufe UI auf!");
                plugin.getGuiManager().openOwnedMaps(player, 1);
            }
            default -> player.sendMessage("§cUnbekannter Subbefehl.");
        }
        return true;
    }
}
