package lanse.worldwarchess.steam;

import com.codedisaster.steamworks.*;
import lanse.worldwarchess.cascading.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class SteamNetworkingManager {

    private static SteamNetworkingSockets sockets;
    private static final ConcurrentMap<SteamID, Integer> connections = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, SteamID> rev_connections = new ConcurrentHashMap<>();
    private static final Map<SteamID, Consumer<SteamID>> connectedCallbacks = new HashMap<>();
    public static boolean isPlayerTryingToLeave = false;
    public static CascadingHost host;

    public static void initHost(CascadingHost host) {
        SteamNetworkingManager.host = host;
        sockets.createListenSocketP2P(0);
    }

    public static void init() {
        try {
            //This will mainly be called in a callback. When you use connectP2P, it will come here, and try to
            //connect to the other person. Once it does, it can call logic from one of these switch cases.
            sockets = new SteamNetworkingSockets((handle, peerID, newState, oldState) -> {
                SteamID peerSteamID = SteamID.createFromNativeHandle(peerID);
                SteamNetworkingSocketsCallback.ConnectionState state = SteamNetworkingSocketsCallback.ConnectionState.fromValue(newState);
                System.out.printf("[Networking] Connection status change: peerID=%d old=%d new=%d%n",
                        peerID, oldState, newState);

                switch (state) {
                    case CONNECTING -> {
                        if (sockets.acceptConnection(handle) == SteamResult.OK) {
                            System.out.println("Accepting connection from peer " + peerID);
                        }
                    }

                    case FINDING_ROUTE -> System.out.println("Connection being established...");

                    //WE WANT THIS CASE (OBVIOUSLY)
                    case CONNECTED -> {
                        connections.put(peerSteamID, handle);
                        rev_connections.put(handle, peerSteamID);
                        System.out.printf("[Networking] Connected (peerID %d)%n", peerID);

                        //update the connection list when it connects
                        if (SteamNetworkingManager.host != null) {
                            for (CascadeTree tree : SteamNetworkingManager.host.getCascadeManager().getTrees()) {
                                CascadeNode node = tree.getNode(peerSteamID);
                                if (node != null) {
                                    tree.setConnectionHandle(peerSteamID, handle);
                                }
                            }
                        }
                        runConnectedCallbacks(peerSteamID);
                    }

                    case CLOSED_BY_PEER, PROBLEM_DETECTED_LOCALLY -> {
                        sockets.closeConnection(handle, newState, false);
                        connections.remove(peerSteamID);
                        rev_connections.remove(handle);
                        tryReconnectingToHost(peerSteamID, peerID);
                        System.out.printf("[Networking] Connection to %d closed, or problem occured%n", peerID);
                    }

                    default -> { //This is called when a player leaves. (I think)
                        tryReconnectingToHost(peerSteamID, peerID);
                        System.err.println("Unknown new state: " + newState);
                    }
                }
            });
            sockets.initRelayNetworkAccess();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    //This function will be a problem if you try using a new object as a callback, and it will segfault.
    public static void runCallBacks() { SteamAPI.runCallbacks(); }

    public static SteamNetworkingSockets getSockets() {
        return sockets;
    }

    public static void tryReconnectingToHost(SteamID peerSteamID, long peerID){
        // If the player is not trying to leave, and we exist, and we arent currently connected to the host,
        // then try connecting to the host.
        if (!isPlayerTryingToLeave && CascadePacketHandler.cascadePacketManager.us.isPresent()){

            SteamID hostID = host.getHostID();

            if (connections.containsKey(hostID) || rev_connections.containsValue(hostID)) return;

            if (CascadePacketHandler.cascadePacketManager.us.get().getParent().getId().equals(peerSteamID)) {
                CascadingClient.connectToHost(hostID, Optional.of(CascadingClient.team), Optional.of(SteamID.createFromNativeHandle(peerID)));
            }
        }
    }

    public static ConcurrentMap<SteamID, Integer> getConnections() {
        return connections;
    }

    public static ConcurrentMap<Integer, SteamID> getRevConnections() {
        return rev_connections;
    }

    private static void runConnectedCallbacks(SteamID peerID) {
        try {
            if (connectedCallbacks.containsKey(peerID)) {
                connectedCallbacks.get(peerID).accept(peerID);
                connectedCallbacks.remove(peerID);
            }
        } catch (Exception e) {
            System.out.println("Exception in callback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addConnectedCallback(SteamID peer, Consumer<SteamID> callback) {
        connectedCallbacks.put(peer, callback);
    }

    public static SteamID parseSteamIDString(String id) {
        if (id == null || id.isBlank()) return new SteamID();
        long idValue;
        try {
            idValue = Long.parseUnsignedLong(id.trim(), 16);
        } catch (NumberFormatException hexEx) {
            try {
                // fallback to decimal
                idValue = Long.parseLong(id.trim());
            } catch (NumberFormatException decEx) {
                throw new IllegalArgumentException("Invalid SteamID string: " + id);
            }
        }
        return SteamID.createFromNativeHandle(idValue);
    }
}