package lanse.worldwarchess.shared.core.screens.background;

import lanse.worldwarchess.shared.server.GameLobby;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.Objects;

public class MapPreview {

    private static final String presetPath = "/images/mapPreset/";

    public static Image getMapPreview(GameLobby lobby) {
        String map = lobby.getMapType().toLowerCase();

        try {
            return switch (map){
                case "diamond" -> ImageIO.read(Objects.requireNonNull(MapPreview.class.getResource(presetPath + "DiamondPreset.png")));
                case "hourglass" -> ImageIO.read(Objects.requireNonNull(MapPreview.class.getResource(presetPath + "HourglassPreset.png")));
                case "britisharmy1812" -> ImageIO.read(Objects.requireNonNull(MapPreview.class.getResource(presetPath + "BritishArmyPreset.png")));
                case "infernocrossing" -> ImageIO.read(Objects.requireNonNull(MapPreview.class.getResource(presetPath + "InfernoCrossingPreset.png")));
                case "hypothermic" -> ImageIO.read(Objects.requireNonNull(MapPreview.class.getResource(presetPath + "HypothermicPreset.png")));
                case "commanderdebug1" -> ImageIO.read(Objects.requireNonNull(MapPreview.class.getResource(presetPath + "CommanderDebugPreset.png")));
                case "siege" -> ImageIO.read(Objects.requireNonNull(MapPreview.class.getResource(presetPath + "SiegePreset.png")));
                default -> ImageIO.read(Objects.requireNonNull(MapPreview.class.getResource(presetPath + "OtherPreset.png")));
            };

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
