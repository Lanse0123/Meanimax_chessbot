package lanse.worldwarchess.shared.core.screens.pregame;

import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaException;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import lanse.worldwarchess.shared.central.LogoManager;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.screens.history.HistoryMenu;
import lanse.worldwarchess.shared.server.GameLobby;
import lanse.worldwarchess.shared.server.StartGame;
import lanse.worldwarchess.shared.utils.StringUtils;
import lanse.worldwarchess.steam.Account;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    //This class works a lot with VideoBackground class. This creates the window, and starts the correct videoBackground
    // player. It also has the logic for each button.

    private final JFXPanel fxPanel = new JFXPanel();
    private VideoBackground videoBg;
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
//        getContentPane().add(fxPanel, BorderLayout.CENTER);
//        setVisible(true);

        boolean firstLaunch = Settings.isPlayMainMenuStartup();

        reset(firstLaunch);
    }

    public void reset(boolean firstLaunch) {
        getContentPane().removeAll();
        getContentPane().add(fxPanel, BorderLayout.CENTER);
        setVisible(true);

        if (videoBg == null) {
            // create VideoBackground and wire callbacks to open Swing windows
            videoBg = new VideoBackground(fxPanel);

            root = new StackPane();
            root.setStyle("-fx-background-color: black;");

            try {
                videoBg.reset(firstLaunch, root);
            } catch (MediaException e) {
                System.err.println("Unable to play background animation for reason " + e.getType());
            }

            // Main menu buttons
            this.mainMenu = new VBox(12);
            mainMenu.setPickOnBounds(false);
            mainMenu.setAlignment(Pos.CENTER_LEFT);
            mainMenu.setPadding(new Insets(24));
            mainMenu.setMaxWidth(200);

            Button playButton = new Button("Play");
            Button historyButton = new Button("Game Review");
            Button settingsButton = new Button("Settings");
            Button creditsButton = new Button("Credits");
            Button devlogButton = new Button("DevLog");
            Button quitButton = new Button("Quit");

            playButton.setMaxWidth(Double.MAX_VALUE);
            historyButton.setMaxWidth(Double.MAX_VALUE);
            settingsButton.setMaxWidth(Double.MAX_VALUE);
            creditsButton.setMaxWidth(Double.MAX_VALUE);
            devlogButton.setMaxWidth(Double.MAX_VALUE);
            quitButton.setMaxWidth(Double.MAX_VALUE);

            mainMenu.getChildren().addAll(playButton, historyButton, settingsButton, creditsButton, devlogButton, quitButton);
            mainMenu.setOpacity(0);
            mainMenu.setDisable(true);

            // Play submenu
            this.playMenu = new VBox(12);
            playMenu.setAlignment(Pos.CENTER_LEFT);
            playMenu.setPadding(new Insets(24));
            playMenu.setMaxWidth(200);
            playMenu.setVisible(false);

            Button singlePlayerButton = new Button("Single Player");
            Button hostButton = new Button("Host Game");
            Button joinButton = new Button("Join Game");
            Button sandboxButton = new Button("Sandbox (SOON)");
            Button tutorialButton = new Button("Tutorial");
            Button rulesButton = new Button("Rules");
            Button backButton = new Button("<- Back");

            singlePlayerButton.setMaxWidth(Double.MAX_VALUE);
            hostButton.setMaxWidth(Double.MAX_VALUE);
            joinButton.setMaxWidth(Double.MAX_VALUE);
            sandboxButton.setMaxWidth(Double.MAX_VALUE);
            tutorialButton.setMaxWidth(Double.MAX_VALUE);
            rulesButton.setMaxWidth(Double.MAX_VALUE);
            backButton.setMaxWidth(Double.MAX_VALUE);

            if (!Account.steamSetupWorked()) {
                hostButton.setDisable(true);
                joinButton.setDisable(true);

                Text mpUnavailable = new Text("Cannot connect to Steam");
                mpUnavailable.setFill(new Color(1, 0, 0, 1));
                playMenu.getChildren().add(mpUnavailable);
            }

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

            historyButton.setOnAction(ae -> SwingUtilities.invokeLater(this::showHistoryWindow));
            settingsButton.setOnAction(ae -> SwingUtilities.invokeLater(this::showSettingsWindow));
            creditsButton.setOnAction(ae -> SwingUtilities.invokeLater(this::showCreditsWindow));
            devlogButton.setOnAction(ae -> SwingUtilities.invokeLater(this::showDevLogWindow));
            quitButton.setOnAction(ae -> SwingUtilities.invokeLater(this::quitGame));

            singlePlayerButton.setOnAction(ae -> SwingUtilities.invokeLater(() -> StartGame.initializeSinglePlayer(this)));
            rulesButton.setOnAction(ae -> SwingUtilities.invokeLater(this::showRulesWindow));
            tutorialButton.setOnAction(ae -> SwingUtilities.invokeLater(() -> StartGame.initializeTutorial(this)));
            sandboxButton.setOnAction(ae -> SwingUtilities.invokeLater(() -> StartGame.initializeSandbox(this)));
            hostButton.setOnAction(ae -> SwingUtilities.invokeLater(this::showHostWindow));
            joinButton.setOnAction(ae -> SwingUtilities.invokeLater(this::showJoinWindow));
        }

        //THIS HAS ALL OF THE RUNTIME STUFF AND CALLS INIT AND PLATFORM.RUNLATER();
        videoBg.startAnimations(firstLaunch, root, mainMenu);
    }

    private void showHostWindow() {
        final JFrame[] hostFrameRef = new JFrame[1];

        HostPanel hostPanel = new HostPanel(() -> {
            setVisible(false);
            //wtf
            if (hostFrameRef[0] != null) hostFrameRef[0].setVisible(false);
        }, () -> {
            if (hostFrameRef[0] != null) hostFrameRef[0].setVisible(false);
        }, this);

        JFrame hostFrame = new JFrame("Host Game");
        hostFrameRef[0] = hostFrame;
        hostFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        hostFrame.setIconImages(LogoManager.getLogos());
        hostFrame.setSize(1040, 760);
        hostFrame.setLocationRelativeTo(this);
        hostFrame.getContentPane().add(hostPanel.getPanel());
        hostFrame.setVisible(true);
    }

    private void showJoinWindow() {
        final JFrame[] joinFrameRef = new JFrame[1];

        LobbyFinderPanel joinPanel = new LobbyFinderPanel((GameLobby joined) -> {
                    StartGame.initializeAsClient(joined, this);
                    setVisible(false);
                    if (joinFrameRef[0] != null) joinFrameRef[0].setVisible(false);
                }, () -> {
            if (joinFrameRef[0] != null) joinFrameRef[0].setVisible(false);
        });

        JFrame joinFrame = new JFrame("Join Game");
        joinFrameRef[0] = joinFrame;
        joinFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        joinFrame.setIconImages(LogoManager.getLogos());
        joinFrame.setSize(620, 420);
        joinFrame.setLocationRelativeTo(this);
        joinFrame.getContentPane().add(joinPanel.getPanel());
        joinFrame.setVisible(true);
    }

    private void showRulesWindow() {
        Rules.show();
    }

    private void showHistoryWindow() {
        HistoryMenu.show(this);
    }

    private void showSettingsWindow() {
        Settings.showSettingsWindow();
    }

    private void showCreditsWindow() {
        CreditScreen.show(this);
    }

    private void showDevLogWindow() {
        DevLogScreen.show(this);
    }

    private void quitGame() {
        System.exit(0);
    }
}