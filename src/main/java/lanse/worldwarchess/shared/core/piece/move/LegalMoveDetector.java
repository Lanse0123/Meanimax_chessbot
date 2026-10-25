
package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.List;

public class LegalMoveDetector {

    // This class is responsible for calculating all the base piece movements in chess. Like rooks move horrizontally
    // and vertically, while bishops move diagonally, knights move in an L, and queens are like a rook + bishop.
    // Extra work was needed to make it use a context window board instead of the global board so AI algorithms
    // can search extra far with it.

    public record Coordinate(int x, int y) {}
    public static int OFFSET_CORRECTION_VALUE = 25;
    public static int[] bishopDirections = {1, 1, 1, -1, -1, 1, -1, -1};
    public static int[] rookDirections = {1, 0, -1, 0, 0, 1, 0, -1};
    public static int[] queenDirections = {1, 1, 1, -1, -1, 1, -1, -1, 1, 0, -1, 0, 0, 1, 0, -1};

    public static List<Coordinate> getLegalMoves(Piece piece) {
        if (piece == null || piece.getBoard() == null) return new ArrayList<>();
        return getLegalMoves(piece, MoveContext.contextify(piece.getBoard()));
    }

    public static List<Coordinate> getLegalMoves(Piece piece, MoveContext context) {
        if (piece == null || context == null || context.board() == null || piece.getBoard() != context.board() || !context.pieces().contains(piece)) {
            return new ArrayList<>();
        }

        return switch (piece.getType()) {
            case PAWN -> getPawnMoves(piece, context);
            case KNIGHT -> getKnightMoves(piece, context);
            case BISHOP -> getSlidingMoves(piece, context, bishopDirections);
            case ROOK -> getSlidingMoves(piece, context, rookDirections);
            case QUEEN -> getSlidingMoves(piece, context, queenDirections);
            case KING -> getKingMoves(piece, context);
        };
    }

    protected static void addMoveForOffset(Piece piece, int offsetX, int offsetY, MoveContext context, List<Coordinate> moves) {
        Board board = context.board();
        int localX = piece.getPosX() + offsetX;
        int localY = piece.getPosY() + offsetY;
        int boardSize = board.getBoardSize();

        if (localX >= 0 && localX < boardSize && localY >= 0 && localY < boardSize && isValidMove(localX, localY, context, piece)) {
            moves.add(new Coordinate(localX, localY));
        }
    }

    private static void addCaptureMoveForOffset(Piece piece, int offsetX, int offsetY, MoveContext context, List<Coordinate> moves) {
        Board board = context.board();
        int localX = piece.getPosX() + offsetX;
        int localY = piece.getPosY() + offsetY;
        int boardSize = board.getBoardSize();

        if (localX >= 0 && localX < boardSize && localY >= 0 && localY < boardSize && isValidCapture(localX, localY, context, piece)) {
            moves.add(new Coordinate(localX, localY));
        }
    }

    public static List<Coordinate> getKingMoves(Piece piece, MoveContext context) {
        List<Coordinate> moves = new ArrayList<>(12);
        int stepSize = piece.getPieceSize();

        int[][] offsets = {
                {-stepSize, -stepSize}, {-stepSize, 0}, {-stepSize, stepSize},
                {0, -stepSize}, {0, stepSize},
                {stepSize, -stepSize}, {stepSize, 0}, {stepSize, stepSize}
        };

        for (int[] offset : offsets) {
            addMoveForOffset(piece, offset[0], offset[1], context, moves);
        }

        // Castling
        if (!piece.hasMovedEver) {
            int y = piece.getPosY();

            Piece leftRook = context.getPieceAt(0, y, piece);
            if (isARookThatCanCastleAtThisExactMomentInTime(leftRook, piece) && isPathClearForCastling(context, piece.getPosX(), y, -1)) {
                int newX = piece.getPosX() - 2;
                if (newX >= 0 && newX < context.board().getBoardSize()) {
                    moves.add(new Coordinate(newX, y));
                }
            }

            Piece rightRook = context.getPieceAt(7, y, piece);
            if (isARookThatCanCastleAtThisExactMomentInTime(rightRook, piece)
                    && isPathClearForCastling(context, piece.getPosX(), y, 1)) {
                int newX = piece.getPosX() + 2;
                if (newX >= 0 && newX < context.board().getBoardSize()) {
                    moves.add(new Coordinate(newX, y));
                }
            }
        }

        return moves;
    }

    private static boolean isARookThatCanCastleAtThisExactMomentInTime(Piece rook, Piece king) {
        return rook != null && rook.getType() == PieceType.ROOK && rook.getTeam().equals(king.getTeam()) && !rook.hasMovedEver;
    }

    public static List<Coordinate> getKnightMoves(Piece piece, MoveContext context) {
        List<Coordinate> moves = new ArrayList<>(12);

        int[][] baseOffsets = {
                {-2, -1}, {-2, 1},
                {2, -1}, {2, 1},
                {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}
        };

        int scale = piece.getPieceSize();
        for (int[] offset : baseOffsets) {
            addMoveForOffset(piece, offset[0] * scale, offset[1] * scale, context, moves);
        }
        return moves;
    }

