package lanse.worldwarchess.shared.core;

import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.marker.Marker;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.background.BackgroundColors;
import lanse.worldwarchess.shared.core.screens.history.HistoryPanel;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.screens.ingame.SidebarMenu;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.util.ArrayList;

public class Timer {
    public static double maxTimePerTurn;
    public static double currentTime;
    public static int turnCounter = 0;
    public static ArrayList<Integer> playerIDMovedList = new ArrayList<>();
    public static double preGameCountdown = 3.0;
    public static boolean gameStarted = false;
    public static boolean isGamePaused = false;
    private static int lastSecondPlayed = -1;
    public static boolean skipTurn = false;

    public static void count() {
        // pregame countdown
        if (!gameStarted) {
            preGameCountdown -= 0.1;

            //play preGame sound each second
            int currentSecond = (int) Math.ceil(preGameCountdown);
            if (currentSecond != lastSecondPlayed && preGameCountdown > 0.5) {
                lastSecondPlayed = currentSecond;
                SoundManager.preGame();
            }

            // end countdown logic
            if (preGameCountdown <= 0) {
                gameStarted = true;
                currentTime = maxTimePerTurn;
                GamePanel.sidebarEvaluator.updateEvaluation();
            }
            return;
        }

        if (Main.gamePanel instanceof HistoryPanel) return;

        if (currentTime <= 0) {
            doHostLogic();
        }
    }

    private static void doHostLogic(){
        NPC.recalculateAttackerLocations();
        currentTime = maxTimePerTurn;
        turnCounter++;
        playerIDMovedList.clear();
        GamePanel.sidebarEvaluator.updateEvaluation();
        WorldMap.tickBoards();
        WorldMap.resetPieces();
        Marker.tickMarkers();
        WorldMap.tickMinionRequests();

        MoveHistory.saveData();
        MoveHistory.addNewTurn();

        if (turnCounter % 2 == 1){
            NPC.doAllNpcLogic("Black", "White",true, true);
        }
        else if (turnCounter % 2 == 0) {
            NPC.doAllNpcLogic("White", "Black", false,false);
        }
        Main.gamePanel.recalculateLegalMoves();
    }

    private static void doClientLogic(){
        NPC.recalculateAttackerLocations();
        currentTime = maxTimePerTurn;
        turnCounter++;
        playerIDMovedList.clear();
        GamePanel.sidebarEvaluator.updateEvaluation();
        WorldMap.resetPieces();
        Marker.tickMarkers();

        MoveHistory.saveData();
        MoveHistory.addNewTurn();

        skipTurn = false;
        Main.gamePanel.recalculateLegalMoves();
    }

    //This is responsible for drawing the timer box and timer itself. everything TIMER RELATED.
    // I tried moving it to a new thread for optimization but that broke it a lot...
    public static void draw(Graphics g, int width, int height, SidebarMenu sidebarMenu){
        if (!Timer.gameStarted) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            String countdownText = "Game starts in: " + (int) Math.ceil(Timer.preGameCountdown);
            int textWidth = g.getFontMetrics().stringWidth(countdownText);
            g.drawString(countdownText, (width - textWidth) / 2, height / 2);
            return;
        }

        String team2 = "Black's";

        String turnText = (Timer.turnCounter % 2 == 0) ? "White's turn" : team2 + " turn";
        boolean isWhiteTurn = Timer.turnCounter % 2 == 0;

        // Set colors based on turn
        Color boxColor = isWhiteTurn ? BackgroundColors.firstBackgroundColor : BackgroundColors.secondBackgroundColor;
        Color textColor = isWhiteTurn ? BackgroundColors.firstBackgroundColor : BackgroundColors.secondBackgroundColor;
        Color borderColor = new Color(150, 150, 150); // Gray border

        int boxX = width - 200;
        int boxY = 20;
        int boxWidth = 180;
        int boxHeight = 70;

        if (sidebarMenu.isOpen) boxX = width - 350;

        // Draw box with border
        g.setColor(borderColor);
        g.fillRect(boxX - 2, boxY - 2, boxWidth + 4, boxHeight + 4); // Border
        g.setColor(boxColor);
        g.fillRect(boxX, boxY, boxWidth, boxHeight); // Main box

        // Set font
        g.setFont(new Font("Arial", Font.BOLD, 18));
        int textX = boxX + 10;
        int textY1 = boxY + 25;
        int textY2 = boxY + 50;

        // Draw text with outline for better readability
        g.setColor(Color.GRAY);
        g.drawString(turnText, textX + 1, textY1 + 1);
        g.drawString("Time left: " + String.format("%.1f", Timer.currentTime) + "s", textX + 1, textY2 + 1);

        g.setColor(textColor);
        g.drawString(turnText, textX, textY1);
        g.drawString("Time left: " + String.format("%.1f", Timer.currentTime) + "s", textX, textY2);
    }
}