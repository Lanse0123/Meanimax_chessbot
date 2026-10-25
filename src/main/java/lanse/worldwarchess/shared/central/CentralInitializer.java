package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.piece.ai.bosses.security.c.h.e.c.k.SecurityCheck;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.pregame.MainMenu;
import lanse.worldwarchess.shared.core.thread.ThreadNames;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CentralInitializer {

    //This is the main class but not in Main because people kept complaining.

    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void initializeGame(){
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());

        SecurityCheck.securityCheck("SECURITY CHECK");

        Settings.loadSettings();

        SwingUtilities.invokeLater(MainMenu::new);

        // Start server tick loop (10 times per second)
        scheduler.scheduleAtFixedRate(CentralTick::centralTick, 0, 100, TimeUnit.MILLISECONDS);

        // Load the textures once for everyone, but not in the UI thread
        TextureKey.loadTextures();
        AudioKey.loadSounds();

        MoveHistory.loadClass();

        Returner.clearGameData();

        ThreadNames.init();
    }
}
