package lanse.worldwarchess.cascading;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNativeHandle;
import com.codedisaster.steamworks.SteamNetworkingSockets;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.steam.Account;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class CascadeManager {
    //This class is responsible for setting up the teams and cascading trees depending on what gamemode it is.
    //If it is a player vs player army gamemode, the 2 teams will be equal. If it is player vs NPC, there will
    //only be 1 full team, and 1 empty team.
    private CascadeTree teamA, teamB;
    private boolean hasRootA = false;
    private boolean hasRootB = false;
    public static int playerCount = 0;

    public List<CascadeTree> getTrees() {
        return Stream.of(teamA, teamB).filter(Objects::nonNull).toList();
    }

    public Optional<CascadeNode> getUs() {
        return getTrees().stream().map((tree) -> tree.getNode(Account.getMySteamID())).filter(Objects::nonNull).findFirst();
    }

    public void addUnderBoss(SteamID playerId, int playerConn, SteamID bossId, int bossConn) {
        boolean isWarMode = GameMode.getCurrentGameModeAsString().equalsIgnoreCase("War");

        CascadeTree targetTree = getTrees().stream().filter(tree -> tree.getNode(bossId) != null).findFirst().orElse(null);

        // Boss not found in any existing tree > might be new root
        if (targetTree == null) {
            if (isWarMode) {
                if (!hasRootA) {
                    teamA = new CascadeTree(bossId, bossConn);
                    hasRootA = true;
                    targetTree = teamA;
                    System.out.printf("> %s is now top node of Team A%n", bossId);
                } else if (!hasRootB) {
                    teamB = new CascadeTree(bossId, bossConn);
                    hasRootB = true;
                    targetTree = teamB;
                    System.out.printf("> %s is now top node of Team B%n", bossId);
                } else {
                    System.out.println("James please help lol");
                    return;
                }
            } else {
                // Non-War mode > everyone belongs to the same team (A)
                if (!hasRootA) {
                    teamA = new CascadeTree(bossId, bossConn);
                    hasRootA = true;
                    System.out.printf("> %s is now top node of Team A%n", bossId);
                }
                targetTree = teamA;
            }
        }

        // Assign the player under the found (or newly created) boss
        if (targetTree != null) {
            targetTree.assignNewNode(playerId, playerConn, bossId);
            System.out.printf("> %s joined under %s%n", playerId, bossId);
        }
    }

    //TODO - this needs to be smarter, in the event a root disconnects.
    public SteamID addNewPlayer(SteamID playerId, int conn) {
        playerCount++;

        if (GameMode.getCurrentGameModeAsString().equalsIgnoreCase("War")) {
            if (!hasRootA) {
                teamA = new CascadeTree(playerId, conn);
                hasRootA = true;
                System.out.printf("> %s is now top node of Team A%n", playerId);
                return playerId;
            } else if (!hasRootB) {
                teamB = new CascadeTree(playerId, conn);
                hasRootB = true;
                System.out.printf("> %s is now top node of Team B%n", playerId);
                return playerId;
            }

            if (teamA.size() <= teamB.size()) {
                var boss = teamA.assignNewNode(playerId, conn);
                System.out.printf("> %s joined Team A under %s%n", playerId, boss.getId());
                return boss.getId();
            } else {
                var boss = teamB.assignNewNode(playerId, conn);
                System.out.printf("> %s joined Team B under %s%n", playerId, boss.getId());
                return boss.getId();
            }

            //gamemode is not war (currently only other option is 1 team vs NPCs)
        } else {
            if (!hasRootA) {
                teamA = new CascadeTree(playerId, conn);
                hasRootA = true;
                System.out.printf("> %s is now top node of Team A%n", playerId);
                return playerId;
            }
            var boss = teamA.assignNewNode(playerId, conn);
            System.out.printf("> %s joined Team A under %s%n", playerId, boss.getId());
            return boss.getId();
        }
    }

    //Responsible for adding the new player to the cascade tree structure, and notifying them that they joined.
    public static void acceptJoiningPlayer(SteamID playerId, CascadeManager cascade, SteamNetworkingSockets sockets, int conn, int sendReliable) {
        SteamID bossId = cascade.addNewPlayer(playerId, conn);
        String bossHex = Long.toHexString(SteamNativeHandle.getNativeHandle(bossId));
        //TODO - this line changed so it passes hex instead of id string, make sure it is right and working
        CascadePacketManager.manager.sendTo(playerId, new CascadePacketManager.Packet(
                CascadePacketManager.PacketType.ASSIGN_BOSS, CascadePacketManager.PacketRange.SEND_TO, Account.username(), bossHex));
        System.out.printf("[Host] %s > boss %s%n", playerId, bossId);
    }
}
