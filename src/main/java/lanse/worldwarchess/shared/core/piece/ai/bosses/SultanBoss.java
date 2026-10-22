package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.fairies.FairyLegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SultanBoss extends NpcBoss{

    public SultanBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "sultan", color, boardX, boardY, posX, posY, pieceSize);
        this.texture = "sultan";
        this.pieceSize = 2;
        this.attack = 4;
        this.barPoints = 16;
        this.maxHealth = 10;

        setCurrentHealthToMax();
    }

    @Override
    public void move() { NPC.spartanMoveLogic(this, false); }

    @Override
    public Collection<LegalMoveDetector.Coordinate> getMovementOverride() {
        //this piece is hell
        List<LegalMoveDetector.Coordinate> legalMoves = new ArrayList<>();
        legalMoves.addAll(LegalMoveDetector.getSlidingMoves(this, this.board, LegalMoveDetector.queenDirections));
        legalMoves.addAll(LegalMoveDetector.getKnightMoves(this, this.board));
        legalMoves.addAll((FairyLegalMoveDetector.getCamelMoves(this, this.board)));
        legalMoves.addAll((FairyLegalMoveDetector.getZebraMoves(this, this.board)));
        legalMoves.addAll((FairyLegalMoveDetector.getFlamingoMoves(this, this.board)));
        return legalMoves;
    }
}
