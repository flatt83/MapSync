package de.flattze.mapsync.extractor;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class MapExtractor {

    public byte[] extractColors(int mapId, World world) {
        File mapFile = new File(world.getWorldFolder(), "data/map_" + mapId + ".dat");
        if (!mapFile.exists()) {
            Bukkit.getLogger().warning("[MapSync] map_" + mapId + ".dat nicht gefunden.");
            return new byte[16384];
        }

        try (NBTInputStream nbtIn = new NBTInputStream(new FileInputStream(mapFile))) {
            CompoundTag rootTag = (CompoundTag) nbtIn.readTag();
            Map<String, Tag<?>> rootMap = rootTag.getValue();

            CompoundTag dataTag = (CompoundTag) rootMap.get("data");
            Map<String, Tag<?>> dataMap = dataTag.getValue();

            ByteArrayTag colorsTag = (ByteArrayTag) dataMap.get("colors");
            byte[] colors = colorsTag.getValue();

            if (colors.length != 16384) {
                throw new IllegalArgumentException("Farben sind nicht 16384 Bytes!");
            }

            return colors;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[16384];
    }
}
