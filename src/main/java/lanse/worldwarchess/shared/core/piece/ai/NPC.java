package lanse.worldwarchess.shared.core.piece.ai;

import lanse.worldwarchess.cascading.CascadePacketBundler;
import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.gamemode.GameModeType;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.ai.bosses.BossHealthManager;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.NpcCommanderPiece;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.MoveExecutor;
import lanse.worldwarchess.shared.core.worldmap.Utils;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

public class NPC {
    /** remember when adding new pieces to add it to the texture key in the TextureKey class! **/

    //This is the central class that orders all NPC pieces to move when needed. It also stores the core logic for
    // some of the ai types like zombies, vampires, and etc.

    public static volatile boolean stopMovement = false;
    public static volatile boolean areNpcsMoving = false;
    public static Map<LegalMoveDetector.Coordinate, List<String>> attackedSquares = new HashMap<>();
    public static CascadePacketBundler bundler;

    public static int setLimiter = 16;
    private static final HashMap<Integer, Integer> npcLimiterMap = new HashMap<>();

    public static void doAllNpcLogic(String team, String ignoredTeam, boolean doOtherTeams, boolean skipTurn) {
        //moved NPC logic on a different thread because it can be laggy
        new Thread(() -> {
            bundler = new CascadePacketBundler(CascadePacketManager.PacketRange.EVERYONE);
            areNpcsMoving = true;
            stopMovement = false;
            npcLimiterMap.clear();

            List<Piece> pieceList = new ArrayList<>(WorldMap.getAllNPCPieces());
            Collections.shuffle(pieceList); //randomize which order the pieces are in to effectively randomize piece movement
            for (Piece npc : pieceList) {
                if (stopMovement){
                    stopMovement = false;
                    areNpcsMoving = false;
                    return;
                }

                int playerID = npc.getPlayerID();

                int usedMoves = npcLimiterMap.getOrDefault(playerID, 0);
                if (usedMoves >= setLimiter && !(GameMode.currentMode == GameModeType.SIEGE) && !(GameMode.currentMode == GameModeType.NPC_WAR)) {
                    continue;
                }

                boolean canMove = false;

                if (Objects.equals(npc.getTeam(), team)) {
                    canMove = true;
                } else if (doOtherTeams && !Objects.equals(npc.getTeam(), ignoredTeam)) {
                    canMove = true;
                }

                if (canMove) {
                    npc.move();
                    npcLimiterMap.put(playerID, usedMoves + 1);
                }
            }

            if (!GameMode.isSinglePlayer) {
                CascadePacketManager.Packet movebundle = bundler.getPacketBundle();
                if (!movebundle.content().isEmpty()) {
                    CascadePacketManager.manager.sendPacketToEveryone(
                            CascadePacketManager.PacketType.BUNDLE,
                            movebundle.serialize(),
                            Optional.empty());
                }
            }

            if (GameMode.currentMode == GameModeType.NPC_WAR || (GameMode.currentMode != GameModeType.WAR && skipTurn) ) {
                //Sleep at the end of NPC turn so it gives a nice break between moves
                if (Main.gamePanel.sidebarMenu.slowMode) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }

                //end turn if its an NPC one since they all moved
                Timer.currentTime = 0;
            }
            areNpcsMoving = false;
        }, "NPCMover").start();
    }

    public static void stopAllNpcMovement() {
        boolean isThereAnyNPCs = false;
        for (Piece piece : WorldMap.pieces){
            if (piece instanceof NpcPiece || piece instanceof NpcCommanderPiece) {
                isThereAnyNPCs = true;
                break;
            }
        }

        if (!isThereAnyNPCs) return;

        stopMovement = true;

        //loop until its successfully stopped the NPCs
        while (stopMovement && areNpcsMoving);
    }

    private static void performMove(Piece piece, List<LegalMoveDetector.Coordinate> candidateMoves) {
        if (candidateMoves.isEmpty()) return;

        LegalMoveDetector.Coordinate chosenMove = candidateMoves.get(new Random().nextInt(candidateMoves.size()));
        int moveX = chosenMove.x();
        int moveY = chosenMove.y();
        int moveBoardID = chosenMove.boardID();

        Board moveBoard = WorldMap.getBoardByID(moveBoardID);
        if (moveBoard == null) return;

        MoveExecutor.executeMove(piece, moveBoard, moveX, moveY, piece.getPosX(), piece.getPosY(), true,
                "move", false, bundler, false);
    }

    public static List<LegalMoveDetector.Coordinate> filterCaptureMoves(Piece piece, List<LegalMoveDetector.Coordinate> moves) {
        List<LegalMoveDetector.Coordinate> captureMoves = new ArrayList<>();
        for (LegalMoveDetector.Coordinate move : moves) {
            int targetX = move.x();
            int targetY = move.y();
            int targetBoardID = move.boardID();
            Board targetBoard = WorldMap.getBoardByID(targetBoardID);

            if (targetBoard != null) {
                Piece targetPiece = targetBoard.getPieceAt(targetX, targetY, Optional.of(piece), WorldMap.getAllPiecesOfBoard(targetBoard));
                if (targetPiece != null && !targetPiece.getTeam().equals(piece.getTeam())) {
                    captureMoves.add(move);
                }
            }
        }
        return captureMoves;
    }

    public static void zombieMoveLogic(Piece piece, int moveChance) {
        Random random = new Random(); //chance zombie just does nothing that turn
        if (random.nextInt(100) > moveChance) return;

        List<LegalMoveDetector.Coordinate> legalMoves = LegalMoveDetector.getLegalMoves(piece);
        // Zombies = random legal move
        performMove(piece, legalMoves);
    }

    public static void vampireMoveLogic(Piece piece) {
        List<LegalMoveDetector.Coordinate> legalMoves = LegalMoveDetector.getLegalMoves(piece);

        // Vampires = prioritize captures, otherwise random legal move
        List<LegalMoveDetector.Coordinate> captureMoves = filterCaptureMoves(piece, legalMoves);
        performMove(piece, captureMoves.isEmpty() ? legalMoves : captureMoves);
    }

    public static void neonMoveLogic(Piece piece, int moveChance) {
        Random random = new Random();
        if (random.nextInt(100) > moveChance) return;

        List<LegalMoveDetector.Coordinate> legalMoves = LegalMoveDetector.getLegalMoves(piece);

        // neon = prioritize highest value captures, otherwise random legal move
        List<LegalMoveDetector.Coordinate> captureMoves = filterCaptureMoves(piece, legalMoves);

        LegalMoveDetector.Coordinate candidate = null;
        int max = -1;

        for (LegalMoveDetector.Coordinate move : captureMoves) {
            Piece p = getPieceAt(move, piece);
            if (p == null) continue;

            int strength = BossHealthManager.getPieceStrength(p);
            if (strength > max){
                max = strength;
                candidate = move;
            }
        }

        if (candidate != null) {
            performMove(piece, Collections.singletonList(candidate));
        } else {
            performMove(piece, legalMoves);
        }
    }

    public static void necromancerMoveLogic(Piece piece) {
        List<LegalMoveDetector.Coordinate> legalMoves = LegalMoveDetector.getLegalMoves(piece);

        // Avoid captures
        List<LegalMoveDetector.Coordinate> nonCaptureMoves = new ArrayList<>(legalMoves);
        nonCaptureMoves.removeAll(filterCaptureMoves(piece, legalMoves));
        performMove(piece, nonCaptureMoves.isEmpty() ? legalMoves : nonCaptureMoves);
    }

    public static void healerMoveLogic(Piece piece) {
        List<LegalMoveDetector.Coordinate> legalMoves = LegalMoveDetector.getLegalMoves(piece);
        if (legalMoves.isEmpty()) return;

        // Find nearest ally
        Piece nearestAlly = WorldMap.getAllNPCPieces().stream()
                .filter(p -> p != piece && p.getTeam().equals(piece.getTeam()))
                .min(Comparator.comparingDouble(p -> Utils.distance(piece, p)))
                .orElse(null);

        if (nearestAlly != null) {
            // Try to move closer
            int bestDist = Integer.MAX_VALUE;
            LegalMoveDetector.Coordinate bestMove = null;

            for (LegalMoveDetector.Coordinate move : legalMoves) {
                Board board = WorldMap.getBoardByID(move.boardID());
                if (board != null) {
                    int dist = Utils.distance(move.x(), move.y(), WorldMap.getBoardByID(move.boardID()), nearestAlly.getPosX(), nearestAlly.getPosY(), nearestAlly.getBoard());
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestMove = move;
                    }
                }
            }
            if (bestMove != null) {
                performMove(piece, List.of(bestMove));
                return;
            }
        }

        // Fallback: move randomly
        performMove(piece, legalMoves);
    }

    public static void spartanMoveLogic(Piece piece, boolean findCommanderOrRegular) {
        List<LegalMoveDetector.Coordinate> legalMoves = LegalMoveDetector.getLegalMoves(piece);
        if (legalMoves.isEmpty()) return;
        Piece nearestCommander;

        if (findCommanderOrRegular) {
            // Find nearest enemy Commander King
            nearestCommander = WorldMap.getAllKings(piece.getTeam(), true,true, false, WorldMap.pieces).stream()
                    .min(Comparator.comparingDouble(p -> Utils.distance(piece, p))).orElse(null);
        } else {
            // Find nearest enemy King
            nearestCommander = WorldMap.getAllKings(piece.getTeam(), false, true, false, WorldMap.pieces).stream()
                    .min(Comparator.comparingDouble(p -> Utils.distance(piece, p))).orElse(null);
        }

        if (nearestCommander != null) {
            // Try to move closer to that commander
            int bestDist = Integer.MAX_VALUE;
            LegalMoveDetector.Coordinate bestMove = null;

            for (LegalMoveDetector.Coordinate move : legalMoves) {
                Board board = WorldMap.getBoardByID(move.boardID());
                if (board != null) {
                    int dist = Utils.distance(move.x(), move.y(), WorldMap.getBoardByID(move.boardID()), nearestCommander.getPosX(), nearestCommander.getPosY(), nearestCommander.getBoard());
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestMove = move;
                    }
                }
            }
            if (bestMove != null) {
                performMove(piece, List.of(bestMove));
                return;
            }
        }

        // Fallback: move randomly
        performMove(piece, legalMoves);
    }

    public static void sentinelMoveLogic(Piece piece) {
        List<LegalMoveDetector.Coordinate> legalMoves = LegalMoveDetector.getLegalMoves(piece);
        if (legalMoves.isEmpty()) return;

        List<LegalMoveDetector.Coordinate> kingCaptures = new ArrayList<>();
        for (LegalMoveDetector.Coordinate move : legalMoves) {
            Piece p = getPieceAt(move, piece);
            if (p != null && !p.getTeam().equals(piece.getTeam()) && p.getType() == PieceType.KING) {
                kingCaptures.add(move);
            }
        }
        if (!kingCaptures.isEmpty()) {
            performMove(piece, kingCaptures);
            return;
        }

        List<LegalMoveDetector.Coordinate> queenCaptures = new ArrayList<>();
        for (LegalMoveDetector.Coordinate move : legalMoves) {
            Piece p = getPieceAt(move, piece);
            if (p != null && !p.getTeam().equals(piece.getTeam()) && p.getType() == PieceType.QUEEN) {
                queenCaptures.add(move);
            }
        }
        if (!queenCaptures.isEmpty()) {
            performMove(piece, queenCaptures);
            return;
        }

        Random random = new Random();
        int roll = random.nextInt(3);

        // do random capture.
        if (roll == 0) {
            List<LegalMoveDetector.Coordinate> captures = filterCaptureMoves(piece, legalMoves);
            if (!captures.isEmpty()) {
                performMove(piece, captures);
                return;
            } else {
                //try other strat if no captures exist
                roll = 1 + random.nextInt(2);
            }
        }

        // move as far away as possible from current position
        if (roll == 1) {
            LegalMoveDetector.Coordinate bestSafe = null;
            int bestSafeDist = -1;

            for (LegalMoveDetector.Coordinate m : legalMoves) {
                // skip unsafe unless it is a capture
                if (!isSquareEnemyAttacked(piece, m)) {
                    int d = Utils.distance(piece.getPosX(), piece.getPosY(), piece.getBoard(),
                            m.x(), m.y(), WorldMap.getBoardByID(m.boardID()));

                    if (d > bestSafeDist) {
                        bestSafeDist = d;
                        bestSafe = m;
                    }
                }
            }

            if (bestSafe != null) {
                performMove(piece, List.of(bestSafe));
                return;
            }

            // fallback to unsafe but farthest
            LegalMoveDetector.Coordinate best = null;
            int bestDist = -1;
            for (LegalMoveDetector.Coordinate m : legalMoves) {
                int d = Utils.distance(piece.getPosX(), piece.getPosY(), piece.getBoard(),
                        m.x(), m.y(), WorldMap.getBoardByID(m.boardID()));
                if (d > bestDist) {
                    bestDist = d;
                    best = m;
                }
            }
            if (best != null) {
                performMove(piece, List.of(best));
                return;
            }
        }

        // Move closest to nearest enemy king
        Piece nearestKing = WorldMap.getAllKings(piece.getTeam(), false, true, false, WorldMap.pieces).stream()
                .min(Comparator.comparingDouble(k -> Utils.distance(piece, k)))
                .orElse(null);

        if (nearestKing != null) {
            LegalMoveDetector.Coordinate best = null;
            int bestDist = Integer.MAX_VALUE;

            for (LegalMoveDetector.Coordinate m : legalMoves) {
                int d = Utils.distance(m.x(), m.y(), WorldMap.getBoardByID(m.boardID()),
                        nearestKing.getPosX(), nearestKing.getPosY(), nearestKing.getBoard());
                if (d < bestDist) {
                    bestDist = d;
                    best = m;
                }
            }

            if (best != null) {
                performMove(piece, List.of(best));
                return;
            }
        }

        List<LegalMoveDetector.Coordinate> safe = new ArrayList<>();
        List<LegalMoveDetector.Coordinate> captures = filterCaptureMoves(piece, legalMoves);

        for (LegalMoveDetector.Coordinate m : legalMoves) {
            if (captures.contains(m)) {
                safe.add(m);
                continue;
            }

            if (!isSquareEnemyAttacked(piece, m)) {
                safe.add(m);
            }
        }

        if (!safe.isEmpty()) {
            performMove(piece, safe);
            return;
        }

        performMove(piece, legalMoves);
    }

    public static String getRandomPieceType(int knightChance, int bishopChance, int rookChance, int queenChance) {
        double total = knightChance + bishopChance + rookChance + queenChance;
        double rand = Math.random() * total;

        if (rand < knightChance) return "knight";
        rand -= knightChance;

        if (rand < bishopChance) return "bishop";
        rand -= bishopChance;

        if (rand < rookChance) return "rook";

        return "queen";
    }

    public static void recalculateAttackerLocations() {
        attackedSquares.clear();
        boolean needsSafetyCheck = false;

        if (GameMode.isFogOfWar){
            needsSafetyCheck = true;
        }

        if (!needsSafetyCheck) {
            for (Piece piece : WorldMap.pieces) {
                if (piece instanceof NeedsSafetyCheck) {
                    needsSafetyCheck = true;
                    break;
                }
            }
        }

        if (needsSafetyCheck){
            attackedSquares = WorldMap.getAllEnemyMoves();
        }
    }

    private static boolean isSquareEnemyAttacked(Piece piece, LegalMoveDetector.Coordinate move) {
        LegalMoveDetector.Coordinate key = new LegalMoveDetector.Coordinate(move.x(), move.y(), move.boardID());

        List<String> attackers = NPC.attackedSquares.get(key);
        if (attackers == null) return false;

        return attackers.stream().anyMatch(team -> !team.equals(piece.getTeam()));
    }

    private static Piece getPieceAt(LegalMoveDetector.Coordinate move, Piece mover) {
        Board b = WorldMap.getBoardByID(move.boardID());
        if (b == null) return null;
        return b.getPieceAt(move.x(), move.y(), Optional.of(mover), WorldMap.getAllPiecesOfBoard(b));
    }
}