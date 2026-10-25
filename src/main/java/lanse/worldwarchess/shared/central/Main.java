package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;

public class Main {
    public static GamePanel gamePanel = null;

    public static void main(String[] args) {
//        LineCounter.main();
        CentralInitializer.initializeGame();
    }
}