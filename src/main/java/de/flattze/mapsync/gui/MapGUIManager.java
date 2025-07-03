package de.flattze.mapsync.gui;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MapGUIManager {

    private final MapSyncPlugin plugin;

    public MapGUIManager(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    public void openOwnedMaps(Player player, int page) {
        List<MapRecord> records = plugin.getDatabaseManager().getMapsFor(player.getUniqueId());

        int itemsPerPage = 45; // 5 Reihen * 9 Slots
        int maxPage = (int) Math.ceil((double) records.size() / itemsPerPage);

        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        Inventory inv = Bukkit.createInventory(null, 54, "§7Meine Karten - Seite " + page);

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, records.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            MapRecord record = records.get(i);

            ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
            ItemMeta meta = mapItem.getItemMeta();
            meta.setDisplayName("§aKarte ID: " + record.mapId());
            mapItem.setItemMeta(meta);

            inv.setItem(slot++, mapItem);
        }

        // Blätter-Buttons
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.setDisplayName("§eVorherige Seite");
            prev.setItemMeta(meta);
            inv.setItem(45, prev);
        }

        if (page < maxPage) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName("§eNächste Seite");
            next.setItemMeta(meta);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }
}
