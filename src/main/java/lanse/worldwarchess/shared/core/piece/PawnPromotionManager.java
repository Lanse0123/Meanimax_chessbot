package lanse.worldwarchess.shared.core.piece;

import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PawnPromotionManager {

    //This class is responsible for making a screen for promoting a pawn, and logic for promotion.
    private final List<PawnPromotion> promotions = new ArrayList<>();
    private static final PawnPromotionManager instance = new PawnPromotionManager();

    private PawnPromotionManager() {}
    public static PawnPromotionManager getInstance() {
        return instance;
    }

    public void addPromotion(Piece pawn, Board board, int logicalX, int logicalY, boolean isOurMove) {
        promotions.add(new PawnPromotion(pawn, board, logicalX, logicalY));
    }

    public void drawPromotions(Graphics2D g, double zoomFactor) {
        for (PawnPromotion promo : promotions) {
            promo.draw(g, zoomFactor);
        }
    }

    public boolean processClick(Point clickPoint) {
        List<PawnPromotion> activePromotions = new ArrayList<>(promotions);
        for (PawnPromotion promo : activePromotions) {
            String chosen = promo.processClick(clickPoint);
            if (chosen != null) {
                promotePawn(promo, chosen);
                promotions.remove(promo);
                return true;
            }
        }
        return false;
    }

    private void promotePawn(PawnPromotion promo, String newType) {
        Piece pawn = promo.pawn;
        int playerID = pawn.getPlayerID();
        Board board = promo.board;
        int boardX = promo.board.getBoardX();
        int boardY = promo.board.getBoardY();
        int posX = promo.logicalX;
        int posY = promo.logicalY;
        Piece newPiece = new Piece(playerID, board, newType, pawn.getTeam(), boardX, boardY, posX, posY, 1);
        WorldMap.erasePiece(pawn, false);
        WorldMap.spawnPiece(newPiece, false);

        MoveHistory.addPromotionForRecord(newPiece, true);
    }

    private static class PawnPromotion {
        private final Piece pawn;
        private final Board board;
        private final int logicalX;
        private final int logicalY;
        private Rectangle optionKnight;
        private Rectangle optionBishop;
        private Rectangle optionRook;

        public PawnPromotion(Piece pawn, Board board, int logicalX, int logicalY) {
            this.pawn = pawn;
            this.board = board;
            this.logicalX = logicalX;
            this.logicalY = logicalY;
        }

        public void draw(Graphics2D g, double zoomFactor) {
            int baseTile = board.getBaseTileSize();
            int tileSize = (int)(baseTile * zoomFactor);

            int regionX = (int)(board.getBoardX() * zoomFactor);
            int regionY = (int)(board.getBoardY() * zoomFactor);
            int promoX = regionX + logicalX * tileSize;
            int promoY = regionY + logicalY * tileSize;

            int bubbleY = promoY - tileSize - 10;
            int bubbleWidth = tileSize * 3 + 20;
            int bubbleHeight = tileSize + 20;

            g.setColor(new Color(0, 0, 0, 200));
            g.fillRoundRect(promoX - 10, bubbleY - 10, bubbleWidth, bubbleHeight, 10, 10);

            String prefix = pawn.getTeam().equalsIgnoreCase("White") ? "w" : "b";
            BufferedImage knightImg = TextureKey.textures.get(prefix + "n");
            BufferedImage bishopImg = TextureKey.textures.get(prefix + "b");
            BufferedImage rookImg = TextureKey.textures.get(prefix + "r");

            int iconX2 = promoX + tileSize + 10;
            int iconX3 = promoX + 2 * (tileSize + 10);
            if (knightImg != null) g.drawImage(knightImg, promoX, bubbleY, tileSize, tileSize, null);
            if (bishopImg != null) g.drawImage(bishopImg, iconX2, bubbleY, tileSize, tileSize, null);
            if (rookImg != null) g.drawImage(rookImg, iconX3, bubbleY, tileSize, tileSize, null);

            optionKnight = new Rectangle(promoX, bubbleY, tileSize, tileSize);
            optionBishop = new Rectangle(iconX2, bubbleY, tileSize, tileSize);
            optionRook = new Rectangle(iconX3, bubbleY, tileSize, tileSize);
        }

        public String processClick(Point clickPoint) {
            if (optionKnight != null && optionKnight.contains(clickPoint)) return "knight";
            if (optionBishop != null && optionBishop.contains(clickPoint)) return "bishop";
            if (optionRook != null && optionRook.contains(clickPoint)) return "rook";

            double distKnight = optionKnight != null ? clickDistance(optionKnight, clickPoint.x, clickPoint.y) : Double.MAX_VALUE;
            double distBishop = optionBishop != null ? clickDistance(optionBishop, clickPoint.x, clickPoint.y) : Double.MAX_VALUE;
            double distRook = optionRook != null ? clickDistance(optionRook, clickPoint.x, clickPoint.y) : Double.MAX_VALUE;
            double minDistance = Math.min(distKnight, Math.min(distBishop, distRook));

            if (minDistance <= 20) {
                if (minDistance == distKnight) return "knight";
                if (minDistance == distBishop) return "bishop";
                if (minDistance == distRook) return "rook";
            }
            return null;
        }

        private double clickDistance(Rectangle option, int clickX, int clickY) {
            int centerX = option.x + option.width / 2;
            int centerY = option.y + option.height / 2;
            return Math.sqrt(Math.pow(centerX - clickX, 2) + Math.pow(centerY - clickY, 2));
        }
    }
}