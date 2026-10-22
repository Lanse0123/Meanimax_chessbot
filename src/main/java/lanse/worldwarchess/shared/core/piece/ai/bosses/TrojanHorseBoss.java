package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.Random;

public class TrojanHorseBoss extends NpcBoss{
    //to anyone debugging this class, trojan horse is not an actual trojan horse, and your pc is safe.
    //your chess pieces on the other hand are NOT safe lol


    //This is overloaded, its just called from a generic
    public TrojanHorseBoss(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int size) {
        super(playerId, board, "knight", team, boardX, boardY, posX, posY, size);

        Random random = new Random();
        int randomSize = random.nextBoolean() ? 4 : 2;
        initBoss(randomSize);
    }

    public TrojanHorseBoss(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int size, boolean isCertified) {
        super(playerId, board, "knight", team, boardX, boardY, posX, posY, size);
        initBoss(size);
    }

    private void initBoss(int size) {
        this.pieceSize = size;
        this.texture = "trojanknight";

        if (size == 4) {
            this.barPoints = 20;
            this.maxHealth = 64;
            this.attack = 32;
            this.movementChance = 0.55;

        } else if (size == 2) {
            this.barPoints = 5;
            this.maxHealth = 16;
            this.attack = 8;
            this.movementChance = 0.75;

        } else if (size == 1) {
            this.barPoints = 1;
            this.maxHealth = 8;
            this.attack = 2;
            this.movementChance = 0.9;
            this.texture = "sn";
        }

        setCurrentHealthToMax();
    }

    @Override
    public void move() {
        if (this.pieceSize > 1) {
            NPC.vampireMoveLogic(this);
        } else {
            NPC.spartanMoveLogic(this, true);
        }
    }

    @Override
    public void killPiece(Piece piece, boolean shouldSend){
        WorldMap.killPiece(this, false, shouldSend);

        if (this.pieceSize <= 1){
            return;
        }

        //TODO - make sure it spawns the minions in the right spot (this is probably wrong)
        int newSize = this.pieceSize / 2;

        if (!shouldSend) {
            System.err.println("Error: shouldSend is false, but spawning is always true here.");
        }
        //these dont need to use the minion spawn request since it should happen within the boss
        WorldMap.spawnPiece(new TrojanHorseBoss(this.playerID, this.board, String.valueOf(this.type), this.team, this.boardX,
                this.boardY, this.posX, this.posY, newSize, true), true);

        WorldMap.spawnPiece(new TrojanHorseBoss(this.playerID, this.board, String.valueOf(this.type), this.team, this.boardX,
                this.boardY, this.posX + newSize, this.posY, newSize, true), true);

        WorldMap.spawnPiece(new TrojanHorseBoss(this.playerID, this.board, String.valueOf(this.type), this.team, this.boardX,
                this.boardY, this.posX, this.posY + newSize, newSize, true), true);

        WorldMap.spawnPiece(new TrojanHorseBoss(this.playerID, this.board, String.valueOf(this.type),  this.team, this.boardX,
                this.boardY, this.posX + newSize, this.posY + newSize, newSize, true), true);
    }
}
