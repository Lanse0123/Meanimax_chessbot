package lanse.worldwarchess.shared.core.screens.background;

import java.awt.*;
import java.util.List;

public class StarFieldBackground {

    public static void drawBackground(int width, int height, int centerX, int centerY, int step, Graphics2D g2d) {
        List<Color> colors = BackgroundColors.getBackgroundColors();
        Color background = colors.get(0);
        Color starColor = colors.get(1);
        g2d.setColor(background);
        g2d.fillRect(0, 0, width, height);
        int offsetX = centerX / step;
        int offsetY = centerY / step;

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int worldX = (x / step) + offsetX;
                int worldY = (y / step) + offsetY;
                int h = worldX * 374761393 + worldY * 668265263;
                h = (h ^ (h >> 13)) * 1274126177;
                int rnd = h ^ (h >> 16);
                double chance = (rnd & 0xFFFF) / 65535.0;
                if (chance < 0.01) {
                    int brightness = 200 + (rnd % 56);
                    g2d.setColor(new Color(
                            Math.min(255, starColor.getRed() * brightness / 255),
                            Math.min(255, starColor.getGreen() * brightness / 255),
                            Math.min(255, starColor.getBlue() * brightness / 255)));
                    int size = (rnd >> 16) % 6 + 4;
                    g2d.fillRect(x, y, size, size);
                }
            }
        }
    }
}
