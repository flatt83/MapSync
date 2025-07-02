
package de.flattze.mapsync;

import de.flattze.mapsync.database.DatabaseManager;
import de.flattze.mapsync.extractor.MapExtractor;
import de.flattze.mapsync.gui.MapGUIManager;
import de.flattze.mapsync.listeners.MapListener;
import org.bukkit.plugin.java.JavaPlugin;

public class MapSyncPlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private MapExtractor mapExtractor;
    private MapGUIManager guiManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect();
        this.databaseManager.ensureTableExists();

        this.mapExtractor = new MapExtractor();
        this.guiManager = new MapGUIManager(this);

        getServer().getPluginManager().registerEvents(new MapListener(this), this);

        getLogger().info("MapSync gestartet!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MapExtractor getMapExtractor() {
        return mapExtractor;
    }

    public MapGUIManager getGuiManager() {
        return guiManager;
    }
}
