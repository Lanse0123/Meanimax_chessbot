package lanse.worldwarchess.shared.core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class RenderCache {
    //TODO - if I am making a bufferedImage texture atlas in TextureKey, this might need to change
    private static final Map<String, BufferedImage> cache = new HashMap<>();

    private static String key(String textureKey, double zoom, int w, int h) {
        return textureKey + "|" + zoom + "|" + w + "x" + h;
    }

    private static String key(String textureKey, int w, int h) {
        return textureKey + "|" + w + "x" + h;
    }

    //This will make a map of unique textures and sizes, and it will cache those to be reused. This is far more
    // efficient than having each texture for each object recache itself.
    public static BufferedImage getScaled(String textureKey, double zoomFactor, BufferedImage original, int targetWidth, int targetHeight) {
        if (original == null) return null;

        if (targetWidth <= 0 || targetHeight <= 0) {
            return original;
        }

        String cacheKey = key(textureKey, zoomFactor, targetWidth, targetHeight);
        BufferedImage cached = cache.get(cacheKey);
        if (cached != null) return cached;

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        cache.put(cacheKey, resized);
        return resized;
    }

    public static void clear() {
        cache.clear();
    }

    // Same as getScaled, but not multiplied by zoomFactor. Use only in a context when the g2d has already been scaled
    public static BufferedImage getUnscaled(String textureKey, BufferedImage original, int targetWidth, int targetHeight) {
        if (original == null) return null;

        if (targetWidth <= 0 || targetHeight <= 0) {
            return original;
        }

        String cacheKey = key(textureKey, targetWidth, targetHeight);
        BufferedImage cached = cache.get(cacheKey);
        if (cached != null) return cached;

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        cache.put(cacheKey, resized);
        return resized;

    }
}