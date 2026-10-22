package lanse.worldwarchess.shared.core.screens.lounge;

import lanse.worldwarchess.shared.central.LogoManager;
import lanse.worldwarchess.shared.core.screens.lounge.diego.MainSpin;
import lanse.worldwarchess.shared.server.StartGame;

import javax.swing.*;
import java.awt.*;

public class DevLounge {


    public static void show(JFrame parent, JFrame grandparent) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Secret Dev Lounge >:)");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setMinimumSize(new Dimension(700, 520));
            frame.setLocationRelativeTo(parent);
            frame.setLayout(null);
            frame.setIconImages(LogoManager.getLogos());

            // background panel (dark theme)
            JPanel content = new JPanel(null);
            content.setBackground(new Color(30, 30, 30));
            frame.setContentPane(content);

            // Title
            JLabel title = new JLabel("Adding more stuff whenever I feel like it >:)", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setForeground(Color.WHITE);
            title.setBounds(0, 16, frame.getWidth(), 48);
            content.add(title);

            // Back / Close button
            JButton back = new JButton("Back");
            back.setBounds(14, 14, 90, 28);
            back.setFocusPainted(false);
            back.addActionListener(e -> frame.dispose());
            content.add(back);

            JButton basement = new JButton("<html>Basement Bossfight <span style='color:red;'>(DANGER, DO NOT ENTER)</span></html>");
            basement.setBounds(300, 400, 500, 40);
            basement.setForeground(new Color(50, 50, 50));
            basement.setBackground(new Color(0, 0, 0));
            basement.setFocusPainted(false);
            basement.addActionListener(e -> showBasementWarning(frame, parent, grandparent));
            content.add(basement);

            JButton debug = new JButton("Debug Map");
            debug.setBounds(300, 200, 500, 40);
            debug.setForeground(Color.WHITE);
            debug.setBackground(new Color(0, 0, 0));
            debug.setFocusPainted(false);
            debug.addActionListener(e -> startDebugGame(frame, parent, grandparent));
            content.add(debug);

            JButton badMap = new JButton("Bad Position...");
            badMap.setBounds(300, 300, 500, 40);
            badMap.setForeground(Color.WHITE);
            badMap.setBackground(new Color(0, 0, 0));
            badMap.setFocusPainted(false);
            badMap.addActionListener(e -> startBadGame(frame, parent, grandparent));
            content.add(badMap);

            JButton diegoButton = new JButton("Speen");
            diegoButton.setBounds(20, 400, 100, 30);
            diegoButton.setForeground(Color.WHITE);
            diegoButton.setBackground(new Color(0, 0, 0));
            diegoButton.setFocusPainted(false);
            diegoButton.addActionListener(e -> MainSpin.show());
            content.add(diegoButton);

            // Panel where icons will be placed (we use absolute positions relative to this panel)
            int panelX = 40;
            int panelY = 140;
            int panelW = frame.getWidth() - panelX * 2;
            int panelH = frame.getHeight() - panelY - 40;

            JPanel iconsPanel = new JPanel(null);
            iconsPanel.setOpaque(false);
            iconsPanel.setBounds(panelX, panelY, panelW, panelH);
            content.add(iconsPanel);

            // Recompute positions if window is resized
            frame.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    int w = frame.getWidth();
                    int h = frame.getHeight();
                    title.setBounds(0, 16, w, 48);
                    int pW = w - panelX * 2;
                    int pH = h - panelY - 40;
                    iconsPanel.setBounds(panelX, panelY, pW, pH);
                }
            });

            frame.setVisible(true);
        });
    }

    private static void showBasementWarning(JFrame frame, JFrame parent, JFrame grandparent) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                "Are you sure you want to enter the basement?",
                "Warning: Basement Bossfight",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            StartGame.startShadowGame(grandparent);
            parent.dispose();
            frame.dispose();
        }
    }

    private static void startDebugGame(JFrame frame, JFrame parent, JFrame grandparent){
        StartGame.startDebugGame(grandparent);
        parent.dispose();
        frame.dispose();
    }

    private static void startBadGame(JFrame frame, JFrame parent, JFrame grandparent){
        StartGame.startBadGame(grandparent);
        parent.dispose();
        frame.dispose();
    }
}
