package de.flattze.mapsync.renderer;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class MapCaptureRenderer extends MapRenderer {

    private final BufferedImage image;

    public MapCaptureRenderer(BufferedImage image) {
        super(true); // Nur einmal rendern
        this.image = image;
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        // Das gespeicherte Bild auf die Karte zeichnen
        canvas.drawImage(0, 0, image);
    }
}
