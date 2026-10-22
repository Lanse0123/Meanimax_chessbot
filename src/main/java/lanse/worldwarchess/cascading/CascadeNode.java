package lanse.worldwarchess.cascading;

import com.codedisaster.steamworks.SteamID;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CascadeNode {

    //This is responsible for just 1 player or node of the cascading tree. It is an ugly class that needs more
    //documentation and cleaning up.

    private final SteamID id;
    private final List<CascadeNode> children = new CopyOnWriteArrayList<>();
    private volatile CascadeNode parent;

    public CascadeNode(SteamID id) { this.id = id; }

    public synchronized boolean addChild(CascadeNode child) {
        if (children.size() >= 10) return false;
        children.add(child);
        child.setParent(this);
        return true;
    }
    private void setParent(CascadeNode parent) { this.parent = parent; }

    public synchronized int subtreeSize() {
        int size = 1;
        for (CascadeNode child : children) {
            size += child.subtreeSize();
        }
        return size;
    }

    public SteamID getId() { return id; }
    public CascadeNode getParent() { return parent; }
    public List<CascadeNode> getChildren() { return List.copyOf(children); }

    public boolean wasSentByChild(SteamID senderID){
        if (CascadePacketManager.manager.us.isEmpty()){
            System.out.println("Manager.us.isEmpty");
            return false;
        }

        return CascadePacketManager.manager.us.get().getChildren().stream().anyMatch(child -> child.getId().equals(senderID));
    }
}
