package lanse.worldwarchess.shared.core.piece.marker;

import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;

public class MoveMarker extends Marker{

    public MoveMarker(Board board, int posX, int posY, int size) {
        super(board, posX, posY, new Color(250, 250, 100, 100), 2, size);
    }
}
