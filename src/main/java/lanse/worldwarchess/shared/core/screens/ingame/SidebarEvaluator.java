package lanse.worldwarchess.shared.core.screens.ingame;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SidebarEvaluator extends JPanel {

    //This will calculate the combined piece points on both sides, and just use that number as a display
    // to show which team is winning and by how many points.

    private int whiteStrength;
    private int blackStrength;

    public SidebarEvaluator() {
        setPreferredSize(new Dimension(40, 550));
    }

    public void updateEvaluation() {
        // Reset strengths
        whiteStrength = 0;
        blackStrength = 0;

        // Count strength for each team
        List<Piece> whitePieces = WorldMap.getAllPiecesOfTeam("White");
        List<Piece> blackPieces = WorldMap.getAllPiecesOfTeam("Black");

        for (Piece piece : whitePieces) {
            whiteStrength += piece.getStrength();
        }
        for (Piece piece : blackPieces) {
            blackStrength += piece.getStrength();
        }

        repaint(); // Refresh UI
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (Timer.preGameCountdown > 0) return;
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();
        int evalScore = Math.abs(whiteStrength - blackStrength);

        // Normalize bar height (avoid division by zero)
        int totalStrength = Math.max(whiteStrength + blackStrength, 1);
        int whiteBarHeight = (int) ((whiteStrength / (double) totalStrength) * height);
        int blackBarHeight = height - whiteBarHeight; // Remaining height

        // Draw bars
        g2d.setColor(GameMode.currentGamemodeColor1);
        g2d.fillRect(0, 0, width, whiteBarHeight);

        g2d.setColor(GameMode.currentGamemodeColor2);
        g2d.fillRect(0, whiteBarHeight, width, blackBarHeight);

        // Draw a small gray border
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(2)); // Border thickness
        g2d.drawRect(0, 0, width - 1, height - 1); // Border around the sidebar

        // Draw evaluation score
        // Draw evaluation score
        g2d.setColor(Color.RED);

        String evalText = String.valueOf(evalScore);

        // Base font size
        int baseFontSize = 18;

        // Shrink font size for longer text (starts scaling after 3 digits)
        int textLength = evalText.length();
        int adjustedFontSize = Math.max(12, baseFontSize - Math.max(0, textLength - 3) * 2);

        g2d.setFont(new Font("Arial", Font.BOLD, adjustedFontSize));

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(evalText);
        int textHeight = fm.getAscent();

        // Draw top or bottom based on which side is stronger
        int y = (whiteStrength <= blackStrength) ? (height - 20) : (textHeight + 20);
        g2d.drawString(evalText, (width - textWidth) / 2, y);
    }
}
