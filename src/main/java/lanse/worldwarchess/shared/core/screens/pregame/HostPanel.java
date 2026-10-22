package lanse.worldwarchess.shared.core.screens.pregame;

import lanse.worldwarchess.shared.chat.ChatFilter;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeType;
import lanse.worldwarchess.shared.server.StartGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class HostPanel {

    private final JPanel panel;
    private final Runnable onStartSuccess;
    private final Runnable onBack;
    private final JFrame mainWindow;

    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color ACCENT = new Color(100, 160, 255);
    private static final Color FIELD_BG = new Color(55, 55, 55);
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private static final String passwordHider = """
            According to all known laws of aviation, there is no way a bee should be able to fly.
             Its wings are too small to get its fat little body off the ground. The bee, of course, flies anyway
             because bees don't care what humans think is impossible. Why are you still typing? I will loop here.""";

    public HostPanel(Runnable onStartSuccess, Runnable onBack, JFrame mainWindow) {
        this.onStartSuccess = onStartSuccess;
        this.onBack = onBack;
        this.mainWindow = mainWindow;
        this.panel = buildPanel();
    }

    private JPanel buildPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = createGBC();

        // Title
        JLabel title = new JLabel("Host a New Game");
        title.setFont(FONT.deriveFont(Font.BOLD, 22f));
        title.setForeground(ACCENT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 25, 0);
        panel.add(title, gbc);

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridy++;

        // Fields
        JTextField roomNameField = styledTextField();
        JPasswordField passwordField = styledPasswordField();
        JSpinner turnLengthField = styledSpinner(30, 10, 180, 1);
        JComboBox<String> mapSelect = styledComboBox(GameMode.getAvailableMaps());
        JSpinner maxPlayersSpinner = styledSpinner(10, 2, 1000000, 1);
        JComboBox<String> gamemodeSelect = styledComboBox(GameMode.getAvailableGameModes(true));
        GameMode.setCurrentGamemode(Objects.requireNonNull(gamemodeSelect.getSelectedItem()).toString());
        JCheckBox frenzySelector = styledCheckBox("Frenzy Mode?");
        frenzySelector.setToolTipText("If enabled, each player gains control of each piece on their team, like in singleplayer. Otherwise, you only control your 1 set of pieces.");
        JCheckBox fogOfWarSelector = styledCheckBox("Fog of War?");
        fogOfWarSelector.setToolTipText("If enabled, you can only see the pieces that you are attacking.");

        JPanel npcPanel = new JPanel();
        npcPanel.setOpaque(false);
        JLabel npcLabel = new JLabel("NPC Faction:");
        npcLabel.setForeground(TEXT_COLOR);
        JComboBox<String> npcDropdown = styledComboBox(GameMode.NPC_CLASSES.toArray(new String[0]));
        npcPanel.add(npcLabel);
        npcPanel.add(npcDropdown);
        npcPanel.setVisible(false);

        // Password + Show toggle
        JPanel passwordRow = new JPanel(new BorderLayout(5, 0));
        passwordRow.setOpaque(false);
        passwordRow.add(passwordField, BorderLayout.CENTER);

        JButton showPasswordBtn = styledButton("Show", FIELD_BG, TEXT_COLOR);
        showPasswordBtn.setFont(FONT.deriveFont(12f));
        showPasswordBtn.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        showPasswordBtn.setBackground(new Color(70, 70, 70));
        showPasswordBtn.setFocusPainted(false);
        showPasswordBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final boolean[] showing = {false};
        showPasswordBtn.addActionListener(e -> {
            showing[0] = !showing[0];
            if (passwordField instanceof PasswordField custom) {
                custom.setShowPassword(showing[0]);
            } else {
                passwordField.setEchoChar(showing[0] ? (char) 0 : ' ');
            }
            showPasswordBtn.setText(showing[0] ? "Hide" : "Show");
        });
        passwordRow.add(showPasswordBtn, BorderLayout.EAST);

        // Questionaire
        JTextField questionText = styledTextField();
        questionText.setToolTipText("This is a true or false question to assign everyone to their teams.");

        // Layout fields
        addRow(panel, gbc, "Room Name:", roomNameField);
        addRow(panel, gbc, "Password (Optional):", passwordRow);
        addRow(panel, gbc, "Team questionaire (Optional):", questionText);
        addRow(panel, gbc, "Turn Length (sec):", turnLengthField);
        addRow(panel, gbc, "Gamemode:", gamemodeSelect);
        addRow(panel, gbc, "NPC Faction:", npcPanel);
        addRow(panel, gbc, "Select Map:", mapSelect);
        addRow(panel, gbc, "Max Players:", maxPlayersSpinner);
        addRow(panel, gbc, "", frenzySelector);
        addRow(panel, gbc, "", fogOfWarSelector);

        // Buttons
        JButton startButton = styledButton("Start Game", ACCENT, Color.WHITE);
        JButton backButton = styledButton("Back", new Color(90, 90, 90), TEXT_COLOR);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(startButton);
        buttonPanel.add(backButton);

        gbc.gridy++;
        gbc.insets = new Insets(25, 0, 0, 0);
        panel.add(buttonPanel, gbc);

        // Actions
        startButton.addActionListener(e -> {
            String roomName = ChatFilter.cleanInput(roomNameField.getText().trim());
            String password = new String(passwordField.getPassword()).trim();
            int turnLength = (int) turnLengthField.getValue();
            String selectedMap = (String) mapSelect.getSelectedItem();
            String gameMode = (String) gamemodeSelect.getSelectedItem();
            int maxPlayers = (int) maxPlayersSpinner.getValue();
            boolean isFrenzy = frenzySelector.isSelected();
            boolean isFogOfWar = fogOfWarSelector.isSelected();
            String npcType = (String) npcDropdown.getSelectedItem();

            if (roomName.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Room name is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            StartGame.initializeAsHost(roomName, password, questionText.getText(), turnLength, selectedMap, gameMode,
                    maxPlayers, mainWindow, isFrenzy, isFogOfWar, npcType);
            if (onStartSuccess != null) onStartSuccess.run();
        });

        gamemodeSelect.addActionListener(ev -> {
            String selectedModeName = Objects.requireNonNull(gamemodeSelect.getSelectedItem()).toString();
            GameModeType mode = GameModeType.fromName(selectedModeName);

            boolean isHumanVsNpc = (mode == GameModeType.HUMAN_VS_NPC);
            npcPanel.setVisible(isHumanVsNpc);

            if (isHumanVsNpc) {
                GameMode.selectedNPCFaction = Objects.requireNonNull((String) npcDropdown.getSelectedItem());
            }

            panel.revalidate();
            panel.repaint();
        });

        gamemodeSelect.addActionListener(ev -> {
            String selectedModeName = Objects.requireNonNull(gamemodeSelect.getSelectedItem()).toString();
            GameMode.setCurrentGamemode(selectedModeName);

            // refresh maps
            String[] maps = GameMode.getAvailableMaps();
            DefaultComboBoxModel<String> mapModel = new DefaultComboBoxModel<>(maps);
            mapSelect.setModel(mapModel);
            if (maps.length > 0) mapSelect.setSelectedIndex(0);

            // show/hide NPC selection UI
            GameModeType mode = GameModeType.fromName(selectedModeName);
            boolean isHumanVsNpc = (mode == GameModeType.HUMAN_VS_NPC);

            npcPanel.setVisible(isHumanVsNpc);

            // update selected NPC defaults into GameMode
            if (isHumanVsNpc) {
                GameMode.selectedNPCFaction = Objects.requireNonNull((String) npcDropdown.getSelectedItem());
            }

            panel.validate();
            panel.repaint();
        });

        backButton.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        return panel;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public JPanel getPanel() {
        return panel;
    }

    private JTextField styledTextField() {
        JTextField f = new JTextField(15);
        styleComponent(f);
        f.setCaretColor(Color.WHITE); // visible cursor
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new PasswordField(15);
        styleComponent(f);
        f.setCaretColor(ACCENT);
        return f;
    }

    private JSpinner styledSpinner(int value, int min, int max, int step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        styleComponent(((JSpinner.DefaultEditor) s.getEditor()).getTextField());
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setCaretColor(Color.WHITE);
        return s;
    }

    private JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        styleComponent(box);
        return box;
    }

    private JCheckBox styledCheckBox(String label) {
        JCheckBox c = new JCheckBox(label);
        c.setOpaque(false);
        c.setForeground(TEXT_COLOR);
        c.setFont(FONT);
        return c;
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(FONT.deriveFont(Font.BOLD, 14f));
        b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleComponent(JComponent comp) {
        comp.setFont(FONT);
        comp.setBackground(FIELD_BG);
        comp.setForeground(TEXT_COLOR);
        comp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private static JLabel createHelpIcon(String helpText) {
        JLabel help = new JLabel("?");
        help.setForeground(new Color(180, 180, 180));
        help.setFont(new Font("Segoe UI", Font.BOLD, 14));
        help.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

        help.setToolTipText("<html><body style='width:250px;'>" + helpText + "</body></html>");

        return help;
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent comp) {
        gbc.gridy++;
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        if (!label.isEmpty()) {
            JLabel lbl = new JLabel(label);
            lbl.setFont(FONT);
            lbl.setForeground(TEXT_COLOR);
            row.add(lbl, BorderLayout.WEST);
        }

        row.add(comp, BorderLayout.CENTER);
        panel.add(row, gbc);
    }

    /**THIS WAS SO WORTH IT**/
    private static class PasswordField extends JPasswordField {
        private boolean showPassword = false;

        public PasswordField(int columns) {
            super(columns);
            setEchoChar(' ');
            setCaretColor(Color.WHITE);
        }

        public void setShowPassword(boolean show) {
            this.showPassword = show;
            setEchoChar(show ? (char) 0 : ' ');
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            super.paintComponent(g);

            if (!showPassword) {
                g2.setFont(getFont());
                g2.setColor(getForeground());

                char[] pass = getPassword();
                StringBuilder masked = new StringBuilder();
                int maskLen = passwordHider.length();

                for (int i = 0; i < pass.length; i++) {
                    masked.append(passwordHider.charAt(i % maskLen));
                }

                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int x = insets.left;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2.drawString(masked.toString(), x, y);
            }
            g2.dispose();
        }
    }
}