package lanse.worldwarchess.shared.server;

import com.codedisaster.steamworks.*;
import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.steam.Account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public class LobbyServer implements SteamMatchmakingCallback {

    //This class is a listing of a server on the server list. It is also important for getting the initial data
    //of a server.

    private static SteamID curLobby;
    private static SteamMatchmaking matcher;
    private static Integer hostConn;
    private static BiConsumer<GameLobby, Integer> createdCallback;

    // Time is unix in ms, empty password means public server
    private String name;
    private List<String> players = new ArrayList<>();
    private int currentPlayerCount;
    private int maxPlayerCount;
    private boolean isGameOngoing;
    private int turnLength;
    private String mapType;
    private String gamemode;
    private String password;
    private String questionText;
    private SteamID hostSteamID;
    private boolean isFrenzyGamemode;
    private boolean isFogOfWarGamemode;
    private String npcType;

    @Override
    public void onLobbyCreated(SteamResult res, SteamID lobbyID) {
        if (res == SteamResult.OK) {
            curLobby = lobbyID;
            System.out.println("Created lobby " + curLobby);
            players.add(Account.username());
        }
        else {
            System.out.println("Error creating lobby: " + res);
        }

        if (matcher == null) {
            matcher = new SteamMatchmaking(this);
        }

        updateLobbyData();
        createdCallback.accept(new GameLobby(matcher, lobbyID, this), hostConn);
    }

    private void updateLobbyData() {
        matcher.setLobbyData(curLobby, "lobbyName", name);
        matcher.setLobbyData(curLobby, "currentPlayerCount", String.valueOf(currentPlayerCount));
        matcher.setLobbyData(curLobby, "maxPlayers", String.valueOf(maxPlayerCount));
        matcher.setLobbyData(curLobby, "turnLength", String.valueOf(turnLength));
        matcher.setLobbyData(curLobby, "mapType", mapType);
        matcher.setLobbyData(curLobby, "gamemode", gamemode);
        matcher.setLobbyData(curLobby, "npcFaction", npcType);
        matcher.setLobbyData(curLobby, "isFrenzyGamemode", String.valueOf(isFrenzyGamemode));
        matcher.setLobbyData(curLobby, "isFogOfWarGamemode", String.valueOf(isFogOfWarGamemode));
        matcher.setLobbyData(curLobby, "isGameOngoing", String.valueOf(isGameOngoing));
        matcher.setLobbyData(curLobby, "password", password);
        matcher.setLobbyData(curLobby, "questionText", questionText);
        matcher.setLobbyData(curLobby, "hostSteamID", String.valueOf(hostSteamID));
    }

    @Override
    public void onLobbyDataUpdate(SteamID steamIDLobby, SteamID steamIDMember, boolean success) {
        SteamMatchmakingCallback.super.onLobbyDataUpdate(steamIDLobby, steamIDMember, success);
        updateLobbyData();
    }

    public boolean addServer(String name, int currentPlayerCount, int maxPlayerCount, boolean isGameOngoing,
                             int turnLength, String mapType, String gamemode, String password, String questionText, SteamID hostSteamID,
                             boolean isFrenzyGamemode, boolean isFogOfWarGamemode, String npcType,
                             Integer hostConn, BiConsumer<GameLobby, Integer> createdCallback) {

        LobbyServer.createdCallback = createdCallback;
        LobbyServer.hostConn = hostConn;
        this.name = name;
        this.currentPlayerCount = currentPlayerCount;
        this.maxPlayerCount = maxPlayerCount;
        this.isGameOngoing = isGameOngoing;
        this.turnLength = turnLength;
        this.mapType = mapType;
        this.gamemode = gamemode;
        this.password = password;
        this.questionText = questionText;
        this.hostSteamID = hostSteamID;
        this.isFrenzyGamemode = isFrenzyGamemode;
        this.isFogOfWarGamemode = isFogOfWarGamemode;
        this.npcType = npcType;
        matcher = new SteamMatchmaking(this);
        SteamAPICall addResult = matcher.createLobby(SteamMatchmaking.LobbyType.Public, maxPlayerCount);
        System.out.println(addResult.isValid());
        return addResult.isValid();
    }

    public void setLobbyData(String key, String value) {
        matcher.setLobbyData(curLobby, key, value);
    }
    
    public void editPlayerCount(int newPlayerCount){
        setLobbyData("currentPlayerCount", String.valueOf(newPlayerCount));
        this.currentPlayerCount = newPlayerCount;
    }

    public void setPlayers(Collection<String> players) {
        this.players.clear();
        this.players.addAll(players);
    }

    public void addPlayer(String username) {
        this.players.add(username);
    }

    public void removePlayer(String username) {
        this.players.remove(username);
    }

    public List<String> playerList() {
        return this.players;
    }

    public int getPlayerCount() {
        return this.currentPlayerCount;
    }

    public int getMaxPlayerCount() {
        return this.maxPlayerCount;
    }

    public static void leaveCurrentLobby() {
        if (matcher != null) {
            matcher.leaveLobby(curLobby);
        }
        matcher = null;

        CascadingClient.disconnectFromAllSockets();
    }
}