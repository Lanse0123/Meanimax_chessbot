package lanse.worldwarchess.shared.core.piece.fairies;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.NPC;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

// this will likely be the most horrific java file to be seen ever.
public class FairyLegalMoveDetector extends LegalMoveDetector{

    //TODO - Reference: https://www.chess.com/blog/Lachelner/fairy-pieces-info-chess-com
    //TODO - other reference: https://en.wikipedia.org/wiki/List_of_fairy_chess_pieces

    public static List<Coordinate> getLegalMoves(Piece piece, PieceType type, Board currentBoard) {
        List<Coordinate> legalMoves = new ArrayList<>();

        switch (type){
            //default cases in event fairy piece is a regular piece
            case PAWN -> legalMoves.addAll(getPawnMoves(piece, currentBoard));
            case KNIGHT -> legalMoves.addAll(getKnightMoves(piece, currentBoard));
            case BISHOP -> legalMoves.addAll(getSlidingMoves(piece, currentBoard, bishopDirections));
            case ROOK -> legalMoves.addAll(getSlidingMoves(piece, currentBoard, rookDirections));
            case QUEEN -> legalMoves.addAll(getSlidingMoves(piece, currentBoard, queenDirections));
            case KING -> legalMoves.addAll(getKingMoves(piece, currentBoard));

            case CHECKER -> legalMoves.addAll(getCheckerMoves(piece, currentBoard, false));
            case CHECKER_KING -> legalMoves.addAll(getCheckerMoves(piece, currentBoard, true));

            case AMAZON -> { // moves like knight + queen
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, queenDirections));
            }
            case CHANCELLOR -> { // moves like knight + rook
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, rookDirections));
            }
            case ARCHBISHOP -> { // moves like knight + bishop
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, bishopDirections));
            }
            case GENERAL -> { // moves like knight + king
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getKingMoves(piece, currentBoard));
            }
            case STONE_GENERAL -> {
                //TODO - only moves and captures diagonally 1 square like a pawn. Promotes if reaches end.
            }
            case CAMEL -> // moves like elongated knight (3 + 1)
                    legalMoves.addAll(getCamelMoves(piece, currentBoard));

            case WILDEBEEST -> { // moves like knight + camel
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getCamelMoves(piece, currentBoard));
            }
            case GRASSHOPPER -> {
                //TODO - requires actual effort to make so ill do it later
            }
            case FERZ -> legalMoves.addAll(getFerzMoves(piece, currentBoard));

            case SOLDIER -> {
                //TODO - it can only move forward. Attack and move is the same. Promotes like pawn.
            }
            case XIANGQI_HORSE -> legalMoves.addAll(getXiangqiMoves(piece, currentBoard));

            case DRAGON_BISHOP -> { // moves like bishop + xiangqi horse
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, bishopDirections));
                legalMoves.addAll(getXiangqiMoves(piece, currentBoard));
            }
            case SERGEANT -> {
                //TODO - can move and capture diagonally or forward, like a pawn + general or something
            }
            case ZEBRA -> // moves like a camel but even further (3 + 2)
                    legalMoves.addAll(getZebraMoves(piece, currentBoard));

            case BISON -> { // moves like a camel + zebra
                legalMoves.addAll(getCamelMoves(piece, currentBoard));
                legalMoves.addAll(getZebraMoves(piece, currentBoard));
            }
            case BODYGUARD -> //moves like a queen but only for 2 squares
                    legalMoves.addAll(getSlidingMoves(piece, currentBoard, 2, queenDirections));

            case BUFFALO -> { // moves like bison + knight
                legalMoves.addAll(getCamelMoves(piece, currentBoard));
                legalMoves.addAll(getZebraMoves(piece, currentBoard));
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
            }
            case CANVASSER -> { // moves like camel + rook
                legalMoves.addAll(getCamelMoves(piece, currentBoard));
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, rookDirections));
            }
            case CHAMELEON -> //changes between moving like a knight, bishop, rook, and queen per move.
                    legalMoves.addAll(getChameleonMoves(piece, currentBoard));

            case DINOSAUR -> { //moves like a queen, but only for capture moves
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, queenDirections));
                legalMoves = NPC.filterCaptureMoves(piece, legalMoves);
            }
            case DRAGON -> { // moves like knight + pawn
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getPawnMoves(piece, currentBoard));
            }
            case DUCK -> {
                //TODO - can move anywhere 8 tiles away (regular size chess board), is colorless, can not capture,
                // and can not be captured. Its like a moving barrier
            }
            case DUMMY -> {
                //dummys dont move
            }
            case EDGEHOG -> { // a queen that can only move to or from the edge of a board
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, queenDirections));
                //TODO - filter for moves that are on the egde of a board
            }
            case ENSIGN -> { // moves like a bishop but captures like a rook
                List<Coordinate> bishopMoves = new ArrayList<>(getSlidingMoves(piece, currentBoard, bishopDirections));
                bishopMoves.removeAll(NPC.filterCaptureMoves(piece, bishopMoves));

                List<Coordinate> rookMoves = new ArrayList<>(getSlidingMoves(piece, currentBoard, rookDirections));
                rookMoves = NPC.filterCaptureMoves(piece, rookMoves);

                legalMoves.addAll(bishopMoves);
                legalMoves.addAll(rookMoves);
            }
            case FLAMINGO -> // moves like a knight but its (6 + 1)
                    legalMoves.addAll(getFlamingoMoves(piece, currentBoard));

            case GUARD -> // moves like a king but isnt an actual king
                    legalMoves.addAll(getKingMoves(piece, currentBoard));

            case HARE -> // moves like a knight but its (2 + 2) instead
                    legalMoves.addAll(getHareMoves(piece, currentBoard));

            case HIPPOSAUR -> { //moves like a knight, but only for capture moves
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves = NPC.filterCaptureMoves(piece, legalMoves);
            }
            case JESTER -> { // moves like a queen but cant capture or be captured
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, queenDirections));
                legalMoves = NPC.filterCaptureMoves(piece, legalMoves);
                int a; // to remove annoying yellow lines
                //TODO - make impossible to capture jester
            }
            case KNEEN -> { // moves like a knight but can only capture like a queen
                List<Coordinate> knightMoves = new ArrayList<>(getKnightMoves(piece, currentBoard));
                knightMoves.removeAll(NPC.filterCaptureMoves(piece, knightMoves));

                List<Coordinate> queenMoves = new ArrayList<>(getSlidingMoves(piece, currentBoard, queenDirections));
                queenMoves = NPC.filterCaptureMoves(piece, queenMoves);

                legalMoves.addAll(knightMoves);
                legalMoves.addAll(queenMoves);
            }
            case LEOPARD -> { // moves like knight + bishop limited to 2 squares away
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, 2, bishopDirections));
            }
            case LLAMA -> { // moves like camel + pawn
                legalMoves.addAll(getCamelMoves(piece, currentBoard));
                legalMoves.addAll(getPawnMoves(piece, currentBoard));
            }
            case MAHARAJA -> { // moves like an amazon, but if captured, it dies like a king
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, queenDirections));
                int a; //just to get rid of annoying yellow lines
            }
            case MEGASAUR -> { //moves like an amazon but only for capture moves
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, queenDirections));
                legalMoves = NPC.filterCaptureMoves(piece, legalMoves);
            }
            case OKAPI -> { // moves like knight + zebra
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getZebraMoves(piece, currentBoard));
            }
            case PRIEST -> { // moves like knight + ferz
                legalMoves.addAll(getKnightMoves(piece, currentBoard));
                legalMoves.addAll(getFerzMoves(piece, currentBoard));
            }
            case REFLECTING_BISHOP -> {
                //TODO - can move like a bishop, but can either pass through to other boards, or
                // can bounce around the walls of it's own board similar to a DVD bounce thing
            }
            case RHUBARB -> {
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, rookDirections));
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, 3, bishopDirections));
            }
            case ROYAL_GUARD -> { // moves like a king, captures like a queen.
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, queenDirections));
                legalMoves = NPC.filterCaptureMoves(piece, legalMoves);
                legalMoves.addAll(getKingMoves(piece, currentBoard));
            }
            case SHIP -> {
                //TODO - moves 1 square like ferz, then from there moves like a rook
            }
            case SHORT_ROOK -> //rook limited to 4 squares away
                    legalMoves.addAll(getSlidingMoves(piece, currentBoard, 4, rookDirections));

            case SNAKE -> {
                //TODO - moves 1 square like a rook, then from there moves like a bishop
            }
            case SQUIRREL -> {
                //TODO - moves to any square away a distance of 2 from you like castle or bear
            }
            case THREELEAPER -> legalMoves.addAll(getThreeLeaperMoves(piece, currentBoard));
            case TIGER -> {
                //TODO - moves like a zebra, then from there moves like a bishop
            }
            case VANGUARD -> { // moves like a bishop but cant only move 1 square
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, bishopDirections));
                legalMoves.removeAll(getFerzMoves(piece, currentBoard));
            }
            case DUNGEON -> { // moves like a king + rook
                legalMoves.addAll(getSlidingMoves(piece, currentBoard, rookDirections));
                legalMoves.addAll(getKingMoves(piece, currentBoard));
            }
        }

        return legalMoves;
    }

    //TODO - I probably made atleast 1 error with this.
    private static Collection<Coordinate> getCheckerMoves(Piece piece, Board currentBoard, boolean isKing) {
        List<Coordinate> moves = new ArrayList<>();

        int startX = piece.getPosX();
        int startY = piece.getPosY();
        int step = piece.getPieceSize();
        int boardSize = currentBoard.getBoardSize();
        int dir = piece.getTeam().equals("White") ? -1 : 1;

        int[][] normalOffsets = isKing ?
                new int[][]{
                        {-step,  dir * step},
                        { step,  dir * step},
                        {-step, -dir * step},
                        { step, -dir * step},
                } :
                new int[][]{
                        {-step,  dir * step},
                        { step,  dir * step}
                };

        int[][] captureOffsets = isKing ?
                new int[][]{
                        {-2 * step,  2 * dir * step},
                        { 2 * step,  2 * dir * step},
                        {-2 * step, -2 * dir * step},
                        { 2 * step, -2 * dir * step}
                } :
                new int[][]{
                        {-2 * step,  2 * dir * step},
                        { 2 * step,  2 * dir * step}
                };

        int baseTileSize = currentBoard.getBaseTileSize();

        for (int[] off : normalOffsets) {
            int nx = startX + off[0];
            int ny = startY + off[1];

            if (nx >= 0 && nx < boardSize && ny >= 0 && ny < boardSize) {
                if (isValidMove(nx, ny, currentBoard, piece)) {
                    moves.add(new Coordinate(nx, ny, currentBoard.getBoardID()));
                }
            } else {
                Coordinate found = findBoardCoordinate(currentBoard, piece, off[0], off[1], baseTileSize);
                if (found != null) moves.add(found);
            }
        }

        Queue<int[]> toCheck = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        toCheck.add(new int[]{startX, startY});
        visited.add(startX + "," + startY);

        while (!toCheck.isEmpty()) {
            int[] pos = toCheck.poll();
            int x = pos[0];
            int y = pos[1];

            for (int[] off : captureOffsets) {
                int nx = x + off[0];
                int ny = y + off[1];

                if (nx >= 0 && nx < boardSize && ny >= 0 && ny < boardSize) {
                    if (isValidCapture(nx, ny, currentBoard, piece)) {
                        moves.add(new Coordinate(nx, ny, currentBoard.getBoardID()));

                        String k = nx + "," + ny;
                        if (!visited.contains(k)) {
                            visited.add(k);
                            toCheck.add(new int[]{nx, ny});
                        }
                    }
                } else {
                    Coordinate found = findBoardCoordinate(currentBoard, piece, off[0], off[1], baseTileSize);
                    if (found != null) {
                        moves.add(found);

                        String k = found.x() + "," + found.y() + "," + found.boardID();
                        if (!visited.contains(k)) {
                            visited.add(k);
                            toCheck.add(new int[]{found.x(), found.y()});
                        }
                    }
                }
            }
        }
        return moves;
    }
    private static Coordinate findBoardCoordinate(Board board, Piece piece, int dx, int dy, int tileSize) {
        List<Coordinate> list = new ArrayList<>();
        boardFinder(board, piece, dx, dy, tileSize, list);
        return list.isEmpty() ? null : list.get(0);
    }

    public static List<Coordinate> getCamelMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>();

        // Base camel offsets
        int[][] baseOffsets = {
                {-3, -1}, {-3, 1},
                {3, -1}, {3, 1},
                {-1, -3}, {-1, 3},
                {1, -3}, {1, 3}
        };

        for (int[] offset : baseOffsets) {
            int scale = piece.getPieceSize();
            int offsetX = offset[0] * scale;
            int offsetY = offset[1] * scale;
            addMoveForOffset(piece, offsetX, offsetY, board, moves);
        }
        return moves;
    }

    public static List<Coordinate> getZebraMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>();

        // Base camel offsets
        int[][] baseOffsets = {
                {-3, -2}, {-3, 2},
                {3, -2}, {3, 2},
                {-2, -3}, {-2, 3},
                {2, -3}, {2, 3}
        };

        for (int[] offset : baseOffsets) {
            int scale = piece.getPieceSize();
            int offsetX = offset[0] * scale;
            int offsetY = offset[1] * scale;
            addMoveForOffset(piece, offsetX, offsetY, board, moves);
        }
        return moves;
    }

    public static List<Coordinate> getFlamingoMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>();

        // Base flamingo offsets
        int[][] baseOffsets = {
                {-6, -1}, {-6, 1},
                {6, -1}, {6, 1},
                {-1, -6}, {-1, 6},
                {1, -6}, {1, 6}
        };

        for (int[] offset : baseOffsets) {
            int scale = piece.getPieceSize();
            int offsetX = offset[0] * scale;
            int offsetY = offset[1] * scale;
            addMoveForOffset(piece, offsetX, offsetY, board, moves);
        }
        return moves;
    }

    public static List<Coordinate> getHareMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>();

        // Base hare offsets
        int[][] baseOffsets = {
                {-2, -2}, {-2, 2},
                {2, -2}, {2, 2}
        };

        for (int[] offset : baseOffsets) {
            int scale = piece.getPieceSize();
            int offsetX = offset[0] * scale;
            int offsetY = offset[1] * scale;
            addMoveForOffset(piece, offsetX, offsetY, board, moves);
        }
        return moves;
    }

    public static List<Coordinate> getThreeLeaperMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>();

        // Base leaper offsets
        int[][] baseOffsets = {
                {-3, -3}, {-3, 3},
                {3, -3}, {3, 3},
                {-3, -3}, {-3, 3},
                {3, -3}, {3, 3}
        };

        for (int[] offset : baseOffsets) {
            int scale = piece.getPieceSize();
            int offsetX = offset[0] * scale;
            int offsetY = offset[1] * scale;
            addMoveForOffset(piece, offsetX, offsetY, board, moves);
        }
        return moves;
    }

    public static List<Coordinate> getFerzMoves(Piece piece, Board board) {
        List<Coordinate> moves = new ArrayList<>();
        int stepSize = piece.getPieceSize();

        // moves 1 square diagonally
        int[][] offsets = {
                {-stepSize, -stepSize}, {-stepSize, stepSize},
                {stepSize, -stepSize}, {stepSize, stepSize}
        };

        for (int[] offset : offsets) {
            addMoveForOffset(piece, offset[0], offset[1], board, moves);
        }

        return moves;
    }

    public static List<Coordinate> getXiangqiMoves(Piece piece, Board board){
        List<Coordinate> moves = new ArrayList<>();

        //TODO - Moves one square orthogonally and one diagonally. It cannot jump over other pieces.

        return moves;
    }

    public static List<Coordinate> getChameleonMoves(Piece piece, Board board){
        List<Coordinate> moves = new ArrayList<>();
        //TODO - probably needs fixing.

        //TODO - Changes its powers, but not its color, on each move.
        // Starts as a Knight on its first move, then plays as Bishop,
        // then as Rook, then as Queen, then reverts as Knight and
        // again always in this order. [wiki]

        if (Timer.turnCounter / 2 % 4 == 0){
            moves.addAll(getKnightMoves(piece, board));
        } else if (Timer.turnCounter / 2 % 4 == 1){
            moves.addAll(getSlidingMoves(piece, board, bishopDirections));
        } else if (Timer.turnCounter / 2 % 4 == 2){
            moves.addAll(getSlidingMoves(piece, board, rookDirections));
        } else if (Timer.turnCounter / 2 % 4 == 3){
            moves.addAll(getSlidingMoves(piece, board, queenDirections));
        }

        return moves;
    }
}