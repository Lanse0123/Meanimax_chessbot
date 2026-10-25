package lanse.worldwarchess.shared.core.piece.ai;

import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.MoveContext;
import lanse.worldwarchess.shared.core.piece.move.MoveExecutor;
import lanse.worldwarchess.shared.core.piece.move.TranspositionTable;
import lanse.worldwarchess.shared.core.thread.ThreadNames;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Written by Lanse
 * <br><br>
 * This was inspired by Sebastian Lague YT. I watched his chess bot videos for ideas with Alpha Beta pruning, Iterative Deepening,
 * and I saw a lot of other ideas but I want this to be my own creation, not a tutorial or something else. **/
public class NPC {

    public static Map<LegalMoveDetector.Coordinate, List<Boolean>> attackedSquares = new HashMap<>();

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

    public static void doAllNpcLogic(boolean team, boolean ignoredTeam, boolean skipTurn) {
        //moved NPC logic on a different thread because it can be laggy
        new Thread(() -> {
            performMove(team);

            if (skipTurn) {
                Timer.currentTime = 0;
            }

        }, ThreadNames.NPC_MOVER).start();
    }

    public static void performMove(boolean team) {
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

    public static SearchResult findBestMove(Board board, boolean team, NPC.SELECTED_AI ai, long timeLimitMillis) {
        if (board == null || ai == null) {
            return null;
        }

        MoveContext liveContext = MoveContext.contextify(board);
        MoveContext root = copyContext(liveContext);

        List<RootMove> rootMoves = new ArrayList<>();

        for (int i = 0; i < root.pieces().size(); i++) {
            Piece piece = root.pieces().get(i);

            if (piece.getTeam() != team) {
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
        orderCapturesFirst(root, rootMoves);

        long limitNanos = Math.max(1L, timeLimitMillis) * 1_000_000L;
        jobDeadline = System.nanoTime() + limitNanos;
        time = 0;

        RootMove fallback = rootMoves.getFirst();
        SearchResult best = new SearchResult(fallback.livePiece(), fallback.move(), evaluate(root, team, !team, ai), 0);

        //For not evaluating duplicate positions
        Map<Long, TranspositionTable.TranspositionTableEntry> transpositionTable = new HashMap<>(1 << 16);

        // Iterative deepening. Start at depth 1, find the best, then search depth 2. Since it keeps in mind the best
        // Of each depth, it can guess the alpha beta pruning quickly, making it more efficient
        for (int depth = 1; ; depth++) {
            try {
                checkTime();
                System.out.println(depth + ": " + time);

                SearchResult iterationBest = null;
                double alpha = Double.NEGATIVE_INFINITY;
                double beta = Double.POSITIVE_INFINITY;

                for (RootMove rootMove : rootMoves) {
                    checkTime();

                    Piece movingPiece = root.pieces().get(rootMove.pieceIndex());

                    MoveContext.TemporaryMove undo = root.makeMove(movingPiece, rootMove.move().x(), rootMove.move().y());

                    //For testing Zobrist hash mismatches:
                    ////////////////////////////////////////////////////////////////////////////////////////////
//                    long before = root.positionHash();
//
//                    MoveContext.TemporaryMove undo = root.makeMove(movingPiece, rootMove.move.x(), rootMove.move.y());
//
//                    long incremental = root.positionHash();
//                    long rebuilt = new MoveContext(root.board(), root.pieces()).positionHash();
//
//                    if (incremental != rebuilt) {
//                        throw new IllegalStateException("Hash mismatch after makeMove()");
//                    }
//
//                    root.undoMove(undo);
//
//                    if (root.positionHash() != before) {
//                        throw new IllegalStateException("Hash mismatch after undoMove()");
//                    }
                    ////////////////////////////////////////////////////////////////////////////////////////////


                    double score;
                    try {
                        score = search(root, !team, team, !team, depth - 1, alpha, beta, ai, transpositionTable);
                    } finally {
                        root.undoMove(undo);
                    }

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

    private static double search(MoveContext context, boolean teamToMove, boolean rootTeam, boolean opponentTeam, int depth,
            double alpha, double beta, SELECTED_AI ai, Map<Long, TranspositionTable.TranspositionTableEntry> transpositionTable) {

        checkTime();

        long hash = TranspositionTable.hashPosition(context, teamToMove, rootTeam, ai);
        double originalAlpha = alpha;
        double originalBeta = beta;

        TranspositionTable.TranspositionTableEntry cached = transpositionTable.get(hash);

        if (cached != null && cached.depth() >= depth) {
            switch (cached.bound()) {
                case EXACT -> {
                    return cached.score();
                }
                case LOWER -> alpha = Math.max(alpha, cached.score());
                case UPPER -> beta = Math.min(beta, cached.score());
            }

            if (alpha >= beta) {
                return cached.score();
            }
        }

        if (depth <= 0) {
            double score = evaluate(context, rootTeam, opponentTeam, ai);
            storePosition(transpositionTable, hash, depth, score, TranspositionTable.Bound.EXACT);
            return score;
        }

        List<Piece> pieces = context.pieces();
        List<RootMove> moves = new ArrayList<>();

        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);

            if (piece.getTeam() != teamToMove) {
                continue;
            }

            for (LegalMoveDetector.Coordinate move : LegalMoveDetector.getLegalMoves(piece, context)) {
                moves.add(new RootMove(null, i, move));
            }
        }

        if (moves.isEmpty()) {
            double score = evaluate(context, rootTeam, opponentTeam, ai);
            storePosition(transpositionTable, hash, depth, score, TranspositionTable.Bound.EXACT);
            return score;
        }

        orderCapturesFirst(context, moves);

        boolean rootTurn = teamToMove == rootTeam;
        boolean maximizing = (ai == SELECTED_AI.MAXIMIN) != rootTurn;
        boolean nextTeam = teamToMove == rootTeam ? opponentTeam : rootTeam;

        double bestScore = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (RootMove move : moves) {
            checkTime();

            Piece movingPiece = pieces.get(move.pieceIndex());

            MoveContext.TemporaryMove undo = context.makeMove(movingPiece, move.move().x(), move.move().y());

            double score;
            try {
                score = search(context, nextTeam, rootTeam, opponentTeam, depth - 1, alpha, beta, ai, transpositionTable);
            } finally {
                context.undoMove(undo);
            }

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

        TranspositionTable.Bound bound;
        if (bestScore <= originalAlpha) {
            bound = TranspositionTable.Bound.UPPER;
        } else if (bestScore >= originalBeta) {
            bound = TranspositionTable.Bound.LOWER;
        } else {
            bound = TranspositionTable.Bound.EXACT;
        }

        storePosition(transpositionTable, hash, depth, bestScore, bound);
        return bestScore;
    }

    private static void storePosition(Map<Long, TranspositionTable.TranspositionTableEntry> table,
            long hash, int depth, double score, TranspositionTable.Bound bound) {

        TranspositionTable.TranspositionTableEntry existing = table.get(hash);

        // Prefer deeper results because THE FUTUREEE
        if (existing == null || depth > existing.depth() || (depth == existing.depth() && bound == TranspositionTable.Bound.EXACT && existing.bound() != TranspositionTable.Bound.EXACT)) {
            table.put(hash, new TranspositionTable.TranspositionTableEntry(depth, score, bound));
        }

        // If we are searching forever, don't let the Transposition table grow to infinity
        if (table.size() > 500_000) {
            table.clear();
        }
    }

    private static MoveContext copyContext(MoveContext source) {
        List<Piece> copies = new ArrayList<>(source.pieces().size());

        for (Piece piece : source.pieces()) {
            copies.add(piece.clonePiece(source.board()));
        }

        return new MoveContext(source.board(), copies);
    }

    private static void orderCapturesFirst(MoveContext context, List<RootMove> moves) {
        List<RootMove> captures = new ArrayList<>(moves.size());
        List<RootMove> quietMoves = new ArrayList<>(moves.size());

        for (RootMove move : moves) {
            Piece piece = context.pieces().get(move.pieceIndex());
            Piece target = context.getPieceAt(move.move().x(), move.move().y(), piece);

            if (target != null && !target.getTeam() == piece.getTeam()) {
                captures.add(move);
            } else {
                quietMoves.add(move);
            }
        }

        moves.clear();
        moves.addAll(captures);
        moves.addAll(quietMoves);
    }

    //Positive scores favor the root team, negative scores favor its opponent.
    private static double evaluate(MoveContext context, boolean rootTeam, boolean opponentTeam, NPC.SELECTED_AI ai) {
        double material = 0;

        for (Piece piece : context.pieces()) {
            double value = piece.getValue();

            if (piece.getTeam() == rootTeam) {
                material += value;
            } else {
                material -= value;
            }
        }

        double rootPosition = evaluatePosition(context, rootTeam);
        double enemyPosition = evaluatePosition(context, opponentTeam);

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
    private static double evaluatePosition(MoveContext context, boolean team) {
        List<Piece> pieces = context.pieces();
        int[] pawnFiles = new int[8];
        int homeRank = team ? 7 : 0;
        int enemyBackRank = team ? 0 : 7;

        // Count pawns before the switch so its not O(n^2)
        for (Piece piece : pieces) {
            if (piece.getTeam() == team && piece.getType() == PieceType.PAWN) {
                int x = piece.getPosX();
                if (x >= 0 && x < 8) {
                    pawnFiles[x]++;
                }
            }
        }

        double score = 0;
        for (Piece piece : pieces) {
            if (!piece.getTeam() == team) {
                continue;
            }

            int x = piece.getPosX();
            int y = piece.getPosY();

            if ((x | y) < 0 || x >= 8 || y >= 8) {
                continue;
            }

            int center = centerScore(x, y);

            switch (piece.getType()) {
                case PAWN -> {
                    int advancement = team ? 6 - y : y - 1;
                    if (advancement > 0) {
                        score += advancement * 0.08;
                    }

                    // Central pawns help control the board
                    if (x == 3 || x == 4) {
                        score += 0.15;
                    }

                    // Doubled pawns are bad
                    int filePawns = pawnFiles[x];
                    if (filePawns > 1) {
                        score -= 0.25 * (filePawns - 1) / filePawns;
                    }

                    if ((x == 0 || pawnFiles[x - 1] == 0) && (x == 7 || pawnFiles[x + 1] == 0)) {
                        score -= 0.20;
                    }
                }

                case KNIGHT -> {
                    score += center * 0.22;
                    if (piece.hasMovedEver && !(y == homeRank && (x == 1 || x == 6))) {
                        score += 0.45;
                    }
                }

                case BISHOP -> {
                    score += center * 0.12;
                    if (piece.hasMovedEver && !(y == homeRank && (x == 2 || x == 5))) {
                        score += 0.45;
                    }
                }

                case ROOK -> {
                    if (y == enemyBackRank) {
                        score += 0.25;
                    }
                }

                case QUEEN -> score += center * 0.06;

                case KING -> {
                    // Reward the king's usual castled squares.
                    if (y == homeRank && (x == 2 || x == 6) && piece.hasMovedEver) {
                        score += 0.60;
                    }
                }
            }
        }

        return score;
    }

    private static int centerScore(int x, int y) {
        return Math.min(x, 7 - x) + Math.min(y, 7 - y);
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