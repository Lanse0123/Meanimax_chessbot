package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.cascading.CascadingHost;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeMapType;
import lanse.worldwarchess.shared.core.gamemode.GameModeType;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.piece.marker.Marker;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.screens.pregame.MainMenu;
import lanse.worldwarchess.shared.core.screens.sandbox.SandboxMapDataStack;
import lanse.worldwarchess.shared.core.tutorial.Tutorial;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.shared.core.worldmap.board.BoardPresets;
import lanse.worldwarchess.shared.server.LobbyServer;

import javax.swing.*;
import java.util.Optional;

public class Returner {

    //Class broken up from a large main class, responsible for returning you to the main menu, and resetting a lot
    // of values that should not be full.

    public static void clearGameData() {
        Main.userIsHost = false;
        CentralTick.isGameActive = false;
        GameMode.isFogOfWar = false;
        GameMode.isTutorial = false;
        GameMode.currentMode = GameModeType.WAR;
        GameMode.isSandbox = false;
        GameMode.isFischerRandom = false;
        Timer.gameStarted = false;
        Tutorial.fireItUp = false;
        DeathMarker.deathRow.clear();
        MoveHistory.moveRecords.clear();
        MoveHistory.shouldRecordHistory = false;
        WorldMap.minionPieceSpawnRequests.clear();
        WorldMap.minionPieceDestroyRequests.clear();
        SandboxMapDataStack.clearSandboxMapStackData();
        NPC.attackedSquares.clear();
        WorldMap.boards.clear();
        WorldMap.pieces.clear();
        BoardPresets.resetOrder();
        MapShapePresets.playerIDCount = 1;
        MapShapePresets.boardID = 1;
        CascadingHost.playerCount = 1;
        CascadingHost.lobbyServer = null;
        Marker.markers.clear();
        Tutorial.bubbles.clear();
        if (WorldMap.currentMapPreset != GameModeMapType.SHADOW) {
            Board.clearBoardCache();
        }
        WorldMap.currentMapPreset = GameModeMapType.DIAMOND;
        RenderCache.clear();
        CentralTick.tickCount = 0;
        Tutorial.phase = -404;
        CascadingClient.playerID = -4040404;
        CascadingClient.isAliveInChat = true;
    }

    public static void returnToMainMenu() {
        if (!GameMode.isSinglePlayer){
            CascadePacketManager.manager.sendPacketToEveryone(CascadePacketManager.PacketType.LEAVE, Settings.getLeaveMessage(), Optional.empty());
        }

        if (Main.gamePanel != null) {
            if (SwingUtilities.getWindowAncestor(Main.gamePanel) != null){
                SwingUtilities.getWindowAncestor(Main.gamePanel).dispose();
            }
            Main.gamePanel = new GamePanel();
        }

        clearGameData();

        if (Tutorial.tutorialThread != null) {
            Tutorial.tutorialThread.interrupt();
        }

        MainMenu.instance.setJMenuBar(null);

        // Open the Main Menu
        SwingUtilities.invokeLater(() -> MainMenu.instance.reset(false));

        LobbyServer.leaveCurrentLobby();
    }
}
