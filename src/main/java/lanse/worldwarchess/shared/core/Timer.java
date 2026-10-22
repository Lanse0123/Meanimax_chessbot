package lanse.worldwarchess.shared.core;

import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.central.Returner;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.piece.AttackOnTitans;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.marker.Marker;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.history.HistoryPanel;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.screens.ingame.SidebarMenu;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.tutorial.Tutorial;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class Timer {
    public static double maxTimePerTurn;
    public static double currentTime;
    public static int turnCounter = 0;
    public static ArrayList<Integer> playerIDMovedList = new ArrayList<>();
    public static double preGameCountdown = 10.0;
    public static boolean gameStarted = false;
    public static boolean isGamePaused = false;
    private static int lastSecondPlayed = -1;
    public static boolean skipTurn = false;
    public static String currentFunFact;

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

                if (GameMode.isTutorial && Tutorial.phase == -404) {
                    Tutorial.startTutorial();
                }
            }
            return;
        }

        if (GameMode.isTutorial && Tutorial.phase == 0xABC) {
            Returner.returnToMainMenu();
        }

        if (!(GameMode.isTutorial && Tutorial.staticPhases.contains(Tutorial.phase))) {
            currentTime -= 0.1;
        }

        if (Main.gamePanel instanceof HistoryPanel) return;

        if (currentTime <= 0) {
            if (GameMode.isSinglePlayer || Main.userIsHost) {
                doHostLogic();
            } else {
                if (skipTurn){
                    doClientLogic();
                } else {
                    currentTime = 0;
                }
            }
        }
    }

    private static void doHostLogic(){
        if (!GameMode.isSinglePlayer) {
            CascadePacketManager.manager.sendPacketToEveryone(CascadePacketManager.PacketType.SKIP_TURN, "", Optional.empty());
        }

        NPC.stopAllNpcMovement();
        NPC.recalculateAttackerLocations();
        currentTime = maxTimePerTurn;
        turnCounter++;
        playerIDMovedList.clear();
        AttackOnTitans.resolveAttacksOnTitans();
        GamePanel.sidebarEvaluator.updateEvaluation();
        WorldMap.tickBoards();
        WorldMap.tickAttacks();
        WorldMap.resetPieces();
        Marker.tickMarkers();
        WorldMap.tickMinionRequests();
        WorldMap.healAutoHealables();

        MoveHistory.saveData();
        MoveHistory.addNewTurn();

        if (turnCounter % 2 == 1){
            NPC.doAllNpcLogic("Black", "White",true, true);
            if (!GameMode.isSinglePlayer){
                if (CascadingClient.team.equalsIgnoreCase("Black")) SoundManager.newTurn();
            }
        }
        else if (turnCounter % 2 == 0) {
            NPC.doAllNpcLogic("White", "Black", false,false);

            if (!GameMode.isSinglePlayer) {
                if (CascadingClient.team.equalsIgnoreCase("White")) SoundManager.newTurn();
            }
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
        WorldMap.healAutoHealables();

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

            // FUN FACT :)
            Font funFactFont = new Font("Arial", Font.PLAIN, 14);
            g.setFont(funFactFont);
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            int funFactWidth = fm.stringWidth(currentFunFact);
            int padding = 160;
            int usableWidth = width - sidebarMenu.getWidth();
            int funFactX = (usableWidth - funFactWidth) / 2;
            int funFactY = height - padding;
            g.drawString(currentFunFact, funFactX, funFactY);
            return;
        }

        String team2 = "Black's";

        //Turn UI painter
        if (!Objects.equals(GameMode.getCurrentGameModeAsString(), "War")) {
            team2 = GameMode.getCurrentGameModeAsString();
        }

        String turnText = (Timer.turnCounter % 2 == 0) ? "White's turn" : team2 + " turn";
        boolean isWhiteTurn = Timer.turnCounter % 2 == 0;

        // Set colors based on turn
        Color boxColor = isWhiteTurn ? GameMode.currentGamemodeColor1 : GameMode.currentGamemodeColor2;
        Color textColor = isWhiteTurn ? GameMode.currentGamemodeColor2 : GameMode.currentGamemodeColor1;
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

        //TODO - move this code to a new location. It is responsible for drawing the box next to the timer that tells
        // you what team you are on if its multiplayer.
        if (!GameMode.isSinglePlayer) {
            String playerTeam = CascadingClient.team;

            Font teamFont = new Font("Arial", Font.BOLD, 16);
            g.setFont(teamFont);
            FontMetrics fm = g.getFontMetrics(teamFont);

            String text = "Your Team: " + playerTeam;
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();

            int paddingX = 16;
            int paddingY = 10;
            int teamBoxWidth = textWidth + paddingX * 2;
            int teamBoxHeight = textHeight + paddingY;

            int teamBoxX = boxX - teamBoxWidth - 20;
            int teamBoxY = boxY + (boxHeight - teamBoxHeight) / 2;

            Color teamBoxColor = playerTeam.equalsIgnoreCase("White") ? GameMode.currentGamemodeColor1 : GameMode.currentGamemodeColor2;
            Color teamTextColor = playerTeam.equalsIgnoreCase("White") ? GameMode.currentGamemodeColor2 : GameMode.currentGamemodeColor1;
            Color teamBorderColor = new Color(150, 150, 150);

            g.setColor(teamBorderColor);
            g.fillRect(teamBoxX - 2, teamBoxY - 2, teamBoxWidth + 4, teamBoxHeight + 4);

            g.setColor(teamBoxColor);
            g.fillRect(teamBoxX, teamBoxY, teamBoxWidth, teamBoxHeight);

            int textXX = teamBoxX + paddingX;
            int textY = teamBoxY + (teamBoxHeight + textHeight) / 2 - 3;

            g.setColor(Color.GRAY);
            g.drawString(text, textXX + 1, textY + 1);

            g.setColor(teamTextColor);
            g.drawString(text, textXX, textY);
        }
    }
}