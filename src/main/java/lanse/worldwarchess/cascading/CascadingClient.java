package lanse.worldwarchess.cascading;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNativeHandle;
import com.codedisaster.steamworks.SteamNetworkingMessage;
import com.codedisaster.steamworks.SteamNetworkingSockets;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.server.LobbyServer;
import lanse.worldwarchess.steam.Account;
import lanse.worldwarchess.steam.SteamNetworkingManager;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class CascadingClient {

    //This class is you. Everyone who is in a server will have a cascading client.
    //It has logic for recieving new messages, and asking CascadePacketManager for what to do with it.

//    private static final int SEND_RELIABLE = 0;
//    private static final int SEND_UNRELIABLE = 1; // keep for reference if needed

    public static int playerID = -4040404;
    public static String team = "null";
    public static boolean isAliveInChat = true;
    public static LobbyServer lobbyServer;

    public static int connectToHost(SteamID serverID, Optional<String> team, Optional<SteamID> bossID) {
        SteamNetworkingSockets sockets = SteamNetworkingManager.getSockets();

        SteamNetworkingManager.addConnectedCallback(serverID, (SteamID server) -> {
            System.out.println("Sending JOIN");

            String content = (team.isPresent() && bossID.isPresent()) ? team + "," + bossID : "";

            CascadePacketManager.manager.sendTo(server, new CascadePacketManager.Packet(
                    CascadePacketManager.PacketType.JOIN, CascadePacketManager.PacketRange.SEND_TO, Account.username(), content));
        });

        int connection = sockets.connectP2P(SteamNativeHandle.getNativeHandle(serverID), 0);
        System.out.printf("[Client] Connecting to %s (handle %d)%n", serverID, connection);
        return connection;
    }

    public static void disconnectFromHost(){
        SteamID host = SteamNetworkingManager.host.getHostID();
        Integer hostConn = SteamNetworkingManager.getConnections().get(host);

        if (hostConn != null) {
            System.out.println("Disconnecting from host " + host);

            // Close the P2P connection. linger false = drop immediately
            SteamNetworkingManager.getSockets().closeConnection(hostConn, 0, false);

            // Remove from connection maps
            SteamNetworkingManager.getConnections().remove(host);
            SteamNetworkingManager.getRevConnections().remove(hostConn);
        }
    }

    public static void disconnectFromAllSockets(){
        SteamNetworkingManager.isPlayerTryingToLeave = true;

        try {
            var sockets = SteamNetworkingManager.getSockets();
            if (sockets != null) {

                for (Integer handle : SteamNetworkingManager.getRevConnections().keySet()) {
                    try {
                        sockets.closeConnection(handle, 0, true);
                    } catch (Exception e) {
                        System.err.println("Error closing connection " + handle + ": " + e.getMessage());
                    }
                }

                // Clear lookup tables
                SteamNetworkingManager.getConnections().clear();
                SteamNetworkingManager.getRevConnections().clear();

                // Clear CascadeTree connection handles
                if (SteamNetworkingManager.host != null && SteamNetworkingManager.host.getCascadeManager() != null) {

                    for (CascadeTree tree : SteamNetworkingManager.host.getCascadeManager().getTrees()) {

                        for (SteamID id : tree.getSteamIDsOnTree()) {
                            tree.setConnectionHandle(id, 0);
                        }
                    }
                }
            }
        } catch (Exception err) {
            System.err.println("Error while shutting down networking: " + err.getMessage());
        }

        SteamNetworkingManager.host = null;

        SteamNetworkingManager.isPlayerTryingToLeave = false;
    }

    public static void tick() {
        SteamNetworkingSockets sockets = SteamNetworkingManager.getSockets();
        if (sockets == null) return;

        for (int conn : SteamNetworkingManager.getConnections().values()) {
            SteamNetworkingMessage[] messages = {new SteamNetworkingMessage()};
            int count = sockets.receiveMessagesOnConnection(conn, messages);

            for (int i = 0; i < count; i++) {
                SteamNetworkingMessage msg = messages[i];
                if (msg == null || msg.getPayload() == null) continue;
                String text = new String(msg.getPayload(), StandardCharsets.UTF_8);
                System.out.println("[Client] Received: " + text);
                CascadePacketManager.manager.handleIncomingLine(text, conn);
            }
        }
    }

    public static void doMultiplayerTick() {
        SteamNetworkingManager.runCallBacks();

        if (!GameMode.isSinglePlayer) {
            CascadingClient.tick();
        }
    }
}