package lanse.worldwarchess.shared.core.tutorial;

import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class TutorialMapPreset extends MapShapePresets {

    public static void tutorialBattlePreset(int boardCount, boolean showCommanders) {
        int boardWidth = 8 * TILE_SIZE;
        int boardHeight = 8 * TILE_SIZE;

        int centerX = -GAP;
        int startY = 0;
        int totalWidth = boardCount * boardWidth + (boardCount - 1) * GAP;

        boolean odd = boardCount % 2 == 1;
        int half = boardCount / 2; // number of boards to each side

        if (!showCommanders) {
            if (odd) {
                placeRow(centerX, startY, 1, boardWidth, "Border");
            }

            for (int i = 1; i <= half; i++) {
                int x = centerX - i * (boardWidth + GAP) + (boardWidth / 2) + GAP;
                placeRow(x, startY, 1, boardWidth, "Border");
            }

            for (int i = 1; i <= half; i++) {
                int x = centerX + i * (boardWidth + GAP) - boardWidth / 2;
                placeRow(x, startY, 1, boardWidth, "Border");
            }

            //lazy fix to overlapping board problem...
            WorldMap.boards.remove(6);
            WorldMap.pieces.removeIf(piece -> piece.getBoard().getBoardID() == 6);
        } else {
            // Commander boards
            int commanderCount = 1;
            int startX = centerX + totalWidth / 17;
            addCommanderBoards(startX, startY, commanderCount, true, "BlackTerritory");
            addCommanderBoards(startX, startY + boardHeight, commanderCount, false, "WhiteTerritory");
        }

        Board.linkAllBoardNeighbors();
    }
}
