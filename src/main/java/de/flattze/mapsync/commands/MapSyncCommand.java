package de.flattze.mapsync.commands;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;
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
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§7Verfügbare Befehle: /mapsync upload | gui | migrate | mymaps");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "upload" -> {
                player.sendMessage("§cStarte den Upload-Prozess!");
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.FILLED_MAP) {
                    player.sendMessage("§cDu musst eine FILLED_MAP in der Hand halten!");
                    return true;
                }
                player.sendMessage("§c1!");
                MapMeta meta = (MapMeta) item.getItemMeta();
                MapView view = meta.getMapView();
                if (view == null) {
                    player.sendMessage("§cKeine MapView gefunden.");
                    return true;
                }
                player.sendMessage("§c2!");
                // Hole deinen Renderer
                CustomMapRenderer myRenderer = null;
                for (MapRenderer renderer : view.getRenderers()) {
                    if (renderer instanceof CustomMapRenderer custom) {
                        myRenderer = custom;
                        break;
                    }
                }
                player.sendMessage("§c3!");
                if (myRenderer == null) {
                    player.sendMessage("§cDie Karte wurde nicht mit MapSync erstellt! Kein CustomRenderer gefunden.");
                    return true;
                }
                player.sendMessage("§c4!");
                byte[] colors = myRenderer.getColors();
                player.sendMessage("§7Farbenlänge: " + colors.length);
                player.sendMessage("§c5!");
                // Speichern
                MapRecord record = new MapRecord(
                        view.getId(),
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
                player.sendMessage("§c6!");
                plugin.getDatabaseManager().uploadMap(record);
                player.sendMessage("§aUpload abgeschlossen!");
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
