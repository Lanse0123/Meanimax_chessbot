package lanse.worldwarchess.cascading;

import com.codedisaster.steamworks.SteamID;
import lanse.worldwarchess.shared.server.LobbyServer;

public class CascadingHost {

    //This class used to be big and strong, but now it is a very tiny class. The logic for getting messages was
    //moved to cascading client, and currently all it really does is hold the cascade structure.

    private final CascadeManager cascade;
    private final SteamID hostID;

    //these can probably be not static but like effort
    public static LobbyServer lobbyServer;
    public static int playerCount = 1;


    public CascadingHost(SteamID hostID, int conn) {
        this.cascade = new CascadeManager();
        cascade.addNewPlayer(hostID, conn);
        this.hostID = hostID;
    }

    public CascadeManager getCascadeManager() {
        return cascade;
    }

    public SteamID getHostID() {return hostID;}
}
