package lanse.worldwarchess.shared.core.piece;

import lanse.worldwarchess.shared.central.CentralTick;
import lanse.worldwarchess.shared.core.gamemode.GameMode;
import lanse.worldwarchess.shared.core.piece.ai.bosses.BossHealthManager;
import lanse.worldwarchess.shared.core.piece.ai.bosses.NpcBoss;
import lanse.worldwarchess.shared.core.piece.marker.DeathMarker;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.util.*;

public class AttackOnTitans {

    //This class is responsible for calculating how pieces capture a titan. Since it is chance based, this will
    // resolve all conflicts, and either kill the titan, or push the attackers away.

    public static List<TitanAttack> attacksOnTitans = new ArrayList<>();
    public record TitanAttack(Piece attacker, Piece titan) {}

    public static void resolveAttacksOnTitans() {
        if (attacksOnTitans.isEmpty()) return;

        Map<Piece, List<Piece>> attackGroups = new HashMap<>();

        for (TitanAttack attack : AttackOnTitans.attacksOnTitans) {
            attackGroups.computeIfAbsent(attack.titan, k -> new ArrayList<>()).add(attack.attacker);
        }

        for (Map.Entry<Piece, List<Piece>> entry : attackGroups.entrySet()) {
            Piece titan = entry.getKey();
            List<Piece> attackers = entry.getValue();
            int attackerCount = attackers.size();
            int baseDamage = 0;

            for (Piece attacker : attackers) {
                baseDamage += BossHealthManager.getPieceStrength(attacker);
            }

            double multiplier = 1.0 + (attackerCount - 1) * 0.2;
            int totalDamage = (int) Math.round(baseDamage * multiplier);

            int remaining = 0;

            // Decrease boss HP
            if (titan instanceof NpcBoss boss) {
                boss.damagePiece(totalDamage);
                remaining = boss.getCurrentHealth();

            } else if (titan instanceof CommanderPiece cp) {
                cp.damagePiece(totalDamage);
                cp.currentHealth -= totalDamage;
                remaining = cp.getCurrentHealth();
            }

            //  Boss dies L
            if (remaining <= 0) {

                if (titan.getType() == PieceType.KING) {
                    if (titan instanceof CommanderPiece) {
                        GamePanel.winningTeam = titan.getTeam().equals("White") ? "BLACK" : "WHITE";
                        if (!GameMode.isTutorial) CentralTick.gameOver = true;
                        DeathMarker.deathRow.addAll(WorldMap.getAllPiecesOfTeam(titan.getTeam()));
                        SoundManager.gameOver();
                    }
                }

                WorldMap.killPiece(titan, true, true, Optional.empty());
            }

            // Boss survived, scatter attackers
            else {
                for (Piece attacker : attackers) {
                    movePieceToRandomEmptySquare(attacker, titan.getCurrentHealth());
                }
            }
        }
        AttackOnTitans.attacksOnTitans.clear();
    }

    private static void movePieceToRandomEmptySquare(Piece piece, int bossHealth) {
        Board board = piece.getBoard();
        assert board != null;
        List<int[]> emptySquares = board.getEmptySquares();

        if (emptySquares.isEmpty()) {
            // No empty squares available, kill the attacker piece
            WorldMap.killPiece(piece, true, true);
            return;
        }

        int px = piece.getPosX();
        int py = piece.getPosY();
        int boardSize = board.getBoardSize();

        // Search for empty squares in increasing radius
        for (int radius = 1; radius <= boardSize; radius++) {
            List<int[]> validMoves = new ArrayList<>();
            for (int[] pos : emptySquares) {
                int x = pos[0], y = pos[1];

                // Manhattan distance check
                if (Math.abs(x - px) + Math.abs(y - py) == radius) {
                    // Ensure it's inside board boundaries
                    if (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
                        validMoves.add(pos);
                    }
                }
            }
            if (!validMoves.isEmpty()) {
                int[] newPos = validMoves.get(new Random().nextInt(validMoves.size()));
                int newX = newPos[0];
                int newY = newPos[1];
                Piece oldPiece = new Piece(piece.getPlayerID(), piece.getBoard(), String.valueOf(piece.getType()), piece.getTeam(),
                        piece.getBoardX(), piece.getBoardY(), piece.getPosX(), piece.getPosY(), piece.getPieceSize());
                // Move the piece
                piece.setMapPosition(board.getBoardX(), board.getBoardY());
                piece.setBoardPosition(newX, newY);
                MoveHistory.addMoveForRecord(oldPiece, piece, true, "push", bossHealth, null);
                return;
            }
        }

        // If somehow no valid position is found, kill that piece
        WorldMap.killPiece(piece, true, true, Optional.empty());
    }
}
