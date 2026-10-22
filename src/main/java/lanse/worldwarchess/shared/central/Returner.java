package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.piece.marker.Marker;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.screens.pregame.MainMenu;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.BoardPresets;

import javax.swing.*;

public class Returner {

    //Class broken up from a large main class, responsible for returning you to the main menu, and resetting a lot
    // of values that should not be full.

    public static void clearGameData() {
        CentralTick.isGameActive = false;
        Timer.gameStarted = false;
        DeathMarker.deathRow.clear();
        MoveHistory.moveRecords.clear();
        MoveHistory.shouldRecordHistory = false;
        NPC.attackedSquares.clear();
        WorldMap.boards.clear();
        WorldMap.pieces.clear();
        BoardPresets.resetOrder();
        MapShapePresets.playerIDCount = 1;
        MapShapePresets.boardID = 1;
        Marker.markers.clear();
        RenderCache.clear();
        CentralTick.tickCount = 0;
    }

    public static void returnToMainMenu() {
        if (Main.gamePanel != null) {
            if (SwingUtilities.getWindowAncestor(Main.gamePanel) != null){
                SwingUtilities.getWindowAncestor(Main.gamePanel).dispose();
            }
            Main.gamePanel = new GamePanel();
        }

        clearGameData();

        MainMenu.instance.setJMenuBar(null);

        // Open the Main Menu
        SwingUtilities.invokeLater(() -> MainMenu.instance.reset(false));
    }
}
