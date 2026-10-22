package lanse.worldwarchess.shared.core.piece;

import lanse.worldwarchess.shared.core.piece.fairies.FairyCommanderPiece;
import lanse.worldwarchess.shared.core.piece.fairies.FairyPiece;

import java.util.List;
import java.util.stream.Stream;

public enum PieceType {

    //TODO - only the true ones should be allowed to be selected in sandbox by FAIRY PIECE / FAIRY COMMANDER ONLY!
    // OTHERWISE, USE NORMAL PIECE LIST (except for unknown)

    //NORMAL PIECE TYPES:
    PAWN("pawn", true, "p"),
    BISHOP("bishop", true, "b"),
    KNIGHT("knight", true, "n"),
    ROOK("rook", true, "r"),
    QUEEN("queen", true, "q"),
    KING("king", true, "k"),
    UNKNOWN("unknown", false, null),

    //FAIRY PIECE TYPES:
    //TODO: add textureKeySuffixes here
    CHECKER("checker", false, null),
    CHECKER_KING("checker_king", false, null),
    AMAZON("amazon", true, "amazon"),
    CHANCELLOR("chancellor", false, null),
    ARCHBISHOP("archbishop", false, null),
    GENERAL("general", false, null),
    STONE_GENERAL("stone_general", false, null),
    CAMEL("camel", true, "camel"),
    WILDEBEEST("wildebeest", false, null),
    GRASSHOPPER("grasshopper", false, null),
    FERZ("ferz", false, null),
    SOLDIER("soldier", false, null),
    XIANGQI_HORSE("xiangqi_horse", false, null),
    DRAGON_BISHOP("dragon_bishop", false, null),
    SERGEANT("sergeant", false, null),
    ZEBRA("zebra", true, "zebra"),
    BISON("bison", false, null),
    BODYGUARD("bodyguard", false, null),
    BUFFALO("buffalo", false, null),
    CANVASSER("canvasser", false, null),
    CHAMELEON("chameleon", false, null),
    DINOSAUR("dinosaur", false, null),
    DRAGON("dragon", false, null),
    DUCK("duck", false, null),
    DUMMY("dummy", false, null),
    EDGEHOG("edgehog", false, null),
    ENSIGN("ensign", false, null),
    FLAMINGO("flamingo", true, "flamingo"),
    GUARD("guard", false, null),
    HARE("hare", false, null),
    HIPPOSAUR("hipposaur", false, null),
    JESTER("jester", false, null),
    KNEEN("kneen", false, null),
    LEOPARD("leopard", false, null),
    LLAMA("llama", false, null),
    MAHARAJA("maharaja", false, null),
    MEGASAUR("megasaur", false, null),
    OKAPI("okapi", false, null),
    PRIEST("priest", false, null),
    REFLECTING_BISHOP("reflecting_bishop", false, null),
    RHUBARB("rhubarb", false, null),
    ROYAL_GUARD("royal_guard", false, null),
    SHIP("ship", false, null),
    SHORT_ROOK("short_rook", false, null),
    SNAKE("snake", false, null),
    SQUIRREL("squirrel", false, null),
    THREELEAPER("threeleaper", false, null),
    TIGER("tiger", false, null),
    VANGUARD("vanguard", false, null),
    SULTAN("sultan", false, null),
    DUNGEON("dungeon", false, null);

    private final String displayName;
    private final Boolean isComplete;
    private final String textureKeySuffix;

    PieceType(String displayName, boolean isComplete, String textureKeySuffix) {
        this.displayName = displayName;
        this.isComplete = isComplete;
        this.textureKeySuffix = textureKeySuffix;
    }

    public static List<PieceType> getAvaiableTypeList(Piece piece) {
        if (!(piece instanceof FairyPiece) && !(piece instanceof FairyCommanderPiece)){
            return List.of(PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING);
        } else {
            return Stream.of(values())
                    .filter(type -> type.isComplete)
                    .filter(type -> type != UNKNOWN)
                    .toList();
        }
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
        return UNKNOWN; // default fallback
    }
}
