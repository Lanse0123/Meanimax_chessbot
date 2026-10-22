package lanse.worldwarchess.shared.core.screens.sandbox;

import java.nio.file.Path;

public class CustomMapPreset {

    public static Path mapPath;

    public static void loadMap() {
        try {
            SandboxMenu.loadMapFromFile(mapPath, new SandboxMenu(null, false));
        } catch (Exception ignored){}

        //TODO - If you are host, send the map data to everyone now that it was made?
    }
}
