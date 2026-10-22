package lanse.worldwarchess.shared.core.piece.ai.enemies.commander;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class CommanderVampirePiece extends NpcCommanderPiece {

    public CommanderVampirePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    @Override
    public void move() { NPC.vampireMoveLogic(this); }

    @Override
    public char getPrefixReplacement(){return 'v';}

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(4, 4, 4, 1);

        return new CommanderVampirePiece(piece.getPlayerID(), piece.getBoard(), pieceType,
                piece.getTeam(), piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }
}
