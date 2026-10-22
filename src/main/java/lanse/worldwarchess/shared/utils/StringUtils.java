package lanse.worldwarchess.shared.utils;

import lanse.worldwarchess.shared.chat.ChatManager;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class StringUtils {
    public static String abbreviateString(String input, int maxLength) {
        if (input.length() <= maxLength)
            return input;
        else
            return input.substring(0, maxLength-1) + "…";
    }

    public static String abbreviateString(String input) {
        return abbreviateString(input, ChatManager.MAX_MSG_LEN);
    }

    public static void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // fallback for some linux environments
                Runtime.getRuntime().exec(new String[] { "xdg-open", url });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to open link:\n" + url);
        }
    }
}
