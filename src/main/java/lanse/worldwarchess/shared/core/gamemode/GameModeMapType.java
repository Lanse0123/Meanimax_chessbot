package lanse.worldwarchess.shared.core.gamemode;

import lanse.worldwarchess.shared.core.screens.lounge.DebugPreset;
import lanse.worldwarchess.shared.core.screens.sandbox.CustomMapPreset;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.badboard.BadApplePreset;

public enum GameModeMapType {
    DIAMOND, HOURGLASS, BRITISHARMY1812, INFERNOCROSSING, HYPOTHERMIC, COMMANDERDEBUG1,
    FAIRYDEBUG, /**CITYDEBUG,**/ SQUARE, CONWAYDEBUG, SIEGE, TUTORIAL, SHADOW, DEBUG,
    SANDBOX, BADAPPLE, CUSTOM;


    public static void setupMap(){
        switch (WorldMap.currentMapPreset) {
            case DIAMOND -> MapShapePresets.diamondPreset();
            case HOURGLASS -> MapShapePresets.hourglassPreset();
            case BRITISHARMY1812 -> MapShapePresets.lineBattlePreset();
            case INFERNOCROSSING -> MapShapePresets.infernoCrossingPreset(false);
            case HYPOTHERMIC -> MapShapePresets.infernoCrossingPreset(true);
            case COMMANDERDEBUG1 -> MapShapePresets.commanderTesting();
            case CONWAYDEBUG -> MapShapePresets.conwayTesting();
            case FAIRYDEBUG -> MapShapePresets.fairyTesting();
            case /**CITYDEBUG,**/ SQUARE -> MapShapePresets.cityTesting();
            case SIEGE -> MapShapePresets.siegePreset();
            case SHADOW -> MapShapePresets.shadowPreset();
            case DEBUG -> DebugPreset.debugPreset();
            case BADAPPLE -> BadApplePreset.badApplePreset();
            case CUSTOM -> CustomMapPreset.loadMap();
        }
    }
}
