package torneoapp.ui;

import torneoapp.model.*;
import torneoapp.service.DataStore;
import torneoapp.util.UITheme;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo de resultado.
 * - Resultado calculado pelos eventos de golo registados.
 * - Eliminação: se empate ao "Terminar Tempo Normal" → avança para Prolongamento.
 *   Se ainda empate ao "Terminar Prolongamento" → avança para Penaltis.
 * - Prolongamento e Penaltis têm o mesmo estilo de registo que o tempo normal.
 */
public class ResultadoDialog extends JDialog {

    private final Jogo jogo;
    private final boolean isEliminacao;
    private Runnable onSaved;

    // Fase atual: "normal" | "prolongamento" | "penaltis" | "terminado"
    private String fase = "normal";

    // Models para cada tipo de evento (tempo normal)
    private DefaultListModel<String> golosCasaModel, golosVisitModel;
    private DefaultListModel<String> assistCasaModel, assistVisitModel;
    private DefaultListModel<String> amarCasaModel, amarVisitModel;
    private DefaultListModel<String> vermCasaModel, vermVisitModel;

    // Models prolongamento
    private DefaultListModel<String> prolCasaModel, prolVisitModel;

    // Penaltis (só contagem)
    private JTextField penCasaF, penVisitF;

    private List<Jogador> jogadoresCasa  = new ArrayList<>();
    private List<Jogador> jogadoresVisit = new ArrayList<>();

    // Estatísticas
    private JTextField posseCasaF, posseVisitF, cantosCasaF, cantosVisitF;

    // Score bar
    private JLabel scoreLbl, faseLbl;
    private JTabbedPane tabs;

    // Bottom action button changes per phase
    private JButton actionBtn;
    private JButton cancelBtn;

    public ResultadoDialog(Frame parent, Jogo jogo, Runnable onSaved) {
        super(parent, "Resultado — " + jogo, true);
        this.jogo = jogo;
        this.onSaved = onSaved;
        this.isEliminacao = "eliminacao".equals(jogo.getFase());

        if (jogo.getDataHora() == null) {
            JOptionPane.showMessageDialog(parent,
                "Apenas é possível registar dados de eventos já ocorridos e calendarizados.",
                "Erro", JOptionPane.ERROR_MESSAGE);
            dispose(); return;
        }

        // Restore phase if game was already in progress
        if (jogo.isFoiPenaltis()) fase = "terminado";
        else if (jogo.isFoiProlongamento()) fase = "prolongamento";
        else fase = "normal";

        setSize(860, 680);
        setLocationRelativeTo(parent);
        setResizable(true);

        if (jogo.getEquipaCasa()      != null) jogadoresCasa  = jogo.getEquipaCasa().getJogadores();
        if (jogo.getEquipaVisitante() != null) jogadoresVisit = jogo.getEquipaVisitante().getJogadores();

        initModels();
        buildUI();
        loadExistingData();
        updatePhaseUI();
    }

    private void initModels() {
        golosCasaModel  = new DefaultListModel<>(); golosVisitModel  = new DefaultListModel<>();
        assistCasaModel = new DefaultListModel<>(); assistVisitModel = new DefaultListModel<>();
        amarCasaModel   = new DefaultListModel<>(); amarVisitModel   = new DefaultListModel<>();
        vermCasaModel   = new DefaultListModel<>(); vermVisitModel   = new DefaultListModel<>();
        prolCasaModel   = new DefaultListModel<>(); prolVisitModel   = new DefaultListModel<>();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ═══════════════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.CONTENT_BG);

        String nomeCasa  = jogo.getEquipaCasa()      != null ? jogo.getEquipaCasa().getNome()      : "Casa";
        String nomeVisit = jogo.getEquipaVisitante() != null ? jogo.getEquipaVisitante().getNome() : "Visitante";

        // ── Score + phase header ─────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        JPanel scoreRow = new JPanel(new GridLayout(1, 3));
        scoreRow.setBackground(UITheme.PRIMARY_DARK);

        JLabel casaLbl = new JLabel(nomeCasa, SwingConstants.CENTER);
        casaLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        casaLbl.setForeground(new Color(220, 100, 100));

