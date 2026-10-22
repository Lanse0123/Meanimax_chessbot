package lanse.worldwarchess.shared.core.screens.ingame;

import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.PawnPromotionManager;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.ai.enemies.NpcPiece;
import lanse.worldwarchess.shared.core.piece.marker.Marker;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDrawer;
import lanse.worldwarchess.shared.core.piece.move.MoveExecutor;
import lanse.worldwarchess.shared.core.screens.background.Background;
import lanse.worldwarchess.shared.core.sound.SoundManager;
import lanse.worldwarchess.shared.core.worldmap.Utils;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Optional;

public class GamePanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, KeyListener {

    //This class is the main screen class responsible for calling all draw logic for the game,
    // and responsible for mouse listeners like zooming in and out, and moving left and right.
    public static String winningTeam;
    protected double zoomFactor = 1.0;
    public static int cameraX = 0;
    public static int cameraY = 0; // Camera offset for panning
    protected int lastMouseX, lastMouseY; // For camera drag tracking
    protected Piece selectedPiece = null;
    protected Board selectedBoard = null;
    protected List<LegalMoveDetector.Coordinate> legalMoves = List.of();
    protected boolean pieceDragging = false;
    protected final Point dragOffset = new Point(0, 0);
    protected Point currentDragPoint = new Point(0, 0);
    protected int originalLogicalX, originalLogicalY;
    public static SidebarEvaluator sidebarEvaluator;
    public SidebarMenu sidebarMenu;

    public GamePanel() {
        setLayout(new BorderLayout());

        WorldMap.initializeBoards();
        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        setFocusable(true);
        requestFocus();
        requestFocusInWindow();

        sidebarEvaluator = new SidebarEvaluator();
        add(sidebarEvaluator, BorderLayout.WEST);

        sidebarMenu = new SidebarMenu();
        add(sidebarMenu, BorderLayout.EAST);
    }

    public void recalculateLegalMoves() {
        if (selectedPiece != null) {
            legalMoves = LegalMoveDetector.getLegalMoves(selectedPiece);
        }
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

        // Highlight all legal moves
        LegalMoveDrawer.draw(pieceDragging, selectedPiece, legalMoves, g2d, zoomFactor);

        // Draw all pieces on top of the legal move indicators
        for (Piece piece : WorldMap.pieces){
            if (piece != selectedPiece) piece.draw(g2d, zoomFactor);
        }

        // Draw the dragged piece on top
        if (selectedPiece != null) {
            selectedPiece.drawSelectedPiece(pieceDragging, selectedBoard, currentDragPoint, dragOffset, legalMoves, zoomFactor, g2d);
        }

        PawnPromotionManager.getInstance().drawPromotions(g2d, zoomFactor);

        // Undo camera translation for UI drawing.
        g2d.translate(-cameraX, -cameraY);

        Timer.draw(g, width, height, sidebarMenu);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double maxZoomOut = Math.max((double) 1 / 10, 0.001);

        double zoomMultiplier = 1.1;
        int rotation = e.getWheelRotation();
        int mouseX = e.getX();
        int mouseY = e.getY();

        double oldZoom = zoomFactor;
        double newZoom = (rotation < 0) ? oldZoom * zoomMultiplier : oldZoom / zoomMultiplier;

        newZoom = Math.clamp(newZoom, maxZoomOut, 1);

        double zoomRatio = newZoom / oldZoom;

        cameraX = (int)((mouseX - cameraX) * (1 - zoomRatio) + cameraX);
        cameraY = (int)((mouseY - cameraY) * (1 - zoomRatio) + cameraY);
        zoomFactor = newZoom;
        repaint();
    }


    // If left mouse button pressed, check if a piece is clicked.
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            requestFocus();
            requestFocusInWindow();

            int worldX = e.getX() - cameraX;
            int worldY = e.getY() - cameraY;

