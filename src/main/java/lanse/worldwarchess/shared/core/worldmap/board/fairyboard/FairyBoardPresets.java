package lanse.worldwarchess.shared.core.worldmap.board.fairyboard;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.fairies.FairyCommanderPiece;
import lanse.worldwarchess.shared.core.piece.fairies.FairyPiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.List;

public class FairyBoardPresets {

    //TODO - this class can probably extend boardPresets

    private static final String[] order = {"rook", "flamingo", "knight", "camel", "zebra", "bishop", "queen", "king", "amazon", "bishop", "zebra", "camel", "knight", "flamingo", "rook"};

    public static void setPreset(String preset, int boardX, int boardY, int playerID1, int playerID2, Board board, boolean isCommander) {
        switch (preset.toLowerCase()) {
            case "border" -> createBorderPreset(boardX, boardY, playerID1, playerID2, board, isCommander);
            case "whiteterritory" -> createWhiteTerritoryPreset(boardX, boardY, playerID1, board, isCommander);
            case "blackterritory" -> createBlackTerritoryPreset(boardX, boardY, playerID1, board, isCommander);
            case "empty", "siege" -> {
                return;
            }
            default -> throw new IllegalArgumentException("Unknown preset: " + preset);
        };
    }

    private static void createBorderPreset(int boardX, int boardY, int playerID, int playerID2, Board board, boolean isCommander) {
        List<Piece> pieces = new ArrayList<>();
        List<FairyCommanderPiece> commanderPieces = new ArrayList<>();

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
        List<FairyCommanderPiece> commanderPieces = new ArrayList<>();
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
        List<FairyCommanderPiece> commanderPieces = new ArrayList<>();
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
        String texture;
        int pawnRow;

        if (team.equalsIgnoreCase("White")){
            pawnRow = board.getBoardSize() - 2;
            texture = "wp";
        } else {
            pawnRow = 1;
            texture = "bp";
        }

        for (int i = 0; i < board.getBoardSize(); i++) {
            pieces.add(new FairyPiece(playerID, board, "Pawn", team, texture, boardX, boardY, i, pawnRow, 1));
        }
    }

    private static void addBackRow(List<Piece> pieces, int boardX, int boardY, String team, int playerID, Board board) {
        int backRow = team.equals("White") ? 14 : 0;
        String prefix = team.equals("White") ? "w" : "b";

        for (int i = 0; i < board.getBoardSize(); i++) {
            String texture = prefix + shortenTextureNameIfNeeded(order[i]);
            pieces.add(new FairyPiece(playerID, board, order[i], team, texture, boardX, boardY, i, backRow, 1));
        }
    }

    private static void addCommanderPawns(List<FairyCommanderPiece> pieces, int boardX, int boardY, String team, int playerID, Board board) {
        int pawnRow = team.equals("White") ? 12 : 2;
        for (int i = 0; i < board.getBoardSize(); i++) {
            int column = 2 * i;
            pieces.add(new FairyCommanderPiece(playerID, board, "Pawn", team, boardX, boardY, column, pawnRow));
        }
    }

    private static void addCommanderBackRow(List<FairyCommanderPiece> pieces, int boardX, int boardY, String color, int playerID, Board board) {
        int backRow = color.equals("White") ? 14 : 0;
        for (int i = 0; i < board.getBoardSize(); i++) {
            int column = 2 * i;  // Place back row pieces at 1, 3, 5, ..., 15.
            pieces.add(new FairyCommanderPiece(playerID, board, order[i], color, boardX, boardY, column, backRow));
        }
    }

    private static String shortenTextureNameIfNeeded(String type){
        return switch (type){
            case "pawn" -> "p";
            case "rook" -> "r";
            case "knight" -> "n";
            case "bishop" -> "b";
            case "queen" -> "q";
            case "king" -> "k";
            default -> type;
        };
    }
}
