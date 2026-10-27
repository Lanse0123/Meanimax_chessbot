package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class LegalMoveDrawer {

    // Responsible for drawing all legal move highlights for a selected piece.
    public static void draw(boolean pieceDragging, Piece selectedPiece, List<LegalMoveDetector.Coordinate> legalMoves, Graphics2D g2d, double zoomFactor) {
        if (!pieceDragging || selectedPiece == null) return;

        if (legalMoves == null || legalMoves.isEmpty()) return;

        BufferedImage legalMoveTexture = TextureKey.textures.get("LegalSquare");
        BufferedImage captureTexture = TextureKey.textures.get("CaptureSquare");
        if (legalMoveTexture == null || captureTexture == null) return;

        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

        for (LegalMoveDetector.Coordinate move : legalMoves) {
            drawMove(selectedPiece, g2d, zoomFactor, move, captureTexture, legalMoveTexture);
        }

        g2d.setComposite(oldComposite);
    }

    public static void drawMove(Piece selectedPiece, Graphics2D g2d, double zoomFactor, LegalMoveDetector.Coordinate move, BufferedImage captureTexture, BufferedImage legalMoveTexture) {
        Board moveBoard = WorldMap.getBoard();
        if (moveBoard == null) return;

        int baseTile = moveBoard.getBaseTileSize();
        double scaledTile = baseTile * zoomFactor;

        double rawX = moveBoard.getBoardX() * zoomFactor + move.x() * scaledTile;
        double rawY = moveBoard.getBoardY() * zoomFactor + move.y() * scaledTile;

        int drawX = (int) Math.round(rawX);
        int drawY = (int) Math.round(rawY);

        int highlightSize = (int) Math.round(scaledTile);

        Piece targetPiece = moveBoard.getPieceAt(move.x(), move.y(), selectedPiece, WorldMap.getMainPieces());

        BufferedImage textureToDraw = (targetPiece != null) ? captureTexture : legalMoveTexture;

        g2d.drawImage(textureToDraw, drawX, drawY, highlightSize, highlightSize, null);
    }
}