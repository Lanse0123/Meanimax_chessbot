package lanse.worldwarchess.shared.core.piece.ai.attacks;

import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.Random;

public class FreezeAttack extends PieceAttack {

    public FreezeAttack(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, type, team, boardX, boardY, posX, posY, size);
        this.isSolid = true;
        this.texture = "FreezeAttack";
        this.lifeSpan = new Random().nextInt(10) + 10;
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
