package lanse.worldwarchess.shared.core.screens.ingame;

import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.central.Returner;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import javax.swing.*;
import java.awt.*;

public class SidebarMenu extends JPanel {

    //This is the class responsible for the sidebar on the RIGHT side of the screen, giving you a few button options.

    public boolean isOpen = false;
    private final int openWidth = 150;
    private final int closedWidth = 30;
    private final JButton toggleButton;
    private final JPanel menuPanel;
    public boolean slowMode = false;

    public SidebarMenu() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        toggleButton = new JButton(">>");
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(e -> toggleMenu());

        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(9, 9, 9));

        JButton homeButton = createSidebarButton("<html>Home<br></html>");
        JButton settingsButton = createSidebarButton("<html>Settings<br></html>");
        JButton resetCameraButton = createSidebarButton("<html>Reset<br>Camera</html>");
        JButton pauseGameButton = createSidebarButton("<html>Pause<br>Game</html>");

        homeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetCameraButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        homeButton.addActionListener(e -> returnToMainMenu());
        settingsButton.addActionListener(e -> Settings.showSettingsWindow());
        resetCameraButton.addActionListener(e -> resetCamera());
        pauseGameButton.addActionListener(e -> pauseGame());

        menuPanel.add(homeButton);
        menuPanel.add(Box.createVerticalStrut(7));
        menuPanel.add(settingsButton);
        menuPanel.add(Box.createVerticalStrut(7));
        menuPanel.add(resetCameraButton);
        menuPanel.add(Box.createVerticalStrut(7));

        add(toggleButton, BorderLayout.WEST);
        updateSize();
    }

    private void pauseGame() {
        Timer.isGamePaused = !Timer.isGamePaused;
    }

    public void toggleMenu() {
        isOpen = !isOpen;
        removeAll();
        add(toggleButton, BorderLayout.WEST);

        if (isOpen) {
            toggleButton.setText(">>");
            add(menuPanel, BorderLayout.CENTER);
        } else {
            toggleButton.setText("<<");
        }

        updateSize();
        revalidate();
        repaint();
    }

    private void updateSize() {
        int width = isOpen ? openWidth : closedWidth;
        setPreferredSize(new Dimension(width, getHeight()));
        setMinimumSize(new Dimension(width, 0));
        setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
    }

    private void resetCamera() {
        GamePanel.cameraX = 0;
        GamePanel.cameraY = 0;

        if (Main.gamePanel != null) {
            Main.gamePanel.repaint();
        }
    }

    private void returnToMainMenu() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to quit?",
                "Warning",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            Returner.returnToMainMenu();
        }
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0x3a3a3a));
        button.setForeground(new Color(0xdddddd));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0x3c3c3c)));
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
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
}
