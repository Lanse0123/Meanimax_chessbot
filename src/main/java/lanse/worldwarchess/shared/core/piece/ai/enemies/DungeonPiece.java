package lanse.worldwarchess.shared.core.piece.ai.enemies;

import lanse.worldwarchess.shared.core.worldmap.board.Board;

public class DungeonPiece extends NpcPiece{

    //TODO - Has an ability to freeze all enemy pieces within 2 blocks of it for (x) turns.
    // I need a way for a player to override NPC piece movement and abilities
    // This will be for the Dungeons and Dragons gamemode, in which the board will have:
    // 1 king, 1 queen, 4 dungeons, 4 rooks, and 4 dragons.
    // Dragons are a type of piece from Fairy Chess which move like a bishop and knight.
    // There is also a Xiangqi Horse, which looks dragonlike, so I might use it too. It is even weirder

    public DungeonPiece(int playerId, Board board, String type, String color, int boardX, int boardY, int posX, int posY, int pieceSize) {
        super(playerId, board, "dungeon", color, boardX, boardY, posX, posY, pieceSize);
    }
}
