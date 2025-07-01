package de.flattze.mapsync.renderer;

import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class CustomMapRenderer extends MapRenderer {

    private final byte[] colors;

    public CustomMapRenderer(byte[] colors) {
        super(true); // true = remove other renderers
        this.colors = colors;
    }

    @Override
    public void render(MapView view, MapCanvas canvas, org.bukkit.entity.Player player) {
        if (colors.length != 16384) {
            throw new IllegalArgumentException("Colors must be 16384 bytes!");
        }

        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                int index = x + y * 128;
                canvas.setPixel(x, y, colors[index]);
            }
        }

        view.setTrackingPosition(false);
        view.setLocked(true);
    }
}
