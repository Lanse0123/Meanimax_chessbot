package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.PawnPromotionManager;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.marker.Marker;
import lanse.worldwarchess.shared.core.piece.marker.MoveMarker;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class MoveExecutor {

    //This class is responsible for moving a piece to a new spot, checking if it is legal, and doing logic for
    //when the piece makes it there.

    public static void executeMove(Piece executorPiece, int newX, int newY, int originalX, int originalY,
                                   boolean shouldSend, String moveType, boolean isOurMove) {

        boolean playNormalSound = true;

        Board board = WorldMap.board;

        Marker.markers.add(new MoveMarker(board, newX, newY, 1));
        Marker.markers.add(new MoveMarker(board, originalX, originalY, 1));

        Piece targetPiece = board.getPieceAt(newX, newY, executorPiece, WorldMap.getMainPieces());

        Piece oldPiece = new Piece(executorPiece.getPlayerID(), board, executorPiece.getType(), executorPiece.getTeam(),
                executorPiece.getPosX(), executorPiece.getPosY());

        // Handle captures
        if (targetPiece != null && !targetPiece.getTeam() == executorPiece.getTeam()) {
            SoundManager.capturePiece(executorPiece);
            playNormalSound = false;
            WorldMap.killPiece(targetPiece);

        }

        // Move the piece
        executorPiece.setBoardPosition(newX, newY);

        // En passant
        //TODO - give en passant it's own sound and fix en passant
        if (executorPiece.getType() == PieceType.PAWN) {

            if (Math.abs(newX - originalX) == 1 && Math.abs(newY - originalY) == 1 &&
                    board.getPieceAt(newX, newY, executorPiece, WorldMap.getMainPieces()) == null) {

                int capturedPawnY = executorPiece.getTeam() ? newY + 1 : newY - 1;

                Piece capturedPawn = board.getPieceAt(newX, capturedPawnY, executorPiece, WorldMap.getMainPieces());

                if (capturedPawn != null && capturedPawn.getType() == PieceType.PAWN && capturedPawn.getTeam() != executorPiece.getTeam()) {
                    WorldMap.killPiece(capturedPawn);
                }
            }
        }

        // Castlin
        int endzone = 7;

        int stepCheck = 2;
        if (executorPiece.getType() == PieceType.KING && Math.abs(newX - originalX) == stepCheck) {
            SoundManager.castle();
            playNormalSound = false;
            if (newX > originalX) {
                Piece rook = board.getPieceAt(endzone, originalY, executorPiece, WorldMap.getMainPieces());
                if (rook != null && rook.getType() == PieceType.ROOK) {
                    rook.setBoardPosition(newX - 1, originalY);
                }
            } else {
                Piece rook = board.getPieceAt(0, originalY, executorPiece, WorldMap.getMainPieces());
                if (rook != null && rook.getType() == PieceType.ROOK) {
                    rook.setBoardPosition(newX + 1, originalY);
                }
            }
        }

        Timer.playerIDMovedList.add(executorPiece.getPlayerID());

        if (playNormalSound) {
            SoundManager.validMove(executorPiece);
        }

        MoveHistory.addMoveForRecord(oldPiece, executorPiece, shouldSend, moveType, -40404);

        // Promotion
        if (executorPiece.getType() == PieceType.PAWN) {
            if ((executorPiece.getTeam() && newY == 0) || (!executorPiece.getTeam() && newY == endzone)) {
                PawnPromotionManager.getInstance().addPromotion(executorPiece, board, newX, newY, isOurMove);
            }
        }

        Timer.currentTime = 0;
    }
}
