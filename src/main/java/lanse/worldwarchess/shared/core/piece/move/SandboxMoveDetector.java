package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SandboxMoveDetector extends LegalMoveDetector{

    public static List<Coordinate> getSandboxMoves(Piece piece){
        List<Coordinate> legalMoves = new ArrayList<>(16);
        List<Piece> boardPieces;
        int boardID;

        for (Board board : WorldMap.boards.values()) {
            boardPieces = WorldMap.getAllPiecesOfBoard(board);
            boardID = board.getBoardID();

            for (int x = 0; x <= board.getBoardSize(); x++) {
                for (int y = 0; y <= board.getBoardSize(); y++) {

                    if (board.getPieceAt(x, y, Optional.empty(), boardPieces) == null) {
                        legalMoves.add(new Coordinate(x, y, boardID));
                    }
                }
            }
        }
        return legalMoves;
    }
}
