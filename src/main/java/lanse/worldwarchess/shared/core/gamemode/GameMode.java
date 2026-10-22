package lanse.worldwarchess.shared.core.gamemode;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GameMode {
    public static GameModeType currentMode = GameModeType.WAR;
    public static Color currentGamemodeColor1 = Color.WHITE;
    public static Color currentGamemodeColor2 = Color.BLACK;
    public static boolean isSinglePlayer = true;
    public static boolean isTutorial = false;
    public static boolean isFrenzy = false;
    public static boolean isFogOfWar = false;
    public static boolean isFischerRandom = false;
    public static boolean isSandbox = false;
    public static boolean isTimed = true;

    public static int playerCountFull;
    public static int playerCountOneTeam;
    public static int commanderCountOneTeam;

    // Maps for normal Gamemode
    public static final List<String> NORMAL_MAPS = List.of(
            "Diamond", "Hourglass", "BritishArmy1812", "InfernoCrossing", "Hypothermic",
            "CommanderDebug1", "FairyDebug", "Square"
    );

    // Maps for NPC Gamemodes
    public static final List<String> NPC_MAPS = List.of(
            "Diamond", "Hourglass", "BritishArmy1812", "InfernoCrossing", "Hypothermic",
            "CommanderDebug1", "ConwayDebug", "Square"
    );

    // Maps for Siege Gamemode
    public static final List<String> SIEGE_MAPS = List.of("Siege"); //TODO - add more siege maps

    // factions for Human vs NPC or NPC WAR Gamemode
    public static final List<String> NPC_CLASSES = List.of("Zombies", "Vampires", "Spartans",
            "Assassins", "Phoenix", "Winter", "Mage", "Necromancer", "Sentinel", "Neon");

    // Human vs NPC config
    public static String selectedNPCFaction = "Zombies";

    // NPC War config
    public static String npcFactionA = "Zombies";
    public static String npcFactionB = "Vampires";

    public static String[] getAvailableGameModes(boolean isMultiplayer) {
        return Arrays.stream(GameModeType.values())
                .map(GameModeType::getDisplayName)
                .filter(displayName -> !(isMultiplayer && displayName.equalsIgnoreCase("NPC WAR")))
                .toArray(String[]::new);
    }

    public static String[] getAvailableMaps() {
        if (currentMode == null) {
            currentMode = GameModeType.HUMAN_VS_NPC;
        }
        List<String> maps = switch (currentMode){
            case WAR -> NORMAL_MAPS;
            case HUMAN_VS_NPC, NPC_WAR -> NPC_MAPS;
            case SIEGE -> SIEGE_MAPS;
        };

        return maps.toArray(new String[0]);
    }

    public static void setCurrentGamemode(String modeName) {
        currentMode = GameModeType.fromName(modeName);
        setNewGamemodeColor();
    }

    public static String getCurrentGameModeAsString() {
        return currentMode.getDisplayName();
    }

    public static int getBoardCount(boolean fullOrOneTeam) {
        int baseCount = fullOrOneTeam ? playerCountFull : playerCountOneTeam;

        return switch (currentMode) {
            case WAR, SIEGE -> baseCount;
            case HUMAN_VS_NPC, NPC_WAR -> baseCount * 2;
        };
    }

    public static String getFactionMatchup() {
        return switch (currentMode) {
            case WAR -> "War";
            case HUMAN_VS_NPC -> "Humans vs " + selectedNPCFaction;
            case NPC_WAR -> npcFactionA + " vs " + npcFactionB;
            case SIEGE -> "Siege";
        };
    }

    public static void setNewGamemodeColor() {
        if (currentMode == GameModeType.WAR){
            currentGamemodeColor2 = Color.BLACK;
        } else if (currentMode == GameModeType.SIEGE){
            currentGamemodeColor2 = new Color(130, 10, 160);
        }
    }
}
