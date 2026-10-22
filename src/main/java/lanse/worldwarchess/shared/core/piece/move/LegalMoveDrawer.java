package lanse.worldwarchess.shared.core.piece.move;

import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

//        g2d.setColor(Color.RED);
//        for (Point p : LegalMoveDetector.debugRawPoints) {
//            int sx = (int) Math.round(p.x * zoomFactor);
//            int sy = (int) Math.round(p.y * zoomFactor);
//            g2d.fillOval(sx - 4, sy - 4, 8, 8);
//        }

        g2d.setComposite(oldComposite);
    }

    public static void drawMove(Piece selectedPiece, Graphics2D g2d, double zoomFactor, LegalMoveDetector.Coordinate move, BufferedImage captureTexture, BufferedImage legalMoveTexture) {
        Board moveBoard = WorldMap.getBoardByID(move.boardID());
        if (moveBoard == null) return;

        int baseTile = moveBoard.getBaseTileSize();
        double scaledTile = baseTile * zoomFactor;

        double rawX = moveBoard.getBoardX() * zoomFactor + move.x() * scaledTile;
        double rawY = moveBoard.getBoardY() * zoomFactor + move.y() * scaledTile;

        int drawX = (int) Math.round(rawX);
        int drawY = (int) Math.round(rawY);

        int highlightSize = (int) Math.round(selectedPiece.getPieceSize() * scaledTile);

        Piece targetPiece = moveBoard.getPieceAt(move.x(), move.y(), Optional.of(selectedPiece), WorldMap.getAllPiecesOfBoard(moveBoard));

        BufferedImage textureToDraw = (targetPiece != null) ? captureTexture : legalMoveTexture;

        g2d.drawImage(textureToDraw, drawX, drawY, highlightSize, highlightSize, null);
    }

    public static void drawAllTeamMoves(Graphics2D g2d, double zoomFactor) {
        String team = CascadingClient.team; // Your team

        BufferedImage visibleTexture = TextureKey.textures.get("VisibleSquare");
        BufferedImage visibleCaptureTexture = TextureKey.textures.get("VisibleCapture");
        if (visibleTexture == null || visibleCaptureTexture == null) return;

        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

        for (Map.Entry<LegalMoveDetector.Coordinate, List<String>> entry : NPC.attackedSquares.entrySet()) {
            LegalMoveDetector.Coordinate coord = entry.getKey();
            List<String> teamsAttacking = entry.getValue();

            if (!teamsAttacking.contains(team)) continue;

            Board board = WorldMap.getBoardByID(coord.boardID());
            if (board == null) continue;

            int baseTile = board.getBaseTileSize();
            double scaledTile = baseTile * zoomFactor;

            double rawX = board.getBoardX() * zoomFactor + coord.x() * scaledTile;
            double rawY = board.getBoardY() * zoomFactor + coord.y() * scaledTile;

            int drawX = (int) Math.round(rawX);
            int drawY = (int) Math.round(rawY);

            // Check if a piece exists on this square
            Piece targetPiece = board.getPieceAt(coord.x(), coord.y(), Optional.empty(), WorldMap.getAllPiecesOfBoard(board));

            BufferedImage textureToDraw = (targetPiece != null) ? visibleCaptureTexture : visibleTexture;

            int highlightSize = (int) Math.round(scaledTile);
            g2d.drawImage(textureToDraw, drawX, drawY, highlightSize, highlightSize, null);
        }

        g2d.setComposite(oldComposite);
    }
}