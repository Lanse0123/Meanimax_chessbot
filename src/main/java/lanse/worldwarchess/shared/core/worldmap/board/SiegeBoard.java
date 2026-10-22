package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceClassPrefixMap;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.bosses.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.DungeonPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.NpcCommanderPiece;
import lanse.worldwarchess.shared.core.tutorial.CommanderTutorialPiece;
import lanse.worldwarchess.shared.core.tutorial.TutorialPiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SiegeBoard extends CommanderBoard {

    protected int spawnBarAttempt = 15;
    protected int wave = 0;
    protected int timeWaited = 7;
    protected static final Random random = new Random();

    public SiegeBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, "Siege", -40404, -40404, boardID, false, false, false);
        this.borderColor = new Color(105, 0, 140);
        this.boardSize = 64;
        this.tileScale = 4;
    }

    @Override
    public void tick() {
        if (!shouldSpawnNewWave()) return;

        spawnBarAttempt += 5;
        wave = spawnBarAttempt / 5;
        int amountBarSpawned = 0;

        List<Class<? extends Piece>> npcList = getAllRegisteredPieceClasses();

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

    //TODO - this is 1 way of getting completely randomized siege waves. However, maybe add more presets so like
    // maybe there could be a fire wave or something

    protected Piece spawnRandomRegisteredNpc(Point tile, List<Class<? extends Piece>> npcList) {
        List<Class<? extends Piece>> candidates = new ArrayList<>();

        for (Class<? extends Piece> clazz : npcList) {
            if (clazz == null || Modifier.isAbstract(clazz.getModifiers())) continue;

            //TODO - change to list of excluded pieces for now
            if (clazz == PointerPiece.class || clazz == MysteryBoss.class || clazz == ZooBoss.class ||
                    clazz == Piece.class || clazz == CommanderPiece.class || clazz == DungeonPiece.class ||
                    clazz == TetrachBritishTankBoss.class || clazz == ShotgunKingBoss.class ||
                    clazz == TutorialPiece.class || clazz == CommanderTutorialPiece.class) continue;

            // check inheritance
            if (NpcPiece.class.isAssignableFrom(clazz)
                    || NpcCommanderPiece.class.isAssignableFrom(clazz)
                    || NpcBoss.class.isAssignableFrom(clazz)) {
                candidates.add(clazz);
            }
        }

        if (candidates.isEmpty()) return null;

        // Pick random class
        Class<? extends Piece> clazz = candidates.get(random.nextInt(candidates.size()));
        try {
            //TODO - make this use the class's default piece size
            return clazz.getConstructor(
                    int.class, Board.class, String.class, String.class, int.class, int.class, int.class, int.class, int.class
            ).newInstance(-404, this, NPC.getRandomPieceType(1, 1, 1, 1),
                    "NPC", this.boardX, this.boardY, tile.x, tile.y, 1);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Class<? extends Piece>> getAllRegisteredPieceClasses() {
        List<Class<? extends Piece>> result = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            Class<? extends Piece> c = PieceClassPrefixMap.getPieceClass(i);
            if (c != null && c != Piece.class) {
                result.add(c);
            }
        }
        return result;
    }

    private boolean shouldSpawnNewWave(){
        timeWaited--;
        if (timeWaited <= 0){
            timeWaited = 7 + wave / 6;
            return true;
        }

        int npcCount = 0;
        for (Piece piece : WorldMap.pieces) {
            if (piece instanceof NpcPiece || piece instanceof NpcCommanderPiece)
                npcCount++;
        }
        return npcCount <= wave;
    }
}
