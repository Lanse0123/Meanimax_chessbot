package lanse.worldwarchess.shared.core.tutorial;

import lanse.worldwarchess.shared.central.TextureKey;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class TutorialTextBubble {

    public enum Type { TEXT }

    private final String text;
    private final Rectangle bounds;

    public TutorialTextBubble(String text, Type type, int x, int y, int width, int height) {
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void onClicked() {
        Tutorial.requestAdvanceByUI();
    }

    public void draw(Graphics2D g2d) {
        // draw independent of zoom
        AffineTransform oldTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform());

        BufferedImage texture = TextureKey.textures.get("TextBubble");

        if (texture != null) {
            g2d.drawImage(texture, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        }

        // text
        g2d.setColor(Color.RED);
        Font font = new Font("Arial", Font.BOLD, 18);
        g2d.setFont(font);

        String[] lines = text.split("\n");
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int textY = bounds.y + (bounds.height - lines.length * lineHeight) / 2 + fm.getAscent();

        for (String line : lines) {
            int textWidth = fm.stringWidth(line);
            int textX = bounds.x + (bounds.width - textWidth) / 2;
            g2d.drawString(line, textX, textY);
            textY += lineHeight;
        }

        g2d.setTransform(oldTransform);
    }
}