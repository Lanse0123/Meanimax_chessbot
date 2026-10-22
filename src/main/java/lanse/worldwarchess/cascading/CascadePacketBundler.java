package lanse.worldwarchess.cascading;

import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.steam.Account;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CascadePacketBundler {

    //PLEASE FOR THE LOVE OF MY CAT SMUDGE, DO NOT MAKE NESTED BUNDLED PACKETS
    private final CascadePacketManager.PacketRange range;
    private final ArrayList<CascadePacketManager.Packet> packetBundle;
    private final static int MAX_BUNDLE_SIZE = 100;

    public CascadePacketBundler(CascadePacketManager.PacketRange range){
        this.range = range;
        this.packetBundle = new ArrayList<>();
    }

    public boolean isEmpty() {
        return packetBundle.isEmpty();
    }

    public void addPacket(CascadePacketManager.Packet pkt){
        assert (pkt.range() == range);
        packetBundle.add(pkt);
        if (packetBundle.size() > MAX_BUNDLE_SIZE){
            sendPackets();
        }
    }

    public void removeContents(){
        packetBundle.clear();
    }

    public CascadePacketManager.Packet getPacketBundle() {
        String content = packetBundle.stream()
                .map(CascadePacketManager.Packet::serialize)
                .collect(Collectors.joining("\n"));

        return new CascadePacketManager.Packet(CascadePacketManager.PacketType.BUNDLE, range, Account.username(), content);
    }

    public void sendPackets() {
        if (!GameMode.isSinglePlayer) {
            CascadePacketManager.manager.sendWithRange(getPacketBundle());
        }
        packetBundle.clear();
    }
}
