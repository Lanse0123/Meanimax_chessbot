package lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.FreezeAttack;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.List;
import java.util.*;

public class IcePiece extends NpcPiece {

    public IcePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
        this.pieceSize = 1;
    }

    @Override
    public void move(){
        Board board = this.board;
        assert board != null;
        Set<Piece> nearbyPieces = board.getPiecesOfRange(this, 2);
        List<Piece> attackablePieces = new ArrayList<>();

        if (!nearbyPieces.isEmpty()){
            for (Piece piece : nearbyPieces){
                if (!(piece instanceof PieceAttack) && !Objects.equals(piece.getTeam(), this.getTeam())){
                    attackablePieces.add(piece);
                }
            }
        }

        if (attackablePieces.isEmpty()) {
            NPC.zombieMoveLogic(this, 90);
            return;
        }

        // Pick a random target
        Random random = new Random();
        Piece target = attackablePieces.get(random.nextInt(attackablePieces.size()));
        //TODO - freeze the piece not kill it
        WorldMap.killPiece(target, true, true);
        FreezeAttack ice = new FreezeAttack(this.playerID, target.getBoard(), String.valueOf(this.type),
                this.team, target.getBoardX(), target.getBoardY(), target.getPosX(), target.getPosY(), pieceSize);

        WorldMap.spawnPiece(ice, true);
    }

    @Override
    public char getPrefixReplacement(){return 'i';}

    public static Color getColorOverride(){return new Color(173, 216, 230); }

    @Override
    public void killPiece(Piece piece, boolean shouldSend){
        WorldMap.killPiece(this, false, shouldSend);
        FreezeAttack ice = new FreezeAttack(this.playerID, this.board, String.valueOf(this.type),
                this.team, this.boardX, this.boardY, this.posX, this.posY, pieceSize);

        if (shouldSend) {
            System.err.println("shouldSend is true, but we called spawnPiece with it as false earlier");
        }
        WorldMap.spawnPiece(ice, false);
    }
}
