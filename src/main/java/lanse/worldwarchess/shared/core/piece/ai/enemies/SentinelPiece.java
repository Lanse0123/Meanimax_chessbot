package lanse.worldwarchess.shared.core.piece.ai.enemies;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.NeedsSafetyCheck;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;

public class SentinelPiece extends NpcPiece implements NeedsSafetyCheck {

    public SentinelPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    @Override
    public void move(){ NPC.sentinelMoveLogic(this); }

    @Override
    public char getPrefixReplacement(){return 'c';}

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(4, 4, 4, 0);

        return new SentinelPiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    public static Color getColorOverride(){return new Color(50, 50, 200); }
}
