package lanse.worldwarchess.shared.core.piece.fairies;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

/** REMEMBER TO ADD TO THE PieceClassPrefixMap.java WHEN ADDING A NEW PIECE CLASS*/
public class FairyPiece extends Piece {

    protected String texture;

    //This is a much more broad type of piece that comes from different types of chess variants.

    public FairyPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, size);
        //TODO: this won't work until texture are added to PieceType.java
        this.texture = team.equals("White") ? "w" : "b" + PieceType.fromName(type).getTextureKeySuffix();
    }

    public FairyPiece(int playerId, Board board, String type, String color, String texture, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, size);
        this.texture = texture;
    }

    @Override
    public String getTexture() { return texture; }

    @Override
    public Piece promoteOverride(Piece piece){
        if (piece.getType() == PieceType.CHECKER){
            String texture = "wkc";

            if (piece.getTeam().equalsIgnoreCase("Black")) texture = "bkc";
            return new FairyPiece(piece.getPlayerID(), piece.getBoard(), "checkerking", piece.getTeam(),
                    texture, piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
        }
        else {
            return this;
        }
    }
}