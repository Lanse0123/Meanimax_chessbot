package lanse.worldwarchess.shared.core.piece.ai;

import lanse.worldwarchess.shared.client.storage.Settings;
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

    private record MoveCandidate(Piece piece, LegalMoveDetector.Coordinate move, double score) {}

    public static void doAllNpcLogic(String team, String ignoredTeam, boolean doOtherTeams, boolean skipTurn) {
        //moved NPC logic on a different thread because it can be laggy
        new Thread(() -> {
            performMove(team);

            if (skipTurn) {
                Timer.currentTime = 0;
            }
            areNpcsMoving = false;

        }, "NPCMover").start();
    }

    public static void performMove(String team) {
        List<MoveCandidate> candidates = new ArrayList<>();
        long maxTimeMillisecondsForBotToMove = Settings.getMaxTimeMillisecondsForBot();

        for (Piece piece : WorldMap.pieces) {
            if (!piece.getTeam().equals(team)) continue;

            List<LegalMoveDetector.Coordinate> moves = LegalMoveDetector.getLegalMoves(piece);

            for (LegalMoveDetector.Coordinate move : moves) {
                double score = evaluateMove(piece, move, team);
                candidates.add(new MoveCandidate(piece, move, score));
            }
        }

        if (candidates.isEmpty()) return;

        MoveCandidate chosen = switch (selectedAI) {
            case MINIMAX -> candidates.stream().max(Comparator.comparingDouble(MoveCandidate::score)).orElseThrow();

            //this code is a bit extra confusing because it wants to get as close to 0 as possible, but will not allow
            // itself to get into a losing position to achieve that.
            case MEANIMAX -> candidates.stream().min(Comparator.comparingDouble(candidate -> {
                double score = candidate.score();
                double minimumScore = -3.0;

                if (score < minimumScore) {
                    return Math.abs(score) + (minimumScore - score) * 10;
                }

                return Math.abs(score);
            })).orElseThrow();

            case MAXIMIN -> candidates.stream().min(Comparator.comparingDouble(MoveCandidate::score)).orElseThrow();
        };

        performMove(chosen.piece(), List.of(chosen.move()));
    }

    //TODO - crappy implementation, just so it can atleast move. Prepare for searching the FUTURE!!! Currently, it is
    // "ME SEE ROOK, ME ATTACK ROOK" -then it gets checkmated instantly because it is stupid
    private static double evaluateMove(Piece piece, LegalMoveDetector.Coordinate move, String team) {
        double score = 0;

        Piece target = WorldMap.board.getPieceAt(move.x(), move.y(), Optional.of(piece), WorldMap.getAllPiecesOfBoard(WorldMap.board));

        if (target != null && !target.getTeam().equals(team)) {
            score += target.getValue() * 10;
        }

        if (isSquareEnemyAttacked(piece, move)) {
            score -= piece.getValue() * 5;
        }

        return score;
    }

    private static void performMove(Piece piece, List<LegalMoveDetector.Coordinate> candidateMoves) {
        if (candidateMoves.isEmpty()) return;

        LegalMoveDetector.Coordinate chosenMove = candidateMoves.get(new Random().nextInt(candidateMoves.size()));
        int moveX = chosenMove.x();
        int moveY = chosenMove.y();

        MoveExecutor.executeMove(piece, WorldMap.getBoard(), moveX, moveY, piece.getPosX(), piece.getPosY(), true,
                "move", false, false);
    }

    public static List<LegalMoveDetector.Coordinate> filterCaptureMoves(Piece piece, List<LegalMoveDetector.Coordinate> moves) {
        List<LegalMoveDetector.Coordinate> captureMoves = new ArrayList<>();
        for (LegalMoveDetector.Coordinate move : moves) {
            int targetX = move.x();
            int targetY = move.y();
            Board targetBoard = WorldMap.getBoard();

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
        LegalMoveDetector.Coordinate key = new LegalMoveDetector.Coordinate(move.x(), move.y());

        List<String> attackers = NPC.attackedSquares.get(key);
        if (attackers == null) return false;

        return attackers.stream().anyMatch(team -> !team.equals(piece.getTeam()));
    }

    private static Piece getPieceAt(LegalMoveDetector.Coordinate move, Piece mover) {
        return WorldMap.board.getPieceAt(move.x(), move.y(), Optional.of(mover), WorldMap.getAllPiecesOfBoard(WorldMap.getBoard()));
    }
}