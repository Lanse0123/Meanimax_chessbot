package lanse.worldwarchess.shared.core.worldmap;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.piece.move.VisibleMoveDetector;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.shared.utils.LagTester;

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

    public static void initializeBoards() {
        boards.clear();

        //TODO - spawn the board
        GameModeMapType.setupMap();
        Board.linkAllBoardNeighbors();

        System.out.println("Players accounted for: " + MapShapePresets.playerIDCount);

        GameModeInitializer.initializeTeams();

        MoveHistory.createNewGameForRecord();
        MoveHistory.createBoardListForRecord();
        MoveHistory.createPieceListForRecord();
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

    public static Map<LegalMoveDetector.Coordinate, List<String>> getAllEnemyMoves() {
        LagTester lagTester = new LagTester();
        lagTester.start("Get ALL Enemy Moves");

        Map<LegalMoveDetector.Coordinate, List<String>> attackedSquares = new HashMap<>();

        for (Piece piece : pieces) {
            List<LegalMoveDetector.Coordinate> legalMoves = VisibleMoveDetector.getVisibleMoves(piece);
            for (LegalMoveDetector.Coordinate coord : legalMoves) {
                attackedSquares.computeIfAbsent(coord, k -> new ArrayList<>()).add(piece.getTeam());
            }
        }

        lagTester.stop();
        return attackedSquares;
    }

    public static List<Piece> getAllNPCPieces() {
        return pieces.stream().filter(p -> p instanceof NpcPiece).toList();
    }

    public static List<Piece> getAllPiecesOfPlayerID(int playerID) {
        return pieces.stream().filter(p -> p.getPlayerID() == playerID).toList();
    }

    public static List<Piece> getAllKings(String team, boolean findNearestEnemy, boolean findNearestTeamate, List<Piece> pieceList) {
        return pieceList.stream().filter(p -> p.getType() == PieceType.KING)
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
        if (doNpcCheck) {
            if (piece instanceof NpcPiece) {
                ((NpcPiece) piece).killPiece(piece, shouldSend);
                return;
            }
        }

        new DeathMarker(piece.getBoard(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
        pieces.remove(piece);

        if (piece.getType() == PieceType.KING) {
            DeathMarker.deathRow.addAll(WorldMap.getAllPiecesOfPlayerID(piece.getPlayerID()));
        }
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
        MoveHistory.erasePieceForRecord(piece);
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