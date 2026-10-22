package lanse.worldwarchess.steam;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamApps;
import com.codedisaster.steamworks.SteamLibraryLoader;
import com.codedisaster.steamworks.SteamLibraryLoaderLwjgl3;

import java.io.File;

public class SteamVerification {

    //this initializes the steam library and initializes your steam account info.
    public static void init(boolean launchedFromSteam) {
        try {
            File codeSource = new File(SteamVerification.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            File steamDir;
            if (launchedFromSteam) {
                System.out.println("Steam!");
                String os = System.getProperty("os.name").toLowerCase();
                String steam_dir;
                if (os.contains("windows")) {
                    steam_dir = "..\\windows\\steam";
                } else if (os.contains("linux")) {
                    steam_dir = "../linux/steam";
                } else if (os.contains("mac")) {
                    steam_dir = "../mac/steam";
                } else {
                    System.err.println("Unsupported operating system: cannot load Steam!");
                    return;
                }
                steamDir = new File(codeSource.getParentFile(), steam_dir);
            } else {
                System.out.println("IntelliJ!");
                steamDir = new File("steam");
            }

            System.out.println(steamDir);

            SteamLibraryLoader loader = new SteamLibraryLoaderLwjgl3();

            loader.setLibraryPath(steamDir.getAbsolutePath());
            if (!SteamAPI.loadLibraries(loader)) {
                System.err.println("Failed to load libraries!");
                return;
            }

            if (!SteamAPI.init()) {
                System.err.println("Failed to initialize Steam API.");
                SteamAPI.printDebugInfo(System.err);
                return;
            }

            Account.init();

            boolean ownsGame = userOwnsGame();
            if (!ownsGame) {
                System.out.println("You do not own this game.");
                System.exit(0);
            }

            // Init networking once
            SteamNetworkingManager.init();
            SteamAchivementManager.init();
            SteamPlayerCountCallback.init();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean userOwnsGame() {
        SteamApps apps = new SteamApps();
        return apps.isSubscribedApp(SteamAppID.WORLD_WAR_CHESS_STEAM_ID);
    }
}
