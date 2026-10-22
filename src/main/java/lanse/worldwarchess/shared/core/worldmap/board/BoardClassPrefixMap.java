package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.worldmap.board.badboard.BadBoard;
import lanse.worldwarchess.shared.core.worldmap.board.fairyboard.FairyBoard;
import lanse.worldwarchess.shared.utils.BiDirectionalMap;

public class BoardClassPrefixMap {

    private static final BiDirectionalMap<Integer, Class<? extends Board>> map = new BiDirectionalMap<>();

    public static void register(int index, Class<? extends Board> boardClass) {
        map.put(index, boardClass);
    }

    public static Class<? extends Board> getBoardClass(int index) {
        return map.getValue(index);
    }

    public static int getPrefixIndex(Class<? extends Board> boardClass) {
        Integer result = map.getKey(boardClass);
        return result != null ? result : -1;
    }

    public static void initPrefixMap() {
        //DO NOT REMOVE OR CHANGE THESE, ONLY ADD TO THE LIST!
        register(1, Board.class);
        register(2, CommanderBoard.class);
        register(3, InfernoBoard.class);
        register(4, WinterBoard.class);
        register(5, SiegeBoard.class);
        register(6, ShadowBoard.class);
        register(7, FairyBoard.class);
        register(8, ConwayBoard.class);
        register(9, EmptyBoard.class);
        register(10, BadBoard.class);
    }
}
