package lanse.worldwarchess.shared.core.piece.ai.enemies.commander;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.enemies.ZombiePiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class CommanderNecromancerPiece extends NpcCommanderPiece{

    public CommanderNecromancerPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
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
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(5, 5, 5, 1);

        return new CommanderNecromancerPiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    @Override
    public char getPrefixReplacement(){return 'n';}

    @Override
    public void killPiece(Piece piece, boolean shouldSend) {
        WorldMap.killPiece(this, false, shouldSend);

        //kill all the necromancer's minions
        for (Piece piece1 : WorldMap.pieces){
            if (piece1 instanceof ZombiePiece){
                if (((ZombiePiece) piece1).getBoss() == this){
                    WorldMap.erasePiece(piece1, true);
                }
            }
        }
    }
}
