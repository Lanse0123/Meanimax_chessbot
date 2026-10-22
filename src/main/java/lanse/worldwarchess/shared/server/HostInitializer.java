package lanse.worldwarchess.shared.server;

import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.cascading.CascadingHost;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.screens.pregame.GameLobbyWindow;
import lanse.worldwarchess.steam.Account;

import javax.swing.*;
import java.util.Optional;

public class HostInitializer {

    public static void initializeAsHost(String name, String password, String questionText, int turnLength, String selectedMap,
                                        String gameMode, int maxPlayers, JFrame mainWindow, boolean isFrenzyGamemode,
                                        boolean isFogOfWar, String npcType) {

        GameMode.setCurrentGamemode(gameMode);
        GameMode.isFrenzy = isFrenzyGamemode;
        GameMode.isFogOfWar = isFogOfWar;

        // Connect client for local player
        Integer hostConn = CascadingClient.connectToHost(Account.getMySteamID(), Optional.empty(), Optional.empty());

        CascadingHost.lobbyServer = new LobbyServer();
        CascadingHost.lobbyServer.addServer(name, 1, maxPlayers, false,
                turnLength, selectedMap, gameMode, password, questionText, Account.getMySteamID(), isFrenzyGamemode, isFogOfWar, npcType,
                hostConn, (lobby, hostConn1) -> new GameLobbyWindow(lobby, hostConn1, mainWindow));
    }
}
