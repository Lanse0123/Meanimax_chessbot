package lanse.worldwarchess.shared.core.tutorial;

import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.central.Returner;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeMapType;
import lanse.worldwarchess.shared.core.gamemode.GameModeType;
import lanse.worldwarchess.shared.core.piece.ai.bosses.PointerPiece;
import lanse.worldwarchess.shared.core.piece.ai.bosses.security.c.h.e.c.k.SecurityCheck;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.piece.marker.Marker;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.screens.ingame.SidebarMenu;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.TeamCaster;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Tutorial {

    //This class starts a new game, and teaches the player how to play the game.

    public static List<TutorialTextBubble> bubbles = new CopyOnWriteArrayList<>();
    public static List<Integer> staticPhases = List.of(0, 1, 2, 6, 7, 9, 10);
    public static volatile int phase = -404;
    public static boolean fireItUp = false;

    private static volatile boolean clickRequested = false;
    private static volatile boolean nextPhase = false;

    private static double originalCameraX;
    private static double originalCameraY;
    private static double originalZoomFactor;
    public static Thread tutorialThread;

    public static void startTutorial(){
        Returner.clearGameData();

        GameMode.isTutorial = true;
        WorldMap.currentMapPreset = GameModeMapType.TUTORIAL;
        GameMode.currentMode = GameModeType.HUMAN_VS_NPC;

        phase = 0;

        tutorialThread = new Thread(() -> {
            try {

                welcomePlayer();

                explainGame();

                showThemTheWorld();

                waitForTheNoobUsingTheTutorialToKillAnEnemyKing();

                makeSureThePlayerDidNotCheatToKillTheKingBecauseThatCouldBeAPossibility();

                congratulateTheNoobPlayerForKillingTheirFirstEnemyKingUnlessItsAGoodPlayerSmurfingAndIsNotANoob();

                showPlayerCommanders();

                waitForTheNoobUsingTheTutorialToKillAnEnemyCOMMANDERKing();

                peekIntoTheFuture();

                tellPlayerTheyAreReadyForWARWhenInRealityTheyAreInfactNotReadyAndWillGetPTSDFlashbacksToTheNextFewGamesFor58Years();

                phase = 0XABC; // another part of the game in Timer.class will
                // notice once this line of code happens, and call Main.returnToMainMenu();
            }
            catch (InterruptedException ignored) {
            }
        }, "Tutorial");
        tutorialThread.start();
    }


    private static void welcomePlayer() throws InterruptedException {
        phase = 1;

        GamePanel.cameraX = 832;
        GamePanel.cameraY = 129;

        showMessage("Welcome to World War Chess!");
        waitForBubbleClick();
    }

    private static void explainGame() throws InterruptedException {
        phase = 2;

        new Board(0, 0, MapShapePresets.TILE_SIZE,"Border", 1000, 1001, 1000, false, true, true);
        TeamCaster.convertTeam("Black", TutorialPiece.class, CommanderTutorialPiece.class);

        showMessage("This game is like chess!");
        waitForBubbleClick();

        showMessage("Don't worry, it's easy!\nClick a piece to see where it can move.");
        waitForBubbleClick();

        sleep(3);
        Tutorial.nextPhase();
    }

    private static void showThemTheWorld() throws InterruptedException {
        phase = 3;
        bubbles.clear();

        TutorialMapPreset.tutorialBattlePreset(10, false);
        TeamCaster.convertTeam("Black", TutorialPiece.class, CommanderTutorialPiece.class);

        showMessage("Each player controls 1 set of pieces.\nSince this is multiplayer,\nthere are many boards!");
        waitForBubbleClick();

        showMessage("""
                You can move to other boards too!
                Even when your pieces travel far from home,
                you still control just that set
                of original home pieces.""");

        waitForBubbleClick();
    }

    private static void waitForTheNoobUsingTheTutorialToKillAnEnemyKing() throws InterruptedException {
        bubbles.clear();

        showMessage("For singleplayer,\nyou can control all of your team's pieces.");
        waitForBubbleClick();

        showMessage("Try to kill an enemy king!");
        waitForBubbleClick();
        phase = 4;

        //phase ends upon king death
        nextPhase = false;
        waitForPhaseCompletion();
    }

    private static void makeSureThePlayerDidNotCheatToKillTheKingBecauseThatCouldBeAPossibility() throws InterruptedException {
        phase = 5;
        sleep(2);
        SecurityCheck.securityCheck("Security Recheck. Safety and Security is a Priority! (or something)");
    }

    private static void congratulateTheNoobPlayerForKillingTheirFirstEnemyKingUnlessItsAGoodPlayerSmurfingAndIsNotANoob() throws InterruptedException {
        phase = 6;
        double[] cameraPos = Main.gamePanel.getCameraPos();

        // show congrats on left and then wait for click (unchanged semantics, but top bubble style)
        originalCameraX = cameraPos[0];
        originalCameraY = cameraPos[1];
        originalZoomFactor = Main.gamePanel.getZoomFactor();

        Main.gamePanel.setZoomFactor(1.0);

        showMessage("Congrats! You killed a player!");
        waitForBubbleClick();
    }

    private static void showPlayerCommanders() throws InterruptedException {
        phase = 7;
        TutorialMapPreset.tutorialBattlePreset(10, true);
        TeamCaster.convertTeam("Black", TutorialPiece.class, CommanderTutorialPiece.class);

        GamePanel.cameraX = 832;
        GamePanel.cameraY = 400;
        Main.gamePanel.setZoomFactor(0.1);

        showMessage("Each team has a commander.\nThey are big and powerful!");
        waitForBubbleClick();

        bubbles.clear();

        GamePanel.cameraX = 832;
        GamePanel.cameraY = 721;
        Main.gamePanel.setZoomFactor(0.45);

        showMessage("To win the game,\ndefeat the enemy commander king!");
        waitForBubbleClick();

        PointerPiece pointer = new PointerPiece(-404, WorldMap.getBoardByID(11), "king", "black", 0, 0, 6, -2, 6);
        WorldMap.spawnPiece(pointer, false);
        sleep(1);
        WorldMap.erasePiece(pointer, false);
        sleep(1);
        WorldMap.spawnPiece(pointer, false);
        sleep(1);
        WorldMap.erasePiece(pointer, false);
        sleep(1);
        WorldMap.spawnPiece(pointer, false);
        sleep(1);
        WorldMap.erasePiece(pointer, false);

    }

    private static void waitForTheNoobUsingTheTutorialToKillAnEnemyCOMMANDERKing() throws InterruptedException {
        phase = 8;
        showMessage("Try working together\nto defeat enemy commanders!");
        waitForBubbleClick();

        showMessage("Your damage is multiplied\nif multiple attack the same\ncommander at once!");
        waitForBubbleClick();

        Main.gamePanel.setCameraPos(originalCameraX, originalCameraY);
        Main.gamePanel.setZoomFactor(originalZoomFactor);

        fireItUp = true;
        Main.gamePanel.remove(Main.gamePanel.sidebarMenu);
        Main.gamePanel.sidebarMenu = new SidebarMenu();
        Main.gamePanel.add(Main.gamePanel.sidebarMenu, BorderLayout.EAST);

        showMessage("The Sidebar on the right \ncan help you if you need help!");
        waitForBubbleClick();

        //phase ends when commander king dies
        waitForPhaseCompletion();
    }

    private static void peekIntoTheFuture() throws InterruptedException {
        phase = 9;

        fireItUp = false;
        Main.gamePanel.remove(Main.gamePanel.sidebarMenu);
        Main.gamePanel.sidebarMenu = new SidebarMenu();
        Main.gamePanel.add(Main.gamePanel.sidebarMenu, BorderLayout.EAST);

        showMessage("Congrats!");
        waitForBubbleClick();
        Main.gamePanel.setZoomFactor(1.0);

        WorldMap.pieces.clear();
        WorldMap.boards.clear();

        GamePanel.cameraX = 832;
        GamePanel.cameraY = 129;

        new Board(0, 0, MapShapePresets.TILE_SIZE,"Border", 1, 2, 1, false, true, true);

        DeathMarker.deathRow.clear();
        Marker.markers.clear();
        TutorialBoardLooper.loopBoardPresets(phase);

        showMessage("There are many gamemodes");
        waitForBubbleClick();
        sleep(5);
    }

    private static void tellPlayerTheyAreReadyForWARWhenInRealityTheyAreInfactNotReadyAndWillGetPTSDFlashbacksToTheNextFewGamesFor58Years() throws InterruptedException {
        phase = 10;

        WorldMap.pieces.clear();
        WorldMap.boards.clear();
        bubbles.clear();

        showMessage("You are now ready.\nDestroy the enemy.\nTHIS. IS. WAR!!!");
        waitForBubbleClick();
    }

    public static void hasPhaseEnded(int phase) throws Exception {
        //crash the thread if phase has ended to end the thread early
        if (Tutorial.phase != phase){
            throw new Exception(){};
        }
    }

    private static void showMessage(String text) {
        bubbles.clear();

        int panelWidth = (Main.gamePanel != null) ? Main.gamePanel.getWidth() : 1000;
        int panelHeight = (Main.gamePanel != null) ? Main.gamePanel.getHeight() : 800;

        int bubbleWidth = Math.max(600, Math.min(1200, (int)(panelWidth * 0.8)));
        int bubbleHeight = 200;

        int x = (panelWidth - bubbleWidth) / 2;
        int y = Math.max(20, panelHeight / 10);

        bubbles.add(new TutorialTextBubble(text, TutorialTextBubble.Type.TEXT, x, y, bubbleWidth, bubbleHeight));
    }

    public static void sleep(int secondsTime) throws InterruptedException {
        Thread.sleep(secondsTime * 1000L);
    }

    public static void nextPhase() {
        nextPhase = true;
        bubbles.clear();
    }

    private static void waitForPhaseCompletion() throws InterruptedException {
        while (true) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Tutorial ended.");
            }
            if (nextPhase) {
                nextPhase = false;
                return;
            }
        }
    }

    public static void requestAdvanceByUI() {
        clickRequested = true;
    }

    private static void waitForBubbleClick() throws InterruptedException {
        while (true) {
            if (Thread.interrupted()) throw new InterruptedException("Tutorial ended.");
            if (clickRequested) {
                clickRequested = false; // consume event
                bubbles.clear();
                return;
            }
        }
    }
}