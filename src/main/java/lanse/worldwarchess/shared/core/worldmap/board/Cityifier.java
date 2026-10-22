package lanse.worldwarchess.shared.core.worldmap.board;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cityifier {

    //TODO - make structure better...
    public static void cityify(){
        Random rand = new Random();

        for (Board board : WorldMap.boards.values()) {
            if (board instanceof CommanderBoard) continue;
            List<Piece> pieces = WorldMap.getAllPiecesOfBoard(board);
            if (pieces.isEmpty()) continue;

            int boardSize = 8 * pieces.get(0).getPieceSize();
            int centerX = boardSize / 2;
            int centerY = boardSize / 2;

            int clusterCount = Math.max(1, pieces.size() / 8);
            List<Point> clusterCenters = new ArrayList<>();
            for (int i = 0; i < clusterCount; i++) {
                double angle = (2 * Math.PI / clusterCount) * i;
                int radius = (int) (boardSize * 0.25 + rand.nextDouble() * boardSize * 0.2);
                int cx = (int) (centerX + Math.cos(angle) * radius);
                int cy = (int) (centerY + Math.sin(angle) * radius);
                clusterCenters.add(new Point(cx, cy));
            }

            for (Piece piece : pieces) {
                Point cluster = clusterCenters.get(rand.nextInt(clusterCenters.size()));

                int jitter = piece.getPieceSize() * (2 + rand.nextInt(4));
                int px = cluster.x + rand.nextInt(jitter) - jitter / 2;
                int py = cluster.y + rand.nextInt(jitter) - jitter / 2;

                piece.setBoardPosition(px, py);
            }

            for (int i = 0; i < clusterCenters.size() - 1; i++) {
                Point a = clusterCenters.get(i);
                Point b = clusterCenters.get(i + 1);

                for (Piece piece : pieces) {
                    if (piece.getType() == PieceType.PAWN) {
                        if (rand.nextDouble() < 0.3) {
                            double t = rand.nextDouble();
                            int x = (int) (a.x + t * (b.x - a.x));
                            int y = (int) (a.y + t * (b.y - a.y));
                            piece.setBoardPosition(x, y);
                        }
                    }
                }
            }
        }
    }
}
