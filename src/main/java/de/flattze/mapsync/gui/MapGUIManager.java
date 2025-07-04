package de.flattze.mapsync.gui;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;
import de.flattze.mapsync.renderer.CustomMapRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.List;

public class MapGUIManager {

    private final MapSyncPlugin plugin;

    public MapGUIManager(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    public void openOwnedMaps(Player player, int page) {
        List<MapRecord> records = plugin.getDatabaseManager().getMapsFor(player.getUniqueId());

        if (records.isEmpty()) {
            player.sendMessage("§cDu hast noch keine gespeicherten Karten!");
            return;
        }

        int itemsPerPage = 45;
        int maxPage = Math.max(1, (int) Math.ceil((double) records.size() / itemsPerPage));

        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        int startIndex = Math.max(0, (page - 1) * itemsPerPage);
        int endIndex = Math.min(startIndex + itemsPerPage, records.size());

        Inventory inv = Bukkit.createInventory(null, 54, "§7Meine Karten - Seite " + page);

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            MapRecord record = records.get(i);

            ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
            ItemMeta meta = mapItem.getItemMeta();
            meta.setDisplayName("§aKarte ID: " + record.mapId());
            mapItem.setItemMeta(meta);

            inv.setItem(slot++, mapItem);
        }

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

    public void giveMapToPlayer(Player player, MapRecord record) {
        MapView view = Bukkit.createMap(player.getWorld());

        view.setScale(MapView.Scale.values()[Math.max(0, Math.min(record.scale(), 4))]);
        view.setCenterX(record.centerX());
        view.setCenterZ(record.centerZ());
        view.setTrackingPosition(record.tracking());
        view.setLocked(record.locked());
        view.getRenderers().clear();
        view.addRenderer(new CustomMapRenderer(record.mapData()));

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapView(view);
        meta.setDisplayName("§aKarte ID: " + record.mapId());
        mapItem.setItemMeta(meta);

        player.getInventory().addItem(mapItem);
    }
}
