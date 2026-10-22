package lanse.worldwarchess.shared.chat;

import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.shared.central.CentralTick;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.shared.core.worldmap.board.Board;
import lanse.worldwarchess.steam.Account;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.*;

public class InGameChatRenderer {
    public static boolean ingameChatEnabled = true;

    private enum ChatType {
        KING_LOCAL,      // existing bubble chat
        GLOBAL_SPECIAL,  // specialtyLevel == 1
        GLOBAL_DEAD      // dead king global chat
    }

    private record ChatEntry(String msg, String username, int specialtyLevel, long timestampMillis, ChatType type) {}
    private final Map<Piece, List<ChatEntry>> messages = new HashMap<>();
    private final List<ChatEntry> globalSpecialMessages = new ArrayList<>();
    private final List<ChatEntry> globalDeadMessages = new ArrayList<>();

    public final JTextField chatInput;

    public InGameChatRenderer() {
        chatInput = new JTextField();
        ((AbstractDocument)chatInput.getDocument()).setDocumentFilter(new ChatManager.DocumentSizeFilter());
        Main.gamePanel.setLayout(null);
        //The amount of work to center it is kinda annoying
        Main.gamePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerChatBox();
            }
        });

        SwingUtilities.invokeLater(this::centerChatBox);

        chatInput.setLocation(Main.gamePanel.getWidth() / 2, Main.gamePanel.getHeight() / 2);
        chatInput.setSize(200, 20);
        chatInput.setVisible(false);
        chatInput.addActionListener(e -> {
            int specialtyLevel;
            if (!e.getActionCommand().isEmpty()) {
                if (Account.isDev()) {
                    specialtyLevel = 2;
                    CascadePacketManager.manager.sendPacketToEveryone(CascadePacketManager.PacketType.SUPER_SPECIAL_CHAT, e.getActionCommand(), Optional.empty());
                } else {
                    specialtyLevel = 0;
                    CascadePacketManager.manager.sendPacketToEveryone(CascadePacketManager.PacketType.CHAT, e.getActionCommand(), Optional.empty());
                }

                Optional<Piece> king = WorldMap.getKingWithName(Account.username());
                addMessage(king, Account.username(), ChatFilter.cleanInput(chatInput.getText()), specialtyLevel);
            }

            chatInput.setText("");
            chatInput.setFocusable(false);
            chatInput.setVisible(false);
        });
    }

    private void centerChatBox() {
        int w = Main.gamePanel.getWidth();
        int x = (w - chatInput.getWidth()) / 2;
        int y = (int)(Main.gamePanel.getHeight() * 0.15);
        chatInput.setLocation(x, y);
    }

    public void showChatInput() {
        if (Timer.gameStarted) {
            chatInput.setVisible(true);
            chatInput.setFocusable(true);
            chatInput.requestFocus();
        }
    }

    public void addMessage(Optional<Piece> piece, String username, String message, int specialtyLevel) {
        long now = System.currentTimeMillis();

        if (specialtyLevel == 1) {
            globalSpecialMessages.add(new ChatEntry(message, username, specialtyLevel, now, ChatType.GLOBAL_SPECIAL));
            trim(globalSpecialMessages, 8);
            return;
        }

        if (piece.isEmpty()) {
            globalDeadMessages.add(new ChatEntry(message, username, specialtyLevel, now, ChatType.GLOBAL_DEAD));
            trim(globalDeadMessages, 8);
            if (!Main.gamePanel.sidebarMenu.isOpen) {
                Main.gamePanel.sidebarMenu.toggleMenu();
            }
            return;
        }

        Piece king = piece.get();
        messages.computeIfAbsent(king, k -> new ArrayList<>()).add(new ChatEntry(message, username, specialtyLevel, now, ChatType.KING_LOCAL));
        trim(messages.get(king), 3);
    }

    private void trim(List<ChatEntry> list, int maxSize) {
        if (list.size() > maxSize) list.remove(0);
    }

    public void drawAllTextBubbles(Graphics2D g2d, double zoomFactor) {
        if (!ingameChatEnabled) return;

        // 1.0 = normal, 0.5 = half size, 0.1 is tiny, etc.
        final double SCALE = 0.5;
        final Font ORIGINAL_FONT = g2d.getFont();

        for (Map.Entry<Piece, List<ChatEntry>> entry : messages.entrySet()) {
            Piece king = entry.getKey();
            List<ChatEntry> msgs = entry.getValue();

            Board board = king.getBoard();
            int baseTile = board.getBaseTileSize();
            double scaledTile = baseTile * zoomFactor;

            int pieceSize = (int) (king.getPieceSize() * scaledTile);
            int pieceDrawX = (int) (board.getBoardX() * zoomFactor + king.getPosX() * scaledTile);
            int pieceDrawY = (int) (board.getBoardY() * zoomFactor + king.getPosY() * scaledTile);

            int centerX = pieceDrawX + pieceSize / 2;

            int y = pieceDrawY - (int) (150 * zoomFactor);

            for (ChatEntry chat : msgs) {
                Font scaledFont = ORIGINAL_FONT.deriveFont((float)(ORIGINAL_FONT.getSize2D() * SCALE));

                Font useFont = switch (chat.specialtyLevel) {
                    case 1, 2 -> scaledFont.deriveFont(Font.BOLD);
                    default -> scaledFont;
                };

                g2d.setFont(useFont);

                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(chat.msg);
                int textHeight = fm.getHeight();

                int padX = (int) (6 * zoomFactor * SCALE);
                int padY = (int) (4 * zoomFactor * SCALE);

                int bubbleW = textWidth + padX * 2;
                int bubbleH = textHeight + padY * 2;
                int arc = (int) (bubbleH * 0.9);

                int bubbleX = centerX - bubbleW / 2;
                int bubbleY = y - fm.getAscent() - padY;
                int textX = centerX - textWidth / 2;
                int textY = y;

                if (chat.specialtyLevel == 2) {
                    g2d.setColor(new Color(120, 120, 120, 150));
                } else {
                    g2d.setColor(new Color(0, 100, 255, 150));
                }

                g2d.fillRoundRect(bubbleX, bubbleY, bubbleW, bubbleH, arc, arc);

                if (chat.specialtyLevel == 2) {
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke((float)(2 * zoomFactor * SCALE)));
                    g2d.drawRoundRect(bubbleX, bubbleY, bubbleW, bubbleH, arc, arc);
                }

                switch (chat.specialtyLevel) {
                    case 1 -> { //special message
                        g2d.setColor(new Color(255, 220, 0));
                    }
                    case 2 -> { //super special message
                        g2d.setColor(Color.RED);
                    }
                    default -> { //regular message
                        g2d.setColor(Color.WHITE);
                    }
                }

                g2d.drawString(chat.msg, textX, textY);

                y -= (bubbleH + (int)(6 * zoomFactor));
            }
        }
    }

    public void drawAllGlobalChats(Graphics g) {
        if (!ingameChatEnabled) return;

        drawGlobalChats(g, globalSpecialMessages, true);

        Optional<Piece> king = WorldMap.getKingWithName(Account.username());

        if (king.isEmpty() || CentralTick.gameOver) {
            drawGlobalChats(g, globalDeadMessages, false);
        }
    }

    @SuppressWarnings("all") //annoying inline warnings begone
    private void drawGlobalChats(Graphics g, List<ChatEntry> list, boolean leftSide) {
        if (!ingameChatEnabled || list.isEmpty() || !Main.gamePanel.sidebarMenu.isOpen) return;

        Font font = g.getFont().deriveFont(Font.BOLD, 14f);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int xPadding = 10;
        int yPadding = 6;
        int spacing = 4;

        int screenW = Main.gamePanel.getWidth();
        int screenH = Main.gamePanel.getHeight();

        int sidebarWidth = Main.gamePanel.sidebarMenu.getPreferredSize().width;

        int baseX = leftSide ? xPadding : screenW - sidebarWidth - xPadding;
        int baseY = screenH - (yPadding * 20);
        int y = baseY;

        for (int i = list.size() - 1; i >= 0; i--) {
            ChatEntry chat = list.get(i);
            String username = chat.username();
            String msg = chat.msg();

            int bubbleW = 500;
            int textH = fm.getHeight();
            int bubbleH = textH + yPadding * 2;

            int bubbleX = baseX - (leftSide ? 0 : bubbleW);
            int bubbleY = y - bubbleH;

            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(bubbleX, bubbleY, bubbleW, bubbleH, 10, 10);

            switch (chat.specialtyLevel()) {
                case 1 -> g.setColor(new Color(255, 220, 0));
                case 2 -> g.setColor(Color.RED);
                default -> g.setColor(Color.WHITE);
            }

            g.drawString(username + ": " + msg, bubbleX + 8, bubbleY + fm.getAscent() + yPadding);

            y -= (bubbleH + spacing);
        }
    }

    public void removeOldMessages() {
        long now = System.currentTimeMillis();
        long cutoff = now - 30000;

        Iterator<Map.Entry<Piece, List<ChatEntry>>> it = messages.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Piece, List<ChatEntry>> entry = it.next();
            List<ChatEntry> list = entry.getValue();

            list.removeIf(chat -> chat.timestampMillis < cutoff);

            if (list.isEmpty()) {
                it.remove();
            }
        }
    }
}