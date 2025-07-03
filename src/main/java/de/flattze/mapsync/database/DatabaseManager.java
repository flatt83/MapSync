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

    public void ensureTableExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS mapsync_maps (
                  map_id INT PRIMARY KEY,
                  owner_uuid VARCHAR(36) NOT NULL,
                  owner_name VARCHAR(64) NOT NULL,
                  dimension VARCHAR(32) NOT NULL,
                  scale INT NOT NULL,
                  center_x INT NOT NULL,
                  center_z INT NOT NULL,
                  locked BOOLEAN NOT NULL,
                  tracking BOOLEAN NOT NULL,
                  map_data BLOB NOT NULL
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der Tabelle: " + e.getMessage());
        }
    }

    public void uploadMap(MapRecord record) {
        if (record.mapData().length != 16384) {
            throw new IllegalArgumentException("uploadMap: Farben müssen 16384 Bytes sein!");
        }

        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "REPLACE INTO mapsync_maps " +
                            "(map_id, owner_uuid, owner_name, dimension, scale, center_x, center_z, locked, tracking, map_data) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, record.mapId());
            stmt.setString(2, record.owner());
            stmt.setString(3, record.ownerName());
            stmt.setString(4, record.dimension());
            stmt.setInt(5, record.scale());
            stmt.setInt(6, record.centerX());
            stmt.setInt(7, record.centerZ());
            stmt.setBoolean(8, record.locked());
            stmt.setBoolean(9, record.tracking());
            stmt.setBytes(10, record.mapData());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<MapRecord> getMapsFor(UUID owner) {
        List<MapRecord> result = new ArrayList<>();
        String sql = "SELECT * FROM mapsync_maps WHERE owner_uuid = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, owner.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new MapRecord(
                            rs.getInt("map_id"),
                            UUID.fromString(rs.getString("owner_uuid")),
                            rs.getString("owner_name"),
                            rs.getString("dimension"),
                            rs.getInt("scale"),
                            rs.getInt("center_x"),
                            rs.getInt("center_z"),
                            rs.getBoolean("locked"),
                            rs.getBoolean("tracking"),
                            rs.getBytes("map_data")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Abrufen fehlgeschlagen: " + e.getMessage());
        }

        return result;
    }

    public MapRecord getMapById(int mapId) {
        String sql = "SELECT * FROM mapsync_maps WHERE map_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, mapId);
            try (ResultSet rs = ps.executeQuery()) {
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
                            rs.getBoolean("tracking"),
                            rs.getBytes("map_data")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Abrufen fehlgeschlagen: " + e.getMessage());
        }

        return null;
    }
}
