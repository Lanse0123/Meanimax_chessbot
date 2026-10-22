package lanse.worldwarchess.shared.core.piece;

import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.Utils;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static lanse.worldwarchess.shared.core.piece.move.LegalMoveDrawer.drawMove;

public class Piece {

    //This class is responsible for a piece object. There are a ton of subclasses for piece, so this is one of
    // the most central classes. It has a ton of polymorphism and functions to be overriden.

    protected int playerID;
    protected Board board;
    protected PieceType type;
    protected String team;
    protected int boardX, boardY;  // Position on the chessboard grid
    protected int posX, posY;      // Position within the board
    protected int pieceSize;
    public boolean hasMoved = false;
    public boolean hasMovedEver = false;
    protected String playerName;

    /** REMEMBER TO ADD TO THE PieceClassPrefixMap.java WHEN ADDING A NEW PIECE CLASS*/

    public Piece(int playerId, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int pieceSize) {
        this.playerID = playerId;
        this.board = board;
        this.type = PieceType.fromName(type);
        this.team = team;
        this.boardX = boardX;
        this.boardY = boardY;
        this.posX = posX;
        this.posY = posY;
        this.pieceSize = pieceSize;

        this.playerName = "";
    }

    public int getPlayerID() { return playerID; }
    public Board getBoard() { return board; }
    public PieceType getType() { return type; }
    public String getTeam() { return team; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getBoardX() { return boardX; }
    public int getBoardY() { return boardY; }
    public int getPieceSize() { return pieceSize; }
    public int getPieceClassPrefix() { return PieceClassPrefixMap.getPrefixIndex(this.getClass()); }
    public String getPlayerName(){ return playerName; }

    public void setMapPosition(int boardX, int boardY) {
        this.boardX = (boardX / pieceSize) * pieceSize;
        this.boardY = (boardY / pieceSize) * pieceSize;
    }
    public void setBoardPosition(int posX, int posY) {
        this.posX = (posX / pieceSize) * pieceSize; // Snap to grid of piece size
        this.posY = (posY / pieceSize) * pieceSize;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }
    public void setPlayerID(int playerID) { this.playerID = playerID; }

    //TO BE OVERRIDEN:
    public void move(){}
    public void heal(int amount){}
    public void setHealth(int bossHealth) {}
    public int getCurrentHealth() { return 0; }
    public Piece promoteOverride(Piece piece) {return null;}
    public char getPrefixReplacement(){return 0;}
    public String getTexture() { return null; }
    public int getBarPoints() { return 0; }
    public Collection<LegalMoveDetector.Coordinate> getMovementOverride() { return Collections.emptyList(); }

    public void setSandboxInfo(PieceType chosenType, String chosenTeam) {
        this.type = chosenType;
        this.team = chosenTeam;
    }

    public void setType(PieceType type){
        this.type = type;
    }

    public void draw(Graphics2D g2d, double zoomFactor) {
        Board board = this.getBoard();
        int baseTile = board.getBaseTileSize();
        double scaledBaseTile = baseTile * zoomFactor;
        int pieceSize = (int) (this.getPieceSize() * scaledBaseTile);
        int drawX = (int) (board.getBoardX() * zoomFactor + this.getPosX() * scaledBaseTile);
        int drawY = (int) (board.getBoardY() * zoomFactor + this.getPosY() * scaledBaseTile);

        // Skip if offscreen
        Rectangle clip = g2d.getClipBounds();
        if (clip != null && !clip.intersects(drawX, drawY, pieceSize, pieceSize)) {
            return;
        }

        if (GameMode.isFogOfWar && !team.equals(CascadingClient.team)) {
            LegalMoveDetector.Coordinate coord = new LegalMoveDetector.Coordinate(
                    this.getPosX(), this.getPosY(), board.getBoardID());

            List<String> attackers = NPC.attackedSquares.get(coord);

            if (attackers == null || !attackers.contains(CascadingClient.team)) {
                return;
            }
        }

        String textureKey = TextureKey.getTextureKey(this);
        BufferedImage original = TextureKey.textures.get(textureKey);
        if (original == null) return;

        BufferedImage scaled = RenderCache.getScaled(textureKey, zoomFactor, original, pieceSize, pieceSize);
        if (scaled == null) return;

        Composite old = g2d.getComposite();
        if (this.hasMoved) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }

        g2d.drawImage(scaled, drawX, drawY, null);

        //if this is your piece, draw a glow around it.
        if (this.playerID == CascadingClient.playerID) {
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke((float)(6 * zoomFactor)));
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawRect(drawX, drawY, pieceSize, pieceSize);
            g2d.setStroke(oldStroke);
        }

