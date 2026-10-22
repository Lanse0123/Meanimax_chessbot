package lanse.worldwarchess.shared.core.piece.ai.attacks;

import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.Random;

public class FireAttack extends PieceAttack{

    public FireAttack(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, type, team, boardX, boardY, posX, posY, size);
        this.isSolid = true;
        this.texture = "FireAttack";
        this.lifeSpan = new Random().nextInt(9) + 7;
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
