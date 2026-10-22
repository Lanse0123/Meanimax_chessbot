package lanse.worldwarchess.shared.core.piece;

import lanse.worldwarchess.cascading.CascadingClient;
import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.piece.ai.AutoHealable;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;

/** REMEMBER TO ADD TO THE PieceClassPrefixMap.java WHEN ADDING A NEW PIECE CLASS*/
public class CommanderPiece extends Piece implements AutoHealable {

    protected int maxHealth = 100;
    protected int currentHealth = 100;

    //This class is the 2nd most central Piece class, which represents one of the large Piece bossfight objects.
    public CommanderPiece(int playerID, Board board, String type, String team, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerID, board, type, team, boardX, boardY, posX, posY, pieceSize);
        this.pieceSize = 2;
    }

    //TODO - this is duplicated code with NPCBoss.java
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

        if (GameMode.isFogOfWar && !team.equals(CascadingClient.team)) {
            LegalMoveDetector.Coordinate coord = new LegalMoveDetector.Coordinate(
                    this.getPosX(), this.getPosY(), board.getBoardID());

            java.util.List<String> attackers = NPC.attackedSquares.get(coord);

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
        if (maxHealth > 1 && maxHealth != currentHealth) {
            //TODO - somehow only draws hp bar right at 0.5 alpha... so this crude fix can work
            if (!this.hasMoved) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }

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

    public void damagePiece(int totalDamage) {
        this.currentHealth -= totalDamage;
    }

    public int getCurrentHealth() { return currentHealth; }
    public void heal(int amount) {
        this.currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
    public void passiveHeal(){ this.currentHealth = Math.min(maxHealth, currentHealth + 1); }
    public void setHealth(int bossHealth) { this.currentHealth = bossHealth; }
}
