package torneoapp.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class Icons {

    /**
     * Carrega um ícone PNG já com alpha correto (branco sobre transparente).
     * Faz escalonamento de alta qualidade em múltiplos passos.
     */
    public static ImageIcon load(String name, int w, int h) {
        return loadColored(name, w, h, null);
    }

    public static ImageIcon loadColored(String name, int w, int h, Color recolor) {
        try {
            URL url = Icons.class.getResource("/icons/" + name);
            if (url == null) return null;

            // Use ImageIO for proper alpha reading
            BufferedImage raw = ImageIO.read(url);
            if (raw == null) return null;

            // High-quality multi-step downscale
            BufferedImage scaled = multiStepScale(raw, w, h);

            // If recolor requested: tint all non-transparent pixels to that color
            // preserving the alpha channel as-is
            if (recolor != null) {
                scaled = tint(scaled, recolor);
            }

            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Multi-step high-quality downscale.
     * Halves each step until within 2x of target, then final bicubic pass.
     */
    private static BufferedImage multiStepScale(BufferedImage src, int targetW, int targetH) {
        int curW = src.getWidth();
        int curH = src.getHeight();
        BufferedImage current = src;

        // Halving steps
        while (curW > targetW * 2 || curH > targetH * 2) {
            curW = Math.max(targetW, curW / 2);
            curH = Math.max(targetH, curH / 2);
            BufferedImage next = new BufferedImage(curW, curH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = next.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(current, 0, 0, curW, curH, null);
            g.dispose();
            current = next;
        }

        // Final pass to exact target size
        if (curW != targetW || curH != targetH) {
            BufferedImage final_ = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = final_.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(current, 0, 0, targetW, targetH, null);
            g.dispose();
            current = final_;
        }

        return current;
    }

    /**
     * Tints an ARGB image: replaces RGB of every pixel with the given color,
     * preserving the original alpha exactly (no brightness heuristics).
     */
    private static BufferedImage tint(BufferedImage src, Color color) {
        int w = src.getWidth(), h = src.getHeight();
        int cr = color.getRed(), cg = color.getGreen(), cb = color.getBlue();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = src.getRGB(x, y);
                int a = (pixel >> 24) & 0xFF;
                // Only paint pixels that have meaningful alpha
                if (a > 4) {
                    out.setRGB(x, y, (a << 24) | (cr << 16) | (cg << 8) | cb);
                }
                // else leave transparent
            }
        }
        return out;
    }

    /** Para sidebar: ícone recolorido a branco */
    public static ImageIcon sidebar(String name, int size) {
        return loadColored(name, size, size, Color.WHITE);
    }

    // ── Named shortcuts ──────────────────────────────────────────────────────
    public static ImageIcon casa(int s)        { return load("casa.png", s, s); }
    public static ImageIcon estadio(int s)     { return load("estadioc.png", s, s); }
    public static ImageIcon equipa(int s)      { return load("equipac.png", s, s); }
    public static ImageIcon equipaVerde(int s) { return load("equipaverde.png", s, s); }
    public static ImageIcon jogador(int s)     { return load("jogador.png", s, s); }
    public static ImageIcon treinador(int s)   { return load("treinadorc.png", s, s); }
    public static ImageIcon taca(int s)        { return load("tacac.png", s, s); }
    public static ImageIcon torneio(int s)     { return load("torneio.png", s, s); }
    public static ImageIcon leaderboard(int s) { return load("leaderboard.png", s, s); }
    public static ImageIcon bola(int s)        { return load("bolac.png", s, s); }
    public static ImageIcon bolaShoot(int s)   { return load("bola.png", s, s); }
    public static ImageIcon assist(int s)      { return load("assistc.png", s, s); }
    public static ImageIcon ranking(int s)     { return load("rankingc.png", s, s); }
    public static ImageIcon bilhete(int s)     { return load("bilhetec.png", s, s); }
    public static ImageIcon contrato(int s)    { return load("contratoc.png", s, s); }
    public static ImageIcon patrocinio(int s)  { return load("patrocinioc.png", s, s); }
    public static ImageIcon receita(int s)     { return load("receita.png", s, s); }
    public static ImageIcon olho(int s)        { return load("olho.png", s, s); }
    public static ImageIcon lixo(int s)        { return load("lixop.png", s, s); }
}
