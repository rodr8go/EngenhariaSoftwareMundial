package torneoapp.ui;

import torneoapp.model.*;
import torneoapp.service.DataStore;
import torneoapp.util.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PatrociniosPanel extends JPanel {

    private DefaultTableModel patModel;
    private JTable patTable;
    private DefaultTableModel conModel;
    private JTable conTable;
    private JLabel contratosTitle;
    private Patrocinador selectedPat = null;

    public PatrociniosPanel() {
        setBackground(UITheme.CONTENT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.CONTENT_BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ── Header ─────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CONTENT_BG);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        header.add(UITheme.titleLabel("Patrocínios"), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UITheme.CONTENT_BG);
        JButton addPatBtn = UITheme.successButton("+ Registar Patrocínio");
        JButton addConBtn = UITheme.grayButton("+ Criar Contrato");
        addPatBtn.addActionListener(e -> showPatrocinadorDialog(null));
        addConBtn.addActionListener(e -> showContratoDialog());
        btns.add(addConBtn); btns.add(addPatBtn);
        header.add(btns, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Main split: top = patrocinadores, bottom = contratos ────────────
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 0, 14));
        mainPanel.setBackground(UITheme.CONTENT_BG);

        // ── TOP: Patrocinadores table ───────────────────────────────────────
        JPanel patSection = new JPanel(new BorderLayout(0, 6));
        patSection.setBackground(UITheme.CONTENT_BG);

        String[] patCols = {"Logo", "Nome", "NIF", "E-mail", "Setor de Atividade", "Telemóvel"};
        patModel = new DefaultTableModel(patCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return c == 0 ? ImageIcon.class : String.class; }
        };
        patTable = new JTable(patModel);
        patTable.setRowHeight(46);
        UITheme.styleTable(patTable);
        patTable.getColumnModel().getColumn(0).setMaxWidth(54); patTable.getColumnModel().getColumn(0).setMinWidth(54);

        // Highlight selected row
        patTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = patTable.getSelectedRow();
                if (row >= 0 && row < DataStore.getInstance().getPatrocinadores().size()) {
                    selectedPat = DataStore.getInstance().getPatrocinadores().get(row);
                    refreshContratos();
                }
            }
        });

        JPanel patCard = new JPanel(new BorderLayout());
        patCard.setBackground(UITheme.CARD_BG);
        patCard.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 240)));

        JPanel patHeader = new JPanel(new BorderLayout());
        patHeader.setBackground(UITheme.CARD_BG);
        patHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
        patHeader.add(UITheme.sectionLabel("Patrocinadores"), BorderLayout.WEST);
        patCard.add(patHeader, BorderLayout.NORTH);

        JScrollPane patScroll = new JScrollPane(patTable);
        patScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 240)));
        patCard.add(patScroll, BorderLayout.CENTER);

        JPanel patActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        patActions.setBackground(UITheme.CONTENT_BG);
        JButton editBtn   = UITheme.grayButton("✏ Editar");
        JButton removeBtn = UITheme.dangerButton("🗑 Remover");
        patActions.add(editBtn); patActions.add(removeBtn);

        editBtn.addActionListener(e -> {
            int row = patTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um patrocinador."); return; }
            // UC 2.1
            showPatrocinadorDialog(DataStore.getInstance().getPatrocinadores().get(row));
        });
        removeBtn.addActionListener(e -> {
            int row = patTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um patrocinador."); return; }
            Patrocinador p = DataStore.getInstance().getPatrocinadores().get(row);
            if (JOptionPane.showConfirmDialog(this, "Remover \"" + p.getNome() + "\"?",
                    "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DataStore.getInstance().removePatrocinador(p);
                selectedPat = null;
                refresh();
            }
        });

        patSection.add(patCard, BorderLayout.CENTER);
        patSection.add(patActions, BorderLayout.SOUTH);
        mainPanel.add(patSection);

        // ── BOTTOM: Contratos table ─────────────────────────────────────────
        JPanel conSection = new JPanel(new BorderLayout(0, 0));
        conSection.setBackground(UITheme.CONTENT_BG);

        String[] conCols = {"Número", "Patrocinador", "Valor (€)", "Tipo", "Direitos", "Jogo", "Data"};
        conModel = new DefaultTableModel(conCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        conTable = new JTable(conModel);
        UITheme.styleTable(conTable);

        JPanel conCard = new JPanel(new BorderLayout());
        conCard.setBackground(UITheme.CARD_BG);
        conCard.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 240)));

        JPanel conHeader = new JPanel(new BorderLayout());
        conHeader.setBackground(UITheme.CARD_BG);
        conHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
        contratosTitle = new JLabel("Contratos");
        contratosTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        contratosTitle.setForeground(UITheme.TEXT_PRIMARY);
        JLabel conHint = new JLabel("Selecione um patrocinador para filtrar");
        conHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        conHint.setForeground(UITheme.TEXT_SECONDARY);
        conHeader.add(contratosTitle, BorderLayout.WEST);
        conHeader.add(conHint, BorderLayout.EAST);
        conCard.add(conHeader, BorderLayout.NORTH);

        JScrollPane conScroll = new JScrollPane(conTable);
        conScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 240)));
        conCard.add(conScroll, BorderLayout.CENTER);

        conSection.add(conCard, BorderLayout.CENTER);
        mainPanel.add(conSection);

        root.add(mainPanel, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    public void refresh() {
        // UC 1.1 — try/catch simulates DB access
        try {
            patModel.setRowCount(0);
            List<Patrocinador> pats = DataStore.getInstance().getPatrocinadores();
            for (Patrocinador p : pats) {
                patModel.addRow(new Object[]{
                    loadThumb(p.getFotoPath(), 38, 38),
                    p.getNome(), p.getNif(), p.getEmail(),
                    p.getSetorAtividade(), p.getTelemovel()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Não foi possível carregar a informação da base de dados.",
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
        refreshContratos();
    }

    private void refreshContratos() {
        try {
            conModel.setRowCount(0);
            List<ContratoPatrocinio> todos = DataStore.getInstance().getContratos();
            // Filter by selected patrocinador, or show all if none selected
            for (ContratoPatrocinio c : todos) {
                if (selectedPat != null && c.getPatrocinador() != selectedPat) continue;
                conModel.addRow(new Object[]{
                    c.getNumero(),
                    c.getPatrocinador() != null ? c.getPatrocinador().getNome() : "",
                    String.format("%.0f€", c.getValor()),
                    c.getTipoPatrocinio(),
                    c.getDireitos(),
                    c.getJogo() != null ? c.getJogo().toString() : "—",
                    c.getDataContrato()
                });
            }
            if (selectedPat != null) {
                contratosTitle.setText("Contratos — " + selectedPat.getNome());
            } else {
                contratosTitle.setText("Contratos (todos)");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Não foi possível carregar a informação da base de dados.",
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon loadThumb(String path, int w, int h) {
        if (path == null || path.isBlank()) return null;
        try { return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)); }
        catch (Exception ex) { return null; }
    }

    // ── Registar Patrocínio (UC R5) ─────────────────────────────────────────
    private void showPatrocinadorDialog(Patrocinador existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Registar Patrocínio" : "Editar Patrocinador", true);
        dlg.setSize(500, 540);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.CONTENT_BG);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 4, 6, 4);

        // Photo picker
        String[] pathHolder = {existing != null ? existing.getFotoPath() : null};
        JLabel photoPicker = UITheme.photoPickerLabel(pathHolder, 88, dlg);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        p.add(photoPicker, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        JTextField nomeF  = UITheme.formField(""); if (existing != null) nomeF.setText(existing.getNome());
        JTextField nifF   = UITheme.formField(""); if (existing != null) nifF.setText(existing.getNif());
        JTextField moradaF= UITheme.formField(""); if (existing != null) moradaF.setText(existing.getMorada());
        JTextField emailF = UITheme.formField(""); if (existing != null) emailF.setText(existing.getEmail());
        JTextField setorF = UITheme.formField(""); if (existing != null) setorF.setText(existing.getSetorAtividade());
        JTextField telF   = UITheme.formField(""); if (existing != null) telF.setText(existing.getTelemovel());
        JTextField descF  = UITheme.formField(""); if (existing != null) descF.setText(existing.getDescricao());

        String[] labels = {"Nome *", "NIF", "Morada", "E-mail", "Setor Atividade", "Telemóvel", "Descrição"};
        JTextField[] tfs  = {nomeF, nifF, moradaF, emailF, setorF, telF, descF};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.38; p.add(UITheme.formLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.62; p.add(tfs[i], gbc);
        }

        // Bottom buttons bar
        JPanel btnBar = new JPanel(new BorderLayout());
        btnBar.setBackground(UITheme.CONTENT_BG);
        btnBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 240)),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)));

        JButton cancelBtn = UITheme.grayButton("Cancelar");
        // UC 3.1 — cancelar fecha sem guardar
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton saveBtn = UITheme.primaryButton(existing == null ? "Guardar" : "Confirmar");
        saveBtn.addActionListener(e -> {
            String nome = nomeF.getText().trim();
            if (nome.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Nome obrigatório."); return; }
            // UC 4 / 4.1
            try {
                if (existing == null) {
                    Patrocinador pat = new Patrocinador(nome, nifF.getText().trim(), moradaF.getText().trim(),
                        emailF.getText().trim(), setorF.getText().trim(), telF.getText().trim(), descF.getText().trim());
                    pat.setFotoPath(pathHolder[0]);
                    DataStore.getInstance().addPatrocinador(pat);
                } else {
                    existing.setNome(nome); existing.setNif(nifF.getText().trim());
                    existing.setMorada(moradaF.getText().trim()); existing.setEmail(emailF.getText().trim());
                    existing.setSetorAtividade(setorF.getText().trim()); existing.setTelemovel(telF.getText().trim());
                    existing.setDescricao(descF.getText().trim()); existing.setFotoPath(pathHolder[0]);
                }
                refresh(); dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,
                    "Ocorreu um erro inesperado. Não foi possível registar o patrocínio. Tente novamente.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setBackground(UITheme.CONTENT_BG);
        rightBtns.add(cancelBtn); rightBtns.add(saveBtn);
        btnBar.add(rightBtns, BorderLayout.EAST);

        root.add(new JScrollPane(p) {{ setBorder(null); getViewport().setBackground(UITheme.CONTENT_BG); }}, BorderLayout.CENTER);
        root.add(btnBar, BorderLayout.SOUTH);
        dlg.add(root);
        dlg.setVisible(true);
    }

    // ── Criar Contrato (UC R5) ──────────────────────────────────────────────
    private void showContratoDialog() {
        DataStore ds = DataStore.getInstance();
        if (ds.getPatrocinadores().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione primeiro um patrocinador.");
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Criar Contrato", true);
        dlg.setSize(480, 420);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.CONTENT_BG);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(8, 4, 8, 4);

        JTextField numF   = UITheme.formField("");
        JComboBox<Patrocinador> patCombo = new JComboBox<>(ds.getPatrocinadores().toArray(new Patrocinador[0]));
        patCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        // Pre-select the currently selected patrocinador
        if (selectedPat != null) patCombo.setSelectedItem(selectedPat);

        JTextField valorF = UITheme.formField("");
        JTextField tipoF  = UITheme.formField("");
        JTextField dirF   = UITheme.formField("");
        JComboBox<String> jogoCombo = new JComboBox<>();
        jogoCombo.addItem("— Sem jogo específico —");
        ds.getJogosTorneio().forEach(j -> jogoCombo.addItem(j.toString()));
        jogoCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));

        String[] labels = {"Número *", "Patrocinador *", "Valor (€) *", "Tipo Patrocínio", "Direitos", "Jogo associado"};
        Component[] comps = {numF, patCombo, valorF, tipoF, dirF, jogoCombo};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.38; p.add(UITheme.formLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.62; p.add(comps[i], gbc);
        }

        // Bottom buttons
        JPanel btnBar = new JPanel(new BorderLayout());
        btnBar.setBackground(UITheme.CONTENT_BG);
        btnBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 240)),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)));

        JButton cancelBtn = UITheme.grayButton("Cancelar");
        // UC 3.1 — cancelar descarta e regressa
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton saveBtn = UITheme.primaryButton("Aceitar");
        saveBtn.addActionListener(e -> {
            String num = numF.getText().trim();
            if (num.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Número do contrato obrigatório."); return; }
            if (valorF.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dlg, "Valor obrigatório."); return; }
            try {
                double valor = Double.parseDouble(valorF.getText().trim().replace(",", "."));
                // Find jogo if selected
                Jogo jogoSel = null;
                int jogoIdx = jogoCombo.getSelectedIndex();
                if (jogoIdx > 0) {
                    List<Jogo> jogos = ds.getJogosTorneio();
                    if (jogoIdx - 1 < jogos.size()) jogoSel = jogos.get(jogoIdx - 1);
                }
                // Block if jogo already terminated
                if (jogoSel != null && jogoSel.isTerminado()) {
                    JOptionPane.showMessageDialog(dlg,
                        "Não é possível criar um contrato para um jogo já terminado.",
                        "Jogo terminado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // UC 4 / 4.1
                try {
                    ContratoPatrocinio c = new ContratoPatrocinio(num,
                        (Patrocinador) patCombo.getSelectedItem(), valor,
                        tipoF.getText().trim(), dirF.getText().trim(),
                        jogoSel, LocalDate.now());
                    ds.addContrato(c);
                    refresh();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "Contrato registado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg,
                        "Ligação com base de dados comprometida. O contrato não foi guardado. Tente novamente.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Valor inválido. Use formato numérico (ex: 50000).");
            }
        });

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setBackground(UITheme.CONTENT_BG);
        rightBtns.add(cancelBtn); rightBtns.add(saveBtn);
        btnBar.add(rightBtns, BorderLayout.EAST);

        root.add(p, BorderLayout.CENTER);
        root.add(btnBar, BorderLayout.SOUTH);
        dlg.add(root);
        dlg.setVisible(true);
    }
}
