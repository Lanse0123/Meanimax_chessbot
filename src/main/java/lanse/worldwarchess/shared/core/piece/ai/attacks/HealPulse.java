package lanse.worldwarchess.shared.core.piece.ai.attacks;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.Optional;

public class HealPulse extends PieceAttack{

    public HealPulse(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, type, team, boardX, boardY, posX, posY, size);
        this.isSolid = false;
        this.lifeSpan = 2;
        this.texture = "HealPulse";

        Piece target = board.getPieceAt(posX, posY, Optional.empty(), WorldMap.pieces);

        if (target != null && target.getTeam().equalsIgnoreCase(team)){
            target.heal(2);
        }
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
