package lanse.worldwarchess.shared.server;

import lanse.worldwarchess.shared.central.CentralTick;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;

import javax.swing.*;

public class StartGame {

    //This class is simply responsible for starting a game.

    public static void startGame(JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Main.gamePanel = new GamePanel();

            frame.add(Main.gamePanel);
            frame.revalidate();
            frame.repaint();
            frame.setVisible(true);

            CentralTick.gameOver = false;
            Timer.gameStarted = false;
            Timer.turnCounter = 0;
            Timer.preGameCountdown = 3.0;

            Timer.currentTime = Timer.maxTimePerTurn;
            Timer.playerIDMovedList.clear();

            CentralTick.isGameActive = true;
        });
    }

    public static void initializeSinglePlayer(JFrame frame) {
        System.out.println("Starting Single Player Mode.");
        StartGame.startGame(frame);
    }
}