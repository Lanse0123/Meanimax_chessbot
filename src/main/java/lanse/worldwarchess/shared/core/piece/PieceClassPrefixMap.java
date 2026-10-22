package lanse.worldwarchess.shared.core.piece;

public class PieceClassPrefixMap {

    public static Class<? extends Piece> getPieceClass(int index) {
        return Piece.class;
    }

    public static int getPrefixIndex(Class<? extends Piece> pieceClass) {
        return 1;
    }
}
