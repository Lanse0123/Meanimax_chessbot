package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.FunFactGenerator;
import lanse.worldwarchess.shared.core.piece.PieceClassPrefixMap;
import lanse.worldwarchess.shared.core.piece.ai.bosses.security.c.h.e.c.k.SecurityCheck;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.pregame.JavaFXInitialization;
import lanse.worldwarchess.shared.core.screens.pregame.MainMenu;
import lanse.worldwarchess.shared.core.screens.sandbox.SandboxMenu;
import lanse.worldwarchess.shared.core.worldmap.board.BoardClassPrefixMap;
import lanse.worldwarchess.steam.SteamVerification;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CentralInitializer {

    //This is the main class but not in Main because people kept complaining.

    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void initializeGame(boolean launchedFromSteam){
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());

        if (Main.debugMode) new KotlinTestFile().run();

        SecurityCheck.securityCheck("SECURITY CHECK");

        SteamVerification.init(launchedFromSteam);

        PieceClassPrefixMap.initPrefixMap();
        BoardClassPrefixMap.initPrefixMap();
        CascadePacketManager.initPacketNames();

        Settings.loadSettings();

        SwingUtilities.invokeLater(() -> {
            new MainMenu();
            JavaFXInitialization.initJavaFXToolkit();
        });

//        try {
            // Set cross-platform Java L&F (also called "Metal")
//            UIManager.setLookAndFeel(
//                    UIManager.getCrossPlatformLookAndFeelClassName());
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }

        // Start server tick loop (10 times per second)
        scheduler.scheduleAtFixedRate(CentralTick::centralTick, 0, 100, TimeUnit.MILLISECONDS);

        // Load the textures once for everyone, but not in the UI thread
        TextureKey.loadTextures();
        AudioKey.loadSounds();

        MoveHistory.loadClass();
        SandboxMenu.loadClass();

        Returner.clearGameData();

        FunFactGenerator.initializeFunFacts();
    }
}
