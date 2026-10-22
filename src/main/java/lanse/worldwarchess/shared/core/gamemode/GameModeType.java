package lanse.worldwarchess.shared.core.gamemode;

public enum GameModeType {
    WAR("War", "Two human factions fight each other."),
    HUMAN_VS_NPC("Human vs NPC", "One human faction fights an army of AI."),
    NPC_WAR("NPC War", "Two AI factions fight each other. \nNot allowed in multiplayer."),
    SIEGE("Siege", "One human faction fight endless waves of bosses. \nRequires a Siege Board.");
    private final String displayName;
    private final String description;

    GameModeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getDescription() {
        return description;
    }

    public static GameModeType fromName(String name) {
        for (GameModeType mode : values()) {
            if (mode.displayName.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return WAR; // default fallback
    }
}