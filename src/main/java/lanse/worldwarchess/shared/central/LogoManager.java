package lanse.worldwarchess.shared.central;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

public class LogoManager {

    private static ImageIcon getIcon(String resourcePath) {
        URL resource = LogoManager.class.getResource(resourcePath);

        if (resource == null) {
            throw new IllegalStateException("Image resource not found: " + resourcePath + ". Check its location in the resources directory.");
        }

        try {
            return new ImageIcon(ImageIO.read(resource));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load image resource: " + resourcePath, e);
        }
    }

    public static List<Image> getLogos() {
        return Stream.of("WWCIcon.png", "WWCIconSmall.png").map(name -> getIcon("/images/icon/" + name).getImage()).toList();
    }
}
