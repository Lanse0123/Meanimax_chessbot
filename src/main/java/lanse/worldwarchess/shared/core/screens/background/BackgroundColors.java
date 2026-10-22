package lanse.worldwarchess.shared.core.screens.background;

import lanse.worldwarchess.shared.core.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BackgroundColors {

    public static List<Color> getBackgroundColors() {
        List<Color> colorList = new ArrayList<>();
        Color color1 = new Color(40, 40, 40);
        Color color2 = new Color(80, 80, 80);
        if (Timer.turnCounter % 2 == 0) {
            color1 = new Color(40, 40, 40);
        } else {
            color1 = new Color(2, 2, 2);
            color2 = new Color(50, 50, 50);
        }

        colorList.add(color1);
        colorList.add(color2);

        return colorList;
    }
}