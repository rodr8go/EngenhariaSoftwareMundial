package torneoapp.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class UITheme {
    public static final Color PRIMARY      = new Color(26, 60, 120);
    public static final Color PRIMARY_DARK = new Color(15, 40, 90);
    public static final Color ACCENT       = new Color(0, 122, 255);
    public static final Color SIDEBAR_BG   = new Color(18, 42, 90);
    public static final Color SIDEBAR_TEXT = new Color(200, 215, 240);
    public static final Color SIDEBAR_SELECTED = new Color(0, 122, 255);
    public static final Color CONTENT_BG  = new Color(235, 238, 248);
    public static final Color CARD_BG     = Color.WHITE;
    public static final Color SUCCESS     = new Color(52, 199, 89);
    public static final Color DANGER      = new Color(255, 59, 48);
    public static final Color YELLOW      = new Color(255, 204, 0);
    public static final Color TEXT_PRIMARY   = new Color(30, 30, 50);
    public static final Color TEXT_SECONDARY = new Color(120, 130, 160);
    public static final Color TABLE_HEADER  = new Color(240, 243, 250);
    public static final Color TABLE_ALT     = new Color(250, 251, 255);

    public static final Color GRAY_BTN = new Color(130, 130, 140);

    // ── Buttons ─────────────────────────────────────────────────────────────
    public static JButton primaryButton(String text) {
        return styledBtn(text, ACCENT, Color.WHITE, true);
    }
    public static JButton primaryButton(String text, ImageIcon icon) {
        JButton b = primaryButton(text);
        b.setIcon(icon);
        b.setIconTextGap(6);
        return b;
    }
    public static JButton successButton(String text) {
        return styledBtn(text, SUCCESS, Color.WHITE, true);
    }
    public static JButton successButton(String text, ImageIcon icon) {
        JButton b = successButton(text);
        b.setIcon(icon);
        b.setIconTextGap(6);
        return b;
    }
    public static JButton dangerButton(String text) {
        return styledBtn(text, DANGER, Color.WHITE, true);
    }
    public static JButton dangerButton(String text, ImageIcon icon) {
        JButton b = dangerButton(text);
        b.setIcon(icon);
        b.setIconTextGap(6);
        return b;
    }
    public static JButton grayButton(String text) {
        return styledBtn(text, GRAY_BTN, Color.WHITE, true);
    }
    public static JButton grayButton(String text, ImageIcon icon) {
        JButton b = grayButton(text);
        b.setIcon(icon);
        b.setIconTextGap(6);
        return b;
    }
    /** @deprecated use successButton / grayButton / dangerButton instead */
    public static JButton outlineButton(String text) {
        return grayButton(text);
    }
    public static JButton outlineButton(String text, ImageIcon icon) {
        return grayButton(text, icon);
    }
    public static JButton iconButton(ImageIcon icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return btn;
    }
    private static JButton styledBtn(String text, Color bg, Color fg, boolean filled) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g2 = (Graphics2D) g0.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getBackground();
                if (!filled) {
                    // outline style: white/light bg with coloured border
                    g2.setColor(getModel().isRollover() ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 230) : base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(fg);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                } else {
                    // filled style with hover/press darkening
                    Color paint;
                    if (getModel().isPressed()) paint = base.darker();
                    else if (getModel().isRollover()) paint = base.brighter();
                    else paint = base;
                    g2.setColor(paint);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g0);
            }
            @Override protected void paintBorder(Graphics g) {
                if (!filled) return; // border drawn in paintComponent
                // no default border for filled buttons
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // ── Labels ───────────────────────────────────────────────────────────────
    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(TEXT_PRIMARY);
        return l;
    }
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        l.setForeground(TEXT_PRIMARY);
        return l;
    }
    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    // ── Cards & panels ───────────────────────────────────────────────────────
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 240), 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return p;
    }
    public static JPanel statCard(String label, String value, Color accent) {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 8));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        valLbl.setForeground(accent);
        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblLbl.setForeground(TEXT_SECONDARY);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(lblLbl, BorderLayout.SOUTH);
        return card;
    }

    // ── Table ────────────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(210, 228, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 240)));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? CARD_BG : TABLE_ALT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    // ── Form fields ──────────────────────────────────────────────────────────
    public static JTextField formField(String placeholder) {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 230)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    // ── Photo picker ─────────────────────────────────────────────────────────
    /** Mostra um JLabel circular clicável que abre file chooser e guarda o path no array [0] */
    public static JLabel photoPickerLabel(String[] pathHolder, int size, Component parent) {
        JLabel lbl = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 245));
                g2.fillOval(0, 0, size, size);
                if (pathHolder[0] != null) {
                    try {
                        BufferedImage img = ImageIO.read(new File(pathHolder[0]));
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                        g2.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, size, size));
                        // Scale to fill circle maintaining aspect ratio
                        double scale = Math.max((double)size/img.getWidth(), (double)size/img.getHeight());
                        int sw = (int)(img.getWidth()*scale), sh = (int)(img.getHeight()*scale);
                        g2.drawImage(img, (size-sw)/2, (size-sh)/2, sw, sh, null);
                    } catch (Exception ignored) {}
                } else {
                    g2.setColor(new Color(160, 170, 200));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    String t = "+ Foto";
                    g2.drawString(t, (size - fm.stringWidth(t)) / 2, size / 2 + fm.getAscent() / 2 - 2);
                }
                g2.dispose();
            }
        };
        lbl.setPreferredSize(new Dimension(size, size));
        lbl.setMinimumSize(new Dimension(size, size));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.setToolTipText("Clique para escolher foto");
        lbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens", "png","jpg","jpeg","gif"));
                if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                    pathHolder[0] = fc.getSelectedFile().getAbsolutePath();
                    lbl.repaint();
                }
            }
        });
        return lbl;
    }

    /** Carrega imagem de ficheiro e escala para caber em size×size, recortada em círculo */
    public static ImageIcon loadPhotoCircle(String path, int size) {
        try {
            if (path == null || path.isBlank()) return null;
            BufferedImage src = ImageIO.read(new File(path));
            BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, size, size));
            g2.drawImage(src, 0, 0, size, size, null);
            g2.dispose();
            return new ImageIcon(out);
        } catch (Exception e) { return null; }
    }

    /** Cria barra de estatística estilo FIFA */
    public static JPanel statBar(String label, double valCasa, double valVisitante, Color corCasa, Color corVisitante) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        double total = valCasa + valVisitante;
        double pctCasa = total == 0 ? 0.5 : valCasa / total;
        double pctVisit = 1 - pctCasa;
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_SECONDARY);
        JPanel barRow = new JPanel(new GridLayout(1, 3, 8, 0));
        barRow.setBackground(CARD_BG);
        JLabel vCasa = new JLabel(formatStatVal(valCasa), SwingConstants.RIGHT);
        vCasa.setFont(new Font("SansSerif", Font.BOLD, 13));
        JPanel barPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), half = w / 2;
                int r = h / 2;
                // background
                g2.setColor(new Color(220, 222, 230));
                g2.fillRoundRect(0, 0, w, h, r*2, r*2);
                // casa (left from center going left)
                int barCasa = (int)(half * pctCasa * 2);
                if (barCasa > 0) {
                    g2.setColor(corCasa);
                    g2.fillRoundRect(half - barCasa, 0, barCasa, h, r*2, r*2);
                }
                // visitante (right from center going right)
                int barVisit = (int)(half * pctVisit * 2);
                if (barVisit > 0) {
                    g2.setColor(corVisitante);
                    g2.fillRoundRect(half, 0, barVisit, h, r*2, r*2);
                }
            }
        };
        barPanel.setBackground(CARD_BG);
        barPanel.setPreferredSize(new Dimension(0, 12));
        JLabel vVisit = new JLabel(formatStatVal(valVisitante), SwingConstants.LEFT);
        vVisit.setFont(new Font("SansSerif", Font.BOLD, 13));
        barRow.add(vCasa);
        barRow.add(barPanel);
        barRow.add(vVisit);
        p.add(lbl, BorderLayout.NORTH);
        p.add(barRow, BorderLayout.CENTER);
        return p;
    }

    private static String formatStatVal(double v) {
        return v == (int) v ? String.valueOf((int) v) : String.format("%.0f%%", v);
    }
}
