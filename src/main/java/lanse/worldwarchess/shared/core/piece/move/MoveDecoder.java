package lanse.worldwarchess.shared.core.piece.move;

import com.codedisaster.steamworks.SteamID;
import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceClassPrefixMap;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.ParallelUniverseHandler;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MoveDecoder {

    //This class converts packets into an order for what your game should do, whether it be move a piece, or spawn
    // a new piece somewhere else, or etc.

    //TODO - make a way to undo a move in the case of a conflict

    public static Optional<CascadePacketManager.Packet> decodeMove(CascadePacketManager.Packet pkt, SteamID senderID) {
        String[] content = pkt.content().split(",");
        String moveType = content[0];
        int oldPieceBoardID = Integer.parseInt(content[1]);
        int oldPiecePosX = Integer.parseInt(content[2]);
        int oldPiecePosY = Integer.parseInt(content[3]);
        int newPieceBoardID = Integer.parseInt(content[4]);
        int newPiecePosX = Integer.parseInt(content[5]);
        int newPiecePosY = Integer.parseInt(content[6]);

        int bossHealth = -40404;
        if (moveType.equals("push")) bossHealth = Integer.parseInt(content[7]);

        if (decodeMove(oldPieceBoardID, oldPiecePosX, oldPiecePosY, newPieceBoardID, newPiecePosX, newPiecePosY, moveType, bossHealth, pkt, senderID)) {
            return Optional.of(pkt);
        }
        else {
            return Optional.empty();
        }
    }

    public static boolean decodeMove(int oldPieceBoardID, int oldPiecePosX, int oldPiecePosY, int newPieceBoardID,
                                  int newPiecePosX, int newPiecePosY, String moveType, int bossHealth,
                                     CascadePacketManager.Packet pkt, SteamID senderID){

        Board oldBoard = WorldMap.getBoardByID(oldPieceBoardID);
        Board newBoard = oldBoard;

        if (oldPieceBoardID != newPieceBoardID){
            newBoard = WorldMap.getBoardByID(newPieceBoardID);
        }

        Piece target;
        if (Objects.equals(moveType, "move")) {
            target = oldBoard.getPieceAt(oldPiecePosX, oldPiecePosY, Optional.empty(), WorldMap.getAllPiecesOfBoard(oldBoard));
        } else if (Objects.equals(moveType, "push")) {
            target = oldBoard.getPieceAt(oldPiecePosX, oldPiecePosY, Optional.empty(), WorldMap.getRegularPiecesOnBoard(oldBoard));
            Piece boss = oldBoard.getPieceAt(oldPiecePosX, oldPiecePosY, Optional.empty(), WorldMap.getCommanderPiecesOnBoard(oldBoard));
            boss.setHealth(bossHealth);
        }
        else {
            System.err.println("Unknown move type " + moveType);
            return false;
        }

        if (target == null) {
            ParallelUniverseHandler.handleParallelUniverse(oldPieceBoardID, senderID);
            System.err.println("Invalid move: no piece was at " + oldPiecePosX + "," + oldPiecePosY + " on board ID " + oldPieceBoardID);
            return false;
        }

        if (Objects.equals(moveType, "move")) {
            List<LegalMoveDetector.Coordinate> coordinates = LegalMoveDetector.getLegalMoves(target);

            for (LegalMoveDetector.Coordinate coordinate : coordinates) {
                if (coordinate.boardID() == newPieceBoardID &&
                        coordinate.x() == newPiecePosX &&
                        coordinate.y() == newPiecePosY) {
                    MoveExecutor.executeMove(target, newBoard, newPiecePosX, newPiecePosY, oldPiecePosX, oldPiecePosY,
                            false, moveType, false, null, true);
                    return true;
                }
            }

            ParallelUniverseHandler.handleParallelUniverse(newPieceBoardID, oldPieceBoardID, senderID);
            System.err.println("Invalid move: illegal to move from " + oldPiecePosX + "," + oldPiecePosY + " on board ID " + oldPieceBoardID +
                    " to " + newPiecePosX + "," + newPiecePosY + " on board ID " + newPieceBoardID);

            return false;
        } else if (Objects.equals(moveType, "push")) {
            MoveExecutor.executeMove(target, newBoard, newPiecePosX, newPiecePosY, oldPiecePosX, oldPiecePosY,
                    false, moveType, false, null, true);
            return true;
        }
        else {
            System.err.println("Unknown move type '" + moveType + "', not in ['move', 'push']!");
            return false;
        }
    }

    public static Optional<CascadePacketManager.Packet> decodePromotion(CascadePacketManager.Packet pkt) {
        String[] content = pkt.content().split(",");
        int newPieceBoardID = Integer.parseInt(content[1]);
        int newPiecePosX = Integer.parseInt(content[2]);
        int newPiecePoxY = Integer.parseInt(content[3]);
        String type = content[4];

        if (decodePromotion(newPieceBoardID, newPiecePosX, newPiecePoxY, type)) {
            return Optional.of(pkt);
        }
        else {
            return Optional.empty();
        }
    }

    public static boolean decodePromotion(int newPieceBoardID, int newPiecePosX, int newPiecePosY, String type){
        Board board = WorldMap.getBoardByID(newPieceBoardID);

        Piece target = board.getPieceAt(newPiecePosX, newPiecePosY, Optional.empty(), WorldMap.getAllPiecesOfBoard(board));

        if (target != null && target.getType() == PieceType.PAWN) {
            int playerID = target.getPlayerID();
            int boardX = target.getBoardX();
            int boardY = target.getBoardY();
            String team = target.getTeam();
            int size = target.getPieceSize();
            Piece newPiece;

            try {
                Class<?> clazz = target.getClass();
                newPiece = (Piece) clazz.getConstructor(
                        int.class, Board.class, String.class, String.class, int.class, int.class, int.class, int.class, int.class
                ).newInstance(playerID, board, type, team, boardX, boardY, newPiecePosX, newPiecePosY, size);

                WorldMap.erasePiece(target, false);
                WorldMap.spawnPiece(newPiece, false);
                MoveHistory.addPromotionForRecord(newPiece, false, null);
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            System.err.println("Error promoting:");
            System.err.println("MSG: boardID=" + newPieceBoardID + ",X=" + newPiecePosX + ",Y=" + newPiecePosY);
            System.err.println("MSG: target=" + target);
            if (target != null) {
                System.err.println("MSG: type=" + target.getType());
                System.err.println("MSG: boardID=" + target.getBoard() + ",X=" + target.getPosX() + ",Y=" + target.getPosY());
            }
            return false;
        }
    }

    public static Optional<CascadePacketManager.Packet> decodeSpawn(CascadePacketManager.Packet pkt) {
        String[] content = pkt.content().split(",");
        int newPieceBoardID = Integer.parseInt(content[1]);
        int newPiecePosX = Integer.parseInt(content[2]);
        int newPiecePosY = Integer.parseInt(content[3]);
        String newPieceType = content[4];
        String newPieceTeam = content[5];
        int newPieceSize = Integer.parseInt(content[6]);
        int newPlayerID = Integer.parseInt(content[7]);
        int pieceClassIndex = Integer.parseInt(content[8]);

        Board newBoard = WorldMap.getBoardByID(newPieceBoardID);

        Piece newPiece = null;

        try {
            Class<?> clazz = PieceClassPrefixMap.getPieceClass(pieceClassIndex);
            newPiece = (Piece) clazz.getConstructor(
                    int.class, Board.class, String.class, String.class, int.class, int.class, int.class, int.class, int.class
            ).newInstance(
                    newPlayerID, newBoard, newPieceType, newPieceTeam, newBoard.getBoardX(),
                    newBoard.getBoardY(), newPiecePosX, newPiecePosY, newPieceSize);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        WorldMap.pieces.add(newPiece);
        return Optional.of(pkt);
    }

    public static Optional<CascadePacketManager.Packet> decodeKill(CascadePacketManager.Packet pkt) {
        String[] content = pkt.content().split(",");

        int boardID = Integer.parseInt(content[0]);
        int posX = Integer.parseInt(content[1]);
        int posY = Integer.parseInt(content[2]);
        String context = content[3];

        Board board = WorldMap.getBoardByID(boardID);

        Piece piece;
        if (context.equals("cmdr")) {
            piece = board.getPieceAt(posX, posY, Optional.empty(), WorldMap.getCommanderPiecesOnBoard(board));
        }
        else {
            piece = board.getPieceAt(posX, posY, Optional.empty(), WorldMap.getAllPiecesOfBoard(board));
        }
        if (piece == null) {
            return Optional.empty();
        }
        WorldMap.killPiece(piece, false, false);
        return Optional.of(pkt);
    }

    public static Optional<CascadePacketManager.Packet> decodeErase(CascadePacketManager.Packet pkt, boolean eraseWholeID) {
        String[] content = pkt.content().split(",");

        int newPieceBoardID = Integer.parseInt(content[1]);
        int newPiecePosX = Integer.parseInt(content[2]);
        int newPiecePoxY = Integer.parseInt(content[3]);

        Board board = WorldMap.getBoardByID(newPieceBoardID);
        Piece thanosSnappedPiece = board.getPieceAt(newPiecePosX, newPiecePoxY, Optional.empty(), WorldMap.getAllPiecesOfBoard(board));
        if (thanosSnappedPiece == null) {
            return Optional.empty();
        }
        WorldMap.erasePiece(thanosSnappedPiece, false);

        if (eraseWholeID){
            int ID = thanosSnappedPiece.getPlayerID();

            for (Piece piece : WorldMap.pieces){
                if (piece.getPlayerID() == ID){
                    WorldMap.erasePiece(piece, false);
                }
            }
        }
        return Optional.of(pkt);
    }
}
