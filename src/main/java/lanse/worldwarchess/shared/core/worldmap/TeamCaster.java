package lanse.worldwarchess.shared.core.worldmap;

import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.piece.CommanderPiece;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TeamCaster {

    // needs documentation because it was like 12:30 AM after a football game day and I just could not
    // be bothered to understand how doing a lookup for a class constructor was the slowest possible thing
    // java could do thousands of times in a row so I asked AI to help optimize it and the mess became
    // even messier, but this is so much better

    private static final Map<Class<? extends Piece>, Constructor<? extends Piece>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    private static Constructor<? extends Piece> getCachedConstructor(Class<? extends Piece> clazz) throws Exception {
        Constructor<? extends Piece> ctor = CONSTRUCTOR_CACHE.get(clazz);
        if (ctor == null) {
            ctor = clazz.getConstructor(
                    int.class, Board.class, String.class, String.class,
                    int.class, int.class, int.class, int.class, int.class
            );
            ctor.setAccessible(true);
            CONSTRUCTOR_CACHE.put(clazz, ctor);
        }
        return ctor;
    }

    public static void convertTeam(String teamName, Class<? extends Piece> pieceClass, Class<? extends Piece> commanderClass, Predicate<Piece> filter) {
        List<Piece> allPieces = WorldMap.pieces;
        List<Piece> toConvert = WorldMap.getAllPiecesOfTeam(teamName);

        List<Piece> converted = toConvert.parallelStream()
                .filter(p -> filter == null || filter.test(p))
                .map(p -> {
                    try {
                        Class<? extends Piece> clazz =
                                (p instanceof CommanderPiece && commanderClass != null)
                                        ? commanderClass
                                        : pieceClass;

                        Constructor<? extends Piece> ctor = getCachedConstructor(clazz);
                        return ctor.newInstance(
                                p.getPlayerID(), p.getBoard(), String.valueOf(p.getType()), teamName,
                                p.getBoardX(), p.getBoardY(), p.getPosX(), p.getPosY(), p.getPieceSize()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Set<Piece> convertedSet = new HashSet<>(toConvert);
        List<Piece> newList = new ArrayList<>(allPieces.size());
        for (Piece p : allPieces) {
            if (convertedSet.contains(p)) continue; // skip old
            newList.add(p);
        }
        newList.addAll(converted);
        newList.removeIf(p -> !TextureKey.textures.containsKey(TextureKey.getTextureKey(p)));

        WorldMap.pieces = new CopyOnWriteArrayList<>(newList);
    }

    public static void convertTeam(String teamName, Class<? extends Piece> pieceClass, Class<? extends Piece> commanderClass) {
        convertTeam(teamName, pieceClass, commanderClass, null);
    }
}