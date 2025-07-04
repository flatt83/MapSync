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
        String db = plugin.getConfig().getString("database.database");
        String user = plugin.getConfig().getString("database.username");
        String pass = plugin.getConfig().getString("database.password");

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false",
                    user, pass
            );

            Statement stmt = connection.createStatement();
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mapsync_maps (" +
                            "map_id INT PRIMARY KEY," +
                            "owner_uuid VARCHAR(36)," +
                            "owner_name VARCHAR(100)," +
                            "dimension VARCHAR(50)," +
                            "scale INT," +
                            "center_x INT," +
                            "center_z INT," +
                            "locked BOOLEAN," +
                            "tracking BOOLEAN," +
                            "map_data LONGBLOB" +
                            ");"
            );

            plugin.getLogger().info("[MapSync] Datenbank verbunden!");

        } catch (SQLException e) {
            plugin.getLogger().severe("[MapSync] DB-Verbindung fehlgeschlagen: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("[MapSync] Datenbank getrennt.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void uploadMap(MapRecord record) {
        if (record.mapData().length != 16384) {
            throw new IllegalArgumentException("Farben müssen 16384 Bytes sein!");
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "REPLACE INTO mapsync_maps (map_id, owner_uuid, owner_name, dimension, scale, center_x, center_z, locked, tracking, map_data) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public MapRecord getMapById(int mapId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM mapsync_maps WHERE map_id = ?"
        )) {
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

    public List<MapRecord> getMapsFor(UUID uuid) {
        List<MapRecord> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM mapsync_maps WHERE owner_uuid = ?"
        )) {
            stmt.setString(1, uuid.toString());
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
