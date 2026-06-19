package torneoapp.ui;

import torneoapp.model.*;
import torneoapp.service.DataStore;
import torneoapp.util.Icons;
import torneoapp.util.UITheme;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {

    private MainFrame mainFrame;

    // Stat cards (top row)
    private JLabel valTotalLbl, valBilhLbl, valPatLbl, valJogosLbl;

    // Ranking panel
    private JPanel rankingPanel;

    // Receita por jogo panel
    private JPanel receitaJogoPanel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(UITheme.CONTENT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.CONTENT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        // ── Title ─────────────────────────────────────────────────────────
        JLabel title = UITheme.titleLabel("Dashboard Financeiro");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        root.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(UITheme.CONTENT_BG);

        // ── Top stat cards ─────────────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setBackground(UITheme.CONTENT_BG);
        statsRow.setPreferredSize(new Dimension(0, 100));

        valTotalLbl = new JLabel("0€");
        valBilhLbl  = new JLabel("0€");
        valPatLbl   = new JLabel("0€");
        valJogosLbl = new JLabel("0");

        ImageIcon recIcon = Icons.load("receita.png", 18, 18);
        ImageIcon bilhIcon= Icons.load("bilhetec.png", 18, 18);
        ImageIcon patIcon = Icons.load("patrocinioc.png", 18, 18);
        ImageIcon bolIcon = Icons.load("bolac.png", 18, 18);

        statsRow.add(buildStatCard("Receitas totais",   valTotalLbl, UITheme.SUCCESS,              recIcon));
        statsRow.add(buildStatCard("Bilheteira",         valBilhLbl,  UITheme.ACCENT,              bilhIcon));
        statsRow.add(buildStatCard("Patrocínios",        valPatLbl,   new Color(130, 80, 220),     patIcon));
        statsRow.add(buildStatCard("Jogos Realizados",   valJogosLbl, UITheme.TEXT_PRIMARY,        bolIcon));

        body.add(statsRow, BorderLayout.NORTH);

        // ── Bottom two columns ─────────────────────────────────────────────
        JPanel cols = new JPanel(new GridLayout(1, 2, 16, 0));
        cols.setBackground(UITheme.CONTENT_BG);

        // Left: Ranking de patrocinadores
        JPanel leftCard = buildCard();
        leftCard.setLayout(new BorderLayout(0, 12));

        JPanel leftTitle = buildCardHeader("Ranking de patrocinadores", Icons.ranking(18));
        leftCard.add(leftTitle, BorderLayout.NORTH);

        rankingPanel = new JPanel();
        rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));
        rankingPanel.setBackground(UITheme.CARD_BG);
        JScrollPane leftScroll = new JScrollPane(rankingPanel);
        leftScroll.setBorder(null);
        leftScroll.getViewport().setBackground(UITheme.CARD_BG);
        leftCard.add(leftScroll, BorderLayout.CENTER);
        cols.add(leftCard);

        // Right: Receita por jogo
        JPanel rightCard = buildCard();
        rightCard.setLayout(new BorderLayout(0, 12));

        JPanel rightTitle = buildCardHeader("Receita por jogo", Icons.bola(18));
        rightCard.add(rightTitle, BorderLayout.NORTH);

        receitaJogoPanel = new JPanel();
        receitaJogoPanel.setLayout(new BoxLayout(receitaJogoPanel, BoxLayout.Y_AXIS));
        receitaJogoPanel.setBackground(UITheme.CARD_BG);
        JScrollPane rightScroll = new JScrollPane(receitaJogoPanel);
        rightScroll.setBorder(null);
        rightScroll.getViewport().setBackground(UITheme.CARD_BG);
        rightCard.add(rightScroll, BorderLayout.CENTER);
        cols.add(rightCard);

        body.add(cols, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        add(new JScrollPane(root) {{
            setBorder(null);
            getViewport().setBackground(UITheme.CONTENT_BG);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        }}, BorderLayout.CENTER);
    }

    // ── Stat card (top row) ───────────────────────────────────────────────────
    private JPanel buildStatCard(String label, JLabel valueLbl, Color valueColor, ImageIcon icon) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 240)),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        // Icon + label row
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topRow.setBackground(UITheme.CARD_BG);
        if (icon != null) topRow.add(new JLabel(icon));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        topRow.add(lbl);
        card.add(topRow, BorderLayout.NORTH);

        // Value
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLbl.setForeground(valueColor);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 240)),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return card;
    }

    private JPanel buildCardHeader(String title, ImageIcon icon) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(UITheme.CARD_BG);
        if (icon != null) row.add(new JLabel(icon));
        JLabel lbl = UITheme.sectionLabel(title);
        row.add(lbl);
        return row;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  REFRESH
    // ═════════════════════════════════════════════════════════════════════════
    public void refresh() {
        DataStore ds = DataStore.getInstance();
        Torneio t    = ds.getTorneioAtual();
        List<Jogo> jogos = t != null ? t.getJogos() : ds.getJogos();

        double recBilh = ds.getTotalReceitaBilheteira();
        double recPat  = ds.getTotalReceitaPatrocinios();
        long terminados = jogos.stream().filter(Jogo::isTerminado).count();

        // ── Stat cards ────────────────────────────────────────────────────
        valTotalLbl.setText(formatEur(recBilh + recPat));
        valBilhLbl.setText(formatEur(recBilh));
        valPatLbl.setText(formatEur(recPat));
        valJogosLbl.setText(String.valueOf(terminados));

        // ── Ranking patrocinadores ─────────────────────────────────────────
        rankingPanel.removeAll();
        List<Patrocinador> pats = new ArrayList<>(ds.getPatrocinadores());
        // Sort by total contract value descending
        pats.sort((a, b) -> Double.compare(
            ds.getContratos().stream().filter(c -> c.getPatrocinador() == b).mapToDouble(ContratoPatrocinio::getValor).sum(),
            ds.getContratos().stream().filter(c -> c.getPatrocinador() == a).mapToDouble(ContratoPatrocinio::getValor).sum()
        ));

        if (pats.isEmpty()) {
            rankingPanel.add(emptyLabel("Sem patrocinadores registados"));
        } else {
            Color[] rankColors = {new Color(255,180,0), new Color(160,165,175), new Color(180,120,60)};
            for (int i = 0; i < Math.min(pats.size(), 10); i++) {
                Patrocinador p = pats.get(i);
                double total = ds.getContratos().stream()
                    .filter(c -> c.getPatrocinador() == p)
                    .mapToDouble(ContratoPatrocinio::getValor).sum();

                rankingPanel.add(buildRankingRow(i + 1, p, total,
                    i < rankColors.length ? rankColors[i] : UITheme.TEXT_SECONDARY));
                rankingPanel.add(Box.createVerticalStrut(6));
            }
        }

        // ── Receita por jogo ───────────────────────────────────────────────
        receitaJogoPanel.removeAll();
        List<Jogo> comReceita = jogos.stream()
            .filter(j -> j.isTerminado() && j.getReceitaBilheteira() > 0)
            .sorted(Comparator.comparingDouble(Jogo::getReceitaBilheteira).reversed())
            .collect(Collectors.toList());

        if (comReceita.isEmpty()) {
            receitaJogoPanel.add(emptyLabel("Sem receitas de bilheteira registadas"));
        } else {
            for (Jogo j : comReceita) {
                receitaJogoPanel.add(buildReceitaRow(j));
                receitaJogoPanel.add(Box.createVerticalStrut(4));
            }
        }

        revalidate(); repaint();
    }

    // ── Ranking row ───────────────────────────────────────────────────────────
    private JPanel buildRankingRow(int rank, Patrocinador p, double total, Color rankColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(UITheme.CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        // Rank number
        JLabel rankLbl = new JLabel("#" + rank);
        rankLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        rankLbl.setForeground(rankColor);
        rankLbl.setPreferredSize(new Dimension(36, 40));
        rankLbl.setHorizontalAlignment(SwingConstants.CENTER);

        // Logo
        ImageIcon logo = loadThumb(p.getFotoPath(), 40, 28);
        JLabel logoLbl = logo != null ? new JLabel(logo) : new JLabel();
        logoLbl.setPreferredSize(new Dimension(50, 36));
        logoLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setBackground(UITheme.CARD_BG);
        left.add(rankLbl); left.add(logoLbl);

        // Name
        JLabel nameLbl = new JLabel(p.getNome());
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);

        // Value
        JLabel valLbl = new JLabel(formatEur(total));
        valLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        valLbl.setForeground(UITheme.TEXT_PRIMARY);

        row.add(left, BorderLayout.WEST);
        row.add(nameLbl, BorderLayout.CENTER);
        row.add(valLbl, BorderLayout.EAST);
        return row;
    }

    // ── Receita por jogo row ──────────────────────────────────────────────────
    private JPanel buildReceitaRow(Jogo j) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UITheme.CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String casa  = j.getEquipaCasa()      != null ? j.getEquipaCasa().getNome()      : "?";
        String visit = j.getEquipaVisitante() != null ? j.getEquipaVisitante().getNome() : "?";
        int gc = j.getGolosTotalCasa(), gv = j.getGolosTotalVisitante();

        JLabel matchLbl = new JLabel(
            String.format("%s  %d  -  %d  %s", casa, gc, gv, visit));
        matchLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        matchLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel recLbl = new JLabel(formatEur(j.getReceitaBilheteira()));
        recLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        recLbl.setForeground(UITheme.TEXT_SECONDARY);

        row.add(matchLbl, BorderLayout.CENTER);
        row.add(recLbl,   BorderLayout.EAST);
        return row;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String formatEur(double v) {
        // Format like "49 400 000,00€"
        if (v == 0) return "0€";
        return String.format("%,.0f€", v).replace(",", " ");
    }

    private JLabel emptyLabel(String txt) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setFont(new Font("SansSerif", Font.ITALIC, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private ImageIcon loadThumb(String path, int w, int h) {
        if (path == null || path.isBlank()) return null;
        try { return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)); }
        catch (Exception e) { return null; }
    }
}
