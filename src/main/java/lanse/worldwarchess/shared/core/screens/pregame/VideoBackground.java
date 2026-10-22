package lanse.worldwarchess.shared.core.screens.pregame;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;

public class VideoBackground {

    //This class works with MainMenu. It has the video animation player, and the button menu.

    private final JFXPanel fxPanel;

    private MediaPlayer startupPlayer;
    private MediaPlayer loopPlayer;
    private MediaView mediaView;

    public VideoBackground(JFXPanel fxPanel) {
        this.fxPanel = fxPanel;
    }

    @SuppressWarnings("all")
    public void reset(boolean firstLaunch, StackPane root) throws MediaException {
        //TODO - THIS SOLUTION IS INCOMPLETE. I ACCIDENTALLY FIXED THE RETURN TO MAIN MENU SCREEN BUG THAT HAS
        // BEEN TERRORIZING MY MIND. THIS SHOULD NOT WORK, BUT IT DOES, AND IDK WHY... THIS IS TRY CATCH HELL!
        try {
            if (startupPlayer != null) {
                startupPlayer.setOnEndOfMedia(null);
                startupPlayer.setOnPlaying(null);
                try {
                    startupPlayer.stop();
                } catch (Throwable ignored) {
                }
                try {
                    startupPlayer.dispose();
                } catch (Throwable ignored) {
                }
                startupPlayer = null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            startupPlayer = null;
        }
        try {
            if (loopPlayer != null) {
                loopPlayer.setOnEndOfMedia(null);
                loopPlayer.setOnPlaying(null);
                try {
                    loopPlayer.stop();
                } catch (Throwable ignored) {
                }
                try {
                    loopPlayer.dispose();
                } catch (Throwable ignored) {
                }
                loopPlayer = null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            loopPlayer = null;
        }
        URL startupUrl = Objects.requireNonNull(getClass().getResource("/animations/startup.mp4"));
        URL loopUrl = Objects.requireNonNull(getClass().getResource("/animations/loop normal.mp4"));

        Media startupMedia = new Media(startupUrl.toExternalForm());
        Media loopMedia = new Media(loopUrl.toExternalForm());

        startupPlayer = new MediaPlayer(startupMedia);
        loopPlayer = new MediaPlayer(loopMedia);

        mediaView = new MediaView(firstLaunch ? startupPlayer : loopPlayer);
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);
        root.getChildren().add(mediaView);

    }

    @SuppressWarnings("all")
    public void startAnimations(boolean firstLaunch, StackPane root, VBox mainMenu) {
        Platform.runLater(() -> {
            try {
                if (root.getScene() == null) {
                    Scene scene = new Scene(root, Color.BLACK);
                    fxPanel.setScene(scene);
                }

                mediaView.fitWidthProperty().bind(root.widthProperty());
                mediaView.fitHeightProperty().bind(root.heightProperty());

                if (firstLaunch) {
                    FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), mainMenu);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);

                    startupPlayer.setOnPlaying(() -> {
                        PauseTransition delay = new PauseTransition(Duration.seconds(5.09));
                        delay.setOnFinished(ev -> {
                            fadeIn.play();
                            mainMenu.setDisable(false);
                        });
                        delay.play();
                    });
                    startupPlayer.setOnEndOfMedia(() -> {
                        try {
                            loopPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                            mediaView.setMediaPlayer(loopPlayer);
                            loopPlayer.play();
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        } finally {
                            try {
                                startupPlayer.setOnEndOfMedia(null);
                                startupPlayer.setOnPlaying(null);
                                try {
                                    startupPlayer.stop();
                                } catch (Throwable ignored) {
                                }
                                try {
                                    startupPlayer.dispose();
                                } catch (Throwable ignored) {
                                }
                            } catch (Throwable ignored) {
                            }
                            startupPlayer = null;
                        }
                    });
                    startupPlayer.play();
                } else {
                    mainMenu.setOpacity(1);
                    mainMenu.setDisable(false);
                    loopPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    loopPlayer.play();
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                mainMenu.setOpacity(1);
                mainMenu.setDisable(false);
                root.setBackground(new Background(new BackgroundImage(
                        new Image(MainMenu.class.getResource("/images/background/background.png").toString()),
                        null, null, null,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                                true, true, true, true)
                )));
            }
        });
    }
}
