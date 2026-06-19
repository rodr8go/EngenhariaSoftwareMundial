package torneoapp.ui;

import torneoapp.model.*;
import torneoapp.service.DataStore;
import torneoapp.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TorneioPanel extends JPanel {
    private MainFrame mainFrame;
    private CardLayout innerLayout;
    private JPanel innerContent;

    // ── Setup view components ────────────────────────────────────────────────
    private JTextField dataInicioF;
    private JComboBox<Integer> numEquipasCombo;
    // Equipas selection
    private DefaultListModel<Equipa> dispEquipasModel  = new DefaultListModel<>();
    private DefaultListModel<Equipa> selEquipasModel   = new DefaultListModel<>();
    private JList<Equipa> dispEquipasList, selEquipasList;
    // Estadios selection
    private DefaultListModel<Estadio> dispEstadiosModel = new DefaultListModel<>();
    private DefaultListModel<Estadio> selEstadiosModel  = new DefaultListModel<>();
    private JList<Estadio> dispEstadiosList, selEstadiosList;

    // ── Jogos view components ────────────────────────────────────────────────
    private JTable jogosTable;
    private DefaultTableModel jogosModel;
    private JLabel tituloLbl, faseStatusLbl, jogosCountLbl;

    public TorneioPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(UITheme.CONTENT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        innerLayout = new CardLayout();
        innerContent = new JPanel(innerLayout);
        innerContent.setBackground(UITheme.CONTENT_BG);
        innerContent.add(buildSetupView(), "setup");
        innerContent.add(buildJogosView(), "jogos");
        add(innerContent, BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SETUP VIEW — Selecionar equipas, estádios e gerar calendário
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSetupView() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.CONTENT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // ── Header ─────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CONTENT_BG);
        JLabel title = UITheme.titleLabel("Configurar Torneio");
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // ── Main form ──────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CONTENT_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(6, 6, 6, 6);

        // Row 0: Data início + Nº equipas
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        infoRow.setBackground(UITheme.CONTENT_BG);

        dataInicioF = UITheme.formField("");
        dataInicioF.setText(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dataInicioF.setPreferredSize(new Dimension(120, 32));
        Integer[] nums = {4, 8, 16, 32};
        numEquipasCombo = new JComboBox<>(nums);
        numEquipasCombo.setSelectedItem(8);
        numEquipasCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        numEquipasCombo.setPreferredSize(new Dimension(90, 32));
        numEquipasCombo.addActionListener(e -> updateEquipasCount());

        infoRow.add(UITheme.formLabel("Data de início:"));
        infoRow.add(dataInicioF);
        infoRow.add(Box.createHorizontalStrut(12));
        infoRow.add(UITheme.formLabel("Nº Equipas:"));
        infoRow.add(numEquipasCombo);

        g.gridx=0; g.gridy=0; g.gridwidth=3; g.weighty=0;
        form.add(infoRow, g);

        // Row 1: labels
        g.gridwidth=1; g.weighty=0; g.insets=new Insets(10,6,2,6);
        g.gridx=0; g.gridy=1; g.weightx=0.45;
        form.add(UITheme.sectionLabel("Equipas Disponíveis"), g);
        g.gridx=1; g.weightx=0.1;
        form.add(new JLabel(""), g);
        g.gridx=2; g.weightx=0.45;
        form.add(UITheme.sectionLabel("Equipas no Torneio"), g);

        // Row 2: equipas dual list
        g.gridy=2; g.weighty=0.5; g.insets=new Insets(0,6,6,6);

        dispEquipasModel = new DefaultListModel<>();
        selEquipasModel  = new DefaultListModel<>();
        dispEquipasList  = buildList(dispEquipasModel);
        selEquipasList   = buildList(selEquipasModel);
        JPanel eqArrows = buildArrowButtons(dispEquipasList, dispEquipasModel, selEquipasList, selEquipasModel, true);

        g.gridx=0; form.add(new JScrollPane(dispEquipasList){{setBorder(BorderFactory.createLineBorder(new Color(210,218,240)));}}, g);
        g.gridx=1; form.add(eqArrows, g);
        g.gridx=2; form.add(new JScrollPane(selEquipasList){{setBorder(BorderFactory.createLineBorder(new Color(210,218,240)));}}, g);

        // Row 3: labels estádios
        g.weighty=0; g.insets=new Insets(10,6,2,6);
        g.gridx=0; g.gridy=3;
        form.add(UITheme.sectionLabel("Estádios Disponíveis"), g);
        g.gridx=1; form.add(new JLabel(""), g);
        g.gridx=2; form.add(UITheme.sectionLabel("Estádios no Torneio"), g);

        // Row 4: estadios dual list
        g.gridy=4; g.weighty=0.35; g.insets=new Insets(0,6,6,6);
        dispEstadiosModel = new DefaultListModel<>();
        selEstadiosModel  = new DefaultListModel<>();
        dispEstadiosList  = buildList(dispEstadiosModel);
        selEstadiosList   = buildList(selEstadiosModel);
        JPanel estArrows = buildArrowButtons(dispEstadiosList, dispEstadiosModel, selEstadiosList, selEstadiosModel, false);

        g.gridx=0; form.add(new JScrollPane(dispEstadiosList){{setBorder(BorderFactory.createLineBorder(new Color(210,218,240)));}}, g);
        g.gridx=1; form.add(estArrows, g);
        g.gridx=2; form.add(new JScrollPane(selEstadiosList){{setBorder(BorderFactory.createLineBorder(new Color(210,218,240)));}}, g);

        // Row 5: info + gerar button
        g.gridy=5; g.weighty=0; g.insets=new Insets(10,6,0,6);
        g.gridx=0; g.gridwidth=2;
        JLabel infoLbl = new JLabel("<html><i style='color:#888'>Selecione as equipas e os estádios para o torneio, depois clique em Gerar Calendário.</i></html>");
        infoLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        form.add(infoLbl, g);

        JButton gerarBtn = new JButton("⚽ Gerar Calendário") {
            @Override protected void paintComponent(Graphics g2) {
                Graphics2D g3 = (Graphics2D) g2.create();
                g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g3.setColor(getModel().isPressed() ? new Color(25,145,40) : new Color(50,200,70));
                g3.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g3.dispose();
                super.paintComponent(g2);
            }
        };
        gerarBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        gerarBtn.setForeground(Color.WHITE);
        gerarBtn.setOpaque(false); gerarBtn.setContentAreaFilled(false);
        gerarBtn.setBorderPainted(false); gerarBtn.setFocusPainted(false);
        gerarBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gerarBtn.setPreferredSize(new Dimension(220, 42));
        g.gridx=2; g.gridwidth=1;
        form.add(gerarBtn, g);

        gerarBtn.addActionListener(e -> gerarCalendario());

        root.add(form, BorderLayout.CENTER);
        return root;
    }

    private <T> JList<T> buildList(DefaultListModel<T> model) {
        JList<T> list = new JList<>(model);
        list.setFont(new Font("SansSerif", Font.PLAIN, 13));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setFixedCellHeight(30);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, v, i, sel, foc);
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                if (v instanceof Equipa eq) {
                    int njog = eq.getJogadores().size();
                    String tr = eq.getTreinador() != null ? " ✓" : "";
                    lbl.setText(eq.getNome() + tr + "  (" + njog + " jog.)");
                } else if (v instanceof Estadio est) {
                    lbl.setText(est.getNome() + " — " + est.getCidade() + " (" + est.getLotacao() + ")");
                }
                return lbl;
            }
        });
        return list;
    }

    @SuppressWarnings("unchecked")
    private <T> JPanel buildArrowButtons(JList<T> fromList, DefaultListModel<T> fromModel,
                                          JList<T> toList, DefaultListModel<T> toModel, boolean isEquipa) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        JButton addBtn    = UITheme.primaryButton("→");
        JButton remBtn    = UITheme.grayButton("←");
        JButton addAllBtn = new JButton("⇒");
        addAllBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addAllBtn.setBorderPainted(false); addAllBtn.setFocusPainted(false);
        addAllBtn.setBackground(new Color(200,225,255)); addAllBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton remAllBtn = new JButton("⇐");
        remAllBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        remAllBtn.setBorderPainted(false); remAllBtn.setFocusPainted(false);
        remAllBtn.setBackground(new Color(255,220,220)); remAllBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addBtn.addActionListener(e -> {
            List<T> selected = fromList.getSelectedValuesList();
            for (T item : selected) {
                if (isEquipa && item instanceof Equipa eq) {
                    String err = validarEquipa(eq);
                    if (err != null) {
                        JOptionPane.showMessageDialog(TorneioPanel.this,
                            "Equipa \"" + eq.getNome() + "\" não pode ser adicionada:\n" + err,
                            "Equipa incompleta", JOptionPane.WARNING_MESSAGE);
                        continue;
                    }
                }
                fromModel.removeElement(item); toModel.addElement(item);
            }
            if (isEquipa) updateEquipasCount();
        });
        remBtn.addActionListener(e -> {
            for (T item : (List<T>) toList.getSelectedValuesList()) {
                toModel.removeElement(item); fromModel.addElement(item);
            }
            if (isEquipa) updateEquipasCount();
        });
        addAllBtn.addActionListener(e -> {
            List<T> all = new ArrayList<>();
            for (int i = 0; i < fromModel.size(); i++) all.add(fromModel.get(i));
            StringBuilder erros = new StringBuilder();
            for (T item : all) {
                if (isEquipa && item instanceof Equipa eq) {
                    String err = validarEquipa(eq);
                    if (err != null) { erros.append("• ").append(eq.getNome()).append(": ").append(err).append("\n"); continue; }
                }
                fromModel.removeElement(item); toModel.addElement(item);
            }
            if (erros.length() > 0) JOptionPane.showMessageDialog(TorneioPanel.this,
                "Equipas ignoradas (incompletas):\n" + erros, "Atenção", JOptionPane.WARNING_MESSAGE);
            if (isEquipa) updateEquipasCount();
        });
        remAllBtn.addActionListener(e -> {
            while (!toModel.isEmpty()) { T item = toModel.remove(0); fromModel.addElement(item); }
            if (isEquipa) updateEquipasCount();
        });

        g.gridy=0; p.add(addAllBtn, g);
        g.gridy=1; p.add(addBtn, g);
        g.gridy=2; p.add(remBtn, g);
        g.gridy=3; p.add(remAllBtn, g);
        return p;
    }

    /** Valida se uma equipa tem treinador e pelo menos 26 jogadores. Retorna null se OK, mensagem de erro se não. */
    private String validarEquipa(Equipa eq) {
        if (eq.getTreinador() == null) return "Sem treinador definido.";
        if (eq.getJogadores().size() < 26) return "Tem " + eq.getJogadores().size() + " jogadores (mínimo 26).";
        return null;
    }

    private void updateEquipasCount() {
        // intentionally minimal
    }

    private void gerarCalendario() {
        DataStore ds = DataStore.getInstance();
        Torneio t = ds.getTorneioAtual();
        if (t == null) { JOptionPane.showMessageDialog(this, "Sem torneio ativo."); return; }

        // Validate
        if (selEquipasModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos 2 equipas.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selEquipasModel.size() < 2) {
            JOptionPane.showMessageDialog(this, "São precisas pelo menos 2 equipas.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selEstadiosModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos 1 estádio.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int required = (Integer) numEquipasCombo.getSelectedItem();
        int sel = selEquipasModel.size();

        // UC 2.1 validations (exact match required)
        if (dataInicioF.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "A data de início é obrigatória.", "Dados em falta", JOptionPane.ERROR_MESSAGE); return;
        }
        if (sel != required) {
            JOptionPane.showMessageDialog(this,
                "A quantidade de equipas selecionadas (" + sel + ") não corresponde ao número definido (" + required + ").",
                "Número de equipas incorreto", JOptionPane.ERROR_MESSAGE); return;
        }
        // Validate all teams have exactly 26 players
        StringBuilder erroEqs = new StringBuilder();
        for (int i = 0; i < selEquipasModel.size(); i++) {
            Equipa eq = selEquipasModel.get(i);
            if (eq.getJogadores().size() < 26) erroEqs.append("• ").append(eq.getNome()).append(": ").append(eq.getJogadores().size()).append(" jogadores\n");
        }
        if (erroEqs.length() > 0) {
            JOptionPane.showMessageDialog(this,
                "Todas as equipas devem ter pelo menos 26 jogadores:\n" + erroEqs,
                "Equipas incompletas", JOptionPane.ERROR_MESSAGE); return;
        }

        // Apply selections to torneio
        t.getEquipas().clear();
        for (int i = 0; i < selEquipasModel.size(); i++) t.addEquipa(selEquipasModel.get(i));

        // Set selected estadios as the active ones for generation
        // Store them temporarily so DataStore uses them
        List<Estadio> estadiosSel = new ArrayList<>();
        for (int i = 0; i < selEstadiosModel.size(); i++) estadiosSel.add(selEstadiosModel.get(i));

        // Parse data
        try {
            LocalDate d = LocalDate.parse(dataInicioF.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            t.setDataInicio(d);
        } catch (Exception ex) { t.setDataInicio(LocalDate.now().plusMonths(1)); }
        t.setNumEquipas(required);

        // Generate with selected estadios
        ds.gerarCalendarioGruposComEstadios(t, estadiosSel);

        int total = t.getJogos().size();
        // Switch to jogos view
        innerLayout.show(innerContent, "jogos");
        refreshJogosView();

        JOptionPane.showMessageDialog(this,
            "Calendário gerado com sucesso!\n" +
            total + " jogos criados na fase de grupos.",
            "Calendário Gerado", JOptionPane.INFORMATION_MESSAGE);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  JOGOS VIEW
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildJogosView() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UITheme.CONTENT_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CONTENT_BG);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setBackground(UITheme.CONTENT_BG);
        tituloLbl = UITheme.titleLabel("Torneio");
        faseStatusLbl = new JLabel("● Fase de Grupos");
        faseStatusLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        faseStatusLbl.setForeground(UITheme.ACCENT);
        jogosCountLbl = new JLabel("");
        jogosCountLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jogosCountLbl.setForeground(UITheme.TEXT_SECONDARY);
        titleRow.add(tituloLbl); titleRow.add(faseStatusLbl); titleRow.add(jogosCountLbl);
        header.add(titleRow, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.CONTENT_BG);

        JButton reconfBtn = UITheme.grayButton("⚙ Reconfigurar");
        reconfBtn.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                "Voltar à configuração irá apagar o calendário, bilhetes vendidos, contratos e estatísticas dos jogadores.\nConfirmar?",
                "Reconfigurar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                DataStore ds = DataStore.getInstance();
                Torneio t = ds.getTorneioAtual();
                if (t != null) {
                    ds.getJogos().removeAll(t.getJogos());
                    t.getJogos().clear();
                    t.setFaseAtual("grupos");
                }
                // Clear bilhetes, contratos
                ds.getBilhetes().clear();
                ds.getContratos().clear();
                // Reset player stats
                ds.getJogadores().forEach(j -> {
                    j.setGolos(0); j.setAssistencias(0);
                    j.setCartoesAmarelos(0); j.setCartoesVermelhos(0);
                });
                innerLayout.show(innerContent, "setup");
                refreshSetupView();
            }
        });

        btnRow.add(reconfBtn);
        header.add(btnRow, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Fase","Grupo/Ronda","Data/Hora","Casa","Visitante","Estádio","Estado","Resultado"};
        jogosModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        jogosTable = new JTable(jogosModel);
        UITheme.styleTable(jogosTable);
        jogosTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String estado = (String) t.getModel().getValueAt(row, 6);
                    c.setBackground("Terminado".equals(estado) ? new Color(240,255,240) : row%2==0 ? UITheme.CARD_BG : UITheme.TABLE_ALT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        // Double-click to insert result
        jogosTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = jogosTable.getSelectedRow();
                    if (row >= 0) {
                        Torneio t = DataStore.getInstance().getTorneioAtual();
                        List<Jogo> lista = t != null ? t.getJogos() : DataStore.getInstance().getJogos();
                        if (row < lista.size()) showDadosPosJogoDialog(lista.get(row));
                    }
                }
            }
        });

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(jogosTable.getTableHeader(), BorderLayout.NORTH);
        tableCard.add(new JScrollPane(jogosTable) {{ setBorder(null); }}, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBackground(UITheme.CONTENT_BG);
        JButton inserirBtn  = UITheme.grayButton("📊 Inserir Resultado");
        JButton precarioBtn = UITheme.grayButton("💶 Definir Preçário");
        actions.add(inserirBtn); actions.add(precarioBtn);
        JLabel dblClickHint = new JLabel("  duplo-clique = inserir resultado");
        dblClickHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dblClickHint.setForeground(UITheme.TEXT_SECONDARY);
        actions.add(dblClickHint);

        inserirBtn.addActionListener(e -> {
            int row = jogosTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um jogo."); return; }
            Torneio t = DataStore.getInstance().getTorneioAtual();
            List<Jogo> lista = t != null ? t.getJogos() : DataStore.getInstance().getJogos();
            if (row < lista.size()) showDadosPosJogoDialog(lista.get(row));
        });
        precarioBtn.addActionListener(e -> {
            int row = jogosTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um jogo."); return; }
            Torneio t = DataStore.getInstance().getTorneioAtual();
            List<Jogo> lista = t != null ? t.getJogos() : DataStore.getInstance().getJogos();
            if (row < lista.size()) showDefinirPrecarioDialog(lista.get(row));
        });

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UITheme.CONTENT_BG);
        content.add(tableCard, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REFRESH
    // ════════════════════════════════════════════════════════════════════════
    public void refresh() {
        DataStore ds = DataStore.getInstance();
        Torneio t = ds.getTorneioAtual();
        if (t == null || t.getJogos().isEmpty()) {
            refreshSetupView();
            innerLayout.show(innerContent, "setup");
        } else {
            innerLayout.show(innerContent, "jogos");
            refreshJogosView();
        }
    }

    private void refreshSetupView() {
        DataStore ds = DataStore.getInstance();
        // Populate disponíveis
        dispEquipasModel.clear(); selEquipasModel.clear();
        ds.getEquipas().forEach(dispEquipasModel::addElement);
        dispEstadiosModel.clear(); selEstadiosModel.clear();
        ds.getEstadios().forEach(dispEstadiosModel::addElement);
    }

    private void refreshJogosView() {
        DataStore ds = DataStore.getInstance();
        Torneio t = ds.getTorneioAtual();
        if (t == null) return;

        tituloLbl.setText(t.getNome());
        boolean temElim = t.getJogos().stream().anyMatch(j -> "eliminacao".equals(j.getFase()));
        long total = t.getJogos().size();
        long term  = t.getJogos().stream().filter(Jogo::isTerminado).count();

        faseStatusLbl.setText(temElim ? "● Eliminatórias" : "● Fase de Grupos");
        faseStatusLbl.setForeground(temElim ? new Color(200,130,0) : UITheme.ACCENT);
        jogosCountLbl.setText("  " + term + "/" + total + " concluídos");

        jogosModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Jogo j : t.getJogos()) {
            String fase   = "grupos".equals(j.getFase()) ? "Grupos" : "Eliminação";
            String ronda  = j.getRonda() != null ? j.getRonda() : "";
            String data   = j.getDataHora() != null ? j.getDataHora().format(fmt) : "";
            String casa   = j.getEquipaCasa() != null ? j.getEquipaCasa().getNome() : "";
            String visit  = j.getEquipaVisitante() != null ? j.getEquipaVisitante().getNome() : "";
            String est    = j.getEstadio() != null ? j.getEstadio().getNome() : "—";
            String estado = j.isTerminado() ? "Terminado" : "Agendado";
            String result = j.isTerminado() ? j.getGolosCasa() + " - " + j.getGolosVisitante() : "—";
            jogosModel.addRow(new Object[]{fase, ronda, data, casa, visit, est, estado, result});
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DIALOGS
    // ════════════════════════════════════════════════════════════════════════
    private void showDadosPosJogoDialog(Jogo jogo) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        ResultadoDialog dlg = new ResultadoDialog(parent, jogo, this::refreshJogosView);
        dlg.setVisible(true);
    }

    private void showVoluntariosDialog(Jogo jogo) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Voluntários — " + jogo, true);
        dlg.setSize(560, 440); dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

        // Table
        String[] cols = {"Categoria", "Quantidade", "Descrição"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m); UITheme.styleTable(t);
        jogo.getVoluntarios().forEach(v -> m.addRow(new Object[]{v.getCategoria(), v.getQuantidade(), ""}));

        JScrollPane sp = new JScrollPane(t); sp.setBorder(BorderFactory.createLineBorder(new Color(220,225,240)));
        p.add(sp, BorderLayout.CENTER);

        // Form to add
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CONTENT_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField catF  = UITheme.formField(""); catF.setColumns(12);
        JTextField qtdF  = UITheme.formField(""); qtdF.setColumns(6);
        JTextField descF = UITheme.formField(""); descF.setColumns(18);

        gbc.gridy=0; gbc.gridx=0; gbc.weightx=0; form.add(UITheme.formLabel("Categoria:"), gbc);
        gbc.gridx=1; gbc.weightx=0.3; form.add(catF, gbc);
        gbc.gridx=2; gbc.weightx=0; form.add(UITheme.formLabel("Qtd:"), gbc);
        gbc.gridx=3; gbc.weightx=0.15; form.add(qtdF, gbc);
        gbc.gridx=4; gbc.weightx=0; form.add(UITheme.formLabel("Descrição:"), gbc);
        gbc.gridx=5; gbc.weightx=0.55; form.add(descF, gbc);

        p.add(form, BorderLayout.SOUTH);
        dlg.add(p, BorderLayout.CENTER);

        // Buttons bar
        JPanel btnBar = new JPanel(new BorderLayout());
        btnBar.setBackground(UITheme.CONTENT_BG);
        btnBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0, new Color(220,225,240)),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        JButton addBtn    = UITheme.successButton("+ Adicionar");
        JButton removeBtn = UITheme.dangerButton("Remover");
        JButton cancelBtn = UITheme.grayButton("Cancelar");

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setBackground(UITheme.CONTENT_BG);
        left.add(cancelBtn);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(UITheme.CONTENT_BG);
        right.add(removeBtn); right.add(addBtn);
        btnBar.add(left, BorderLayout.WEST); btnBar.add(right, BorderLayout.EAST);
        dlg.add(btnBar, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            try {
                String cat = catF.getText().trim();
                if (cat.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Categoria obrigatória."); return; }
                int qtd = Integer.parseInt(qtdF.getText().trim());
                if (qtd <= 0) { JOptionPane.showMessageDialog(dlg, "Quantidade deve ser maior que zero."); return; }
                try {
                    jogo.addVoluntario(new Voluntario(cat, qtd));
                    m.addRow(new Object[]{cat, qtd, descF.getText().trim()});
                    catF.setText(""); qtdF.setText(""); descF.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg,
                        "Falha na base de dados. Não foi possível registar os voluntários.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Quantidade deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        removeBtn.addActionListener(e -> {
            int row = t.getSelectedRow();
            if (row >= 0 && row < jogo.getVoluntarios().size()) {
                jogo.getVoluntarios().remove(row); m.removeRow(row);
            }
        });
        // UC 4.1 Cancelar — fecha sem guardar
        cancelBtn.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    private void showAddJogoManualDialog() {
        DataStore ds = DataStore.getInstance();
        Torneio t = ds.getTorneioAtual();
        if (t == null || t.getEquipas().size() < 2) { JOptionPane.showMessageDialog(this,"Gere primeiro o calendário."); return; }
        JDialog dlg = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Adicionar Jogo Manual",true);
        dlg.setSize(420,280); dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20,24,20,24)); p.setBackground(UITheme.CONTENT_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(6,4,6,4);

        JComboBox<Equipa> casaC  = new JComboBox<>(t.getEquipas().toArray(new Equipa[0]));
        JComboBox<Equipa> visitC = new JComboBox<>(t.getEquipas().toArray(new Equipa[0]));
        JComboBox<Estadio> estC  = new JComboBox<>(ds.getEstadios().toArray(new Estadio[0]));
        JTextField dataF = UITheme.formField("dd/MM/yyyy HH:mm");
        String[] rondas = {"Grupo A","Grupo B","Grupo C","Grupo D","Grupo E","Grupo F","Grupo G","Grupo H","Oitavos","Quartos","Meias-Finais","Final"};
        JComboBox<String> rondaC = new JComboBox<>(rondas);

        String[] labels = {"Equipa Casa","Equipa Visitante","Estádio","Data/Hora","Fase/Grupo"};
        Component[] comps = {casaC,visitC,estC,dataF,rondaC};
        for (int i=0; i<labels.length; i++) {
            gbc.gridx=0; gbc.gridy=i; gbc.weightx=0.4; p.add(UITheme.formLabel(labels[i]),gbc);
            gbc.gridx=1; gbc.weightx=0.6; p.add(comps[i],gbc);
        }
        JButton save = UITheme.successButton("Adicionar");
        gbc.gridx=0; gbc.gridy=labels.length; gbc.gridwidth=2; p.add(save,gbc);
        save.addActionListener(e -> {
            Equipa casa=(Equipa)casaC.getSelectedItem(), visit=(Equipa)visitC.getSelectedItem();
            if (casa==visit) { JOptionPane.showMessageDialog(dlg,"Equipas devem ser diferentes."); return; }
            java.time.LocalDateTime dt;
            try { dt=java.time.LocalDateTime.parse(dataF.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")); }
            catch(Exception ex){ dt=java.time.LocalDateTime.now().plusDays(7); }
            Jogo j = new Jogo(casa,visit,(Estadio)estC.getSelectedItem(),dt);
            String sel=(String)rondaC.getSelectedItem();
            boolean isElim=sel.equals("Oitavos")||sel.equals("Quartos")||sel.equals("Meias-Finais")||sel.equals("Final");
            j.setFase(isElim?"eliminacao":"grupos"); j.setRonda(sel);
            t.addJogo(j); ds.addJogo(j);
            refreshJogosView(); dlg.dispose();
        });
        dlg.add(p); dlg.setVisible(true);
    }
    // ════════════════════════════════════════════════════════════════════════
    //  DEFINIR PREÇÁRIO (UC R3)
    // ════════════════════════════════════════════════════════════════════════
    private void showDefinirPrecarioDialog(Jogo jogo) {
        // UC 2.2 — jogo já terminado
        if (jogo.isTerminado()) {
            JOptionPane.showMessageDialog(this,
                "Não é possível definir o preçário. O jogo já foi terminado.",
                "Jogo terminado", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // UC 2.1 — estádio sem setores
        if (jogo.getEstadio() == null || jogo.getEstadio().getSetores().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Não é possível definir o preçário. O estádio alocado a este jogo não possui setores registados.",
                "Sem setores", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Definir Preçário — " + jogo, true);
        dlg.setSize(440, 340);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UITheme.CONTENT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Header
        JLabel infoLbl = new JLabel("Defina o preço de cada setor do estádio:");
        infoLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        infoLbl.setForeground(UITheme.TEXT_SECONDARY);
        root.add(infoLbl, BorderLayout.NORTH);

        // Table: Setor | Lotação | Preço (€) | Desconto (%) — cols 2 and 3 are editable
        String[] cols = {"Setor", "Lotação", "Preço (€)", "Desconto (%)"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 2 || c == 3; }
        };
        JTable t = new JTable(m);
        UITheme.styleTable(t);
        t.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));
        t.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JTextField()));
        t.getColumnModel().getColumn(3).setPreferredWidth(90);
        jogo.getEstadio().getSetores().forEach(s ->
            m.addRow(new Object[]{s.getNome(), s.getLotacao(), String.format("%.2f", s.getPreco()), String.format("%.0f", s.getDesconto())}));

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 240)));
        root.add(sp, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UITheme.CONTENT_BG);
        JButton cancelBtn = UITheme.grayButton("Cancelar");
        JButton saveBtn   = UITheme.successButton("Guardar");
        btnRow.add(cancelBtn); btnRow.add(saveBtn);
        root.add(btnRow, BorderLayout.SOUTH);

        // UC 3.2 — cancelar descarta alterações
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            // Stop any active cell editing
            if (t.isEditing()) t.getCellEditor().stopCellEditing();

            // UC 3.1 — validar valores
            for (int i = 0; i < jogo.getEstadio().getSetores().size(); i++) {
                String raw = m.getValueAt(i, 2).toString().trim().replace(",", ".");
                try {
                    double preco = Double.parseDouble(raw);
                    if (preco < 0) {
                        JOptionPane.showMessageDialog(dlg,
                            "Por favor, insira um valor numérico válido (maior ou igual a zero) para todos os setores.",
                            "Valor inválido", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg,
                        "Por favor, insira um valor numérico válido (maior ou igual a zero) para todos os setores.",
                        "Valor inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // UC 4 — guardar
            try {
                for (int i = 0; i < jogo.getEstadio().getSetores().size(); i++) {
                    double preco = Double.parseDouble(m.getValueAt(i, 2).toString().trim().replace(",", "."));
                    jogo.getEstadio().getSetores().get(i).setPreco(preco);
                    try {
                        double desc = Double.parseDouble(m.getValueAt(i, 3).toString().trim().replace(",", "."));
                        jogo.getEstadio().getSetores().get(i).setDesconto(desc);
                    } catch (Exception ignored) {}
                }
                jogo.setPrecarioDefined(true);
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Preçário guardado com sucesso!", "Guardado", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                // UC 4.1
                JOptionPane.showMessageDialog(dlg,
                    "Ocorreu um erro inesperado. Não foi possível registar o preçário. Tente novamente.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(root);
        dlg.setVisible(true);
    }

}
