package lanse.worldwarchess.shared.core.screens.ingame;

import lanse.worldwarchess.shared.central.Returner;

import javax.swing.*;
import java.awt.*;

public class GameOverScreen extends JFrame {

    //This class is responsible for pulling up the game over screen, giving you the option to continue
    // spectating the board or return to the main menu.

    public GameOverScreen(String winningTeam) {
        setTitle("Game Over");
        setSize(400, 200);
        setLayout(new BorderLayout());

        JLabel winLabel = new JLabel(winningTeam + " TEAM WINS!", SwingConstants.CENTER);
        winLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(winLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton mainMenuButton = new JButton("Return to Main Menu");
        JButton spectateButton = new JButton("Continue Spectating");

        mainMenuButton.addActionListener(e -> returnToMainMenu());
        spectateButton.addActionListener(e -> dispose()); // Just close the window to continue spectating

        buttonPanel.add(mainMenuButton);
        buttonPanel.add(spectateButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void returnToMainMenu() {
        dispose(); // Close game over screen
        Returner.returnToMainMenu();
    }
}