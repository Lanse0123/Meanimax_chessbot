package lanse.worldwarchess.shared.core.piece.fairies;

import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

/** REMEMBER TO ADD TO THE PieceClassPrefixMap.java WHEN ADDING A NEW PIECE CLASS*/
public class FairyCommanderPiece extends CommanderPiece {
    private final String texture;

    //This is a much more broad type of piece that comes from different types of chess variants, but a commander.

    public FairyCommanderPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, 2);
        //TODO: this won't work until texture are added to PieceType.java
        this.texture = team.equals("White") ? "w" : "b" + PieceType.fromName(type).getTextureKeySuffix();
    }

    // TODO - Reference: https://www.chess.com/blog/Lachelner/fairy-pieces-info-chess-com
    //TODO - other reference: https://en.wikipedia.org/wiki/List_of_fairy_chess_pieces

}
