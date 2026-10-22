package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.RenderCache;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WeepingKnight extends NpcBoss{

    public WeepingKnight(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "knight", color, boardX, boardY, posX, posY, 1);
        this.barPoints = 4;
        this.maxHealth = 1;
        this.attack = 4;
        this.movementChance = 1;
        this.pieceSize = pieceSize;
        this.texture = "weepingknight";

        setCurrentHealthToMax();
    }

    @Override
    public void move() {
        NPC.vampireMoveLogic(this);
    }

    @Override
    public void draw(Graphics2D g2d, double zoomFactor) {
        //Only visible 1/4th the turns
        if (!(Timer.turnCounter % 4 == 1)){
            return;
        }

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

        g2d.setComposite(old);
    }
}
