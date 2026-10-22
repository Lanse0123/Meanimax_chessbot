package lanse.worldwarchess.shared.core.piece.ai.enemies.commander;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.piece.marker.MageMarker;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

public class CommanderMagePiece extends NpcCommanderPiece{


    public CommanderMagePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
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

        for (int i = 0; i <= 5; i++) {

            // Pick a random target
            Random random = new Random();
            Piece target = attackablePieces.get(random.nextInt(attackablePieces.size()));
            new MageMarker(target.getBoard(), target.getPosX(), target.getPosY(), 2, target.getPieceSize());
        }
    }

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(4, 4, 4, 0);

        return new CommanderMagePiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    @Override
    public char getPrefixReplacement(){return 'm';}
}
