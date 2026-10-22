package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;

public class BossHealthManager {

    public static int getPieceStrength(Piece piece) {
        if (piece instanceof NpcBoss boss) return boss.getAttackDamage();

        int baseStrength = switch (piece.getType()) {
            case PAWN -> 1;
            case KNIGHT -> 3;
            case BISHOP -> 4;
            case ROOK -> 5;
            case QUEEN -> 9;
            case KING -> 25;
            default -> getFairyPieceStrength(piece);
        };

        if (piece instanceof CommanderPiece) {
            baseStrength *= 4;
        }

        return baseStrength;
    }

    private static int getFairyPieceStrength(Piece piece) {
        return switch (piece.getType()){
            case UNKNOWN -> 0;
            case CHECKER -> 1;
            case CHECKER_KING -> 2;
            case AMAZON -> 15;
            case CHANCELLOR -> 8;
            case ARCHBISHOP -> 6;
            case GENERAL -> 5;
            case STONE_GENERAL -> 1;
            case CAMEL -> 4;
            case WILDEBEEST -> 7;
            case GRASSHOPPER -> 0;
            case FERZ -> 1;
            case SOLDIER -> 1;
            case XIANGQI_HORSE -> 3;
            case DRAGON_BISHOP -> 6;
            case SERGEANT -> 2;
            case ZEBRA -> 4;
            case BISON -> 8;
            case BODYGUARD -> 4;
            case BUFFALO -> 11;
            case CANVASSER -> 9;
            case CHAMELEON -> 5;
            case DINOSAUR -> 4;
            case DRAGON -> 4;
            case DUCK -> 0;
            case DUMMY -> 0;
            case EDGEHOG -> 4;
            case ENSIGN -> 5;
            case FLAMINGO -> 4;
            case GUARD -> 2;
            case HARE -> 3;
            case HIPPOSAUR -> 1;
            case JESTER -> 0;
            case KNEEN -> 5;
            case LEOPARD -> 5;
            case LLAMA -> 5;
            case MAHARAJA -> 25;
            case MEGASAUR -> 7;
            case OKAPI -> 6;
            case PRIEST -> 4;
            case REFLECTING_BISHOP -> 3;
            case RHUBARB -> 7;
            case ROYAL_GUARD -> 5;
            case SHIP -> 5;
            case SHORT_ROOK -> 3;
            case SNAKE -> 4;
            case SQUIRREL -> 6;
            case THREELEAPER -> 3;
            case TIGER -> 6;
            case VANGUARD -> 2;
            case SULTAN -> 20;
            default -> 0;
        };
    }
}
