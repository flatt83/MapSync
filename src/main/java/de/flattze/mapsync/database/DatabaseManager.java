package de.flattze.mapsync.database;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final MapSyncPlugin plugin;
    private Connection connection;

    public DatabaseManager(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String host = plugin.getConfig().getString("database.host");
        int port = plugin.getConfig().getInt("database.port");
        String database = plugin.getConfig().getString("database.database");
        String username = plugin.getConfig().getString("database.username");
        String password = plugin.getConfig().getString("database.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        try {
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Datenbank verbunden!");
            checkAndCreateTable();
        } catch (SQLException e) {
            plugin.getLogger().severe("DB-Verbindung fehlgeschlagen: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("DB-Verbindung getrennt.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkAndCreateTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS mapsync_maps (
                  map_id INT PRIMARY KEY,
                  owner_uuid VARCHAR(36) NOT NULL,
                  owner_name VARCHAR(100) NOT NULL,
                  dimension VARCHAR(64) NOT NULL,
                  scale INT NOT NULL,
                  center_x INT NOT NULL,
                  center_z INT NOT NULL,
                  locked BOOLEAN NOT NULL,
                  tracking BOOLEAN NOT NULL,
                  colors BLOB NOT NULL
                )
            """);
            plugin.getLogger().info("[MapSync] Tabelle 'maps' geprüft oder erstellt.");
        } catch (SQLException e) {
            plugin.getLogger().severe("[MapSync] Fehler beim Tabellen-Check: " + e.getMessage());
        }
    }

    public void uploadMap(MapRecord record) {
        String sql = "INSERT INTO mapsync_maps " +
                "(map_id, owner_uuid, owner_name, dimension, scale, center_x, center_z, locked, tracking, map_data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE owner_name = VALUES(owner_name), map_data = VALUES(map_data)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, record.mapId());
            ps.setString(2, record.owner().toString());
            ps.setString(3, record.ownerName());
            ps.setString(4, record.dimension());
            ps.setInt(5, record.scale());
            ps.setInt(6, record.centerX());
            ps.setInt(7, record.centerZ());
            ps.setBoolean(8, record.locked());
            ps.setBoolean(9, record.tracking());
            ps.setBytes(10, record.mapData());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Upload fehlgeschlagen: " + e.getMessage());
        }
    }

    public MapRecord getMapById(int mapId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM maps WHERE map_id = ?"
            );
            stmt.setInt(1, mapId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new MapRecord(
                        rs.getInt("map_id"),
                        UUID.fromString(rs.getString("owner_uuid")),
                        rs.getString("owner_name"),
                        rs.getString("dimension"),
                        rs.getInt("scale"),
                        rs.getInt("center_x"),
                        rs.getInt("center_z"),
                        rs.getBoolean("locked"),
                        rs.getBoolean("tracking_position"),
                        rs.getBytes("colors")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[MapSync] Fehler bei getMapById: " + e.getMessage());
        }
        return null;
    }

    public List<MapRecord> getMapsFor(UUID ownerId) {
        List<MapRecord> maps = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM maps WHERE owner_uuid = ?"
            );
            stmt.setString(1, ownerId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                maps.add(new MapRecord(
                        rs.getInt("map_id"),
                        UUID.fromString(rs.getString("owner_uuid")),
                        rs.getString("owner_name"),
                        rs.getString("dimension"),
                        rs.getInt("scale"),
                        rs.getInt("center_x"),
                        rs.getInt("center_z"),
                        rs.getBoolean("locked"),
                        rs.getBoolean("tracking_position"),
                        rs.getBytes("colors")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[MapSync] Fehler bei getMapsFor: " + e.getMessage());
        }
        return maps;
    }
}
