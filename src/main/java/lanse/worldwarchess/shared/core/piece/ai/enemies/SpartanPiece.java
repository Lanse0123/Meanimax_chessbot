package lanse.worldwarchess.shared.core.piece.ai.enemies;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;

public class SpartanPiece extends NpcPiece{

    /** spartan pieces will make a B line for the nearest commander king.
    Whatever move they can make that puts them the closest to the commander king is the move they will make.**/

    public SpartanPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, type, color, boardX, boardY, posX, posY, pieceSize);
    }

    /** spartans were speedran and added in like 20 minutes entirely, including their gamemode. I am very happy
    with how fast I can add new gamemodes in the style of Zombies, Vampires, and now Spartans**/

    @Override
    public void move(){ NPC.spartanMoveLogic(this, true); }

    @Override
    public char getPrefixReplacement(){return 's';}

    @Override
    public Piece promoteOverride(Piece piece) {
        String pieceType = NPC.getRandomPieceType(4, 4, 4, 0);

        return new SpartanPiece(piece.getPlayerID(), piece.getBoard(), pieceType, piece.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
    }

    public static Color getColorOverride(){return new Color(110, 100, 0); }
}
