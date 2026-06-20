package torneoapp.ui;

import torneoapp.service.DataStore;
import torneoapp.util.Icons;
import torneoapp.util.UITheme;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private DashboardPanel   dashboardPanel;
    private EstadiosPanel    estadiosPanel;
    private EquipasPanel     equipasPanel;
    private TorneioPanel     torneioPanel;
    private ResultadosPanel  resultadosPanel;
    private PatrociniosPanel patrociniosPanel;
    private BilheteiraPanel  bilheteiraPanel;
    private VoluntariosPanel voluntariosPanel;

    private JButton[] navBtns;
    private JLabel torneioNameLbl;

    private final String[] panelKeys = {
        "Dashboard","Estádios","Equipas","Torneio","Resultados","Patrocínios","Bilheteira","Voluntários"
    };

    public MainFrame() {
        setTitle("Gestor de Torneio");
        setSize(1320, 800);
        setMinimumSize(new Dimension(1050, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    public void setTorneiName(String nome) {
        if (torneioNameLbl != null) torneioNameLbl.setText(nome);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout());
        main.add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.CONTENT_BG);

        dashboardPanel   = new DashboardPanel(this);
        estadiosPanel    = new EstadiosPanel();
        equipasPanel     = new EquipasPanel();
        torneioPanel     = new TorneioPanel(this);
        resultadosPanel  = new ResultadosPanel(this);
        patrociniosPanel = new PatrociniosPanel();
        bilheteiraPanel  = new BilheteiraPanel();
        voluntariosPanel = new VoluntariosPanel();

        contentPanel.add(dashboardPanel,   "Dashboard");
        contentPanel.add(estadiosPanel,    "Estádios");
        contentPanel.add(equipasPanel,     "Equipas");
        contentPanel.add(torneioPanel,     "Torneio");
        contentPanel.add(resultadosPanel,  "Resultados");
        contentPanel.add(patrociniosPanel, "Patrocínios");
        contentPanel.add(bilheteiraPanel,  "Bilheteira");
        contentPanel.add(voluntariosPanel, "Voluntários");

        main.add(contentPanel, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        showPanel("Dashboard");
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(UITheme.PRIMARY_DARK);
        h.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        h.setPreferredSize(new Dimension(0, 52));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(UITheme.PRIMARY_DARK);
        ImageIcon tacaIcon = Icons.sidebar("tacac.png", 28);
        if (tacaIcon != null) left.add(new JLabel(tacaIcon));
        JLabel logo = new JLabel("Gestor de Torneio");
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        left.add(logo);
        h.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setBackground(UITheme.PRIMARY_DARK);

        // Torneio name badge
        torneioNameLbl = new JLabel("");
        torneioNameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        torneioNameLbl.setForeground(new Color(160, 200, 255));
        String tnome = DataStore.getInstance().getTorneioAtual() != null
            ? DataStore.getInstance().getTorneioAtual().getNome() : "";
        torneioNameLbl.setText(tnome);
        right.add(torneioNameLbl);


        return h;
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setBackground(UITheme.SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // ── Logo: compact horizontal strip ──────────────────────────────
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        logoArea.setBackground(new Color(8, 22, 58));
        logoArea.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));
        logoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoArea.setMaximumSize(new Dimension(220, 64));

        ImageIcon ballIco = Icons.sidebar("bolac.png", 32);
        JLabel ballLbl = ballIco != null
            ? new JLabel(ballIco)
            : new JLabel("⚽");

        JLabel torneioName = new JLabel("Torneio");
        torneioName.setForeground(new Color(200, 215, 245));
        torneioName.setFont(new Font("SansSerif", Font.BOLD, 15));

        logoArea.add(ballLbl);
        logoArea.add(torneioName);
        sb.add(logoArea);

        // thin separator
        JPanel sep = new JPanel();
        sep.setBackground(new Color(30, 55, 110));
        sep.setMaximumSize(new Dimension(220, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(sep);

        // ── Navigation sections ──────────────────────────────────────────
        sb.add(Box.createVerticalStrut(4));

        String[][][] sections = {
            {{"Principal"}, {"Dashboard","casa.png"}, {"Estádios","estadioc.png"}, {"Equipas","equipac.png"}},
            {{"Torneio"},   {"Torneio","tacac.png"},  {"Resultados","rankingc.png"}},
            {{"Financeiro"},{"Patrocínios","patrocinioc.png"}, {"Bilheteira","bilhetec.png"}},
            {{"Gestão"},    {"Voluntários","assistc.png"}}
        };

        navBtns = new JButton[panelKeys.length];
        for (String[][] section : sections) {
            // Section label
            JPanel sRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            sRow.setBackground(UITheme.SIDEBAR_BG);
            sRow.setMaximumSize(new Dimension(220, 28));
            sRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel sLbl = new JLabel(section[0][0].toUpperCase());
            sLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
            sLbl.setForeground(new Color(95, 125, 175));
            sRow.add(sLbl);
            sb.add(Box.createVerticalStrut(2));
            sb.add(sRow);

            for (int i = 1; i < section.length; i++) {
                String key = section[i][0], ico = section[i][1];
                JButton btn = createNavButton(key, ico);
                for (int k = 0; k < panelKeys.length; k++)
                    if (panelKeys[k].equals(key)) { navBtns[k] = btn; break; }
                sb.add(btn);
            }
        }
        sb.add(Box.createVerticalGlue());
        return sb;
    }

    private JButton createNavButton(String text, String iconFile) {
        ImageIcon ico = Icons.sidebar(iconFile, 17);
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g2 = (Graphics2D) g0.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground();
                boolean selected = bg.equals(UITheme.SIDEBAR_SELECTED);
                if (selected) {
                    // left accent bar
                    g2.setColor(UITheme.ACCENT);
                    g2.fillRect(0, 0, 3, getHeight());
                    // highlight bg
                    g2.setColor(new Color(30, 65, 140));
                    g2.fillRect(3, 0, getWidth()-3, getHeight());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(22, 48, 108));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g0);
            }
        };
        if (ico != null) { btn.setIcon(ico); btn.setIconTextGap(11); }
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(new Color(200, 215, 245));
        btn.setBackground(UITheme.SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showPanel(text));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.repaint(); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.repaint(); }
        });
        return btn;
    }

    public void showPanel(String name) {
        for (JButton b : navBtns) {
            if (b == null) continue;
            b.setBackground(UITheme.SIDEBAR_BG);
            b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        }
        for (int k = 0; k < panelKeys.length; k++) {
            if (panelKeys[k].equals(name) && navBtns[k] != null) {
                navBtns[k].setBackground(UITheme.SIDEBAR_SELECTED);
                navBtns[k].setFont(new Font("SansSerif", Font.BOLD, 13));
            }
        }
        cardLayout.show(contentPanel, name);
        switch (name) {
            case "Dashboard"   -> dashboardPanel.refresh();
            case "Estádios"    -> estadiosPanel.refresh();
            case "Equipas"     -> equipasPanel.refresh();
            case "Torneio"     -> torneioPanel.refresh();
            case "Resultados"  -> resultadosPanel.refresh();
            case "Patrocínios" -> patrociniosPanel.refresh();
            case "Bilheteira"  -> bilheteiraPanel.refresh();
            case "Voluntários" -> voluntariosPanel.refresh();
        }
    }
}
