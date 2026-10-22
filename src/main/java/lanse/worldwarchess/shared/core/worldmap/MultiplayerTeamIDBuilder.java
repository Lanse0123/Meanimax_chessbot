package lanse.worldwarchess.shared.core.worldmap;

import com.codedisaster.steamworks.SteamID;
import lanse.worldwarchess.cascading.CascadePacketBundler;
import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.cascading.CascadeTree;
import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeType;
import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.steam.Account;
import lanse.worldwarchess.steam.SteamNetworkingManager;

import java.util.*;

public class MultiplayerTeamIDBuilder {

    //This class will be responsible for assigning players teams, and giving them a playerID so they can only
    // move THEIR playerID pieces. If the gamemode is frenzy, this class should only assign them to black or white team,
    // but not a playerID.

    // this function should probably be broken up into smaller functions
    public static void buildTeams() {
        Map<String, List<Integer>> groupedByTeam = new HashMap<>();
        Map<Integer, String> normalPieceTeamList = new HashMap<>();
        Map<String, List<Integer>> commanderByTeam = new HashMap<>();

        for (Piece piece : WorldMap.pieces) {
            int id = piece.getPlayerID();
            String team = piece.getTeam();

            groupedByTeam.computeIfAbsent(team, t -> new ArrayList<>());

            if (piece instanceof CommanderPiece) {
                commanderByTeam.computeIfAbsent(team, t -> new ArrayList<>());
                List<Integer> commanderList = commanderByTeam.get(team);
                if (!commanderList.contains(id)) {
                    commanderList.add(id);
                }
            } else {
                normalPieceTeamList.putIfAbsent(id, team);
            }

            if (!groupedByTeam.get(team).contains(id)) {
                groupedByTeam.get(team).add(id);
            }
        }

        List<String> teamOrder = List.of("White", "Black");

        ArrayDeque<Integer> teamIds = new ArrayDeque<>();

        for (String team : teamOrder) {

            // Add commander IDs FIRST
            if (commanderByTeam.containsKey(team)) {
                teamIds.addAll(commanderByTeam.get(team));
            }

            List<Integer> ids = groupedByTeam.get(team);
            if (ids != null) {
                for (int id : ids) {
                    if (!commanderByTeam.getOrDefault(team, List.of()).contains(id) && !teamIds.contains(id)) {
                        teamIds.add(id);
                    }
                }
            }
        }

        ArrayDeque<Integer> whiteIds = new ArrayDeque<>();
        ArrayDeque<Integer> blackIds = new ArrayDeque<>();

        for (int id : teamIds) {
            String team = null;

            for (String tean : groupedByTeam.keySet()) {
                if (groupedByTeam.get(tean).contains(id)) {
                    team = tean;
                    break;
                }
            }

            if (team == null) continue;

            if (team.equals("White")) {
                whiteIds.add(id);
            } else if (team.equals("Black")) {
                blackIds.add(id);
            }
        }

        System.out.println("WHITE IDS: " + whiteIds);
        System.out.println("BLACK IDS: " + blackIds);

        if (GameMode.currentMode == GameModeType.HUMAN_VS_NPC) {

            int humanCount = 0;
            List<CascadeTree> trees = SteamNetworkingManager.host.getCascadeManager().getTrees();

            if (!trees.isEmpty()) {
                CascadeTree whiteTree = trees.get(0);
                for (SteamID ignored : whiteTree.getSteamIDsOnTree()) humanCount++;
            }

            //scary but i think i can ignore this error
            int blackCommander = blackIds.peekFirst();

            ArrayDeque<Integer> newBlack = new ArrayDeque<>();
            newBlack.add(blackCommander);

            //Black ID count should equal White ID count
            for (int id : blackIds) {
                if (id == blackCommander) continue;
                if (newBlack.size() >= humanCount) break;
                newBlack.add(id);
            }

            blackIds = newBlack;

            while (whiteIds.size() > humanCount) {
                whiteIds.removeLast();
            }

            //more and more additions, this code is so bad lol
            for (int id : blackIds) {
                normalPieceTeamList.remove(id);
            }

            for (int id : whiteIds) {
                normalPieceTeamList.remove(id);
            }

            System.out.println("[HUMAN VS NPC] White IDs: " + whiteIds);
            System.out.println("[HUMAN VS NPC] Black IDs: " + blackIds);
        }


        CascadePacketBundler bundler = new CascadePacketBundler(CascadePacketManager.PacketRange.EVERYONE);
        List<CascadeTree> trees = SteamNetworkingManager.host.getCascadeManager().getTrees();

        for (int i = 0; i < trees.size(); i++) {
            CascadeTree tree = trees.get(i);

            ArrayDeque<Integer> source = (i == 0 ? whiteIds : i == 1 ? blackIds : null);

            if (source == null) {
                // Extra trees get no assignment for now. This code needs to be made more extendible soon.
                continue;
            }

            for (SteamID player : tree.getSteamIDsOnTree()) {
                if (source.isEmpty()) break;

                int nextTeamId = source.pop();

                CascadePacketManager.Packet pkt = new CascadePacketManager.Packet(
                        CascadePacketManager.PacketType.PLAYER_ID,
                        CascadePacketManager.PacketRange.EVERYONE,
                        Account.username(),
                        player.toString() + "," + nextTeamId
                );

                bundler.addPacket(pkt);
                assignTeams(player, nextTeamId);
                normalPieceTeamList.remove(nextTeamId);
            }
        }

        CascadePacketManager.Packet bundlePacket = bundler.getPacketBundle();
        CascadePacketManager.manager.sendPacketToEveryone(
                CascadePacketManager.PacketType.BUNDLE,
                bundlePacket.serialize(),
                Optional.empty());

        if (!GameMode.isFrenzy) {
            CascadePacketBundler eraseBundler = new CascadePacketBundler(CascadePacketManager.PacketRange.EVERYONE);

            for (Piece piece : WorldMap.pieces) {
                if (piece instanceof CommanderPiece) continue;

                if (normalPieceTeamList.containsKey(piece.getPlayerID())) {
                    WorldMap.erasePiece(piece, true, eraseBundler);
                }
            }

            CascadePacketManager.Packet eraseBundle = eraseBundler.getPacketBundle();
            CascadePacketManager.manager.sendPacketToEveryone(
                    CascadePacketManager.PacketType.BUNDLE,
                    eraseBundle.serialize(),
                    Optional.empty());
        }
    }

    public static void assignTeams(SteamID playerID, int teamID) {
        if (playerID.equals(Account.getMySteamID())) {
            CascadingClient.playerID = teamID;
        }

        for (Piece piece : WorldMap.pieces) {
            if (piece.getPlayerID() == teamID && piece.getType() == PieceType.KING){
                if (!GameMode.isFrenzy) piece.setPlayerName(Account.username(playerID));

                if (playerID.equals(Account.getMySteamID())) {
                    CascadingClient.team = piece.getTeam();
                    System.out.println("You are now on " + piece.getTeam() + " team.");
                }
                break;
            }
        }

        //reset renderCache because this playerID assignment changes textures
        RenderCache.clear();
        Board.clearBoardCache();
    }
}