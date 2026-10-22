package lanse.worldwarchess.shared.core.screens.pregame;

import lanse.worldwarchess.cascading.CascadeManager;
import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.cascading.CascadeTree;
import lanse.worldwarchess.cascading.CascadingHost;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.central.Returner;
import lanse.worldwarchess.shared.chat.ChatManager;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeMapType;
import lanse.worldwarchess.shared.core.screens.background.Background;
import lanse.worldwarchess.shared.core.screens.background.MapPreview;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.server.GameLobby;
import lanse.worldwarchess.shared.server.StartGame;
import lanse.worldwarchess.steam.Account;
import lanse.worldwarchess.steam.SteamNetworkingManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

public class GameLobbyWindow extends JFrame {

    private final JLabel lobbyInfoLabel;
    private final JButton startButton;
    private final DefaultListModel<String> playerListModel;
    private Timer updateTimer;
    private final GameLobby lobby;
    private static GameLobbyWindow lobbyWindow;
    private static JFrame mainWindow;

    private static final Color BG = new Color(0x1e1e1e);
    private static final Color PANEL_BG = new Color(0x252526);
    private static final Color TEXT = new Color(0xdddddd);
    private static final Color BORDER = new Color(0x3c3c3c);
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    public GameLobbyWindow(GameLobby lobby, int hostConn, JFrame mainWindow) {
        GameLobbyWindow.mainWindow = mainWindow;
        lobbyWindow = this;
        boolean isHost = Main.userIsHost;
        this.lobby = lobby;

        if (isHost) {
            SteamNetworkingManager.initHost(new CascadingHost(Account.getMySteamID(), hostConn));
            new CascadeTree(Account.getMySteamID(), hostConn);
            CascadePacketManager.manager = new CascadePacketManager();
        } else {
            SteamNetworkingManager.initHost(new CascadingHost(lobby.getServerHostID(), hostConn));
            //TODO - this passes in the wrong root ID, this needs to get the rootID of this tree.
            // this almost seems preemptive...
            new CascadeTree(Account.getMySteamID(), hostConn);
            CascadePacketManager.manager = new CascadePacketManager();
        }

        setTitle("Game Lobby");
        setSize(1440, 810);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        // ---------- Right panel: Map ----------
        JPanel mapPreview = new JPanel() {
            private final Image mapImage = MapPreview.getMapPreview(lobby);
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Background.draw(getWidth(), getHeight(), getWidth() / 2, getHeight() / 2, (Graphics2D) g);
                if (mapImage != null) {
                    int imgWidth = mapImage.getWidth(this);
                    int imgHeight = mapImage.getHeight(this);
                    int x = (getWidth() - imgWidth) / 2;
                    int y = (getHeight() - imgHeight) / 2;
                    g.drawImage(mapImage, x, y, this);
                }
            }
        };
        mapPreview.setPreferredSize(new Dimension(350, 500));
        mapPreview.setBackground(PANEL_BG);
        mapPreview.setBorder(new TitledBorder(new LineBorder(BORDER), "Map", TitledBorder.LEADING, TitledBorder.TOP, DEFAULT_FONT, TEXT));
        add(mapPreview, BorderLayout.EAST);

        // ---------- Center panel: Players + Lobby Info ----------
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(BG);

        playerListModel = new DefaultListModel<>();
        JList<String> playerList = new JList<>(playerListModel);
        playerList.setBackground(PANEL_BG);
        playerList.setForeground(TEXT);
        playerList.setFont(DEFAULT_FONT);
        playerList.setBorder(new TitledBorder(new LineBorder(BORDER), "Players", TitledBorder.LEADING, TitledBorder.TOP, DEFAULT_FONT, TEXT));
        centerPanel.add(new JScrollPane(playerList), BorderLayout.NORTH);

        lobbyInfoLabel = new JLabel("Waiting for players...", SwingConstants.CENTER);
        lobbyInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        lobbyInfoLabel.setForeground(TEXT);
        centerPanel.add(lobbyInfoLabel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // ---------- Bottom panel: Chat + Controls ----------
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG);

        ChatManager.init();
        JComponent chatPanel = ChatManager.getPanel();
        chatPanel.setBackground(PANEL_BG);
        bottomPanel.add(chatPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BG);

        startButton = createDarkButton("Start Game");
        startButton.setEnabled(isHost);
        startButton.addActionListener((ActionEvent e) -> startGame());

        JButton cancelButton = createDarkButton("Cancel");
        cancelButton.addActionListener((ActionEvent e) -> {
            if (updateTimer != null) updateTimer.stop();
            Main.userIsHost = false;
            dispose();
            Returner.returnToMainMenu();
        });

        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        updateTimer = new Timer(1000, (ActionEvent e) -> updateLobbyInfo());
        updateTimer.start();
        setVisible(true);
    }

    private JButton createDarkButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0x3a3a3a));
        button.setForeground(TEXT);
        button.setFont(DEFAULT_FONT);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(BORDER));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x505050));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x3a3a3a));
            }
        });

        return button;
    }

    public static void startGame() {
        if (lobbyWindow.updateTimer != null) {
            lobbyWindow.updateTimer.stop();
        }
        if (Main.userIsHost) {
            CascadePacketManager.manager.sendPacketToEveryone(
                    CascadePacketManager.PacketType.START,
                    String.valueOf(CascadeManager.playerCount),
                    Optional.empty()
            );
        }

        lobbyWindow.dispose();
        WorldMap.currentMapPreset = GameModeMapType.valueOf(lobbyWindow.lobby.getMapType().toUpperCase());
        GameMode.setCurrentGamemode(lobbyWindow.lobby.getGamemode());
        GameMode.isFrenzy = lobbyWindow.lobby.getIsFrenzyGamemode();
        GameMode.isFogOfWar = lobbyWindow.lobby.getIsFogOfWarGamemode();
        GameMode.selectedNPCFaction = lobbyWindow.lobby.getNpcFaction();
        GameMode.setNewGamemodeColor();
        lanse.worldwarchess.shared.core.Timer.maxTimePerTurn = lobbyWindow.lobby.getTurnLength();

        //TODO: do something different when not in frenzy GameMode
//        if (GameMode.isFrenzyGamemode){
        GameMode.playerCountFull = lobbyWindow.lobby.getMaxPlayers();
        GameMode.playerCountOneTeam = GameMode.playerCountFull / 2;
//        } else {
        //TODO - get current playercount and use that.
//        }

        StartGame.startGame(mainWindow, false, false);
    }

    private void updateLobbyInfo() {
        playerListModel.clear();
        //TODO: actually add players here
        for (String p : lobby.getCurrentPlayers()) {
            playerListModel.addElement(p);
        }

        lobbyInfoLabel.setText("<html>Lobby: " + lobby.getLobbyName() +
                " | Players: " + lobby.getCurrentPlayerCount() + " / " + lobby.getMaxPlayers() + "<br>" +
                "(MULTIPLAYER STILL HAS A LOT OF BUGS! THESE WILL BE FIXED SOON!)</html>");

        if (Main.userIsHost) {
            startButton.setEnabled(CascadeManager.playerCount >= 2);
            if (Main.debugMode){
                startButton.setEnabled(true);
            }
        }
    }
}