        scoreLbl = new JLabel("0 — 0", SwingConstants.CENTER);
        scoreLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        scoreLbl.setForeground(Color.WHITE);

        JLabel visitLbl = new JLabel(nomeVisit, SwingConstants.CENTER);
        visitLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        visitLbl.setForeground(new Color(100, 160, 255));

        scoreRow.add(casaLbl); scoreRow.add(scoreLbl); scoreRow.add(visitLbl);

        faseLbl = new JLabel("⏱ Tempo Normal (90 min)", SwingConstants.CENTER);
        faseLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        faseLbl.setForeground(new Color(160, 185, 230));

        header.add(scoreRow, BorderLayout.CENTER);
        header.add(faseLbl, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // ── Tabs ─────────────────────────────────────────────────────────
        tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabs.addTab("⚽ Golos",         buildEventoTab("Golo",        nomeCasa, nomeVisit, jogadoresCasa, jogadoresVisit, golosCasaModel,  golosVisitModel,  true));
        tabs.addTab("🅰 Assistências",  buildEventoTab("Assistência", nomeCasa, nomeVisit, jogadoresCasa, jogadoresVisit, assistCasaModel, assistVisitModel, false));
        tabs.addTab("🟨 Amarelos",      buildEventoTab("Amarelo",     nomeCasa, nomeVisit, jogadoresCasa, jogadoresVisit, amarCasaModel,   amarVisitModel,   false));
        tabs.addTab("🟥 Vermelhos",     buildEventoTab("Vermelho",    nomeCasa, nomeVisit, jogadoresCasa, jogadoresVisit, vermCasaModel,   vermVisitModel,   false));
        tabs.addTab("📊 Estatísticas",  buildEstatisticasTab(nomeCasa, nomeVisit));

        if (isEliminacao) {
            tabs.addTab("⏱ Prolongamento", buildEventoTab("Golo", nomeCasa, nomeVisit, jogadoresCasa, jogadoresVisit, prolCasaModel, prolVisitModel, true));
            tabs.addTab("🥅 Penaltis",     buildPenaltisTab(nomeCasa, nomeVisit));
        }

        add(tabs, BorderLayout.CENTER);

        // ── Bottom bar ────────────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setBackground(UITheme.CONTENT_BG);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 240)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        cancelBtn = UITheme.grayButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveOnlyBtn = UITheme.grayButton("💾 Guardar");
        saveOnlyBtn.setToolTipText("Guarda os dados sem terminar o jogo");
        saveOnlyBtn.addActionListener(e -> guardarSemTerminar());

        actionBtn = UITheme.primaryButton("Terminar Tempo Normal →");
        actionBtn.addActionListener(e -> handleAction());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.CONTENT_BG);
        btnRow.add(cancelBtn); btnRow.add(saveOnlyBtn); btnRow.add(actionBtn);
        bottom.add(btnRow, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  EVENTO TAB (reused for normal goals, assists, cards AND prolongamento)
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildEventoTab(String tipo, String nomeCasa, String nomeVisit,
            List<Jogador> jogCasa, List<Jogador> jogVisit,
            DefaultListModel<String> casaModel, DefaultListModel<String> visitModel,
            boolean updatesScore) {
        boolean isGolo = "Golo".equals(tipo);
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        // For goal tabs pass opposing model so auto-golos credit the right team
        p.add(buildEquipaPanel(tipo, nomeCasa, jogCasa, casaModel, isGolo ? visitModel : null, new Color(200,60,60), updatesScore));
        p.add(buildEquipaPanel(tipo, nomeVisit, jogVisit, visitModel, isGolo ? casaModel : null, new Color(50,100,210), updatesScore));
        return p;
    }

    private JPanel buildEquipaPanel(String tipo, String nomeEquipa,
            List<Jogador> jogadores, DefaultListModel<String> model,
            DefaultListModel<String> opposingModel,
            Color teamColor, boolean updatesScore) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 240)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel header = new JLabel(nomeEquipa, SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setForeground(teamColor);
        p.add(header, BorderLayout.NORTH);

        JList<String> list = new JList<>(model);
        list.setFont(new Font("SansSerif", Font.PLAIN, 13));
        list.setFixedCellHeight(26);
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 240)));
        sp.setPreferredSize(new Dimension(0, 180));
        p.add(sp, BorderLayout.CENTER);

        // Auto-golo checkbox (only for goal tabs where opposingModel != null)
        final JCheckBox autoGoloChk;
        int rows = 2;
        if (opposingModel != null) {
            autoGoloChk = new JCheckBox("Auto-golo (conta para o adversário)");
            autoGoloChk.setFont(new Font("SansSerif", Font.ITALIC, 11));
            autoGoloChk.setBackground(UITheme.CARD_BG);
            autoGoloChk.setForeground(new Color(180, 80, 0));
            rows = 3;
        } else {
            autoGoloChk = null;
        }

        JPanel addRow = new JPanel(new GridLayout(rows, 1, 0, 4));
        addRow.setBackground(UITheme.CARD_BG);

        JComboBox<String> playerCombo = new JComboBox<>();
        playerCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        playerCombo.addItem("— Selecionar Jogador —");
        jogadores.forEach(j -> playerCombo.addItem(j.getNumeroCamisola() + " · " + j.toString()));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setBackground(UITheme.CARD_BG);
        JTextField minF = UITheme.formField(""); minF.setColumns(4);
        JButton addBtn = UITheme.primaryButton("+");
        JButton remBtn = UITheme.dangerButton("−");
        btnRow.add(UITheme.formLabel("Min:")); btnRow.add(minF);
        btnRow.add(addBtn); btnRow.add(remBtn);

        addBtn.addActionListener(e -> {
            if (playerCombo.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Selecione um jogador.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String min = minF.getText().trim().isEmpty() ? "?" : minF.getText().trim();
            boolean ag = autoGoloChk != null && autoGoloChk.isSelected();
            if (ag && opposingModel != null) {
                opposingModel.addElement(min + "' — " + playerCombo.getSelectedItem() + " (AG)");
            } else {
                model.addElement(min + "' — " + playerCombo.getSelectedItem());
            }
            minF.setText("");
            if (autoGoloChk != null) autoGoloChk.setSelected(false);
            if (updatesScore) updateScore();
        });
        remBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) { model.remove(idx); if (updatesScore) updateScore(); }
        });

        addRow.add(playerCombo);
        if (autoGoloChk != null) addRow.add(autoGoloChk);
        addRow.add(btnRow);
        p.add(addRow, BorderLayout.SOUTH);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PENALTIS TAB — just two counters
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildPenaltisTab(String nomeCasa, String nomeVisit) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10); g.fill = GridBagConstraints.HORIZONTAL;

        JLabel info = new JLabel("<html><center>Introduza o resultado da série de penaltis.<br>"
            + "Apenas o número de penaltis convertidos por cada equipa.</center></html>", SwingConstants.CENTER);
        info.setFont(new Font("SansSerif", Font.ITALIC, 12));
        info.setForeground(UITheme.TEXT_SECONDARY);
        g.gridx=0; g.gridy=0; g.gridwidth=3; p.add(info, g); g.gridwidth=1;

        penCasaF  = buildBigField(String.valueOf(jogo.getPenaltisCasa()));
        penVisitF = buildBigField(String.valueOf(jogo.getPenaltisVisitante()));

        JLabel dashLbl = new JLabel("—", SwingConstants.CENTER);
        dashLbl.setFont(new Font("SansSerif", Font.BOLD, 32));

        JLabel cLbl = new JLabel(nomeCasa,  SwingConstants.CENTER); cLbl.setFont(new Font("SansSerif",Font.BOLD,13)); cLbl.setForeground(new Color(200,60,60));
        JLabel vLbl = new JLabel(nomeVisit, SwingConstants.CENTER); vLbl.setFont(new Font("SansSerif",Font.BOLD,13)); vLbl.setForeground(new Color(50,100,210));

        g.gridy=1; g.gridx=0; g.weightx=0.4; p.add(cLbl, g);
        g.gridx=1; g.weightx=0.2; p.add(new JLabel("",SwingConstants.CENTER), g);
        g.gridx=2; g.weightx=0.4; p.add(vLbl, g);

        g.gridy=2; g.gridx=0; p.add(penCasaF,  g);
        g.gridx=1; p.add(dashLbl, g);
        g.gridx=2; p.add(penVisitF, g);

        // live score update
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateScore(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateScore(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        };
        penCasaF.getDocument().addDocumentListener(dl);
        penVisitF.getDocument().addDocumentListener(dl);

        return p;
    }

    private JTextField buildBigField(String val) {
        JTextField tf = UITheme.formField(val);
        tf.setText(val);
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setFont(new Font("SansSerif", Font.BOLD, 28));
        return tf;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ESTATÍSTICAS TAB
    // ═══════════════════════════════════════════════════════════════════
    private JPanel buildEstatisticasTab(String nomeCasa, String nomeVisit) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 50, 24, 50));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(10, 8, 10, 8);

        posseCasaF   = UITheme.formField(""); posseCasaF.setText(String.valueOf((int)jogo.getPosseCasa()));
        posseVisitF  = UITheme.formField(""); posseVisitF.setText(String.valueOf((int)jogo.getPosseVisitante()));

        // Posse auto-sync: shared flag prevents re-entrant loops
        boolean[] posseSyncing = {false};
        posseCasaF.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void sync() {
                if (posseSyncing[0]) return;
                posseSyncing[0] = true;
                try { int v = Integer.parseInt(posseCasaF.getText().trim());
                    if (v >= 0 && v <= 100) posseVisitF.setText(String.valueOf(100 - v));
                } catch (NumberFormatException ignored) {} finally { posseSyncing[0] = false; }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { sync(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { sync(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        posseVisitF.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void sync() {
                if (posseSyncing[0]) return;
                posseSyncing[0] = true;
                try { int v = Integer.parseInt(posseVisitF.getText().trim());
                    if (v >= 0 && v <= 100) posseCasaF.setText(String.valueOf(100 - v));
                } catch (NumberFormatException ignored) {} finally { posseSyncing[0] = false; }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { sync(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { sync(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        cantosCasaF  = UITheme.formField(""); cantosCasaF.setText(String.valueOf(jogo.getCantosCasa()));
        cantosVisitF = UITheme.formField(""); cantosVisitF.setText(String.valueOf(jogo.getCantosVisitante()));

        JLabel cH = new JLabel(nomeCasa, SwingConstants.CENTER); cH.setFont(new Font("SansSerif",Font.BOLD,12)); cH.setForeground(new Color(200,60,60));
        JLabel vH = new JLabel(nomeVisit,SwingConstants.CENTER); vH.setFont(new Font("SansSerif",Font.BOLD,12)); vH.setForeground(new Color(50,100,210));

        g.gridy=0; g.gridx=0;g.weightx=0.35; p.add(cH,g);
        g.gridx=1;g.weightx=0.3; p.add(UITheme.sectionLabel("Estatística"),g);
        g.gridx=2;g.weightx=0.35; p.add(vH,g);

        addStatRow(p,g,1,"Posse de Bola (%)",posseCasaF,posseVisitF);
        addStatRow(p,g,2,"Cantos",cantosCasaF,cantosVisitF);
        return p;
    }

    private void addStatRow(JPanel p, GridBagConstraints g, int row, String label, JTextField l, JTextField r) {
        g.gridwidth=1; g.gridy=row;
        g.gridx=0; p.add(l,g);
        g.gridx=1; JLabel lbl=new JLabel(label,SwingConstants.CENTER); lbl.setFont(new Font("SansSerif",Font.PLAIN,13)); lbl.setForeground(UITheme.TEXT_SECONDARY); p.add(lbl,g);
        g.gridx=2; p.add(r,g);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SCORE UPDATE
    // ═══════════════════════════════════════════════════════════════════
    private void updateScore() {
        if (scoreLbl == null) return;
        int gc = golosCasaModel.size();
        int gv = golosVisitModel.size();
        int pc = prolCasaModel.size();
        int pv = prolVisitModel.size();

        switch (fase) {
            case "normal" -> scoreLbl.setText(gc + " — " + gv);
            case "prolongamento" -> {
                int tc = gc+pc, tv = gv+pv;
                scoreLbl.setText(tc + " — " + tv + "  (90': "+gc+"-"+gv+")");
            }
            case "penaltis" -> {
                int tc = gc+pc, tv = gv+pv;
                int penC = safeInt(penCasaF), penV = safeInt(penVisitF);
                scoreLbl.setText(tc+"-"+tv+"  🥅 "+penC+"-"+penV);
            }
            case "terminado" -> scoreLbl.setText(buildFinalScore());
        }
    }

    private String buildFinalScore() {
        int gc=golosCasaModel.size(), gv=golosVisitModel.size();
        int pc=prolCasaModel.size(), pv=prolVisitModel.size();
        int tc=gc+pc, tv=gv+pv;
        String s = tc + " — " + tv;
        if (jogo.isFoiProlongamento()) s += "  (p.e.)";
        if (jogo.isFoiPenaltis()) s += "  🥅 "+safeInt(penCasaF)+"-"+safeInt(penVisitF);
        return s;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PHASE TRANSITIONS
    // ═══════════════════════════════════════════════════════════════════
    private void updatePhaseUI() {
        switch (fase) {
            case "normal" -> {
                faseLbl.setText("⏱ Tempo Normal (0–90 min)");
                actionBtn.setText("Terminar Tempo Normal →");
                actionBtn.setBackground(UITheme.ACCENT);
                // Disable prol/pen tabs if exist
                if (isEliminacao) { setTabEnabled(5, false); setTabEnabled(6, false); }
            }
            case "prolongamento" -> {
                faseLbl.setText("⏱ Prolongamento (90–120 min) — Empate no tempo normal");
                faseLbl.setForeground(new Color(255, 200, 80));
                actionBtn.setText("Terminar Prolongamento →");
                actionBtn.setBackground(new Color(200, 150, 0));
                if (isEliminacao) { setTabEnabled(5, true); setTabEnabled(6, false); tabs.setSelectedIndex(5); }
            }
            case "penaltis" -> {
                faseLbl.setText("🥅 Penaltis — Empate após prolongamento");
                faseLbl.setForeground(new Color(255, 120, 120));
                actionBtn.setText("Guardar Resultado Final ✓");
                actionBtn.setBackground(new Color(50, 200, 70));
                if (isEliminacao) { setTabEnabled(5, true); setTabEnabled(6, true); tabs.setSelectedIndex(6); }
            }
            case "terminado" -> {
                faseLbl.setText("✅ Jogo Terminado");
                faseLbl.setForeground(new Color(100, 220, 130));
                actionBtn.setText("Guardar Alterações ✓");
                actionBtn.setBackground(UITheme.SUCCESS);
                if (isEliminacao) { setTabEnabled(5, jogo.isFoiProlongamento()); setTabEnabled(6, jogo.isFoiPenaltis()); }
            }
        }
        updateScore();
    }

    private void setTabEnabled(int idx, boolean enabled) {
        if (tabs != null && idx < tabs.getTabCount()) tabs.setEnabledAt(idx, enabled);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  MAIN ACTION BUTTON
    // ═══════════════════════════════════════════════════════════════════
    private void handleAction() {
        switch (fase) {
            case "normal"        -> terminarTempoNormal();
            case "prolongamento" -> terminarProlongamento();
            case "penaltis", "terminado" -> guardar();
        }
    }

    private void terminarTempoNormal() {
        int gc = golosCasaModel.size();
        int gv = golosVisitModel.size();

        if (isEliminacao && gc == gv) {
            // Empate → prolongamento
            JOptionPane.showMessageDialog(this,
                "⏱ Empate (" + gc + "–" + gv + ") ao fim do tempo normal!\n\nA seguir para o Prolongamento (90'–120').",
                "Empate — Prolongamento", JOptionPane.INFORMATION_MESSAGE);
            fase = "prolongamento";
            jogo.setFoiProlongamento(true);
            updatePhaseUI();
        } else {
            // Há vencedor ou grupos
            fase = "terminado";
            updatePhaseUI();
        }
    }

    private void terminarProlongamento() {
        int gc = golosCasaModel.size()  + prolCasaModel.size();
        int gv = golosVisitModel.size() + prolVisitModel.size();

        if (gc == gv) {
            // Ainda empatado → penaltis
            JOptionPane.showMessageDialog(this,
                "🥅 Ainda empatado (" + gc + "–" + gv + ") ao fim do prolongamento!\n\nA seguir para os Penaltis.",
                "Empate — Penaltis", JOptionPane.INFORMATION_MESSAGE);
            fase = "penaltis";
            jogo.setFoiPenaltis(true);
            updatePhaseUI();
        } else {
            // Desempate no prolongamento
            fase = "terminado";
            jogo.setFoiPenaltis(false);
            updatePhaseUI();
        }
    }

    private void guardar() {
        // Validate penaltis if needed
        if (fase.equals("penaltis") || jogo.isFoiPenaltis()) {
            int penC = safeInt(penCasaF), penV = safeInt(penVisitF);
            if (penC == penV) {
                JOptionPane.showMessageDialog(this,
                    "O resultado dos penaltis não pode ser empate.\nVerifique o tab 🥅 Penaltis.",
                    "Penaltis inválidos", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Validate posse de bola when terminating
        double posseC = 0, posseV = 0;
        try { posseC = Double.parseDouble(posseCasaF.getText().trim()); } catch (Exception ignored) {}
        try { posseV = Double.parseDouble(posseVisitF.getText().trim()); } catch (Exception ignored) {}
        if (posseC <= 0 && posseV <= 0) {
            JOptionPane.showMessageDialog(this,
                "A posse de bola é obrigatória para terminar o jogo.\nPreencha os valores no separador Estat\u00edsticas.",
                "Posse de bola obrigatória", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Apply scores from event counts
        jogo.setGolosCasa(golosCasaModel.size());
        jogo.setGolosVisitante(golosVisitModel.size());
        jogo.setGolosCasaProlongamento(prolCasaModel.size());
        jogo.setGolosVisitanteProlongamento(prolVisitModel.size());
        if (jogo.isFoiPenaltis()) {
            jogo.setPenaltisCasa(safeInt(penCasaF));
            jogo.setPenaltisVisitante(safeInt(penVisitF));
        }

        // Stats
        try { jogo.setPosseCasa(Double.parseDouble(posseCasaF.getText().trim())); } catch (Exception ignored) {}
        try { jogo.setPosseVisitante(Double.parseDouble(posseVisitF.getText().trim())); } catch (Exception ignored) {}
        try { jogo.setCantosCasa(Integer.parseInt(cantosCasaF.getText().trim())); } catch (Exception ignored) {}
        try { jogo.setCantosVisitante(Integer.parseInt(cantosVisitF.getText().trim())); } catch (Exception ignored) {}
        jogo.setCartoesAmarelosCasa(amarCasaModel.size());
        jogo.setCartoesAmarelosVisitante(amarVisitModel.size());
        jogo.setCartoesVermelhosCasa(vermCasaModel.size());
        jogo.setCartoesVermelhosVisitante(vermVisitModel.size());

        // Rebuild events
        jogo.getEventos().clear();
        saveEventsFromModel(golosCasaModel,  jogo.getEquipaCasa(),      "Golo");
        saveEventsFromModel(golosVisitModel, jogo.getEquipaVisitante(), "Golo");
        saveEventsFromModel(prolCasaModel,   jogo.getEquipaCasa(),      "Golo"); // prol golos also count
        saveEventsFromModel(prolVisitModel,  jogo.getEquipaVisitante(), "Golo");
        saveEventsFromModel(assistCasaModel, jogo.getEquipaCasa(),      "Assistência");
        saveEventsFromModel(assistVisitModel,jogo.getEquipaVisitante(), "Assistência");
        saveEventsFromModel(amarCasaModel,   jogo.getEquipaCasa(),      "Amarelo");
        saveEventsFromModel(amarVisitModel,  jogo.getEquipaVisitante(), "Amarelo");
        saveEventsFromModel(vermCasaModel,   jogo.getEquipaCasa(),      "Vermelho");
        saveEventsFromModel(vermVisitModel,  jogo.getEquipaVisitante(), "Vermelho");

        updatePlayerStats();
        jogo.setTerminado(true);

        // Auto-advance elimination rounds
        DataStore dsPost = DataStore.getInstance();
        Torneio torneioPost = dsPost.getTorneioAtual();
        if (isEliminacao) {
            String novaRonda = dsPost.verificarEAvancarEliminacao(torneioPost);
            if (novaRonda != null) {
                JOptionPane.showMessageDialog(this,
                    "Ronda concluída! Os jogos dos " + novaRonda + " foram gerados automaticamente.",
                    "Nova Ronda", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (torneioPost != null && torneioPost.faseGruposCompleta()) {
            boolean temElim = torneioPost.getJogos().stream().anyMatch(j -> "eliminacao".equals(j.getFase()));
            if (!temElim) {
                try {
                    dsPost.gerarFaseEliminacao(torneioPost);
                    JOptionPane.showMessageDialog(this,
                        "Fase de grupos concluída! Os jogos da eliminação foram gerados automaticamente.",
                        "Eliminatórias Geradas", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) { /* silent */ }
            }
        }

        DataStore.getInstance().save();
        if (onSaved != null) onSaved.run();
        dispose();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LOAD EXISTING
    // ═══════════════════════════════════════════════════════════════════
    private void loadExistingData() {
        // Split events: goals in prol time have minuto > 90
        for (EventoJogo ev : jogo.getEventos()) {
            if (!(ev instanceof EventoPessoal ep)) continue;
            String entry = ep.getMinuto() + "' — " +
                (ep.getJogador() != null ? ep.getJogador().getNumeroCamisola() + " · " + ep.getJogador() : "?");
            boolean isCasa = ep.getJogador() != null && ep.getJogador().getEquipa() == jogo.getEquipaCasa();
            switch (ep.getTipoEvento()) {
                case "Golo" -> {
                    if (ep.getMinuto() > 90) (isCasa ? prolCasaModel : prolVisitModel).addElement(entry);
                    else                     (isCasa ? golosCasaModel : golosVisitModel).addElement(entry);
                }
                case "Assistência" -> (isCasa ? assistCasaModel : assistVisitModel).addElement(entry);
                case "Amarelo"     -> (isCasa ? amarCasaModel   : amarVisitModel).addElement(entry);
                case "Vermelho"    -> (isCasa ? vermCasaModel   : vermVisitModel).addElement(entry);
            }
        }
        if (posseCasaF  != null) posseCasaF.setText(String.valueOf((int)jogo.getPosseCasa()));
        if (posseVisitF != null) posseVisitF.setText(String.valueOf((int)jogo.getPosseVisitante()));
        if (cantosCasaF != null) cantosCasaF.setText(String.valueOf(jogo.getCantosCasa()));
        if (cantosVisitF!= null) cantosVisitF.setText(String.valueOf(jogo.getCantosVisitante()));
        if (penCasaF    != null) penCasaF.setText(String.valueOf(jogo.getPenaltisCasa()));
        if (penVisitF   != null) penVisitF.setText(String.valueOf(jogo.getPenaltisVisitante()));
        updateScore();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════
    private void saveEventsFromModel(DefaultListModel<String> model, Equipa equipa, String tipo) {
        if (equipa == null) return;
        for (int i = 0; i < model.size(); i++) {
            String entry = model.get(i);
            int minuto = 0; Jogador jogador = null;
            try {
                minuto = Integer.parseInt(entry.split("'")[0].trim());
                String rest = entry.substring(entry.indexOf("—")+1).trim();
                int num = Integer.parseInt(rest.split("·")[0].trim());
                for (Jogador j : equipa.getJogadores()) if (j.getNumeroCamisola()==num) { jogador=j; break; }
            } catch (Exception ignored) {}
            jogo.addEvento(new EventoPessoal(jogo, minuto, tipo, jogador, tipo));
        }
    }

    private void updatePlayerStats() {
        List<Jogador> todos = new ArrayList<>();
        if (jogo.getEquipaCasa()      != null) todos.addAll(jogo.getEquipaCasa().getJogadores());
        if (jogo.getEquipaVisitante() != null) todos.addAll(jogo.getEquipaVisitante().getJogadores());
        todos.forEach(j -> { j.setGolos(0); j.setAssistencias(0); j.setCartoesAmarelos(0); j.setCartoesVermelhos(0); });
        DataStore ds = DataStore.getInstance();
        List<Jogo> all = ds.getTorneioAtual() != null ? ds.getTorneioAtual().getJogos() : ds.getJogos();
        for (Jogo jg : all)
            for (EventoJogo ev : jg.getEventos())
                if (ev instanceof EventoPessoal ep && ep.getJogador() != null)
                    switch (ep.getTipoEvento()) {
                        case "Golo"        -> ep.getJogador().setGolos(ep.getJogador().getGolos()+1);
                        case "Assistência" -> ep.getJogador().setAssistencias(ep.getJogador().getAssistencias()+1);
                        case "Amarelo"     -> ep.getJogador().setCartoesAmarelos(ep.getJogador().getCartoesAmarelos()+1);
                        case "Vermelho"    -> ep.getJogador().setCartoesVermelhos(ep.getJogador().getCartoesVermelhos()+1);
                    }
    }

    private int safeInt(JTextField tf) {
        if (tf == null) return 0;
        try { return Integer.parseInt(tf.getText().trim()); } catch (Exception e) { return 0; }
    }
    /** Guarda todos os dados actuais sem marcar o jogo como terminado */
    private void guardarSemTerminar() {
        // Apply current event counts
        jogo.setGolosCasa(golosCasaModel.size());
        jogo.setGolosVisitante(golosVisitModel.size());
        jogo.setGolosCasaProlongamento(prolCasaModel.size());
        jogo.setGolosVisitanteProlongamento(prolVisitModel.size());

        try { jogo.setPosseCasa(Double.parseDouble(posseCasaF.getText().trim())); } catch (Exception ignored) {}
        try { jogo.setPosseVisitante(Double.parseDouble(posseVisitF.getText().trim())); } catch (Exception ignored) {}
        try { jogo.setCantosCasa(Integer.parseInt(cantosCasaF.getText().trim())); } catch (Exception ignored) {}
        try { jogo.setCantosVisitante(Integer.parseInt(cantosVisitF.getText().trim())); } catch (Exception ignored) {}
        jogo.setCartoesAmarelosCasa(amarCasaModel.size());
        jogo.setCartoesAmarelosVisitante(amarVisitModel.size());
        jogo.setCartoesVermelhosCasa(vermCasaModel.size());
        jogo.setCartoesVermelhosVisitante(vermVisitModel.size());

        jogo.getEventos().clear();
        saveEventsFromModel(golosCasaModel,  jogo.getEquipaCasa(),      "Golo");
        saveEventsFromModel(golosVisitModel, jogo.getEquipaVisitante(), "Golo");
        saveEventsFromModel(prolCasaModel,   jogo.getEquipaCasa(),      "Golo");
        saveEventsFromModel(prolVisitModel,  jogo.getEquipaVisitante(), "Golo");
        saveEventsFromModel(assistCasaModel, jogo.getEquipaCasa(),      "Assistência");
        saveEventsFromModel(assistVisitModel,jogo.getEquipaVisitante(), "Assistência");
        saveEventsFromModel(amarCasaModel,   jogo.getEquipaCasa(),      "Amarelo");
        saveEventsFromModel(amarVisitModel,  jogo.getEquipaVisitante(), "Amarelo");
        saveEventsFromModel(vermCasaModel,   jogo.getEquipaCasa(),      "Vermelho");
        saveEventsFromModel(vermVisitModel,  jogo.getEquipaVisitante(), "Vermelho");

        updatePlayerStats();
        DataStore.getInstance().save();
        JOptionPane.showMessageDialog(this, "Dados guardados! O jogo não foi terminado.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
        if (onSaved != null) onSaved.run();
    }

}
