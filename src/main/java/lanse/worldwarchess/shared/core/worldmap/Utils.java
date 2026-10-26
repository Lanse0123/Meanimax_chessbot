package lanse.worldwarchess.shared.core.worldmap;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static lanse.worldwarchess.shared.core.screens.ingame.GamePanel.getBoardRegion;

public class Utils {
    //for healers and maybe terrestrials etc needing to know how to find a piece

    // Returns the distance between a piece and a coordinate on the same board, or +∞ if they are on separate boards
    public static double localDistance(Piece p, LegalMoveDetector.Coordinate c) {
        return p.squares().map((square) -> localDistance(square, c)).min(Double::compareTo).orElse(Double.POSITIVE_INFINITY);
    }

    // Returns the distance between two coordinates on a board, or +∞ if they are on separate boards
    public static double localDistance(LegalMoveDetector.Coordinate c1, LegalMoveDetector.Coordinate c2) {
        return localDistance(c1.x(), c1.y(), c2.x(), c2.y());
    }

    // Returns the distance between two points on a board, or +∞ if they are on separate boards
    public static double localDistance(int x1, int y1, int x2, int y2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }

    /**
     * Converts a pixel location into a logical coordinate (x, y, boardID)
     * @param px The point on the screen
     * @param zoomFactor The current screen zoom factor
     * @return The nearest logical coordinate the piece could land on, or (0, 0, -404) if there is none within range.
     */
    public static LegalMoveDetector.Coordinate pxToLogicalCoords(Point px, double zoomFactor) {
        return recursivePxToLogicalCoords(px, zoomFactor, List.of(
                new Point(0, 0),
                new Point(-1, 0), new Point(1, 0),
                new Point(0, -1), new Point(0, 1),
                new Point(-1, -1), new Point(1, 1),
                new Point(-1, 1), new Point(1, -1)
        ).iterator());
    }

    private static LegalMoveDetector.Coordinate recursivePxToLogicalCoords(Point px, double zoomFactor, Iterator<Point> offsets) {
        Point offset = offsets.next();

        double worldX = px.x - LegalMoveDetector.OFFSET_CORRECTION_VALUE * offset.x;
        double worldY = px.y - LegalMoveDetector.OFFSET_CORRECTION_VALUE * offset.y;

        Board board = WorldMap.board;
        Rectangle region = getBoardRegion(board, zoomFactor);

        int baseTile = board.getBaseTileSize();
        int pieceTileSize = (int) (baseTile * zoomFactor);

        double newLogicalX = (worldX - region.x) / pieceTileSize;
        double newLogicalY = (worldY - region.y) / pieceTileSize;
        int boardSize = board.getBoardSize();

        if (newLogicalX >= 0 && newLogicalY >= 0 && newLogicalX < boardSize - 1 && newLogicalY < boardSize - 1) {

            return new LegalMoveDetector.Coordinate((int) Math.round(newLogicalX),
                    (int) Math.round(newLogicalY));
        }

        if (offsets.hasNext()) {
            return recursivePxToLogicalCoords(px, zoomFactor, offsets);
        }

        //TODO - this is supposed to be an invalid move
        return new LegalMoveDetector.Coordinate(0, 0);
    }

    /**
     * Finds the nearest coordinate a piece could move to, within range
     * @param p The coordinate the piece is technically over
     * @param legalMoves Every move it is allowed to make
     * @return The nearest move to p (on the same board), that is within snapping distance.
     */
    public static Optional<LegalMoveDetector.Coordinate> nearestLegalCoordinate(LegalMoveDetector.Coordinate p, List<LegalMoveDetector.Coordinate> legalMoves) {
        try {
            int boardSize = WorldMap.getBoard().getBoardSize();
            return legalMoves.stream().filter(coordinate -> localDistance(coordinate, p) < Math.pow(boardSize / 4.0, 1.5))
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
        int boardSize = WorldMap.getBoard().getBoardSize();
        return validPieces.stream().filter(piece -> localDistance(piece, coord) < Math.pow(boardSize / 32.0, 2))
                .min(Comparator.comparing(piece -> localDistance(piece, coord)));
    }

    public static int boardPxSize(Board board) {
        return (board.getBoardSize() * board.getBaseTileSize());
    }
}
