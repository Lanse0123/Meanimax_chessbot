package lanse.worldwarchess.shared.core.sound;

import lanse.worldwarchess.shared.central.AudioKey;
import lanse.worldwarchess.shared.client.storage.Settings;
import lanse.worldwarchess.shared.core.piece.Piece;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.util.Random;

public class SoundManager {

    /** DON'T FORGET TO ADD TO AUDIO KEY CLASS WHEN ADDING NEW SOUNDS!!! **/

    // Prevents audio spam from multiple NPCs
    public static boolean soundLimitReached = false;

    /** Plays a preloaded sound by key (no pitch variation). */
    public static void playSound(String soundFilePath) {
        if (soundLimitReached) return;

        if (soundFilePath.contains("invalid") && !Settings.isPlayIllegalMoveSound()) {
            return; // Don't play sound if illegal move sounds are disabled
        }

        String key = extractKey(soundFilePath);
        Clip clip = AudioKey.getClip(key);

        if (clip == null) {
            System.out.println("Sound not found in preloaded cache: " + key);
            return;
        }

        setVolume(clip, Settings.getVolume());

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) clip.close();
        });

        clip.start();
        soundLimitReached = true;
    }

    /** Plays a preloaded sound with randomized pitch. */
    public static void playSoundWithRandomPitch(String soundFilePath, boolean lowSound) {
        if (soundLimitReached) return;

        if (soundFilePath.contains("illegal_move") && !Settings.isPlayIllegalMoveSound()) {
            return; // Don't play sound if illegal move sounds are disabled
        }

        String key = extractKey(soundFilePath);
        Clip baseClip = AudioKey.clips.get(key);
        if (baseClip == null) {
            System.out.println("Sound not found in preloaded cache: " + key);
            return;
        }

        try {
            // Reload from stream to avoid modifying preloaded clip
            Clip clip = AudioKey.getClip(key);
            if (clip == null) return;

            // Randomize pitch
            Random rand = new Random();
            float randomPitch = 0.25f + (rand.nextFloat() * 2.0f);
            if (lowSound) randomPitch *= 0.1f;

            AudioFormat format = clip.getFormat();
            AudioFormat newFormat = new AudioFormat(
                    format.getEncoding(),
                    format.getSampleRate() * randomPitch,
                    format.getSampleSizeInBits(),
                    format.getChannels(),
                    format.getFrameSize(),
                    format.getFrameRate() * randomPitch,
                    format.isBigEndian()
            );

            // Apply volume
            setVolume(clip, Settings.getVolume());

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) clip.close();
            });

            clip.start();
            soundLimitReached = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Adjusts clip volume based on user settings. */
    private static void setVolume(Clip clip, int volumePercentage) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float volume = min + (volumePercentage / 100.0f) * (max - min);
            gainControl.setValue(volume);
        } catch (Exception ignored) {}
    }

    private static String extractKey(String path) {
        return path.replace("/sounds/", "").replace(".wav", "");
    }


    public static void capturePiece(Piece piece) {
        if (Settings.THE_ROOK()) {
            if (piece != null) validMove(piece);
            playSoundWithRandomPitch("/sounds/Lego-yoda-death-sound.wav", false);
        }

        playSoundWithRandomPitch("/sounds/capture.wav", false);
    }

    public static void commanderMove(Piece piece) {
        boolean lowSound = false;
        String sound = "/sounds/commander-move.wav";

        if (Settings.THE_ROOK()) {
            lowSound = true;
            sound = switch (piece.getType()) {
                case ROOK -> "/sounds/THE-ROOK.wav";
                case BISHOP -> "/sounds/THE-BISHOP.wav";
                case KNIGHT -> "/sounds/THE-KNIGHT.wav";
                case QUEEN -> "/sounds/THE-QUEEN.wav";
                default -> {
                    lowSound = false;
                    yield "/sounds/commander-move.wav";
                }
            };
        }

        playSoundWithRandomPitch(sound, lowSound);
    }

    public static void validMove(Piece piece) {
        String sound = "/sounds/valid-move.wav";

        if (Settings.THE_ROOK()) {
            sound = switch (piece.getType()) {
                case ROOK -> "/sounds/THE-ROOK.wav";
                case BISHOP -> "/sounds/THE-BISHOP.wav";
                case KNIGHT -> "/sounds/THE-KNIGHT.wav";
                case QUEEN -> "/sounds/THE-QUEEN.wav";
                default -> "/sounds/valid-move.wav";
            };
        }

        playSoundWithRandomPitch(sound, false);
    }

    public static void ultraKill(Piece piece) {
        if (Settings.THE_ROOK() && piece != null) commanderMove(piece);
        playSoundWithRandomPitch("/sounds/commander-ultrakill.wav", false);
    }

    public static void commanderAttack(Piece piece) {
        if (Settings.THE_ROOK() && piece != null) commanderMove(piece);
        playSoundWithRandomPitch("/sounds/commander-attack.wav", false);
    }

    public static void gameOver() { playSound("/sounds/game-over.wav"); }
    public static void preGame() {
        playSound("/sounds/pregame.wav");
    }
    public static void promote() { playSound("/sounds/promote.wav"); }
    public static void invalidMove() { playSound("/sounds/invalid-move.wav"); }
    public static void castle() { playSound("/sounds/castle.wav"); }

    public static void enPassant(Piece piece) {
        playSoundWithRandomPitch("/sounds/en-passant.wav", false);
    }
}
