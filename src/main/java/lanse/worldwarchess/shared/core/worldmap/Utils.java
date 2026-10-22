package lanse.worldwarchess.shared.core.worldmap;

import javafx.util.Pair;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static lanse.worldwarchess.shared.core.screens.ingame.GamePanel.getBoardRegion;

public class Utils {
    //for healers and maybe terrestrials etc needing to know how to find a piece

    // Pixel distance between the two pieces
    public static int distance(Piece a, Piece b) {
        return distance(a.getBoardX(), a.getBoardY(), a.getBoard(), b.getBoardX(), b.getBoardY(), b.getBoard());
    }

    // Pixel distance between the two points
    public static int distance(int x1, int y1, Board board1, int x2, int y2, Board board2) {
        if (board1 == null || board2 == null) return Integer.MAX_VALUE;

        // Convert local board coords to global coords
        int globalX1 = (board1.getBoardX() / board1.getBaseTileSize()) + x1;
        int globalY1 = (board1.getBoardY() / board1.getBaseTileSize()) + y1;

        int globalX2 = (board2.getBoardX() / board2.getBaseTileSize()) + x2;
        int globalY2 = (board2.getBoardY() / board2.getBaseTileSize()) + y2;

        return Math.abs(globalX1 - globalX2) + Math.abs(globalY1 - globalY2);
    }

    // Returns the distance between a piece and a coordinate on the same board, or +∞ if they are on separate boards
    public static double localDistance(Piece p, LegalMoveDetector.Coordinate c) {
        return p.squares().map((square) -> localDistance(square, c)).min(Double::compareTo).orElse(Double.POSITIVE_INFINITY);
    }

    // Returns the distance between two coordinates on a board, or +∞ if they are on separate boards
    public static double localDistance(LegalMoveDetector.Coordinate c1, LegalMoveDetector.Coordinate c2) {
        return localDistance(c1.x(), c1.y(), c1.boardID(), c2.x(), c2.y(), c2.boardID());
    }

    // Returns the distance between two points on a board, or +∞ if they are on separate boards
    public static double localDistance(int x1, int y1, int b1, int x2, int y2, int b2) {
        if (b1 != b2) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }

    /**
     * Converts a pixel location into a logical coordinate (x, y, boardID)
     * @param px The point on the screen
     * @param pieceForScale A piece to give the size for where to snap to the grid
     * @param zoomFactor The current screen zoom factor
     * @return The nearest logical coordinate the piece could land on, or (0, 0, -404) if there is none within range.
     */
    public static LegalMoveDetector.Coordinate pxToLogicalCoords(Point px, Piece pieceForScale, double zoomFactor) {
        return recursivePxToLogicalCoords(px, pieceForScale, zoomFactor, List.of(
                new Pair<>(0, 0),
                new Pair<>(-1, 0), new Pair<>(1, 0),
                new Pair<>(0, -1), new Pair<>(0, 1),
                new Pair<>(-1, -1), new Pair<>(1, 1),
                new Pair<>(-1, 1), new Pair<>(1, -1)
        ).iterator());
    }

    // Helper for pxToLogicalCoords, to allow checking offsets in different directions
    private static LegalMoveDetector.Coordinate recursivePxToLogicalCoords(Point px, Piece pieceForScale, double zoomFactor, Iterator<Pair<Integer, Integer>> offsets) {
        Pair<Integer, Integer> offset = offsets.next();
        double worldX = px.x - LegalMoveDetector.OFFSET_CORRECTION_VALUE * pieceForScale.getPieceSize() * offset.getKey();
        double worldY = px.y - LegalMoveDetector.OFFSET_CORRECTION_VALUE * pieceForScale.getPieceSize() * offset.getValue();

        for (Board board : WorldMap.boards.values()) {
            Rectangle region = getBoardRegion(board, zoomFactor);
            if (!region.contains(worldX, worldY)) continue;

            int baseTile = board.getBaseTileSize();
            int pieceTileSize = (int) (pieceForScale.getPieceSize() * baseTile * zoomFactor);

            double newLogicalX = (worldX - region.x) / pieceTileSize;
            double newLogicalY = (worldY - region.y) / pieceTileSize;
            int boardSize = board.getBoardSize();

            if (newLogicalX >= 0 && newLogicalY >= 0 && newLogicalX < boardSize - 1 && newLogicalY < boardSize - 1) {
                return new LegalMoveDetector.Coordinate(
                        (int)Math.round(newLogicalX * pieceForScale.getPieceSize()),
                        (int)Math.round(newLogicalY * pieceForScale.getPieceSize()),
                        board.getBoardID());
            }
        }
        // Invalid board ID
        if (offsets.hasNext()) {
            return recursivePxToLogicalCoords(px, pieceForScale, zoomFactor, offsets);
        }
        else {
            return new LegalMoveDetector.Coordinate(0, 0, -404);
        }
    }

