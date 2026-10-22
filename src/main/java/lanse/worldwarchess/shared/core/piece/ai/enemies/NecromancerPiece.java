package lanse.worldwarchess.shared.core.piece.ai.enemies;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;


public class NecromancerPiece extends NpcPiece{

    public NecromancerPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    @Override
    public void move() {
        if (type != PieceType.PAWN && type != PieceType.KING) {
            WorldMap.minionPieceSpawnRequests.add(new ZombiePiece(this.playerID, this.board, String.valueOf(this.type), this.team,
                    this.boardX, this.boardY, this.posX, this.posY, this.pieceSize, this));
        }

        NPC.necromancerMoveLogic(this);
    }

    @Override
    public char getPrefixReplacement(){return 'n';}

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(5, 5, 5, 1);

        return new NecromancerPiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    @Override
    public void killPiece(Piece piece, boolean shouldSend) {
        WorldMap.killPiece(this, false, shouldSend);

        //kill all the necromancer's minions
        for (Piece piece1 : WorldMap.pieces){
            if (piece1 instanceof ZombiePiece){
                if (((ZombiePiece) piece1).getBoss() == this){
                    if (!shouldSend) {
                        System.err.println("shouldSend is false, but we called erasePiece with it as true earlier");
                    }
                    WorldMap.erasePiece(piece1, true);
                }
            }
        }
    }

    public static Color getColorOverride(){return new Color(100, 0, 100); }
}