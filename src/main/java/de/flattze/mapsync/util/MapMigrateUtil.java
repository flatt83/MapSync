package de.flattze.mapsync.util;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.stream.NBTInputStream;
import de.flattze.mapsync.MapSyncPlugin;

import java.awt.image.BufferedImage;
import java.io.*;

public class MapMigrateUtil {

    private final MapSyncPlugin plugin;

    public MapMigrateUtil(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    private byte[] readColorsWithJNBT(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             NBTInputStream nbtIn = new NBTInputStream(fis)) { // ❌ ohne GZIPInputStream

            CompoundTag root = (CompoundTag) nbtIn.readTag();
            CompoundTag dataTag = (CompoundTag) root.getValue().get("data");
            ByteArrayTag colorsTag = (ByteArrayTag) dataTag.getValue().get("colors");
            return colorsTag.getValue();
        }
    }


    private BufferedImage createImageFromColors(byte[] colors) {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int index = y * 128 + x;
                int color = colors[index] & 0xFF;
                int rgb = (color << 16) | (color << 8) | color; // Simple grayscale mapping
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }
}
