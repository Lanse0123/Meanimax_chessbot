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
public final class MoveContext {
    private final Board board;
    private final List<Piece> pieces;
    private final Piece[][] occupancy;
    private long positionHash;

    public Board board() {
        return board;
    }

    public List<Piece> pieces() {
        return pieces;
    }

    public Piece[][] occupancy() {
        return occupancy;
    }

    public long positionHash() {
        return positionHash;
    }

    public MoveContext(Board board, List<Piece> pieces) {
        this.board = board;
        this.pieces = new ArrayList<>(pieces);
        this.occupancy = new Piece[board.getBoardSize()][board.getBoardSize()];

        for (Piece piece : this.pieces) {
            if (piece.getBoard() == board) {
                int x = piece.getPosX();
                int y = piece.getPosY();

                occupancy[x][y] = piece;
                positionHash ^= TranspositionTable.hashPiece(piece, x, y);
            }
        }
    }

    public static MoveContext contextify(Board board) {
        return new MoveContext(board, new ArrayList<>(WorldMap.getAllPiecesOfBoard(board)));
    }

    public Piece getPieceAt(int x, int y, Piece ignoredPiece) {
        Piece piece = occupancy[x][y];
        return piece == ignoredPiece ? null : piece;
    }

    public void movePiece(Piece piece, int newX, int newY) {
        int oldX = piece.getPosX();
        int oldY = piece.getPosY();

        if (occupancy[oldX][oldY] == piece) {
            occupancy[oldX][oldY] = null;
        }

        piece.setBoardPosition(newX, newY);

        occupancy[newX][newY] = piece;
    }

    public void removePiece(Piece piece) {
        int x = piece.getPosX();
        int y = piece.getPosY();

        if (occupancy[x][y] == piece) {
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

        if (piece.getBoard() == board) {
            occupancy[x][y] = piece;
        }
    }

    public record TemporaryMove(Piece movingPiece, int oldX, int oldY, boolean oldHasMovedEver,
                                Piece capturedPiece, int capturedX, int capturedY, int capturedPieceIndex, Piece castlingRook,
                                int rookOldX, int rookOldY, boolean rookOldHasMovedEver) {}

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
        boolean rookOldHasMovedEver = false;

        // Castling made this so much more verbose
        if (piece.getType() == PieceType.KING && oldY == newY && Math.abs(newX - oldX) == 2) {
            rookOldX = newX > oldX ? board.getBoardSize() - 1 : 0;
            rookOldY = oldY;

            rook = getPieceAt(rookOldX, rookOldY, piece);

            if (rook != null && rook.getType() == PieceType.ROOK) {
                rookOldHasMovedEver = rook.hasMovedEver;
            } else {
                rook = null;
            }
        }

        TemporaryMove undo = new TemporaryMove(piece, oldX, oldY, piece.hasMovedEver, captured,
                capturedX, capturedY, capturedIndex, rook, rookOldX, rookOldY, rookOldHasMovedEver);

        //Transposition tables make this so much harder to read...
        positionHash ^= TranspositionTable.hashPiece(piece, oldX, oldY);

        if (captured != null) {
            positionHash ^= TranspositionTable.hashPiece(captured, capturedX, capturedY);
            removePiece(captured);
        }

        if (rook != null) {
            positionHash ^= TranspositionTable.hashPiece(rook, rookOldX, rookOldY);
        }

        movePiece(piece, newX, newY);
        piece.hasMovedEver = true;

        if (rook != null) {
            int rookNewX = newX > oldX ? newX - 1 : newX + 1;
            movePiece(rook, rookNewX, oldY);
            rook.hasMovedEver = true;
            positionHash ^= TranspositionTable.hashPiece(rook, rookNewX, oldY);
        }

        positionHash ^= TranspositionTable.hashPiece(piece, newX, newY);

        return undo;
    }

    public void undoMove(TemporaryMove undo) {
        Piece piece = undo.movingPiece();

        int currentX = piece.getPosX();
        int currentY = piece.getPosY();

        // Remove the current contributions before restoring the old state because it makes transposition table faster
        positionHash ^= TranspositionTable.hashPiece(piece, currentX, currentY);

        Piece rook = undo.castlingRook();
        if (rook != null) {
            positionHash ^= TranspositionTable.hashPiece(rook, rook.getPosX(), rook.getPosY());
        }

        movePiece(piece, undo.oldX(), undo.oldY());
        piece.hasMovedEver = undo.oldHasMovedEver();

        if (rook != null) {
            movePiece(rook, undo.rookOldX(), undo.rookOldY());
            rook.hasMovedEver = undo.rookOldHasMovedEver();
        }

        Piece captured = undo.capturedPiece();
        if (captured != null) {
            pieces.add(undo.capturedPieceIndex(), captured);
            occupancy[undo.capturedX()][undo.capturedY()] = captured;
        }

        positionHash ^= TranspositionTable.hashPiece(piece, undo.oldX(), undo.oldY());

        if (rook != null) {
            positionHash ^= TranspositionTable.hashPiece(rook, undo.rookOldX(), undo.rookOldY());
        }

        if (captured != null) {
            positionHash ^= TranspositionTable.hashPiece(captured, undo.capturedX(), undo.capturedY());
        }
    }
}