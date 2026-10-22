package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.fairyboard.FairyBoard;
import lanse.worldwarchess.shared.core.worldmap.board.fairyboard.FairyBoardPresets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoardPresets {

    private static String[] order = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};

    //This is responsible for creating the pieces of a board depending on what preset the board has
    public static void setPreset(String preset, int boardX, int boardY, int playerID1, int playerID2, Board board, boolean isCommander) {
        if (board instanceof FairyBoard){
            FairyBoardPresets.setPreset(preset, boardX, boardY, playerID1, playerID2, board, isCommander);
            return;
        }

        if (GameMode.isFischerRandom && GameMode.isSinglePlayer){
            shuffleOrder();
        }

        switch (preset.toLowerCase()) {
            case "border" -> createBorderPreset(boardX, boardY, playerID1, playerID2, board, isCommander);
            case "whiteterritory" -> createWhiteTerritoryPreset(boardX, boardY, playerID1, board, isCommander);
            case "blackterritory" -> createBlackTerritoryPreset(boardX, boardY, playerID1, board, isCommander);
            case "empty", "siege" -> { return; }
            default -> throw new IllegalArgumentException("Unknown preset: " + preset);
        };
    }

    private static void createBorderPreset(int boardX, int boardY, int playerID, int playerID2, Board board, boolean isCommander) {
        List<Piece> pieces = new ArrayList<>();
        List<CommanderPiece> commanderPieces = new ArrayList<>();

        if (!isCommander) {
            addPawns(pieces, boardX, boardY, "White", playerID, board);
            addPawns(pieces, boardX, boardY, "Black", playerID2, board);
            addBackRow(pieces, boardX, boardY, "White", playerID, board);
            addBackRow(pieces, boardX, boardY, "Black", playerID2, board);
        } else {
            addCommanderPawns(commanderPieces, boardX, boardY, "White", playerID, board);
            addCommanderPawns(commanderPieces, boardX, boardY, "Black", playerID2, board);
            addCommanderBackRow(commanderPieces, boardX, boardY, "White", playerID, board);
            addCommanderBackRow(commanderPieces, boardX, boardY, "Black", playerID2, board);
        }

        // Add pieces to WorldMap
        WorldMap.pieces.addAll(pieces);
        WorldMap.pieces.addAll(commanderPieces);
    }

    private static void createWhiteTerritoryPreset(int boardX, int boardY, int playerID, Board board, boolean isCommander) {
        List<Piece> pieces = new ArrayList<>();
        List<CommanderPiece> commanderPieces = new ArrayList<>();
        if (!isCommander) {
            addPawns(pieces, boardX, boardY, "White", playerID, board);
            addBackRow(pieces, boardX, boardY, "White", playerID, board);
        } else {
            addCommanderPawns(commanderPieces, boardX, boardY, "White", playerID, board);
            addCommanderBackRow(commanderPieces, boardX, boardY, "White", playerID, board);
        }
        WorldMap.pieces.addAll(pieces);
        WorldMap.pieces.addAll(commanderPieces);
    }

    private static void createBlackTerritoryPreset(int boardX, int boardY, int playerID, Board board, boolean isCommander) {
        List<Piece> pieces = new ArrayList<>();
        List<CommanderPiece> commanderPieces = new ArrayList<>();
        if (!isCommander) {
            addPawns(pieces, boardX, boardY, "Black", playerID, board);
            addBackRow(pieces, boardX, boardY, "Black", playerID, board);
        } else {
            addCommanderPawns(commanderPieces, boardX, boardY, "Black", playerID, board);
            addCommanderBackRow(commanderPieces, boardX, boardY, "Black", playerID, board);
        }
        WorldMap.pieces.addAll(pieces);
        WorldMap.pieces.addAll(commanderPieces);
    }

    private static void addPawns(List<Piece> pieces, int boardX, int boardY, String team, int playerID, Board board) {
        int pawnRow = team.equals("White") ? 6 : 1;
        for (int i = 0; i < 8; i++) {
            pieces.add(new Piece(playerID, board, "Pawn", team, boardX, boardY, i, pawnRow, 1));
        }
    }

    private static void addBackRow(List<Piece> pieces, int boardX, int boardY, String team, int playerID, Board board) {
        int backRow = team.equals("White") ? 7 : 0;

        for (int i = 0; i < 8; i++) {
            pieces.add(new Piece(playerID, board, order[i], team, boardX, boardY, i, backRow, 1));
        }
    }

    private static void addCommanderPawns(List<CommanderPiece> pieces, int boardX, int boardY, String team, int playerID, Board board) {
        int pawnRow = team.equals("White") ? 12 : 2;
        for (int i = 0; i < 8; i++) {
            int column = 2 * i;
            pieces.add(new CommanderPiece(playerID, board, "Pawn", team, boardX, boardY, column, pawnRow, 1));
        }
    }

    private static void addCommanderBackRow(List<CommanderPiece> pieces, int boardX, int boardY, String color, int playerID, Board board) {
        int backRow = color.equals("White") ? 14 : 0;
        for (int i = 0; i < 8; i++) {
            int column = 2 * i;  // Place back row pieces at 1, 3, 5, ..., 15.
            pieces.add(new CommanderPiece(playerID, board, order[i], color, boardX, boardY, column, backRow, 1));
        }
    }

    public static void resetOrder(){
        order = new String[]{"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};
    }

    public static void shuffleOrder(){
            List<String> list = new ArrayList<>(Arrays.asList(order));
            Collections.shuffle(list);
            order = list.toArray(new String[0]);
    }
}