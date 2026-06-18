package torneoapp.ui;

import torneoapp.model.*;
import torneoapp.service.DataStore;
import torneoapp.util.Icons;
import torneoapp.util.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ResultadosPanel extends JPanel {
    private MainFrame mainFrame;
    private CardLayout innerLayout;
    private JPanel innerContent;

    // sub-views
    private JPanel faseGruposView;
    private JPanel faseEliminacaoView;
    private JPanel leaderboardsView;

    private JButton btnGrupos, btnEliminacao;

    public ResultadosPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(UITheme.CONTENT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ── Top bar ────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(18, 28, 10, 28));

        // Toggle buttons: Fase Grupos | trophy | Fase Eliminação
        JPanel toggle = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        toggle.setBackground(UITheme.CONTENT_BG);

        btnGrupos = roundedToggleBtn("Fase Grupos", true);
        btnEliminacao = roundedToggleBtn("Fase de Eliminação", false);

        ImageIcon tacaIco = Icons.taca(60);
        JLabel tacaLbl = tacaIco != null ? new JLabel(tacaIco) : new JLabel("🏆");

        toggle.add(btnGrupos);
        toggle.add(tacaLbl);
        toggle.add(btnEliminacao);
        topBar.add(toggle, BorderLayout.CENTER);

        // Leaderboards button top-right
        ImageIcon lbIco = Icons.leaderboard(18);
        JButton lbBtn = UITheme.primaryButton("  Leaderboards", lbIco);
        lbBtn.addActionListener(e -> showLeaderboards());
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRight.setBackground(UITheme.CONTENT_BG);
        topRight.add(lbBtn);
        topBar.add(topRight, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ── Inner CardLayout ────────────────────────────────────────────────
        innerLayout = new CardLayout();
        innerContent = new JPanel(innerLayout);
        innerContent.setBackground(UITheme.CONTENT_BG);

        faseGruposView    = new JPanel(new BorderLayout());
        faseGruposView.setBackground(UITheme.CONTENT_BG);
        faseEliminacaoView = new JPanel(new BorderLayout());
        faseEliminacaoView.setBackground(UITheme.CONTENT_BG);
        leaderboardsView  = buildLeaderboardsView();

        innerContent.add(faseGruposView,    "grupos");
        innerContent.add(faseEliminacaoView,"eliminacao");
        innerContent.add(leaderboardsView,  "leaderboards");

        add(innerContent, BorderLayout.CENTER);

        btnGrupos.addActionListener(e -> {
            setToggle(true);
            innerLayout.show(innerContent, "grupos");
        });
        btnEliminacao.addActionListener(e -> {
            setToggle(false);
            innerLayout.show(innerContent, "eliminacao");
        });
    }

    private void showLeaderboards() {
        refreshLeaderboards();
        innerLayout.show(innerContent, "leaderboards");
        btnGrupos.setBackground(new Color(220,225,240));
        btnEliminacao.setBackground(new Color(220,225,240));
    }

    private JButton roundedToggleBtn(String text, boolean selected) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(selected ? Color.WHITE : new Color(220,225,240));
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,208,225), 1, true),
            BorderFactory.createEmptyBorder(10, 28, 10, 28)
        ));
        return btn;
    }

    private void setToggle(boolean gruposSelected) {
        btnGrupos.setBackground(gruposSelected ? Color.WHITE : new Color(220,225,240));
        btnEliminacao.setBackground(gruposSelected ? new Color(220,225,240) : Color.WHITE);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FASE DE GRUPOS
    // ════════════════════════════════════════════════════════════════════════
    private void buildGruposView() {
        faseGruposView.removeAll();
        DataStore ds = DataStore.getInstance();

        List<Jogo> jogosGrupos = ds.getJogosTorneio().stream()
            .filter(j -> "grupos".equals(j.getFase()))
            .collect(Collectors.toList());

        // Group by grupo name
        Map<String, List<Jogo>> porGrupo = new LinkedHashMap<>();
        for (Jogo j : jogosGrupos) {
            String g = j.getRonda().isBlank() ? "Grupo A" : j.getRonda();
            porGrupo.computeIfAbsent(g, k -> new ArrayList<>()).add(j);
        }
        // If no grupos defined yet, create a default group with all games
        if (porGrupo.isEmpty() && !jogosGrupos.isEmpty()) {
            porGrupo.put("Grupo A", jogosGrupos);
        }

        // Classifica + Resultados split
        JPanel top = new JPanel(new GridLayout(0, 4, 10, 10));
        top.setBackground(UITheme.CONTENT_BG);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,210,230), 1, true),
            BorderFactory.createEmptyBorder(12,12,12,12)
        ));
        top.setBackground(new Color(240,242,250));

        if (porGrupo.isEmpty()) {
            // show placeholder
            JLabel ph = new JLabel("Nenhum jogo de fase de grupos registado.", SwingConstants.CENTER);
            ph.setForeground(UITheme.TEXT_SECONDARY);
            top.setLayout(new BorderLayout());
            top.add(ph, BorderLayout.CENTER);
        } else {
            for (Map.Entry<String, List<Jogo>> e : porGrupo.entrySet()) {
                top.add(buildGrupoCard(e.getKey(), e.getValue()));
            }
        }

        // Resultados recentes
        JPanel resultsPanel = buildResultadosRecentes(jogosGrupos);

        JPanel content = new JPanel(new BorderLayout(0,12));
        content.setBackground(UITheme.CONTENT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(0,20,20,20));
        content.add(new JScrollPane(top) {{
            setBorder(null);
            getViewport().setBackground(new Color(240,242,250));
            setPreferredSize(new Dimension(0, 320));
        }}, BorderLayout.NORTH);
        content.add(resultsPanel, BorderLayout.CENTER);

        faseGruposView.add(content, BorderLayout.CENTER);
        faseGruposView.revalidate();
        faseGruposView.repaint();
    }

    private JPanel buildGrupoCard(String nomeGrupo, List<Jogo> jogos) {
        JPanel card = new JPanel(new BorderLayout(0,6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210,218,235), 1),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        JLabel titulo = new JLabel(nomeGrupo);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(UITheme.TEXT_PRIMARY);
        card.add(titulo, BorderLayout.NORTH);

        // collect teams and their stats
        Map<Equipa, int[]> stats = new LinkedHashMap<>(); // [P, DG, G]
        for (Jogo j : jogos) {
            if (j.getEquipaCasa() != null) stats.putIfAbsent(j.getEquipaCasa(), new int[3]);
            if (j.getEquipaVisitante() != null) stats.putIfAbsent(j.getEquipaVisitante(), new int[3]);
            if (j.isTerminado()) {
                int[] c = stats.getOrDefault(j.getEquipaCasa(), new int[3]);
                int[] v = stats.getOrDefault(j.getEquipaVisitante(), new int[3]);
                int gcasa = j.getGolosCasa(), gvisit = j.getGolosVisitante();
                c[1] += gcasa - gvisit; c[2] += gcasa;
                v[1] += gvisit - gcasa; v[2] += gvisit;
                if (gcasa > gvisit) { c[0] += 3; }
                else if (gcasa == gvisit) { c[0] += 1; v[0] += 1; }
                else { v[0] += 3; }
                if (j.getEquipaCasa() != null) stats.put(j.getEquipaCasa(), c);
                if (j.getEquipaVisitante() != null) stats.put(j.getEquipaVisitante(), v);
            }
        }

        String[] cols = {"Equipa","P","DG","G"};
        Object[][] data = stats.entrySet().stream()
            .sorted((a,b) -> b.getValue()[0]-a.getValue()[0])
            .map(e -> new Object[]{e.getKey().getNome(), e.getValue()[0], e.getValue()[1], e.getValue()[2]})
            .toArray(Object[][]::new);

        JTable t = new JTable(data, cols) { public boolean isCellEditable(int r, int c) { return false; } };
        t.setRowHeight(22);
        t.setFont(new Font("SansSerif", Font.PLAIN, 11));
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 10));
        t.getTableHeader().setBackground(new Color(245,247,255));
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0,0));
        t.setFillsViewportHeight(true);
        card.add(new JScrollPane(t) {{ setBorder(null); }}, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildResultadosRecentes(List<Jogo> jogos) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0,8));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(UITheme.CARD_BG);
        JLabel title = UITheme.sectionLabel("Resultados");
        JButton verTudo = new JButton("Ver tudo ›");
        verTudo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        verTudo.setForeground(UITheme.ACCENT);
        verTudo.setBorderPainted(false); verTudo.setContentAreaFilled(false); verTudo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(verTudo, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 8));
        grid.setBackground(UITheme.CARD_BG);
        List<Jogo> terminados = jogos.stream().filter(Jogo::isTerminado).collect(Collectors.toList());
        for (Jogo j : terminados) {
            grid.add(buildResultRow(j, true));
        }
        if (terminados.isEmpty()) {
            JLabel none = new JLabel("Sem resultados", SwingConstants.CENTER);
            none.setForeground(UITheme.TEXT_SECONDARY);
            grid.add(none);
        }
        card.add(new JScrollPane(grid) {{
            setBorder(null); getViewport().setBackground(UITheme.CARD_BG);
            setPreferredSize(new Dimension(0,180));
        }}, BorderLayout.CENTER);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FASE DE ELIMINAÇÃO
    // ════════════════════════════════════════════════════════════════════════
    private void buildEliminacaoView() {
        faseEliminacaoView.removeAll();
        DataStore ds = DataStore.getInstance();

        List<Jogo> elim = ds.getJogosTorneio().stream()
            .filter(j -> "eliminacao".equals(j.getFase()))
            .sorted(Comparator.comparingInt(Jogo::getPosicaoBracket))
            .collect(Collectors.toList());

        JPanel bracketPanel = buildBracket(elim);
        JScrollPane sp = new JScrollPane(bracketPanel);
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0,20,20,20),
            BorderFactory.createLineBorder(new Color(200,210,230), 1, true)
        ));
        sp.getViewport().setBackground(new Color(240,242,250));
        faseEliminacaoView.add(sp, BorderLayout.CENTER);
        faseEliminacaoView.revalidate();
        faseEliminacaoView.repaint();
    }

    private JPanel buildBracket(List<Jogo> elim) {
        // layout: oitavos(8) | quartos(4) | semi(2) | final(1)
        String[] rondas = {"Oitavos","Quartos","Meias-Finais","Final"};
        int[] counts = {8, 4, 2, 1};

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(240,242,250));
        int colW = 180, gap = 12, rowH = 60, padding = 20;
        int totalCols = rondas.length;
        int maxRows = 8;
        panel.setPreferredSize(new Dimension(totalCols * (colW + gap) + padding*2, maxRows * (rowH + gap) + padding*2));

        // group elim jogos by ronda
        Map<String, List<Jogo>> byRonda = new LinkedHashMap<>();
        for (String r : rondas) byRonda.put(r, new ArrayList<>());
        for (Jogo j : elim) {
            String r = mapRonda(j.getRonda());
            byRonda.computeIfAbsent(r, k -> new ArrayList<>()).add(j);
        }

        for (int col = 0; col < rondas.length; col++) {
            String ronda = rondas[col];
            List<Jogo> jogosRonda = byRonda.getOrDefault(ronda, new ArrayList<>());
            int slots = counts[col];
            int spacing = (maxRows * (rowH + gap)) / slots;

            for (int slot = 0; slot < slots; slot++) {
                int x = padding + col * (colW + gap);
                int y = padding + slot * spacing + spacing/2 - rowH/2;
                JPanel matchCard = buildBracketMatch(slot < jogosRonda.size() ? jogosRonda.get(slot) : null);
                matchCard.setBounds(x, y, colW, rowH);
                panel.add(matchCard);

                // connector lines drawn via paintComponent override — skip for now
            }
        }
        return panel;
    }

    private String mapRonda(String r) {
        if (r == null) return "Oitavos";
        return switch (r.toLowerCase()) {
            case "oitavos","oitavos de final","r16" -> "Oitavos";
            case "quartos","quartos de final","qf" -> "Quartos";
            case "semi","meias","semi-finais","meias-finais","sf" -> "Meias-Finais";
            case "final","f" -> "Final";
            default -> "Oitavos";
        };
    }

    private JPanel buildBracketMatch(Jogo j) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230), 1));

        if (j == null) {
            card.add(buildBracketRow("—", -1, false, false, -1));
            card.add(buildBracketRow("—", -1, false, false, -1));
            return card;
        }

        String nomeCasa  = j.getEquipaCasa()       != null ? j.getEquipaCasa().getNome()      : "—";
        String nomeVisit = j.getEquipaVisitante()  != null ? j.getEquipaVisitante().getNome() : "—";

        boolean terminado = j.isTerminado();
        boolean penaltis  = j.isFoiPenaltis();

        // Total goals including extra time
        int totalCasa  = terminado ? j.getGolosTotalCasa()      : -1;
        int totalVisit = terminado ? j.getGolosTotalVisitante() : -1;

        // Winner determination (accounts for penalties)
        Equipa vencedor = terminado ? j.getVencedor() : null;
        boolean casaWins  = vencedor == j.getEquipaCasa();
        boolean visitWins = vencedor == j.getEquipaVisitante();

        // Penalties score for display alongside goals
        int penC = penaltis ? j.getPenaltisCasa()      : -1;
        int penV = penaltis ? j.getPenaltisVisitante() : -1;

        card.add(buildBracketRow(nomeCasa,  totalCasa,  casaWins,  casaWins  && penaltis, penaltis && casaWins  ? penC : (penaltis ? penC : -1)));
        card.add(buildBracketRow(nomeVisit, totalVisit, visitWins, visitWins && penaltis, penaltis && visitWins ? penV : (penaltis ? penV : -1)));

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showJogoStats(j); }
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(245, 248, 255)); }
            public void mouseExited(MouseEvent e)  { card.setBackground(Color.WHITE); }
        });
        return card;
    }

    /**
     * @param nome       team name
     * @param golos      total goals (normal + extra time), -1 if not played
     * @param winner     true if this team won
     * @param wonByPen   true if this team won by penalties (shows *)
     * @param penGolos   penalty shootout score for this team (-1 = don't show)
     */
    private JPanel buildBracketRow(String nome, int golos, boolean winner, boolean wonByPen, int penGolos) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        // Name label — add * if won by penalties
        String displayName = wonByPen ? nome + " *" : nome;
        JLabel nameLbl = new JLabel(displayName);
        nameLbl.setFont(new Font("SansSerif", winner ? Font.BOLD : Font.PLAIN, 11));
        nameLbl.setForeground(wonByPen ? UITheme.SUCCESS
                            : winner   ? UITheme.TEXT_PRIMARY
                                       : UITheme.TEXT_SECONDARY);
        p.add(nameLbl, BorderLayout.CENTER);

        if (golos >= 0) {
            // Right side: goals  [pen]
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            rightPanel.setOpaque(false);

            if (penGolos >= 0) {
                // Show penalty score in parentheses in a smaller muted label
                JLabel penLbl = new JLabel("(" + penGolos + ")", SwingConstants.RIGHT);
                penLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                penLbl.setForeground(new Color(130, 140, 170));
                rightPanel.add(penLbl);
            }

            JLabel gLbl = new JLabel(String.valueOf(golos), SwingConstants.RIGHT);
            gLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            gLbl.setForeground(wonByPen ? UITheme.SUCCESS
                             : winner   ? UITheme.PRIMARY
                                        : UITheme.TEXT_SECONDARY);
            rightPanel.add(gLbl);
            p.add(rightPanel, BorderLayout.EAST);
        }
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ESTATÍSTICAS DE JOGO
    // ════════════════════════════════════════════════════════════════════════
    public void showJogoStats(Jogo j) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Estatísticas do Jogo", true);
        dlg.setSize(700, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel main = new JPanel(new BorderLayout(0,0));
        main.setBackground(UITheme.CONTENT_BG);

        // Back button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        JButton back = new JButton("◀");
        back.setFont(new Font("SansSerif", Font.BOLD, 16));
        back.setForeground(UITheme.TEXT_PRIMARY);
        back.setBorderPainted(false); back.setContentAreaFilled(false); back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> dlg.dispose());
        topBar.add(back, BorderLayout.EAST);
        main.add(topBar, BorderLayout.NORTH);

        // Score header
        JPanel scorePanel = buildScoreHeader(j);
        main.add(scorePanel, BorderLayout.CENTER);

        // Stats body (editable if not terminado or always allow editing)
        JPanel statsBody = buildStatsBody(j, dlg);
        main.add(statsBody, BorderLayout.SOUTH);

        dlg.add(main);
        dlg.setVisible(true);
    }

    private JPanel buildScoreHeader(Jogo j) {
        JPanel p = new JPanel(new GridLayout(1,3));
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20,30,10,30));

        String casa  = j.getEquipaCasa() != null ? j.getEquipaCasa().getNome() : "?";
        String visit = j.getEquipaVisitante() != null ? j.getEquipaVisitante().getNome() : "?";

        // left: casa
        JPanel leftP = new JPanel(new BorderLayout(0,6));
        leftP.setBackground(UITheme.CONTENT_BG);
        JLabel casaLbl = new JLabel(casa, SwingConstants.CENTER);
        casaLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        // photo
        ImageIcon caIcon = j.getEquipaCasa() != null ? UITheme.loadPhotoCircle(j.getEquipaCasa().getFotoPath(), 60) : null;
        JLabel caImg = caIcon != null ? new JLabel(caIcon, SwingConstants.CENTER) : new JLabel(Icons.equipa(60), SwingConstants.CENTER);
        leftP.add(caImg, BorderLayout.CENTER);
        leftP.add(casaLbl, BorderLayout.SOUTH);

        // center: score + info
        JPanel centerP = new JPanel(new BorderLayout(0,4));
        centerP.setBackground(UITheme.CONTENT_BG);
        String dateStr = j.getDataHora() != null ? j.getDataHora().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "";
        JLabel dateLbl = new JLabel(dateStr, SwingConstants.CENTER);
        dateLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dateLbl.setForeground(UITheme.TEXT_SECONDARY);
        JLabel scoreLbl = new JLabel(j.isTerminado() ? j.getGolosCasa()+" - "+j.getGolosVisitante() : "- vs -", SwingConstants.CENTER);
        scoreLbl.setFont(new Font("SansSerif", Font.BOLD, 38));
        scoreLbl.setForeground(UITheme.TEXT_PRIMARY);
        JLabel statusLbl = new JLabel(j.isTerminado() ? "Terminado" : "Agendado", SwingConstants.CENTER);
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLbl.setForeground(j.isTerminado() ? UITheme.TEXT_SECONDARY : UITheme.ACCENT);
        String estNome = j.getEstadio() != null ? "🏟 "+j.getEstadio().getNome() : "";
        JLabel estLbl = new JLabel(estNome, SwingConstants.CENTER);
        estLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        estLbl.setForeground(UITheme.TEXT_SECONDARY);
        centerP.add(dateLbl, BorderLayout.NORTH);
        centerP.add(scoreLbl, BorderLayout.CENTER);
        JPanel sub = new JPanel(new GridLayout(2,1));
        sub.setBackground(UITheme.CONTENT_BG);
        sub.add(statusLbl); sub.add(estLbl);
        centerP.add(sub, BorderLayout.SOUTH);

        // right: visitante
        JPanel rightP = new JPanel(new BorderLayout(0,6));
        rightP.setBackground(UITheme.CONTENT_BG);
        JLabel visitLbl = new JLabel(visit, SwingConstants.CENTER);
        visitLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        ImageIcon viIcon = j.getEquipaVisitante() != null ? UITheme.loadPhotoCircle(j.getEquipaVisitante().getFotoPath(), 60) : null;
        JLabel viImg = viIcon != null ? new JLabel(viIcon, SwingConstants.CENTER) : new JLabel(Icons.equipa(60), SwingConstants.CENTER);
        rightP.add(viImg, BorderLayout.CENTER);
        rightP.add(visitLbl, BorderLayout.SOUTH);

        p.add(leftP); p.add(centerP); p.add(rightP);
        return p;
    }

    private JPanel buildStatsBody(Jogo j, JDialog dlg) {
        JPanel outer = new JPanel(new BorderLayout(0,0));
        outer.setBackground(UITheme.CARD_BG);
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0, new Color(220,225,240)),
            BorderFactory.createEmptyBorder(16,30,16,30)
        ));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(4,6,4,6);

        String casa  = j.getEquipaCasa() != null ? j.getEquipaCasa().getNome() : "Casa";
        String visit = j.getEquipaVisitante() != null ? j.getEquipaVisitante().getNome() : "Visitante";
        Color corCasa = new Color(220,50,50);
        Color corVisit = new Color(50,120,220);

        // headers
        g.gridy=0; g.gridx=0; g.weightx=0.15; form.add(new JLabel(casa, SwingConstants.RIGHT){{setFont(new Font("SansSerif",Font.BOLD,12));setForeground(corCasa);}}, g);
        g.gridx=1; g.weightx=0.7; form.add(new JLabel("", SwingConstants.CENTER), g);
        g.gridx=2; g.weightx=0.15; form.add(new JLabel(visit, SwingConstants.LEFT){{setFont(new Font("SansSerif",Font.BOLD,12));setForeground(corVisit);}}, g);

        // stat bars (read-only)
        addStatBar(form, "Posse de Bola", j.getPosseCasa(), j.getPosseVisitante(), corCasa, corVisit, 1);
        addStatBar(form, "Cantos", j.getCantosCasa(), j.getCantosVisitante(), corCasa, corVisit, 2);
        addStatBar(form, "Cartões Amarelos", j.getCartoesAmarelosCasa(), j.getCartoesAmarelosVisitante(), corCasa, corVisit, 3);
        addStatBar(form, "Cartões Vermelhos", j.getCartoesVermelhosCasa(), j.getCartoesVermelhosVisitante(), corCasa, corVisit, 4);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private void addStatBar(JPanel form, String label, double valCasa, double valVisit, Color cc, Color cv, int row) {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(4,6,4,6);
        g.gridy = row;
        g.gridx=0; g.weightx=0.15;
        JLabel vC = new JLabel(formatVal(valCasa), SwingConstants.RIGHT);
        vC.setFont(new Font("SansSerif",Font.BOLD,13));
        form.add(vC, g);
        g.gridx=1; g.weightx=0.7;
        form.add(UITheme.statBar(label, valCasa, valVisit, cc, cv), g);
        g.gridx=2; g.weightx=0.15;
        JLabel vV = new JLabel(formatVal(valVisit), SwingConstants.LEFT);
        vV.setFont(new Font("SansSerif",Font.BOLD,13));
        form.add(vV, g);
    }

    private String formatVal(double v) { return v == (int)v ? String.valueOf((int)v) : String.format("%.1f",v); }

    // ════════════════════════════════════════════════════════════════════════
    //  INSERIR RESULTADO
    // ════════════════════════════════════════════════════════════════════════
    public void showInserirResultadoDialog(Jogo j) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        ResultadoDialog dlg = new ResultadoDialog(parent, j, this::refresh);
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LEADERBOARDS
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildLeaderboardsView() {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(UITheme.CONTENT_BG);
        view.setName("leaderboardsView");
        return view;
    }

    private void refreshLeaderboards() {
        leaderboardsView.removeAll();
        DataStore ds = DataStore.getInstance();

        // Back button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.CONTENT_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        JButton back = new JButton("◀ Voltar");
        back.setFont(new Font("SansSerif",Font.BOLD,13));
        back.setForeground(UITheme.ACCENT);
        back.setBorderPainted(false); back.setContentAreaFilled(false); back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> { setToggle(true); innerLayout.show(innerContent,"grupos"); });
        topBar.add(back, BorderLayout.EAST);
        leaderboardsView.add(topBar, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2,2,16,16));
        grid.setBackground(UITheme.CONTENT_BG);
        grid.setBorder(BorderFactory.createEmptyBorder(4,20,20,20));

        List<Jogador> todos = ds.getJogadoresTorneio();
        grid.add(buildLeaderCard("Melhor Marcador", todos.stream().sorted((a,b)->b.getGolos()-a.getGolos()).limit(3).collect(Collectors.toList()),
            j->j.getGolos()+" Golos", Icons.bolaShoot(36), new Color(255,200,0)));
        grid.add(buildLeaderCard("Mais Assistências", todos.stream().sorted((a,b)->b.getAssistencias()-a.getAssistencias()).limit(3).collect(Collectors.toList()),
            j->j.getAssistencias()+" Assistências", Icons.assist(36), new Color(255,200,0)));
        grid.add(buildLeaderCard("Mais Amarelos", todos.stream().sorted((a,b)->b.getCartoesAmarelos()-a.getCartoesAmarelos()).limit(3).collect(Collectors.toList()),
            j->j.getCartoesAmarelos()+" Amarelos", null, UITheme.YELLOW));
        grid.add(buildLeaderCard("Mais Vermelhos", todos.stream().sorted((a,b)->b.getCartoesVermelhos()-a.getCartoesVermelhos()).limit(3).collect(Collectors.toList()),
            j->j.getCartoesVermelhos()+" Vermelho(s)", null, UITheme.DANGER));

        leaderboardsView.add(new JScrollPane(grid){{setBorder(null);getViewport().setBackground(UITheme.CONTENT_BG);}}, BorderLayout.CENTER);
        leaderboardsView.revalidate(); leaderboardsView.repaint();
    }

    private JPanel buildLeaderCard(String title, List<Jogador> list, java.util.function.Function<Jogador,String> valFn, ImageIcon icon, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout(0,10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210,218,235),1,true),
            BorderFactory.createEmptyBorder(16,16,16,16)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif",Font.BOLD,14));
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);
        card.add(titleLbl, BorderLayout.NORTH);

        if (!list.isEmpty()) {
            Jogador top = list.get(0);
            JPanel topArea = new JPanel(new BorderLayout(12,0));
            topArea.setBackground(new Color(248,249,255));
            topArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            JLabel rankLbl = new JLabel("#1");
            rankLbl.setFont(new Font("SansSerif",Font.BOLD,20));
            rankLbl.setForeground(new Color(200,155,0));

            JPanel iconArea = new JPanel(new BorderLayout());
            iconArea.setBackground(new Color(248,249,255));
            if (icon != null) { iconArea.add(new JLabel(icon, SwingConstants.CENTER), BorderLayout.CENTER); }
            else {
                // color card (yellow or red)
                JPanel card2 = new JPanel();
                card2.setBackground(iconColor);
                card2.setPreferredSize(new Dimension(28,36));
                iconArea.add(card2, BorderLayout.CENTER);
            }

            // photo
            ImageIcon fotoIco = UITheme.loadPhotoCircle(top.getFotoPath(), 80);
            JLabel fotoLbl = fotoIco != null ? new JLabel(fotoIco) : new JLabel(Icons.jogador(80));
            fotoLbl.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel info = new JPanel(new BorderLayout(0,4));
            info.setBackground(new Color(248,249,255));
            JLabel nameLbl = new JLabel("<html><b>"+top.toString().replace(" ","<br>")+"</b></html>");
            nameLbl.setFont(new Font("SansSerif",Font.BOLD,14));
            JLabel valLbl = new JLabel(valFn.apply(top));
            valLbl.setFont(new Font("SansSerif",Font.BOLD,18));
            valLbl.setForeground(UITheme.PRIMARY);
            // equipa icon
            ImageIcon equipaIco = top.getEquipa() != null ? UITheme.loadPhotoCircle(top.getEquipa().getFotoPath(), 24) : null;
            JLabel equipaLbl = equipaIco != null ? new JLabel(equipaIco) : new JLabel(Icons.equipa(24));

            info.add(nameLbl, BorderLayout.CENTER);
            info.add(valLbl, BorderLayout.SOUTH);

            topArea.add(rankLbl, BorderLayout.WEST);
            topArea.add(fotoLbl, BorderLayout.CENTER);
            JPanel rightArea = new JPanel(new BorderLayout(0,4));
            rightArea.setBackground(new Color(248,249,255));
            rightArea.add(iconArea, BorderLayout.NORTH);
            rightArea.add(equipaLbl, BorderLayout.SOUTH);
            topArea.add(rightArea, BorderLayout.EAST);

            JPanel bottom = new JPanel(new GridLayout(0,1,0,4));
            bottom.setBackground(Color.WHITE);
            for (int i=1;i<list.size();i++) {
                Jogador jj = list.get(i);
                JPanel row = new JPanel(new BorderLayout(8,0));
                row.setBackground(Color.WHITE);
                JLabel r = new JLabel("#"+(i+1));
                r.setFont(new Font("SansSerif",Font.BOLD,12)); r.setForeground(UITheme.TEXT_SECONDARY);
                r.setPreferredSize(new Dimension(28,20));
                JLabel n = new JLabel(jj.toString()); n.setFont(new Font("SansSerif",Font.PLAIN,13));
                JLabel v = new JLabel("- "+valFn.apply(jj)); v.setFont(new Font("SansSerif",Font.BOLD,12)); v.setForeground(UITheme.ACCENT);
                row.add(r,BorderLayout.WEST); row.add(n,BorderLayout.CENTER); row.add(v,BorderLayout.EAST);
                bottom.add(row);
            }

            JPanel body = new JPanel(new BorderLayout(0,8));
            body.setBackground(Color.WHITE);
            body.add(topArea, BorderLayout.NORTH);
            body.add(info, BorderLayout.CENTER);
            body.add(bottom, BorderLayout.SOUTH);
            card.add(body, BorderLayout.CENTER);
        } else {
            JLabel none = new JLabel("Sem dados", SwingConstants.CENTER);
            none.setForeground(UITheme.TEXT_SECONDARY);
            card.add(none, BorderLayout.CENTER);
        }
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildResultRow(Jogo j, boolean clickable) {
        JPanel row = new JPanel(new GridLayout(1,3));
        row.setBackground(new Color(248,252,248));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210,235,210)),
            BorderFactory.createEmptyBorder(8,10,8,10)
        ));

        String grupo = j.getRonda().isBlank() ? "" : "["+j.getRonda()+"] ";
        JLabel casaL = new JLabel(grupo+j.getEquipaCasa().getNome(), SwingConstants.RIGHT);
        casaL.setFont(new Font("SansSerif",Font.BOLD,12));
        JLabel score = new JLabel(j.getGolosCasa()+" - "+j.getGolosVisitante(), SwingConstants.CENTER);
        score.setFont(new Font("SansSerif",Font.BOLD,15)); score.setForeground(UITheme.PRIMARY);
        JLabel visitL = new JLabel(j.getEquipaVisitante().getNome(), SwingConstants.LEFT);
        visitL.setFont(new Font("SansSerif",Font.BOLD,12));
        row.add(casaL); row.add(score); row.add(visitL);

        if (clickable) {
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { showJogoStats(j); }
                public void mouseEntered(MouseEvent e) { row.setBackground(new Color(230,248,230)); }
                public void mouseExited(MouseEvent e) { row.setBackground(new Color(248,252,248)); }
            });
        }
        return row;
    }

    public void refresh() {
        buildGruposView();
        buildEliminacaoView();
    }
}
