package lanse.worldwarchess.shared.core.screens.history;

import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.marker.Marker;
import lanse.worldwarchess.shared.core.piece.move.MoveHistory;
import lanse.worldwarchess.shared.core.screens.background.Background;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

public class HistoryPanel extends GamePanel implements KeyListener{

    //This class is the main screen class responsible for calling all draw logic for the game,
    // and responsible for mouse listeners like zooming in and out, and moving left and right.
    // This extends Gamepanel, so HistoryPanel also is responsible for going forward and backwards in time
    // to let a user view one of their games.

    private int moveCount = 0;

    public HistoryPanel() {
        super();

        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        MoveHistory.shouldRecordHistory = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2 + cameraX + 2500;
        int centerY = height / 2 + cameraY;

        Background.draw(width, height, centerX, centerY, g2d);

        g2d.translate(cameraX, cameraY);

        WorldMap.board.draw(g2d, zoomFactor);

        Marker.drawAllMarkers(zoomFactor, g2d);

        for (Piece piece : WorldMap.pieces){
            piece.draw(g2d, zoomFactor);
        }

        // Undo camera translation for UI drawing.
        g2d.translate(-cameraX, -cameraY);
    }




    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                int a; //to get rid of annoying yellow lines
                //TODO - go back 1 move. (currently impossible in a normal manner of time)

            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                //TODO - read next move data and apply it.

            }
        }
        repaint();
    }
    @Override
    public void keyReleased(KeyEvent keyEvent) {}
    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    //TODO - this should just be gamePanel logic without letting the user interact with pieces.
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    // pan the camera.
    @Override
    public void mouseDragged(MouseEvent e) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int edgeBuffer = 50;
        int scrollSpeed = 10;
        currentDragPoint.x = e.getX() - cameraX;
        currentDragPoint.y = e.getY() - cameraY;
        if (e.getX() < edgeBuffer) {
            cameraX += scrollSpeed;
        } else if (e.getX() > screenWidth - edgeBuffer) {
            cameraX -= scrollSpeed;
        }
        if (e.getY() < edgeBuffer) {
            cameraY += scrollSpeed;
        } else if (e.getY() > screenHeight - edgeBuffer) {
            cameraY -= scrollSpeed;
        }
        repaint();

    }

    @Override
    public void mouseReleased(MouseEvent e) {}
}
