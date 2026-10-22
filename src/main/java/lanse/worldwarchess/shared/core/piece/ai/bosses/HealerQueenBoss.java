package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.AutoHealable;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class HealerQueenBoss extends NpcBoss implements AutoHealable {

    public HealerQueenBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "queen", color, boardX, boardY, posX, posY, 2);
        this.barPoints = 10;
        this.maxHealth = 20;
        this.attack = 3;
        this.pieceSize = 2;
        this.movementChance = 1;
        this.texture = "healerqueen";
        // Other: Has a visible heal pool around it within 4 blocks.
        // All bosses within this range will get +5 hp per turn. OVERRIDE piece.draw FOR THIS

        setCurrentHealthToMax();
    }

    @Override
    public void move() {
        NPC.healerMoveLogic(this);

        for (Piece piece : WorldMap.pieces){
            if (piece == this || piece.getBoard() != this.board) continue;

            // if piece is more than 4 away from the queen, dont heal it
            if (Math.abs(piece.getPosX() - this.posX) > 4 || Math.abs(piece.getPosY() - this.posY) > 4) continue;

            if (Objects.equals(piece.getTeam(), this.team) && (piece instanceof NpcBoss || piece instanceof CommanderPiece)){
                piece.heal(5);
            }
        }
    }

    public void passiveHeal(){ this.currentHealth = Math.min(maxHealth, currentHealth + 1); }

    @Override
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

        //Draw heal pool
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke((float) (6 * zoomFactor)));
        double radiusTiles = 4.0;
        double totalTiles = radiusTiles * 2 + 1;
        double poolSize = totalTiles * scaledBaseTile;
        double poolX = drawX + pieceSize / 2.0 - poolSize / 2.0;
        double poolY = drawY + pieceSize / 2.0 - poolSize / 2.0;
        g2d.setColor(new Color(255, 50, 50, 100));
        g2d.fillRect((int) poolX, (int) poolY, (int) poolSize, (int) poolSize);
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawRect((int) poolX, (int) poolY, (int) poolSize, (int) poolSize);
        g2d.setStroke(oldStroke);


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

        //health bar
        if (maxHealth != currentHealth) {
            //TODO - same crappy solution as NPCBoss and Commander
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            int barWidth = (int) (pieceSize * 0.9);
            int barHeight = (int) (6 * zoomFactor);
            int barX = drawX + (pieceSize - barWidth) / 2;
            int barY = (int) (drawY + (pieceSize * 1.05) + (int) (4 * zoomFactor));

            g2d.setColor(Color.BLACK);
            g2d.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

            double healthRatio = Math.max(0, Math.min(1.0, (double) currentHealth / maxHealth));
            int filledWidth = (int) (barWidth * healthRatio);

            g2d.setColor(new Color(0, 255, 0));
            g2d.fillRect(barX, barY, filledWidth, barHeight);

            g2d.setColor(new Color(0, 128, 0));
            g2d.drawRect(barX, barY, barWidth, barHeight);
        }

        g2d.setComposite(old);
    }
}
