package lanse.worldwarchess.shared.client.storage;

import dev.dirs.ProjectDirectories;
import lanse.worldwarchess.shared.central.LogoManager;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Settings {

    //This class stores and queries your settings file. It also loads the settings screen. It can probably be made static.

    private static final Path settingsDir = Path.of(ProjectDirectories.from("dev", "Lanse", "WorldWarChess").configDir);
    private static final String SETTINGS_PATH = settingsDir + "/settings.json";

    private static int volume;
    private static String background;
    private static boolean playGothamSound;
    private static boolean showDeathMarkers;
    private static boolean playMainMenuStartup;
    private static boolean playIllegalMoveSound;

    public static void loadSettings() {
        Path path = Paths.get(SETTINGS_PATH);
        if (Files.exists(path)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_PATH))) {
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                JSONObject json = new JSONObject(jsonContent.toString());
                volume = json.optInt("volume", 100);
                background = json.optString("background", "Warped Plane");
                playGothamSound = json.optBoolean("playGothamSound", false);
                showDeathMarkers = json.optBoolean("showDeathMarkers", true);
                playMainMenuStartup = json.optBoolean("playMainMenuStartup", false);
                playIllegalMoveSound = json.optBoolean("playIllegalMoveSound", false);
            } catch (IOException e) {
                e.printStackTrace();
                setDefaultSettings();
            }
        } else {
            setDefaultSettings();
        }
    }

    private static void setDefaultSettings() {
        volume = 100;
        playGothamSound = false;
        showDeathMarkers = true;
        playMainMenuStartup = false;
        playIllegalMoveSound = false;
        background = "Warped Plane";
        saveSettings();
    }

    private static void saveSettings() {
        try {
            if (!Files.exists(settingsDir)) {
                Files.createDirectories(settingsDir);
            }

            JSONObject settingsJson = new JSONObject();
            settingsJson.put("volume", volume);
            settingsJson.put("background", background);
            settingsJson.put("playGothamSound", playGothamSound);
            settingsJson.put("showDeathMarkers", showDeathMarkers);
            settingsJson.put("playMainMenuStartup", playMainMenuStartup);
            settingsJson.put("playIllegalMoveSound", playIllegalMoveSound);

            try (FileWriter file = new FileWriter(SETTINGS_PATH)) {
                file.write(settingsJson.toString(4)); // Pretty print JSON
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showSettingsWindow() {
        JFrame frame = new JFrame("Settings");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1440, 810);
        frame.setLayout(new GridLayout(6, 1, 10, 10)); // Add spacing between rows
        frame.setIconImages(LogoManager.getLogos());
        frame.getContentPane().setBackground(new Color(45, 45, 45)); // Dark background

        // Common colors
        Color labelColor = Color.WHITE;
        Color panelColor = new Color(55, 55, 55);
        Color sliderTrackColor = new Color(100, 100, 100);
        Color sliderThumbColor = new Color(200, 200, 200);

        // Volume slider
        JPanel volumePanel = new JPanel();
        volumePanel.setBackground(panelColor);
        JLabel volumeLabel = new JLabel("Volume:");
        volumeLabel.setForeground(labelColor);
        JSlider volumeSlider = new JSlider(0, 100, volume);
        volumeSlider.setBackground(panelColor);
        volumeSlider.setForeground(labelColor);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setForeground(labelColor);
        volumeSlider.setBackground(panelColor);
        volumeSlider.setForeground(sliderThumbColor);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumePanel.add(volumeLabel);
        volumePanel.add(volumeSlider);
        frame.add(volumePanel);

        // Background selection
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBackground(panelColor);
        JLabel backgroundLabel = new JLabel("Background:");
        backgroundLabel.setForeground(labelColor);
        String[] backgrounds = {"Gray", "Warped Plane", "Ripple", "Spiral", "StarField", "Plasma"};
        JComboBox<String> backgroundDropdown = new JComboBox<>(backgrounds);
        backgroundDropdown.setSelectedItem(background);
        backgroundDropdown.setBackground(panelColor);
        backgroundDropdown.setForeground(labelColor);
        backgroundPanel.add(backgroundLabel);
        backgroundPanel.add(backgroundDropdown);
        frame.add(backgroundPanel);

        // Checkboxes
        JCheckBox deathMarkersCheck = new JCheckBox("Show Death Markers", showDeathMarkers);
        JCheckBox illegalMoveSoundCheck = new JCheckBox("Play Illegal Move Sound", playIllegalMoveSound);
        JCheckBox gothamSoundCheck = new JCheckBox("THE ROOK!!!", playGothamSound);
        JCheckBox playMainMenuCheck = new JCheckBox("Play Main Menu Animation on Startup", playMainMenuStartup);
        for (JCheckBox checkBox : new JCheckBox[]{deathMarkersCheck, illegalMoveSoundCheck, gothamSoundCheck, playMainMenuCheck}) {
            checkBox.setBackground(panelColor);
            checkBox.setForeground(labelColor);
            frame.add(checkBox);
        }

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(panelColor);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            volume = volumeSlider.getValue();
            playGothamSound = gothamSoundCheck.isSelected();
            showDeathMarkers = deathMarkersCheck.isSelected();
            playMainMenuStartup = playMainMenuCheck.isSelected();
            playIllegalMoveSound = illegalMoveSoundCheck.isSelected();
            background = (String) backgroundDropdown.getSelectedItem();
            saveSettings();
            frame.dispose();
        });

        JButton defaultButton = new JButton("Return to Default");
        defaultButton.setBackground(new Color(70, 70, 70));
        defaultButton.setForeground(Color.WHITE);
        defaultButton.addActionListener(e -> {
            setDefaultSettings();
            volumeSlider.setValue(volume);
            gothamSoundCheck.setSelected(playGothamSound);
            backgroundDropdown.setSelectedItem(background);
            deathMarkersCheck.setSelected(showDeathMarkers);
            playMainMenuCheck.setSelected(playMainMenuStartup);
            illegalMoveSoundCheck.setSelected(playIllegalMoveSound);
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(defaultButton);
        frame.add(buttonPanel);

        frame.setVisible(true);
    }

    public static int getVolume() { return volume; }
    public static String getBackground() { return background; }
    public static boolean THE_ROOK() { return playGothamSound; }
    public static boolean isShowDeathMarkers() { return showDeathMarkers; }
    public static boolean isPlayMainMenuStartup() {return playMainMenuStartup; }
    public static boolean isPlayIllegalMoveSound() { return playIllegalMoveSound; }
}