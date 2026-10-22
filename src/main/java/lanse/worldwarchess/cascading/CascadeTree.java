package lanse.worldwarchess.cascading;

import com.codedisaster.steamworks.SteamID;
import lanse.worldwarchess.steam.Account;
import lanse.worldwarchess.steam.SteamNetworkingManager;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class CascadeTree {

    //This class is the data structure of the Cascading Tree itself. All it does is be a tree. It has some
    //functions for general tree data logic, and it can add new nodes to itself, but it does not do too much.

    /**
     * root is the top node of the tree
     * nodeMap is for the steamIDs of the players, and holds structure of the tree
     * connectionHandles holds the Steam Connection number for each steamID. this is given from connectP2P
     */
    private final CascadeNode root;
    private final Map<SteamID, CascadeNode> nodeMap = new ConcurrentHashMap<>();
    private final Map<SteamID, Integer> connectionHandles = new ConcurrentHashMap<>();
    //TODO - CascadeTree should store a string team name or something like White or Black or NPC.

    public CascadeTree(SteamID rootId, int conn) {
        this.root = new CascadeNode(rootId);
        nodeMap.put(rootId, root);
        connectionHandles.put(rootId, conn);
    }

    public Iterable<SteamID> getSteamIDsOnTree() {
        return nodeMap.keySet();
    }

    public CascadeNode assignNewNode(SteamID newId, int conn, SteamID bossId) {
        CascadeNode bossNode = nodeMap.get(bossId);
        if (newId == bossId) {
            return bossNode;
        }
        CascadeNode newNode = new CascadeNode(newId);
        bossNode.addChild(newNode);
        nodeMap.put(newId, newNode);
        connectionHandles.put(newId, conn);
        return newNode;
    }

    //TODO - fix so it spreads out randomly or evenly instead of overloading 1 child at a time, and make sure if all
    // those level bosses are full, go down to next level so bosses under those bosses.
    public CascadeNode assignNewNode(SteamID newId, int conn) {
        CascadeNode newNode = new CascadeNode(newId);
        Queue<CascadeNode> q = new LinkedList<>();
        q.add(root);

        while (!q.isEmpty()) {
            var curr = q.poll();
            if (curr.addChild(newNode)) {
                nodeMap.put(newId, newNode);
                connectionHandles.put(newId, conn);
                return curr;
            }
            q.addAll(curr.getChildren());
        }
        //there should never be no spot available, since its infinitely scalable...
        throw new IllegalStateException("No spot available. This error should be impossible to reach, so if you are here, hi lol");
    }

    public CascadeNode getRoot() {
        return root;
    }

    public static boolean amIARoot() {
        return SteamNetworkingManager.host.getCascadeManager().getTrees().stream()
                .anyMatch(tree -> tree.getRoot().getId().equals(Account.getMySteamID()));
    }

    public int size() {
        return root.subtreeSize();
    }

    public CascadeNode getNode(SteamID id) {
        return nodeMap.get(id);
    }

    public Integer getConnectionHandle(SteamID id) {
        return connectionHandles.get(id);
    }

    public void setConnectionHandle(SteamID id, int conn) {
        connectionHandles.put(id, conn);
    }
}
