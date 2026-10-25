package lanse.worldwarchess.shared.central;

import lanse.worldwarchess.shared.core.piece.Piece;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class TextureKey {

    public static final Map<String, BufferedImage> textures = new HashMap<>();

    /** remember to add new pieces to this texture key!!! **/

    //This class will initialize all the textures, so it doesn't need to keep doing the lookup to the PNGs library.

    //TODO - considering merging this all into 1 buffered image texture atlas since its apparently faster to
    // use 1 image instead of constantly accessing different images. Maybe its because its stored into a cache but idk.
    public final static String[] tiles = {"bs", "ws", "us", "is"};

    public final static String[] pieces = {"bb", "bk", "bn", "bp", "bq", "br", "wb", "wk", "wn", "wp", "wq",
            "wr", "zb", "zk", "zn", "zp", "zq", "zr", "zs", "vb", "vk", "vn", "vp", "vq", "vr", "vs",
            "hb", "hk", "hn", "hp", "hq", "hr", "hs", "mb", "mk", "mn", "mp", "mq", "mr", "pb", "pk",
            "pn", "pq", "pr", "sb", "sk", "sn", "sp", "sq", "sr", "ab", "ak", "an", "ap", "aq", "ar",
            "ib", "ik", "in", "iq", "ir", "nb", "nk", "nn", "np", "nq", "nr", "bkc", "bc", "wc", "wkc",
            "cb", "ck", "cn", "cp", "cq", "cr", "eb", "ek", "en", "ep", "eq", "er", "ss", "2s"};

    public final static String[] fairies = {"wamazon", "bamazon", "wcamel", "bcamel", "wflamingo", "bflamingo",
            "wzebra", "bzebra"};

    public final static String[] highlights = {"CaptureSquare", "LegalSquare", "VisibleSquare", "VisibleCapture"};

    public final static String[] bosses = {"electroking", "trojanknight", "MysteryBoss", "pointer", "Amalgamation", "weepingknight",
            "illusioner", "zoo", "healerqueen", "conway", "sultan"};

    public final static String[] attacks = {"FireAttack", "FreezeAttack", "MageAttack", "ThunderAttack", "Barrier", "HealPulse"};

    public final static String[] ui = {"TextBubble"};


    //preload all these textures
    public static void loadTextures() {
        String[] textureKeys = Stream.of(tiles, pieces, fairies, highlights, bosses, attacks, ui)
                .flatMap(Arrays::stream).toArray(String[]::new);

        for (String key : textureKeys) {
            try {
                textures.put(key, ImageIO.read(Objects.requireNonNull(TextureKey.class.getResource("/images/assets/" + key + ".png"))));
            } catch (IOException | NullPointerException ignored) {}
        }
    }


    public static String getTextureKey(Piece piece) {
        String textureKey = switch (piece.getType()) {
            case PAWN -> "wp";
            case ROOK -> "wr";
            case KNIGHT -> "wn";
            case BISHOP -> "wb";
            case QUEEN -> "wq";
            case KING -> "wk";
        };

        char replacement;

        if (piece.getTeam().equalsIgnoreCase("Black")) {
            replacement = 'b';
            textureKey = replacement + textureKey.substring(1);
        }

        return textureKey;
    }
}