package de.flattze.mapsync.listeners;

import de.flattze.mapsync.MapSyncPlugin;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class MapAutoUploadListener implements Listener {

    private final MapSyncPlugin plugin;

    public MapAutoUploadListener(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPlaceMap(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) return;

        Player player = event.getPlayer();
        if (!plugin.getConfig().getBoolean("settings.auto-upload-enabled", true)) return;

        if (player.getInventory().getItemInMainHand().getType() == Material.FILLED_MAP) {
            plugin.getGuiManager().uploadCurrentMap(player);
            player.sendMessage("§aAutomatischer Upload ausgeführt!");
        }
    }
}
