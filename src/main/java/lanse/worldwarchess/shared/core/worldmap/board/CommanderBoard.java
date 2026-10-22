package lanse.worldwarchess.shared.core.worldmap.board;

import java.awt.*;

public class CommanderBoard extends Board {

    //This represetns a large chess board. It works pretty much the same as a Board.

    /** REMEMBER TO ADD TO THE BoardClassPrefixMap.java WHEN ADDING A NEW BOARD CLASS*/

    public CommanderBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, preset, playerID, playerID2, boardID, true, false, true);
        this.boardSize = 16;
        this.tileScale = 2;
        this.borderColor = new Color(255, 215, 0);
    }
}