package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.bosses.AmalgamationBoss;
import lanse.worldwarchess.shared.core.piece.ai.enemies.AssassinPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.CommanderAssassinPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.MagePiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.util.List;

public class ShadowBoard extends SiegeBoard{

    public ShadowBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, preset, playerID, playerID2, boardID, false, false, false);
        this.borderColor = new Color(10, 10, 10);
        this.blackTexturePrefix = '2';
        this.whiteTexturePrefix = 's';
    }

    @Override
    public void tick() {
        spawnBarAttempt += 5;
        wave = spawnBarAttempt / 5;
        int amountBarSpawned = 0;

        List<Class<? extends Piece>> npcList = List.of(AssassinPiece.class, CommanderAssassinPiece.class, AmalgamationBoss.class,
                MagePiece.class);

        int failedAttempts = 0;

        while (failedAttempts < 10) {
            Point emptyTile = getRandomEmptyTile();
            if (emptyTile == null) continue;

            Piece npc = spawnRandomRegisteredNpc(emptyTile, npcList);
            if (npc == null) {
                failedAttempts++;
                continue;
            }

            int barPoints = npc.getBarPoints();

            if (barPoints + amountBarSpawned < spawnBarAttempt) {
                WorldMap.spawnPiece(npc, true);
                amountBarSpawned += barPoints;
            } else {
                failedAttempts++;
            }
        }
    }
}
