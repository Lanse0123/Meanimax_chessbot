package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class ShotgunKingBoss extends NpcBoss{

    public ShotgunKingBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "king", color, boardX, boardY, posX, posY, 1);
        this.barPoints = 6;
        this.maxHealth = 4;
        this.attack = 25;
        this.texture = "ak"; //TODO - replace this with shotgun king img
        // Other: On the start of the turn, there is a 50% chance to use the shotgun ability.

        setCurrentHealthToMax();
    }

    @Override
    public void move() { NPC.spartanMoveLogic(this, false); }

    //TODO - Brought from Shotgun King, The Final Checkmate game. The main character became yet another bossfight.

    //TODO - shotgun ability:
    // Check the 8x8 tiles around the king. If there is an enemy piece, get the nearest one.
    // Shoot the shotgun in it's direction. Friendly fire is on.
        //idc wtf you need friendly fire. PLEASE. this message was approved by a wild mexican
    // Shooting the shotgun releases 4 - 7 Shotgun Shells with random offset line tracing from the original direction.

    //TODO - shotgun shell:
    // Attack: 1
    // Size: small. It is a line tracer thing.
    // Movement: follows a trajectory given at birth, and moves up to 8 tiles.
    // If at any point it collides with a piece, stop the shell and kill the shell soon after.
    // Then apply damage to the enemy.
}
