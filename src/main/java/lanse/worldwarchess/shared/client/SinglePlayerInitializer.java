package lanse.worldwarchess.shared.client;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeMapType;
import lanse.worldwarchess.shared.core.gamemode.GameModeType;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.sandbox.CustomMapPreset;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.server.StartGame;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.nio.file.Path;
import java.util.Objects;

import static lanse.worldwarchess.shared.core.screens.sandbox.SandboxMapDataStack.extractGamemodeFromMapFile;
import static lanse.worldwarchess.shared.core.screens.sandbox.SandboxMenu.MAPS_DIR;

public class SinglePlayerInitializer {

    //This class entirely sets up the game for singleplayer. It has the screen, the options, and calls
    //the start game class when ready.

    private static final Color backgroundColor = new Color(30, 30, 30);
    private static final Color foregroundColor = Color.WHITE;
    private static final Color accentColor = new Color(70, 130, 180); // steel blue
    private static final Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);

    public static void initializeGame(JFrame frame) {
        JDialog setupDialog = new JDialog((JFrame) null, "Game Setup", true);
        setupDialog.setSize(560, 560);
        setupDialog.setLayout(new BoxLayout(setupDialog.getContentPane(), BoxLayout.Y_AXIS));
        setupDialog.setLocationRelativeTo(null);
        setupDialog.getContentPane().setBackground(backgroundColor);

        // Utility function to style panels
        java.util.function.Consumer<JPanel> stylePanel = panel -> {
            panel.setBackground(backgroundColor);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        };

        // Utility function to style labels
        java.util.function.Consumer<JLabel> styleLabel = label -> {
            label.setForeground(foregroundColor);
            label.setFont(defaultFont);
        };

        // Player count selection
        JPanel playerPanel = new JPanel();
        stylePanel.accept(playerPanel);
        JLabel playerLabel = new JLabel("How many sets of pieces? :");
        String playerHelpText = "This roughly dictates roughly how many sets of chess pieces the map starts with. Even numbers are better.";
        styleLabel.accept(playerLabel);
        JSpinner playerSpinner = new JSpinner(new SpinnerNumberModel(10, 2, 1000000, 1));
        playerSpinner.setBackground(backgroundColor);
        playerSpinner.setForeground(foregroundColor);
        playerSpinner.setFont(defaultFont);
        playerPanel.add(playerLabel);
        playerPanel.add(createHelpIcon(playerHelpText));
        playerPanel.add(playerSpinner);
        styleSpinner(playerSpinner);

        // GameMode selection
        JPanel gameModePanel = new JPanel();
        stylePanel.accept(gameModePanel);
        JLabel gameModeLabel = new JLabel("Pick a GameMode:");
        String gameModeHelpText = "War you control both teams, Human vs NPC you only control 1 team, NPC War you just spectate, Siege you control 1 team vs bossfights.";
        styleLabel.accept(gameModeLabel);
        JComboBox<String> gameModeDropdown = new JComboBox<>(GameMode.getAvailableGameModes(false));
        GameMode.setCurrentGamemode(Objects.requireNonNull(gameModeDropdown.getSelectedItem()).toString());
        styleComboBox(gameModeDropdown);
        gameModeDropdown.setSelectedIndex(1); //this should probably be made more future proof
        gameModePanel.add(gameModeLabel);
        gameModePanel.add(createHelpIcon(gameModeHelpText));
        gameModePanel.add(gameModeDropdown);

        // Map selection (model will be refreshed when mode changes)
        JPanel mapPanel = new JPanel();
        stylePanel.accept(mapPanel);
        JLabel mapLabel = new JLabel("Pick a map:");
        styleLabel.accept(mapLabel);
        JComboBox<String> mapDropdown = new JComboBox<>(GameMode.getAvailableMaps());
        styleComboBox(mapDropdown);
        mapDropdown.setSelectedIndex(0);
        mapPanel.add(mapLabel);
        mapPanel.add(mapDropdown);

        //since technically its multithreaded, this cursed line of code seems to work???
        final java.util.concurrent.atomic.AtomicReference<Path> customMapFile = new java.util.concurrent.atomic.AtomicReference<>(null);
        final java.util.concurrent.atomic.AtomicBoolean mapIsCustom = new java.util.concurrent.atomic.AtomicBoolean(false);

        JButton customMapButton = new JButton("Custom...");
        customMapButton.setToolTipText("Load a custom sandbox map (locks gamemode to the map's gamemode)");
        customMapButton.setFocusPainted(false);

        customMapButton.setFont(defaultFont);
        customMapButton.setBackground(accentColor);
        customMapButton.setForeground(foregroundColor);

        customMapButton.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser(MAPS_DIR.toFile());
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int res = chooser.showOpenDialog(setupDialog);
            if (res != JFileChooser.APPROVE_OPTION) return;

            Path chosen = chooser.getSelectedFile().toPath();
            try {
                String gamemodeFromFile = extractGamemodeFromMapFile(chosen);

                String displayName = "Custom: " + chooser.getSelectedFile().getName().replaceFirst("\\.json$", "");
                DefaultComboBoxModel<String> customModel = new DefaultComboBoxModel<>(new String[] { displayName });
                mapDropdown.setModel(customModel);
                mapDropdown.setSelectedIndex(0);

                // remember custom file and mark state as custom
                customMapFile.set(chosen);
                mapIsCustom.set(true);

                // Set the GameMode to the gamemode in the file (if found) and lock gamemode selection
                if (gamemodeFromFile != null && !gamemodeFromFile.isBlank()) {
                    GameMode.setCurrentGamemode(gamemodeFromFile);
                    // try to reflect change in UI (if the dropdown contains that item)
                    try {
                        gameModeDropdown.setSelectedItem(gamemodeFromFile);
                    } catch (Exception ignore) {}
                }
                gameModeDropdown.setEnabled(false);

                setupDialog.validate();
                setupDialog.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(setupDialog,
                        "Failed to load custom map file.",
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        mapPanel.add(customMapButton);

        mapDropdown.addActionListener(e -> {
            // keep gamemode locked if its custom map
            gameModeDropdown.setEnabled(!mapIsCustom.get());
        });

        // NPC selectors (hidden unless needed)
        JPanel npcSinglePanel = new JPanel();
        stylePanel.accept(npcSinglePanel);
        JLabel npcSingleLabel = new JLabel("NPC Faction:");
        styleLabel.accept(npcSingleLabel);
        JComboBox<String> npcSingleDropdown = new JComboBox<>(GameMode.NPC_CLASSES.toArray(new String[0]));
        styleComboBox(npcSingleDropdown);
        npcSinglePanel.add(npcSingleLabel);
        npcSinglePanel.add(npcSingleDropdown);
        npcSinglePanel.setVisible(true);

        JPanel npcDualPanel = new JPanel();
        stylePanel.accept(npcDualPanel);
        JLabel npcLabelA = new JLabel("NPC Faction A:");
        styleLabel.accept(npcLabelA);
        JComboBox<String> npcDropdownA = new JComboBox<>(GameMode.NPC_CLASSES.toArray(new String[0]));
        styleComboBox(npcDropdownA);
        JLabel npcLabelB = new JLabel("NPC Faction B:");
        styleLabel.accept(npcLabelB);
        JComboBox<String> npcDropdownB = new JComboBox<>(GameMode.NPC_CLASSES.toArray(new String[0]));
        styleComboBox(npcDropdownB);
        npcDualPanel.add(npcLabelA);
        npcDualPanel.add(npcDropdownA);
        npcDualPanel.add(npcLabelB);
        npcDualPanel.add(npcDropdownB);
        npcDualPanel.setVisible(false);

        // Timer selection
        JPanel timerPanel = new JPanel();
        stylePanel.accept(timerPanel);
        JLabel timerLabel = new JLabel("Seconds per turn:");
        String timerHelpText = "The max time for you to move a piece from each chess set on your team. If this is taking too long, there is a skip button on the right sidebar.";
        styleLabel.accept(timerLabel);
        JSpinner timerSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        timerSpinner.setBackground(backgroundColor);
        timerSpinner.setForeground(foregroundColor);
        timerSpinner.setFont(defaultFont);
        timerPanel.add(timerLabel);
        timerPanel.add(createHelpIcon(timerHelpText));
        timerPanel.add(timerSpinner);
        styleSpinner(timerSpinner);

        // Fischer Random toggle
        JPanel fischerPanel = new JPanel();
        stylePanel.accept(fischerPanel);
        JCheckBox fischerCheck = new JCheckBox("Enable Fischer Random");
        fischerCheck.setBackground(backgroundColor);
        fischerCheck.setForeground(foregroundColor);
        fischerCheck.setFont(defaultFont);
        fischerCheck.setFocusPainted(false);
        fischerCheck.setSelected(false);
        fischerCheck.addActionListener(e -> GameMode.isFischerRandom = fischerCheck.isSelected());
        fischerPanel.add(fischerCheck);

        // NPC limiter selection
        JPanel NPCLimiterPanel = new JPanel();
        stylePanel.accept(NPCLimiterPanel);
        JLabel NPCLimiterLabel = new JLabel("How many pieces per set for NPCs? :");
        String NPCLimiterText = "This dictates how many chess pieces per set an NPC can move per turn. This only effects Human vs Npc Gamemode. Limit: 1 - 16.";
        styleLabel.accept(NPCLimiterLabel);
        JSpinner NPCLimiterSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 16, 1));
        NPCLimiterSpinner.setBackground(backgroundColor);
        NPCLimiterSpinner.setForeground(foregroundColor);
        NPCLimiterSpinner.setFont(defaultFont);
        NPCLimiterPanel.add(NPCLimiterLabel);
        NPCLimiterPanel.add(createHelpIcon(NPCLimiterText));
        NPCLimiterPanel.add(NPCLimiterSpinner);
        styleSpinner(NPCLimiterSpinner);

        gameModeDropdown.addActionListener(ev -> {
            String selectedModeName = Objects.requireNonNull(gameModeDropdown.getSelectedItem()).toString();
            GameMode.setCurrentGamemode(selectedModeName);

            // CLEAR any previous custom map selection because the user explicitly changed gamemode
//            customMapFile.set(null); //bad?
//            mapIsCustom.set(false); //bad?
            gameModeDropdown.setEnabled(true);

            String[] maps = GameMode.getAvailableMaps();
            DefaultComboBoxModel<String> mapModel = new DefaultComboBoxModel<>(maps);
            mapDropdown.setModel(mapModel);
            if (maps.length > 0) mapDropdown.setSelectedIndex(0);

            GameModeType mode = GameModeType.fromName(selectedModeName);
            boolean isHumanVsNpc = (mode == GameModeType.HUMAN_VS_NPC);
            boolean isNpcWar = (mode == GameModeType.NPC_WAR);

            npcSinglePanel.setVisible(isHumanVsNpc);
            npcDualPanel.setVisible(isNpcWar);

            if (isHumanVsNpc) {
                GameMode.selectedNPCFaction = Objects.requireNonNull((String) npcSingleDropdown.getSelectedItem());
            } else if (isNpcWar) {
                String a = Objects.requireNonNull((String) npcDropdownA.getSelectedItem());
                String b = Objects.requireNonNull((String) npcDropdownB.getSelectedItem());
                GameMode.npcFactionA = a;
                GameMode.npcFactionB = b;
                MoveHistory.shouldRecordHistory = false;
            }
            setupDialog.validate();
            setupDialog.repaint();
        });

        npcSingleDropdown.addActionListener(e -> GameMode.selectedNPCFaction = Objects.requireNonNull((String) npcSingleDropdown.getSelectedItem()));
        npcDropdownA.addActionListener(e -> GameMode.npcFactionA = Objects.requireNonNull((String) npcDropdownA.getSelectedItem()));
        npcDropdownB.addActionListener(e -> GameMode.npcFactionB = Objects.requireNonNull((String) npcDropdownB.getSelectedItem()));

        // Start button
        JButton startButton = new JButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setBackground(accentColor);
        startButton.setForeground(foregroundColor);
        startButton.setFocusPainted(false);
        startButton.setFont(defaultFont);
        startButton.addActionListener(e -> {
            GameMode.playerCountFull = (int) playerSpinner.getValue();
            int commanderCountFull = 2;

            if (mapIsCustom.get()){
                WorldMap.currentMapPreset = GameModeMapType.CUSTOM;
                CustomMapPreset.mapPath = customMapFile.get();
            } else {

                // map string -> world map enum (caller code used uppercase / valueOf previously)
                String chosenMap = Objects.requireNonNull(mapDropdown.getSelectedItem()).toString();
                try {
                    WorldMap.currentMapPreset = GameModeMapType.valueOf(chosenMap.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    // fallback: try to normalize (remove spaces / punctuation) or log and use default
                    try {
                        WorldMap.currentMapPreset = GameModeMapType.valueOf(chosenMap.replaceAll("\\s+", "").toUpperCase());
                    } catch (Exception ex2) {
                        WorldMap.currentMapPreset = GameModeMapType.DIAMOND; // safe default
                    }
                }
            }

            String selMode = Objects.requireNonNull(gameModeDropdown.getSelectedItem()).toString();
            GameMode.setCurrentGamemode(selMode);
            GameMode.setNewGamemodeColor();
            Timer.maxTimePerTurn = ((Number) timerSpinner.getValue()).doubleValue();

            GameMode.selectedNPCFaction = Objects.requireNonNull((String) npcSingleDropdown.getSelectedItem());
            GameMode.npcFactionA = Objects.requireNonNull((String) npcDropdownA.getSelectedItem());
            GameMode.npcFactionB = Objects.requireNonNull((String) npcDropdownB.getSelectedItem());
            NPC.setLimiter = (int) NPCLimiterSpinner.getValue();

            GameMode.playerCountFull -= commanderCountFull;
            GameMode.playerCountOneTeam = GameMode.playerCountFull / 2;
            GameMode.commanderCountOneTeam = commanderCountFull / 2;

            setupDialog.dispose();
            StartGame.startGame(frame, false, false);
        });

        setupDialog.add(Box.createVerticalStrut(8));
        setupDialog.add(playerPanel);
        setupDialog.add(gameModePanel);
        setupDialog.add(mapPanel);
        setupDialog.add(npcSinglePanel);
        setupDialog.add(npcDualPanel);
        setupDialog.add(timerPanel);
        setupDialog.add(fischerPanel);
        setupDialog.add(NPCLimiterPanel);
        setupDialog.add(Box.createVerticalStrut(10));
        setupDialog.add(startButton);
        setupDialog.setVisible(true);
    }

    // Helper for dark theme combo box
    private static void styleComboBox(JComboBox<String> combo) {
        combo.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? new Color(70, 130, 180) : new Color(45, 45, 45));
                c.setForeground(Color.WHITE);
                return c;
            }
        });
        combo.setBackground(new Color(45, 45, 45));
        combo.setForeground(Color.WHITE);
    }

    private static void styleSpinner(JSpinner spinner) {
        spinner.setFont(defaultFont);

        // Style the text field inside the spinner
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            JTextField textField = defaultEditor.getTextField();
            textField.setBackground(backgroundColor);
            textField.setForeground(foregroundColor);
            textField.setCaretColor(foregroundColor);
            textField.setBorder(BorderFactory.createLineBorder(accentColor, 1));
        }

        // Style the spinner buttons
        for (Component comp : spinner.getComponents()) {
            if (comp instanceof JButton btn) {
                btn.setBackground(backgroundColor.darker());
                btn.setForeground(foregroundColor);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(accentColor, 1));
            }
        }

        spinner.setBackground(backgroundColor);
        spinner.setForeground(foregroundColor);
    }

    private static JLabel createHelpIcon(String helpText) {
        JLabel help = new JLabel("?");
        help.setForeground(new Color(180, 180, 180));
        help.setFont(new Font("Segoe UI", Font.BOLD, 14));
        help.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

        help.setToolTipText("<html><body style='width:250px;'>" + helpText + "</body></html>");

        return help;
    }
}