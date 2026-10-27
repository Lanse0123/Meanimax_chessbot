
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
    public static final int OFFSET_CORRECTION_VALUE = 25;
    public static final int[] bishopDirections = {1, 1, 1, -1, -1, 1, -1, -1};
    public static final int[] rookDirections = {1, 0, -1, 0, 0, 1, 0, -1};
    public static final int[] queenDirections = {1, 1, 1, -1, -1, 1, -1, -1, 1, 0, -1, 0, 0, 1, 0, -1};
    private static final int[][] KING_OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},             {0, 1},
            {1, -1},  {1, 0},   {1, 1}
    };

    private static final int[][] KNIGHT_OFFSETS = {
            {-2, -1}, {-2, 1},
            {2, -1},  {2, 1},
            {-1, -2}, {-1, 2},
            {1, -2},  {1, 2}
    };

    private static final List<Coordinate> NO_MOVES = List.of();

    public static List<Coordinate> getLegalMoves(Piece piece) {
        return getLegalMoves(piece, MoveContext.contextify(piece.getBoard()));
    }

    public static List<Coordinate> getLegalMoves(Piece piece, MoveContext context) {
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
        int x = piece.getPosX() + offsetX;
        int y = piece.getPosY() + offsetY;

        if (isValidMove(x, y, context, piece)) {
            moves.add(new Coordinate(x, y));
        }
    }

    private static void addCaptureMoveForOffset(Piece piece, int offsetX, int offsetY, MoveContext context, List<Coordinate> moves) {
        int x = piece.getPosX() + offsetX;
        int y = piece.getPosY() + offsetY;

        if (isValidCapture(x, y, context, piece)) {
            moves.add(new Coordinate(x, y));
        }
    }

    public static List<Coordinate> getKingMoves(Piece piece, MoveContext context) {
        List<Coordinate> moves = new ArrayList<>(12);

        for (int[] offset : KING_OFFSETS) {
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
        return rook != null && rook.getType() == PieceType.ROOK && rook.getTeam() == king.getTeam() && !rook.hasMovedEver;
    }

    public static List<Coordinate> getKnightMoves(Piece piece, MoveContext context) {
        List<Coordinate> moves = new ArrayList<>(12);

        int scale = 1;
        for (int[] offset : KNIGHT_OFFSETS) {
            addMoveForOffset(piece, offset[0] * scale, offset[1] * scale, context, moves);
        }
        return moves;
    }

    public static List<Coordinate> getPawnMoves(Piece piece, MoveContext context) {
        List<Coordinate> moves = new ArrayList<>(4);
        Board board = context.board();

        int dir = piece.getTeam() ? -1 : 1;
        int x = piece.getPosX();
        int y = piece.getPosY();
        int boardSize = board.getBoardSize();

        // Forward movement
        int newY = y + dir;
        if (newY >= 0 && newY < boardSize && context.getPieceAt(x, newY, piece) == null) {
            moves.add(new Coordinate(x, newY));

            if (!piece.hasMovedEver) {
                int newY2 = y + 2 * dir;
                if (newY2 >= 0 && newY2 < boardSize && context.getPieceAt(x, newY2, piece) == null) {
                    moves.add(new Coordinate(x, newY2));
                }
            }
        }

        // Captures
        addCaptureMoveForOffset(piece, -1, dir, context, moves);
        addCaptureMoveForOffset(piece, 1, dir, context, moves);

        //TODO - I HATE EN PASSANT, ITS STILL PROBABLY BROKEN
        int enPassantRow = piece.getTeam() ? 4 : 3;
        if (y == enPassantRow && x > 0 && x < 7) {
            Piece left = context.getPieceAt(x - 1, y, piece);
            Piece right = context.getPieceAt(x + 1, y, piece);
            int y1 = y + dir;

            if (isEnPassantTarget(left, piece)) {
                if (MoveHistory.wasLastMovePawnDoubleAdvance(left.getPosX(), left.getPosY(), left.getTeam())) {
                    moves.add(new Coordinate(left.getPosX(), y1));
                }
            }

            if (isEnPassantTarget(right, piece)) {
                if (MoveHistory.wasLastMovePawnDoubleAdvance(right.getPosX(), right.getPosY(), right.getTeam())) {
                    moves.add(new Coordinate(right.getPosX(), y1));
                }
            }
        }

        return moves;
    }

    private static boolean isEnPassantTarget(Piece target, Piece pawn) {
        return target != null && target.getType() == PieceType.PAWN && target.getTeam() != pawn.getTeam();
    }

    public static List<Coordinate> getSlidingMoves(Piece piece, MoveContext context, int... directions) {
        return getSlidingMoves(piece, context, Integer.MAX_VALUE, directions);
    }

    public static List<Coordinate> getSlidingMoves(Piece piece, MoveContext context, int maxDistance, int... directions) {
        List<Coordinate> moves = new ArrayList<>(16);
        int x = piece.getPosX();
        int y = piece.getPosY();
        boolean team = piece.getTeam();
        int boardSize = context.board().getBoardSize();

        for (int i = 0; i < directions.length; i += 2) {
            int dx = directions[i];
            int dy = directions[i + 1];
            int currentX = x;
            int currentY = y;

            for (int distance = 1; distance <= maxDistance; distance++) {
                currentX += dx;
                currentY += dy;

                if (currentX < 0 || currentY < 0 || currentX >= boardSize || currentY >= boardSize) {
                    break;
                }

                Piece target = context.getPieceAt(currentX, currentY, null);

                if (target != null) {
                    if (target.getTeam() != team) {
                        moves.add(new Coordinate(currentX, currentY));
                    }
                    break;
                }

                moves.add(new Coordinate(currentX, currentY));
            }
        }

        return moves;
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
        if (newX < 0 || newY < 0 || newX >= 8 || newY >= 8) {
            return false;
        }

        Piece other = context.getPieceAt(newX, newY, piece);
        return other == null || other == piece || !other.getTeam() == piece.getTeam();
    }

    protected static boolean isValidCapture(int newX, int newY, MoveContext context, Piece piece) {
        if (newX < 0 || newY < 0 || newX >= 8 || newY >= 8) {
            return false;
        }

        Piece other = context.getPieceAt(newX, newY, piece);
        return other != null && other != piece && !other.getTeam() == piece.getTeam();
    }
}