package de.flattze.mapsync.listeners;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;
import de.flattze.mapsync.renderer.CustomMapRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class MapListener implements Listener {

    private final MapSyncPlugin plugin;

    public MapListener(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.startsWith("§7Meine Karten")) return;

        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;

        boolean clickInMenu = clickedInv.equals(event.getView().getTopInventory());
        if (clickInMenu) {
            event.setCancelled(true);
        } else {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int currentPage = 1;
        if (title.contains("Seite")) {
            String pagePart = title.substring(title.lastIndexOf(" ") + 1);
            try {
                currentPage = Integer.parseInt(pagePart);
            } catch (NumberFormatException ignored) {}
        }

        if (clicked.getType() == Material.ARROW) {
            if (clicked.getItemMeta().getDisplayName().contains("Vorherige")) {
                plugin.getGuiManager().openOwnedMaps(player, currentPage - 1);
            } else if (clicked.getItemMeta().getDisplayName().contains("Nächste")) {
                plugin.getGuiManager().openOwnedMaps(player, currentPage + 1);
            }
            return;
        }

        if (clicked.getType() == Material.FILLED_MAP) {
            int mapId = Integer.parseInt(clicked.getItemMeta().getDisplayName().replace("§aKarte ID: ", ""));
            MapRecord record = plugin.getDatabaseManager().getMapById(mapId);

            if (record != null) {
                plugin.getGuiManager().giveMapToPlayer(player, record);
                player.sendMessage("§aKarte " + mapId + " wurde deinem Inventar hinzugefügt.");
                player.closeInventory();
            } else {
                player.sendMessage("§cKarte nicht gefunden!");
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.startsWith("§7Meine Karten")) return;

        if (event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize())) {
            event.setCancelled(true);
        }
    }
}
