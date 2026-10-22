package lanse.worldwarchess.shared.core.piece.marker;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.bosses.ConwayBoss;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.Optional;

public class ConwayMarker extends Marker{

    public ConwayMarker(Board board, int posX, int posY, int lifeSpan, int size) {
        super(board, posX, posY, new Color(230, 160, 20, 100), lifeSpan, size);
    }

    @Override
    public void onLifeSpanEnding(){
        Piece target = board.getPieceAt(this.posX, this.posY, Optional.empty(), WorldMap.getAllPiecesOfBoard(board));

        if (target != null) {
            WorldMap.killPiece(target, true, true);
            markers.remove(this);
            WorldMap.spawnPiece(new ConwayBoss(-404, target.getBoard(), "NPC", "NPC",
                    target.getBoardX(), target.getBoardY(), posX, posY, 1), true);
        }
    }
}