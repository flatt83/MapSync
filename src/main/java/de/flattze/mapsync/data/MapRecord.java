package de.flattze.mapsync.data;

import java.util.UUID;

public record MapRecord(
        int mapId,
        UUID owner,
        String ownerName,
        String dimension,
        int scale,
        int centerX,
        int centerZ,
        boolean locked,
        boolean tracking,
        byte[] mapData
) {}