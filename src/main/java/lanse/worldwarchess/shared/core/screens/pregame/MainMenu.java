
package lanse.worldwarchess.shared.core.screens.pregame;

import lanse.worldwarchess.shared.central.LogoManager;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.screens.history.HistoryMenu;
import lanse.worldwarchess.shared.server.StartGame;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    private JPanel root;
    private JPanel mainMenu;
    private JPanel playMenu;

    public static MainMenu instance;

    public MainMenu() {
        super("World War Chess - Main Menu");
        instance = this;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1824, 1025);
        setLocationRelativeTo(null);
        setIconImages(LogoManager.getLogos());

        getContentPane().setLayout(new BorderLayout());

        boolean firstLaunch = Settings.isPlayMainMenuStartup();

        reset(firstLaunch);
    }

    public void reset(boolean firstLaunch) {
        getContentPane().removeAll();

        // Root panel, replacing JavaFX StackPane.
        root = new JPanel(new GridBagLayout());
        root.setBackground(Color.BLACK);

        mainMenu = new JPanel();
        mainMenu.setLayout(new BoxLayout(mainMenu, BoxLayout.Y_AXIS));
        mainMenu.setOpaque(false);
        mainMenu.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        mainMenu.setPreferredSize(new Dimension(248, 0));

        JButton playButton = new JButton("Play");
        JButton historyButton = new JButton("Game Review");
        JButton settingsButton = new JButton("Settings");
        JButton quitButton = new JButton("Quit");

        mainMenu.add(playButton);
        mainMenu.add(Box.createVerticalStrut(12));
        mainMenu.add(historyButton);
        mainMenu.add(Box.createVerticalStrut(12));
        mainMenu.add(settingsButton);
        mainMenu.add(Box.createVerticalStrut(12));
        mainMenu.add(quitButton);

        playMenu = new JPanel();
        playMenu.setLayout(new BoxLayout(playMenu, BoxLayout.Y_AXIS));
        playMenu.setOpaque(false);
        playMenu.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        playMenu.setPreferredSize(new Dimension(248, 0));

        JButton singlePlayerButton = new JButton("SinglePlayer");
        JButton hostButton = new JButton("Host");
        JButton joinButton = new JButton("Join");
        JButton sandboxButton = new JButton("Sandbox");
        JButton tutorialButton = new JButton("Tutorial");
        JButton rulesButton = new JButton("Rules");
        JButton backButton = new JButton("Back");

        playMenu.add(singlePlayerButton);
        playMenu.add(Box.createVerticalStrut(12));
        playMenu.add(hostButton);
        playMenu.add(Box.createVerticalStrut(12));
        playMenu.add(joinButton);
        playMenu.add(Box.createVerticalStrut(12));
        playMenu.add(sandboxButton);
        playMenu.add(Box.createVerticalStrut(12));
        playMenu.add(tutorialButton);
        playMenu.add(Box.createVerticalStrut(12));
        playMenu.add(rulesButton);
        playMenu.add(Box.createVerticalStrut(12));
        playMenu.add(backButton);

        // Make buttons fill the menu's width.
        for (Component component : mainMenu.getComponents()) {
            if (component instanceof JButton button) {
                button.setAlignmentX(Component.LEFT_ALIGNMENT);
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
            }
        }

        for (Component component : playMenu.getComponents()) {
            if (component instanceof JButton button) {
                button.setAlignmentX(Component.LEFT_ALIGNMENT);
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
            }
        }

        root.add(mainMenu);
        root.add(playMenu);

        mainMenu.setVisible(true);
        playMenu.setVisible(false);

        getContentPane().add(root, BorderLayout.CENTER);

        playButton.addActionListener(e -> {
            mainMenu.setVisible(false);
            playMenu.setVisible(true);
            root.revalidate();
            root.repaint();
        });

        backButton.addActionListener(e -> {
            playMenu.setVisible(false);
            mainMenu.setVisible(true);
            root.revalidate();
            root.repaint();
        });

        historyButton.addActionListener(e -> showHistoryWindow());
        settingsButton.addActionListener(e -> showSettingsWindow());
        quitButton.addActionListener(e -> quitGame());

        singlePlayerButton.addActionListener(e -> StartGame.initializeSinglePlayer(this));

        setVisible(true);
        root.revalidate();
        root.repaint();
    }

    private void showHistoryWindow() {
        HistoryMenu.show(this);
    }

    private void showSettingsWindow() {
        Settings.showSettingsWindow();
    }

    private void quitGame() {
        System.exit(0);
    }
}
