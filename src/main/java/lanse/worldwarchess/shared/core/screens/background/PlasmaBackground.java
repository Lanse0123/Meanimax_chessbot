package lanse.worldwarchess.shared.core.screens.background;

import java.awt.*;
import java.util.List;

public class PlasmaBackground {

    public static void drawBackground(int width, int height, int centerX, int centerY, int step, Graphics2D g2d) {
        List<Color> colors = BackgroundColors.getBackgroundColors();
        Color color1 = colors.get(0);
        Color color2 = colors.get(1);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                double value = Math.sin(x * 0.05) + Math.sin(y * 0.07) + Math.sin((x + y) * 0.03);
                g2d.setColor(value > 0 ? color1 : color2);
                g2d.fillRect(x, y, step, step);
            }
        }
    }
}
