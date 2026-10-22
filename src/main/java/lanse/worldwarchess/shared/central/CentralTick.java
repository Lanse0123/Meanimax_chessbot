package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.screens.ingame.GameOverScreen;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.steam.SteamPlayerCountCallback;

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

            CascadingClient.doMultiplayerTick();

            if (tickCount % 600 == 0 || tickCount == 30) { // low check once so it is accurate enough
                SteamPlayerCountCallback.updatePlayerCount();
            }

            SoundManager.soundLimitReached = false;

            if (Timer.isGamePaused) return;

            //Responsible for the timer and the playerIDMoved list.
            if ((!gameOver && isGameActive) || GameMode.isTutorial) Timer.count();

            SwingUtilities.invokeLater(() -> {
                if (Main.gamePanel != null) {
                    Main.gamePanel.repaint();
                }
            });

            if (!GameMode.isSinglePlayer && Main.gamePanel.chat != null) {
                Main.gamePanel.chat.removeOldMessages();
            }

            //Kill a random piece from the list of pieces on death row.
            if (!DeathMarker.deathRow.isEmpty()) {
                if (gameOver) {
                    for (int i = 0; i < 8; i++) {
                        if (!DeathMarker.deathRow.isEmpty()) {
                            WorldMap.killPiece(Objects.requireNonNull(DeathMarker.deathRow.poll()), true, false);
                        }
                    }
                } else {
                    //if death row is too long, speed it up
                    int amount = 1;
                    if (DeathMarker.deathRow.size() > 25) amount = 8;
                    for (int i = 0; i < amount; i++) {
                        if (!DeathMarker.deathRow.isEmpty()) {
                            WorldMap.killPiece(Objects.requireNonNull(DeathMarker.deathRow.poll()), true, false);
                            SoundManager.capturePiece(null);
                        }
                    }
                }
            }
            if (gameOver && !gameOverScreenShown) {
                gameOverScreenShown = true;
                isGameActive = false;
                SwingUtilities.invokeLater(() -> new GameOverScreen(GamePanel.winningTeam));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}