package lanse.worldwarchess.shared.core.screens.lounge;

import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.bosses.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.HealerPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.IcePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.MagePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.PhoenixPiece;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.shared.core.worldmap.board.EmptyBoard;

public class DebugPreset {

    public static void debugPreset() {
        EmptyBoard board = new EmptyBoard(0, 0, MapShapePresets.TILE_SIZE,
                "empty", 0, 0, 32767, false, false, false);

        int boardID = board.getBoardID();
        int boardX = board.getBoardX();
        int boardY = board.getBoardY();

        addRow(Piece.class, 1, 2, board, "White");
        addRow(Piece.class, 3, 2, board, "Black");
        addRow(ZombiePiece.class, 5, 2, board, "NPC");
        addRow(VampirePiece.class,7, 2, board, "NPC");
        addRow(SpartanPiece.class, 9, 2, board, "NPC");
        addRow(AssassinPiece.class, 11, 2, board, "NPC");
        addRow(PhoenixPiece.class, 13, 2, board, "NPC");
        addRow(IcePiece.class, 15, 2, board, "NPC");
        addRow(MagePiece.class, 17, 2, board, "NPC");
        addRow(NecromancerPiece.class, 19, 2, board, "NPC");
        addRow(SentinelPiece.class, 21, 2, board, "NPC");
        addRow(NeonPiece.class, 23, 2, board, "NPC");

        addRow(CommanderPiece.class, 26, 2, board, "White");
        addRow(CommanderPiece.class, 30, 2, board, "Black");
        addRow(CommanderZombiePiece.class, 34, 2, board, "NPC");
        addRow(CommanderVampirePiece.class,38, 2, board, "NPC");
        addRow(CommanderSpartanPiece.class, 42, 2, board, "NPC");
        addRow(CommanderAssassinPiece.class, 46, 2, board, "NPC");
        addRow(CommanderPhoenixPiece.class, 50, 2, board, "NPC");
        addRow(CommanderIcePiece.class, 54, 2, board, "NPC");
        addRow(CommanderMagePiece.class, 58, 2, board, "NPC");
        addRow(CommanderNecromancerPiece.class, 62, 2, board, "NPC");

        addRow(CommanderSentinelPiece.class, 2, 16, board, "NPC");
        addRow(CommanderNeonPiece.class, 6, 16, board, "NPC");

        WorldMap.pieces.add(new Illusioner(-404, board, "illusioner", "NPC", boardX, boardY, 9, 16, 1));
        WorldMap.pieces.add(new WeepingKnight(-404, board, "knight", "NPC", boardX, boardY, 11, 16, 1));
        WorldMap.pieces.add(new ShotgunKingBoss(-404, board, "king", "NPC", boardX, boardY, 13, 16, 1));
        WorldMap.pieces.add(new ConwayBoss(-404, board, "king", "NPC", boardX, boardY, 15, 16, 1));

        WorldMap.pieces.add(new SpartanPiece(-404, board, "knight", "NPC", boardX, boardY, 9, 20, 1));
        WorldMap.pieces.add(new TrojanHorseBoss(-404, board, "knight", "NPC", boardX, boardY, 9, 24, 2, true));
        WorldMap.pieces.add(new TrojanHorseBoss(-404, board, "knight", "NPC", boardX, boardY, 8, 28, 4, true));

        WorldMap.pieces.add(new ElectroKingBoss(-404, board, "king", "NPC", boardX, boardY, 18, 16, 1));
        WorldMap.pieces.add(new SultanBoss(-404, board, "sultan", "NPC", boardX, boardY, 22, 16, 1));
        WorldMap.pieces.add(new HealerQueenBoss(-404, board, "queen", "NPC", boardX, boardY, 14, 40, 1));

        addRow(HealerPiece.class, 13, 20, board, "NPC");

        WorldMap.pieces.add(new AmalgamationBoss(-404, board, "amalgamation", "NPC", boardX, boardY, 18, 24, 1));
    }


    public static void addRow(Class<? extends Piece> clazz, int startX, int startY, Board board, String team) {
        final String[] order = {"pawn", "rook", "knight", "bishop", "queen", "king"};

        int pieceSize = CommanderPiece.class.isAssignableFrom(clazz) ? 2 : 1;
        int step = pieceSize * 2;
        int y = startY;
        boolean skippedPawn = false;

        for (String type : order) {
            if (clazz == PhoenixPiece.class || clazz == IcePiece.class ||
                    clazz == CommanderPhoenixPiece.class || clazz == CommanderIcePiece.class){
                if (!skippedPawn){
                    skippedPawn = true;
                    continue;
                }
            }

            try {
                Piece piece = clazz.getConstructor(
                        int.class, Board.class, String.class, String.class,
                        int.class, int.class, int.class, int.class, int.class
                ).newInstance(
                        -404, board, type, team, board.getBoardX(), board.getBoardY(), startX, y, pieceSize);

                WorldMap.pieces.add(piece);
                y += step;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}