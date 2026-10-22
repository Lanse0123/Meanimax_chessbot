package lanse.worldwarchess.shared.core.piece.ai.attacks;

import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class MageAttack extends PieceAttack{

    public MageAttack(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, type, team, boardX, boardY, posX, posY, size);
        this.isSolid = false;
        this.texture = "MageAttack";
        this.lifeSpan = 2;
        this.isAttackableByGiants = false;
    }

    @Override
    public void doLifeSpanCheck(){
        if (this.lifeSpan <= 0){
            WorldMap.erasePiece(this, true);
        }
        else {
            this.lifeSpan--;
        }
    }
}
