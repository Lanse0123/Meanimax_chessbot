package lanse.worldwarchess.cascading;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNativeHandle;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.chat.ChatManager;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.move.MoveDecoder;
import lanse.worldwarchess.shared.core.screens.pregame.GameLobbyWindow;
import lanse.worldwarchess.shared.core.worldmap.MultiplayerTeamIDBuilder;
import lanse.worldwarchess.shared.core.worldmap.ParallelUniverseHandler;
import lanse.worldwarchess.shared.utils.StringUtils;
import lanse.worldwarchess.steam.Account;
import lanse.worldwarchess.steam.SteamNetworkingManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import static lanse.worldwarchess.cascading.CascadePacketManager.manager;
import static lanse.worldwarchess.shared.chat.ChatManager.MAX_MSG_LEN;

public class CascadePacketHandler {

    //This class is responsible for figuring out what to do with a packet.

    public static CascadePacketManager cascadePacketManager;

    private static boolean isFraudulentSender(SteamID senderID) {
        return (cascadePacketManager.us.isEmpty() || !cascadePacketManager.us.get().getParent().getId().equals(senderID)) &&
                SteamNetworkingManager.host.getCascadeManager().getTrees().stream().noneMatch(
                        (tree) -> tree.getRoot().getId().equals(senderID));
    }

