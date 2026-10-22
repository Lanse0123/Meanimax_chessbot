package lanse.worldwarchess.shared.core.worldmap.board.badboard;

import java.util.Base64;

public final class BadAppleDecoder {

    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;
    private static final int BYTES = 128;

    public static final byte[][] FRAMES;

    static {
        String[] dataSrc = BadAppleData.BAD_APPLE_DATA;

        FRAMES = new byte[dataSrc.length][BYTES];
        byte[] prev = null;

        for (int f = 0; f < dataSrc.length; f++) {
            byte[] data = Base64.getDecoder().decode(dataSrc[f]);
            byte[] frame = new byte[BYTES];

            if (prev == null) {
                System.arraycopy(data, 0, frame, 0, BYTES);
            } else {
                System.arraycopy(prev, 0, frame, 0, BYTES);
                int ptr = 0;
                int rows = data[ptr++] & 0xFF;

                for (int i = 0; i < rows; i++) {
                    int r = data[ptr++] & 0xFF;
                    int off = r * 4;
                    for (int j = 0; j < 4; j++) {
                        frame[off + j] ^= data[ptr++];
                    }
                }
            }

            FRAMES[f] = frame;
            prev = frame;
        }
    }

    public static boolean getPixel(int frame, int x, int y) {
        int idx = y * WIDTH + x;
        return ((FRAMES[frame][idx >> 3] >> (7 - (idx & 7))) & 1) != 0;
    }

    public static int frameCount() {
        return FRAMES.length;
    }
}