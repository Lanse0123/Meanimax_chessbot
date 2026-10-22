package lanse.worldwarchess.shared.central;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LogoManager {

    private static ImageIcon getIcon(String resource_path) {
        try {
            return new ImageIcon(ImageIO.read(Objects.requireNonNull(LogoManager.class.getResource(resource_path))));
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load image resource", e);
        }
    }

    public static List<Image> getLogos() {
        return Stream.of("WWCIcon.png", "WWCIconSmall.png").map((name) -> getIcon("/images/icon/" + name).getImage()).toList();
    }

}
