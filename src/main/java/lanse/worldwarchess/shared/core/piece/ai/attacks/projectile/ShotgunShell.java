package lanse.worldwarchess.shared.core.piece.ai.attacks.projectile;

import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class ShotgunShell extends PieceAttack {

    public ShotgunShell(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY) {
        super(playerId, board, type, team, boardX, boardY, posX, posY, 1);
        this.isSolid = false;
        this.lifeSpan = 1; //?
    }
}
