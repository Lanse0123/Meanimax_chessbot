package lanse.worldwarchess.steam;

import com.codedisaster.steamworks.*;

public class SteamPlayerCountCallback implements SteamUserStatsCallback {

    private static SteamUserStats userStats;
    public static int globalPlayercount = -1;

    public static void init() {
        userStats = new SteamUserStats(new SteamUserStatsCallback() {
            @Override
            public void onNumberOfCurrentPlayersReceived(boolean success, int players) {
                if (success) {
                    globalPlayercount = players;
                    System.out.println("Global players online: " + players);
                }
            }
        });
    }

    public static void updatePlayerCount() {
        if (userStats != null) {
            userStats.getNumberOfCurrentPlayers();
        }
    }
}