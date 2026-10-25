package lanse.worldwarchess.shared.core.piece.ai;

import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.MoveContext;
import lanse.worldwarchess.shared.core.piece.move.MoveExecutor;
import lanse.worldwarchess.shared.core.piece.move.SimulatedMoveExecutor;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

/** Written by Lanse
 * <br><br>
 * This was inspired by Sebastian Lague YT. I watched his chess bot videos for ideas with Alpha Beta pruning, Iterative Deepening,
 * and I saw a lot of other ideas but I want this to be my own creation, not a tutorial or something else. **/
public class NPC {

    public static Map<LegalMoveDetector.Coordinate, List<String>> attackedSquares = new HashMap<>();

    public enum SELECTED_AI {
        MINIMAX,
        MEANIMAX,
        MAXIMIN
    }

    public static SELECTED_AI selectedAI = SELECTED_AI.MEANIMAX;

    public record SearchResult(Piece piece, LegalMoveDetector.Coordinate move, double score, int depth) {}
    private record RootMove(Piece livePiece, int pieceIndex, LegalMoveDetector.Coordinate move) {}

    //James likes custom exceptions so I wanted to mess around with one to break out when we hit the bot time limit
    private static final class SearchTimeout extends RuntimeException {
        private SearchTimeout() {
            super(null, null, false, false);
        }
    }

    private static long jobDeadline;
    private static long time;

    public static void doAllNpcLogic(String team, String ignoredTeam, boolean skipTurn) {
        //moved NPC logic on a different thread because it can be laggy
        new Thread(() -> {
            performMove(team);

            if (skipTurn) {
                Timer.currentTime = 0;
            }

        }, "NPCMover").start();
    }

    public static void performMove(String team) {
        SearchResult result = findBestMove(WorldMap.getBoard(), team, selectedAI, Settings.getMaxTimeMillisecondsForBot());

        if (result != null) {
            Piece piece = result.piece();
            LegalMoveDetector.Coordinate move = result.move();

            MoveExecutor.executeMove(piece, piece.getBoard(), move.x(), move.y(), piece.getPosX(),
                    piece.getPosY(), true, "move", false, false);
        }
    }

    public static void recalculateAttackerLocations() {
        attackedSquares.clear();
        attackedSquares = WorldMap.getAllEnemyMoves();
    }

    public static SearchResult findBestMove(Board board, String team, NPC.SELECTED_AI ai, long timeLimitMillis) {
        if (board == null || team == null || ai == null) {
            return null;
        }

        MoveContext liveContext = MoveContext.contextify(board);
        MoveContext root = copyContext(liveContext);

        String opponent = root.pieces().stream().map(Piece::getTeam).filter(t -> !t.equals(team)).findFirst().orElse(null);

        if (opponent == null) {
            return null;
        }

        List<RootMove> rootMoves = new ArrayList<>();

        for (int i = 0; i < root.pieces().size(); i++) {
            Piece piece = root.pieces().get(i);

            if (!piece.getTeam().equals(team)) {
                continue;
            }

            for (LegalMoveDetector.Coordinate move : LegalMoveDetector.getLegalMoves(piece, root)) {
                rootMoves.add(new RootMove(liveContext.pieces().get(i), i, move));
            }
        }

        if (rootMoves.isEmpty()) {
            return null;
        }

        // Try good looking captures first to improve alpha-beta pruning, since alpha beta pruning seems to be fastest when
        // we start with good moves. This also lets iterative deepening work, since each level we find the best of that depth,
        // which speeds up further depths, etc
        rootMoves.sort(Comparator.comparingInt((RootMove rm) -> capturePriority(root, rm)).reversed());

        long limitNanos = Math.max(1L, timeLimitMillis) * 1_000_000L;
        jobDeadline = System.nanoTime() + limitNanos;
        time = 0;

        RootMove fallback = rootMoves.getFirst();
        SearchResult best = new SearchResult(fallback.livePiece(), fallback.move(), evaluate(root, team, ai), 0);

        // Iterative deepening: retain the last fully completed result.
        for (int depth = 1; ; depth++) {
            try {
                checkTime();
                System.out.println(depth + ": " + time);

                SearchResult iterationBest = null;
                double alpha = Double.NEGATIVE_INFINITY;
                double beta = Double.POSITIVE_INFINITY;

                for (RootMove rootMove : rootMoves) {
                    checkTime();

                    MoveContext child = copyContext(root);
                    Piece movingPiece = child.pieces().get(rootMove.pieceIndex());

                    SimulatedMoveExecutor.executeMove(child, movingPiece, rootMove.move().x(), rootMove.move().y());

                    double score = search(child, opponent, team, opponent, depth - 1, alpha, beta, ai);

                    if (iterationBest == null || isBetter(score, iterationBest.score(), ai)) {
                        iterationBest = new SearchResult(rootMove.livePiece(), rootMove.move(), score, depth);
                    }

                    if (ai != NPC.SELECTED_AI.MAXIMIN) {
                        alpha = Math.max(alpha, score);
                    } else {
                        beta = Math.min(beta, score);
                    }
                }

                if (iterationBest != null) {
                    best = iterationBest;
                }

            } catch (SearchTimeout ignored) {
                //tbh it looks kinda nice making custom exceptions that aren't like CONCURRENTMODIFICATIONEXCEPTION E which give you a heart attack, but only you not me, my heart wouldnt be foolish enough to attack me, I am too powerful, and I already won last time it tried doing that when I was born, and although it did have the upper hand for a while to the point I only had a 5% chance of living, I still won, so there is really no hope that it can win vs me. To anyone actually reading this, hello. Idk why am I typing this much for just a simple silly exception.
                break;
            }
        }

        return best;
    }

