package lanse.worldwarchess.shared.core.screens.background;

import java.awt.*;
import java.util.List;

public class WarpedPlaneBackground {

    public static void drawBackground(int width, int height, int centerX, int centerY, int step, Graphics2D g2d){
        int checkerSize = 160;
        List<Color> colors = BackgroundColors.getBackgroundColors();
        Color color1 = colors.get(0);
        Color color2 = colors.get(1);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                double dx = x - centerX;
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double angle = Math.atan2(dy, dx);
                double swirlFactor = Math.log1p(distance) * 1.5;
                double distortedAngle = angle + swirlFactor;
                double wave = (Math.sin(distortedAngle * 6) * 0.5 + 0.5);
                int checkerX = (int) ((dx / checkerSize) + wave * 5) % 2;
                int checkerY = (int) ((dy / checkerSize) + wave * 5) % 2;
                boolean isBlack = (checkerX + checkerY) % 2 == 0;
                g2d.setColor(isBlack ? color1 : color2);
                g2d.fillRect(x, y, step, step);
            }
        }
    }

}
