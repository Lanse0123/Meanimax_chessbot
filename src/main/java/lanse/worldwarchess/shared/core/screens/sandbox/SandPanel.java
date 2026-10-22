package lanse.worldwarchess.shared.core.screens.sandbox;

import javafx.util.Pair;
import lanse.worldwarchess.shared.central.TextureKey;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.piece.PieceType;
import lanse.worldwarchess.shared.core.piece.move.LegalMoveDetector;
import lanse.worldwarchess.shared.core.screens.background.Background;
import lanse.worldwarchess.shared.core.screens.ingame.GamePanel;
import lanse.worldwarchess.shared.core.worldmap.MapShapePresets;
import lanse.worldwarchess.shared.core.worldmap.Utils;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class SandPanel extends GamePanel {

    //This class is the main screen class responsible for calling all draw logic for the game,
    // and responsible for mouse listeners like zooming in and out, and moving left and right.
    // This extends Gamepanel, so SandPanel also is responsible for making all sandbox logic
    // so players can make their own maps or board presets.

    protected boolean sandboxDragMode = false;
    private boolean showPieceEditor = false;

    private final int ARC_SIZE = 10;
    private final int ICON_SPACING = 8;
    private final int PANEL_PADDING = 10;
    private final int PANEL_CORNER = 12;
    private final int TEXT_PADDING = 10;


    //TODO - move the piece editor to a new class please, this is getting disgusting, and like outside this file
    ///////////////////////////////////////////////////////////////
    // UI state for the piece editor
    private final java.util.List<Rectangle> editorIconBounds = new ArrayList<>();
    private Rectangle editorConfirmBounds = null;
    //TODO - make scalable...
    private Rectangle editorTeamToggleWhiteBounds = null;
    private Rectangle editorTeamToggleBlackBounds = null;
    private Rectangle editorTeamToggleNPCBounds = null;

    private int editorChosenTypeIdx = -1; // index into typeList (the list returned by PieceType.getAvaiableTypeList)
    private int editorChosenTeamIdx = 0; // 0 = White, 1 = Black, 2 = NPC. Probably should be better noted than a comment.
    ///////////////////////////////////////////////////////////////

    public SandPanel() {
        super();

        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

//        new SandboxMenu(this);

        add(new SandboxMenu(this, true), BorderLayout.NORTH);

        new Board(0, 0, MapShapePresets.TILE_SIZE, "Border", 1000, 1001, 1000, false, true, true);

        sidebarEvaluator.updateEvaluation();

        SandboxMapDataStack.pushState();
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

        for (Board board : WorldMap.boards.values()) {
            if (board != selectedBoard) {
                board.draw(g2d, zoomFactor);
            }
        }

        for (Piece piece : WorldMap.pieces) {
            if (piece != selectedPiece && (piece.getBoard() != selectedBoard || !pieceDragging)) piece.draw(g2d, zoomFactor);
        }

        if (selectedBoard != null) {
            int origX = selectedBoard.getBoardX();
            int origY = selectedBoard.getBoardY();
            if (pieceDragging) {
                selectedBoard.setMapPos(currentDragPoint.x - dragOffset.x, currentDragPoint.y - dragOffset.y);
            }
            selectedBoard.drawSelectedBoard(g2d, zoomFactor);
            for (Piece piece : WorldMap.getAllPiecesOfBoard(selectedBoard)) {
                piece.draw(g2d, zoomFactor);
            }
            selectedBoard.setMapPos(origX, origY);
        }

        // Draws 0,0
        g2d.setColor(Color.RED);
        g2d.fillOval(0, 0, 16, 16);

        // Draw the dragged piece on top
        if (selectedPiece != null) {
            selectedPiece.drawSelectedPiece(pieceDragging, selectedBoard, currentDragPoint, dragOffset, legalMoves, zoomFactor, g2d);
        }

        // Finally, draw the piece editor if it is open
        if (showPieceEditor && selectedPiece != null) {
            Font oldFont = g2d.getFont();
            g2d.setFont(g2d.getFont().deriveFont(20f));
            drawPieceEditor(g2d, zoomFactor);
            g2d.setFont(oldFont);
        }

        // Undo camera translation for UI drawing.
        g2d.translate(-cameraX, -cameraY);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;

        //brought from GamePanel bc it looks looks like pawn promotion stuff, and this is similar
        Point worldClick = new Point(e.getX() - cameraX, e.getY() - cameraY);

        if (showPieceEditor && selectedPiece != null) {
            if (processPieceEditorClick(worldClick)) {
                repaint();
                return; // consumed the click
            }
        }

        lastMouseX = e.getX();
        lastMouseY = e.getY();
        int worldX = e.getX() - cameraX;
        int worldY = e.getY() - cameraY;

        // Try piece selection first
        for (Board board : WorldMap.boards.values()) {
            Rectangle region = getBoardRegion(board, zoomFactor);
            if (!region.contains(worldX, worldY)) continue;

            int tileSize = (int) (board.getBaseTileSize() * zoomFactor);
            int logicalX = (worldX - region.x) / tileSize;
            int logicalY = (worldY - region.y) / tileSize;

            Optional<Piece> hit = Utils.nearestPiece(
                    new LegalMoveDetector.Coordinate(logicalX, logicalY, board.getBoardID()),
                    WorldMap.getAllPiecesOfBoard(board)
            );

            if (hit.isPresent()) {
                Piece piece = hit.get();

                // Clicking same piece again = deselect
                if (piece == selectedPiece && pieceDragging) {
                    selectedPiece = null;
                    pieceDragging = false;
                    return;
                }
                selectPiece(piece, worldX, worldY);
            }

            else {
            // No piece hit = board click
                if (selectedPiece != null) {
                    // Clicking board while piece selected = place piece
//                    selectedPiece.setMapPosition((int) (worldX / zoomFactor), (int) (worldY / zoomFactor));
                    selectedPiece.setMapPosition(region.x, region.y);
                    selectedPiece.setBoard(board);
                    selectedPiece.setBoardPosition(logicalX, logicalY);
                    selectedPiece = null;
                    pieceDragging = false;
                    SandboxMapDataStack.pushState();
                } else {
                    if (selectedBoard == board) {
                        clearSelection();
                    } else {
                        selectBoard(board, worldX, worldY);
                    }

                }
            }
            repaint();
            return;
        }

        // Clicked empty space
        clearSelection();
        repaint();
    }

    //TODO - something aint right with this...
    @Override
    public void mouseDragged(MouseEvent e) {
        if (pieceDragging) {
            if (selectedPiece != null || selectedBoard != null) {
                int worldX = e.getX() - cameraX;
                int worldY = e.getY() - cameraY;
                currentDragPoint = new Point((int) (worldX / zoomFactor), (int) (worldY / zoomFactor));
            } else {
                System.err.println("pieceDragging is true, but nothing is selected!");
            }
        }
        else {
            cameraX += e.getX() - lastMouseX;
            cameraY += e.getY() - lastMouseY;
        }
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int worldX = e.getX() - cameraX;
        int worldY = e.getY() - cameraY;

        if (pieceDragging) {
            if (selectedPiece != null) {
                for (Board board : WorldMap.boards.values()) {
                    Rectangle region = getBoardRegion(board, zoomFactor);
                    if (!region.contains(worldX, worldY)) continue;

                    int tileSize = (int) (board.getBaseTileSize() * zoomFactor);
                    int logicalX = (worldX - region.x) / tileSize;
                    int logicalY = (worldY - region.y) / tileSize;

                    Optional<Piece> hit = Utils.nearestPiece(
                            new LegalMoveDetector.Coordinate(logicalX, logicalY, board.getBoardID()),
                            WorldMap.getAllPiecesOfBoard(board)
                    );

                    if (hit.isPresent()) {
                        pieceDragging = false;
                        selectedPiece = null;
                        repaint();
                        return;
                    } else {
                        selectedPiece.setBoard(board);
                        selectedPiece.setMapPosition(region.x, region.y);
                        selectedPiece.setBoardPosition(logicalX, logicalY);
                        selectedPiece = null;
                        pieceDragging = false;
                        SandboxMapDataStack.pushState();
                        repaint();
                        return;
                    }
                }
            } else if (selectedBoard != null && sandboxDragMode) {
                int newBoardX = (int) ((worldX - dragOffset.x) * zoomFactor);
                int newBoardY = (int) ((worldY - dragOffset.y) * zoomFactor);

                Pair<Integer, Integer> snappedCoords = Utils.snapBoard((int) (worldX / zoomFactor), (int) (worldY / zoomFactor), selectedBoard, zoomFactor);

                newBoardX = snappedCoords.getKey();
                newBoardY = snappedCoords.getValue();

                selectedBoard.setMapPos(newBoardX, newBoardY);

                for (Piece piece : WorldMap.getAllPiecesOfBoard(selectedBoard)) {
                    piece.setMapPosition(newBoardX, newBoardY);
                }
                selectedBoard = null;
                pieceDragging = false;
                SandboxMapDataStack.pushState();
            }
            else {
                System.err.println("pieceDragging is true, but nothing is selected!");
            }
        }
    }

    // chat is disabled in sandbox
    @Override
    public void keyPressed(KeyEvent e) {
    }

    protected void selectPiece(Piece piece, int worldX, int worldY) {
        // Clear board selection explicitly
        selectedBoard = null;

        selectedPiece = piece;
        recalculateLegalMoves();

        if (sandboxDragMode) {
            pieceDragging = true;
            originalLogicalX = piece.getPosX();
            originalLogicalY = piece.getPosY();

            currentDragPoint = new Point(worldX, worldY);
            dragOffset.x = worldX - piece.getBoard().getBoardX();
            dragOffset.y = worldY - piece.getBoard().getBoardY();
        }
        else {
            currentDragPoint = new Point(piece.getBoardX(), piece.getBoardY());
            dragOffset.x = 0;
            dragOffset.y = 0;
        }
    }

    private void selectBoard(Board board, int worldX, int worldY) {
        clearSelection();

        selectedBoard = board;

        if (sandboxDragMode) {
            pieceDragging = true; // reuse same drag flag

            currentDragPoint = new Point((int) (worldX / zoomFactor), (int) (worldY / zoomFactor));
            dragOffset.x = (int) (worldX / zoomFactor - board.getBoardX());
            dragOffset.y = (int) (worldY / zoomFactor - board.getBoardY());
        }
        else {
            dragOffset.x = 0;
            dragOffset.y = 0;
        }
    }

    private void clearSelection() {
        selectedPiece = null;
        selectedBoard = null;
        legalMoves = java.util.List.of();
    }

    public void toggleDragMode() {
        this.sandboxDragMode = !this.sandboxDragMode;
    }

    public void showPieceEditor() {
        if (selectedPiece == null) return;

        this.showPieceEditor = true;

        String team = selectedPiece.getTeam() == null ? "White" : selectedPiece.getTeam();
        // :v
        if (team.equalsIgnoreCase("White")){
            editorChosenTeamIdx = 0;
        } else if (team.equalsIgnoreCase("Black")){
            editorChosenTeamIdx = 1;
        } else {
            editorChosenTeamIdx = 2;
        }

        java.util.List<PieceType> typeList = PieceType.getAvaiableTypeList(selectedPiece);
        editorChosenTypeIdx = 0;
        for (int i = 0; i < typeList.size(); i++) {
            if (typeList.get(i).getDisplayName().equalsIgnoreCase(selectedPiece.getType().getDisplayName())) {
                editorChosenTypeIdx = i;
                break;
            }
        }
        repaint();
    }

    private boolean processPieceEditorClick(Point click) {
        //TODO - EWWWWWWW HOW DID I END UP WITH THIS

        // team toggles
        if (editorTeamToggleWhiteBounds != null && editorTeamToggleWhiteBounds.contains(click)) {
            editorChosenTeamIdx = 0;
            repaint();
            return true;
        }
        if (editorTeamToggleBlackBounds != null && editorTeamToggleBlackBounds.contains(click)) {
            editorChosenTeamIdx = 1;
            repaint();
            return true;
        }
        if (editorTeamToggleNPCBounds != null && editorTeamToggleNPCBounds.contains(click)) {
            editorChosenTeamIdx = 2;
            repaint();
            return true;
        }

        for (int i = 0; i < editorIconBounds.size(); ++i) {
            if (editorIconBounds.get(i).contains(click)) {
                editorChosenTypeIdx = i;
                repaint();
                return true;
            }
        }

        // confirm button
        //TODO - might want to move this to a new function to make it easier to find
        if (editorConfirmBounds != null && editorConfirmBounds.contains(click)) {
            if (selectedPiece == null) return false;

            java.util.List<PieceType> typeList = PieceType.getAvaiableTypeList(selectedPiece);
            if (typeList == null || editorChosenTypeIdx < 0 || editorChosenTypeIdx >= typeList.size()) return false;

            PieceType chosenType = typeList.get(editorChosenTypeIdx);
            String chosenTeam; //TODO - move this ugly if else chain somewhere else because it shows up multiple times.
            if (editorChosenTeamIdx == 0){
                chosenTeam = "White";
            } else if (editorChosenTeamIdx == 1){
                chosenTeam = "Black";
            } else {
                chosenTeam = "NPC";
            }

            selectedPiece.setSandboxInfo(chosenType, chosenTeam);

            showPieceEditor = false;

            SandboxMapDataStack.pushState();
            GamePanel.sidebarEvaluator.updateEvaluation();
            repaint();
            return true;
        }

        return false;
    }

    private void drawPieceEditor(Graphics2D g2d, double zoomFactor) {
        //TODO - text is being scaled wrong bc of zoomfactor, it shouldnt be
        if (selectedPiece == null) return;

        java.util.List<PieceType> typeList = PieceType.getAvaiableTypeList(selectedPiece);
        if (typeList == null || typeList.isEmpty()) return;

        String teamPrefix;
        switch (editorChosenTeamIdx) {
            case 1 -> teamPrefix = "b";
            case 2 -> teamPrefix = "w"; //NPC defaults to white icons for now. I might make standard NPC eventually.
            default -> teamPrefix = "w";
        }

        int tileSize = selectedPiece.getBoard().getBaseTileSize();
        int centerX = selectedPiece.getBoardX() + (int) (selectedPiece.getPosX() * tileSize * zoomFactor)
                + (int) (selectedPiece.getPieceSize() * selectedPiece.getBoard().getBaseTileSize() * zoomFactor / 2);
        int anchorY = selectedPiece.getBoardY() + (int) (selectedPiece.getPosY() * tileSize * zoomFactor);

        // PleAsE dO NoT rEdEeM Te gIfTcArDs
        TextLayout confirm = new TextLayout("Confirm", g2d.getFont(), g2d.getFontRenderContext());
        editorConfirmBounds = new Rectangle((int) (centerX - confirm.getBounds().getWidth() / 2 - TEXT_PADDING),
                (int) (anchorY - confirm.getBounds().getHeight() - 2 * PANEL_PADDING - 2 * TEXT_PADDING),
                (int) confirm.getBounds().getWidth() + 2 * TEXT_PADDING, (int) confirm.getBounds().getHeight() + 2 * TEXT_PADDING);
        g2d.setColor(new Color(120, 200, 120));
        g2d.fillRoundRect(editorConfirmBounds.x, editorConfirmBounds.y,
                editorConfirmBounds.width, editorConfirmBounds.height,
                ARC_SIZE, ARC_SIZE);
        g2d.setColor(Color.BLACK);
        confirm.draw(g2d, (float) editorConfirmBounds.x + TEXT_PADDING, (float) (editorConfirmBounds.y + confirm.getBounds().getHeight() + TEXT_PADDING));

        Pair<Integer, Integer> buttonStuff = drawTeamButtons(g2d, centerX);
        int buttonsY = buttonStuff.getKey();
        int buttonsWidth = buttonStuff.getValue();

        java.util.List<BufferedImage> icons = new ArrayList<>();
        for (PieceType t : typeList) {
            BufferedImage img = TextureKey.textures.get(teamPrefix + t.getTextureKeySuffix());
            if (img == null) {
                img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            }
            icons.add(img);
        }

        int iconRowWidth = icons.stream().mapToInt(i -> i.getWidth() + ICON_SPACING).sum() - ICON_SPACING;

        int panelWidth = Math.max(iconRowWidth, buttonsWidth) + PANEL_PADDING * 2;

        int panelHeight = icons.get(0).getHeight() + 2 * PANEL_PADDING;
        int panelX = centerX - panelWidth / 2;
        int panelY = buttonsY - panelHeight - 2 * PANEL_PADDING;

        // I messed with the background box, its now transparent :p
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, PANEL_CORNER, PANEL_CORNER);

        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, PANEL_CORNER, PANEL_CORNER);

        editorIconBounds.clear();

        int iconX = centerX - iconRowWidth / 2;
        int iconY = panelY + PANEL_PADDING;

        for (int i = 0; i < icons.size(); i++) {
            BufferedImage img = icons.get(i);

            int ix = iconX;

            if (i == editorChosenTypeIdx) {
                g2d.setColor(new Color(255, 255, 255, 70));
                g2d.fillRoundRect(ix - 4, iconY - 4, img.getWidth() + 8, img.getHeight() + 8, 8, 8);
            }

            g2d.drawImage(img, ix, iconY, null);
            editorIconBounds.add(new Rectangle(ix, iconY, img.getWidth(), img.getHeight()));

            iconX += img.getWidth() + ICON_SPACING;
        }
    }

    private Pair<Integer, Integer> drawTeamButtons(Graphics2D g2d, int centerX) {
        // messed with this 1 value too much...
        TextLayout teamWhite = new TextLayout("White", g2d.getFont(), g2d.getFontRenderContext());
        TextLayout teamBlack = new TextLayout("Black", g2d.getFont(), g2d.getFontRenderContext());
        TextLayout teamNPC = new TextLayout("NPC", g2d.getFont(), g2d.getFontRenderContext());
        int width = Stream.of(teamWhite, teamBlack, teamNPC).map(TextLayout::getBounds).map(Rectangle2D::getWidth).mapToInt(Double::intValue).max().getAsInt();
        int height = Stream.of(teamWhite, teamBlack, teamNPC).map(TextLayout::getBounds).map(Rectangle2D::getHeight).mapToInt(Double::intValue).max().getAsInt();
        width = width + 2 * TEXT_PADDING;
        height = height + 2 * TEXT_PADDING;

        int buttonsY = editorConfirmBounds.y - height - PANEL_PADDING - 2 * TEXT_PADDING;
        final int ICON_SPACING = 8;
        int fullWidth = width * 3 + TEXT_PADDING * 6 + ICON_SPACING * 2;
        int buttonsX = centerX - fullWidth / 2;

        editorTeamToggleWhiteBounds = new Rectangle(buttonsX, buttonsY, width, height);
        editorTeamToggleBlackBounds = new Rectangle(buttonsX + width + ICON_SPACING, buttonsY,
                width, height);
        editorTeamToggleNPCBounds = new Rectangle(buttonsX + (width + ICON_SPACING) * 2, buttonsY,
                width, height);

        drawTeamButton(g2d, editorTeamToggleWhiteBounds, teamWhite, editorChosenTeamIdx == 0);
        drawTeamButton(g2d, editorTeamToggleBlackBounds, teamBlack, editorChosenTeamIdx == 1);
        drawTeamButton(g2d, editorTeamToggleNPCBounds, teamNPC, editorChosenTeamIdx == 2);

        return new Pair<>(buttonsY, fullWidth);
    }
    private void drawTeamButton(Graphics2D g2d, Rectangle r, TextLayout text, boolean selected) {
        g2d.setColor(selected ? new Color(180, 180, 255) : new Color(70, 70, 70));
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, ARC_SIZE, ARC_SIZE);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, ARC_SIZE, ARC_SIZE);
        text.draw(g2d, (float) (r.x + TEXT_PADDING), (float) (r.y + TEXT_PADDING + text.getBounds().getHeight()));
    }
}
