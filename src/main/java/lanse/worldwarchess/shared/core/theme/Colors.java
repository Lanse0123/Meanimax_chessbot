package lanse.worldwarchess.shared.core.theme;

import javax.swing.*;
import java.awt.*;

public class Colors {
    public static Color text_color = Color.WHITE;
    public static Color menu_color = Color.BLACK;

    public static void styleMenuBar(JMenuBar menu) {
        menu.setBackground(menu_color);
        menu.setForeground(text_color);
        for (int i = 0; i < menu.getMenuCount(); i++) {
            styleMenu(menu.getMenu(i));
        }
    }

    private static void styleMenu(JMenu menu) {
        menu.setBackground(menu_color);
        menu.setForeground(text_color);
        menu.getPopupMenu().setBackground(menu_color);
        for (Component comp : menu.getMenuComponents()) {
            if (comp instanceof JMenu submenu) {
                styleMenu(submenu);
            }
            else {
                comp.setBackground(menu_color);
                comp.setForeground(text_color);
            }
        }
    }

}
