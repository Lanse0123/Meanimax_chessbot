package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.attacks.FireAttack;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.PhoenixPiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.util.Random;

public class InfernoBoard extends CommanderBoard{

    //This represents a firey chess board. It will spawn fire which will spawn phoenix pieces.

    public InfernoBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, preset, playerID, playerID2, boardID, false, false, false);
        this.borderColor = new Color(255, 119, 0);
    }

    @Override
    public void tick() {
        if (Timer.turnCounter % 5 == 0) {
            // Spawn a Phoenix on a random FireAttack
            java.util.List<Piece> fireAttacks = WorldMap.getAllPiecesOfBoard(this).stream()
                    .filter(p -> p instanceof FireAttack)
                    .toList();

            if (!fireAttacks.isEmpty()) {
                Piece randomFire = fireAttacks.get(new Random().nextInt(fireAttacks.size()));
                PhoenixPiece phoenix = new PhoenixPiece(
                        randomFire.getPlayerID(), randomFire.getBoard(), NPC.getRandomPieceType(3, 3, 3, 1),
                        randomFire.getTeam(), randomFire.getBoardX(), randomFire.getBoardY(), randomFire.getPosX(), randomFire.getPosY(), 1);

                WorldMap.erasePiece(randomFire, true);
                WorldMap.spawnPiece(phoenix, true);
            }
        }

        // Spawn a FireAttack on a random empty tile
        Point emptyTile = getRandomEmptyTile();
        if (emptyTile != null) {
            FireAttack fire = new FireAttack(-1, this, "fire", "fire",
                    this.boardX, this.boardY, emptyTile.x, emptyTile.y, 1);

            WorldMap.spawnPiece(fire, true);
        }
    }
}
