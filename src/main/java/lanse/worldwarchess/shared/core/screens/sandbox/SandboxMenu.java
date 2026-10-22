package lanse.worldwarchess.shared.core.screens.sandbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.dirs.ProjectDirectories;
import javafx.util.Pair;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.gamemode.GameModeSelector;
import lanse.worldwarchess.shared.core.gamemode.GameModeType;
import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceClassPrefixMap;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.attacks.PieceAttack;
import lanse.worldwarchess.shared.core.piece.ai.bosses.NpcBoss;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.screens.pregame.MainMenu;
import lanse.worldwarchess.shared.core.theme.Colors;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.shared.core.worldmap.board.BoardClassPrefixMap;
import lanse.worldwarchess.shared.core.worldmap.board.CommanderBoard;
import lanse.worldwarchess.shared.core.worldmap.board.badboard.BadBoard;
import org.reflections.Reflections;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SandboxMenu extends JPanel {

    public static final Path sandboxDir =
            Path.of(ProjectDirectories.from("dev", "Lanse", "WorldWarChess").dataDir);
    public static final Path MAPS_DIR = sandboxDir.resolve("maps");
    public static final Path BOARD_PRESETS_DIR = sandboxDir.resolve("board_presets");
    public Path currentMapFile = null;
    public String currentMapName = null;
    public GameModeType currentGamemodeType = GameModeType.WAR;
    private final ObjectMapper mapper = new ObjectMapper();

    private final SandPanel panel;

    //same issue as moveHistory, this saves 3 seconds somehow.
    public static void loadClass(){}

    //Might remove or change this, but it should make sure that the directory exists.
    static {
        try {
            Files.createDirectories(MAPS_DIR);
            Files.createDirectories(BOARD_PRESETS_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SandboxMenu(SandPanel panel, boolean addDisplay) {
        this.panel = panel;

        if (!addDisplay) return;

        setLayout(new GridLayout());
        add(new JCheckBoxMenuItem(new ToggleDragMode()));
        add(new JMenuItem(new DeleteElement()));
        add(new JMenuItem(new CloneElement()));
        add(new JMenuItem(new EditElement()));
        add(new JMenuItem(new Undo()));
        add(new JMenuItem(new Redo()));

        JMenuBar menuBar = new JMenuBar();
        menuBar.setForeground(Colors.text_color);
        menuBar.setBackground(Colors.menu_color);

        JMenu file = new JMenu("File");
        file.add(new LoadMap());
        file.add(new SaveMap());
        file.add(new CreateNewMap());
        file.add(new LoadBoardPreset());
        file.add(new SaveBoardPreset());
        file.add(new SetGameModeType());
        menuBar.add(file);

        JMenu edit = new JMenu("Edit");
        edit.add(new ToggleDragMode());
        edit.add(new Undo());
        edit.add(new Redo());
        menuBar.add(edit);

        JMenu pieces = new JMenu("Pieces");
        JMenu add = new JMenu("Add");
        // Add the boards
        JMenu add_board = new JMenu("Board");
        add_board.add(new AddBoard(Board.class));
        Reflections boardClasses = new Reflections("lanse.worldwarchess.shared.core.worldmap.board");
        for (Class<? extends Board> boardType : boardClasses.getSubTypesOf(Board.class)) {
            add_board.add(new AddBoard(boardType));
        }
        add.add(add_board);
        // Add the pieces
        JMenu add_piece = new JMenu("Piece");
        JMenu regular = new JMenu("Piece");
        JMenu commander = new JMenu("Commander");
        JMenu boss = new JMenu("NPC Boss");
        JMenu attack = new JMenu("Piece Attack");
        JMenu celestial = new JMenu("Celestial");
        regular.add(new AddPiece(Piece.class));
        Reflections pieceClasses = new Reflections("lanse.worldwarchess.shared.core.piece");
        for (Class<? extends Piece> pieceType : pieceClasses.getSubTypesOf(Piece.class)) {
            if (PieceAttack.class.isAssignableFrom(pieceType)) {
                attack.add(new AddPiece(pieceType));
            } else if (NpcBoss.class.isAssignableFrom(pieceType)) {
                boss.add(new AddPiece(pieceType));
            } else if (CommanderPiece.class.isAssignableFrom(pieceType)) {
                commander.add(new AddPiece(pieceType));
            }
            else {
                regular.add(new AddPiece(pieceType));
            }
        }
        add_piece.add(regular);
        add_piece.add(commander);
        add_piece.add(boss);
        add_piece.add(attack);
        add_piece.add(celestial);
        add.add(add_piece);
        pieces.add(add);
        edit.add(new EditElement());
        pieces.add(new CloneElement());
        pieces.add(new DeleteElement());
        menuBar.add(pieces);

        Colors.styleMenuBar(menuBar);
        MainMenu.instance.setJMenuBar(menuBar);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class LoadMap extends AbstractAction {
        public LoadMap() {
            super("Load Map");
            //putValue(MNEMONIC_KEY, KeyEvent.VK_L); Keybind?
        }
        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser chooser = new JFileChooser(MAPS_DIR.toFile());
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int res = chooser.showOpenDialog(panel);
            if (res != JFileChooser.APPROVE_OPTION) return;

            try {
                loadMapFromFile(chooser.getSelectedFile().toPath(), SandboxMenu.this);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel,
                        "Failed to load map.",
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class SaveMap extends AbstractAction {
        public SaveMap() { super("Save Map"); }

        @Override
        public void actionPerformed(ActionEvent e) {
            // if we don't have a current map selected/created, ask the user to create one
            if (currentMapFile == null) {
                int res = JOptionPane.showConfirmDialog(panel,
                        "No current map selected. Create a new map now?",
                        "No Map Selected",
                        JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    new CreateNewMap().actionPerformed(null);
                } else {
                    return;
                }
            }

            // if still no file after asking, abort
            if (currentMapFile == null) {
                JOptionPane.showMessageDialog(panel, "Save cancelled.", "Save Map", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // attempt to save current WorldMap state into currentMapFile
            try {
                saveCurrentMapToFile();
                JOptionPane.showMessageDialog(panel, "Map saved: " + currentMapFile.getFileName(), "Save Map", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Failed to save map: " + ex.getMessage(), "Save Map Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class LoadBoardPreset extends AbstractAction {
        public LoadBoardPreset() {
            super("Load Board Preset");
            //putValue(MNEMONIC_KEY, KeyEvent.VK_L); Keybind?
        }
        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser chooser = new JFileChooser(BOARD_PRESETS_DIR.toFile());
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int res = chooser.showOpenDialog(panel);
            if (res != JFileChooser.APPROVE_OPTION) return;

            try {
                loadBoardPreset(chooser.getSelectedFile().toPath(), 0, 0);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel,
                        "Failed to load board preset.",
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class SaveBoardPreset extends AbstractAction {
        public SaveBoardPreset() { super("Save Board Preset"); }

        @Override
        public void actionPerformed(ActionEvent e) {
            Board currentBoard = Main.gamePanel.getSelectedElements().getValue();
            // if we don't have a current map selected/created, ask the user to create one
            if (currentBoard == null) {
                return;
            }

            String presetName = JOptionPane.showInputDialog(
                    panel,
                    "Enter Board Preset Name:",
                    "New Board Preset",
                    JOptionPane.PLAIN_MESSAGE
            );

            // attempt to save current WorldMap state into currentMapFile
            try {
                saveBoardPreset(currentBoard, presetName);
                JOptionPane.showMessageDialog(panel, "Board Preset saved: " + presetName, "Save Board Preset", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Failed to save map: " + ex.getMessage(), "Save Board Preset Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class CreateNewMap extends AbstractAction {
        public CreateNewMap() { super("Create New Map"); }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(
                    panel,
                    "Enter Map Name:",
                    "New Map",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (name == null) return;
            name = name.trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Invalid map name.", "New Map", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Files.createDirectories(MAPS_DIR);

                Path candidate = MAPS_DIR.resolve(name + ".json");
                if (Files.exists(candidate)) {
                    int confirm = JOptionPane.showConfirmDialog(panel,
                            "A map with that name already exists. Overwrite?",
                            "Overwrite Map",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // write basic skeleton JSON (empty boards/pieces arrays)
                ObjectNode root = mapper.createObjectNode();
                root.put("name", name);
                root.set("boards", mapper.createArrayNode());
                root.set("pieces", mapper.createArrayNode());

                Files.writeString(candidate,
                        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                currentMapFile = candidate;
                currentMapName = name;

                JOptionPane.showMessageDialog(panel, "Created map: " + candidate.getFileName(), "New Map", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Failed to create map: " + ex.getMessage(), "New Map Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class AddBoard extends AbstractAction {
        private final Class<? extends Board> boardType;

        public AddBoard(Class<? extends Board> boardType) {
            super("Add " + boardType.getSimpleName());
            this.boardType = boardType;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (boardType == BadBoard.class) return;
                Board board = Board.createFromPrefix(BoardClassPrefixMap.getPrefixIndex(boardType), WorldMap.getNewBoardID(), 0, 0);
                Main.gamePanel.selectBoard(board);
                SandboxMapDataStack.pushState();
                GamePanel.sidebarEvaluator.updateEvaluation();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class AddPiece extends AbstractAction {
        private final Class<? extends Piece> pieceClass;

        public AddPiece(Class<? extends Piece> pieceClass) {
            super("Add " + pieceClass.getSimpleName());
            this.pieceClass = pieceClass;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Board board = Main.gamePanel.getSelectedElements().getValue();
                if (board == null) {
                    System.err.println("No board selected to place piece");
                    return;
                }

                Piece piece = pieceClass.getConstructor(
                        int.class, Board.class, String.class, String.class,
                        int.class, int.class, int.class, int.class, int.class
                ).newInstance(
                        -1, board, PieceType.KNIGHT.getDisplayName(), "White",
                        board.getBoardX(), board.getBoardY(), 0, 0, 1
                );

                Main.gamePanel.spawnPiece(piece);
                SandboxMapDataStack.pushState();
                GamePanel.sidebarEvaluator.updateEvaluation();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class DeleteElement extends AbstractAction {
        public DeleteElement() {
            super("Delete Element");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //TODO - this should record the action to a stack or something so I can undo / redo
            Pair<Piece, Board> elements = Main.gamePanel.getSelectedElements();
            Piece selectedPiece = elements.getKey();
            Board selectedBoard = elements.getValue();

            if (selectedPiece != null){
                WorldMap.pieces.remove(selectedPiece);
            }

            if (selectedBoard != null){
                WorldMap.pieces.removeIf(piece -> piece.getBoard() == selectedBoard);
                WorldMap.boards.values().remove(selectedBoard);
            }

            Main.gamePanel.deselectElements();
            SandboxMapDataStack.pushState();
            GamePanel.sidebarEvaluator.updateEvaluation();

            Main.gamePanel.repaint();
            Main.gamePanel.revalidate();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class EditElement extends AbstractAction {
        public EditElement() {
            super("Edit Element");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ((SandPanel)Main.gamePanel).showPieceEditor();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class CloneElement extends AbstractAction {
        public CloneElement() {
            super("Clone Element");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Pair<Piece, Board> elements = Main.gamePanel.getSelectedElements();
            Piece selectedPiece = elements.getKey();
            Board selectedBoard = elements.getValue();

            if (selectedPiece != null) {
                Board board = selectedPiece.getBoard();

                Piece cloned = selectedPiece.clonePiece(board);

                cloned.setBoardPosition(selectedPiece.getPosX(), selectedPiece.getPosY());

                Main.gamePanel.spawnPiece(cloned);
                SandboxMapDataStack.pushState();
                GamePanel.sidebarEvaluator.updateEvaluation();
                return;
            }

            if (selectedBoard != null) {

                Board clonedBoard = selectedBoard.cloneForSandbox(selectedBoard.getBoardSize() * selectedBoard.getBaseTileSize(), 0);

                WorldMap.boards.put(clonedBoard.getBoardID(), clonedBoard);

                List<Piece> originals = WorldMap.getAllPiecesOfBoard(selectedBoard);
                for (Piece piece : originals) {
                    Piece clonedPiece = piece.clonePiece(clonedBoard);
                    WorldMap.pieces.add(clonedPiece);
                }

                Main.gamePanel.selectBoard(clonedBoard);
                SandboxMapDataStack.pushState();
                GamePanel.sidebarEvaluator.updateEvaluation();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class SetGameModeType extends AbstractAction {

        public SetGameModeType() {
            super("Set Gamemode");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //THIS FILE IS GETTING TOO LONG SO SCREW IT THIS CLASS WAS ABSTRACTED OUTSIDE
            GameModeSelector dialog = new GameModeSelector(
                    SwingUtilities.getWindowAncestor((Component) e.getSource()),
                    currentGamemodeType
            );

            dialog.setVisible(true);

            GameModeType chosen = dialog.getSelectedType();
            if (chosen != null) {
                currentGamemodeType = chosen;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class Undo extends AbstractAction {
        public Undo() {
            super("Undo");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SandboxMapDataStack.undo();
            GamePanel.sidebarEvaluator.updateEvaluation();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class Redo extends AbstractAction {
        public Redo() {
            super("Redo");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SandboxMapDataStack.redo();
            GamePanel.sidebarEvaluator.updateEvaluation();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class ToggleDragMode extends AbstractAction {
        public ToggleDragMode() {
            super("Toggle Drag Mode");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panel.toggleDragMode();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void saveCurrentMapToFile() throws IOException {
        if (currentMapFile == null) throw new IllegalStateException("No current map file set");

        WorldMap.resetIDs();

        ObjectNode root = mapper.createObjectNode();
        root.put("name", currentMapName == null ? currentMapFile.getFileName().toString() : currentMapName);
        root.put("GameModeType", currentGamemodeType.getDisplayName());

        ArrayNode boards = mapper.createArrayNode();
        WorldMap.boards.values().forEach(board ->
                boards.add(BoardClassPrefixMap.getPrefixIndex(board.getClass()) + "," +
                        board.getBoardID() + "," +
                        board.getBoardSize() + "," +
                        board.getBoardX() + "," +
                        board.getBoardY())
        );
        root.set("boards", boards);

        ArrayNode pieces = mapper.createArrayNode();
        WorldMap.pieces.forEach(piece -> pieces.add(
                        piece.getPieceClassPrefix() + "," +
                                piece.getPlayerID() + "," +
                                piece.getPieceSize() + "," +
                                piece.getBoard().getBoardID() + "," +
                                piece.getPosX() + "," +
                                piece.getPosY() + "," +
                                piece.getType().getDisplayName() + "," +
                                piece.getTeam()
                )
        );
        root.set("pieces", pieces);

        Files.writeString(currentMapFile,
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void loadMapFromFile(Path mapFile, SandboxMenu sandboxMenu) throws IOException {
        if (!Files.exists(mapFile)) {
            throw new IOException("Map file does not exist: " + mapFile);
        }

        WorldMap.boards.clear();
        WorldMap.pieces.clear();

        ObjectNode root = (ObjectNode) sandboxMenu.mapper.readTree(mapFile.toFile());
        sandboxMenu.currentMapFile = mapFile;
        sandboxMenu.currentMapName = root.has("name") ? root.get("name").asText() : mapFile.getFileName().toString();
        sandboxMenu.currentGamemodeType = root.has("GameModeType") ? GameModeType.fromName(root.get("GameModeType").asText()) : GameModeType.WAR;

        ArrayNode boards = (ArrayNode) root.get("boards");
        if (boards != null) {
            for (int i = 0; i < boards.size(); i++) {
                String[] data = boards.get(i).asText().split(",");

                int prefix = Integer.parseInt(data[0]);
                int boardID = Integer.parseInt(data[1]);
                int boardSize = Integer.parseInt(data[2]);
                int boardX = Integer.parseInt(data[3]);
                int boardY  = Integer.parseInt(data[4]);

                Board.createFromPrefix(prefix, boardID, boardX, boardY);
            }
        }

        // link after creation
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
                if (board == null) {
                    System.err.println("Skipping piece, no board at with ID " + boardID);
                    continue;
                }

                try {
                    Class<? extends Piece> clazz = PieceClassPrefixMap.getPieceClass(classPrefix);

                    Piece piece = clazz.getConstructor(
                            int.class, Board.class, String.class, String.class,
                            int.class, int.class, int.class, int.class, int.class
                    ).newInstance(
                            playerID, board, type, team,
                            board.getBoardX(), board.getBoardY(), posX, posY, size
                    );

                    WorldMap.pieces.add(piece);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        GamePanel.sidebarEvaluator.updateEvaluation();
    }

    // Map loading / saving above
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Board Preset loading / saving below

    private void saveBoardPreset(Board board, String presetName) throws IOException {
        ObjectNode root = mapper.createObjectNode();

        ObjectNode boardNode = mapper.createObjectNode();
        boardNode.put("prefix", BoardClassPrefixMap.getPrefixIndex(board.getClass()));
        boardNode.put("size", board.getBoardSize());
        boardNode.put("isCommander", board.getClass().isAssignableFrom(CommanderBoard.class));
        root.set("board", boardNode);

        ArrayNode pieces = mapper.createArrayNode();
        for (Piece piece : WorldMap.getAllPiecesOfBoard(board)) {
            pieces.add(
                    piece.getPieceClassPrefix() + "," +
                            piece.getPlayerID() + "," +
                            piece.getPieceSize() + "," +
                            piece.getPosX() + "," +
                            piece.getPosY() + "," +
                            piece.getType().getDisplayName() + "," +
                            piece.getTeam()
            );
        }
        root.set("pieces", pieces);

        Path file = BOARD_PRESETS_DIR.resolve(presetName + ".json");

        Files.writeString(
                file,
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private void loadBoardPreset(Path presetFile, int worldX, int worldY) throws IOException {
        ObjectNode root = (ObjectNode) mapper.readTree(presetFile.toFile());

        String presetName = presetFile.getFileName()
                .toString()
                .replaceFirst("\\.json$", "");

        ObjectNode boardNode = (ObjectNode) root.get("board");

        //TODO - make use of the size
        int prefix = boardNode.get("prefix").asInt();
        int size = boardNode.get("size").asInt();
        int baseTile = MapShapePresets.TILE_SIZE;
        boolean isCommander = boardNode.get("isCommander").asBoolean();

        int newBoardID = WorldMap.getNewBoardID();

        Board board = Board.createFromPrefix(prefix, newBoardID, worldX, worldY);

        WorldMap.boards.put(newBoardID, board);

        ArrayNode pieces = (ArrayNode) root.get("pieces");
        if (pieces != null) {
            for (int i = 0; i < pieces.size(); i++) {
                String[] data = pieces.get(i).asText().split(",");

                int classPrefix = Integer.parseInt(data[0]);
                int playerID = Integer.parseInt(data[1]);
                int pieceSize = Integer.parseInt(data[2]);
                int posX = Integer.parseInt(data[3]);
                int posY = Integer.parseInt(data[4]);
                String type = data[5];
                String team = data[6];

                try {
                    Class<? extends Piece> clazz =
                            PieceClassPrefixMap.getPieceClass(classPrefix);

                    Piece piece = clazz.getConstructor(
                            int.class, Board.class, String.class, String.class,
                            int.class, int.class, int.class, int.class, int.class
                    ).newInstance(
                            playerID, board, type, team, board.getBoardX(),
                            board.getBoardY(), posX, posY, pieceSize
                    );

                    WorldMap.pieces.add(piece);

                } catch (Exception ex) {
                    System.err.println("Failed to load preset piece from " + presetFile);
                    ex.printStackTrace();
                }
            }
        }

        Board.updateBoardNeighbors(board);
        SandboxMapDataStack.clearSandboxMapStackData();
        SandboxMapDataStack.pushState();
        GamePanel.sidebarEvaluator.updateEvaluation();
    }
}