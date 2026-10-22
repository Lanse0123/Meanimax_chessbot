package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.piece.ai.bosses.ConwayBoss;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.util.Random;

public class ConwayBoard extends Board{

    // This board might eventually be deleted

    private boolean hasSpawnedWave = false;

    public ConwayBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, preset, playerID, playerID2, boardID, false, true, true);
        this.borderColor = new Color(36, 148, 36);
        this.boardSize = 24;
    }

    @Override
    public void tick() {
        if (hasSpawnedWave) return;

        //THIS CODE WILL ONLY RUN ONCE, ITS LIKE INITIALIZATION
        hasSpawnedWave = true;

        int boardSize = getBoardSize();
        Random random = new Random();

        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (random.nextBoolean()) {
                    WorldMap.spawnPiece(new ConwayBoss(this.playerID, this, "conway",
                                    "NPC", this.boardX, this.boardY, x, y, 1), true);
                }
            }
        }
    }
}