    public static List<Coordinate> getPawnMoves(Piece piece, MoveContext context) {
        List<Coordinate> moves = new ArrayList<>(4);
        Board board = context.board();

        int dir = piece.getTeam().equalsIgnoreCase("White") ? -1 : 1;
        int x = piece.getPosX();
        int y = piece.getPosY();
        int stepSize = piece.getPieceSize();
        int boardSize = board.getBoardSize();

        // Forward movement
        int newY = y + stepSize * dir;
        if (newY >= 0 && newY < boardSize && context.getPieceAt(x, newY, piece) == null) {
            moves.add(new Coordinate(x, newY));

            if (!piece.hasMovedEver) {
                int newY2 = y + 2 * stepSize * dir;
                if (newY2 >= 0 && newY2 < boardSize && context.getPieceAt(x, newY2, piece) == null) {
                    moves.add(new Coordinate(x, newY2));
                }
            }
        }

        // Captures
        addCaptureMoveForOffset(piece, -stepSize, stepSize * dir, context, moves);
        addCaptureMoveForOffset(piece, stepSize, stepSize * dir, context, moves);

        //TODO - I HATE EN PASSANT, ITS STILL PROBABLY BROKEN
        int enPassantRow = piece.getTeam().equalsIgnoreCase("White") ? 4 : 3;
        if (y == enPassantRow) {
            Piece left = context.getPieceAt(x - stepSize, y, piece);
            Piece right = context.getPieceAt(x + stepSize, y, piece);
            int y1 = y + stepSize * dir;

            if (isEnPassantTarget(left, piece)) {
                if (MoveHistory.wasLastMovePawnDoubleAdvance(left.getPosX(), left.getPosY(), left.getTeam())) {
                    moves.add(new Coordinate(left.getPosX(), y1));
                }
            }

            if (isEnPassantTarget(right, piece)) {
                if (MoveHistory.wasLastMovePawnDoubleAdvance(
                        right.getPosX(), right.getPosY(), right.getTeam())) {
                    moves.add(new Coordinate(right.getPosX(), y1));
                }
            }
        }

        return moves;
    }

    private static boolean isEnPassantTarget(Piece target, Piece pawn) {
        return target != null && target.getType() == PieceType.PAWN && !target.getTeam().equals(pawn.getTeam());
    }

    public static List<Coordinate> getSlidingMoves(Piece piece, MoveContext context, int... directions) {
        return getSlidingMoves(piece, context, Integer.MAX_VALUE - 1, directions);
    }

    public static List<Coordinate> getSlidingMoves(Piece piece, MoveContext context, int maxDistance, int... directions) {
        List<Coordinate> moves = new ArrayList<>(16);
        int x = piece.getPosX();
        int y = piece.getPosY();

        for (int i = 0; i < directions.length; i += 2) {
            slideIterative(piece, context, x, y, directions[i], directions[i + 1], moves, maxDistance);
        }
        return moves;
    }

    private static void slideIterative(Piece piece, MoveContext context, int startX, int startY, int dx, int dy, List<Coordinate> moves, int maxDistance) {
        int localX = startX;
        int localY = startY;
        int stepMultiplier = piece.getPieceSize();
        int boardSize = context.board().getBoardSize();
        int traveled = 0;

        // fetch neighbor list once per loop iteration
        while (traveled < maxDistance) {
            int nextX = localX + dx * stepMultiplier;
            int nextY = localY + dy * stepMultiplier;

            if (nextX < 0 || nextX >= boardSize || nextY < 0 || nextY >= boardSize) {
                break;
            }

            if (!isValidMove(nextX, nextY, context, piece)) {
                break;
            }

            Piece target = context.getPieceAt(nextX, nextY, piece);
            if (target != null) {
                if (!target.getTeam().equals(piece.getTeam())) {
                    moves.add(new Coordinate(nextX, nextY));
                }
                break;
            }

            moves.add(new Coordinate(nextX, nextY));
            localX = nextX;
            localY = nextY;
            traveled++;
        }
    }

    protected static boolean isPathClearForCastling(MoveContext context, int x, int y, int direction) {
        int step = direction > 0 ? 1 : -1;
        int limit = direction > 0 ? 7 : 0;
        int i = x + step;
        while (i != limit && i >= 0 && i <= 7) {
            if (context.getPieceAt(i, y, null) != null) {
                return false;
            }
            i += step;
        }
        return true;
    }

    protected static boolean isValidMove(int newX, int newY, MoveContext context, Piece piece) {
        Board board = context.board();
        int boardSize = board.getBoardSize();
        int size = piece.getPieceSize();
        if (newX < 0 || newY < 0 || newX + size > boardSize || newY + size > boardSize) {
            return false;
        }

        return piece.squaresAt(newX, newY).allMatch(square -> {
            Piece other = context.getPieceAt(square.x(), square.y(), piece);
            return other == null || other == piece || !other.getTeam().equals(piece.getTeam());
        });
    }

    protected static boolean isValidCapture(int newX, int newY, MoveContext context, Piece piece) {
        Board board = context.board();
        int boardSize = board.getBoardSize();
        int size = piece.getPieceSize();
        if (newX < 0 || newY < 0 || newX + size > boardSize || newY + size > boardSize) {
            return false;
        }

        if (!isValidMove(newX, newY, context, piece)) {
            return false;
        }

        return piece.squaresAt(newX, newY).anyMatch(square -> {
            Piece other = context.getPieceAt(square.x(), square.y(), piece);
            return other != null && !other.getTeam().equals(piece.getTeam());
        });
    }
}