package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class ZooBoss extends NpcBoss{

    // This is a meme boss. UNLEASH THE ARMY!!

    public ZooBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, 4);
        this.barPoints = 30;
        this.maxHealth = 50;
        this.attack = 0;
        this.movementChance = 0;
        this.texture = "zoo";

        setCurrentHealthToMax();
        //TODO - i want this boss to be rare...
    }

    @Override
    public void move() {
        //TODO - spawn 4 knights on all 4 corners with random types if no piece is on that square.
        // This function should be abstracted into pieceattacks, since many will have this type of minion spawning
    }

    @Override
    public void killPiece(Piece piece, boolean shouldSend) {
        WorldMap.killPiece(this, false, shouldSend);

//        //kill all the boss's minions
//        for (Piece piece1 : WorldMap.pieces){
//            if (piece1 instanceof ZombiePiece){
//                if (((ZombiePiece) piece1).getBoss() == this){
//                    if (!shouldSend) {
//                        System.err.println("shouldSend is false, but we called erasePiece with it as true earlier");
//                    }
//                    WorldMap.erasePiece(piece1, true);
//                }
//            }
//        }
    }
}
