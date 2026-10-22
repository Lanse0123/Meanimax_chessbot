package lanse.worldwarchess.shared.core.worldmap.board;

import java.awt.*;

public class EmptyBoard extends SiegeBoard{

    public EmptyBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, preset, playerID, playerID2, boardID, false, false, false);
        this.borderColor = new Color(70, 70, 70);
    }

    @Override
    public void tick() {}
}
