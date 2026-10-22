package lanse.worldwarchess.shared.core.piece;

import lanse.worldwarchess.shared.core.piece.ai.attacks.*;
import lanse.worldwarchess.shared.core.piece.ai.attacks.projectile.ShotgunShell;
import lanse.worldwarchess.shared.core.piece.ai.bosses.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.commander.*;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.HealerPiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.IcePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.MagePiece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.elementalpiece.PhoenixPiece;
import lanse.worldwarchess.shared.core.piece.fairies.FairyCommanderPiece;
import lanse.worldwarchess.shared.core.piece.fairies.FairyPiece;
import lanse.worldwarchess.shared.core.tutorial.CommanderTutorialPiece;
import lanse.worldwarchess.shared.core.tutorial.TutorialPiece;
import lanse.worldwarchess.shared.utils.BiDirectionalMap;

public class PieceClassPrefixMap {

    //This class is a 2 way hashmap which makes it easier to cast pieces from 1 class to another, or send a class
    // as data to another player.
    private static final BiDirectionalMap<Integer, Class<? extends Piece>> map = new BiDirectionalMap<>();


    // this could be refactored out but since theres 44 of them, I am way too lazy for that
    public static void register(int index, Class<? extends Piece> pieceClass) {
        map.put(index, pieceClass);
    }

    public static Class<? extends Piece> getPieceClass(int index) {
        return map.getValue(index);
    }

    public static int getPrefixIndex(Class<? extends Piece> pieceClass) {
        Integer result = map.getKey(pieceClass);
        return result != null ? result : -1;
    }

    public static void initPrefixMap(){
        //DO NOT REMOVE OR CHANGE THESE, ONLY ADD TO THE LIST!
        register(1, Piece.class);
        register(2, CommanderPiece.class);
        register(3, NpcPiece.class);
        register(4, NpcCommanderPiece.class);
        register(5, NpcBoss.class);
        register(6, TutorialPiece.class);
        register(7, CommanderTutorialPiece.class);
        register(8, PieceAttack.class);
        register(9, FairyPiece.class);
        register(10, FairyCommanderPiece.class);
        register(11, ZombiePiece.class);
        register(12, VampirePiece.class);
        register(13, SpartanPiece.class);
        register(14, NecromancerPiece.class);
        register(15, DungeonPiece.class);
        register(16, AssassinPiece.class);
        register(17, PhoenixPiece.class);
        register(18, MagePiece.class);
        register(19, IcePiece.class);
        register(20, HealerPiece.class);
        register(21, CommanderZombiePiece.class);
        register(22, CommanderVampirePiece.class);
        register(23, CommanderSpartanPiece.class);
        register(24, CommanderAssassinPiece.class);
        register(25, TrojanHorseBoss.class);
        register(26, ElectroKingBoss.class);
        register(27, HealerQueenBoss.class);
        register(28, Illusioner.class);
        register(29, ShotgunKingBoss.class);
        register(30, WeepingKnight.class);
        register(31, ZooBoss.class);
        register(32, PointerPiece.class);
        register(33, MysteryBoss.class);
        register(34, BarrierWall.class);
        register(35, FireAttack.class);
        register(36, FreezeAttack.class);
        register(37, HealPulse.class);
        register(38, MageAttack.class);
        register(39, ShotgunShell.class);
        register(40, AmalgamationBoss.class);
        register(41, SultanBoss.class);
        register(42, ConwayBoss.class); //42 is life
        register(43, ThunderAttack.class);
        register(44, TetrachBritishTankBoss.class);
        register(45, CommanderPhoenixPiece.class);
        register(46, CommanderIcePiece.class);
        register(47, CommanderMagePiece.class);
        register(48, CommanderNecromancerPiece.class);
        register(49, SentinelPiece.class);
        register(50, CommanderSentinelPiece.class);
        register(51, NeonPiece.class);
        register(52, CommanderNeonPiece.class);
    }
}
