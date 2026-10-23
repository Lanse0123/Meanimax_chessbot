package lanse.worldwarchess.shared.server;

import lanse.worldwarchess.shared.central.CentralTick;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.screens.pregame.MainMenu;

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
        String[] options = {"Minimax", "Meanimax", "Maximin"};

        //TODO - make prettier?
        String choice = (String) JOptionPane.showInputDialog(frame, "Choose the AI enemy:",
                "Hello I hope this is a good final project", JOptionPane.QUESTION_MESSAGE, null, options, "Meanimax");

        if (choice == null) {
            return;
        }

        NPC.selectedAI = switch (choice.toLowerCase()) {
            case "minimax" -> NPC.SELECTED_AI.MINIMAX;
            case "meanimax" -> NPC.SELECTED_AI.MEANIMAX;
            case "maximin" -> NPC.SELECTED_AI.MAXIMIN;
            default -> throw new IllegalStateException("Unexpected value: " + choice);
        };

        System.out.println("Starting Single Player Mode with " + choice + ".");

        StartGame.startGame(frame);
    }
}