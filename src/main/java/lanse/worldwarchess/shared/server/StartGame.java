package lanse.worldwarchess.shared.server;

import com.codedisaster.steamworks.SteamID;
import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.shared.central.CentralTick;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.client.SinglePlayerInitializer;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeMapType;
import lanse.worldwarchess.shared.core.gamemode.GameModeType;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.history.HistoryPanel;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.screens.pregame.GameLobbyWindow;
import lanse.worldwarchess.shared.core.screens.sandbox.SandPanel;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class StartGame {

    //This class is simply responsible for starting a game.

    public static void startGame(JFrame frame, boolean isHistoryMode, boolean isSandboxMode) {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            if (isHistoryMode){
                GameMode.isTimed = false;
                Main.gamePanel = new HistoryPanel();
                MoveHistory.shouldRecordHistory = false;
            } else if (isSandboxMode) {
                GameMode.isTimed = false;
                Main.gamePanel = new SandPanel();
                MoveHistory.shouldRecordHistory = false;
            } else {
                GameMode.isTimed = true;
                Main.gamePanel = new GamePanel();
            }

            frame.add(Main.gamePanel);
            frame.revalidate();
            frame.repaint();
            frame.setVisible(true);

            CentralTick.gameOver = false;
            Timer.gameStarted = false;
            Timer.turnCounter = 0;
            Timer.preGameCountdown = (GameMode.isSinglePlayer) ? 3.0 : 10.0;

            Timer.currentTime = Timer.maxTimePerTurn;
            Timer.playerIDMovedList.clear();

            CentralTick.isGameActive = true;
        });
    }

    public static void initializeAsHost(String name, String password, String questionText, int turnLength, String selectedMap,
                                        String gameMode, int maxPlayers, JFrame mainWindow, boolean isFrenzyGamemode,
                                        boolean isFogOfWar, String npcType) {

        GameMode.isSinglePlayer = false;
        Main.userIsHost = true;
        GameMode.commanderCountOneTeam = 1;
        GameMode.selectedNPCFaction = npcType;

        HostInitializer.initializeAsHost(name, password, questionText, turnLength, selectedMap, gameMode,
                maxPlayers, mainWindow, isFrenzyGamemode, isFogOfWar, npcType);
    }

    public static void initializeAsClient(GameLobby lobby, JFrame mainWindow) {
        GameMode.isSinglePlayer = false;
        Main.userIsHost = false;
        GameMode.commanderCountOneTeam = 1;

        SteamID server = lobby.getServerHostID();

        // Connect to host via cascading client logic
        int conn = CascadingClient.connectToHost(server, lobby.getChosenTeam(), Optional.empty());

        System.out.println("Connected to lobby: " + lobby.getLobbyID());
        SwingUtilities.invokeLater(() -> new GameLobbyWindow(lobby, conn, mainWindow));
    }

    public static void initializeSinglePlayer(JFrame frame) {
        System.out.println("Starting Single Player Mode.");
        GameMode.isSinglePlayer = true;
        SinglePlayerInitializer.initializeGame(frame);
    }

    public static void initializeTutorial(JFrame frame) {
        System.out.println("Starting Tutorial.");
        GameMode.isSinglePlayer = true;
        GameMode.isTutorial = true;
        Timer.maxTimePerTurn = 10;
        StartGame.startGame(frame, false, false);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //TODO - a helper can probably remove a lot of duplicated code here

    public static void startShadowGame(JFrame frame) {
        GameMode.playerCountFull = 2;
        int commanderCountFull = 2;

        WorldMap.currentMapPreset = GameModeMapType.SHADOW;

        GameMode.setCurrentGamemode(GameModeType.SIEGE.getDisplayName());
        GameMode.setNewGamemodeColor();
        Timer.maxTimePerTurn = 10;

        GameMode.playerCountFull -= commanderCountFull;
        GameMode.playerCountOneTeam = GameMode.playerCountFull / 2;
        GameMode.commanderCountOneTeam = commanderCountFull / 2;

        startGame(frame, false, false);
    }

    public static void startDebugGame(JFrame frame) {
        GameMode.playerCountFull = 2;
        int commanderCountFull = 2;

        WorldMap.currentMapPreset = GameModeMapType.DEBUG;

        GameMode.setCurrentGamemode(GameModeType.SIEGE.getDisplayName());
        GameMode.setNewGamemodeColor();
        Timer.maxTimePerTurn = 32767;

        GameMode.playerCountFull -= commanderCountFull;
        GameMode.playerCountOneTeam = GameMode.playerCountFull / 2;
        GameMode.commanderCountOneTeam = commanderCountFull / 2;

        startGame(frame, false, false);
    }

    public static void startBadGame(JFrame frame) {
        GameMode.playerCountFull = 2;
        int commanderCountFull = 2;

        WorldMap.currentMapPreset = GameModeMapType.BADAPPLE;

        GameMode.setCurrentGamemode(GameModeType.WAR.getDisplayName());
        GameMode.currentGamemodeColor2 = Color.WHITE;
        Timer.maxTimePerTurn = 0;

        GameMode.playerCountFull -= commanderCountFull;
        GameMode.playerCountOneTeam = GameMode.playerCountFull / 2;
        GameMode.commanderCountOneTeam = commanderCountFull / 2;

        startGame(frame, false, false);
    }

    public static void initializeSandbox(JFrame frame) {
        WorldMap.currentMapPreset = GameModeMapType.SANDBOX;
        GameMode.setCurrentGamemode(GameModeType.WAR.getDisplayName());

        Timer.maxTimePerTurn = Integer.MAX_VALUE - 1;
        GameMode.isSandbox = true;
        startGame(frame, false, true);
    }
}