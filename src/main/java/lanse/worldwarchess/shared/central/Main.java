package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;

public class Main {
    public static GamePanel gamePanel = new GamePanel();
    public static boolean userIsHost = false;
    public static boolean debugMode;

    public static void main(String[] args) {
        boolean launchedFromSteam = args.length > 1 && args[1].equals("libraries=steam");
        debugMode = !launchedFromSteam;

        CentralInitializer.initializeGame(launchedFromSteam);
    }
}