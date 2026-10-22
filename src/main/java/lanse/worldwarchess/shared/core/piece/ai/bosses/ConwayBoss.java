package lanse.worldwarchess.shared.core.piece.ai.bosses;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.Objects;
import java.util.Optional;

public class ConwayBoss extends NpcBoss {

    public ConwayBoss(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "conway", color, boardX, boardY, posX, posY, pieceSize);
        this.texture = "conway";
        this.barPoints = 3;
    }

    @Override
    public void move() {
        int liveNeighbors = countLiveNeighbors();

        // self preservation check
        if (liveNeighbors < 2 || liveNeighbors > 3) {
            WorldMap.minionPieceDestroyRequests.add(this);
        }

        for (int dx = -pieceSize; dx <= pieceSize; dx += pieceSize) {
            for (int dy = -pieceSize; dy <= pieceSize; dy += pieceSize) {
                if (dx == 0 && dy == 0) continue;

                Board targetBoard = findBoardForOffset(dx, dy);
                int[] local = getSnappedLocal(targetBoard, dx, dy);
                if (local == null) continue;

                int nx = local[0];
                int ny = local[1];

                Piece neighbor = targetBoard.getPieceAt(nx, ny, Optional.empty(), WorldMap.pieces);

                if (neighbor == null || !Objects.equals(neighbor.getTeam(), this.team)) {
                    int liveAround = countLiveNeighborsAt(targetBoard, nx, ny);
                    if (liveAround == 3) {
                        WorldMap.minionPieceSpawnRequests.add(new ConwayBoss(
                                this.playerID, targetBoard, "conway", this.team,
                                targetBoard.getBoardX(), targetBoard.getBoardY(), nx, ny, this.pieceSize));
                    }
                }
            }
        }
    }

    private int countLiveNeighbors() {
        int count = 0;
        for (int dx = -pieceSize; dx <= pieceSize; dx += pieceSize) {
            for (int dy = -pieceSize; dy <= pieceSize; dy += pieceSize) {
                if (dx == 0 && dy == 0) continue;

                Board neighborBoard = findBoardForOffset(dx, dy);
                int[] local = getSnappedLocal(neighborBoard, dx, dy);
                if (local == null) continue;

                Piece neighbor = neighborBoard.getPieceAt(local[0], local[1], Optional.empty(), WorldMap.pieces);
                if (neighbor instanceof ConwayBoss) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countLiveNeighborsAt(Board targetBoard, int x, int y) {
        int count = 0;

        for (int dx = -pieceSize; dx <= pieceSize; dx += pieceSize) {
            for (int dy = -pieceSize; dy <= pieceSize; dy += pieceSize) {
                if (dx == 0 && dy == 0) continue;

                int localNx = x + dx;
                int localNy = y + dy;

                int globalX = targetBoard.getBoardX() + localNx * targetBoard.getBaseTileSize();
                int globalY = targetBoard.getBoardY() + localNy * targetBoard.getBaseTileSize();

                Board found = null;

                int tbSizePx = targetBoard.getBoardSize() * targetBoard.getBaseTileSize();
                if (globalX >= targetBoard.getBoardX() && globalX < targetBoard.getBoardX() + tbSizePx &&
                        globalY >= targetBoard.getBoardY() && globalY < targetBoard.getBoardY() + tbSizePx) {
                    found = targetBoard;
                } else {
                    for (Board nb : targetBoard.getNeighboringBoards()) {
                        int bx = nb.getBoardX();
                        int by = nb.getBoardY();
                        int bSizePx = nb.getBoardSize() * nb.getBaseTileSize();
                        if (globalX >= bx - LegalMoveDetector.TOLERENCE_PIXELS && globalX < bx + bSizePx + LegalMoveDetector.TOLERENCE_PIXELS &&
                                globalY >= by - LegalMoveDetector.TOLERENCE_PIXELS && globalY < by + bSizePx + LegalMoveDetector.TOLERENCE_PIXELS) {
                            found = nb;
                            break;
                        }
                    }
                }

                if (found == null) {
                    continue;
                }

                int snappedX = (int) Math.round((globalX - found.getBoardX()) / (double) found.getBaseTileSize());
                int snappedY = (int) Math.round((globalY - found.getBoardY()) / (double) found.getBaseTileSize());

                if (snappedX < 0 || snappedX >= found.getBoardSize() || snappedY < 0 || snappedY >= found.getBoardSize()) {
                    continue;
                }

                Piece neighbor = found.getPieceAt(snappedX, snappedY, Optional.empty(), WorldMap.pieces);
                if (neighbor instanceof ConwayBoss) {
                    count++;
                }
            }
        }
        return count;
    }


    /** EVERYTHING BELOW WAS CRAPPILY COPIED OVER FROM LEGAL MOVE DETECTOR AND PROBABLY DOESN'T WORK... **/
    private Board findBoardForOffset(int offsetX, int offsetY) {
        int tileSize = board.getBaseTileSize();
        int boardSize = board.getBoardSize();

        int localX = posX + offsetX;
        int localY = posY + offsetY;

        if (localX >= 0 && localX < boardSize && localY >= 0 && localY < boardSize) {
            return board;
        }

        // Use global coords to find neighbors
        int pieceGlobalX = board.getBoardX() + posX * tileSize;
        int pieceGlobalY = board.getBoardY() + posY * tileSize;
        int newGlobalX = pieceGlobalX + offsetX * tileSize;
        int newGlobalY = pieceGlobalY + offsetY * tileSize;

        for (Board neighbor : board.getNeighboringBoards()) {
            if (neighbor.getBoardID() == board.getBoardID()) continue;

            int neighborTileSize = neighbor.getBaseTileSize();
            int neighborTileCount = neighbor.getBoardSize();
            int boardX = neighbor.getBoardX();
            int boardY = neighbor.getBoardY();

            int boardWidth = neighborTileSize * neighborTileCount;
            int boardHeight = neighborTileSize * neighborTileCount;

            if (newGlobalX >= boardX - LegalMoveDetector.TOLERENCE_PIXELS && newGlobalX < boardX + boardWidth + LegalMoveDetector.TOLERENCE_PIXELS &&
                    newGlobalY >= boardY - LegalMoveDetector.TOLERENCE_PIXELS && newGlobalY < boardY + boardHeight + LegalMoveDetector.TOLERENCE_PIXELS) {
                return neighbor;
            }
        }

        return board; // fallback if no valid neighbor found
    }

    private int[] getSnappedLocal(Board targetBoard, int offsetX, int offsetY) {
        int tileSize = board.getBaseTileSize();
        int pieceGlobalX = board.getBoardX() + posX * tileSize;
        int pieceGlobalY = board.getBoardY() + posY * tileSize;
        int newGlobalX = pieceGlobalX + offsetX * tileSize;
        int newGlobalY = pieceGlobalY + offsetY * tileSize;

        int neighborTileSize = targetBoard.getBaseTileSize();
        int neighborTileCount = targetBoard.getBoardSize();
        int boardX = targetBoard.getBoardX();
        int boardY = targetBoard.getBoardY();

        double relX = (double) (newGlobalX - boardX) / neighborTileSize;
        double relY = (double) (newGlobalY - boardY) / neighborTileSize;
        int snappedX = (int) (relX + 0.5);
        int snappedY = (int) (relY + 0.5);

        if (snappedX >= 0 && snappedX < neighborTileCount && snappedY >= 0 && snappedY < neighborTileCount) {
            return new int[]{snappedX, snappedY};
        }
        return null;
    }
}
