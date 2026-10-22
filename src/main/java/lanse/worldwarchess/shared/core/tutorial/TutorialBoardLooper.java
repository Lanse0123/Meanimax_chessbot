package lanse.worldwarchess.shared.core.tutorial;

import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.attacks.FireAttack;
import lanse.worldwarchess.shared.core.piece.ai.bosses.ElectroKingBoss;
import lanse.worldwarchess.shared.core.piece.ai.bosses.MysteryBoss;
import lanse.worldwarchess.shared.core.piece.ai.bosses.TrojanHorseBoss;
import lanse.worldwarchess.shared.core.piece.ai.enemies.AssassinPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.SpartanPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.VampirePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.ZombiePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.CommanderAssassinPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.CommanderSpartanPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.CommanderVampirePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.CommanderZombiePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.MagePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.PhoenixPiece;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.TeamCaster;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import static lanse.worldwarchess.shared.core.tutorial.Tutorial.hasPhaseEnded;
import static lanse.worldwarchess.shared.core.tutorial.Tutorial.sleep;

public class TutorialBoardLooper {

    //This class is responsible for looping through all the types of NPC pieces at the end of the tutorial.

    public static void loopBoardPresets(int phase) {
        new Thread(() -> {
            try {
                while (Tutorial.phase == phase) {
                    WorldMap.pieces.clear();
                    WorldMap.boards.clear();

                    Board board = new Board(0, 0, MapShapePresets.TILE_SIZE,"Border", 1, 2, 1, false, true, true);

//                    WorldMap.pieces.removeIf(piece -> piece.getTeam().equalsIgnoreCase("black"));
//                    WorldMap.pieces.add(new AmalgamationBoss(-404, board, "king", "black", 0, 0, 1, 0, 2));
//                    sleep(3000000);

                    TeamCaster.convertTeam("Black", ZombiePiece.class, CommanderZombiePiece.class);
                    sleep(2);
                    hasPhaseEnded(phase);

                    TeamCaster.convertTeam("Black", VampirePiece.class, CommanderVampirePiece.class);
                    sleep(2);
                    hasPhaseEnded(phase);

                    TeamCaster.convertTeam("Black", SpartanPiece.class, CommanderSpartanPiece.class);
                    sleep(2);
                    hasPhaseEnded(phase);

                    TeamCaster.convertTeam("Black", AssassinPiece.class, CommanderAssassinPiece.class);
                    sleep(2);
                    hasPhaseEnded(phase);

                    TeamCaster.convertTeam("Black", MagePiece.class, null);
                    sleep(2);
                    hasPhaseEnded(phase);

                    TeamCaster.convertTeam("Black", PhoenixPiece.class, null, p -> !(p.getType() == PieceType.PAWN));
                    WorldMap.pieces.add(new FireAttack(-404, board, "fire", "black", 0, 0, 1, 3, 1));
                    WorldMap.pieces.add(new FireAttack(-404, board, "fire", "black", 0, 0, 2, 1, 1));
                    WorldMap.pieces.add(new FireAttack(-404, board, "fire", "black", 0, 0, 4, 3, 1));
                    WorldMap.pieces.add(new FireAttack(-404, board, "fire", "black", 0, 0, 5, 4, 1));
                    WorldMap.pieces.add(new FireAttack(-404, board, "fire", "black", 0, 0, 7, 2, 1));
                    sleep(2);
                    hasPhaseEnded(phase);

                    WorldMap.pieces.removeIf(piece -> piece.getTeam().equalsIgnoreCase("black"));
                    WorldMap.pieces.add(new ElectroKingBoss(-404, board, "king", "black", 0, 0, 3, 0, 2));
                    sleep(2);
                    hasPhaseEnded(phase);

                    WorldMap.pieces.removeIf(piece -> piece.getTeam().equalsIgnoreCase("black"));
                    WorldMap.pieces.add(new TrojanHorseBoss(-404, board, "knight", "black", 0, 0, 2, 0, 4, true));
                    sleep(2);
                    hasPhaseEnded(phase);

                    WorldMap.pieces.removeIf(piece -> piece.getTeam().equalsIgnoreCase("black"));
                    WorldMap.pieces.add(new MysteryBoss(-404, board, "king", "black", 0, 0, 1, 0, 6));
                    sleep(2);
                    hasPhaseEnded(phase);
                }
                //ends thread
            } catch (Exception ignored){}
        },"TutorialBoardLoop").start();
    }

}
