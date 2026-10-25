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

    public static void executeMove(Piece executorPiece, Board newBoard, int newX, int newY, int originalX, int originalY,
                                   boolean shouldSend, String moveType, boolean isOurMove, boolean isFromDecoder) {

        boolean playNormalSound = true;

        Marker.markers.add(new MoveMarker(newBoard, newX, newY, 1));
        Marker.markers.add(new MoveMarker(newBoard, originalX, originalY, 1));

        Piece targetPiece = newBoard.getPieceAt(newX, newY, executorPiece, WorldMap.getAllPiecesOfBoard(newBoard));

        Piece oldPiece = new Piece(executorPiece.getPlayerID(), executorPiece.getBoard(), executorPiece.getType(), executorPiece.getTeam(),
                executorPiece.getBoardX(), executorPiece.getBoardY(), executorPiece.getPosX(), executorPiece.getPosY());

        // Handle captures
        if (targetPiece != null && !targetPiece.getTeam() == executorPiece.getTeam()) {
            SoundManager.capturePiece(executorPiece);
            playNormalSound = false;
            WorldMap.killPiece(targetPiece);

        }

        // Move the piece
        executorPiece.setMapPosition(newBoard.getBoardX(), newBoard.getBoardY());
        executorPiece.setBoardPosition(newX, newY);
        executorPiece.setBoard(newBoard);

        // En passant
        //TODO - give en passant it's own sound
        if (executorPiece.getType() == PieceType.PAWN) {

            if (Math.abs(newX - originalX) == 1 && Math.abs(newY - originalY) == 1 &&
                    newBoard.getPieceAt(newX, newY, executorPiece, WorldMap.getAllPiecesOfBoard(newBoard)) == null) {

                int capturedPawnY = executorPiece.getTeam() ? newY + 1 : newY - 1;

                Piece capturedPawn = newBoard.getPieceAt(newX, capturedPawnY, executorPiece, WorldMap.getAllPiecesOfBoard(newBoard));

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
                Piece rook = newBoard.getPieceAt(endzone, originalY, executorPiece, WorldMap.getAllPiecesOfBoard(newBoard));
                if (rook != null && rook.getType() == PieceType.ROOK) {
                    rook.setBoardPosition(newX - 1, originalY);
                }
            } else {
                Piece rook = newBoard.getPieceAt(0, originalY, executorPiece, WorldMap.getAllPiecesOfBoard(newBoard));
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
                PawnPromotionManager.getInstance().addPromotion(executorPiece, newBoard, newX, newY, isOurMove);
            }
        }

        Timer.currentTime = 0;
    }
}
