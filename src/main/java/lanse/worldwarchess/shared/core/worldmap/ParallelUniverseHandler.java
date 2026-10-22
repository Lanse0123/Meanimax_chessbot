package lanse.worldwarchess.shared.core.worldmap;

import com.codedisaster.steamworks.SteamID;
import lanse.worldwarchess.cascading.CascadeNode;
import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceClassPrefixMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.steam.Account;

import java.util.List;
import java.util.Optional;

public class ParallelUniverseHandler {

    public static void handleParallelUniverse(int boardID, SteamID senderID){
        if (GameMode.isSinglePlayer) return; //how tf would this even happen???

        Optional<CascadeNode> us = CascadePacketManager.manager.us;

        if (Main.userIsHost || (us.isPresent() && us.get().wasSentByChild(senderID))){

            Board board = WorldMap.getBoardByID(boardID);
            List<Piece> pieceList = WorldMap.getAllPiecesOfBoard(board);
            StringBuilder sb = new StringBuilder();

            // Header
            sb.append(boardID).append("|").append(pieceList.size()).append(";");

            for (Piece piece : pieceList) {
                sb.append(piece.getPlayerID()).append(",");
                sb.append(piece.getType()).append(",");
                sb.append(piece.getTeam()).append(",");
                sb.append(piece.getPosX()).append(",");
                sb.append(piece.getPosY()).append(",");
                sb.append(piece.getPieceSize()).append(",");

                String playerName = piece.getPlayerName();
                sb.append(playerName == null ? "" : playerName).append(",");

                sb.append(PieceClassPrefixMap.getPrefixIndex(piece.getClass()));

                sb.append(";");
            }

            String packetContent = sb.toString();

            CascadePacketManager.manager.sendTo(senderID, new CascadePacketManager.Packet(
                    CascadePacketManager.PacketType.PARALLEL_UNIVERSE_FIX,
                    CascadePacketManager.PacketRange.SEND_TO,
                    Account.username(),
                    packetContent));
        }
        else {
            CascadePacketManager.manager.sendTo(senderID, new CascadePacketManager.Packet(
                    CascadePacketManager.PacketType.PARALLEL_UNIVERSE_REQUEST,
                    CascadePacketManager.PacketRange.SEND_TO,
                    Account.username(),
                    String.valueOf(boardID)));
        }
    }

    public static void handleParallelUniverse(int boardID, int boardID2, SteamID senderID){
        handleParallelUniverse(boardID, senderID);
        handleParallelUniverse(boardID2, senderID);
    }

    public static void decodeUniverse(CascadePacketManager.Packet pkt) {
        String content = pkt.content();
        if (content == null || content.isEmpty()) return;

        // Split header from body
        String[] headerSplit = content.split("\\|", 2);
        if (headerSplit.length != 2) return;

        int boardID = Integer.parseInt(headerSplit[0]);
        String body = headerSplit[1];

        Board board = WorldMap.getBoardByID(boardID);
        if (board == null) return;

        // Remove existing pieces on this board
        WorldMap.pieces.removeIf(p -> p.getBoard().getBoardID() == boardID);

        // Split pieces
        String[] pieceEntries = body.split(";");
        for (String entry : pieceEntries) {
            if (entry.isBlank() || !entry.contains(",")) continue;

            String[] data = entry.split(",", -1);
            if (data.length < 8) continue;

            try {
                int playerId = Integer.parseInt(data[0]);
                String type = data[1];
                String team = data[2];
                int posX = Integer.parseInt(data[3]);
                int posY = Integer.parseInt(data[4]);
                int pieceSize = Integer.parseInt(data[5]);
                String playerName = data[6];
                int classIndex = Integer.parseInt(data[7]);

                Class<? extends Piece> clazz = PieceClassPrefixMap.getPieceClass(classIndex);

                Piece piece = clazz.getConstructor(
                                int.class, Board.class, String.class, String.class,
                                int.class, int.class, int.class, int.class, int.class)
                        .newInstance(
                                playerId, board, type, team, board.getBoardX(),
                                board.getBoardY(), posX, posY, pieceSize);

                if (!playerName.isEmpty()) {
                    piece.setPlayerName(playerName);
                }

                WorldMap.pieces.add(piece);

            } catch (Exception ex) {
                System.err.println("Failed to decode universe piece: " + entry);
                ex.printStackTrace();
            }
        }
    }
}