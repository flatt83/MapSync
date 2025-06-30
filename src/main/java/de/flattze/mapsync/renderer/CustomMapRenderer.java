package de.flattze.mapsync.renderer;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class CustomMapRenderer extends MapRenderer {

    private final byte[] colors;

    public CustomMapRenderer(byte[] colors) {
        if (colors.length != 16384) {
            throw new IllegalArgumentException("colors[] muss 16384 Bytes haben!");
        }
        this.colors = colors;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        for (int i = 0; i < 16384; i++) {
            int x = i % 128;
            int y = i / 128;
            canvas.setPixel(x, y, colors[i]);
        }
    }
}