    private static double search(MoveContext context, String teamToMove, String rootTeam, String opponentTeam, int depth, double alpha, double beta, NPC.SELECTED_AI ai) {
        checkTime();

        if (depth <= 0) {
            return evaluate(context, rootTeam, ai);
        }

        List<Piece> pieces = context.pieces();
        List<RootMove> moves = new ArrayList<>();

        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);

            if (!piece.getTeam().equals(teamToMove)) {
                continue;
            }

            for (LegalMoveDetector.Coordinate move : LegalMoveDetector.getLegalMoves(piece, context)) {
                moves.add(new RootMove(null, i, move));
            }
        }

        //TODO - No moves, until checkmate/stalemate detection exists, evaluate the resulting position rather
        // than assuming a terminal outcome.
        if (moves.isEmpty()) {
            return evaluate(context, rootTeam, ai);
        }

        moves.sort(Comparator.comparingInt((RootMove rm) -> capturePriority(context, rm)).reversed());

        boolean rootTurn = teamToMove.equals(rootTeam);
        boolean maximizing = (ai == NPC.SELECTED_AI.MAXIMIN) != rootTurn;

        double bestScore = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        String nextTeam = teamToMove.equals(rootTeam) ? opponentTeam : rootTeam;

        for (RootMove move : moves) {
            checkTime();

            MoveContext child = copyContext(context);
            Piece movingPiece = child.pieces().get(move.pieceIndex());

            SimulatedMoveExecutor.executeMove(child, movingPiece, move.move().x(), move.move().y());

            double score = search(child, nextTeam, rootTeam, opponentTeam, depth - 1, alpha, beta, ai);

            if (maximizing) {
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, bestScore);
            } else {
                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, bestScore);
            }

            if (beta <= alpha) {
                break;
            }
        }

        return bestScore;
    }

    private static MoveContext copyContext(MoveContext source) {
        List<Piece> copies = new ArrayList<>(source.pieces().size());

        for (Piece piece : source.pieces()) {
            copies.add(piece.clonePiece(source.board()));
        }

        return new MoveContext(source.board(), copies);
    }

    //Good captures are better
    private static int capturePriority(MoveContext context, RootMove move) {
        Piece piece = context.pieces().get(move.pieceIndex());
        Piece target = context.getPieceAt(move.move().x(), move.move().y(), piece);
        return target != null && !target.getTeam().equals(piece.getTeam()) ? target.getValue() * 10 - piece.getValue() : 0;
    }

    //Positive scores favor the root team, negative scores favor its opponent.
    private static double evaluate(MoveContext context, String rootTeam, NPC.SELECTED_AI ai) {
        double material = 0;
        double rootPosition = 0;
        double enemyPosition = 0;

        for (Piece piece : context.pieces()) {
            double value = piece.getValue();

            if (piece.getTeam().equals(rootTeam)) {
                material += value;
            } else {
                material -= value;
            }
        }

        rootPosition = evaluatePosition(context, rootTeam);

        String enemyTeam = context.pieces().stream().map(Piece::getTeam).filter(team -> !team.equals(rootTeam)).findFirst().orElse(null);

        if (enemyTeam != null) {
            enemyPosition = evaluatePosition(context, enemyTeam);
        }

        double score = material + rootPosition - enemyPosition;

        return switch (ai) {
            case MINIMAX, MAXIMIN -> score;

            case MEANIMAX -> {
                double balancedScore = -Math.abs(score);

                //Thou shall not lose, only merely draw.
                if (score < -3.0) {
                    balancedScore -= (-3.0 - score) * 10.0;
                }

                yield balancedScore;
            }
        };
    }

    //TODO - I also want to train an evolutionary neural net on Chess, and when I am done I am going to see if hard coding it
    // or evolutionary learning is better, because my tactics are limited.
    private static double evaluatePosition(MoveContext context, String team) {
        double score = 0;
        int[] pawnFiles = new int[8];

        for (Piece piece : context.pieces()) {
            if (!piece.getTeam().equals(team)) {
                continue;
            }

            if (piece.getType() == PieceType.PAWN) {
                int x = piece.getPosX();
                if (x >= 0 && x < 8) {
                    pawnFiles[x]++;
                }
            }
        }

        for (Piece piece : context.pieces()) {
            if (!piece.getTeam().equals(team)) {
                continue;
            }

            int x = piece.getPosX();
            int y = piece.getPosY();

            if (x < 0 || x > 7 || y < 0 || y > 7) {
                continue;
            }

            double center = centerScore(x, y);

            switch (piece.getType()) {
                //TODO - pawn might make search too laggy
                case PAWN -> {
                    // Pawns that complete their journey to becoming queens are rewarded with nail polish
                    int advancement = team.equalsIgnoreCase("White") ? 6 - y : y - 1;

                    score += Math.max(0, advancement) * 0.08;

                    // Central pawns help control the board
                    if (x == 3 || x == 4) {
                        score += 0.15;
                    }

                    // Doubled pawns are bad
                    if (pawnFiles[x] > 1) {
                        score -= 0.25 * (pawnFiles[x] - 1) / pawnFiles[x];
                    }

                    // Isolated pawns have no neighboring pawn support.
                    boolean hasNeighbor = false;

                    for (Piece other : context.pieces()) {
                        if (other.getType() != PieceType.PAWN || !other.getTeam().equals(team) || other == piece) {
                            continue;
                        }

                        if (Math.abs(other.getPosX() - x) == 1) {
                            hasNeighbor = true;
                            break;
                        }
                    }

                    if (!hasNeighbor) {
                        score -= 0.20;
                    }
                }

                case KNIGHT -> {
                    score += center * 0.22;

                    int homeRank = team.equalsIgnoreCase("White") ? 7 : 0;

                    boolean onStartingSquare = y == homeRank && (x == 1 || x == 6);

                    if (piece.hasMovedEver && !onStartingSquare) {
                        score += 0.45;
                    }
                }

                case BISHOP -> {
                    score += center * 0.12;

                    int homeRank = team.equalsIgnoreCase("White") ? 7 : 0;

                    boolean onStartingSquare = y == homeRank && (x == 2 || x == 5);

                    if (piece.hasMovedEver && !onStartingSquare) {
                        score += 0.45;
                    }
                }

                case ROOK -> {
                    // The rook on the 7th rank is good according to GothamchessYT
                    int enemyBackRank = team.equalsIgnoreCase("White") ? 0 : 7;

                    if (y == enemyBackRank) {
                        score += 0.25;
                    }
                }

                case QUEEN -> score += center * 0.06;

                case KING -> {
                    int homeRank = team.equalsIgnoreCase("White") ? 7 : 0;

                    // Reward the king's usual castled squares.
                    if (y == homeRank && (x == 2 || x == 6) && piece.hasMovedEver) {
                        score += 0.60;
                    }
                }
            }
        }

        return score;
    }

    private static double centerScore(int x, int y) {
        return (3.5 - Math.abs(x - 3.5)) + (3.5 - Math.abs(y - 3.5));
    }

    private static boolean isBetter(double candidate, double current, NPC.SELECTED_AI ai) {
        return ai == NPC.SELECTED_AI.MAXIMIN ? candidate < current : candidate > current;
    }

    private static void checkTime() {
        time++;

        //You know, recently I have liked super overcomplicated bit operations because it makes me feel like a hackerman,
        //but when I look back at it a few minutes later, I feel stupid. What am I even doing????
        //Also it throws an exception to escape wherever it is in the code instantly. Its weird but it works, and although
        //Using exceptions in code is inefficient, its only for after the time limit is reached so it doesnt matter
        if ((time & 255) == 0 && System.nanoTime() >= jobDeadline) {
            throw new SearchTimeout();
        }
    }
}