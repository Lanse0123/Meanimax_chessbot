package lanse.worldwarchess.shared.core.thread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedHashMap;
import java.util.Map;

/** Written by Lanse and James, taken from Bonk Universe
 * <br><br>
 * This class is responsible for showcasing the data gathered in the FlameGraph after it is done testing for lag across the
 * project. Areas that are wider are laggier, with a better explanation in FlameGraphLagTester. **/
public class FlameGraphPanel extends JPanel implements MouseListener, KeyListener {

    private static final double MIN_ZOOM = 0.125;
    private static final double MAX_ZOOM = 1024.0;
    private static final int BAR_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 0;
    private static double panX = 0;
    private static double panY = 0;
    private static double zoom = 1.0;
    private final java.util.List<FlameBox> flameBoxes = new java.util.ArrayList<>();
    private final Node root = new Node("root");
    private Map<String, Boolean> threads;

    public static void createFlameGraphWindow(Map<String, Integer> samples) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Flame Graph");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1200, 800);

            FlameGraphPanel panel = new FlameGraphPanel(samples);

            frame.add(panel);
            frame.setVisible(true);

            SwingUtilities.invokeLater(panel::requestFocusInWindow);
        });

    }

    public FlameGraphPanel(Map<String, Integer> samples) {
        buildTree(samples);

        setToolTipText("");
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
    }

    private void buildTree(Map<String, Integer> samples) {
        for (Map.Entry<String, Integer> entry : samples.entrySet()) {
            String[] stack = entry.getKey().split(";");
            int count = entry.getValue();

            Node current = root;
            for (String frame : stack) {
                current = current.children.computeIfAbsent(frame, Node::new);
                current.value += count;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (root.children.isEmpty()) return;

        flameBoxes.clear();

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

//        drawThreadButtons(g2, 0, 0, BUTTON_WIDTH, getHeight());

        g2.translate(panX, panY);
        g2.scale(zoom, zoom);

        drawNode(g2, root, BUTTON_WIDTH, getHeight() - BAR_HEIGHT, getWidth() - BUTTON_WIDTH, BAR_HEIGHT);

        g2.dispose();
    }

//    private void drawThreadButtons(Graphics2D g, int x, int y, int width, int height) {
//        g.setColor(Color.BLACK);
//        g.fillRect(x, y, width, height);
//
//        g.setColor(Color.RED);
//        g.drawRect(x + 10, y + 10, BUTTON_WIDTH - 20, 100);
//    }

    private void drawNode(Graphics2D g, Node node, int x, int y, int width, int height) {
        if (width <= 1 || height <= 0) return;
        if (node.children.isEmpty()) return;

        int total = node.children.values().stream().mapToInt(n -> n.value).sum();
        if (total <= 0) return;

        int offsetX = x;

        for (Node child : node.children.values()) {

            int w = (int) ((child.value / (double) total) * width);

            if (w <= 1) continue;

            // draw flame box
            g.setColor(colorFor(child.name));
            g.fillRect(offsetX, y, w, height);

            g.setColor(Color.BLACK);
            g.drawRect(offsetX, y, w, height);

            flameBoxes.add(new FlameBox(new Rectangle(offsetX, y, w, height), child));

            // draw text
            if (w > 25) {

                double pct = child.value * 100.0 / total;
                String text = child.name + String.format(" (%.1f%%)", pct);

                Font originalFont = g.getFont();

                float fontSize = (float) (12.0 / zoom);

                // clamp size
                fontSize = Math.clamp(fontSize, 4f, 12f);

                Font scaledFont = originalFont.deriveFont(fontSize);

                g.setFont(scaledFont);

                FontMetrics fm = g.getFontMetrics();

                int maxTextWidth = (int) (w * zoom) - 10;

                if (maxTextWidth > 10) {

                    String ellipsis = ">";

                    if (fm.stringWidth(text) > maxTextWidth) {

                        int startIndex = 0;

                        while (startIndex < text.length()) {

                            String candidate =
                                    ellipsis + text.substring(startIndex);

                            if (fm.stringWidth(candidate) <= maxTextWidth) {
                                text = candidate;
                                break;
                            }

                            startIndex++;
                        }
                    }

                    Shape oldClip = g.getClip();

                    g.setClip(offsetX, y, w, height);

                    g.drawString(text, offsetX + 4, y + height - 6);

                    g.setClip(oldClip);
                }

                g.setFont(originalFont);
            }

            drawNode(g, child, offsetX, y - height, w, height);

            offsetX += w;
        }
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int worldX = (int) ((e.getX() - panX) / zoom);
        int worldY = (int) ((e.getY() - panY) / zoom);

        for (int i = flameBoxes.size() - 1; i >= 0; i--) {
            FlameBox box = flameBoxes.get(i);
            if (box.rect.contains(worldX, worldY)) {
                return box.node.name + " (" + box.node.value + ")";
            }
        }

        return null;
    }

    private Color colorFor(String name) {
        int hash = name.hashCode();
        float baseHue = 0.02f + (Math.abs(hash) % 100) / 100f * 0.10f;
        float saturation = 0.7f + (Math.abs(hash >> 8) % 30) / 100f;
        float brightness = 0.8f + (Math.abs(hash >> 16) % 20) / 100f;
        return Color.getHSBColor(baseHue, saturation, brightness);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
        if (SwingUtilities.isLeftMouseButton(e)) {
            zoomIn(e.getX(), e.getY());
        } else if (SwingUtilities.isRightMouseButton(e)) {
            zoomOut(e.getX(), e.getY());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> moveLeft();
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> moveRight();
            case KeyEvent.VK_W, KeyEvent.VK_UP -> moveUp();
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> moveDown();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private static class Node {
        String name;
        int value = 0;
        Map<String, Node> children = new LinkedHashMap<>();

        Node(String name) {
            this.name = name;
        }
    }

    private void moveLeft() {
        panX += (50 / zoom);
        repaint();
    }

    private void moveRight() {
        panX -= (50 / zoom);
        repaint();
    }

    private void moveUp() {
        panY += (50 / zoom);
        repaint();
    }

    private void moveDown() {
        panY -= (50 / zoom);
        repaint();
    }

    private void zoomIn(int x, int y) {
        zoomAt(x, y, 2.0);
    }

    private void zoomOut(int x, int y) {
        zoomAt(x, y, 0.5);
    }

    private void zoomAt(int x, int y, double factor) {
        double oldZoom = zoom;
        double newZoom = Math.clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);

        if (newZoom == oldZoom) {
            return;
        }

        zoom = newZoom;
        double scale = zoom / oldZoom;

        panX = x - ((x - panX) * scale);
        panY = y - ((y - panY) * scale);

        repaint();
    }

    private static class FlameBox {

        Rectangle rect;
        Node node;

        FlameBox(Rectangle rect, Node node) {
            this.rect = rect;
            this.node = node;
        }
    }
}
