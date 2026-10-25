package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Written by Lanse
 * <br><br>
 * The rest of the game was not built for efficiency, but AI algorithms even with alpha beta pruning need to search
 * thousands or millions of positions, so this was made to make my life easier.
 **/
public record MoveContext(Board board, List<Piece> pieces) {

    public static MoveContext contextify(Board board) {
        return new MoveContext(board, new ArrayList<>(WorldMap.getAllPiecesOfBoard(board)));
    }

    public Piece getPieceAt(int x, int y, Piece ignoredPiece) {
        return board.getPieceAt(x, y, Optional.ofNullable(ignoredPiece), pieces);
    }
}
