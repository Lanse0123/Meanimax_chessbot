package lanse.worldwarchess.shared.chat;

import lanse.worldwarchess.cascading.CascadePacketManager;
import lanse.worldwarchess.shared.central.Main;
import lanse.worldwarchess.shared.core.Timer;
import lanse.worldwarchess.shared.core.piece.Piece;
import lanse.worldwarchess.shared.core.worldmap.WorldMap;
import lanse.worldwarchess.steam.Account;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Optional;

public class ChatManager {

    private static JTextPane chatArea; // changed from JTextArea
    private static JTextField chatInput;
    private static JPanel chatPanel;

    public static int MAX_MSG_LEN = 500;

    public static void init() {
        chatPanel = new JPanel(new BorderLayout(5, 5));
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(50, 50, 50));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(400, 120));
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        chatInput = new JTextField();
        chatInput.setBackground(new Color(80, 80, 80));
        chatInput.setForeground(new Color(255, 255, 255));
        ((AbstractDocument) chatInput.getDocument()).setDocumentFilter(new DocumentSizeFilter());

        new Placeholder("Click here to chat!", chatInput);

        chatInput.addActionListener((ActionEvent e) -> sendChatMessage());
        chatPanel.add(chatInput, BorderLayout.SOUTH);
    }

    private static void sendChatMessage() {
        String msg = ChatFilter.cleanInput(chatInput.getText().trim());
        if (!msg.isEmpty()) {
            appendChatMessage(Account.username(), msg, (Account.isDev() ? 2 : 0));
            chatInput.setText("");
            if (Account.isDev()) {
                CascadePacketManager.manager.sendPacketToEveryone(CascadePacketManager.PacketType.SUPER_SPECIAL_CHAT, msg, Optional.empty());
            } else {
                CascadePacketManager.manager.sendPacketToEveryone(CascadePacketManager.PacketType.CHAT, msg, Optional.empty());
            }
        }
    }

    public static void handleChatMessage(String msg, String originName, int specialtyLevel) {
        String cleanText = ChatFilter.cleanInput(msg);
        if (Timer.gameStarted) {
            Optional<Piece> king = WorldMap.getKingWithName(originName);
            Main.gamePanel.chat.addMessage(king, originName, cleanText, specialtyLevel);
        } else {
            appendChatMessage(originName, cleanText, specialtyLevel);
        }
    }

    /** Appends a chat message, with bold yellow style for join messages */
    private static void appendChatMessage(String username, String msg, int specialtyLevel) {
        StyledDocument doc = chatArea.getStyledDocument();

        SimpleAttributeSet attr = new SimpleAttributeSet();
        if (specialtyLevel == 1) {
            StyleConstants.setBold(attr, true);
            StyleConstants.setForeground(attr, Color.YELLOW.darker());
        } else if (specialtyLevel == 2){
            StyleConstants.setBold(attr, true);
            StyleConstants.setForeground(attr, Color.RED);
        } else {
            StyleConstants.setForeground(attr, Color.WHITE);
        }

        try {
            doc.insertString(doc.getLength(), username + ": " + msg + "\n", attr);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Always scroll to bottom after new message
        SwingUtilities.invokeLater(() -> chatArea.setCaretPosition(doc.getLength()));
    }

    public static JPanel getPanel() {
        return chatPanel;
    }

    public static class DocumentSizeFilter extends DocumentFilter {
        private final int maxChars;

        public DocumentSizeFilter(int maxChars) {
            this.maxChars = maxChars;
        }

        public DocumentSizeFilter() {
            this.maxChars = ChatManager.MAX_MSG_LEN;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (fb.getDocument().getLength() + string.length() <= maxChars) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (fb.getDocument().getLength() - length + text.length() <= maxChars) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    static class Placeholder extends JLabel {
        private final JTextField textField;

        public Placeholder(String text, JTextField textField) {
            super(text);
            this.textField = textField;

            setForeground(new Color(180, 180, 180));
            setFont(textField.getFont());
            setBorder(textField.getBorder());
            setOpaque(false);

            textField.setLayout(new BorderLayout());
            textField.add(this);

            updateVisibility();

            textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateVisibility(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateVisibility(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateVisibility(); }
            });

            textField.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { updateVisibility(); }
                @Override public void focusLost(FocusEvent e) { updateVisibility(); }
            });
        }

        private void updateVisibility() {
            setVisible(!textField.hasFocus() && textField.getText().isEmpty());
        }
    }
}