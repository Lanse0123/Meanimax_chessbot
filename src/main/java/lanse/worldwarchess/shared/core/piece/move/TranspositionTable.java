package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.NPC;

import java.util.Random;

/** Written by Lanse
 * <br><br>
 * So in the name of efficiency, I found this idea from Sebastian Lague, where he mentions that searching duplicate
 * positions is inefficient, because you can be searching and evaluating thousands of the same positions over and over.
 * This uses Zobrist hashing, which isn't perfect, because it could technically lead to collisions, but I think its fine
 * because the chances are ike 1 in 18 trillion or so. **/
public class TranspositionTable {

    public enum Bound {
        EXACT, LOWER, UPPER
    }

    public record TranspositionTableEntry(int depth, double score, Bound bound) {}

    private static final int PIECE_TYPE_COUNT = PieceType.values().length;

    private static final long[][][] PIECE_KEYS = new long[2][PIECE_TYPE_COUNT][64];
    private static final long[][][] EVER_MOVED_KEYS = new long[2][PIECE_TYPE_COUNT][64];
    private static final long[] SIDE_TO_MOVE_KEYS = new long[2];
    private static final long[] ROOT_TEAM_KEYS = new long[2];
    private static final long[] AI_KEYS = new long[NPC.SELECTED_AI.values().length];

    static {
        Random random = new Random(0xB04C2026L);

        for (int team = 0; team < 2; team++) {
            for (int type = 0; type < PIECE_TYPE_COUNT; type++) {
                for (int square = 0; square < 64; square++) {
                    PIECE_KEYS[team][type][square] = random.nextLong();

                    for (int flag = 0; flag < 2; flag++) {
                        EVER_MOVED_KEYS[team][type][square] = random.nextLong();
                    }
                }
            }
            SIDE_TO_MOVE_KEYS[team] = random.nextLong();
            ROOT_TEAM_KEYS[team] = random.nextLong();
        }

        for (int i = 0; i < AI_KEYS.length; i++) {
            AI_KEYS[i] = random.nextLong();
        }
    }

    //Lovely going from big function to small function
    public static long hashPosition(MoveContext context, boolean teamToMove, boolean rootTeam, NPC.SELECTED_AI ai) {
        return context.positionHash() ^ SIDE_TO_MOVE_KEYS[teamToMove ? 1 : 0] ^ ROOT_TEAM_KEYS[rootTeam ? 1 : 0] ^ AI_KEYS[ai.ordinal()];
    }

    public static long hashPiece(Piece piece, int x, int y) {
        //TODO - this should be impossible by now, at some point I want to remove all of the bounds checks everywhere
        if (x < 0 || y < 0 || x >= 8 || y >= 8) {
            return 0L;
        }

        int team = piece.getTeam() ? 1 : 0;
        int type = piece.getType().ordinal();
        int square = y * 8 + x;

        long hash = PIECE_KEYS[team][type][square];

        if (piece.hasMovedEver) {
            hash ^= EVER_MOVED_KEYS[team][type][square];
        }

        return hash;
    }
}
