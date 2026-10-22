package lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.FireAttack;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.List;
import java.util.*;

public class PhoenixPiece extends NpcPiece {

    public boolean isAlive = true;
    public boolean canRebirth = true;
    public PhoenixPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
        this.pieceSize = 1;
    }

    @Override
    public void move(){
        if (this.isAlive) {
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
            WorldMap.killPiece(target, true, true);
            FireAttack fire = new FireAttack(this.playerID, target.getBoard(), "fire", "fire", target.getBoardX(), target.getBoardY(), target.getPosX(), target.getPosY(), pieceSize);
            WorldMap.spawnPiece(fire, true);

        } else {
            if (!this.canRebirth){
                this.killPiece(this, true);
                return;
            }

            //I might want to make it invisible while it is respawning
            Random random = new Random();
            if (random.nextInt(10) > 8){
                this.isAlive = true;
                this.pieceSize *= 2;
                this.canRebirth = false;

                Board board = this.board;
                assert board != null;
                Set<Piece> nearbyPieces = board.getPiecesOfRange(this, 2);

                for (Piece piece : nearbyPieces){
                    if (piece instanceof FireAttack) WorldMap.erasePiece(piece, true);
                }
            }
        }
    }

    @Override
    public char getPrefixReplacement(){return 'p';}

    public static Color getColorOverride(){return new Color(255, 30, 30); }

    @Override
    public void killPiece(Piece piece, boolean shouldSend){
        if (this.canRebirth){
            this.isAlive = false;
            FireAttack fire = new FireAttack(this.playerID, this.board, String.valueOf(this.type),
                    this.team, this.boardX, this.boardY, this.posX, this.posY, pieceSize);

            if (shouldSend) {
                System.err.println("Error: shouldSend is true, but spawning is always false here.");
            }
            WorldMap.spawnPiece(fire, false);
        } else {
            WorldMap.killPiece(this, false, shouldSend);
        }
    }
}
