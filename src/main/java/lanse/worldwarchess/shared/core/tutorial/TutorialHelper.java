package lanse.worldwarchess.shared.core.tutorial;

import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.IcePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.PhoenixPiece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.List;
import java.util.Random;

public class TutorialHelper {

    //TODO - add button called "FIRE IT UP" with text above it saying: "Want to speed this up?"

    public static void helpPlayerWithTutorial(){
        Random random = new Random();
        for (Board board : WorldMap.boards.values()){

            List<int[]> emptySquares = board.getEmptySquares();

            if (emptySquares.isEmpty()) {
                continue;
            }

            int[] pos = emptySquares.get(random.nextInt(emptySquares.size()));
            int posX = pos[0];
            int posY = pos[1];

            if (random.nextBoolean()){
                PhoenixPiece phoenix = new PhoenixPiece(
                        -404, board, NPC.getRandomPieceType(3, 3, 3, 3),
                        "White", board.getBoardX(), board.getBoardY(), posX, posY, 1);

                WorldMap.spawnPiece(phoenix, false);

            } else {
                IcePiece icePiece = new IcePiece(
                        -404, board, NPC.getRandomPieceType(3, 3, 3, 3),
                        "White", board.getBoardX(), board.getBoardY(), posX, posY, 1);

                WorldMap.spawnPiece(icePiece, false);
            }
        }
    }
}
