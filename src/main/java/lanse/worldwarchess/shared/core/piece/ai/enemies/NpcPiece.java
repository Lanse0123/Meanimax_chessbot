package lanse.worldwarchess.shared.core.piece.ai.enemies;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class NpcPiece extends Piece {

    public NpcPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    public Piece promoteOverride(Piece piece) {
        return new NpcPiece(piece.getPlayerID(), piece.getBoard(), "queen", piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    public void killPiece(Piece piece, boolean shouldSend){
        WorldMap.killPiece(piece, false, shouldSend);
    }
}
