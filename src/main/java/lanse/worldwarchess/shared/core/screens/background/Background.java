package lanse.worldwarchess.shared.core.screens.background;

import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.Timer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Background {

    //This class is responsible for the math background of the game, like the gray warped plane. For each preset,
    // it will calculate each pixel cluster and get a color based on that

    private static volatile BufferedImage cachedBackground;
    private static int lastWidth, lastHeight, lastCenterX, lastCenterY;
    private static String lastBackgroundSetting;
    private static int lastTurnCount;
    private static int lastStep;

    public static void draw(int width, int height, int centerX, int centerY, Graphics2D g2d){
        // Reduce resolution for performance
        int step = 6;  // Adjust this for better performance (higher = faster)

        String bg = Settings.getBackground();

        boolean needsRegen = (((width != lastWidth || height != lastHeight ||
                centerX != lastCenterX || centerY != lastCenterY || step != lastStep ||
                !Objects.equals(bg, lastBackgroundSetting) || lastTurnCount != Timer.turnCounter)
                || cachedBackground == null));

        if (needsRegen) {
            lastStep = step;
            lastWidth = width;
            lastHeight = height;
            lastCenterX = centerX;
            lastCenterY = centerY;
            lastBackgroundSetting = bg;
            lastTurnCount = Timer.turnCounter;
            generateBackgroundCache(width, height, centerX, centerY, step, bg);
        }

        if (cachedBackground != null) {
            g2d.drawImage(cachedBackground, 0, 0, null);
        } else {
            g2d.setColor(new Color(60, 60, 60));
            g2d.fillRect(0, 0, width, height);
        }
    }

    private static void generateBackgroundCache(int width, int height, int centerX, int centerY, int step, String background) {
        new Thread(() -> {
            cachedBackground = Background.generateBackground(width, height, centerX, centerY, step, background);
            Main.gamePanel.repaint(); // request UI repaint
        }, "BackgroundRenderer").start();
    }

    private static BufferedImage generateBackground(int width, int height, int centerX, int centerY, int step, String background) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        switch (background){
            case "Warped Plane" -> WarpedPlaneBackground.drawBackground(width, height, centerX, centerY, step, g2);
            case "Ripple" -> RippleBackground.drawBackground(width, height, centerX, centerY, step, g2);
            case "Spiral" -> SpiralBackground.drawBackground(width, height, centerX, centerY, step, g2);
            case "StarField" -> StarFieldBackground.drawBackground(width, height, centerX, centerY, step, g2);
            case "Plasma" -> PlasmaBackground.drawBackground(width, height, centerX, centerY, step, g2);
            default -> {
                g2.setColor(new Color(60, 60, 60));
                g2.fillRect(0, 0, width, height);
            }
        }
        g2.dispose();
        return img;
    }

}
