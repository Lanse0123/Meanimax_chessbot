package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.piece.marker.LightningMarker;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

public class ElectroKingBoss extends NpcBoss{

    public ElectroKingBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "king", color, boardX, boardY, posX, posY, 2);
        this.barPoints = 12;
        this.maxHealth = 100;
        this.attack = 4;
        this.pieceSize = 2;
        this.movementChance = 0.4;
        this.texture = "electroking";

        setCurrentHealthToMax();
    }

    @Override
    public void move(){
        Board board = this.board;
        assert board != null;
        Set<Piece> nearbyPieces = board.getPiecesOfRange(this, 8);
        List<Piece> attackablePieces = new ArrayList<>();

        if (!nearbyPieces.isEmpty()){
            for (Piece piece : nearbyPieces){
                if (!(piece instanceof PieceAttack) && !Objects.equals(piece.getTeam(), this.getTeam())){
                    attackablePieces.add(piece);
                }
            }
        }

        if (attackablePieces.isEmpty()) {
            NPC.vampireMoveLogic(this);
            return;
        }

        //TODO - this is 1 attack type it can do. It should be able to pick from other types of attacks too.
        for (int i = 0; i <= 4; i++) {

            // Pick a random target
            Random random = new Random();
            Piece target = attackablePieces.get(random.nextInt(attackablePieces.size()));
            new LightningMarker(target.getBoard(), target.getPosX(), target.getPosY(), 2, target.getPieceSize());
        }
    }
}
