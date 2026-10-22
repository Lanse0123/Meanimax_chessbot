package lanse.worldwarchess.shared.core.screens.background;

import java.awt.*;
import java.util.List;

public class SpiralBackground {

    public static void drawBackground(int width, int height, int centerX, int centerY, int step, Graphics2D g2d) {
        List<Color> colors = BackgroundColors.getBackgroundColors();
        Color color1 = colors.get(0);
        Color color2 = colors.get(1);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                double dx = x - centerX;
                double dy = y - centerY;
                double angle = Math.atan2(dy, dx);
                double distance = Math.sqrt(dx * dx + dy * dy);
                int spiralBand = (int) ((distance / 20) + (angle * 5)) % 2;
                g2d.setColor(spiralBand == 0 ? color1 : color2);
                g2d.fillRect(x, y, step, step);
            }
        }
    }
}
