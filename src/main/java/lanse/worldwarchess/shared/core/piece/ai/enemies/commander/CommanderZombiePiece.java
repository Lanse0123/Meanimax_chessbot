package lanse.worldwarchess.shared.core.piece.ai.enemies.commander;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class CommanderZombiePiece extends NpcCommanderPiece {

    public CommanderZombiePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    @Override
    public void move() { NPC.zombieMoveLogic(this, 70); }

    @Override
    public char getPrefixReplacement(){return 'z';}

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(4, 4, 4, 0);

        return new CommanderZombiePiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }
}
