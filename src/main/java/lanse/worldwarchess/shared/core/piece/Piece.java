package lanse.worldwarchess.shared.core.piece;

import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.Utils;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static lanse.worldwarchess.shared.core.piece.move.LegalMoveDrawer.drawMove;

public final class Piece {

    // This class is responsible for a piece object. There are a ton of subclasses for piece, so this is one of
    // the most central classes. It has a ton of polymorphism and functions to be overridden.
    // It is now final so there will be no polymorphism or functions to be overridden.

    private final int playerID;
    private final PieceType type;
    private final boolean team; // 0 = white, 1 = black
    private int posX, posY;      // Position within the board
    public boolean hasMovedEver = false;

    /** REMEMBER TO ADD TO THE PieceClassPrefixMap.java WHEN ADDING A NEW PIECE CLASS*/

    public Piece(int playerId, PieceType type, boolean team, int posX, int posY) {
        this.playerID = playerId;
        this.type = type;
        this.team = team;
        this.posX = posX;
        this.posY = posY;
    }

    public int getPlayerID() { return playerID; }
    public PieceType getType() { return type; }

    /** True = White, False = Black. **/
    public boolean getTeam() { return team; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getPieceClassPrefix() {
        return 1;
    }

    public void setBoardPosition(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public void draw(Graphics2D g2d, double zoomFactor) {
        Board board = WorldMap.board;
        int baseTile = board.getBaseTileSize();
        double scaledBaseTile = baseTile * zoomFactor;
        int pieceSize = (int) scaledBaseTile;
        int drawX = (int) (board.getBoardX() * zoomFactor + this.getPosX() * scaledBaseTile);
        int drawY = (int) (board.getBoardY() * zoomFactor + this.getPosY() * scaledBaseTile);

        // Skip if offscreen
        Rectangle clip = g2d.getClipBounds();
        if (clip != null && !clip.intersects(drawX, drawY, pieceSize, pieceSize)) {
            return;
        }

        String textureKey = TextureKey.getTextureKey(this);
        BufferedImage original = TextureKey.textures.get(textureKey);
        if (original == null) return;

        BufferedImage scaled = RenderCache.getScaled(textureKey, zoomFactor, original, pieceSize, pieceSize);
        if (scaled == null) return;

        Composite old = g2d.getComposite();

        g2d.drawImage(scaled, drawX, drawY, null);

        g2d.setComposite(old);
    }

    public void drawSelectedPiece(boolean pieceDragging, Board selectedBoard, Point currentDragPoint, Point dragOffset, List<LegalMoveDetector.Coordinate> legalMoves, double zoomFactor, Graphics2D g2d){
        // Draw the dragged piece on top

        if (pieceDragging && selectedBoard != null) {
            int baseTile = selectedBoard.getBaseTileSize();
            int scaledPieceSize = (int) (baseTile * zoomFactor);
            int drawX = currentDragPoint.x - LegalMoveDetector.OFFSET_CORRECTION_VALUE;
            int drawY = currentDragPoint.y - LegalMoveDetector.OFFSET_CORRECTION_VALUE;
            String textureKey = TextureKey.getTextureKey(this);
            BufferedImage img = TextureKey.textures.get(textureKey);

            // Draw a texture showing where it will end up
            LegalMoveDetector.Coordinate real_point = Utils.pxToLogicalCoords(currentDragPoint, zoomFactor);

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
        return Stream.of(new LegalMoveDetector.Coordinate(posX, posY));
    }

    public Piece clonePiece() {
        Piece clone = new Piece(playerID, type, team, posX, posY);
        clone.hasMovedEver = this.hasMovedEver;
        return clone;
    }

    public int getValue() {
        return switch (type){
            case PAWN -> 1;
            case BISHOP -> 3;
            case KNIGHT -> 3;
            case ROOK -> 5;
            case QUEEN -> 9;
            case KING -> 2500;
        };
    }
}