package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.screens.ingame.GameOverScreen;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import javax.swing.*;
import java.util.Objects;

public class CentralTick {

    //This class is responsible for the tick loop of the game. It will pretty much always be getting called.
    //This is a great spot to add logic that needs to be called very often.

    public static int tickCount;
    public static boolean gameOver = false;
    private static boolean gameOverScreenShown = false;
    public static boolean isGameActive = false;

    public static void centralTick() {
        // This runs 10 times per second

        try {
            tickCount++;
            if (tickCount > 1000000000) tickCount = 0;

            SoundManager.soundLimitReached = false;

            if (Timer.isGamePaused) return;

            //Responsible for the timer and the playerIDMoved list.
            if ((!gameOver && isGameActive)) Timer.count();

            SwingUtilities.invokeLater(() -> {
                if (Main.gamePanel != null) {
                    Main.gamePanel.repaint();
                }
            });

            //Kill a random piece from the list of pieces on death row.
            if (!DeathMarker.deathRow.isEmpty()) {
                if (gameOver) {
                    for (int i = 0; i < 8; i++) {
                        if (!DeathMarker.deathRow.isEmpty()) {
                            WorldMap.killPiece(Objects.requireNonNull(DeathMarker.deathRow.poll()));
                        }
                    }
                } else {
                    //if death row is too long, speed it up
                    int amount = 1;
                    if (DeathMarker.deathRow.size() > 25) amount = 8;
                    for (int i = 0; i < amount; i++) {
                        if (!DeathMarker.deathRow.isEmpty()) {
                            WorldMap.killPiece(Objects.requireNonNull(DeathMarker.deathRow.poll()));
                            SoundManager.capturePiece(null);
                        }
                    }
                }
            }
            if (gameOver && !gameOverScreenShown) {
                gameOverScreenShown = true;
                isGameActive = false;
                SoundManager.gameOver();
                SwingUtilities.invokeLater(() -> new GameOverScreen(GamePanel.winningTeam));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}