package lanse.worldwarchess.shared.core.worldmap.board.badboard;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class BadBoard extends Board {

    private int frame = 0;
    private final String[][] lastFrameTeams = new String[32][32];

    public BadBoard(int boardX, int boardY, int baseTileSize, String preset, int playerID, int playerID2, int boardID, boolean isCommander, boolean shouldLinkNeighbors, boolean shouldSetBoardPreset) {
        super(boardX, boardY, baseTileSize, preset, playerID, playerID2, boardID, isCommander, shouldLinkNeighbors, shouldSetBoardPreset);
        this.boardSize = 32;
        this.borderColor = new Color(180, 0, 0);
    }

    @Override
    public void tick() {
        frame = (frame + 1) % BadAppleDecoder.frameCount();
        String newTeam;
        List<Piece> pieceList = WorldMap.getAllPiecesOfBoard(this);

        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                newTeam = (BadAppleDecoder.getPixel(frame, x, y) ? "White" : "Black");
                String oldTeam = lastFrameTeams[x][y];

                if (newTeam.equals(oldTeam)) continue;

                Piece existing = getPieceAt(x, y, Optional.empty(), pieceList);
                if (existing != null) {
                    WorldMap.erasePiece(existing, false);
                }

                WorldMap.pieces.add(new Piece(-1, this, NPC.getRandomPieceType(3, 3, 3, 1),
                        newTeam, this.boardX, this.boardY, x, y, 1));

                lastFrameTeams[x][y] = newTeam;
            }
        }


        List<Piece> newPieceList = WorldMap.getAllPiecesOfBoard(this);

        if (WorldMap.getAllKings("White", false, false, true, newPieceList).isEmpty()) {
            List<Piece> whites = newPieceList.stream()
                    .filter(p -> "White".equals(p.getTeam()))
                    .toList();

            if (!whites.isEmpty()) {
                Piece chosen = whites.get((int) (Math.random() * whites.size()));
                chosen.setType(PieceType.KING);
            }
        }

        if (WorldMap.getAllKings("Black", false, false, true, newPieceList).isEmpty()) {
            List<Piece> blacks = newPieceList.stream()
                    .filter(p -> "Black".equals(p.getTeam()))
                    .toList();

            if (!blacks.isEmpty()) {
                Piece chosen = blacks.get((int) (Math.random() * blacks.size()));
                chosen.setType(PieceType.KING);
            }
        }
    }
}
