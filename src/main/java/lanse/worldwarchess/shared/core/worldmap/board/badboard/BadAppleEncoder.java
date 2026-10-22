package lanse.worldwarchess.shared.core.worldmap.board.badboard;

//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.nio.file.*;
//import java.util.Base64;
//import java.io.*;
//import java.util.List;

public class BadAppleEncoder {

    /** NO EXPLANATION IS NEEDED. ALL CODE HERE IS SELF EVIDENT AND EXTREMELY EASY TO READ. NEVER RUN THIS CODE EVER.
     * NEVER UNCOMMENT THIS CODE EVER. NEVER GIVE UP THE CODE TO THE PESKY GOBLINS. NEVER FEED THE CODE TO THE BIRDS.
     */

//    static final int SIZE = 32;
//    static final int BYTES = 128;
//
//    public static void main(String[] args) throws Exception {
//        Path dir = Paths.get("C:/Users/mrsde/OneDrive/Desktop/Frames/BadApple");
//
//        List<Path> frames = Files.list(dir)
//                .filter(p -> p.toString().endsWith(".png"))
//                .sorted()
//                .toList();
//
//        byte[] prev = null;
//
//        System.out.println("public static final String[] BAD_APPLE_DELTA = {");
//
//        for (Path p : frames) {
//            BufferedImage img = ImageIO.read(p.toFile());
//            byte[] curr = packBits(scale(img));
//
//            byte[] encoded = (prev == null)
//                    ? curr
//                    : encodeRowDelta(prev, curr);
//
//            String b64 = Base64.getEncoder().encodeToString(encoded);
//            System.out.println("    \"" + b64 + "\",");
//
//            prev = curr;
//        }
//
//        System.out.println("};");
//    }
//
//    static BufferedImage scale(BufferedImage src) {
//        BufferedImage dst = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = dst.createGraphics();
//        g.drawImage(src, 0, 0, SIZE, SIZE, null);
//        g.dispose();
//        return dst;
//    }
//
//    static byte[] packBits(BufferedImage img) {
//        byte[] out = new byte[BYTES];
//        int bit = 0;
//
//        for (int y = 0; y < SIZE; y++) {
//            for (int x = 0; x < SIZE; x++) {
//                int rgb = img.getRGB(x, y);
//                int g = (((rgb >> 16) & 255)
//                        + ((rgb >> 8) & 255)
//                        + (rgb & 255)) / 3;
//                if (g > 128) {
//                    out[bit >> 3] |= (byte) (1 << (7 - (bit & 7)));
//                }
//                bit++;
//            }
//        }
//        return out;
//    }
//
//    static byte[] encodeRowDelta(byte[] prev, byte[] curr) throws IOException {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        int changed = 0;
//        for (int r = 0; r < 32; r++) {
//            int off = r * 4;
//            for (int i = 0; i < 4; i++) {
//                if ((prev[off + i] ^ curr[off + i]) != 0) {
//                    changed++;
//                    break;
//                }
//            }
//        }
//
//        out.write(changed);
//
//        for (int r = 0; r < 32; r++) {
//            int off = r * 4;
//            boolean diff = false;
//
//            for (int i = 0; i < 4; i++) {
//                if ((prev[off + i] ^ curr[off + i]) != 0) {
//                    diff = true;
//                    break;
//                }
//            }
//
//            if (diff) {
//                out.write(r);
//                for (int i = 0; i < 4; i++) {
//                    out.write(prev[off + i] ^ curr[off + i]);
//                }
//            }
//        }
//        return out.toByteArray();
//    }
}
