package lanse.worldwarchess.shared.core.worldmap;

import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.worldmap.board.*;
import lanse.worldwarchess.shared.core.worldmap.board.badboard.BadBoard;
import lanse.worldwarchess.shared.core.worldmap.board.fairyboard.FairyBoard;

public class MapShapePresets {

    //This class is responsible for dynamically setting up the map based on given inputs.
    public static final int TILE_SIZE = 80;
    public static final int GAP = 25;

    public static int playerIDCount = 1;
    public static int boardID = 1;

    //new boards will add themselves to the WorldMap of boards.
    private static void addBoard(int x, int y, String type, int p1, int p2, String boardType) {
        switch (boardType.toLowerCase()) {
            case "commander" -> new CommanderBoard(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, false);
            case "board" -> new Board(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, true);
            case "inferno" -> new InfernoBoard(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, false);
            case "winter" -> new WinterBoard(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, false);
            case "siege" -> new SiegeBoard(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, false);
            case "shadow" -> new ShadowBoard(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, false);
            case "fairy" -> new FairyBoard(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, false);
            case "conway" -> new ConwayBoard(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, false);
            case "bad" -> new BadBoard(x, y, TILE_SIZE, type, p1, p2, boardID, false, false, false);
        }
        boardID++;
    }

    protected static void addBoard(int x, int y, String type, String boardType) {
        addBoard(x, y, type, playerIDCount, playerIDCount + 1, boardType);
        playerIDCount += 2;
    }

    //This is responsible for placing a row of boards.
    protected static void placeRow(int centerX, int rowY, int boardsThisRow, int boardWidth, String type) {
        int totalRowWidth = boardsThisRow * boardWidth + (boardsThisRow - 1) * GAP;
        int rowStartX = centerX - totalRowWidth / 2;
        for (int i = 0; i < boardsThisRow; i++) {
            int x = rowStartX + i * (boardWidth + GAP);
            String boardType = "board";
            addBoard(x, rowY, type, boardType);
        }
    }

    protected static void addCommanderBoards(int centerX, int baseY, int count, boolean topSide, String type) {
        if (count <= 0) return;
        int commanderWidth = 16 * TILE_SIZE;
        int commanderHeight = 16 * TILE_SIZE;
        int totalWidth = count * commanderWidth + (count - 1) * GAP;
        int startX = centerX - totalWidth / 2;
        int y = topSide ? baseY - GAP - commanderHeight : baseY + GAP;
        for (int i = 0; i < count; i++) {
            int x = startX + i * (commanderWidth + GAP);
            addBoard(x, y, type, "commander");
        }
    }

    //TODO - add more presets below here
    public static void diamondPreset() {
        int T = Math.max(GameMode.getBoardCount(true), 2);
        int N = (int) Math.floor((-1 + Math.sqrt(1 + 4 * T)) / 2);
        if (N < 1) N = 1;

        int boardWidth = 8 * TILE_SIZE;
        int boardHeight = 8 * TILE_SIZE;

        int borderCount = N;
        int borderStartX = 100;
        int borderRowY = 300;
        int totalBorderWidth = borderCount * boardWidth + (borderCount - 1) * GAP;
        int borderCenterX = borderStartX + totalBorderWidth / 2;

        // Border row
        placeRow(borderCenterX, borderRowY, borderCount, boardWidth, "Border");

        int remainingPlayers = T - borderCount * 2;
        int topCount = remainingPlayers / 2;
        int bottomCount = remainingPlayers / 2;

        // Top territory
        for (int r = 1; r < N && topCount > 0; r++) {
            int boardsThisRow = Math.min(N - r, topCount);
            int y = borderRowY - r * (boardHeight + GAP);
            placeRow(borderCenterX, y, boardsThisRow, boardWidth, "BlackTerritory");
            topCount -= boardsThisRow;
        }

        // Bottom territory
        for (int r = 1; r < N && bottomCount > 0; r++) {
            int boardsThisRow = Math.min(N - r, bottomCount);
            int y = borderRowY + r * (boardHeight + GAP);
            placeRow(borderCenterX, y, boardsThisRow, boardWidth, "WhiteTerritory");
            bottomCount -= boardsThisRow;
        }

        // Commander boards
        int commanderCount = GameMode.commanderCountOneTeam;
        if (commanderCount > 0) {
            int topY = borderRowY - (N - 1) * (boardHeight + GAP);
            int bottomY = borderRowY + (N - 1) * (boardHeight + GAP) + boardHeight;
            addCommanderBoards(borderCenterX, topY, commanderCount, true, "BlackTerritory");
            addCommanderBoards(borderCenterX, bottomY, commanderCount, false, "WhiteTerritory");
        }
    }

