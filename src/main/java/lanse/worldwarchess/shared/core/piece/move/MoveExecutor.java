package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.cascading.CascadePacketBundler;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.piece.*;
import lanse.worldwarchess.shared.core.piece.ai.bosses.NpcBoss;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.NpcCommanderPiece;
import lanse.worldwarchess.shared.core.piece.fairies.FairyPiece;
import lanse.worldwarchess.shared.core.piece.marker.MageMarker;
import lanse.worldwarchess.shared.core.piece.marker.MoveMarker;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.Objects;
import java.util.Optional;

public class MoveExecutor {

    //This class is responsible for moving a piece to a new spot, checking if it is legal, and doing logic for
    //when the piece makes it there.

    public static void executeMove(Piece executorPiece, Board newBoard, int newX, int newY, int originalX, int originalY,
                                   boolean shouldSend, String moveType, boolean isOurMove, CascadePacketBundler bundler, boolean isFromDecoder) {

        boolean playNormalSound = true;

        Piece targetPiece = newBoard.getPieceAt(newX, newY, Optional.of(executorPiece), WorldMap.getAllPiecesOfBoard(newBoard));

        Piece oldPiece = new Piece(executorPiece.getPlayerID(), executorPiece.getBoard(), String.valueOf(executorPiece.getType()), executorPiece.getTeam(),
        executorPiece.getBoardX(), executorPiece.getBoardY(), executorPiece.getPosX(), executorPiece.getPosY(), executorPiece.getPieceSize());

//        new MoveMarker(oldPiece.getBoard(), oldPiece.getPosX(), oldPiece.getPosY(), oldPiece.getPieceSize());

        // Handle captures
        if (targetPiece != null && !targetPiece.getTeam().equals(executorPiece.getTeam())) {
            if (targetPiece instanceof CommanderPiece || targetPiece instanceof NpcBoss) {
                if (executorPiece instanceof CommanderPiece) {
                    WorldMap.killScaledBlock(newBoard, targetPiece.getPosX(), targetPiece.getPosY(), executorPiece);
                } else {
                    AttackOnTitans.attacksOnTitans.add(new AttackOnTitans.TitanAttack(executorPiece, targetPiece));
                }
            } else {
                if (executorPiece instanceof CommanderPiece) {
                    WorldMap.killScaledBlock(newBoard, newX, newY, executorPiece);
                } else {
                    SoundManager.capturePiece(executorPiece);
                    playNormalSound = false;
                    WorldMap.killPiece(targetPiece, true, false); //TODO: should it send packets sometimes?
                }
            }
        }

        // Move the piece
        executorPiece.setMapPosition(newBoard.getBoardX(), newBoard.getBoardY());
        executorPiece.setBoardPosition(newX, newY);
        executorPiece.setBoard(newBoard);

        // En passant
        //TODO - give en passant it's own sound
        if (executorPiece.getType() == PieceType.PAWN) {

            if (Math.abs(newX - originalX) == 1 && Math.abs(newY - originalY) == 1 &&
                    newBoard.getPieceAt(newX, newY, Optional.of(executorPiece), WorldMap.getAllPiecesOfBoard(newBoard)) == null) {

                int capturedPawnY = executorPiece.getTeam().equalsIgnoreCase("White") ? newY + 1 : newY - 1;

                Piece capturedPawn = newBoard.getPieceAt(newX, capturedPawnY, Optional.of(executorPiece), WorldMap.getAllPiecesOfBoard(newBoard));

                if (capturedPawn != null && capturedPawn.getType() == PieceType.PAWN &&
                        !capturedPawn.getTeam().equals(executorPiece.getTeam())) {

                    WorldMap.killPiece(capturedPawn, true, false); //TODO: should it send packets sometimes?
                }
            }
        }

        // Castlin
        int endzone = (executorPiece instanceof CommanderPiece) ? 14 : 7;

        int stepCheck = (executorPiece instanceof CommanderPiece) ? 4 : 2;
        if (executorPiece.getType() == PieceType.KING && Math.abs(newX - originalX) == stepCheck) {
            if (stepCheck == 4) {
                SoundManager.commanderMove(executorPiece);
            } else {
                SoundManager.castle();
            }
            playNormalSound = false;
            if (newX > originalX) {
                Piece rook = newBoard.getPieceAt(endzone, originalY, Optional.of(executorPiece), WorldMap.getAllPiecesOfBoard(newBoard));
                if (rook != null && rook.getType() == PieceType.ROOK) {
                    rook.setBoardPosition(newX - 1, originalY);
                }
            } else {
                Piece rook = newBoard.getPieceAt(0, originalY, Optional.of(executorPiece), WorldMap.getAllPiecesOfBoard(newBoard));
                if (rook != null && rook.getType() == PieceType.ROOK) {
                    rook.setBoardPosition(newX + 1, originalY);
                }
            }
        }

        Timer.playerIDMovedList.add(executorPiece.getPlayerID());

        if (executorPiece instanceof NpcPiece || executorPiece instanceof NpcCommanderPiece){
            executorPiece.hasMoved = true;
        } else {
            for (Piece movedPiece : WorldMap.getAllPiecesOfPlayerID(executorPiece.getPlayerID())) movedPiece.hasMoved = true;
        }

        if (playNormalSound && !(executorPiece instanceof NpcPiece) && !(executorPiece instanceof NpcCommanderPiece)) {
            if (executorPiece instanceof CommanderPiece) {
                SoundManager.commanderMove(executorPiece);
            } else {
                SoundManager.validMove(executorPiece);
            }
        }

        MoveHistory.addMoveForRecord(oldPiece, executorPiece, shouldSend, moveType, -40404, bundler);

        // Promotion
        if (executorPiece.getType() == PieceType.PAWN || executorPiece.getType() == PieceType.CHECKER) {
            if ((executorPiece.getTeam().equalsIgnoreCase("White") && newY == 0) ||
                    (executorPiece.getTeam().equalsIgnoreCase("Black") && newY == endzone)) {

                if (executorPiece instanceof NpcPiece || executorPiece instanceof NpcCommanderPiece || executorPiece instanceof FairyPiece){
                    if (GameMode.isSinglePlayer || Main.userIsHost) {
                        Piece new_piece = executorPiece.promoteOverride(executorPiece);
                        WorldMap.erasePiece(executorPiece, false);
                        WorldMap.spawnPiece(new_piece, false);
                        MoveHistory.addPromotionForRecord(new_piece, !isFromDecoder, bundler);
                    }
                } else {
                    PawnPromotionManager.getInstance().addPromotion(executorPiece, newBoard, newX, newY, isOurMove);
                }
            }
        }

        // skip the turn if every piece on the team has moved.
        if ((GameMode.isSinglePlayer || Main.userIsHost) &&
                !(executorPiece instanceof NpcPiece || executorPiece instanceof NpcCommanderPiece)){
            String currentTeam = executorPiece.getTeam();
            for (Piece piece1 : WorldMap.pieces){
                if (!piece1.hasMoved && Objects.equals(piece1.getTeam(), currentTeam)){
                    return;
                }
            }

            Timer.currentTime = 0;
        }
    }
}
