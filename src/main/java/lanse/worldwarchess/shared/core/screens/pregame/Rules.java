package lanse.worldwarchess.shared.core.screens.pregame;

import lanse.worldwarchess.shared.central.LogoManager;
import lanse.worldwarchess.shared.utils.StringUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class Rules {
    JFrame frame;
    JEditorPane rules;
    public static void show() {
        new Rules().do_show();
    }

    public void do_show() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Rules");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setIconImages(LogoManager.getLogos());
            frame.setMinimumSize(new Dimension(700, 500));

            rules = null;
            URL rulesURL = Rules.class.getResource("/docs/Rules.html");
            try {
                rules = new JEditorPane(rulesURL);
                rules.setEditable(false);
            } catch (IOException e) {
                System.err.println("Rules file inaccessible at " + rulesURL);
                throw new RuntimeException(e);
            }
            rules.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    StringUtils.openUrl(e.getURL().toString());
                }
            });
            JScrollPane pane = new JScrollPane(rules);
            // this makes scrolling a bit slower, but it feels much smoother
            pane.getVerticalScrollBar().setUnitIncrement(8);
            frame.setContentPane(pane);
            frame.setVisible(true);
        });
    }
}
