package lanse.worldwarchess.shared.core.piece.marker;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeathMarker extends Marker{

    //This is an eternal marker that will represent where pieces have died. By its existence, it shows the death
    // and violence of the war. Respect the fallen.
    public static ConcurrentLinkedQueue<Piece> deathRow = new ConcurrentLinkedQueue<>();

    public DeathMarker(Board board, int posX, int posY, int size) {
        super(board, posX, posY, new Color(255, 0, 0, 100), -40404, size);
    }
}
