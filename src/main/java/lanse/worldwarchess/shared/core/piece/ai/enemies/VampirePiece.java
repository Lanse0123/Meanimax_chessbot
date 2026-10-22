package lanse.worldwarchess.shared.core.piece.ai.enemies;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;

public class VampirePiece extends NpcPiece {

    public VampirePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    @Override
    public void move() {
        NPC.vampireMoveLogic(this);
    }

    @Override
    public char getPrefixReplacement(){return 'v';}
    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(1, 3, 3, 2);

        return new VampirePiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    public static Color getColorOverride(){return new Color(100, 20, 20); }

    //TODO - add logic for infecting pieces
}
