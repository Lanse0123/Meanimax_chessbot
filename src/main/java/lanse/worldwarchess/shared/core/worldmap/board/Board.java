package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.utils.BoardLetterIDCreator;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    //This class represents a chess board.

    protected int boardX;
    protected int boardY;
    protected int baseTileSize;
    protected int boardSize;
    protected int tileScale;
    protected final int playerID;
    protected final int playerID2;
    protected float borderSize = 10;
    protected Color borderColor;
    protected char whiteTexturePrefix;
    protected char blackTexturePrefix;

    //drawing
    protected static final Map<String, BufferedImage> BOARD_CACHE = new HashMap<>();

    /** REMEMBER TO ADD TO THE BoardClassPrefixMap.java WHEN ADDING A NEW BOARD CLASS*/

    public Board(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, boolean isCommander, boolean shouldSetBoardPreset) {
        this.boardX = boardX;
        this.boardY = boardY;
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
    }

    public static void clearBoardCache() { BOARD_CACHE.clear(); }
    public List<Piece> getPieces() {
        return new ArrayList<>(WorldMap.getAllPiecesOfBoard(this));
    }
    public int getBoardX() {
        return boardX;
    }
    public int getBoardY() { return boardY; }

    public Piece getPieceAt(int x, int y, Piece attackerPiece, List<Piece> pieceList) {
        for (Piece piece : pieceList) {
            if (piece == attackerPiece || piece.getBoard() != this) {
                continue;
            }

            if (piece.getPosX() == x && piece.getPosY() == y) {
                return piece;
            }
        }

        return null;
    }

    public int getBaseTileSize() { return baseTileSize; }
    public int getBoardSize() { return boardSize; }

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

    //this code labels the number tile, and colors it the opposite color of the current tile its on.
    protected void label(Graphics2D g) {
        BoardLetterIDCreator colLabeler = new BoardLetterIDCreator();
        Font font = new Font(Map.of(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD, TextAttribute.SIZE, 26));
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
}