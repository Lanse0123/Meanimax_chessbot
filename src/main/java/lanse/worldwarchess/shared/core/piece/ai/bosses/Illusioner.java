package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.fairies.FairyLegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Illusioner extends NpcBoss{

    private final boolean isMinion;
    private Piece boss = null;

    //default constructor used by generic call
    public Illusioner(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "illusioner", color, boardX, boardY, posX, posY, pieceSize);
        this.isMinion = false;
        this.texture = "illusioner";
        this.barPoints = 9;
        this.maxHealth = 1;
    }

    public Illusioner(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize, boolean isMinion, Piece boss) {
        super(playerId, board, "illusioner", color, boardX, boardY, posX, posY, pieceSize);
        this.isMinion = isMinion;
        this.texture = "illusioner";
        this.boss = boss;

        setCurrentHealthToMax();
    }

    @Override
    public void move() {
        if (!this.isMinion){
            WorldMap.minionPieceSpawnRequests.add(new Illusioner(this.playerID, this.board, this.type.getDisplayName(),
                    this.team, this.boardX, this.boardY, this.posX, this.posY, this.pieceSize, true, this));
        }

        NPC.vampireMoveLogic(this);
    }

    @Override
    public Collection<LegalMoveDetector.Coordinate> getMovementOverride() {
        List<LegalMoveDetector.Coordinate> legalMoves = new ArrayList<>();
        legalMoves.addAll(LegalMoveDetector.getKingMoves(this, this.board));
        legalMoves.addAll(LegalMoveDetector.getKingMoves(this, this.board));
        legalMoves.addAll((FairyLegalMoveDetector.getCamelMoves(this, this.board)));
        legalMoves.addAll((FairyLegalMoveDetector.getZebraMoves(this, this.board)));
        return legalMoves;
    }

    @Override
    public void killPiece(Piece piece, boolean shouldSend) {
        WorldMap.killPiece(this, false, shouldSend);

        //kill all the illusioner's minions
        for (Piece piece1 : WorldMap.pieces){
            if (piece1 instanceof Illusioner){
                if (((Illusioner) piece1).boss == this){
                    if (!shouldSend) {
                        System.err.println("Error: shouldSend is false, but spawning is always true here.");
                    }
                    WorldMap.erasePiece(piece1, true);
                }
            }
        }
    }
}
