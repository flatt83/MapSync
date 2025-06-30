package de.flattze.mapsync.gui;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;
import de.flattze.mapsync.util.MapMigrateUtil;
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

    public void openPlayerMapGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "§7MapSync Menü");

        ItemStack myMaps = new ItemStack(Material.MAP);
        ItemMeta meta = myMaps.getItemMeta();
        meta.setDisplayName("§aMeine Karten anzeigen");
        myMaps.setItemMeta(meta);

        inv.setItem(3, myMaps);
        player.openInventory(inv);
    }

    public void openOwnedMaps(Player player, int page) {
        List<MapRecord> records = plugin.getDatabaseManager().getMapsFor(player.getUniqueId());

        int itemsPerPage = 45; // 5 Zeilen à 9 Slots
        int maxPage = (int) Math.ceil((double) records.size() / itemsPerPage);

        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        Inventory inv = plugin.getServer().createInventory(null, 54, "§7Meine Karten - Seite " + page);

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

        // Navigation Buttons
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

    public void uploadCurrentMap(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.FILLED_MAP) {
            player.sendMessage("§cDu musst eine gefüllte Karte in der Hand halten!");
            return;
        }

        MapMeta meta = (MapMeta) item.getItemMeta();
        MapView view = meta.getMapView();
        if (view == null) {
            player.sendMessage("§cFehler: Kein MapView gefunden.");
            return;
        }

        int mapId = view.getId();
        String dimension = player.getWorld().getEnvironment().name().toLowerCase();
        int scale = view.getScale().getValue();
        int centerX = view.getCenterX();
        int centerZ = view.getCenterZ();
        boolean locked = view.isLocked();
        boolean tracking = view.isTrackingPosition();

        // ✅ Jetzt korrekt: Übergib mapId & World
        byte[] colors = plugin.getMapExtractor().extractColors(mapId, player.getWorld());

        if (colors == null || colors.length != 16384) {
            player.sendMessage("§cFehler: Farbdaten ungültig!");
            return;
        }

        MapRecord record = new MapRecord(
                mapId,
                player.getUniqueId(),
                player.getName(),
                dimension,
                scale,
                centerX,
                centerZ,
                locked,
                tracking,
                colors
        );

        plugin.getDatabaseManager().uploadMap(record);
        player.sendMessage("§aKarte erfolgreich hochgeladen!");
    }

}
