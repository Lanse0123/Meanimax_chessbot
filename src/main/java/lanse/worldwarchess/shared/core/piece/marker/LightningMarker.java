package lanse.worldwarchess.shared.core.piece.marker;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.attacks.ThunderAttack;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.Optional;

public class LightningMarker extends Marker{

    public LightningMarker(Board board, int posX, int posY, int lifeSpan, int size) {
        super(board, posX, posY, new Color(0, 150, 200), lifeSpan, size);
    }

    @Override
    public void onLifeSpanEnding(){
        Piece target = board.getPieceAt(this.posX, this.posY, Optional.empty(), WorldMap.getAllPiecesOfBoard(board));

        if (target != null) {
            WorldMap.killPiece(target, true, true);
            markers.remove(this);
            WorldMap.spawnPiece(new ThunderAttack(-404, target.getBoard(), "NPC", "NPC", target.getBoardX(), target.getBoardY(), posX, posY, size), true);
        }
    }
}
