package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class LegalMoveDetector {

    //This class is responsible for calculating all the base piece movements in chess. Like rooks move horrizontally
    // and vertically, while bishops move diagonally, knights move in an L, and queens are like a rook + bishop.

    public record Coordinate(int x, int y, int boardID) {}
    public static int TOLERENCE_PIXELS = 100;
    public static int OFFSET_CORRECTION_VALUE = 25;
    public static int[] bishopDirections = {1, 1, 1, -1, -1, 1, -1, -1};
    public static int[] rookDirections = {1, 0, -1, 0, 0, 1, 0, -1};
    public static int[] queenDirections = {1, 1, 1, -1, -1, 1, -1, -1, 1, 0, -1, 0, 0, 1, 0, -1};
//    public static List<Point> debugRawPoints = new ArrayList<>();

    public static List<Coordinate> getLegalMoves(Piece piece) {
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

            if (newMoves != null) {
                legalMoves.addAll(newMoves);
            }

            return legalMoves;

        } catch (Exception e){
            System.err.println("ERROR WITH MOVING PIECE!");
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
                moves.add(new Coordinate(localX, localY, currentBoard.getBoardID()));
            }
        } else {
            boardFinder(currentBoard, piece, offsetX, offsetY, currentBoard.getBaseTileSize(), moves);
        }
    }

    private static void addCaptureMoveForOffset(Piece piece, int offsetX, int offsetY, Board currentBoard, List<Coordinate> moves) {
        int boardSize = currentBoard.getBoardSize();
        int localX = piece.getPosX() + offsetX;
        int localY = piece.getPosY() + offsetY;

        if (localX >= 0 && localX < boardSize && localY >= 0 && localY < boardSize) {
            if (isValidCapture(localX, localY, currentBoard, piece)) {
                moves.add(new Coordinate(localX, localY, currentBoard.getBoardID()));
            }
        } else {
            boardFinder(currentBoard, piece, offsetX, offsetY, currentBoard.getBaseTileSize(), moves);
        }
    }

    public static List<Coordinate> getKingMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>(12);
        int stepSize = piece.getPieceSize();

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
                    isPathClearForCastling(board, piece.getPosX(), y, -1)) {
                int newX = piece.getPosX() - 2;
                if (newX >= 0 && newX < 8) moves.add(new Coordinate(newX, y, board.getBoardID()));
            }
            if (!MoveHistory.hasRookMoved(piece.getBoard(), 7, y) &&
                    isPathClearForCastling(board, piece.getPosX(), y, 1)) {
                int newX = piece.getPosX() + 2;
                if (newX >= 0 && newX < 8) moves.add(new Coordinate(newX, y, board.getBoardID()));
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

        int scale = piece.getPieceSize();
        for (int[] offset : baseOffsets) {
            addMoveForOffset(piece, offset[0] * scale, offset[1] * scale, board, moves);
        }
        return moves;
    }

    public static List<Coordinate> getPawnMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>(4);
        int dir = piece.getTeam().equals("White") ? -1 : 1;
        int x = piece.getPosX(), y = piece.getPosY();
        int stepSize = piece.getPieceSize();
        int firstMoveStep = 2 * stepSize;
        int boardSize = board.getBoardSize();

        List<Piece> boardPieces = WorldMap.getAllPiecesOfBoard(board);

        // Forward movement
        int newY = y + stepSize * dir;
        if (newY >= 0 && newY < boardSize &&
                board.getPieceAt(x, newY, Optional.of(piece), boardPieces) == null) {
            moves.add(new Coordinate(x, newY, board.getBoardID()));

            if (!piece.hasMovedEver) {
                int newY2 = y + firstMoveStep * dir;
                if (newY2 >= 0 && newY2 < boardSize &&
                        board.getPieceAt(x, newY2, Optional.of(piece), boardPieces) == null) {
                    moves.add(new Coordinate(x, newY2, board.getBoardID()));
                }
            }
        }

        // Captures
        addCaptureMoveForOffset(piece, -1, dir, board, moves);
        addCaptureMoveForOffset(piece, 1, dir, board, moves);

        // En passant
        int enPassantRow = piece.getTeam().equals("White") ? 4 : 3;
        if (y == enPassantRow) {
            Piece left = board.getPieceAt(x - 1, y, Optional.of(piece), boardPieces);
            Piece right = board.getPieceAt(x + 1, y, Optional.of(piece), boardPieces);

            int y1 = y + 1 * dir;
            if (left != null && left.getType() == PieceType.PAWN && !left.getTeam().equals(piece.getTeam())
                    && MoveHistory.wasLastMovePawnDoubleAdvance(left.getPosX(), left.getPosY(), left.getTeam())) {
                moves.add(new Coordinate(left.getPosX(), y1, board.getBoardID()));
            }
            if (right != null && right.getType() == PieceType.PAWN && !right.getTeam().equals(piece.getTeam())
                    && MoveHistory.wasLastMovePawnDoubleAdvance(right.getPosX(), right.getPosY(), right.getTeam())) {
                moves.add(new Coordinate(right.getPosX(), y1, board.getBoardID()));
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

    private static void slideIterative(Piece piece, Board startBoard, int startX, int startY, int dx, int dy, List<Coordinate> moves, int maxDistance) {
        Board currentBoard = startBoard;
        int localX = startX;
        int localY = startY;
        final int stepMultiplier = piece.getPieceSize();
        List<Piece> cachedPieces = WorldMap.getAllPiecesOfBoard(currentBoard);

        int tileSize = currentBoard.getBaseTileSize();
        int boardSize = currentBoard.getBoardSize();
        int traveled = 0;

        // fetch neighbor list once per loop iteration
        while (traveled < maxDistance) {
            int nextX = localX + dx * stepMultiplier;
            int nextY = localY + dy * stepMultiplier;

            // same-board move
            if (nextX >= 0 && nextX < boardSize && nextY >= 0 && nextY < boardSize) {
                if (!isValidMove(nextX, nextY, currentBoard, piece)) break;

                Piece target = currentBoard.getPieceAt(nextX, nextY, Optional.of(piece), cachedPieces);
                if (target != null) {
                    if (!target.getTeam().equals(piece.getTeam())) {
                        moves.add(new Coordinate(nextX, nextY, currentBoard.getBoardID()));
                    }
                    break;
                }

                moves.add(new Coordinate(nextX, nextY, currentBoard.getBoardID()));
                localX = nextX;
                localY = nextY;
                traveled++;
                continue;
            }

            // cross-board: compute global once
            final int globalX = currentBoard.getBoardX() + localX * tileSize;
            final int globalY = currentBoard.getBoardY() + localY * tileSize;
            final int newGlobalX = globalX + dx * tileSize * stepMultiplier + OFFSET_CORRECTION_VALUE;
            final int newGlobalY = globalY + dy * tileSize * stepMultiplier + OFFSET_CORRECTION_VALUE;
//            LegalMoveDetector.debugRawPoints.add(new Point(newGlobalX, newGlobalY));

            boolean crossed = false;
            List<Board> neighbors = currentBoard.getNeighboringBoards();
            for (Board neighbor : neighbors) {
                int neighborTileSize = neighbor.getBaseTileSize();
                int neighborSize = neighbor.getBoardSize();
                int boardX = neighbor.getBoardX();
                int boardY = neighbor.getBoardY();
                int boardWidth = neighborTileSize * neighborSize;
                int boardHeight = neighborTileSize * neighborSize;

                if (newGlobalX >= boardX - TOLERENCE_PIXELS && newGlobalX < boardX + boardWidth + TOLERENCE_PIXELS &&
                        newGlobalY >= boardY - TOLERENCE_PIXELS && newGlobalY < boardY + boardHeight + TOLERENCE_PIXELS) {

                    int snappedX = (int) ((newGlobalX - boardX) / neighborTileSize + 0.5);
                    int snappedY = (int) ((newGlobalY - boardY) / neighborTileSize + 0.5);

                    if (snappedX >= 0 && snappedX < neighborSize && snappedY >= 0 && snappedY < neighborSize) {
                        if (!isValidMove(snappedX, snappedY, neighbor, piece)) {
                            // stop sliding in this direction (tile blocked) - ends the direction
                            return;
                        }

                        List<Piece> neighborPieces = WorldMap.getAllPiecesOfBoard(neighbor);
                        Piece target = neighbor.getPieceAt(snappedX, snappedY, Optional.of(piece), neighborPieces);

                        if (target != null) {
                            if (!target.getTeam().equals(piece.getTeam())) {
                                moves.add(new Coordinate(snappedX, snappedY, neighbor.getBoardID()));
                            }
                            // stop after capture
                            return;
                        }

                        moves.add(new Coordinate(snappedX, snappedY, neighbor.getBoardID()));

                        // cross to new board
                        currentBoard = neighbor;
                        cachedPieces = neighborPieces;
                        tileSize = currentBoard.getBaseTileSize();
                        boardSize = currentBoard.getBoardSize();
                        localX = snappedX;
                        localY = snappedY;
                        traveled++;
                        crossed = true;
                        break;
                    }
                }
            }

            if (!crossed) break;
            if (traveled >= maxDistance) break;
        }
    }

    protected static void boardFinder(Board currentBoard, Piece piece, int offsetX, int offsetY, int tileSize, List<Coordinate> moves) {
        final int pieceGlobalX = currentBoard.getBoardX() + piece.getPosX() * tileSize;
        final int pieceGlobalY = currentBoard.getBoardY() + piece.getPosY() * tileSize;
        final int newGlobalX = pieceGlobalX + offsetX * tileSize;
        final int newGlobalY = pieceGlobalY + offsetY * tileSize;
//        LegalMoveDetector.debugRawPoints.add(new Point(newGlobalX, newGlobalY));

        List<Board> neighbors = currentBoard.getNeighboringBoards();
        for (Board board : neighbors) {
            if (board.getBoardID() == currentBoard.getBoardID()) continue;

            final int neighborTileSize = board.getBaseTileSize();
            final int neighborTileCount = board.getBoardSize();
            final int boardX = board.getBoardX();
            final int boardY = board.getBoardY();
            final int boardWidth = neighborTileSize * neighborTileCount;
            final int boardHeight = neighborTileSize * neighborTileCount;

            if (newGlobalX >= boardX - TOLERENCE_PIXELS && newGlobalX < boardX + boardWidth + TOLERENCE_PIXELS &&
                    newGlobalY >= boardY - TOLERENCE_PIXELS && newGlobalY < boardY + boardHeight + TOLERENCE_PIXELS) {

                double relX = (double) (newGlobalX - boardX) / neighborTileSize;
                double relY = (double) (newGlobalY - boardY) / neighborTileSize;
                int snappedX = (int) (relX + 0.5);
                int snappedY = (int) (relY + 0.5);

                if (snappedX >= 0 && snappedX < neighborTileCount && snappedY >= 0 && snappedY < neighborTileCount) {
                    if (isValidMove(snappedX, snappedY, board, piece)) {
                        moves.add(new Coordinate(snappedX, snappedY, board.getBoardID()));
                    }
                }
            }
        }
    }

    protected static boolean isPathClearForCastling(Board board, int x, int y, int direction) {
        int step = (direction > 0) ? 1 : -1;
        List<Piece> boardPieces = WorldMap.getAllPiecesOfBoard(board);
        int limit = (direction > 0) ? 7 : 0;
        int i = x + step;
        while (i != limit && i >= 0 && i <= 7) {
            if (board.getPieceAt(i, y, Optional.empty(), boardPieces) != null) return false;
            i += step;
        }
        return true;
    }

    protected static boolean isValidMove(int newX, int newY, Board board, Piece piece) {
        int boardSize = board.getBoardSize();
        int size = piece.getPieceSize();
        if (newX < 0 || newY < 0 || newX + size > boardSize || newY + size > boardSize) return false;

        List<Piece> boardPieces = WorldMap.getAllPiecesOfBoard(board);
        return piece.squaresAt(newX, newY, board).allMatch((square) -> {
            Piece p = board.getPieceAt(square.x(), square.y(), Optional.empty(), boardPieces);
            return p == null || p == piece || !p.getTeam().equals(piece.getTeam());
        });
    }

    protected static boolean isValidCapture(int newX, int newY, Board board, Piece piece) {
        int boardSize = board.getBoardSize();
        int size = piece.getPieceSize();
        if (newX < 0 || newY < 0 || newX + size > boardSize || newY + size > boardSize) return false;

        List<Piece> boardPieces = WorldMap.getAllPiecesOfBoard(board);
        return isValidMove(newX, newY, board, piece) && piece.squaresAt(newX, newY, board).anyMatch((square) -> {
            Piece p = board.getPieceAt(square.x(), square.y(), Optional.empty(), boardPieces);
            return p != null && !p.getTeam().equals(piece.getTeam());
        });
    }
}