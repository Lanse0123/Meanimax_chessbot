package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoardPresets {

    private static String[] order = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};

    //This is responsible for creating the pieces of a board depending on what preset the board has
    public static void setPreset(String preset, int boardX, int boardY, int playerID1, int playerID2, Board board, boolean isCommander) {
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

        addPawns(pieces, boardX, boardY, true, playerID, board);
        addPawns(pieces, boardX, boardY, false, playerID2, board);
        addBackRow(pieces, boardX, boardY, true, playerID, board);
        addBackRow(pieces, boardX, boardY, false, playerID2, board);

        // Add pieces to WorldMap
        WorldMap.pieces.addAll(pieces);
    }

    private static void createWhiteTerritoryPreset(int boardX, int boardY, int playerID, Board board, boolean isCommander) {
        List<Piece> pieces = new ArrayList<>();
        addPawns(pieces, boardX, boardY, true, playerID, board);
        addBackRow(pieces, boardX, boardY, true, playerID, board);
        WorldMap.pieces.addAll(pieces);
    }

    private static void createBlackTerritoryPreset(int boardX, int boardY, int playerID, Board board, boolean isCommander) {
        List<Piece> pieces = new ArrayList<>();
        addPawns(pieces, boardX, boardY, false, playerID, board);
        addBackRow(pieces, boardX, boardY, false, playerID, board);
        WorldMap.pieces.addAll(pieces);
    }

    private static void addPawns(List<Piece> pieces, int boardX, int boardY, boolean team, int playerID, Board board) {
        int pawnRow = team ? 6 : 1;
        for (int i = 0; i < 8; i++) {
            pieces.add(new Piece(playerID, board, PieceType.fromName("Pawn"), team, boardX, boardY, i, pawnRow));
        }
    }

    private static void addBackRow(List<Piece> pieces, int boardX, int boardY, boolean team, int playerID, Board board) {
        int backRow = team ? 7 : 0;

        for (int i = 0; i < 8; i++) {
            pieces.add(new Piece(playerID, board, PieceType.fromName(order[i]), team, boardX, boardY, i, backRow));
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