package lanse.worldwarchess.shared.core.piece.ai;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.MoveExecutor;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

public class NPC {
    /** remember when adding new pieces to add it to the texture key in the TextureKey class! **/

    //This is the central class that orders all NPC pieces to move when needed. It also stores the core logic for
    // some of the ai types like zombies, vampires, and etc.

    public static volatile boolean areNpcsMoving = false;
    public static Map<LegalMoveDetector.Coordinate, List<String>> attackedSquares = new HashMap<>();

    public enum SELECTED_AI {
        MINIMAX,
        MEANIMAX,
        MAXIMIN
    }

    public static SELECTED_AI selectedAI = SELECTED_AI.MEANIMAX;

    public static void doAllNpcLogic(String team, String ignoredTeam, boolean doOtherTeams, boolean skipTurn) {
        //moved NPC logic on a different thread because it can be laggy
        new Thread(() -> {
            performMove();

            if (skipTurn) {
                Timer.currentTime = 0;
            }
            areNpcsMoving = false;

        }, "NPCMover").start();
    }

    public static void performMove(){
        switch (selectedAI){
            case MINIMAX -> {
                //TODO - minimize the enemy score, maximize your score
            }
            case MEANIMAX -> {
                //TODO - find path to the most average position possible
            }
            case MAXIMIN -> {
                //TODO - maximize the enemy score, minimize your own score
            }
        }

        Board mainBoard = WorldMap.getBoardByID(0);
    }

    private static void performMove(Piece piece, List<LegalMoveDetector.Coordinate> candidateMoves) {
        if (candidateMoves.isEmpty()) return;

        LegalMoveDetector.Coordinate chosenMove = candidateMoves.get(new Random().nextInt(candidateMoves.size()));
        int moveX = chosenMove.x();
        int moveY = chosenMove.y();
        int moveBoardID = chosenMove.boardID();

        Board moveBoard = WorldMap.getBoardByID(moveBoardID);
        if (moveBoard == null) return;

        MoveExecutor.executeMove(piece, moveBoard, moveX, moveY, piece.getPosX(), piece.getPosY(), true,
                "move", false, false);
    }

    public static List<LegalMoveDetector.Coordinate> filterCaptureMoves(Piece piece, List<LegalMoveDetector.Coordinate> moves) {
        List<LegalMoveDetector.Coordinate> captureMoves = new ArrayList<>();
        for (LegalMoveDetector.Coordinate move : moves) {
            int targetX = move.x();
            int targetY = move.y();
            int targetBoardID = move.boardID();
            Board targetBoard = WorldMap.getBoardByID(targetBoardID);

            if (targetBoard != null) {
                Piece targetPiece = targetBoard.getPieceAt(targetX, targetY, Optional.of(piece), WorldMap.getAllPiecesOfBoard(targetBoard));
                if (targetPiece != null && !targetPiece.getTeam().equals(piece.getTeam())) {
                    captureMoves.add(move);
                }
            }
        }
        return captureMoves;
    }

    public static void recalculateAttackerLocations() {
        attackedSquares.clear();

        attackedSquares = WorldMap.getAllEnemyMoves();
    }

    private static boolean isSquareEnemyAttacked(Piece piece, LegalMoveDetector.Coordinate move) {
        LegalMoveDetector.Coordinate key = new LegalMoveDetector.Coordinate(move.x(), move.y(), move.boardID());

        List<String> attackers = NPC.attackedSquares.get(key);
        if (attackers == null) return false;

        return attackers.stream().anyMatch(team -> !team.equals(piece.getTeam()));
    }

    private static Piece getPieceAt(LegalMoveDetector.Coordinate move, Piece mover) {
        Board b = WorldMap.getBoardByID(move.boardID());
        if (b == null) return null;
        return b.getPieceAt(move.x(), move.y(), Optional.of(mover), WorldMap.getAllPiecesOfBoard(b));
    }
}