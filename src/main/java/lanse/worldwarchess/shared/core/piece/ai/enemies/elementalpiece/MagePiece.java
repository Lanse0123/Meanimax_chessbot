package lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.piece.marker.MageMarker;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.List;
import java.util.*;

public class MagePiece extends NpcPiece {

    //The art for mage pieces was inspired by the Mobzilla Bossfight spawn egg from Minecraft's Orespawn mod,
    //combined with the apprentice monkey from Bloons Tower Defense 5

    public MagePiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    //mage moves like a zombie, but needs extra logic to shoot magic
    @Override
    public void move(){
        Board board = this.board;
        assert board != null;
        Set<Piece> nearbyPieces = board.getPiecesOfRange(this, 6);
        List<Piece> attackablePieces = new ArrayList<>();

        if (!nearbyPieces.isEmpty()){
            for (Piece piece : nearbyPieces){
                if (!(piece instanceof PieceAttack) && !Objects.equals(piece.getTeam(), this.getTeam())){
                    attackablePieces.add(piece);
                }
            }
        }

        if (attackablePieces.isEmpty()) {
            NPC.zombieMoveLogic(this, 80);
            return;
        }

        for (int i = 0; i <= 1; i++) {

            // Pick a random target
            Random random = new Random();
            Piece target = attackablePieces.get(random.nextInt(attackablePieces.size()));
            new MageMarker(target.getBoard(), target.getPosX(), target.getPosY(), 2, target.getPieceSize());
        }
    }

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(4, 4, 4, 0);

        return new MagePiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    @Override
    public char getPrefixReplacement(){return 'm';}

    public static Color getColorOverride(){return new Color(50, 0, 50); }
}
