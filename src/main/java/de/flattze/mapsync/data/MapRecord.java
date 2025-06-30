package de.flattze.mapsync.data;

import java.util.UUID;

public record MapRecord(
        int mapId,
        UUID ownerUuid,
        String ownerName,
        String dimension,
        int scale,
        int centerX,
        int centerZ,
        boolean locked,
        boolean trackingPosition,
        byte[] colors
) { }
