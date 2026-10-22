package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class MysteryBoss extends NpcBoss{

    //this boss does nothing, it's just a hint to what there is to come

    public MysteryBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, 6);
        this.barPoints = 12;
        this.maxHealth = 100;
        this.attack = 4;
        this.movementChance = 0;
        this.texture = "MysteryBoss";
    }
}
