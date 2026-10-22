package lanse.worldwarchess.shared.core.worldmap.board.fairyboard;

import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class FairyBoard extends Board {

    public FairyBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, preset, playerID, playerID2, boardID, isCommander, shouldLinkNeighbors, false);
        this.boardSize = 15;

        FairyBoardPresets.setPreset(preset, boardX, boardY, playerID, playerID2, this, isCommander);
    }
}