package de.flattze.mapsync.extractor;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class MapExtractor {

    public byte[] extractColors(int mapId, World world) {
        File file = new File(world.getWorldFolder(), "data/map_" + mapId + ".dat");
        if (!file.exists()) {
            System.out.println("[MapSync] map_" + mapId + ".dat nicht gefunden");
            return null;
        }

        try (NBTInputStream nbtIn = new NBTInputStream(new FileInputStream(file))) {
            CompoundTag rootTag = (CompoundTag) nbtIn.readTag();
            Map<String, Tag<?>> root = rootTag.getValue();

            CompoundTag dataTag = (CompoundTag) root.get("data");
            Map<String, Tag<?>> data = dataTag.getValue();

            ByteArrayTag colorsTag = (ByteArrayTag) data.get("colors");
            byte[] colors = colorsTag.getValue();

            if (colors.length != 16384) {
                throw new IllegalArgumentException("Farben müssen 16384 Bytes sein! Tatsächlich: " + colors.length);
            }

            return colors;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
