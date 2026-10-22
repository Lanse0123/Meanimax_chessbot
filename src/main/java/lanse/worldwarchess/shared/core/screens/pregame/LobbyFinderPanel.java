package lanse.worldwarchess.shared.core.screens.pregame;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.shared.server.GameLobby;
import lanse.worldwarchess.shared.server.LobbyServer;
import lanse.worldwarchess.steam.SteamPlayerCountCallback;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LobbyFinderPanel implements SteamMatchmakingCallback {

    //This is another one of the 4 classes broken up from an old giant class. This is the screen shown
    // for when you are trying to list all of the available lobbies to join.

    private final JPanel panel;
    private final DefaultListModel<GameLobby> joinLobbyListModel = new DefaultListModel<>();
    private final List<GameLobby> availableLobbies = new ArrayList<>();
    private final Consumer<GameLobby> onJoinSuccess;
    private final Runnable onBack;
    private final SteamMatchmaking matcher;

    private static final Color BG_DARK = new Color(25, 25, 25);
    private static final Color PANEL_DARK = new Color(40, 40, 40);
    private static final Color TEXT_LIGHT = new Color(220, 220, 220);
    private static final Color ACCENT_BLUE = new Color(70, 140, 245);

    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerPanel = new JPanel(centerLayout);
    private final JList<GameLobby> lobbyList = new JList<>(joinLobbyListModel);
    private final JLabel noLobbiesLabel = new JLabel("No Lobbies Found", SwingConstants.CENTER);
    private JLabel globalPlayerCountLabel;
    private Thread pulseThread;

    public LobbyFinderPanel(Consumer<GameLobby> onJoinSuccess, Runnable onBack) {
        this.onJoinSuccess = onJoinSuccess;
        this.onBack = onBack;
        this.panel = buildPanel();

        this.matcher = new SteamMatchmaking(this);
        this.matcher.requestLobbyList();

        CascadingClient.lobbyServer = new LobbyServer();

        startPulse();
    }

    private JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BG_DARK);

        JLabel header = new JLabel("Available Lobbies", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setForeground(TEXT_LIGHT);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 2, 0));

        globalPlayerCountLabel = new JLabel("Players Online: " + SteamPlayerCountCallback.globalPlayercount, SwingConstants.CENTER);
        globalPlayerCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        globalPlayerCountLabel.setForeground(TEXT_LIGHT.darker());
        globalPlayerCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        globalPlayerCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        headerPanel.add(header);
        headerPanel.add(globalPlayerCountLabel);

        panel.add(headerPanel, BorderLayout.NORTH);

        lobbyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lobbyList.setBackground(PANEL_DARK);
        lobbyList.setForeground(TEXT_LIGHT);
        lobbyList.setSelectionBackground(ACCENT_BLUE.darker());
        lobbyList.setSelectionForeground(Color.WHITE);
        lobbyList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(lobbyList);
        scrollPane.setBackground(PANEL_DARK);
        scrollPane.getViewport().setBackground(PANEL_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        startPulse();

        centerPanel.add(scrollPane, "LIST");
        centerPanel.add(noLobbiesLabel, "EMPTY");
        centerPanel.setBackground(PANEL_DARK);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(BG_DARK);

        JButton joinButton = styleButton("Join Selected Lobby");
        joinButton.addActionListener(e -> joinSelectedLobby(panel));

        JButton backButton = styleButton("Back");
        backButton.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        bottomPanel.add(joinButton);
        bottomPanel.add(backButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JButton styleButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(ACCENT_BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_BLUE.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_BLUE);
            }
        });

        return button;
    }

    private void joinSelectedLobby(JPanel panel) {
        GameLobby selectedLobby = lobbyList.getSelectedValue();
        if (selectedLobby != null) {
            try {
                if (selectedLobby.joinLobby()) {
                    if (onJoinSuccess != null) onJoinSuccess.accept(selectedLobby);
                } else {
                    JOptionPane.showMessageDialog(panel, "Unable to join: Lobby not available.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Failed to connect to the lobby.\nError: " + ex.getMessage(),
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(panel, "No lobby selected.");
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void onLobbyMatchList(int numLobbiesMatching) {
        System.out.println("Found " + numLobbiesMatching + " lobbies.");

        availableLobbies.clear();

        for (int i = 0; i < numLobbiesMatching; ++i) {
            try {
                SteamID lobbyID = matcher.getLobbyByIndex(i);
                GameLobby lobby = new GameLobby(matcher, lobbyID, CascadingClient.lobbyServer);
                availableLobbies.add(lobby);
            } catch (Exception e) {
                System.err.println("Failed to convert server info: " + e.getMessage());
            }
        }

        SwingUtilities.invokeLater(() -> {
            joinLobbyListModel.clear();
            if (availableLobbies.isEmpty()) {
                centerLayout.show(centerPanel, "EMPTY");
            } else {
                availableLobbies.forEach(joinLobbyListModel::addElement);
                centerLayout.show(centerPanel, "LIST");
            }
        });
    }

    private void startPulse() {
        //This is the most perfect code I have ever seen. I asked ChatGippity to make the text red because
        // I was being incompetent, but I also put "menacing", and 😏 in the request. SO IT MADE IT AWESOME ONG LOL
        noLobbiesLabel.setForeground(new Color(255, 60, 60));
        noLobbiesLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        noLobbiesLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 0, 0), 2),
                BorderFactory.createEmptyBorder(30, 10, 30, 10)
        ));
        noLobbiesLabel.setBackground(new Color(20, 0, 0));
        noLobbiesLabel.setOpaque(true);
        noLobbiesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noLobbiesLabel.setVerticalAlignment(SwingConstants.CENTER);

        pulseThread = new Thread(() -> {
            float phase = 0;
            while (true) {
                try {
                    Thread.sleep(60);
                } catch (InterruptedException ignored) {}
                final float intensity = (float) (0.5 + 0.5 * Math.sin(phase));
                phase += 0.1F;
                Color pulse = new Color(
                        (int) (150 + 100 * intensity),
                        (int) (10 + 20 * intensity),
                        (int) (10 + 20 * intensity)
                );
                SwingUtilities.invokeLater(() -> noLobbiesLabel.setForeground(pulse));
            }
        });
        pulseThread.setDaemon(true);
        pulseThread.start();
    }
}
