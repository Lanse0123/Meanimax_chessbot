package lanse.worldwarchess.shared.core.screens.pregame;

import lanse.worldwarchess.shared.central.LogoManager;
import lanse.worldwarchess.shared.core.screens.lounge.DevLounge;
import lanse.worldwarchess.shared.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class CreditScreen {

    //This class is responsible for showing the Credit Screen for contributors to World War Chess.

    private static final int ICON_SIZE = 96;
    private static final String[] URLS = {
            "https://www.youtube.com/@lanse012",
            "https://github.com/jamesMFelder",
            "https://www.tiktok.com/@the_pigeon_meli?_t=ZP-90cJ1EGzSyE&_r=1",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=RDdQw4w9WgXcQ",
            "https://www.instagram.com/spiderasasen?igsh=NzNlaHd1enFjZjFo",
            "https://www.youtube.com/watch?v=IoajE4BVZL8&list=PLLRPIrDElQHdTYGQgfy1aQMXJq38cEiQv",
            "https://discord.gg/qsbJrBdw5V",
            "https://github.com/Lanse0123/Abstract"
    };

    /**This was the original link for diego lol
     * "https://www.bing.com/images/search?view=detailV2&ccid=CzCK3%2Fru&id=379D0F00E00" +
     * "519CF333D325E59FE46287A916AAD&thid=OIP.CzCK3_ruiYpxkf_pvZRvmQHaES&mediaurl" +
     * "=https%3A%2F%2Ffacts.net%2Fwp-content%2Fuploads%2F2023%2F09%2F20-facts-about-" +
     * "diego-marquez-go-diego-go-1693878339.jpg&cdnurl=https%3A%2F%2Fth.bing.com%2Fth" +
     * "%2Fid%2FR.0b308adffaee898a7191ffe9bd946f99%3Frik%3DrWqReihG%252flleMg%26pid%3DI" +
     * "mgRaw%26r%3D0&exph=867&expw=1498&q=diego&FORM=IRPRST&ck=2AC40C6CA4549EB0C0C9D728" +
     * "F029E905&selectedIndex=2&itb=0&cw=1375&ch=664&ajaxhist=0&ajaxserp=0"
     */

    private static final String[] ICON_PATHS = {
            "/images/credits/LanseIcon.png",
            "/images/credits/JamesIcon.png",
            "/images/credits/pigeonIcon.png",
            "/images/credits/katIcon.png",
            "/images/credits/spiderasasen123.png",
            "/images/credits/ytIcon.png",
            "/images/credits/discordIcon.png",
            "/images/icon/WWCIcon.png"
    };

    public static void show(JFrame parent) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Credits");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setMinimumSize(new Dimension(700, 520));
            frame.setLocationRelativeTo(parent);
            frame.setLayout(null);
            frame.setIconImages(LogoManager.getLogos());

            // background panel (dark theme)
            JPanel content = new JPanel(null);
            content.setBackground(new Color(30, 30, 30));
            frame.setContentPane(content);

            // Title
            JLabel title = new JLabel("World War Chess", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 36));
            title.setForeground(Color.WHITE);
            title.setBounds(0, 16, frame.getWidth(), 48);
            content.add(title);

            // Subtitle / credits text in the center
            JLabel subtitle = new JLabel("<html><div style='text-align:center;'>Developed by<br/>Lanse and James</div></html>", SwingConstants.CENTER);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            subtitle.setForeground(new Color(200, 200, 200));
            subtitle.setBounds(0, 80, frame.getWidth(), 40);
            content.add(subtitle);

            // Back / Close button
            JButton back = new JButton("Back");
            back.setBounds(14, 14, 90, 28);
            back.setFocusPainted(false);
            back.addActionListener(e -> frame.dispose());
            content.add(back);

            // Secret lounge button :)
            JButton lounge = new JButton("Dev Lounge >:)");
            lounge.setFocusPainted(false);
            lounge.setForeground(new Color(180, 180, 180));
            lounge.setBackground(new Color(45, 45, 45));
            lounge.setBorderPainted(false);
            lounge.setOpaque(true);

            lounge.setBounds(frame.getWidth() - 160, frame.getHeight() - 70, 140, 30);

            lounge.addActionListener(e -> {
                enterDevLounge(frame, parent);
                frame.dispose();
            });
            content.add(lounge);

            // Panel where icons will be placed (we use absolute positions relative to this panel)
            int panelX = 40;
            int panelY = 140;
            int panelW = frame.getWidth() - panelX * 2;
            int panelH = frame.getHeight() - panelY - 40;

            JPanel iconsPanel = new JPanel(null);
            iconsPanel.setOpaque(false);
            iconsPanel.setBounds(panelX, panelY, panelW, panelH);
            content.add(iconsPanel);

            // Recompute positions if window is resized
            frame.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    int w = frame.getWidth();
                    int h = frame.getHeight();
                    title.setBounds(0, 16, w, 48);
                    subtitle.setBounds(0, 80, w, 40);
                    int pW = w - panelX * 2;
                    int pH = h - panelY - 40;
                    iconsPanel.setBounds(panelX, panelY, pW, pH);
                    lounge.setBounds(w - 160, h - 70, 160, 30);
                    layoutIcons(iconsPanel);
                }
            });

            // initial layout
            layoutIcons(iconsPanel);

            frame.setVisible(true);
        });
    }

    private static void enterDevLounge(JFrame frame, JFrame parent) {
        DevLounge.show(frame, parent);
    }

    // Layout icons around a circle and add labels + click behavior
    private static void layoutIcons(JPanel iconsPanel) {
        iconsPanel.removeAll();
        int n = URLS.length;
        int w = iconsPanel.getWidth();
        int h = iconsPanel.getHeight();

        // center for the icons
        int cx = w / 2;
        int cy = h / 2;

        // radius (use slightly smaller to keep inside panel)
        int radius = Math.max(120, Math.min(w, h) / 3);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color labelColor = new Color(230, 230, 230);

        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2 + i * 2 * Math.PI / n;
            int x = cx + (int) (Math.cos(angle) * radius) - ICON_SIZE / 2;
            int y = cy + (int) (Math.sin(angle) * radius) - ICON_SIZE / 2;

            // load icon (safe)
            ImageIcon icon = loadScaledIconSafe(ICON_PATHS[i]);

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setBounds(x, y, ICON_SIZE, ICON_SIZE);
            iconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final String url = URLS[i];
            iconLabel.setToolTipText(url);
            iconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    StringUtils.openUrl(url);
                }
            });
            iconsPanel.add(iconLabel);

            // Text label underneath the icon (centered)
            String labelText = getLabelForUrl(url);
            JLabel textLabel = new JLabel(labelText, SwingConstants.CENTER);
            textLabel.setForeground(labelColor);
            textLabel.setFont(labelFont);
            int textW = 160;
            int textH = 20;
            int tx = x + ICON_SIZE / 2 - textW / 2;
            int ty = y + ICON_SIZE + 8;
            textLabel.setBounds(tx, ty, textW, textH);
            iconsPanel.add(textLabel);
        }

        // Center block with some extra credits or licensing text
        JLabel centerBlock = new JLabel("<html><div style='text-align:center;'>:p</div></html>", SwingConstants.CENTER);
        centerBlock.setForeground(new Color(180, 180, 180));
        centerBlock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        centerBlock.setBounds(cx - 220, cy - 30, 440, 60);
        iconsPanel.add(centerBlock);

        iconsPanel.revalidate();
        iconsPanel.repaint();
    }

    private static String getLabelForUrl(String url) {
        if (url.contains("youtube.com/@lanse012")) return "Lanse";
        if (url.contains("github.com/jamesMFelder")) return "James";
        if (url.contains("@the_pigeon_meli")) return "Animator";
        if (url.contains("9WgXcQ&list=RDdQw4w9W")) return "Captain of Moral Support";
        if (url.contains("spiderasasen")) return "Sound Design";
        if (url.contains("youtube.com/watch")) return "WWC Devlog";
        if (url.contains("discord.gg")) return "Discord";
        if (url.contains("github.com/Lanse0123")) return "WWC Repo";
        return url;
    }

    private static ImageIcon loadScaledIconSafe(String path) {
        try {
            java.net.URL res = CreditScreen.class.getResource(path);
            if (res == null) throw new IllegalArgumentException("resource not found: " + path);
            ImageIcon icon = new ImageIcon(ImageIO_readSafe(res));
            Image img = icon.getImage().getScaledInstance(CreditScreen.ICON_SIZE, CreditScreen.ICON_SIZE, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Throwable t) {
            // fallback placeholder icon (colored circle)
            BufferedImage placeholder = new BufferedImage(CreditScreen.ICON_SIZE, CreditScreen.ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(80, 80, 80));
            g.fillRoundRect(0, 0, CreditScreen.ICON_SIZE, CreditScreen.ICON_SIZE, 14, 14);
            g.setColor(new Color(160, 160, 160));
            g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(14, CreditScreen.ICON_SIZE /6)));
            FontMetrics fm = g.getFontMetrics();
            String tchar = "?";
            int tx = (CreditScreen.ICON_SIZE - fm.stringWidth(tchar)) / 2;
            int ty = (CreditScreen.ICON_SIZE - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(tchar, tx, ty);
            g.dispose();
            return new ImageIcon(placeholder);
        }
    }

    // Helper to read with ImageIO (avoids silent NPE)
    private static BufferedImage ImageIO_readSafe(java.net.URL res) throws Exception {
        BufferedImage img = javax.imageio.ImageIO.read(Objects.requireNonNull(res));
        if (img == null) throw new IllegalArgumentException("ImageIO returned null for " + res);
        return img;
    }
}