    // The distance we should try and move a piece to snap it into place on this board size
    private static double snapDistance(int boardSize) {
        return Math.pow(boardSize / 4.0, 2);
    }

    /**
     * Finds the nearest coordinate a piece could move to, within range
     * @param p The coordinate the piece is technically over
     * @param legalMoves Every move it is allowed to make
     * @return The nearest move to p (on the same board), that is within snapping distance.
     */
    public static Optional<LegalMoveDetector.Coordinate> nearestLegalCoordinate(LegalMoveDetector.Coordinate p, List<LegalMoveDetector.Coordinate> legalMoves) {
        try {
            int boardSize = WorldMap.getBoardByID(p.boardID()).getBoardSize();
            return legalMoves.stream()
                    .filter(coordinate -> p.boardID() == coordinate.boardID())
                    .filter(coordinate ->
                            localDistance(coordinate, p) < Math.pow(boardSize / 4.0, 1.5))
                    .min(Comparator.comparing((coordinate -> localDistance(coordinate, p))));
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the nearest piece to coord, from the list of validPieces
     * @param coord The coordinate to start the search at
     * @param validPieces The pieces to check for proximity
     * @return The piece in validPieces that is nearest to coord, while being on the same board and within snapping distance
     */
    public static Optional<Piece> nearestPiece(LegalMoveDetector.Coordinate coord, List<Piece> validPieces) {
        int boardSize = WorldMap.getBoardByID(coord.boardID()).getBoardSize();
        return validPieces.stream().filter(piece -> piece.getBoard().getBoardID() == coord.boardID())
                .filter(piece -> localDistance(piece, coord) < Math.pow(boardSize / 32.0, 2))
                .min(Comparator.comparing(piece -> localDistance(piece, coord)));
    }

    /** Returns the distance of a point on a line to a region from start to start+length on that line
     * @param spot Point on the line to compare
     * @param start Start of region to compare
     * @param length Size of region to compare
     * @return Distance from spot to the nearest of start and start+length,
     * unless it is between them, in which case it returns 0
     */
    private static double distance1D(double spot, double start, double length) {
        if (spot > start && spot <= start + length) {
            return 0;
        }
        else {
            return Math.min(Math.abs(start - spot), Math.abs(spot - (start + length)));
        }

    }

    /**
     * @param x The x coordinate of the point to compare
     * @param y The y coordinate of the point to compare
     * @param region A rectangle bounding a region on the XY plane
     * @return The distance (squared) from the point to the nearest location in that region.
     */
    public static double distance(int x, int y, Rectangle region) {
        return Math.pow(distance1D(x, region.x, region.width), 2) + Math.pow(distance1D(y, region.y, region.height), 2);
    }

    /**
     * Returns the nearest board to coord, from the list of validBoards
     * @param x The coordinate to start the search at
     * @param y The coordinate to start the search at
     * @param validBoards The boards to check
     * @return The board in validBoards that is nearest to coord, while being within a certain snapping distance
     */
    public static Optional<Board> nearestBoard(int x, int y, Stream<Board> validBoards, double zoomFactor) {
        return validBoards.min(Comparator.comparing((board) -> distance(x, y, GamePanel.getBoardRegion(board, zoomFactor))));
    }

    public static int boardPxSize(Board board) {
        return (board.getBoardSize() * board.getBaseTileSize());
    }

    public static Pair<Integer, Integer> snapBoard(int x, int y, Board board, double zoomFactor) {
        Optional<Board> nearest = nearestBoard((int) (x * zoomFactor), (int) (y * zoomFactor), WorldMap.boards.values().stream().filter((b) -> b.getBoardID() != board.getBoardID()), zoomFactor);
        if (nearest.isEmpty()) {
            return new Pair<>(x, y);
        }
        Board n = nearest.get();
        if (x < n.getBoardX()) {
            x = n.getBoardX() - boardPxSize(board) - MapShapePresets.GAP;
        } else if (x > n.getBoardX() + boardPxSize(n)) {
            x = n.getBoardX() + boardPxSize(n) + MapShapePresets.GAP;
        }
        else {
            x = n.getBoardX();
        }
        if (y < n.getBoardY()) {
            y = n.getBoardY() - boardPxSize(board) - MapShapePresets.GAP;
        } else if (y > n.getBoardY() + boardPxSize(n)) {
            y = n.getBoardY() + boardPxSize(n) + MapShapePresets.GAP;
        }
        else {
            y = n.getBoardY();
        }
        return new Pair<>(x, y);
    }
}
