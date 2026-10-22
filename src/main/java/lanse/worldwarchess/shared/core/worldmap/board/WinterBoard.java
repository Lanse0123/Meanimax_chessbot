package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.FreezeAttack;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.IcePiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.util.Random;

public class WinterBoard extends CommanderBoard{

    public WinterBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, preset, playerID, playerID2, boardID, false, false, false);
        this.borderColor = new Color(140, 177, 255);
        this.blackTexturePrefix = 'i';
        this.whiteTexturePrefix = 'w';
    }

    @Override
    public void tick() {
        if (Timer.turnCounter % 3 == 0) {
            // Spawn a IcePiece on a random Ice
            java.util.List<Piece> freezeAttacks = WorldMap.getAllPiecesOfBoard(this).stream()
                    .filter(p -> p instanceof FreezeAttack)
                    .toList();

            if (!freezeAttacks.isEmpty()) {
                Piece randomIce = freezeAttacks.get(new Random().nextInt(freezeAttacks.size()));
                IcePiece icePiece = new IcePiece(
                        randomIce.getPlayerID(), randomIce.getBoard(), NPC.getRandomPieceType(3, 3, 3, 1),
                        randomIce.getTeam(), randomIce.getBoardX(), randomIce.getBoardY(), randomIce.getPosX(), randomIce.getPosY(), 1);

                WorldMap.erasePiece(randomIce, true);
                WorldMap.spawnPiece(icePiece, true);
            }
        }

        // Spawn a FreezeAttack on a random empty tile
        Point emptyTile = getRandomEmptyTile();
        if (emptyTile != null) {
            FreezeAttack ice = new FreezeAttack(-1, this, "ice", "ice",
                    this.boardX, this.boardY, emptyTile.x, emptyTile.y, 1);

            WorldMap.spawnPiece(ice, true);
        }
    }
}
