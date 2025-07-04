
package de.flattze.mapsync;

import de.flattze.mapsync.commands.MapSyncCommand;
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

        this.guiManager = new MapGUIManager(this);
        this.mapExtractor = new MapExtractor();

        getServer().getPluginManager().registerEvents(new MapListener(this), this);

        getCommand("mapsync").setExecutor(new MapSyncCommand(this));
        getCommand("mapsync").setTabCompleter(new MapSyncCommand(this));

        getLogger().info("MapSync gestartet!");
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
