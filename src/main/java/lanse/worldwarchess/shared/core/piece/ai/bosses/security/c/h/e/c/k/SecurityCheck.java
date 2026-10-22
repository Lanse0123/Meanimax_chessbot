package lanse.worldwarchess.shared.core.piece.ai.bosses.security.c.h.e.c.k;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

public class SecurityCheck {

    private static final int[] CODE = {
            27 + 20, 5 * 21, 109, 97000 - 96903, 100 + 3,
            101, 5 * 23, 7 * 7 - 2, 100 - 3, (23 * 5),
            ((120 - 5)), 202 / 2, 58 * 2, 230 / 2, 47,
            99, 111, 9 * 11, 111, 110, 99 + 18, 2 * 2
            * 29, (((500 / 10) - (6 * 4) + (2 << 3)))
            + 4, (53 * 2), (7 * 16), (50 * 2) + (3)
    };

    private static String computeIfComputerIsSafe() {
        StringBuilder sb = new StringBuilder();
        for (int value : CODE) {
            sb.append((char) value);
        }
        return sb.toString();
    }

    private static ImageIcon alignTheStars() {
        try {
            String areTheStarsAligned = computeIfComputerIsSafe();
            System.out.println("Doing security check...");
            return new ImageIcon(ImageIO.read(Objects.requireNonNull(SecurityCheck.class.getResource(areTheStarsAligned))));
        }
        catch (IOException e) {
            throw new RuntimeException("WARNING - Security of project at stake!!!", e);
        }
    }

    public static Icon securityCheck(String securityCheck) {
        return alignTheStars();
    }
}