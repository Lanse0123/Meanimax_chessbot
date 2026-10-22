package lanse.worldwarchess.shared.core.screens.pregame;

import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lanse.worldwarchess.shared.central.LogoManager;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.screens.history.HistoryMenu;
import lanse.worldwarchess.shared.server.StartGame;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    //This class works a lot with VideoBackground class. This creates the window, and starts the correct videoBackground
    // player. It also has the logic for each button.

    private final JFXPanel fxPanel = new JFXPanel();
    private StackPane root;
    private VBox mainMenu, playMenu;

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

    //TODO - change all javafx to jframe
    public void reset(boolean firstLaunch) {
        getContentPane().removeAll();
        getContentPane().add(fxPanel, BorderLayout.CENTER);
        setVisible(true);

        root = new StackPane();
        root.setStyle("-fx-background-color: black;");

        // Main menu buttons
        this.mainMenu = new VBox(12);
        mainMenu.setPickOnBounds(false);
        mainMenu.setAlignment(Pos.CENTER_LEFT);
        mainMenu.setPadding(new Insets(24));
        mainMenu.setMaxWidth(200);

        Button playButton = new Button("Play");
        Button historyButton = new Button("Game Review");
        Button settingsButton = new Button("Settings");
        Button quitButton = new Button("Quit");

        playButton.setMaxWidth(Double.MAX_VALUE);
        historyButton.setMaxWidth(Double.MAX_VALUE);
        settingsButton.setMaxWidth(Double.MAX_VALUE);
        quitButton.setMaxWidth(Double.MAX_VALUE);

        mainMenu.getChildren().addAll(playButton, historyButton, settingsButton, quitButton);
        mainMenu.setOpacity(0);
        mainMenu.setDisable(true);

        // Play submenu
        this.playMenu = new VBox(12);
        playMenu.setAlignment(Pos.CENTER_LEFT);
        playMenu.setPadding(new Insets(24));
        playMenu.setMaxWidth(200);
        playMenu.setVisible(false);

        Button singlePlayerButton = new Button("SinglePlayer");

        singlePlayerButton.setMaxWidth(Double.MAX_VALUE);

        playMenu.getChildren().addAll(singlePlayerButton, hostButton, joinButton, sandboxButton, tutorialButton, rulesButton, backButton);

        // Add both menus to the root
        StackPane.setAlignment(mainMenu, Pos.CENTER);
        StackPane.setMargin(mainMenu, new Insets(0, 0, 0, 10));
        StackPane.setAlignment(playMenu, Pos.CENTER);
        StackPane.setMargin(playMenu, new Insets(0, 0, 0, 10));
        root.getChildren().addAll(mainMenu, playMenu);

        playButton.setOnAction(ae -> {
            mainMenu.setVisible(false);
            playMenu.setVisible(true);
        });
        backButton.setOnAction(ae -> {
            playMenu.setVisible(false);
            mainMenu.setVisible(true);
        });

        historyButton.setOnAction(_ -> SwingUtilities.invokeLater(this::showHistoryWindow));
        settingsButton.setOnAction(_ -> SwingUtilities.invokeLater(this::showSettingsWindow));
        quitButton.setOnAction(_ -> SwingUtilities.invokeLater(this::quitGame));

        singlePlayerButton.setOnAction(_ -> SwingUtilities.invokeLater(() -> StartGame.initializeSinglePlayer(this)));
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