package lanse.worldwarchess.shared.core.tutorial;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class TutorialPiece extends NpcPiece {

    //The weakest NPC piece. Its like a zombie that almost never moves.

    public TutorialPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    @Override
    public void move() {
        NPC.zombieMoveLogic(this, 6);
    }
    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(5, 3, 3, 0);

        return new TutorialPiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }
}
