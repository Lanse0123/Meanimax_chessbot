
package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public final class SimulatedMoveExecutor {

    private SimulatedMoveExecutor() {}

    public static void executeMove(MoveContext context, Piece piece, int newX, int newY) {
        if (context == null || piece == null || !context.pieces().contains(piece)) {
            throw new IllegalArgumentException("Piece must belong to the MoveContext");
        }

        Board board = context.board();
        int originalX = piece.getPosX();
        int originalY = piece.getPosY();

        Piece target = context.getPieceAt(newX, newY, piece);

        // Capture
        if (target != null) {
            if (target.getTeam().equals(piece.getTeam())) {
                throw new IllegalArgumentException("Cannot capture a friendly piece");
            }
            context.pieces().remove(target);
        }

        // En passant
        if (piece.getType() == PieceType.PAWN && Math.abs(newX - originalX) == 1 && Math.abs(newY - originalY) == 1 && target == null) {
            int capturedPawnY = piece.getTeam().equalsIgnoreCase("White") ? newY + 1 : newY - 1;

            Piece capturedPawn = context.getPieceAt(newX, capturedPawnY, piece);

            if (capturedPawn != null && capturedPawn.getType() == PieceType.PAWN && !capturedPawn.getTeam().equals(piece.getTeam())) {
                context.pieces().remove(capturedPawn);
            }
        }

        piece.setMapPosition(board.getBoardX(), board.getBoardY());
        piece.setBoardPosition(newX, newY);
        piece.setBoard(board);
        piece.hasMoved = true;
        piece.hasMovedEver = true;

        // Castling
        if (piece.getType() == PieceType.KING && Math.abs(newX - originalX) == 2) {

            int rookX = newX > originalX ? 7 : 0;
            int rookDestinationX = newX > originalX ? newX - 1 : newX + 1;

            Piece rook = context.getPieceAt(rookX, originalY, piece);

            if (rook != null && rook.getType() == PieceType.ROOK && rook.getTeam().equals(piece.getTeam())) {
                rook.setMapPosition(board.getBoardX(), board.getBoardY());
                rook.setBoardPosition(rookDestinationX, originalY);
                rook.setBoard(board);
                rook.hasMoved = true;
                rook.hasMovedEver = true;
            }
        }

        // Promotion to queen and only queen because lazy.
        if (piece.getType() == PieceType.PAWN && ((piece.getTeam().equalsIgnoreCase("White") && newY == 0)
                || (piece.getTeam().equalsIgnoreCase("Black") && newY == 7))) {

            Piece promotedPiece = piece.clonePiece(board);
            promotedPiece.setType(PieceType.QUEEN);
            promotedPiece.setBoardPosition(newX, newY);
            promotedPiece.hasMoved = true;
            promotedPiece.hasMovedEver = true;

            int index = context.pieces().indexOf(piece);
            context.pieces().set(index, promotedPiece);
        }
    }
}
