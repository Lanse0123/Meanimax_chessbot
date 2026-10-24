package lanse.worldwarchess.shared.core.worldmap;

import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class MapShapePresets {

    //This class is responsible for setting up the board.
    public static final int TILE_SIZE = 80;
    public static final int GAP = 25;

    public static int playerIDCount = 1;

    protected static void setBoard(int x, int y) {
        WorldMap.board = new Board(x, y, TILE_SIZE, "Border", 0, 1, false, true);
    }
}