package lanse.worldwarchess.shared.core.piece.ai.attacks;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

/** REMEMBER TO ADD TO THE PieceClassPrefixMap.java WHEN ADDING A NEW PIECE CLASS*/
public class PieceAttack extends Piece {

    // this attack folder lets bosses easily pick and choose which attack types to use

    protected boolean isSolid; //Can pieces move through this attack?
    protected String texture; //Texture reference
    protected int lifeSpan; //How long might the attack stay for
    protected boolean isAttackableByGiants; //Can a piece with size > 1 attack it?

    public PieceAttack(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, type, team, boardX, boardY, posX, posY, size);
        this.isSolid = false;
        this.isAttackableByGiants = true;
    }

    public boolean isSolid(){ return isSolid; }
    public String getTexture(){ return texture; }
    public int getLifeSpan(){ return lifeSpan; }
    public boolean getIsAttackable(){ return isAttackableByGiants; }

    public void doLifeSpanCheck(){}
}
