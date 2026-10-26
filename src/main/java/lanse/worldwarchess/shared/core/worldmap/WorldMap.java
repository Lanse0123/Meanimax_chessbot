package lanse.worldwarchess.shared.core.worldmap;

import lanse.worldwarchess.shared.central.CentralTick;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.piece.move.VisibleMoveDetector;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMap {

    /** This class stores all the pieces and boards in the world, and has a ton of functions
     * for calling them and making use of them. It is also responsible for setting up the map.
     */

    public static Board board;
    public static List<Piece> pieces = new ArrayList<>();

    public static void initializeBoards() {
        System.out.println("Adding board...");
        WorldMap.board = new Board(0, 0, "Border", 0, 1, false, true);
    }

    public static List<Piece> getAllPiecesOfBoard(Board board) {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p != null && p.getBoard() == board) {
                result.add(p);
            }
        }
        return result;
    }

    public static Map<LegalMoveDetector.Coordinate, List<Boolean>> getAllEnemyMoves() {
        Map<LegalMoveDetector.Coordinate, List<Boolean>> attackedSquares = new HashMap<>();

        for (Piece piece : pieces) {
            List<LegalMoveDetector.Coordinate> legalMoves = VisibleMoveDetector.getVisibleMoves(piece);
            for (LegalMoveDetector.Coordinate coord : legalMoves) {
                attackedSquares.computeIfAbsent(coord, k -> new ArrayList<>()).add(piece.getTeam());
            }
        }

        return attackedSquares;
    }

    public static List<Piece> getAllPiecesOfPlayerID(int playerID) {
        return pieces.stream().filter(p -> p.getPlayerID() == playerID).toList();
    }

    public static List<Piece> getAllPiecesOfTeam(boolean team) {
        return pieces.stream().filter(p -> p.getTeam() == team).toList();
    }

    public static Board getBoard() {
        return board;
    }

    public static void killPiece(Piece piece) {
        new DeathMarker(piece.getBoard(), piece.getPosX(), piece.getPosY(), 1);
        pieces.remove(piece);

        if (piece.getType() == PieceType.KING) {
            DeathMarker.deathRow.addAll(WorldMap.getAllPiecesOfPlayerID(piece.getPlayerID()));
            String winningTeam = "WHITE";

            if (piece.getTeam()){
                winningTeam = "BLACK";
            }

            GamePanel.winningTeam = winningTeam;
            CentralTick.gameOver = true;
        }
    }

    public static void spawnPiece(Piece piece, boolean shouldSend){
        WorldMap.pieces.add(piece);
        MoveHistory.spawnPieceForRecord(piece, shouldSend);
    }

    public static void erasePiece(Piece piece, boolean shouldSend){
        pieces.remove(piece);
        MoveHistory.erasePieceForRecord(piece);
    }
}