    public Optional<CascadePacketManager.Packet> handleChatMessage(CascadePacketManager.Packet pkt, int specialtyLevel) {
        if (pkt.content().length() > MAX_MSG_LEN) {
            pkt = new CascadePacketManager.Packet(pkt.type(), pkt.range(), pkt.originName(), StringUtils.abbreviateString(pkt.content()));
        }
        ChatManager.handleChatMessage(pkt.content(), pkt.originName(), specialtyLevel);
        return Optional.of(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleMove(CascadePacketManager.Packet pkt, SteamID senderID) {
        return MoveDecoder.decodeMove(pkt, senderID);
    }

    public Optional<CascadePacketManager.Packet> handlePromotion(CascadePacketManager.Packet pkt) {
        return MoveDecoder.decodePromotion(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleSpawn(CascadePacketManager.Packet pkt, SteamID senderID) {
        if (isFraudulentSender(senderID)) return Optional.empty();
        return MoveDecoder.decodeSpawn(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleKill(CascadePacketManager.Packet pkt) {
        return MoveDecoder.decodeKill(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleErase(CascadePacketManager.Packet pkt, boolean eraseWholeID) {
        return MoveDecoder.decodeErase(pkt, eraseWholeID);
    }

    public Optional<CascadePacketManager.Packet> handleLeave(CascadePacketManager.Packet pkt) {
        if (Main.userIsHost){
            CascadingHost.playerCount--;
            if (CascadingHost.lobbyServer != null) {
                CascadingHost.lobbyServer.editPlayerCount(CascadingHost.playerCount);
                CascadingHost.lobbyServer.removePlayer(pkt.originName());
            }
        }

        // If the chat message is invalidly long, send a new packet with it truncated to a valid length
        if (pkt.content().length() > MAX_MSG_LEN) {
            pkt = new CascadePacketManager.Packet(pkt.type(), pkt.range(), pkt.originName(), StringUtils.abbreviateString(pkt.content()));
        }
        ChatManager.handleChatMessage(pkt.content(), pkt.originName(), 1);
        return Optional.of(pkt);
        //TODO - update our tree if necessary

        //TODO - this never gets called if player forcibly quits, so make sure the host displays
        // that the player left as a sendToEveryone.
    }

    public Optional<CascadePacketManager.Packet> handleStartGame(CascadePacketManager.Packet pkt, SteamID senderID) {
        //TODO - maybe make use of the packet if it has data? Currently I am sending playerCount as content()
        if (isFraudulentSender(senderID)) return Optional.empty();
        GameLobbyWindow.startGame();
        return Optional.of(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleGetPlayerCount(SteamID sender, CascadePacketManager.Packet pkt) {
        if (manager.us.isPresent() && manager.us.get().getParent() != null) {
            cascadePacketManager.sendTo(manager.us.get().getParent().getId(), new CascadePacketManager.Packet
                    (CascadePacketManager.PacketType.GETPLAYERCOUNT, CascadePacketManager.PacketRange.UP, Account.username(), ""));
        }
        else {
            CascadeManager.playerCount -= 1;
            for (CascadeTree tree : SteamNetworkingManager.host.getCascadeManager().getTrees()) {
                if (!tree.getRoot().getId().equals(Account.getMySteamID())) {
                    cascadePacketManager.sendTo(tree.getRoot().getId(), pkt);
                }
                else {
                    cascadePacketManager.sendDown(pkt, Optional.of(sender));
                }
            }
        }
        return Optional.of(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleJoin(int conn, SteamID playerID, CascadePacketManager.Packet pkt) {
        if (!Main.userIsHost) return Optional.empty();

        // if this is the first time player is joining:
        if (pkt.content().isEmpty()) {
            CascadeManager.acceptJoiningPlayer(playerID, SteamNetworkingManager.host.getCascadeManager(),
                    SteamNetworkingManager.getSockets(), conn, 1);

            CascadingHost.playerCount++;
            if (CascadingHost.lobbyServer != null) {
                CascadingHost.lobbyServer.editPlayerCount(CascadingHost.playerCount);
                CascadingHost.lobbyServer.addPlayer(Account.username(playerID));
                manager.sendPacketToEveryone(CascadePacketManager.PacketType.PLAYER_LIST, String.join(",", CascadingHost.lobbyServer.playerList()), Optional.empty());
            }

            return Optional.empty();
        }

        //TODO - I would be shocked if the code under this TODO works. SHOCKED!

        // otherwise, this is a rejoin attempt.
        try {
            String content = pkt.content();
            String[] parts = content.split(",");
            String team = parts[0];
            long bossSteamIdLong = Long.parseLong(parts[1]);
            SteamID bossSteamID = SteamID.createFromNativeHandle(bossSteamIdLong);

            CascadeManager cascade = SteamNetworkingManager.host.getCascadeManager();

            // TODO - make this way better and scalable.
            CascadeTree teamTree =
                    team.equalsIgnoreCase("White") ? cascade.getTrees().stream()
                            .filter(t -> t.getRoot() != null && cascade.getTrees().indexOf(t) == 0).findFirst().orElse(null)
                            : cascade.getTrees().stream()
                            .filter(t -> t.getRoot() != null && cascade.getTrees().indexOf(t) == 1).findFirst().orElse(null);

            //TODO - make sure it also checks if root of that tree is null.

            // If team tree doesn't exist yet, create it by forcing boss to become root.
            if (teamTree == null) {
                System.out.printf("[Join] Team %s does not exist; creating root %s%n", team, bossSteamID);
                cascade.addNewPlayer(bossSteamID, conn);
                teamTree = cascade.getTrees().stream()
                        .filter(t -> t.getNode(bossSteamID) != null)
                        .findFirst().orElse(null);
            }

            // Check if the boss node exists
            assert teamTree != null;
            CascadeNode bossNode = teamTree.getNode(bossSteamID);

            if (bossNode == null) {
                // Boss disappeared => they left the game (but maybe no leave packet was processed)
                System.out.printf("[Join] Boss %s not found. Treating boss as disconnected.%n", bossSteamID);

                // Notify everyone that the boss left (if not already reported)
                //TODO - make sure this doesn't double send
                CascadePacketManager.manager.sendPacketToEveryone(
                        CascadePacketManager.PacketType.LEAVE, bossSteamID.toString(), Optional.empty());

                CascadeManager.acceptJoiningPlayer(playerID, cascade, SteamNetworkingManager.getSockets(), conn, 1);

                return Optional.empty();
            }

            System.out.printf("[Join] %s rejoining under boss %s on team %s%n", playerID, bossSteamID, team);

            cascade.addUnderBoss(playerID, conn, bossSteamID, teamTree.getConnectionHandle(bossSteamID));

            CascadePacketManager.manager.sendTo(playerID, new CascadePacketManager.Packet(
                            CascadePacketManager.PacketType.ASSIGN_BOSS,
                            CascadePacketManager.PacketRange.SEND_TO,
                            Account.username(), bossSteamID.toString())
            );

            return Optional.empty();

        } catch (Exception e) {
            System.err.println("[Join Error] " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    //When you are assigned a boss, you will send a message saying you joined, and you will do logic to either
    // stay connected to the host boss, or cascade down to a lower boss, which is potentially on a different
    // tree, and then you will disconnect from the host if not connected directly to not overwhelm him.
    public Optional<CascadePacketManager.Packet> handleHavingBeenAssignedBoss(int conn, CascadePacketManager.Packet pkt) {
        // called when this client is told "this is your boss"
        Consumer<Void> onJoined = (ignored) -> {
            manager.us = SteamNetworkingManager.host.getCascadeManager().getUs();
            ChatManager.handleChatMessage(Settings.getJoinMessage(), Account.username(), 1);
            cascadePacketManager.sendPacketToEveryone(
                    CascadePacketManager.PacketType.SPECIAL_CHAT,
                    Settings.getJoinMessage(),
                    Optional.empty()
            );
        };

        SteamID bossID;
        try {
            bossID = SteamNetworkingManager.parseSteamIDString(pkt.content());
        } catch (IllegalArgumentException ex) {
            System.err.println("[Cascading] Bad boss id in packet: '" + pkt.content() + "'");
            return Optional.empty();
        }

        SteamID myID = Account.getMySteamID();
        SteamID hostID = SteamNetworkingManager.host.getHostID();

        // If the boss is me, I'm top of my subtree (no extra P2P needed)
        if (bossID.equals(myID)) {
            SteamNetworkingManager.host.getCascadeManager().addUnderBoss(bossID, 0, bossID, 0);
            onJoined.accept(null);
            return Optional.empty();
        }

        // If the boss is the global host (server), keep connection to host
        if (bossID.equals(hostID)) {
            // conn here is the connection handle to the host (assigner)
            SteamNetworkingManager.host.getCascadeManager().addUnderBoss(myID, 0, bossID, conn);
            onJoined.accept(null);
            return Optional.empty();
        }

        // Otherwise we must connect to the new boss p2p
        // Add a callback that will be triggered when connection to bossID becomes available.
        SteamNetworkingManager.addConnectedCallback(bossID, (SteamID connectedBoss) -> {
            // Look up the connection handle to the boss in the connections map
            Integer bossConnHandle = SteamNetworkingManager.getConnections().get(connectedBoss);
            if (bossConnHandle == null) {
                System.err.printf("[Cascading] connected callback fired but no connection handle for %s%n", connectedBoss);
                return;
            }

            // Find our own connection handle in that table (if any). If not present, pass 0 (unknown).
            Integer myConnHandle = SteamNetworkingManager.getConnections().get(myID);
            if (myConnHandle == null) {
                myConnHandle = 0; // we may not have a dedicated connection handle mapping for ourselves
            }

            // Now register the tree relation using the correct boss connection handle
            SteamNetworkingManager.host.getCascadeManager().addUnderBoss(
                    myID, myConnHandle, connectedBoss, bossConnHandle
            );

            // Tell the boss we are their new employee (payload should include who we are)
            // Create a packet payload containing our native handle (in hex to be safe)
            String myNativeHex = Long.toHexString(SteamNativeHandle.getNativeHandle(myID));
            CascadePacketManager.Packet newEmployeePacket = new CascadePacketManager.Packet(
                    CascadePacketManager.PacketType.NEW_EMPLOYEE,
                    CascadePacketManager.PacketRange.SEND_TO,
                    pkt.originName(), myNativeHex
            );
            cascadePacketManager.sendTo(connectedBoss, newEmployeePacket);

            onJoined.accept(null);

            // If the boss we just connected to is not the global host, we can disconnect
            // from the host to avoid overwhelming it
            // Only do this if we actually have a connection to the host right now.
            SteamID currentHostId = SteamNetworkingManager.host.getHostID();
            if (!connectedBoss.equals(currentHostId)) {
                Integer hostConnHandle = SteamNetworkingManager.getConnections().get(currentHostId);
                if (hostConnHandle != null) {
                    System.out.printf("[Cascading] Connected to boss %s (handle %d). Disconnecting from host %s.%n",
                            connectedBoss, bossConnHandle, currentHostId);
                    CascadingClient.disconnectFromHost();
                } else {
                    System.out.printf("[Cascading] Connected to boss %s but no active host connection found; nothing to disconnect.%n",
                            connectedBoss);
                }
            }
        });

        // Kick off the connect (async). Use native handle for the boss SteamID.
        long bossNative = SteamNativeHandle.getNativeHandle(bossID);
        int attemptedConn = SteamNetworkingManager.getSockets().connectP2P(bossNative, 0);
        System.out.printf("[Cascading] Attempted connectP2P to boss=%s native=%x handle=%d%n",
                bossID, bossNative, attemptedConn);

        return Optional.empty();
    }

    public Optional<CascadePacketManager.Packet> handleNewEmployee(SteamID newEmployee, int conn, CascadePacketManager.Packet pkt) {
        SteamNetworkingManager.host.getCascadeManager().addUnderBoss(newEmployee, conn, Account.getMySteamID(), 0);

        return Optional.of(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleAssignPlayerID(CascadePacketManager.Packet pkt) {
        String[] parts = pkt.content().split(",");
        if (parts.length != 2) {
            System.err.println("Invalid PLAYER_ID packet: " + pkt);
            return Optional.empty();
        }
        MultiplayerTeamIDBuilder.assignTeams(SteamID.createFromNativeHandle(Long.parseLong(parts[0], 16)), Integer.parseInt(parts[1]));

        return Optional.of(pkt);
    }

    public Optional<CascadePacketManager.Packet> handlePacketBundle(CascadePacketManager.Packet pkt, int conn, SteamID senderID) {
        String content = pkt.content();
        String[] lines = content.split("\n");
        ArrayList<CascadePacketManager.Packet> packets = new ArrayList<>();

        for (String line : lines) {
            if (!line.isBlank()) {
                try {
                    packets.add(CascadePacketManager.Packet.deserialize(line));
                } catch (Exception e) {
                    System.err.println("[CascadePacketManager] Failed to parse bundled packet: " + line);
                    e.printStackTrace();
                }
            }
        }

        CascadePacketBundler new_bundle = new CascadePacketBundler(pkt.range());

        for (CascadePacketManager.Packet packet : packets) {
            cascadePacketManager.handlePacket(packet, conn, senderID).ifPresent(new_bundle::addPacket);
        }

        if (new_bundle.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new_bundle.getPacketBundle());
    }

    public Optional<CascadePacketManager.Packet> handleSkipTurn(SteamID senderID, CascadePacketManager.Packet pkt) {
        if (isFraudulentSender(senderID)) return Optional.empty();
        Timer.currentTime = 0;
        Timer.skipTurn = true;
        return Optional.of(pkt);
    }

    public Optional<CascadePacketManager.Packet> handlePlayerList(SteamID senderID, CascadePacketManager.Packet pkt) {
        if (isFraudulentSender(senderID)) return Optional.empty();
        CascadingClient.lobbyServer.setPlayers(Arrays.stream(pkt.content().split(",")).toList());
        return Optional.of(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleCustomMap(SteamID senderID, CascadePacketManager.Packet pkt) {
        //TODO - the content of the packet should be the content of the json. It can be loaded the same way
        // that it is loaded in sandbox normally. Then also make sure to store this somewhere so if the user
        // wants to download the map, they can.
        return Optional.of(pkt);
    }

    public Optional<CascadePacketManager.Packet> handleParallelUniverseRequest(SteamID senderID, CascadePacketManager.Packet pkt) {
        ParallelUniverseHandler.handleParallelUniverse(Integer.parseInt(pkt.content()), senderID);
        return Optional.empty();
    }

    public Optional<CascadePacketManager.Packet> handleParallelUniverseFix(SteamID senderID, CascadePacketManager.Packet pkt) {
        ParallelUniverseHandler.decodeUniverse(pkt);
        return Optional.empty();
    }
}
