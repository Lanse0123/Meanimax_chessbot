package lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece;

import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.HealPulse;
import lanse.worldwarchess.shared.core.piece.ai.bosses.NpcBoss;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.Objects;

public class HealerPiece extends NpcPiece {

    public HealerPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    @Override
    public void move() {
        NPC.healerMoveLogic(this);

        //heal first boss within 3 tiles of it.
        for (Piece piece : WorldMap.pieces){
            if (piece == this || piece.getBoard() != this.board) continue;

            // if piece is more than 3 away from the queen, dont heal it
            if (Math.abs(piece.getPosX() - this.posX) > 3 || Math.abs(piece.getPosY() - this.posY) > 3) continue;

            if (Objects.equals(piece.getTeam(), this.team) && (piece instanceof NpcBoss || piece instanceof CommanderPiece)){
                WorldMap.spawnPiece(new HealPulse(this.playerID, piece.getBoard(), "heal", this.team,
                        piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize()), true);

                return;
            }
        }
    }

    @Override
    public char getPrefixReplacement(){return 'h';}

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(4, 4, 4, 1);

        return new HealerPiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }
}
