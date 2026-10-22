package lanse.worldwarchess.shared.core.piece.move;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.dirs.ProjectDirectories;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.shared.core.worldmap.board.BoardClassPrefixMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoveHistory {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Path gameHistoryDir =
            Path.of(ProjectDirectories.from("dev", "Lanse", "Meanimax").dataDir);

    private static Path currentGameFile;
    public static boolean shouldRecordHistory = false;
    public static final List<MoveRecord> moveRecords = new ArrayList<>();

    private static final List<String> moveBuffer = new ArrayList<>();
    private static final Object bufferLock = new Object();

    public record MoveRecord(Piece piece, int startX, int startY, int endX, int endY, boolean isCapture) {}


    public static MoveRecord getLastMove() {
        return moveRecords.isEmpty() ? null : moveRecords.get(moveRecords.size() - 1);    }

    public static boolean wasLastMovePawnDoubleAdvance(int x, int y, String color) {
        MoveRecord lastMove = getLastMove();
        if (lastMove == null) return false;
        return lastMove.piece().getType() == PieceType.PAWN
                && lastMove.piece().getTeam().equalsIgnoreCase(color)
                && Math.abs(lastMove.startY() - lastMove.endY()) == 2
                && lastMove.endX() == x && lastMove.endY() == y;
    }

    public static boolean hasRookMoved(Board board, int x, int y) {
        Piece rook = board.getPieceAt(x, y, Optional.empty(), WorldMap.getAllPiecesOfBoard(board));
        if (rook == null) return true;
        return rook.hasMovedEver;
    }

    //Somehow optimizes game by loading this class early once...
    public static void loadClass(){}


    public static void createNewGameForRecord() {
        if (!shouldRecordHistory) return;

        try {
            if (!Files.exists(gameHistoryDir))
                Files.createDirectories(gameHistoryDir);

            // Find next available game index
            List<Integer> existing = Files.list(gameHistoryDir)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith("game") && name.endsWith(".json"))
                    .map(name -> name.substring(4, name.length() - 5))
                    .map(Integer::parseInt)
                    .sorted().toList();

            int next = 1;
            for (int n : existing) {
                if (n == next) next++;
                else break;
            }

            currentGameFile = gameHistoryDir.resolve("game" + next + ".json");

            ObjectNode root = mapper.createObjectNode();
            root.put("gameNumber", next);
            root.set("boards", mapper.createArrayNode());
            root.set("pieces", mapper.createArrayNode());
            root.set("moves", mapper.createObjectNode());

            Files.writeString(currentGameFile,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createBoardListForRecord() {
        if (currentGameFile == null || !shouldRecordHistory) return;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(currentGameFile.toFile());
            ArrayNode boards = mapper.createArrayNode();

            for (Board board : WorldMap.boards.values()) {
                boards.add(1 + "," +
                        board.getBoardID() + "," +
                        board.getBoardSize() + "," +
                        board.getBoardX() + "," +
                        board.getBoardY());
            }

            root.set("boards", boards);
            Files.writeString(currentGameFile,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createPieceListForRecord() {
        if (currentGameFile == null || !shouldRecordHistory) return;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(currentGameFile.toFile());
            ArrayNode pieces = mapper.createArrayNode();

            for (Piece piece : WorldMap.pieces) {
                pieces.add(piece.getPieceClassPrefix() + "," +
                                piece.getPlayerID() + "," +
                                piece.getPieceSize() + "," +
                                piece.getBoardX() + "," +
                                piece.getBoardY() + "," +
                                piece.getPosX() + "," +
                                piece.getPosY());
            }

            root.set("pieces", pieces);
            Files.writeString(currentGameFile,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recordData(String move) {
        synchronized (bufferLock) {
            if (shouldRecordHistory) {
                moveBuffer.add(move);
            }
        }
    }

    public static void addNewTurn() {
        if (currentGameFile == null || !shouldRecordHistory) return;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(currentGameFile.toFile());
            ObjectNode moves = (ObjectNode) root.get("moves");

            String turnKey = String.valueOf(Timer.turnCounter);
            if (!moves.has(turnKey)) {
                moves.set(turnKey, mapper.createArrayNode());
            }

            root.set("moves", moves);
            Files.writeString(currentGameFile,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        synchronized (bufferLock) {
            if (!shouldRecordHistory || moveBuffer.isEmpty() || currentGameFile == null) {
                moveBuffer.clear();
                return;
            }

            try {
                ObjectNode root;

                if (Files.exists(currentGameFile)) {
                    root = (ObjectNode) mapper.readTree(currentGameFile.toFile());
                } else {
                    root = mapper.createObjectNode();
                }

                ObjectNode moves = root.has("moves") ? (ObjectNode) root.get("moves") : mapper.createObjectNode();

                String turnKey = String.valueOf(Timer.turnCounter);
                ArrayNode turnMoves = moves.has(turnKey) ? (ArrayNode) moves.get(turnKey) : mapper.createArrayNode();

                for (String move : moveBuffer) {
                    turnMoves.add(move);
                }
                moveBuffer.clear();

                moves.set(turnKey, turnMoves);
                root.set("moves", moves);

                Files.writeString(currentGameFile,
                        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                );

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addMoveForRecord(Piece oldPiece, Piece newPiece, boolean shouldSend, String moveType, int bossHealth) {
        newPiece.hasMovedEver = true;

        String health = (bossHealth == -40404) ? "" : String.valueOf(bossHealth);

        String move = String.format("%s,%d,%d,%d,%d,%d,%d,%s",
                moveType,
                oldPiece.getBoard().getBoardID(),
                oldPiece.getPosX(), oldPiece.getPosY(),
                newPiece.getBoard().getBoardID(),
                newPiece.getPosX(), newPiece.getPosY(),
                health);

        recordData(move);
    }

    public static void addPromotionForRecord(Piece newPiece, boolean shouldSend) {
        newPiece.hasMovedEver = true;
        String move = String.format("promo,%d,%d,%d,%s,%d",
                newPiece.getBoard().getBoardID(),
                newPiece.getPosX(), newPiece.getPosY(),
                newPiece.getType().getDisplayName(),
                newPiece.getPieceClassPrefix());

        recordData(move);
    }

    public static void spawnPieceForRecord(Piece newPiece, boolean shouldSend) {
        String move = String.format("spawn,%d,%d,%d,%s,%s,%d,%s,%d",
                newPiece.getBoard().getBoardID(),
                newPiece.getPosX(), newPiece.getPosY(),
                newPiece.getType().getDisplayName(),
                newPiece.getTeam(),
                newPiece.getPieceSize(),
                newPiece.getPlayerID(),
                newPiece.getPieceClassPrefix());

        recordData(move);
    }

    public static void erasePieceForRecord(Piece piece) {
        String move = String.format("erase,%d,%d,%d",
                piece.getBoard().getBoardID(),
                piece.getPosX(), piece.getPosY());

        recordData(move);
    }
}