    public static void hourglassPreset() {
        int T = Math.max(GameMode.getBoardCount(true), 2);
        int boardWidth = 8 * TILE_SIZE;
        int boardHeight = 8 * TILE_SIZE;

        int centerX = 400;
        int centerY = 300;
        int borderY = centerY - boardHeight / 2;

        // Border board
        placeRow(centerX, borderY, 1, boardWidth, "Border");

        int topCount = (T - 2 + 1) / 2;
        int bottomCount = (T - 2) / 2;

        int lastTopRow = 0;
        for (int row = 1; topCount > 0; row++) {
            int boardsThisRow = Math.min(row, topCount);
            int y = borderY - row * (boardHeight + GAP);
            placeRow(centerX, y, boardsThisRow, boardWidth, "BlackTerritory");
            topCount -= boardsThisRow;
            lastTopRow = row;
        }

        int lastBottomRow = 0;
        for (int row = 1; bottomCount > 0; row++) {
            int boardsThisRow = Math.min(row, bottomCount);
            int y = borderY + boardHeight + GAP + (row - 1) * (boardHeight + GAP);
            placeRow(centerX, y, boardsThisRow, boardWidth, "WhiteTerritory");
            bottomCount -= boardsThisRow;
            lastBottomRow = row;
        }

        // Commander boards
        int commanderCount = GameMode.commanderCountOneTeam;
        if (commanderCount > 0) {
            int topY = borderY - lastTopRow * (boardHeight + GAP);
            int bottomY = borderY + boardHeight + GAP + (lastBottomRow - 1) * (boardHeight + GAP) + boardHeight;
            addCommanderBoards(centerX, topY, commanderCount, true, "BlackTerritory");
            addCommanderBoards(centerX, bottomY, commanderCount, false, "WhiteTerritory");
        }
    }

    public static void lineBattlePreset() {
        int T = Math.max(GameMode.getBoardCount(false), 2);
        int boardWidth = 8 * TILE_SIZE;
        int boardHeight = 8 * TILE_SIZE;

        int startX = 100;
        int startY = 300;
        int totalWidth = T * boardWidth + (T - 1) * GAP;
        int centerX = startX + totalWidth / 2;

        // Border line
        placeRow(centerX, startY, T, boardWidth, "Border");

        // Commander boards
        int commanderCount = GameMode.commanderCountOneTeam;
        if (commanderCount > 0) {
            addCommanderBoards(centerX, startY, commanderCount, true, "BlackTerritory");
            addCommanderBoards(centerX, startY + boardHeight, commanderCount, false, "WhiteTerritory");
        }
    }

    //Creates a preset with inferno boards in the middle, white and black above and below, and commanders above and below that.
    public static void infernoCrossingPreset(boolean isWinterPreset) {
        int boardW = 8 * TILE_SIZE;
        int boardH = 8 * TILE_SIZE;
        int infernoW = 2 * boardW;
        int infernoH = 2 * boardH;
        int groupW = 2 * boardW + GAP;

        int startX = 100;
        int centerY = 300;
        int infernoY = centerY - infernoH / 2;

        int topGroupTopY = infernoY - GAP - (2 * boardH + GAP);
        int bottomGroupTopY = infernoY + infernoH + GAP;

        int commanderCount = Math.max(GameMode.commanderCountOneTeam, 1);
        int totalBoards = Math.max(GameMode.getBoardCount(true), 8);

        int perSide = (int) Math.ceil(totalBoards / 2.0);
        int remainingBlack = perSide;
        int remainingWhite = perSide;

        int clusters = Math.max(1, (int) Math.ceil(perSide / 4.0));
        int totalWidth = clusters * groupW + (clusters - 1) * GAP;
        int centerX = startX + totalWidth / 2;
        int firstClusterLeftX = centerX - totalWidth / 2;

        for (int i = 0; i < clusters; i++) {
            int clusterLeftX = firstClusterLeftX + i * (groupW + GAP);
            int clusterCenterX = clusterLeftX + groupW / 2;

            int infernoX = clusterCenterX - infernoW / 2;
            addBoard(infernoX, infernoY, "Empty", isWinterPreset ? "winter" : "inferno");

            int topGroupStartX = clusterCenterX - groupW / 2;
            int bottomGroupStartX = clusterCenterX - groupW / 2;

            if (remainingBlack > 0) {
                int boardsToPlace = Math.min(remainingBlack, 4);
                placePartial2x2("BlackTerritory", new int[]{topGroupStartX, topGroupTopY}, boardsToPlace);
                remainingBlack -= boardsToPlace;
            }

            if (remainingWhite > 0) {
                int boardsToPlace = Math.min(remainingWhite, 4);
                placePartial2x2("WhiteTerritory", new int[]{bottomGroupStartX, bottomGroupTopY}, boardsToPlace);
                remainingWhite -= boardsToPlace;
            }

            if (i == clusters / 2) {
                addCommanderBoards(centerX, topGroupTopY, commanderCount, true, "BlackTerritory");

                int bottomGroupBottomY = bottomGroupTopY + (2 * boardH + GAP);
                addCommanderBoards(centerX, bottomGroupBottomY, commanderCount, false, "WhiteTerritory");
            }
        }

        while (remainingBlack > 0 || remainingWhite > 0) {
            addBoard(centerX - boardW / 2, topGroupTopY - (boardH + GAP), "BlackTerritory", "board");
            addBoard(centerX - boardW / 2, bottomGroupTopY + (2 * boardH + GAP), "WhiteTerritory", "board");
            remainingBlack--; remainingWhite--;
        }
    }

