package lanse.worldwarchess.shared.core.piece.ai.attacks;

import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class BarrierWall extends PieceAttack{

    // as weird as this might be, a plain old wall is just a piece attack that has no special properties.

    public BarrierWall(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, type, team, boardX, boardY, posX, posY, size);
        this.isAttackableByGiants = false;
        this.texture = "Barrier";
    }
}