            Board board = WorldMap.board;
            Rectangle region = getBoardRegion(board, zoomFactor);
            if (region.contains(worldX, worldY)) {
                int baseTile = board.getBaseTileSize();
                int tileSize = (int) (baseTile * zoomFactor);

                int pieceLogicalX = (worldX - region.x) / tileSize;
                int pieceLogicalY = (worldY - region.y) / tileSize;

                // Get the nearest piece within snapping range of the click
                Optional<Piece> opt_piece = Utils.nearestPiece(new LegalMoveDetector.Coordinate(pieceLogicalX, pieceLogicalY, board.getBoardID()), WorldMap.getAllPiecesOfBoard(board));
                if (opt_piece.isPresent()) {
                    selectPiece(opt_piece.get(), board, worldX, worldY);
                    return;
                }
            }
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }


    // If dragging a piece, update its drag position; otherwise, pan the camera.
    @Override
    public void mouseDragged(MouseEvent e) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int edgeBuffer = 50;
        int scrollSpeed = 10;
        if (pieceDragging && selectedPiece != null) {
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
        } else {
            int dx = e.getX() - lastMouseX;
            int dy = e.getY() - lastMouseY;
            cameraX += dx;
            cameraY += dy;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            repaint();
        }
    }

    // On mouse release, drop the piece or process promotion selection.
    @Override
    public void mouseReleased(MouseEvent e) {
        Point worldClick = new Point(e.getX() - cameraX, e.getY() - cameraY);

        // promotion menu click
        if (PawnPromotionManager.getInstance().processClick(worldClick)) {
            SoundManager.promote();
            repaint();
            return;
        }

        if (!pieceDragging || selectedPiece == null || selectedBoard == null) return;

        // find clicked board and the logical coordinates of where on it
        LegalMoveDetector.Coordinate logicalCoords = Utils.pxToLogicalCoords(new Point(e.getX() - cameraX, e.getY() - cameraY), selectedPiece, zoomFactor);
        Board newBoard = WorldMap.getBoardByID(logicalCoords.boardID());

        boolean sameSquare = (logicalCoords.x() == originalLogicalX && logicalCoords.y() == originalLogicalY && newBoard == selectedPiece.getBoard());
        boolean invalidMoveBase = newBoard == null || sameSquare ||
                Timer.playerIDMovedList.contains(selectedPiece.getPlayerID()) ||
                !selectedPiece.getTeam().equals((Timer.turnCounter % 2 == 0) ? "White" : "Black") ||
                (selectedPiece instanceof NpcPiece);

        if (invalidMoveBase) {
            resetDrag();
            legalMoves.clear();
            return;
        }

        // check if move is legal
        Optional<LegalMoveDetector.Coordinate> move = Utils.nearestLegalCoordinate(logicalCoords, legalMoves);
        legalMoves.clear();

        if (move.isPresent()) {
            LegalMoveDetector.Coordinate valid_move = move.get();
            // EXECUTE ORDER 66 or wait no I mean EXECUTE MOVE!!
            MoveExecutor.executeMove(selectedPiece, newBoard, valid_move.x(), valid_move.y(), originalLogicalX, originalLogicalY,
                    true, "move", true, false);
        }
        // handle illegal move
        else {
            selectedPiece.setBoardPosition(originalLogicalX, originalLogicalY);
            SoundManager.invalidMove();
        }

        resetDrag();
        repaint();
    }

    private void resetDrag() {
        pieceDragging = false;
        selectedPiece = null;
        selectedBoard = null;
    }

    //  region in zoomed (world) coordinates.
    public static Rectangle getBoardRegion(Board board, double zoomFactor) {
        int baseTile = board.getBaseTileSize();
        int boardSize = board.getBoardSize();
        int x = (int)(board.getBoardX() * zoomFactor);
        int y = (int)(board.getBoardY() * zoomFactor);
        int width = (int)(boardSize * baseTile * zoomFactor);
        int height = (int)(boardSize * baseTile * zoomFactor);
        return new Rectangle(x, y, width, height);
    }

    private void selectPiece(Piece piece, Board board, int worldX, int worldY) {
        selectedPiece = piece;
        selectedBoard = board;
        recalculateLegalMoves();
        pieceDragging = true;
        originalLogicalX = piece.getPosX();
        originalLogicalY = piece.getPosY();

        currentDragPoint = new Point(worldX, worldY);
        dragOffset.x = worldX - (int)(piece.getPosX() * zoomFactor);
        dragOffset.y = worldY - (int)(piece.getPosY() * zoomFactor);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ESCAPE){
            sidebarMenu.toggleMenu();
        }

        repaint();
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyReleased(KeyEvent keyEvent) {}
    @Override public void keyTyped(KeyEvent keyEvent) {}
}