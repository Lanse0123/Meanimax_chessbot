package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class PointerPiece extends NpcBoss {

    //OMG MOM GET THE CAMERA

    public PointerPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, 6);
        this.texture = "pointer";
    }
}
