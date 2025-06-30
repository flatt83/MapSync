package de.flattze.mapsync;

import de.flattze.mapsync.commands.MapSyncCommand;
import de.flattze.mapsync.database.DatabaseManager;
import de.flattze.mapsync.extractor.MapExtractor;
import de.flattze.mapsync.gui.MapGUIManager;
import de.flattze.mapsync.listeners.MapListener;
import org.bukkit.plugin.java.JavaPlugin;

public class MapSyncPlugin extends JavaPlugin {

    private static MapSyncPlugin instance;
    private DatabaseManager databaseManager;
    private MapGUIManager guiManager;
    private MapExtractor mapExtractor;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect();

        this.guiManager = new MapGUIManager(this);
        this.mapExtractor = new MapExtractor();
        getCommand("mapsync").setExecutor(new MapSyncCommand(this));

        getServer().getPluginManager().registerEvents(new MapListener(this), this);

        getLogger().info("MapSync aktiviert.");

    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
            getLogger().info("[MapSync] Datenbank-Verbindung geschlossen.");
        }
    }


    public static MapSyncPlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MapGUIManager getGuiManager() {
        return guiManager;
    }

    public MapExtractor getMapExtractor() {
        return mapExtractor;
    }
}