    // Helper to place fewer than 4 boards in a 2x2 block pattern
    private static void placePartial2x2(String territoryType, int[] origin, int count) {
        int boardW = 8 * TILE_SIZE;
        int boardH = 8 * TILE_SIZE;
        int x = origin[0];
        int y = origin[1];

        if (count >= 1) addBoard(x, y, territoryType, "board");
        if (count >= 2) addBoard(x + boardW + GAP, y, territoryType, "board");
        if (count >= 3) addBoard(x, y + boardH + GAP, territoryType, "board");
        if (count >= 4) addBoard(x + boardW + GAP, y + boardH + GAP, territoryType, "board");
    }

    public static void commanderTesting() {
        addBoard(0, 0, "Border", "commander");
    }
    public static void fairyTesting() {
        addBoard(0, 0, "Border", "fairy");
    }

    public static void cityTesting() {
        int T = Math.max(GameMode.getBoardCount(true), 2);

        // Find smallest EVEN square dimension N×N >= T
        int N = (int) Math.ceil(Math.sqrt(T));
        if (N % 2 == 1) N++;

        int boardWidth = 8 * TILE_SIZE;
        int boardHeight = 8 * TILE_SIZE;

        int totalWidth = N * boardWidth + (N - 1) * GAP;
        int totalHeight = N * boardHeight + (N - 1) * GAP;

        int startX = 100;
        int startY = 300 - totalHeight / 2;
        int centerX = startX + totalWidth / 2;

        // Build square
        for (int row = 0; row < N; row++) {
            int y = startY + row * (boardHeight + GAP);
            for (int col = 0; col < N; col++) {
                int x = startX + col * (boardWidth + GAP);

                String territory = (row < N / 2) ? "BlackTerritory" : "WhiteTerritory";
                addBoard(x, y, territory, "board");
            }
        }

        int commanderCount = GameMode.commanderCountOneTeam;
        if (commanderCount > 0) {
            int bottomY = startY + totalHeight;
            addCommanderBoards(centerX, startY, commanderCount, true, "BlackTerritory");
            addCommanderBoards(centerX, bottomY, commanderCount, false, "WhiteTerritory");
        }
    }

    public static void conwayTesting() {
        addBoard(0, 0, "empty", "conway");
        addBoard(8 * TILE_SIZE + GAP, 24 * TILE_SIZE + GAP, "WhiteTerritory", "board");
    }

    public static void siegePreset() {
        int numNormalPlayers = (int) Math.max(Math.ceil((GameMode.getBoardCount(true)-1)/2.), 1);
        addBoard(-24 * TILE_SIZE, -32 * TILE_SIZE, "empty", "siege");
        addBoard(0, 32 * TILE_SIZE + GAP, "WhiteTerritory", "commander");
        for (int i = 0; i < numNormalPlayers; ++i) {
            addBoard(8 * TILE_SIZE + (8 * TILE_SIZE + GAP) * (i+1), 32 * TILE_SIZE + GAP, "WhiteTerritory", "board");
            addBoard(- (8 * TILE_SIZE  + GAP) * (i+1), 32 * TILE_SIZE + GAP, "WhiteTerritory", "board");
        }
    }

    public static void shadowPreset(){
        addBoard(-24 * TILE_SIZE, -32 * TILE_SIZE, "empty", "shadow");
        addBoard(0, 32 * TILE_SIZE + GAP, "WhiteTerritory", "commander");
    }
}