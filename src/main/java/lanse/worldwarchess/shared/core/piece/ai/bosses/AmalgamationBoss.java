package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.enemies.AssassinPiece;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AmalgamationBoss extends NpcBoss {

    public AmalgamationBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "Amalgamation", color, boardX, boardY, posX, posY, 6);
        this.barPoints = 15;
        this.maxHealth = 90;
        this.attack = 20;
        this.movementChance = 1;
        this.texture = "Amalgamation";

        setCurrentHealthToMax();
    }

    @Override
    public void move() { NPC.vampireMoveLogic(this); }

    @Override
    public Collection<LegalMoveDetector.Coordinate> getMovementOverride() {
        List<LegalMoveDetector.Coordinate> legalMoves = new ArrayList<>();
        legalMoves.addAll(LegalMoveDetector.getKingMoves(this, this.board));
        legalMoves.addAll(LegalMoveDetector.getKnightMoves(this, this.board));
        return legalMoves;
    }

    @Override
    public void killPiece(Piece piece, boolean shouldSend){
        WorldMap.killPiece(this, false, shouldSend);

        if (!shouldSend) {
            System.err.println("Error: shouldSend is false, but spawning is always true here.");
        }
        //summon 3 minions upon death
        WorldMap.spawnPiece(new AssassinPiece(this.playerID, this.board, "knight",
                this.team, this.boardX, this.boardY, this.posX + 1, this.posY + 3, 1), true);

        WorldMap.spawnPiece(new AssassinPiece(this.playerID, this.board, "rook",
                this.team, this.boardX, this.boardY, this.posX + 2, this.posY + 3, 1), true);

        WorldMap.spawnPiece(new AssassinPiece(this.playerID, this.board, "knight",
                this.team, this.boardX, this.boardY, this.posX + 3, this.posY + 3, 1), true);
    }
}
