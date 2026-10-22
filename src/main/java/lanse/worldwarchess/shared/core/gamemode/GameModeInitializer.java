package lanse.worldwarchess.shared.core.gamemode;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.IcePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.MagePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.PhoenixPiece;
import lanse.worldwarchess.shared.core.worldmap.TeamCaster;

import java.awt.*;
import java.lang.reflect.Method;

public class GameModeInitializer extends GameMode {

    public static void initializeTeams() {
        switch (currentMode) {
            case WAR, SIEGE -> {} //no casting is needed for these gamemodes

            case HUMAN_VS_NPC -> {
                Class<? extends Piece> npcClass = getPieceClassForFaction(selectedNPCFaction, false);
                Class<? extends Piece> npcCommanderClass = getPieceClassForFaction(selectedNPCFaction, true);
                TeamCaster.convertTeam("Black", npcClass, npcCommanderClass);

                if (NpcPiece.class.isAssignableFrom(npcClass)) {
                    try {
                        Method m = npcClass.getMethod("getColorOverride");
                        GameMode.currentGamemodeColor2 = (Color) m.invoke(null);
                    } catch (Exception e) {
                        System.out.println("Team cast color error: " + e);
                        GameMode.currentGamemodeColor2 = Color.BLACK;
                    }
                } else {
                    GameMode.currentGamemodeColor2 = Color.BLACK;
                }
                GameMode.currentGamemodeColor1 = Color.WHITE;
            }

            case NPC_WAR -> {
                Class<? extends Piece> npcClassA = getPieceClassForFaction(npcFactionA, false);
                Class<? extends Piece> npcCommanderA = getPieceClassForFaction(npcFactionA, true);
                Class<? extends Piece> npcClassB = getPieceClassForFaction(npcFactionB, false);
                Class<? extends Piece> npcCommanderB = getPieceClassForFaction(npcFactionB, true);

                TeamCaster.convertTeam("White", npcClassA, npcCommanderA);
                TeamCaster.convertTeam("Black", npcClassB, npcCommanderB);

                if (NpcPiece.class.isAssignableFrom(npcClassA)) {
                    try {
                        Method m = npcClassA.getMethod("getColorOverride");
                        GameMode.currentGamemodeColor1 = (Color) m.invoke(null);
                    } catch (Exception e) {
                        System.out.println("Team cast color error: " + e);
                        GameMode.currentGamemodeColor1 = Color.WHITE;
                    }
                } else {
                    GameMode.currentGamemodeColor2 = Color.WHITE;
                }

                if (NpcPiece.class.isAssignableFrom(npcClassB)) {
                    try {
                        Method m = npcClassB.getMethod("getColorOverride");
                        GameMode.currentGamemodeColor2 = (Color) m.invoke(null);
                    } catch (Exception e) {
                        System.out.println("Team cast color error: " + e);
                        GameMode.currentGamemodeColor2 = Color.BLACK;
                    }
                } else {
                    GameMode.currentGamemodeColor2 = Color.BLACK;
                }
            }
        }
    }

    private static Class<? extends Piece> getPieceClassForFaction(String factionName, boolean commander) {
        return switch (factionName.toLowerCase()) {
            case "zombies" -> commander ? CommanderZombiePiece.class : ZombiePiece.class;
            case "vampires" -> commander ? CommanderVampirePiece.class : VampirePiece.class;
            case "spartans" -> commander ? CommanderSpartanPiece.class : SpartanPiece.class;
            case "assassins" -> commander ? CommanderAssassinPiece.class : AssassinPiece.class;
            case "mage" -> commander ? CommanderMagePiece.class : MagePiece.class;
            case "phoenix" -> commander ? CommanderPhoenixPiece.class : PhoenixPiece.class;
            case "winter" -> commander ? CommanderIcePiece.class : IcePiece.class;
            case "necromancer" -> commander ? CommanderNecromancerPiece.class : NecromancerPiece.class;
            case "sentinel" -> commander ? CommanderSentinelPiece.class : SentinelPiece.class;
            case "neon" -> commander ? CommanderNeonPiece.class : NeonPiece.class;
            default -> commander ? CommanderZombiePiece.class : ZombiePiece.class; //default should be null
        };
    }
}
