package lanse.worldwarchess.shared.central;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AudioKey {

    //This class preloads all the audio like texture key does so it isnt a problem on first attempt or like game starting.
    public static final String[] soundKeys = {
            "capture", "castle", "en-passant", "game-over", "invalid-move",
            "pregame", "promote", "valid-move", "new-turn",

            "Lego-yoda-death-sound", "THE-ROOK", "THE-BISHOP", "THE-KNIGHT", "THE-QUEEN"
    };

    public static final Map<String, Clip> clips = new HashMap<>();

    public static void loadSounds() {
        for (String key : soundKeys) {
            String path = "/sounds/" + key + ".wav";

            try (InputStream is = AudioKey.class.getResourceAsStream(path)) {
                if (is == null) {
                    System.err.println("Missing sound: " + path);
                    continue;
                }

                BufferedInputStream bis = new BufferedInputStream(is);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(bis);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clips.put(key, clip);

            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                System.err.println("Failed to load sound: " + path);
                e.printStackTrace();
            }
        }

        System.out.println("Preloaded " + clips.size() + " sounds.");
    }

    public static Clip getClip(String key) {
        Clip original = clips.get(key);
        if (original == null) return null;

        try {
            // Duplicate the audio data so each clip can play independently
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(Objects.requireNonNull(AudioKey.class.getResourceAsStream("/sounds/" + key + ".wav"))));
            Clip newClip = AudioSystem.getClip();
            newClip.open(ais);
            return newClip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
