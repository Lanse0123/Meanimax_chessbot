package lanse.worldwarchess.shared.core.piece.ai.enemies;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;

/** REMEMBER TO ADD TO THE PieceClassPrefixMap.java WHEN ADDING A NEW PIECE CLASS*/
public abstract class NpcPiece extends Piece {

    protected int barPoints;

    public NpcPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
        barPoints = 1;
    }

    //this is redundant but nice to have anyway for simplicity. All pieces extending NpcPiece should probably
    // also override this function if they want to move.
    public void move(){}
    public Piece promoteOverride(Piece piece) {return null;}
    public char getPrefixReplacement(){return 0;}
    public void killPiece(Piece piece, boolean shouldSend){
        WorldMap.killPiece(piece, false, shouldSend);
    }
    public static Color getColorOverride() { return Color.BLACK; }

    @Override
    public int getBarPoints() { return this.barPoints; }
}
