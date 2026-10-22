package lanse.worldwarchess.shared.core.piece.ai.enemies.commander;

import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

/** REMEMBER TO ADD TO THE PieceClassPrefixMap.java WHEN ADDING A NEW PIECE CLASS*/
public abstract class NpcCommanderPiece extends CommanderPiece {

    protected int barPoints;

    public NpcCommanderPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, 2);
        barPoints = 4;
    }

    //this is redundant but nice to have anyway for simplicity. All pieces extending NpcPiece should probably
    // also override this function if they want to move.
    public abstract void move();
    public abstract Piece promoteOverride(Piece piece);
    public char getPrefixReplacement(){return 0;}
    public void killPiece(Piece piece, boolean shouldSend) {
        WorldMap.killPiece(piece, false, shouldSend);
    }

    @Override
    public int getBarPoints() { return this.barPoints; }
}
