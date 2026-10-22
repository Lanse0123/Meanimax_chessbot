package lanse.worldwarchess.cascading;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;
import lanse.worldwarchess.shared.utils.BiDirectionalMap;
import lanse.worldwarchess.steam.Account;
import lanse.worldwarchess.steam.SteamNetworkingManager;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class CascadePacketManager {

    //This class is entirely responsible for sending packets of all types up and down the tree, and to other trees.
    //It is also responsible for calling logic depending on what packet type was received.

    public static CascadePacketManager manager;
    public static CascadePacketHandler cascadePacketHandler;

    public enum PacketType { CHAT, MOVE, PROMOTE, SPAWN, KILL, ERASE, ERASE_ID, LEAVE, START,
        GETPLAYERCOUNT, JOIN, ASSIGN_BOSS, NEW_EMPLOYEE, PLAYER_ID, BUNDLE, SKIP_TURN, SPECIAL_CHAT,
        SUPER_SPECIAL_CHAT, PLAYER_LIST, CUSTOM_MAP, PARALLEL_UNIVERSE_REQUEST, PARALLEL_UNIVERSE_FIX }

    private static final BiDirectionalMap<PacketType, String> packetNames = new BiDirectionalMap<>();


    public enum PacketRange {
        EVERYONE, ENTIRE_TREE, UP, DOWN, SEND_TO
    }
    private static final BiDirectionalMap<PacketRange, String> rangeNames = new BiDirectionalMap<>();

    public static void initPacketNames() {
        cascadePacketHandler = new CascadePacketHandler();
        CascadePacketHandler.cascadePacketManager = new CascadePacketManager();

        packetNames.put(PacketType.CHAT, "C");
        packetNames.put(PacketType.SPECIAL_CHAT, "SC");
        packetNames.put(PacketType.SUPER_SPECIAL_CHAT, "SSC");
        packetNames.put(PacketType.MOVE, "M");
        packetNames.put(PacketType.PROMOTE, "P");
        packetNames.put(PacketType.SPAWN, "S");
        packetNames.put(PacketType.KILL, "K");
        packetNames.put(PacketType.ERASE, "E");
        packetNames.put(PacketType.ERASE_ID, "EI");
        packetNames.put(PacketType.LEAVE, "L");
        packetNames.put(PacketType.START, "ST");
        packetNames.put(PacketType.GETPLAYERCOUNT, "G");
        packetNames.put(PacketType.JOIN, "J");
        packetNames.put(PacketType.ASSIGN_BOSS, "A");
        packetNames.put(PacketType.NEW_EMPLOYEE, "N");
        packetNames.put(PacketType.PLAYER_ID, "PI");
        packetNames.put(PacketType.BUNDLE, "B");
        packetNames.put(PacketType.SKIP_TURN, "SK");
        packetNames.put(PacketType.PLAYER_LIST, "PL");
        packetNames.put(PacketType.CUSTOM_MAP, "CM");
        packetNames.put(PacketType.PARALLEL_UNIVERSE_REQUEST, "PUR");
        packetNames.put(PacketType.PARALLEL_UNIVERSE_FIX, "PUF");

        rangeNames.put(PacketRange.EVERYONE, "E");
        rangeNames.put(PacketRange.ENTIRE_TREE, "T");
        rangeNames.put(PacketRange.UP, "U");
        rangeNames.put(PacketRange.DOWN, "D");
        rangeNames.put(PacketRange.SEND_TO, "N");
    }

    public record Packet(PacketType type, PacketRange range, String originName, String content) {
        public String serialize() {
            return packetNames.getValue(type) + "\u001f" + rangeNames.getValue(range) + "\u001f" + originName + "\u001f" + content;
        }

        public static Packet deserialize(String line) {
            // Remember to increase limit if we add another field to the packet!
            String[] parts = line.split("\u001f", 4);
            PacketType t = packetNames.getKey(parts[0]);
            PacketRange r = rangeNames.getKey(parts[1]);
            String originName = parts[2];
            return new Packet(t, r, originName, parts.length > 3 ? parts[3] : "");
        }
    }

    public Optional<CascadeNode> us = Optional.empty();

    // These need to stay updated, meaning I want to move them to be more obvious soon.

    public CascadePacketManager() {
        if (SteamNetworkingManager.host == null) return;
        this.us = SteamNetworkingManager.host.getCascadeManager().getUs();
    }

    // Handle any incoming raw line from a socket.
    public void handleIncomingLine(String rawLine, int conn) {
        SteamID senderID = SteamNetworkingManager.getRevConnections().get(conn);
        try {
            Packet pkt = Packet.deserialize(rawLine);

            Optional<Packet> new_pkt = handlePacket(pkt, conn, senderID);
            if (new_pkt.isEmpty()){
                return;
            }
            pkt = new_pkt.get();

            PacketRange range = pkt.range();
            switch (range){
                case EVERYONE -> {
                    if (us.isEmpty()) break;

                    // Always send down to children (except sender)
                    sendDown(pkt, Optional.of(senderID));

                    // If we have a parent and the sender was not our parent, forward upward
                    if (us.get().getParent() != null && !us.get().getParent().getId().equals(senderID)) {
                        sendTo(us.get().getParent().getId(), pkt);
                    }

                    // If we are a root (no parent) and the packet came from one of our children,
                    // forward to other trees' roots (but not to ourselves)
                    else if (us.get().getParent() == null &&
                            us.get().wasSentByChild(senderID)) {

                        for (CascadeTree tree : SteamNetworkingManager.host.getCascadeManager().getTrees()) {
                            if (!tree.getRoot().getId().equals(Account.getMySteamID())) {
                                sendTo(tree.getRoot().getId(), pkt);
                            }
                        }
                    }
                }
                case ENTIRE_TREE -> {
                    // Always send down
                    sendDown(pkt, Optional.of(senderID));
                    // If we weren't sent this by our parent, send it to our parent
                    if (us.isPresent() && !us.get().getParent().getId().equals(senderID)) {
                        sendTo(us.get().getParent().getId(), pkt);
                    }
                }
                case UP -> sendUp(pkt, Optional.of(senderID), false);
                case DOWN -> sendDown(pkt, Optional.of(senderID));
                case SEND_TO -> {}
            }
        }
        catch (IllegalArgumentException e) {
            System.err.println("Invalid packet format from packet: " + rawLine);
            e.printStackTrace();
        }
    }

    public Optional<Packet> handlePacket(Packet pkt, int conn, SteamID senderID){
        return switch (pkt.type) {
            case CHAT -> cascadePacketHandler.handleChatMessage(pkt, 0);
            case SPECIAL_CHAT -> cascadePacketHandler.handleChatMessage(pkt, 1);
            case SUPER_SPECIAL_CHAT -> cascadePacketHandler.handleChatMessage(pkt, 2);
            case MOVE -> cascadePacketHandler.handleMove(pkt, senderID);
            case PROMOTE -> cascadePacketHandler.handlePromotion(pkt);
            case SPAWN -> cascadePacketHandler.handleSpawn(pkt, senderID);
            case KILL -> cascadePacketHandler.handleKill(pkt);
            case ERASE -> cascadePacketHandler.handleErase(pkt, false);
            case ERASE_ID -> cascadePacketHandler.handleErase(pkt, true);
            case LEAVE -> cascadePacketHandler.handleLeave(pkt);
            case START -> cascadePacketHandler.handleStartGame(pkt, senderID);
            case GETPLAYERCOUNT -> cascadePacketHandler.handleGetPlayerCount(senderID, pkt);
            case JOIN -> cascadePacketHandler.handleJoin(conn, senderID, pkt);
            case ASSIGN_BOSS -> cascadePacketHandler.handleHavingBeenAssignedBoss(conn, pkt);
            case NEW_EMPLOYEE -> cascadePacketHandler.handleNewEmployee(senderID, conn, pkt);
            case PLAYER_ID -> cascadePacketHandler.handleAssignPlayerID(pkt);
            case BUNDLE -> cascadePacketHandler.handlePacketBundle(pkt, conn, senderID);
            case SKIP_TURN -> cascadePacketHandler.handleSkipTurn(senderID, pkt);
            case PLAYER_LIST -> cascadePacketHandler.handlePlayerList(senderID, pkt);
            case CUSTOM_MAP -> cascadePacketHandler.handleCustomMap(senderID, pkt);
            case PARALLEL_UNIVERSE_REQUEST -> cascadePacketHandler.handleParallelUniverseRequest(senderID, pkt);
            case PARALLEL_UNIVERSE_FIX -> cascadePacketHandler.handleParallelUniverseFix(senderID, pkt);
        };
    }


    // send up the chain until the root / top gets it
    private void sendUp(Packet pkt, Optional<SteamID> sender, boolean sendToOtherTrees) {

        if (us.isPresent() && us.get().getParent() != null) {
            SteamID next = us.get().getParent().getId();
            sendTo(next, pkt);
        }
        else if (sendToOtherTrees) {
            for (CascadeTree tree : SteamNetworkingManager.host.getCascadeManager().getTrees()) {
                if (!tree.getRoot().getId().equals(Account.getMySteamID())) {
                    if (sender.isEmpty() || !sender.get().equals(tree.getRoot().getId())) {
                        sendTo(tree.getRoot().getId(), pkt);
                    }
                }
            }
        }
    }

    // once at top, send down to everyone
    void sendDown(Packet pkt, Optional<SteamID> senderID) {
        if (us.isEmpty()) return;
        for (CascadeNode child : us.get().getChildren()) {
            if (senderID.isEmpty() || !senderID.get().equals(child.getId())){
                sendTo(child.getId(), pkt);
            }
        }
    }

    private void sendToOurTree(Packet pkt, Optional<SteamID> senderID) {
        if (us.isPresent() && us.get().getParent() != null) {
            SteamID next = us.get().getParent().getId();
            sendTo(next, pkt);
        }
       else {
           sendDown(pkt, senderID);
        }
    }

    public void sendWithRange(Packet pkt) {
       switch (pkt.range()) {
           case EVERYONE -> sendPacketToEveryone(pkt.type, pkt.content, Optional.empty());
           case ENTIRE_TREE -> sendToOurTree(pkt, Optional.empty());
           case UP -> sendUp(pkt, Optional.empty(), false);
           case DOWN -> sendDown(pkt, Optional.empty());
           case SEND_TO -> {}
       }
    }

    //TODO - in ALL 18 CASES, sender is Optional.EMPTY!!! What is it even here for?
    public void sendPacketToEveryone(PacketType packetType, String message, Optional<SteamID> sender) {
        Packet pkt = new Packet(packetType, PacketRange.EVERYONE, Account.username(), message);

        if (us.isPresent() && us.get().getParent() != null) {
            SteamID next = us.get().getParent().getId();
            sendTo(next, pkt);
            sendDown(pkt, sender);
        }
        else {
            for (CascadeTree tree : SteamNetworkingManager.host.getCascadeManager().getTrees()) {
                if (!tree.getRoot().getId().equals(Account.getMySteamID())) {
                    if (sender.isEmpty() || !sender.get().equals(tree.getRoot().getId())) {
                        sendTo(tree.getRoot().getId(), pkt);
                    }
                }
                else {
                    sendDown(pkt, sender);
                }
            }
        }
    }

    public void sendTo(SteamID targetId, Packet pkt) {
        Integer conn = SteamNetworkingManager.getConnections().get(targetId);
        if (conn == null) return; //seems redundant but a user could leave any millisecond.
        // This also stops you from sending to people you arent connected to in the tree.
        SteamResult result = SteamNetworkingManager.getSockets().sendMessageToConnection(conn, pkt.serialize().getBytes(StandardCharsets.UTF_8), 0);
        if (result != SteamResult.OK) {
            System.err.println("Error sending message: " + result);
        }
    }
}