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
import lanse.worldwarchess.shared.utils.LagTester;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldMap {

    /** This class stores all the pieces and boards in the world, and has a ton of functions
     * for calling them and making use of them. It is also responsible for setting up the map.
     */

    public static Board board;
    public static List<Piece> pieces = new CopyOnWriteArrayList<>();

    public static void initializeBoards() {
        System.out.println("Adding board...");
        MapShapePresets.setBoard(0, 0);
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

    public static Map<LegalMoveDetector.Coordinate, List<String>> getAllEnemyMoves() {
        LagTester lagTester = new LagTester();
        lagTester.start("Get ALL Enemy Moves");

        Map<LegalMoveDetector.Coordinate, List<String>> attackedSquares = new HashMap<>();

        for (Piece piece : pieces) {
            List<LegalMoveDetector.Coordinate> legalMoves = VisibleMoveDetector.getVisibleMoves(piece);
            for (LegalMoveDetector.Coordinate coord : legalMoves) {
                attackedSquares.computeIfAbsent(coord, k -> new ArrayList<>()).add(piece.getTeam());
            }
        }

        lagTester.stop();
        return attackedSquares;
    }

    public static List<Piece> getAllPiecesOfPlayerID(int playerID) {
        return pieces.stream().filter(p -> p.getPlayerID() == playerID).toList();
    }

    public static List<Piece> getAllKings(String team, boolean findNearestEnemy, boolean findNearestTeamate, List<Piece> pieceList) {
        return pieceList.stream().filter(p -> p.getType() == PieceType.KING)
                .filter(p -> (findNearestEnemy && !Objects.equals(team, p.getTeam())) || (findNearestTeamate &&
                        Objects.equals(team, p.getTeam()))).toList();
    }

    public static List<Piece> getAllPiecesOfTeam(String team) {
        return pieces.stream().filter(p -> Objects.equals(p.getTeam(), team)).toList();
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

            if (Objects.equals(piece.getTeam(), "white")){
                winningTeam = "BLACK";
            }

            GamePanel.winningTeam = winningTeam;
            CentralTick.gameOver = true;
        }
    }

    public static void resetPieces() {
        pieces.forEach(p -> p.hasMoved = false);
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