package de.flattze.mapsync.database;

import de.flattze.mapsync.MapSyncPlugin;
import de.flattze.mapsync.data.MapRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final MapSyncPlugin plugin;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public DatabaseManager(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        host = plugin.getConfig().getString("database.host");
        port = plugin.getConfig().getInt("database.port");
        database = plugin.getConfig().getString("database.database");
        username = plugin.getConfig().getString("database.username");
        password = plugin.getConfig().getString("database.password");
        plugin.getLogger().info("[MapSync] DB-Config geladen.");
    }

    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(url, username, password);
    }

    public void ensureTableExists() {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS mapsync_maps (" +
                            "map_id INT PRIMARY KEY," +
                            "owner_uuid VARCHAR(36) NOT NULL," +
                            "owner_name VARCHAR(64) NOT NULL," +
                            "dimension VARCHAR(32) NOT NULL," +
                            "scale INT NOT NULL," +
                            "center_x INT NOT NULL," +
                            "center_z INT NOT NULL," +
                            "locked BOOLEAN NOT NULL," +
                            "tracking BOOLEAN NOT NULL," +
                            "map_data BLOB NOT NULL" +
                            ")"
            );
            stmt.execute();
            plugin.getLogger().info("[MapSync] Tabelle geprüft/erstellt.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void uploadMap(MapRecord record) {
        if (record.mapData().length != 16384) {
            throw new IllegalArgumentException("Farben müssen 16384 Bytes sein!");
        }
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "REPLACE INTO mapsync_maps " +
                            "(map_id, owner_uuid, owner_name, dimension, scale, center_x, center_z, locked, tracking, map_data) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, record.mapId());
            stmt.setString(2, record.owner().toString());
            stmt.setString(3, record.ownerName());
            stmt.setString(4, record.dimension());
            stmt.setInt(5, record.scale());
            stmt.setInt(6, record.centerX());
            stmt.setInt(7, record.centerZ());
            stmt.setBoolean(8, record.locked());
            stmt.setBoolean(9, record.tracking());
            stmt.setBytes(10, record.mapData());
            stmt.executeUpdate();
            plugin.getLogger().info("[MapSync] Map-ID " + record.mapId() + " gespeichert.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public MapRecord getMapById(int mapId) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM mapsync_maps WHERE map_id = ?"
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
                        rs.getBoolean("tracking"),
                        rs.getBytes("map_data")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MapRecord> getMapsFor(UUID owner) {
        List<MapRecord> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM mapsync_maps WHERE owner_uuid = ?"
            );
            stmt.setString(1, owner.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new MapRecord(
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
