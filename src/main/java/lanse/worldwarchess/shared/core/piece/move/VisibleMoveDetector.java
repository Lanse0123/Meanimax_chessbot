package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VisibleMoveDetector extends LegalMoveDetector{

    //This class is responsible for calculating all the base piece movements in chess. Like rooks move horrizontally
    // and vertically, while bishops move diagonally, knights move in an L, and queens are like a rook + bishop.
    // This is for NPCs that want to be defensive and see enemy moves.

    public static int[] bishopDirections = {1, 1, 1, -1, -1, 1, -1, -1};
    public static int[] rookDirections = {1, 0, -1, 0, 0, 1, 0, -1};
    public static int[] queenDirections = {1, 1, 1, -1, -1, 1, -1, -1, 1, 0, -1, 0, 0, 1, 0, -1};

    public static List<Coordinate> getVisibleMoves(Piece piece) {
        try {
            List<Coordinate> legalMoves = new ArrayList<>(16);
            Board currentBoard = piece.getBoard();
            if (currentBoard == null) return legalMoves;
            PieceType type = piece.getType();

            Collection<Coordinate> newMoves = switch (type) {
                case PAWN -> getPawnMoves(piece, currentBoard);
                case KNIGHT -> getKnightMoves(piece, currentBoard);
                case BISHOP -> getSlidingMoves(piece, currentBoard, bishopDirections);
                case ROOK -> getSlidingMoves(piece, currentBoard, rookDirections);
                case QUEEN -> getSlidingMoves(piece, currentBoard, queenDirections);
                case KING -> getKingMoves(piece, currentBoard);
            };

            legalMoves.addAll(newMoves);
            return legalMoves;

        } catch (Exception e){
            System.err.println("FATAL ERROR WITH MOVING PIECE!");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Add one-tile offset; if off-board delegate to boardFinder
    protected static void addMoveForOffset(Piece piece, int offsetX, int offsetY, Board currentBoard, List<Coordinate> moves) {
        int boardSize = currentBoard.getBoardSize();
        int localX = piece.getPosX() + offsetX;
        int localY = piece.getPosY() + offsetY;

        if (localX >= 0 && localX < boardSize && localY >= 0 && localY < boardSize) {
            if (isValidMove(localX, localY, currentBoard, piece)) {
                moves.add(new Coordinate(localX, localY));
            }
        }
    }

    private static void addCaptureMoveForOffset(Piece piece, int offsetX, int offsetY, Board currentBoard, List<Coordinate> moves) {
        int boardSize = currentBoard.getBoardSize();
        int localX = piece.getPosX() + offsetX;
        int localY = piece.getPosY() + offsetY;

        if (localX >= 0 && localX < boardSize && localY >= 0 && localY < boardSize) {
            if (isValidCapture(localX, localY, currentBoard, piece)) {
                moves.add(new Coordinate(localX, localY));
            }
        }
    }

    public static List<Coordinate> getKingMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>(12);
        int stepSize = 1;

        int[][] offsets = {
                {-stepSize, -stepSize}, {-stepSize, 0}, {-stepSize, stepSize},
                {0, -stepSize}, {0, stepSize},
                {stepSize, -stepSize}, {stepSize, 0}, {stepSize, stepSize}
        };

        for (int[] offset : offsets) {
            addMoveForOffset(piece, offset[0], offset[1], board, moves);
        }

        // Castling
        if (!piece.hasMovedEver) {
            int y = piece.getPosY();
            if (!MoveHistory.hasRookMoved(piece.getBoard(), 0, y) &&
                    LegalMoveDetector.isPathClearForCastling(MoveContext.contextify(board), piece.getPosX(), y, -1)) {
                int newX = piece.getPosX() - 2;
                if (newX >= 0 && newX < 8) moves.add(new Coordinate(newX, y));
            }
            if (!MoveHistory.hasRookMoved(piece.getBoard(), 7, y) &&
                    LegalMoveDetector.isPathClearForCastling(MoveContext.contextify(board), piece.getPosX(), y, 1)) {
                int newX = piece.getPosX() + 2;
                if (newX >= 0 && newX < 8) moves.add(new Coordinate(newX, y));
            }
        }
        return moves;
    }

    public static List<Coordinate> getKnightMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>(12);

        int[][] baseOffsets = {
                {-2, -1}, {-2, 1},
                {2, -1}, {2, 1},
                {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}
        };

        for (int[] offset : baseOffsets) {
            addMoveForOffset(piece, offset[0], offset[1], board, moves);
        }
        return moves;
    }

    public static List<Coordinate> getPawnMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>(4);
        int dir = piece.getTeam() ? -1 : 1;
        int x = piece.getPosX(), y = piece.getPosY();
        int firstMoveStep = 2;
        int boardSize = board.getBoardSize();

        List<Piece> boardPieces = WorldMap.getAllPiecesOfBoard(board);

        // Forward movement
        int newY = y + dir;
        if (newY >= 0 && newY < boardSize && board.getPieceAt(x, newY, piece, boardPieces) == null) {
            moves.add(new Coordinate(x, newY));

            if (!piece.hasMovedEver) {
                int newY2 = y + firstMoveStep * dir;
                if (newY2 >= 0 && newY2 < boardSize && board.getPieceAt(x, newY2, piece, boardPieces) == null) {
                    moves.add(new Coordinate(x, newY2));
                }
            }
        }

        // Captures
        addCaptureMoveForOffset(piece, -1, dir, board, moves);
        addCaptureMoveForOffset(piece, 1, dir, board, moves);

        // En passant
        int enPassantRow = piece.getTeam() ? 4 : 3;
        if (y == enPassantRow) {
            Piece left = board.getPieceAt(x - 1, y, piece, boardPieces);
            Piece right = board.getPieceAt(x + 1, y, piece, boardPieces);

            int y1 = y + dir;
            if (left != null && left.getType() == PieceType.PAWN && left.getTeam() != piece.getTeam()
                    && MoveHistory.wasLastMovePawnDoubleAdvance(left.getPosX(), left.getPosY(), left.getTeam())) {
                moves.add(new Coordinate(left.getPosX(), y1));
            }
            if (right != null && right.getType() == PieceType.PAWN && right.getTeam() != piece.getTeam()
                    && MoveHistory.wasLastMovePawnDoubleAdvance(right.getPosX(), right.getPosY(), right.getTeam())) {
                moves.add(new Coordinate(right.getPosX(), y1));
            }
        }

        return moves;
    }

    public static List<Coordinate> getSlidingMoves(Piece piece, Board board, int... directions) {
        return getSlidingMoves(piece, board, Integer.MAX_VALUE - 1, directions);
    }

    public static List<Coordinate> getSlidingMoves(Piece piece, Board board, int maxDistance, int... directions) {
        List<Coordinate> moves = new ArrayList<>(16);
        int x = piece.getPosX(), y = piece.getPosY();
        for (int i = 0; i < directions.length; i += 2) {
            int dx = directions[i], dy = directions[i + 1];
            slideIterative(piece, board, x, y, dx, dy, moves, maxDistance);
        }
        return moves;
    }

    private static void slideIterative(Piece piece, Board board, int startX, int startY, int dx, int dy, List<Coordinate> moves, int maxDistance) {
        int localX = startX;
        int localY = startY;

        int boardSize = board.getBoardSize();
        int traveled = 0;

        // fetch neighbor list once per loop iteration
        while (traveled < maxDistance) {
            int nextX = localX + dx;
            int nextY = localY + dy;

            if (nextX >= 0 && nextX < boardSize && nextY >= 0 && nextY < boardSize) {
                if (!isValidMove(nextX, nextY, board, piece)) break;

                moves.add(new Coordinate(nextX, nextY));
                localX = nextX;
                localY = nextY;
                traveled++;
                continue;
            }

            break;
        }
    }

    protected static boolean isValidMove(int x, int y, Board board, Piece piece) {
        return LegalMoveDetector.isValidMove(x, y, MoveContext.contextify(board), piece);
    }

    protected static boolean isValidCapture(int x, int y, Board board, Piece piece) {
        return LegalMoveDetector.isValidCapture(x, y, MoveContext.contextify(board), piece);
    }
}