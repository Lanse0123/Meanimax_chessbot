package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.List;

/**
 * Written by Lanse
 * <br><br>
 * The rest of the game was not built for efficiency, but AI algorithms even with alpha beta pruning need to search
 * thousands or millions of positions, so this was made to make my life easier.
 **/
public record MoveContext(Board board, List<Piece> pieces, Piece[][] occupancy) {
    public MoveContext(Board board, List<Piece> pieces) {
        this(board, new ArrayList<>(pieces), new Piece[board.getBoardSize()][board.getBoardSize()]);

        for (Piece piece : this.pieces) {
            if (piece.getBoard() == board) {
                int x = piece.getPosX();
                int y = piece.getPosY();

                if (isInBounds(x, y)) {
                    occupancy[x][y] = piece;
                }
            }
        }
    }

    public static MoveContext contextify(Board board) {
        return new MoveContext(board, new ArrayList<>(WorldMap.getAllPiecesOfBoard(board)));
    }

    public Piece getPieceAt(int x, int y, Piece ignoredPiece) {
        if (!isInBounds(x, y)) return null;

        Piece piece = occupancy[x][y];
        return piece == ignoredPiece ? null : piece;
    }

    public void movePiece(Piece piece, int newX, int newY) {
        int oldX = piece.getPosX();
        int oldY = piece.getPosY();

        if (isInBounds(oldX, oldY) && occupancy[oldX][oldY] == piece) {
            occupancy[oldX][oldY] = null;
        }

        piece.setBoardPosition(newX, newY);

        if (isInBounds(newX, newY)) {
            occupancy[newX][newY] = piece;
        }
    }

    public void removePiece(Piece piece) {
        int x = piece.getPosX();
        int y = piece.getPosY();

        if (isInBounds(x, y) && occupancy[x][y] == piece) {
            occupancy[x][y] = null;
        }

        pieces.remove(piece);
    }

    public void addPiece(Piece piece) {
        if (!pieces.contains(piece)) {
            pieces.add(piece);
        }

        int x = piece.getPosX();
        int y = piece.getPosY();

        if (piece.getBoard() == board && isInBounds(x, y)) {
            occupancy[x][y] = piece;
        }
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < occupancy.length && y < occupancy.length;
    }
}