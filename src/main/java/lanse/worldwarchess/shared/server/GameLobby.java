package lanse.worldwarchess.shared.server;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class GameLobby {

    //Kind of like a converter class for LobbyServer.

    private final SteamMatchmaking networkedLobby;
    private final SteamID lobbyID;
    private final String lobbyName;
    private int currentPlayerCount;
    private final int maxPlayers;
    private final SteamID serverHostID;
    private final double turnLength;
    private final String mapType;
    private final String gamemode;
    private final boolean isFrenzyGamemode;
    private final boolean isFogOfWarGamemode;
    private final boolean isGameOngoing;
    private final String password;
    private final String questionText;
    private final List<String> currentPlayers;
    private final String npcFaction;

    private Optional<String> chosenTeam;

    public GameLobby(SteamMatchmaking lobbyFinder, SteamID lobby, LobbyServer lobbyServer) {
        this.networkedLobby = lobbyFinder;
        this.lobbyID = lobby;
        this.serverHostID = SteamID.createFromNativeHandle(
                Long.parseUnsignedLong(lobbyFinder.getLobbyData(lobby, "hostSteamID"), 16));
        this.currentPlayers = lobbyServer.playerList();
        this.lobbyName = lobbyFinder.getLobbyData(lobby, "lobbyName");
        this.currentPlayerCount = Integer.parseInt(lobbyFinder.getLobbyData(lobby, "currentPlayerCount"));
        this.maxPlayers = Integer.parseInt(lobbyFinder.getLobbyData(lobby, "maxPlayers"));
        this.turnLength = Integer.parseInt(lobbyFinder.getLobbyData(lobby, "turnLength"));
        this.mapType = lobbyFinder.getLobbyData(lobby, "mapType");
        this.gamemode = lobbyFinder.getLobbyData(lobby, "gamemode");
        this.isFrenzyGamemode = Boolean.parseBoolean(lobbyFinder.getLobbyData(lobby, "isFrenzyGamemode"));
        this.isFogOfWarGamemode = Boolean.parseBoolean(lobbyFinder.getLobbyData(lobby, "isFogOfWarGamemode"));
        this.isGameOngoing = Boolean.parseBoolean(lobbyFinder.getLobbyData(lobby, "isGameOngoing"));
        this.password = lobbyFinder.getLobbyData(lobby, "password");
        this.questionText = lobbyFinder.getLobbyData(lobby, "questionText");
        this.npcFaction = lobbyFinder.getLobbyData(lobby, "npcFaction");
        this.chosenTeam = Optional.empty();
    }

    public int getCurrentPlayerCount() { return currentPlayerCount; }
    //TODO - make this return the list of player names in the server
    public List<String> getCurrentPlayers() { return currentPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public SteamID getServerHostID() { return serverHostID; }
    public double getTurnLength() { return turnLength; }
    public String getMapType() { return mapType; }
    public String getGamemode() { return gamemode; }
    public boolean getIsFrenzyGamemode() { return isFrenzyGamemode; }
    public boolean getIsFogOfWarGamemode() { return isFogOfWarGamemode; }
    public boolean isGameOngoing() { return isGameOngoing; }
    public String getPassword() { return password; }
    public SteamID getLobbyID() {
        return this.lobbyID;
    }
    public String getLobbyName() { return this.lobbyName; }
    public String getNpcFaction() { return this.npcFaction; }
    public Optional<String> getChosenTeam() { return this.chosenTeam; }

    //TODO - This can probably be a LOT more secure...
    public boolean joinLobby() {
        if (currentPlayerCount < maxPlayers) {
            if (!password.isEmpty()) {
                String userInput = promptPassword(lobbyName);
                if (userInput == null || !userInput.equals(password)) {
                    JOptionPane.showMessageDialog(null, "Incorrect password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            if (!questionText.isEmpty()) {
                this.chosenTeam = Optional.of(promptQuestion(lobbyName, questionText));
            }
            networkedLobby.joinLobby(this.lobbyID);
            currentPlayerCount++;
            return true;
        }
        return false;
    }

    private static String promptPassword(String lobbyName) {
        JPasswordField field = new JPasswordField();
        Object[] message = {"Enter password for lobby \"" + lobbyName + "\":", field};

        int option = JOptionPane.showConfirmDialog(
                null, message, "Private Lobby", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            return new String(field.getPassword());
        }
        return null;
    }

    private static String promptQuestion(String lobbyName, String questionText) {
        switch (JOptionPane.showConfirmDialog(null, questionText, "Answer this question to choose your team.", JOptionPane.YES_NO_OPTION)) {
            case JOptionPane.YES_OPTION -> {
                return "white";
            }
            case JOptionPane.NO_OPTION -> {
                return "black";
            }
            default -> throw new RuntimeException("Invalid response from JOptionPane.showConfirmDialog!");
        }
    }

    @Override
    public String toString() {
        //TODO - make pretty
        return lobbyName + " (" + currentPlayerCount + "/" + maxPlayers + ") | " +
                "Map: " + mapType + ", Mode: " + gamemode + ", Status: " +
                (isGameOngoing ? "Ongoing" : "Waiting") +
                (password.isEmpty() ? " [Public]" : " [Private]");
    }
}
