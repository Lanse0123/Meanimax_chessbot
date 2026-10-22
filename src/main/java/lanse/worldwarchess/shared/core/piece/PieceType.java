package lanse.worldwarchess.shared.core.piece;

import java.util.List;

public enum PieceType {

    PAWN("pawn", true, "p"),
    BISHOP("bishop", true, "b"),
    KNIGHT("knight", true, "n"),
    ROOK("rook", true, "r"),
    QUEEN("queen", true, "q"),
    KING("king", true, "k");

    private final String displayName;
    private final Boolean isComplete;
    private final String textureKeySuffix;

    PieceType(String displayName, boolean isComplete, String textureKeySuffix) {
        this.displayName = displayName;
        this.isComplete = isComplete;
        this.textureKeySuffix = textureKeySuffix;
    }

    public static List<PieceType> getAvaiableTypeList(Piece piece) {
        return List.of(PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING);
    }

    public String getDisplayName() {
        return displayName;
    }
    public boolean getIsComplete() {return isComplete; }
    public String getTextureKeySuffix() {return textureKeySuffix;}

    public static PieceType fromName(String name) {
        for (PieceType type : values()) {
            if (type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return PAWN; // default fallback
    }
}
