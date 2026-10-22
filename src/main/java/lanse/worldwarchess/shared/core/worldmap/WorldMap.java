package lanse.worldwarchess.shared.core.worldmap;

import lanse.worldwarchess.cascading.CascadePacketBundler;
import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.shared.central.CentralTick;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeInitializer;
import lanse.worldwarchess.shared.core.gamemode.GameModeMapType;
import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.AutoHealable;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.piece.ai.bosses.BossHealthManager;
import lanse.worldwarchess.shared.core.piece.ai.bosses.NpcBoss;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.NpcCommanderPiece;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.piece.move.VisibleMoveDetector;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.tutorial.Tutorial;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.shared.utils.LagTester;
import lanse.worldwarchess.steam.Account;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldMap {

    /**This class stores all the pieces and boards in the world, and has a ton of functions
     * for calling them and making use of them. It is also responsible for setting up the map.
     */

    public static Map<Integer, Board> boards = new ConcurrentHashMap<>();
    public static List<Piece> pieces = new CopyOnWriteArrayList<>();

    public static List<Piece> minionPieceSpawnRequests = new CopyOnWriteArrayList<>();
    public static List<Piece> minionPieceDestroyRequests = new CopyOnWriteArrayList<>();

    public static GameModeMapType currentMapPreset;

    public static void initializeBoards() {
        boards.clear();
        if (currentMapPreset == null) return;

        GameModeMapType.setupMap();
        Board.linkAllBoardNeighbors();

        System.out.println("Players accounted for: " + MapShapePresets.playerIDCount);

        GameModeInitializer.initializeTeams();

        MoveHistory.createNewGameForRecord();
        MoveHistory.createBoardListForRecord();
        MoveHistory.createPieceListForRecord();

        /**
        if (WorldMap.currentMapPreset == GameModeMapType.CITYDEBUG) {
            Cityifier.cityify();
        }
         **/

        if (Main.userIsHost) {
            MultiplayerTeamIDBuilder.buildTeams();
        }
    }

    public static List<Piece> getAllPiecesOfBoard(Board board) {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p != null && p.getBoard() == board) {
                result.add(p);
            }
        }
        return result;
    }

    public static Optional<Piece> getKingWithName(String name){
        return WorldMap.pieces.stream()
                .filter(p -> p.getType() == PieceType.KING)
                .filter(p -> name.equalsIgnoreCase(p.getPlayerName()))
                .findFirst();
    }

    public static List<Piece> getCommanderPiecesOnBoard(Board board) {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p != null && p.getBoard() == board && p instanceof CommanderPiece) {
                result.add(p);
            }
        }
        return result;
    }

    public static List<Piece> getRegularPiecesOnBoard(Board board) {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p != null && p.getBoard() == board && !(p instanceof CommanderPiece) && !(p instanceof NpcBoss)) {
                result.add(p);
            }
        }
        return result;
    }

    public static Map<LegalMoveDetector.Coordinate, List<String>> getAllEnemyMoves() {
        LagTester lagTester = new LagTester();
        lagTester.start("Get ALL Enemy Moves");

        Map<LegalMoveDetector.Coordinate, List<String>> attackedSquares = new HashMap<>();

        for (Piece piece : pieces) {
            if (GameMode.isFogOfWar) {
                List<LegalMoveDetector.Coordinate> legalMoves = VisibleMoveDetector.getLegalMoves(piece);
                for (LegalMoveDetector.Coordinate coord : legalMoves) {
                    attackedSquares.computeIfAbsent(coord, k -> new ArrayList<>()).add(piece.getTeam());
                }
            }
            else {
                List<LegalMoveDetector.Coordinate> legalMoves = VisibleMoveDetector.getVisibleMoves(piece);
                for (LegalMoveDetector.Coordinate coord : legalMoves) {
                    attackedSquares.computeIfAbsent(coord, k -> new ArrayList<>()).add(piece.getTeam());
                }
            }
        }

        lagTester.stop();
        return attackedSquares;
    }

    public static List<Piece> getAllNPCPieces() {
        return pieces.stream().filter(p -> p instanceof NpcPiece || p instanceof NpcCommanderPiece).toList();
    }

    public static List<Piece> getAllPiecesOfPlayerID(int playerID) {
        return pieces.stream().filter(p -> p.getPlayerID() == playerID).toList();
    }

    public static List<Piece> getAllKings(String team, boolean commander, boolean findNearestEnemy, boolean findNearestTeamate, List<Piece> pieceList) {
        return pieceList.stream().filter(p -> (p instanceof CommanderPiece || !commander) && p.getType() == PieceType.KING)
                .filter(p -> (findNearestEnemy && !Objects.equals(team, p.getTeam())) || (findNearestTeamate &&
                        Objects.equals(team, p.getTeam()))).toList();
    }

    public static List<Piece> getAllPiecesOfTeam(String team) {
        return pieces.stream().filter(p -> Objects.equals(p.getTeam(), team)).toList();
    }

    public static Board getBoardByID(int boardID) {
        return boards.get(boardID);
    }

    public static void tickBoards() { boards.values().forEach(Board::tick); }

    public static void tickAttacks() {
        for (Piece piece : pieces){
            if (piece instanceof PieceAttack) ((PieceAttack) piece).doLifeSpanCheck();
        }
    }

    public static void tickMinionRequests() {
        for (Piece piece : minionPieceDestroyRequests){
            erasePiece(piece, true);
        }

        for (Piece piece : minionPieceSpawnRequests){
            Board board = piece.getBoard();
            int posX = piece.getPosX();
            int posY = piece.getPosY();
            Piece piece1 = board.getPieceAt(posX, posY, Optional.empty(), pieces);
            if (piece1 == null){
                WorldMap.spawnPiece(piece, true);

            } else if (!Objects.equals(piece1.getTeam(), piece.getTeam())){
                WorldMap.killPiece(piece1, true, true);
                WorldMap.spawnPiece(piece, true);
            }
        }

        minionPieceSpawnRequests.clear();
        minionPieceDestroyRequests.clear();
    }

    public static void killPiece(Piece piece, boolean doNpcCheck, boolean shouldSend) {
        killPiece(piece, doNpcCheck, shouldSend, Optional.empty());
    }

    public static void killPiece(Piece piece, boolean doNpcCheck, boolean shouldSend, Optional<CascadePacketBundler> bundler) {
        if (doNpcCheck) {
            if (piece instanceof NpcPiece) {
                ((NpcPiece) piece).killPiece(piece, shouldSend);
                return;
            } else if (piece instanceof NpcCommanderPiece) {
                ((NpcCommanderPiece) piece).killPiece(piece, shouldSend);
                return;
            }
        }

        new DeathMarker(piece.getBoard(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
        pieces.remove(piece);

        if (piece.getType() == PieceType.KING) {
            if (WorldMap.currentMapPreset.equals(GameModeMapType.SIEGE) && piece instanceof NpcPiece) return;

            if (piece instanceof CommanderPiece){
                GamePanel.winningTeam = (piece.getTeam().equals("White")) ? "BLACK" : "WHITE";
                DeathMarker.deathRow.addAll(WorldMap.getAllPiecesOfTeam(piece.getTeam()));
                if (!GameMode.isTutorial) CentralTick.gameOver = true;
                SoundManager.gameOver();
                if (GameMode.isTutorial && Tutorial.phase == 8){
                    Tutorial.nextPhase();
                }
            } else {
                DeathMarker.deathRow.addAll(WorldMap.getAllPiecesOfPlayerID(piece.getPlayerID()));
                if (GameMode.isTutorial && Tutorial.phase == 4){
                    Tutorial.nextPhase();
                }
            }
        }
        String kill = String.format("%d,%d,%d,%s",
                piece.getBoard().getBoardID(),
                piece.getPosX(), piece.getPosY(),
                (piece instanceof CommanderPiece) ? "cmdr" : "");

        if (!GameMode.isSinglePlayer && shouldSend) {
            if (bundler.isPresent()) {
                bundler.get().addPacket(new CascadePacketManager.Packet(
                        CascadePacketManager.PacketType.ERASE,
                        CascadePacketManager.PacketRange.EVERYONE,
                        Account.username(), kill));
            } else {
                CascadePacketManager.manager.sendPacketToEveryone(CascadePacketManager.PacketType.KILL, kill, Optional.empty());
            }
        }
    }

    public static void infectPiece(Piece infector, Piece piece, boolean doNpcCheck){
        infectPiece(infector, piece, doNpcCheck, false, Optional.empty());
    }

    public static void infectPiece(Piece infector, Piece piece, boolean doNpcCheck, boolean shouldSend, Optional<CascadePacketBundler> bundler){
        int infectorStrength = BossHealthManager.getPieceStrength(infector);

        if (BossHealthManager.getPieceStrength(piece) > infectorStrength * 2){
            killPiece(piece, doNpcCheck, shouldSend, bundler);
            return;
        }

        WorldMap.killPiece(piece, false, shouldSend, bundler);

        Piece newPiece = new Piece(piece.getPlayerID(), piece.getBoard(), piece.getType().getDisplayName(), infector.getTeam(),
                piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());

        WorldMap.spawnPiece(newPiece, false);
    }

    public static void resetPieces() {
        pieces.forEach(p -> p.hasMoved = false);
    }

    public static void spawnPiece(Piece piece, boolean shouldSend){
        WorldMap.pieces.add(piece);
        MoveHistory.spawnPieceForRecord(piece, shouldSend);
    }

    public static void erasePiece(Piece piece, boolean shouldSend){
        pieces.remove(piece);
        MoveHistory.erasePieceForRecord(piece, shouldSend, null);
    }

    public static void erasePiece(Piece piece, boolean shouldSend, CascadePacketBundler bundler) {
        pieces.remove(piece);
        MoveHistory.erasePieceForRecord(piece, shouldSend, bundler);
    }

    public static void killScaledBlock(Board board, int blockX, int blockY, Piece capturingPiece) {
        int killCount = 0;
        for (Piece p : Board.getPiecesAtScaledBlock(board, blockX, blockY, capturingPiece)) {
            if (!p.getTeam().equals(capturingPiece.getTeam())) {
                WorldMap.killPiece(p, true, false); //TODO: should it send packets sometimes?
                killCount++;
            }
        }
        if (killCount > 1) {
            SoundManager.ultraKill(capturingPiece);
        } else {
            SoundManager.commanderAttack(capturingPiece);
        }
    }

    public static void healAutoHealables() {
        for (Piece piece : pieces){
            if (piece instanceof AutoHealable hp){
                hp.passiveHeal();
            }
        }
    }

    public static int getNewBoardID() {
        int newID = 1;
        boolean newIDFound = false;
        boolean brokeLoop;

        while (!newIDFound) {
            brokeLoop = false;
            for (Board board : WorldMap.boards.values()) {
                if (board.getBoardID() == newID) {
                    newID++;
                    brokeLoop = true;
                    break;
                }
            }

            if (!brokeLoop) {
                newIDFound = true;
            }
        }

        return newID;
    }

    public static List<String> getAllTeamNames() {
        List<String> teamNameList = new ArrayList<>();

        for (Piece piece : pieces){
            if (!teamNameList.contains(piece.getTeam())){
                teamNameList.add(piece.getTeam());
            }
        }

        return teamNameList;
    }

    public static void resetIDs() {
        int i = 0;
        for (Board board : boards.values()){
            board.setBoardID(i);
            i++;
        }

        for (Piece piece : pieces){
            if (piece.getTeam().equalsIgnoreCase("White") || piece.getTeam().equalsIgnoreCase("Black")) {
                int newID = piece.getBoard().getBoardID() * 2;
                if (piece.getTeam().equalsIgnoreCase("White")) newID++;
                piece.setPlayerID(newID);
            }
        }

        i*=3;
        List<String> teams = getAllTeamNames();
        for (String team : teams){
            if (team.equalsIgnoreCase("White") || team.equalsIgnoreCase("Black")) continue;
            for (Piece piece : getAllPiecesOfTeam(team)){
                piece.setPlayerID(i);
            }
            i++;
        }
    }
}