package lanse.worldwarchess.shared.core.worldmap.board.badboard;

import javax.swing.*;
import java.awt.*;

public class BadViewTest extends JPanel {

    //This code is more like an example of how to implement it elsewhere.

    private int frame = 0;
    private static final int PIXEL_SIZE = 12;

    public BadViewTest() {
        Timer timer = new Timer(0, e -> { // mess with the delay for slower or faster vid
            frame = (frame + 1) % BadAppleDecoder.frameCount();
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                g.setColor(BadAppleDecoder.getPixel(frame, x, y) ? Color.WHITE : Color.BLACK);
                g.fillRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bad");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new BadViewTest());
        frame.setSize(32 * PIXEL_SIZE + 16, 32 * PIXEL_SIZE + 39);
        frame.setVisible(true);
    }
}
