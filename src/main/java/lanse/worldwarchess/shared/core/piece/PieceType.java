package lanse.worldwarchess.shared.core.piece;

public enum PieceType {

    PAWN("pawn"),
    BISHOP("bishop"),
    KNIGHT("knight"),
    ROOK("rook"),
    QUEEN("queen"),
    KING("king");

    private final String displayName;

    PieceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PieceType fromName(String name) {
        for (PieceType type : values()) {
            if (type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return PAWN; // default fallback
    }
}
