package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;

public class Main {
    public static GamePanel gamePanel = new GamePanel();

    public static void main(String[] args) {
        CentralInitializer.initializeGame();
    }
}