        //draw name above piece if it has one
        String name = this.getPlayerName();
        if (name != null && !name.isEmpty()) {
            Font baseFont = g2d.getFont();
            Font scaledFont = baseFont.deriveFont(Font.BOLD, (float) pieceSize);
            g2d.setFont(scaledFont);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(name);

            int textX = drawX + (pieceSize - textWidth) / 2;
            int textY = drawY - (int) (4 * zoomFactor); // small padding above piece

            GlyphVector gv = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), name);
            Shape textShape = gv.getOutline(textX, textY);

            // Draw outline
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke((float) (25f * zoomFactor))); // how THICC the outline is
            g2d.draw(textShape);

            g2d.setColor(Color.WHITE);
            g2d.fill(textShape);
        }

        g2d.setComposite(old);
    }

    public void drawSelectedPiece(boolean pieceDragging, Board selectedBoard, Point currentDragPoint, Point dragOffset, List<LegalMoveDetector.Coordinate> legalMoves, double zoomFactor, Graphics2D g2d){
        // Draw the dragged piece on top

        if (GameMode.isSandbox){
            int oldX = board.getBoardX();
            int oldY = board.getBoardY();
            if (pieceDragging) {
                board.setMapPos(currentDragPoint.x - dragOffset.x, currentDragPoint.y - dragOffset.y);
            }
            this.draw(g2d, zoomFactor);

            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke((float)(6 * zoomFactor)));
            g2d.setColor(new Color(0, 180, 0));

            Board board = this.getBoard();
            int baseTile = board.getBaseTileSize();
            double scaledBaseTile = baseTile * zoomFactor;
            int pieceSize = (int) (this.getPieceSize() * scaledBaseTile);
            int drawX = (int) (board.getBoardX() * zoomFactor + this.getPosX() * scaledBaseTile);
            int drawY = (int) (board.getBoardY() * zoomFactor + this.getPosY() * scaledBaseTile);

            g2d.drawRect(drawX, drawY, pieceSize, pieceSize);
            g2d.setStroke(oldStroke);
            board.setMapPos(oldX, oldY);
            return;
        }

        if (pieceDragging && selectedBoard != null) {
            int baseTile = selectedBoard.getBaseTileSize();
            int scaledPieceSize = (int) (pieceSize * baseTile * zoomFactor);
            int drawX = currentDragPoint.x - LegalMoveDetector.OFFSET_CORRECTION_VALUE;
            int drawY = currentDragPoint.y - LegalMoveDetector.OFFSET_CORRECTION_VALUE;
            String textureKey = TextureKey.getTextureKey(this);
            BufferedImage img = TextureKey.textures.get(textureKey);

            // Draw a texture showing where it will end up
            LegalMoveDetector.Coordinate real_point = Utils.pxToLogicalCoords(currentDragPoint, this, zoomFactor);

            // If debug mode, show a pointer over the original square of the piece
            if (Main.debugMode && real_point.boardID() != -404) {
                double scaledBaseTile = baseTile * zoomFactor;
                int homeDrawX = (int) (WorldMap.getBoardByID(real_point.boardID()).getBoardX() * zoomFactor + real_point.x() * scaledBaseTile);
                int homeDrawY = (int) (WorldMap.getBoardByID(real_point.boardID()).getBoardY() * zoomFactor + real_point.y() * scaledBaseTile);

                g2d.drawImage(TextureKey.textures.get("pointer"), homeDrawX, homeDrawY, scaledPieceSize, scaledPieceSize, null);
            }

            boolean onOriginalSquare = squares().anyMatch((square) -> square.equals(real_point));

            if (!onOriginalSquare) {
                Optional<LegalMoveDetector.Coordinate> nearestLegalMove = Utils.nearestLegalCoordinate(real_point, legalMoves);
                if (nearestLegalMove.isPresent()) {
                    BufferedImage snapTexture = TextureKey.textures.get("VisibleSquare");
                    BufferedImage snapCaptureTexture = TextureKey.textures.get("VisibleCapture");
                    drawMove(this, g2d, zoomFactor, nearestLegalMove.get(), snapCaptureTexture, snapTexture);
                    drawMove(this, g2d, zoomFactor, nearestLegalMove.get(), img, img);
                } else {
                    if (img != null) {
                        g2d.drawImage(img, drawX, drawY, scaledPieceSize, scaledPieceSize, null);
                    }
                }
            }
            else {
                if (img != null) {
                    g2d.drawImage(img, drawX, drawY, scaledPieceSize, scaledPieceSize, null);
                }
            }
        }
    }

    public Stream<LegalMoveDetector.Coordinate> squares() {
        return IntStream.range(0, pieceSize).mapToObj(
                (x) -> IntStream.range(0, pieceSize).mapToObj(
                        (y) -> new LegalMoveDetector.Coordinate(posX + x, posY + y, board.getBoardID())
                )
        ).flatMap((x) -> x);
    }

    public Stream<LegalMoveDetector.Coordinate> squaresAt(int newX, int newY, Board newBoard) {
        return IntStream.range(0, pieceSize).mapToObj(
                (x) -> IntStream.range(0, pieceSize).mapToObj(
                        (y) -> new LegalMoveDetector.Coordinate(newX + x, newY + y, newBoard.getBoardID())
                )
        ).flatMap((x) -> x);
    }

    public Piece clonePiece(Board newBoard) {
        try {
            return this.getClass().getConstructor(
                    int.class, Board.class, String.class, String.class,
                    int.class, int.class, int.class, int.class, int.class
            ).newInstance(
                    this.getPlayerID(), newBoard, this.getType().name(), this.getTeam(),
                    newBoard.getBoardX(), newBoard.getBoardY(), this.getPosX(),
                    this.getPosY(), this.getPieceSize()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone piece: " + this.getClass(), e);
        }
    }
}