package lanse.worldwarchess.shared.core.screens.pregame;

import lanse.worldwarchess.Changelog;
import lanse.worldwarchess.shared.central.LogoManager;

import javax.swing.*;
import java.awt.*;

public class DevLogScreen {

    public static void show(JFrame parent) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("DevLog");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setMinimumSize(new Dimension(700, 520));
            frame.setLocationRelativeTo(parent);
            frame.setIconImages(LogoManager.getLogos());

            JPanel content = new JPanel(new BorderLayout());
            content.setBackground(new Color(30, 30, 30));
            frame.setContentPane(content);

            JLabel title = new JLabel("DevLog", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setForeground(Color.WHITE);
            title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            content.add(title, BorderLayout.NORTH);

            JTextArea plannedText = createDevLogTextArea(Changelog.plannedChanges);
            JTextArea changeText = createDevLogTextArea(Changelog.changeLog);

            JScrollPane leftScroll = createScrollPane(plannedText, "Planned Changes");
            JScrollPane rightScroll = createScrollPane(changeText, "Changelog");

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
            splitPane.setDividerLocation(frame.getWidth() / 2);
            splitPane.setDividerSize(4);
            splitPane.setContinuousLayout(true);
            splitPane.setBackground(new Color(40, 40, 40));
            content.add(splitPane, BorderLayout.CENTER);

            // Back button
            JButton back = new JButton("Back");
            back.addActionListener(e -> frame.dispose());
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bottom.setBackground(new Color(25, 25, 25));
            bottom.add(back);
            content.add(bottom, BorderLayout.SOUTH);

            frame.setVisible(true);
        });
    }

    private static JTextArea createDevLogTextArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Consolas", Font.PLAIN, 16));
        area.setBackground(new Color(40, 40, 40));
        area.setForeground(Color.WHITE);
        area.setCaretColor(Color.WHITE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private static JScrollPane createScrollPane(JTextArea area, String title) {
        JScrollPane pane = new JScrollPane(area);
        pane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                title, 0, 0, new Font("Segoe UI", Font.BOLD, 16), Color.BLACK
        ));
        return pane;
    }
}
