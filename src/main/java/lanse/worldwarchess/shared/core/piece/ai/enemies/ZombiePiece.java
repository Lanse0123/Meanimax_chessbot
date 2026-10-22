package lanse.worldwarchess.shared.core.piece.ai.enemies;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;

public class ZombiePiece extends NpcPiece {

    private final boolean isMinion;
    private final Piece boss;

    public ZombiePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
        this.isMinion = false;
        this.boss = null;
    }

    public ZombiePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize, Piece boss) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
        this.isMinion = true;
        this.boss = boss;
    }

    @Override
    public void move() {
        NPC.zombieMoveLogic(this, 70);
    }

    @Override
    public char getPrefixReplacement(){return 'z';}

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(4, 4, 4, 1);

        return new ZombiePiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    public Piece getBoss(){ return boss; }

    public static Color getColorOverride(){return new Color(30, 70, 30); }

    //TODO - add logic for infecting pieces:
}
