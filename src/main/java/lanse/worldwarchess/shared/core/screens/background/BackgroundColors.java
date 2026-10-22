package lanse.worldwarchess.shared.core.screens.background;

import lanse.worldwarchess.shared.core.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BackgroundColors {

    public static Color firstBackgroundColor = new Color(40, 40, 40);
    public static Color secondBackgroundColor = new Color(80, 80, 80);

    public static List<Color> getBackgroundColors() {
        List<Color> colorList = new ArrayList<>();
        Color color1;
        Color color2 = secondBackgroundColor;
        if (Timer.turnCounter % 2 == 0) {
            color1 = firstBackgroundColor;
        } else {
            color1 = new Color(2, 2, 2);
            color2 = new Color(50, 50, 50);
        }

        colorList.add(color1);
        colorList.add(color2);

        return colorList;
    }
}