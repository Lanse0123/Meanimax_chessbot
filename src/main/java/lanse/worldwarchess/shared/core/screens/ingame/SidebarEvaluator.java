package lanse.worldwarchess.shared.core.screens.ingame;

import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.move.MoveContext;
import lanse.worldwarchess.shared.core.screens.background.BackgroundColors;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import javax.swing.*;
import java.awt.*;

public class SidebarEvaluator extends JPanel {

    // Positive = White is better; negative = Black is better.
    private volatile double evaluation;
    private double displayedEvaluation;

    private static final double MAX_DISPLAY_EVALUATION = 25.0;
    private static final double ANIMATION_SPEED = 0.15;

    //Timer just makes it smoother because fun
    private Timer animationTimer;

    public SidebarEvaluator() {
        setPreferredSize(new Dimension(40, 550));

        animationTimer = null; //null fixes weird error
        animationTimer = new Timer(16, _ -> {
            double difference = evaluation - displayedEvaluation;

            if (Math.abs(difference) < 0.5) {
                displayedEvaluation = evaluation;
                animationTimer.stop();
            } else {
                displayedEvaluation += difference * ANIMATION_SPEED;
            }

            repaint();
        });

        animationTimer.setCoalesce(true);
    }

    public void updateEvaluation() {
        evaluation = NPC.evaluate(MoveContext.contextify(WorldMap.board), true, false, NPC.SELECTED_AI.MINIMAX);

        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (lanse.worldwarchess.shared.core.Timer.preGameCountdown > 0) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();

        try {
            int width = getWidth();
            int height = getHeight();

            //Normalized to not go off chart
            double normalized = Math.clamp(displayedEvaluation / MAX_DISPLAY_EVALUATION, -1.0, 1.0);

            // White's share of the bar. Even evaluations split it evenly.
            int whiteBarHeight = (int) ((normalized + 1.0) * 0.5 * height);
            int blackBarHeight = height - whiteBarHeight;

            g2d.setColor(BackgroundColors.firstBackgroundColor);
            g2d.fillRect(0, 0, width, whiteBarHeight);

            g2d.setColor(BackgroundColors.secondBackgroundColor);
            g2d.fillRect(0, whiteBarHeight, width, blackBarHeight);

            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(0, 0, width - 1, height - 1);
            String evalText = String.format("%+.1f", displayedEvaluation);

            // Draw evaluation score
            g2d.setColor(Color.RED);
            int baseFontSize = 18;
            int textLength = evalText.length();
            int fontSize = Math.max(12, baseFontSize - Math.max(0, textLength - 4) * 2);

            g2d.setFont(new Font("Arial", Font.BOLD, fontSize));

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(evalText);
            int textHeight = fm.getAscent();

            // Draw top or bottom based on which side is stronger
            int y = (displayedEvaluation >= 0) ? height - 20 : textHeight + 20;

            g2d.drawString(evalText, (width - textWidth) / 2, y);
        } finally {
            g2d.dispose();
        }
    }
}