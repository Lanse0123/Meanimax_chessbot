package lanse.worldwarchess.shared.core.piece.ai.enemies.commander;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.FreezeAttack;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

public class CommanderIcePiece extends NpcCommanderPiece{

    public CommanderIcePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    @Override
    public void move(){
        Board board = this.board;
        assert board != null;
        Set<Piece> nearbyPieces = board.getPiecesOfRange(this, 4);
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

        // Pick a random target
        Random random = new Random();
        Piece target = attackablePieces.get(random.nextInt(attackablePieces.size()));
        //TODO - freeze the piece not kill it
        WorldMap.killPiece(target, true, true);
        FreezeAttack ice = new FreezeAttack(this.playerID, target.getBoard(), String.valueOf(this.type), this.team, target.getBoardX(), target.getBoardY(), target.getPosX(), target.getPosY(), pieceSize);
        WorldMap.spawnPiece(ice, true);
    }

    @Override
    public Piece promoteOverride(Piece piece) {return null;}

    @Override
    public char getPrefixReplacement(){return 'i';}
}
