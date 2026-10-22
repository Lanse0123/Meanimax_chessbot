package lanse.worldwarchess.shared.core.piece.ai.enemies.commander;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.FireAttack;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

public class CommanderPhoenixPiece extends NpcCommanderPiece{

    public boolean isAlive = true;
    public boolean canRebirth = true;

    public CommanderPhoenixPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
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
                NPC.vampireMoveLogic(this);
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

            // I might want to make this invisible while it is in the process of respawning
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
    public Piece promoteOverride(Piece piece) {return null;}

    @Override
    public char getPrefixReplacement(){return 'p';}

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
