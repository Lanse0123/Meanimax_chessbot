package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.util.ArrayList;
import java.util.List;

public class BoardPresets {

    private static final String[] order = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};

    //This is responsible for creating the pieces of a board depending on what preset the board has
    public static void setPreset(String preset, int playerID1, int playerID2) {
        switch (preset.toLowerCase()) {
            case "border" -> createBorderPreset(playerID1, playerID2);
            case "whiteterritory" -> createWhiteTerritoryPreset(playerID1);
            case "blackterritory" -> createBlackTerritoryPreset(playerID1);
            case "empty", "siege" -> { return; }
            default -> throw new IllegalArgumentException("Unknown preset: " + preset);
        };
    }

    private static void createBorderPreset(int playerID, int playerID2) {
        List<Piece> pieces = new ArrayList<>();

        addPawns(pieces, true, playerID);
        addPawns(pieces, false, playerID2);
        addBackRow(pieces, true, playerID);
        addBackRow(pieces, false, playerID2);
        WorldMap.pieces.addAll(pieces);
    }

    private static void createWhiteTerritoryPreset(int playerID) {
        List<Piece> pieces = new ArrayList<>();
        addPawns(pieces, true, playerID);
        addBackRow(pieces, true, playerID);
        WorldMap.pieces.addAll(pieces);
    }

    private static void createBlackTerritoryPreset(int playerID) {
        List<Piece> pieces = new ArrayList<>();
        addPawns(pieces, false, playerID);
        addBackRow(pieces, false, playerID);
        WorldMap.pieces.addAll(pieces);
    }

    private static void addPawns(List<Piece> pieces, boolean team, int playerID) {
        int pawnRow = team ? 6 : 1;
        for (int i = 0; i < 8; i++) {
            pieces.add(new Piece(playerID, PieceType.fromName("Pawn"), team, i, pawnRow));
        }
    }

    private static void addBackRow(List<Piece> pieces, boolean team, int playerID) {
        int backRow = team ? 7 : 0;

        for (int i = 0; i < 8; i++) {
            pieces.add(new Piece(playerID, PieceType.fromName(order[i]), team, i, backRow));
        }
    }
}