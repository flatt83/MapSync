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
        try {
            String url = "jdbc:mysql://" + plugin.getConfig().getString("database.host")
                    + ":" + plugin.getConfig().getInt("database.port")
                    + "/" + plugin.getConfig().getString("database.database")
                    + "?useSSL=false&autoReconnect=true";

            String user = plugin.getConfig().getString("database.username");
            String pass = plugin.getConfig().getString("database.password");

            this.connection = DriverManager.getConnection(url, user, pass);
            plugin.getLogger().info("[MapSync] Verbindung zur Datenbank hergestellt.");

            checkAndCreateTable();

        } catch (SQLException e) {
            plugin.getLogger().severe("[MapSync] Fehler bei DB-Verbindung: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("[MapSync] Datenbank-Verbindung geschlossen.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[MapSync] Fehler beim Schließen: " + e.getMessage());
        }
    }

    public void checkAndCreateTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS maps (
                  map_id INT PRIMARY KEY,
                  owner_uuid VARCHAR(36) NOT NULL,
                  owner_name VARCHAR(100) NOT NULL,
                  dimension VARCHAR(64) NOT NULL,
                  scale INT NOT NULL,
                  center_x INT NOT NULL,
                  center_z INT NOT NULL,
                  locked BOOLEAN NOT NULL,
                  tracking_position BOOLEAN NOT NULL,
                  colors BLOB NOT NULL
                )
            """);
            plugin.getLogger().info("[MapSync] Tabelle 'maps' geprüft oder erstellt.");
        } catch (SQLException e) {
            plugin.getLogger().severe("[MapSync] Fehler beim Tabellen-Check: " + e.getMessage());
        }
    }

    public void uploadMap(MapRecord map) {
        try {
            if (map.colors().length != 16384) {
                throw new IllegalArgumentException("uploadMap: Farben müssen 16384 Bytes sein!");
            }

            PreparedStatement stmt = connection.prepareStatement("""
                INSERT INTO maps (
                    map_id, owner_uuid, owner_name, dimension, scale,
                    center_x, center_z, locked, tracking_position, colors
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    owner_uuid = VALUES(owner_uuid),
                    owner_name = VALUES(owner_name),
                    dimension = VALUES(dimension),
                    scale = VALUES(scale),
                    center_x = VALUES(center_x),
                    center_z = VALUES(center_z),
                    locked = VALUES(locked),
                    tracking_position = VALUES(tracking_position),
                    colors = VALUES(colors)
            """);

            stmt.setInt(1, map.mapId());
            stmt.setString(2, map.ownerUuid().toString());
            stmt.setString(3, map.ownerName());
            stmt.setString(4, map.dimension());
            stmt.setInt(5, map.scale());
            stmt.setInt(6, map.centerX());
            stmt.setInt(7, map.centerZ());
            stmt.setBoolean(8, map.locked());
            stmt.setBoolean(9, map.trackingPosition());
            stmt.setBytes(10, map.colors());

            stmt.executeUpdate();

            plugin.getLogger().info("[MapSync] Karte " + map.mapId() + " gespeichert.");

        } catch (SQLException e) {
            plugin.getLogger().severe("[MapSync] Fehler bei uploadMap: " + e.getMessage());
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
