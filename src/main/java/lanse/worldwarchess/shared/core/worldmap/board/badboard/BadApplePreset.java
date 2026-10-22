package lanse.worldwarchess.shared.core.worldmap.board.badboard;

import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;

public class BadApplePreset extends MapShapePresets {

    public static void badApplePreset() {
        addBoard(0, 0, "empty", "bad");
        addBoard(8 * TILE_SIZE, 32 * TILE_SIZE + GAP, "WhiteTerritory", "commander");
        addBoard(8 * TILE_SIZE, -16 * TILE_SIZE - GAP, "BlackTerritory", "commander");
    }
}
