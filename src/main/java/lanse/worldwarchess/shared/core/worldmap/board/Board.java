package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.utils.BoardLetterIDCreator;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Board {

    //This class represents a chess board.

    protected int boardX;
    protected int boardY;
    protected int baseTileSize;
    protected int boardSize;
    protected int tileScale;
    protected int boardID;
    protected final int playerID;
    protected final int playerID2;
    protected float borderSize = 10;
    protected Color borderColor;
    protected ArrayList<Board> neighboringBoards = new ArrayList<>();
    protected char whiteTexturePrefix;
    protected char blackTexturePrefix;

    //drawing
    protected static final Map<String, BufferedImage> BOARD_CACHE = new HashMap<>();

    /** REMEMBER TO ADD TO THE BoardClassPrefixMap.java WHEN ADDING A NEW BOARD CLASS*/

    public Board(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        this.boardX = boardX;
        this.boardY = boardY;
        this.boardID = boardID;
        this.baseTileSize = baseTileSize;
        this.boardSize = 8;
        this.tileScale = 1;
        this.borderColor = new Color(100, 100, 100);
        this.playerID = playerID;
        this.playerID2 = playerID2;
        this.whiteTexturePrefix = 'w';
        this.blackTexturePrefix = 'b';

        if (shouldSetBoardPreset){
            BoardPresets.setPreset(preset, boardX, boardY, playerID, playerID2, this, isCommander);
        }

        if (shouldLinkNeighbors) {
            updateBoardNeighbors(this);
        }

        WorldMap.boards.put(boardID, this);
    }

    public void setMapPos(int newBoardX, int newBoardY) {
        this.boardX = newBoardX;
        this.boardY = newBoardY;
    }

    public void setBoardID(int boardID) {
        this.boardID = boardID;
    }

    public static Board createFromPrefix(int prefix, int boardID, int boardX, int boardY) {
        try {
            Class<? extends Board> clazz = Board.class;

            return clazz.getConstructor(
                    int.class, int.class, int.class, String.class,
                    int.class, int.class, int.class,
                    boolean.class, boolean.class, boolean.class
            ).newInstance(
                    boardX, boardY,
                    MapShapePresets.TILE_SIZE,
                    "empty", 0, 0,
                    boardID, false,
                    false,  // DO NOT link neighbors during load
                    true
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to create board from prefix: " + prefix, e);
        }
    }

    //TO BE OVERRIDEN:
    public void tick() {}

    //This code will limit the off board travel check to just the neighboring boards, instead of all boards
    // in the world. This might be laggy at the map creation so this can probably be moved to after all
    // boards were made, or maybe there could be a boolean to update if it should make these
    public static void updateBoardNeighbors(Board newBoard) {
        if (newBoard.neighboringBoards == null){
            newBoard.neighboringBoards = new ArrayList<>();
        }

        double zoomFactor = Main.gamePanel.getZoomFactor();
        int tolerancePixels = LegalMoveDetector.TOLERENCE_PIXELS;

        Rectangle newRegion = GamePanel.getBoardRegion(newBoard, zoomFactor);
        Rectangle expandedNewRegion = expandRect(newRegion, tolerancePixels);

        for (Board other : WorldMap.boards.values()) {
            if (other == newBoard) continue;

            Rectangle otherRegion = GamePanel.getBoardRegion(other, zoomFactor);
            Rectangle expandedOtherRegion = expandRect(otherRegion, tolerancePixels);

            if (expandedNewRegion.intersects(expandedOtherRegion)) {
                if (!newBoard.neighboringBoards.contains(other)) {
                    newBoard.neighboringBoards.add(other);
                }
                if (!other.neighboringBoards.contains(newBoard)) {
                    other.neighboringBoards.add(newBoard);
                }
            }
        }
    }

    public static void linkAllBoardNeighbors() {
        List<Board> allBoards = new ArrayList<>(WorldMap.boards.values());
        int tolerancePixels = LegalMoveDetector.TOLERENCE_PIXELS;
        double zoomFactor = Main.gamePanel.getZoomFactor();

        for (int i = 0; i < allBoards.size(); i++) {
            Board a = allBoards.get(i);
            Rectangle regionA = expandRect(GamePanel.getBoardRegion(a, zoomFactor), tolerancePixels);

            for (int j = i + 1; j < allBoards.size(); j++) {
                Board b = allBoards.get(j);
                Rectangle regionB = expandRect(GamePanel.getBoardRegion(b, zoomFactor), tolerancePixels);

                if (regionA.intersects(regionB)) {
                    a.neighboringBoards.add(b);
                    b.neighboringBoards.add(a);
                }
            }
        }
    }

    //Helper function for getting board neighbors
    private static Rectangle expandRect(Rectangle rect, int margin) {
        return new Rectangle(rect.x - margin, rect.y - margin, rect.width + margin * 2, rect.height + margin * 2);
    }

    public void draw(Graphics2D g2d, double zoomFactor) {
        double scaledBaseTile = baseTileSize * zoomFactor;
        int boardPixelSize = (int) Math.round(boardSize * scaledBaseTile);

        int drawX = (int) Math.round(boardX * zoomFactor);
        int drawY = (int) Math.round(boardY * zoomFactor);

        char prefix = 'u';

        String cacheKey = prefix + "_" + tileScale + "_" + boardSize + "_" + String.format("%.3f", zoomFactor);

        BufferedImage cachedBoard = BOARD_CACHE.get(cacheKey);
        if (cachedBoard == null) {
            cachedBoard = buildBoardImage(zoomFactor, boardPixelSize, prefix);
            BOARD_CACHE.put(cacheKey, cachedBoard);
        }

        g2d.drawImage(cachedBoard, drawX, drawY, null);

        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke((float) (borderSize * zoomFactor)));
        g2d.drawRect(drawX, drawY, boardPixelSize, boardPixelSize);

        Font font = g2d.getFont().deriveFont(Font.BOLD, (float) (26 * zoomFactor));
        g2d.setFont(font);
        Rectangle2D textBounds = font.getStringBounds(String.valueOf(boardID), g2d.getFontRenderContext());
        boolean topRightTileIsWhite = (((boardSize - 1) / tileScale) % 2 == 0);
        g2d.setColor(topRightTileIsWhite ? new Color(80, 60, 40) : Color.WHITE);
        g2d.drawString(String.valueOf(boardID),
                (float) (drawX + boardPixelSize - textBounds.getWidth() - borderSize * zoomFactor),
                (float) (drawY + borderSize * zoomFactor + textBounds.getHeight() / 2));
    }

    private BufferedImage buildBoardImage(double zoomFactor, int boardPixelSize, char prefix) {
        if (boardPixelSize <= 0) return null;

        BufferedImage boardImg = new BufferedImage(boardPixelSize, boardPixelSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = boardImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.scale(zoomFactor, zoomFactor);

        BufferedImage originalWhite = TextureKey.textures.get(this.whiteTexturePrefix + "s");
        BufferedImage originalBlack = TextureKey.textures.get(prefix + "s");
        BufferedImage whiteTile = RenderCache.getUnscaled("ws", originalWhite, baseTileSize, baseTileSize);
        BufferedImage blackTile = RenderCache.getUnscaled(prefix + "s", originalBlack, baseTileSize, baseTileSize);

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                int logicalRow = row / tileScale;
                int logicalCol = col / tileScale;
                BufferedImage tile = ((logicalRow + logicalCol) % 2 == 0) ? whiteTile : blackTile;
                g.drawImage(tile, col * baseTileSize, row * baseTileSize, null);
            }
        }

        label(g);

        g.dispose();
        return boardImg;
    }

    public void drawSelectedBoard(Graphics2D g2d, double zoomFactor) {
        Color old = this.borderColor;
        this.borderColor = new Color(0, 180, 15);
        this.draw(g2d, zoomFactor);
        this.borderColor = old;
    }

    public static void clearBoardCache() { BOARD_CACHE.clear(); }
    public List<Piece> getPieces() {
        return new ArrayList<>(WorldMap.getAllPiecesOfBoard(this));
    }
    public int getBoardX() {
        return boardX;
    }
    public int getBoardY() { return boardY; }
    public int getBoardID() {
        return boardID;
    }

    public Piece getPieceAt(int x, int y, Optional<Piece> attackerPiece, List<Piece> pieceList) {
        int scale = attackerPiece.map(Piece::getPieceSize).orElse(1);

        //This will use a scaled block to get pieces

        for (int i = 0; i < scale; i++) {
            for (int j = 0; j < scale; j++) {
                int bx = x + i;
                int by = y + j;

                for (Piece piece : pieceList) {
                    if (piece.getBoard() != this) continue;
                    int size = piece.getPieceSize();
                    int px = piece.getPosX();
                    int py = piece.getPosY();

                    if (bx >= px && bx < px + size && by >= py && by < py + size) {
                        if (attackerPiece.isPresent() && piece == attackerPiece.get()) continue;
                        return piece;
                    }
                }
            }
        }
        return null;
    }

    //TODO - give this to any piece with a size > 1
    public static Set<Piece> getPiecesAtScaledBlock(Board board, int blockX, int blockY, Piece ignoringPiece) {
        int boardSize = board.getBoardSize();
        int pieceSize = ignoringPiece.getPieceSize();
        Set<Piece> foundPieces = new HashSet<>();
        List<Piece> pieceList = WorldMap.getAllPiecesOfBoard(board);

        //check for pieces within the footprint of the large piece
        for (int dx = 0; dx < pieceSize; dx++) {
            for (int dy = 0; dy < pieceSize; dy++) {
                int x = blockX + dx;
                int y = blockY + dy;

                if (x < 0 || y < 0 || x >= boardSize || y >= boardSize) continue;

                Piece found = board.getPieceAt(x, y, Optional.empty(), pieceList);

                if (found != null && found != ignoringPiece) {
                    foundPieces.add(found);
                }
            }
        }
        return foundPieces;
    }

    public int getBaseTileSize() { return baseTileSize; }
    public int getBoardSize() { return boardSize; }
    public ArrayList<Board> getNeighboringBoards(){return this.neighboringBoards;}

    public List<int[]> getEmptySquares() {
        List<int[]> emptySquares = new ArrayList<>();

        // Track occupied positions
        Set<String> occupiedPositions = new HashSet<>();
        for (Piece piece : WorldMap.getAllPiecesOfBoard(this)) {
            int size = Math.max(1, piece.getPieceSize());
            int px = piece.getPosX();
            int py = piece.getPosY();

            // Mark all tiles the piece occupies
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    occupiedPositions.add((px + dx) + "," + (py + dy));
                }
            }
        }

        // Find empty squares
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                String pos = x + "," + y;
                if (!occupiedPositions.contains(pos)) {
                    emptySquares.add(new int[]{x, y});
                }
            }
        }

        return emptySquares;
    }

    protected Point getRandomEmptyTile() {
        int maxAttempts = 50; // prevent infinite loop
        for (int i = 0; i < maxAttempts; i++) {
            int x = new Random().nextInt(this.boardSize);
            int y = new Random().nextInt(this.boardSize);

            boolean occupied = WorldMap.getAllPiecesOfBoard(this).stream()
                    .anyMatch(p -> p.getPosX() == x && p.getPosY() == y);

            if (!occupied) return new Point(x, y);
        }
        return null; // no free tile found
    }

    public Set<Piece> getPiecesOfRange(Piece ignoringPiece, int range) {
        int scaledRange = range * ignoringPiece.getPieceSize();
        Set<Piece> foundPieces = new HashSet<>();
        int posX = ignoringPiece.getPosX();
        int posY = ignoringPiece.getPosY();

        for (int dx = -scaledRange; dx <= scaledRange; dx++) {
            for (int dy = -scaledRange; dy <= scaledRange; dy++) {
                int x = posX + dx;
                int y = posY + dy;

                Piece p = getPieceAt(x, y, Optional.empty(), WorldMap.getAllPiecesOfBoard(this));

                if (p != null && p != ignoringPiece) {
                    foundPieces.add(p);
                }
            }
        }
        return foundPieces;
    }

    //this code labels the number tile, and colors it the opposite color of the current tile its on.
    protected void label(Graphics2D g) {
        BoardLetterIDCreator colLabeler = new BoardLetterIDCreator();
        Font font = new Font(Map.of(
                TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD,
                TextAttribute.SIZE, 26
        ));
        g.setFont(font);

        for (int i = 0; i < boardSize; i++) {
            String rowLabel = String.valueOf(boardSize - i);
            String colLabel = colLabeler.next();

            boolean rowTileIsWhite = ((i / tileScale) % 2 == 0);
            g.setColor(rowTileIsWhite ? new Color(80, 60, 40) : Color.WHITE);

            float textHeight = (float) font.getStringBounds(rowLabel, g.getFontRenderContext()).getHeight();
            g.drawString(rowLabel, borderSize, i * baseTileSize + textHeight);

            boolean colTileIsWhite = (((boardSize - 1) / tileScale + i / tileScale) % 2 == 0);
            g.setColor(colTileIsWhite ? new Color(80, 60, 40) : Color.WHITE);

            float textWidth = (float) font.getStringBounds(colLabel, g.getFontRenderContext()).getWidth();
            g.drawString(colLabel, (i + 1) * baseTileSize - textWidth - borderSize, boardSize * baseTileSize - borderSize);
        }
    }

    public Board cloneForSandbox(int offsetX, int offsetY) {
        int newBoardID = WorldMap.getNewBoardID();
        boolean isCommander = false;

        try {
            return this.getClass().getConstructor(
                    int.class, int.class, int.class, String.class, int.class, int.class,
                    int.class, boolean.class, boolean.class, boolean.class
            ).newInstance(
                    this.boardX + offsetX,
                    this.boardY + offsetY, this.baseTileSize,
                    "empty", this.playerID, this.playerID2,
                    newBoardID, isCommander, false, false
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone board: " + this.getClass(), e);
        }
    }
}