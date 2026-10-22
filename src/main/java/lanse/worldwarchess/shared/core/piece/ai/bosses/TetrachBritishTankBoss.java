package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class TetrachBritishTankBoss extends NpcBoss{

    //TODO - this class will be like ShotgunKing, but FAR stronger and tankier
    public TetrachBritishTankBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, 4);
        this.barPoints = 60;
        this.maxHealth = 40;
        this.attack = 250;

        setCurrentHealthToMax();
    }

    @Override
    public void move() { NPC.spartanMoveLogic(this, false); }
}
