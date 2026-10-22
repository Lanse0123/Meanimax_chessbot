package lanse.worldwarchess.shared.core.screens.sandbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceClassPrefixMap;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.shared.core.worldmap.board.BoardClassPrefixMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class SandboxMapDataStack {

    private static final int MAX_HISTORY = 50;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Deque<String> undoStack = new ArrayDeque<>();
    private static final Deque<String> redoStack = new ArrayDeque<>();

    /** CALL THIS AFTER ANY CHANGE TO THE MAP (bad for storage but like hehehehehehehe too bad) */
    public static void pushState() {
        try {
            undoStack.push(serializeCurrentMap());

            while (undoStack.size() > MAX_HISTORY) {
                undoStack.removeLast();
            }

            // once you change something, redo history is invalid. YoU cAnT cHaNge ThE PaSt
            redoStack.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearSandboxMapStackData(){
        undoStack.clear();
        redoStack.clear();
    }

    public static boolean canUndo() {
        return undoStack.size() > 1;
    }

    public static boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public static void undo() {
        if (!canUndo()) return;

        try {
            // current -> redo
            redoStack.push(undoStack.pop());

            // previous -> load
            loadMapFromString(undoStack.peek());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void redo() {
        if (!canRedo()) return;

        try {
            String next = redoStack.pop();

            undoStack.push(next);

            loadMapFromString(next);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO - the functions below are EXTREMELY similar to the ones in that other Sandbox class, I can probably make there
    // be less duplicated code.

    private static String serializeCurrentMap() throws Exception {
        ObjectNode root = mapper.createObjectNode();

        ArrayNode boards = mapper.createArrayNode();
        WorldMap.boards.values().forEach(board ->
                boards.add(BoardClassPrefixMap.getPrefixIndex(board.getClass()) + "," +
                        board.getBoardID() + "," +
                        board.getBoardX() + "," +
                        board.getBoardY())
        );
        root.set("boards", boards);

        ArrayNode pieces = mapper.createArrayNode();
        WorldMap.pieces.forEach(piece ->
                pieces.add(piece.getPieceClassPrefix() + "," +
                        piece.getPlayerID() + "," +
                        piece.getPieceSize() + "," +
                        piece.getBoard().getBoardID() + "," +
                        piece.getPosX() + "," +
                        piece.getPosY() + "," +
                        piece.getType().getDisplayName() + "," +
                        piece.getTeam())
        );
        root.set("pieces", pieces);

        return mapper.writeValueAsString(root);
    }

    private static void loadMapFromString(String json) throws Exception {
        WorldMap.boards.clear();
        WorldMap.pieces.clear();

        ObjectNode root = (ObjectNode) mapper.readTree(json);

        ArrayNode boards = (ArrayNode) root.get("boards");
        if (boards != null) {
            for (int i = 0; i < boards.size(); i++) {
                String[] data = boards.get(i).asText().split(",");

                int prefix = Integer.parseInt(data[0]);
                int boardID = Integer.parseInt(data[1]);
                int boardX = Integer.parseInt(data[2]);
                int boardY = Integer.parseInt(data[3]);

                Board.createFromPrefix(prefix, boardID, boardX, boardY);
            }
        }

        // link neighbors after creation
        WorldMap.boards.values().forEach(Board::updateBoardNeighbors);

        ArrayNode pieces = (ArrayNode) root.get("pieces");
        if (pieces != null) {
            for (int i = 0; i < pieces.size(); i++) {
                String[] data = pieces.get(i).asText().split(",");

                int classPrefix = Integer.parseInt(data[0]);
                int playerID = Integer.parseInt(data[1]);
                int size = Integer.parseInt(data[2]);
                int boardID = Integer.parseInt(data[3]);
                int posX = Integer.parseInt(data[4]);
                int posY = Integer.parseInt(data[5]);
                String type = data[6];
                String team = data[7];

                Board board = WorldMap.getBoardByID(boardID);
                if (board == null) continue;

                Class<? extends Piece> clazz =
                        PieceClassPrefixMap.getPieceClass(classPrefix);

                Piece piece = clazz.getConstructor(
                        int.class, Board.class, String.class, String.class,
                        int.class, int.class, int.class, int.class, int.class
                ).newInstance(
                        playerID, board, type, team,
                        board.getBoardX(), board.getBoardY(),
                        posX, posY, size
                );

                WorldMap.pieces.add(piece);
            }
        }

        GamePanel.sidebarEvaluator.updateEvaluation();
    }

    public static String extractGamemodeFromMapFile(Path file) throws IOException {
        JsonNode root = mapper.readTree(file.toFile());

        JsonNode n = root.get("GameModeType");
        if (n != null && n.isTextual()) return n.asText();

        return null;
    }
}
