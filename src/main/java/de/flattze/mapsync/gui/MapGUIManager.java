package de.flattze.mapsync.gui;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;
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
        if (records.isEmpty()) {
            player.sendMessage("§cKeine Karten gefunden.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§7Meine Karten - Seite " + page);

        int slot = 0;
        for (MapRecord record : records) {
            ItemStack item = new ItemStack(Material.FILLED_MAP);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§aKarte ID: " + record.mapId());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
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
