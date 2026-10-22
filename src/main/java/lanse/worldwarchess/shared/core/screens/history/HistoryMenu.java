package lanse.worldwarchess.shared.core.screens.history;

import dev.dirs.ProjectDirectories;
import lanse.worldwarchess.shared.central.LogoManager;
import lanse.worldwarchess.shared.central.Returner;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class HistoryMenu {

    private static final Path gameHistoryDir =
            Path.of(ProjectDirectories.from("dev", "Lanse", "WorldWarChess").dataDir);

    public static void show(JFrame parent) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Game Review");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setMinimumSize(new Dimension(700, 520));
            frame.setLocationRelativeTo(parent);
            frame.setLayout(null);
            frame.setIconImages(LogoManager.getLogos());

            JPanel content = new JPanel(null);
            content.setBackground(new Color(30, 30, 30));
            frame.setContentPane(content);

            JLabel title = new JLabel("Game Review (COMING SOON!)", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setForeground(Color.WHITE);
            title.setBounds(0, 40, frame.getWidth(), 50);
            content.add(title);

            int buttonWidth = 220;
            int buttonHeight = 45;
            int spacing = 25;

            int totalHeight = buttonHeight * 3 + spacing * 2;
            int centerY = frame.getHeight() / 2 - totalHeight / 2;
            int centerX = frame.getWidth() / 2 - buttonWidth / 2;

            JButton viewButton = createStyledButton("View Game");
            viewButton.setBounds(centerX, centerY, buttonWidth, buttonHeight);
            content.add(viewButton);

            JButton clearButton = createStyledButton("Clear History");
            clearButton.setBounds(centerX, centerY + buttonHeight + spacing, buttonWidth, buttonHeight);
            content.add(clearButton);

            JButton backButton = createStyledButton("Back");
            backButton.setBounds(centerX, centerY + (buttonHeight + spacing) * 2, buttonWidth, buttonHeight);
            content.add(backButton);

            JLabel sizeLabel = new JLabel("", SwingConstants.CENTER);
            sizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            sizeLabel.setForeground(Color.LIGHT_GRAY);
            sizeLabel.setBounds(0, frame.getHeight() - 100, frame.getWidth(), 30);
            content.add(sizeLabel);

            Runnable updateSize = () -> sizeLabel.setText("Total History Folder Size: " + getFolderSize());
            updateSize.run();

            //TODO - fix
//            viewButton.addActionListener(e -> {
//                try {
//                    if (!Files.exists(gameHistoryDir)) {
//                        JOptionPane.showMessageDialog(parent, "No history directory found.",
//                                "Error", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//
//                    JFileChooser chooser = new JFileChooser(gameHistoryDir.toFile());
//                    chooser.setDialogTitle("Select Game History File");
//                    int result = chooser.showOpenDialog(parent);
//
//                    if (result == JFileChooser.APPROVE_OPTION)
//                        HistoryData.initializeGame(chooser.getSelectedFile().toPath(), parent);
//
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(parent, "Error selecting file: " + ex.getMessage(),
//                            "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            });

            // === Clear History button ===
            clearButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        frame,
                        "Are you sure you want to clear all saved game history?\nThis cannot be undone.",
                        "Confirm Clear",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (Files.exists(gameHistoryDir)) {
                            List<Path> files = Files.list(gameHistoryDir).toList();
                            for (Path file : files) {
                                Files.deleteIfExists(file);
                            }
                            JOptionPane.showMessageDialog(frame, "History cleared successfully.",
                                    "Cleared", JOptionPane.INFORMATION_MESSAGE);
                            updateSize.run();
                        } else {
                            JOptionPane.showMessageDialog(frame, "No history found to clear.",
                                    "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to clear history: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            backButton.addActionListener(e -> {
                Returner.returnToMainMenu();
                frame.setVisible(false);
            });

            frame.setVisible(true);
            frame.revalidate();
            frame.repaint();

            parent.revalidate();
            parent.repaint();
        });
    }

    private static String getFolderSize() {
        if (!Files.exists(gameHistoryDir)) return "0 KB";
        try {
            AtomicLong size = new AtomicLong();
            Files.walk(gameHistoryDir).filter(Files::isRegularFile).forEach(p -> {
                try {
                    size.addAndGet(Files.size(p));
                } catch (IOException ignored) {}
            });
            long bytes = size.get();
            if (bytes > 1_000_000) return String.format("%.2f MB", bytes / 1_000_000.0);
            else if (bytes > 1_000) return String.format("%.1f KB", bytes / 1_000.0);
            else return bytes + " bytes";
        } catch (IOException e) {
            return "Error";
        }
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 80));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 60));
            }
        });
        return button;
    }
}