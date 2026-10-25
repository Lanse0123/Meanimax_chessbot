package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
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

    public record TemporaryMove(Piece movingPiece, int oldX, int oldY, boolean oldHasMoved, boolean oldHasMovedEver,
                                Piece capturedPiece, int capturedX, int capturedY, int capturedPieceIndex, Piece castlingRook,
                                int rookOldX, int rookOldY, boolean rookOldHasMoved, boolean rookOldHasMovedEver) {}

    public TemporaryMove makeMove(Piece piece, int newX, int newY) {
        int oldX = piece.getPosX();
        int oldY = piece.getPosY();

        Piece captured = getPieceAt(newX, newY, piece);
        int capturedX = captured == null ? -1 : captured.getPosX();
        int capturedY = captured == null ? -1 : captured.getPosY();
        int capturedIndex = captured == null ? -1 : pieces.indexOf(captured);

        Piece rook = null;
        int rookOldX = -1;
        int rookOldY = -1;
        boolean rookOldHasMoved = false;
        boolean rookOldHasMovedEver = false;

        // Castling made this so much more verbose
        if (piece.getType() == PieceType.KING && oldY == newY && Math.abs(newX - oldX) == 2) {
            rookOldX = newX > oldX ? board.getBoardSize() - 1 : 0;
            rookOldY = oldY;

            rook = getPieceAt(rookOldX, rookOldY, piece);

            if (rook != null && rook.getType() == PieceType.ROOK) {
                rookOldHasMoved = rook.hasMoved;
                rookOldHasMovedEver = rook.hasMovedEver;
            } else {
                rook = null;
            }
        }

        TemporaryMove undo = new TemporaryMove(piece, oldX, oldY, piece.hasMoved, piece.hasMovedEver, captured, capturedX, capturedY, capturedIndex,
                rook, rookOldX, rookOldY, rookOldHasMoved, rookOldHasMovedEver);

        if (captured != null) {
            removePiece(captured);
        }

        movePiece(piece, newX, newY);
        piece.hasMoved = true;
        piece.hasMovedEver = true;

        if (rook != null) {
            int rookNewX = newX > oldX ? newX - 1 : newX + 1;
            movePiece(rook, rookNewX, oldY);
            rook.hasMoved = true;
            rook.hasMovedEver = true;
        }

        return undo;
    }

    public void undoMove(TemporaryMove undo) {
        Piece piece = undo.movingPiece();

        movePiece(piece, undo.oldX(), undo.oldY());
        piece.hasMoved = undo.oldHasMoved();
        piece.hasMovedEver = undo.oldHasMovedEver();

        Piece rook = undo.castlingRook();
        if (rook != null) {
            movePiece(rook, undo.rookOldX(), undo.rookOldY());
            rook.hasMoved = undo.rookOldHasMoved();
            rook.hasMovedEver = undo.rookOldHasMovedEver();
        }

        Piece captured = undo.capturedPiece();
        if (captured != null) {
            pieces.add(undo.capturedPieceIndex(), captured);
            occupancy[undo.capturedX()][undo.capturedY()] = captured;
        }
    }
}