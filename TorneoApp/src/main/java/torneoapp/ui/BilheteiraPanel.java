package torneoapp.ui;

import torneoapp.model.*;
import torneoapp.service.DataStore;
import torneoapp.util.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Bilheteira — só vende bilhetes.
 * Preço definido na aba Torneio ("Definir Preçário").
 * Suporta venda de múltiplos bilhetes de uma vez,
 * cada um com número de lugar único.
 */
public class BilheteiraPanel extends JPanel {

    private JComboBox<Jogo>   jogoCombo;
    private JComboBox<Setor>  setorCombo;
    private JTextField        nifField;
    private JTextField        quantidadeField;
    private JLabel            precoLabel;
    private JLabel            lugaresLabel;
    private JLabel            totalLabel;

    private DefaultTableModel tableModel;
    private JTable            table;

    public BilheteiraPanel() {
        setBackground(UITheme.CONTENT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(UITheme.CONTENT_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // ── Header ─────────────────────────────────────────────────────────
        JLabel title = UITheme.titleLabel("Bilheteira");
        wrapper.add(title, BorderLayout.NORTH);

        // ── Two-column layout ───────────────────────────────────────────────
        JPanel cols = new JPanel(new GridLayout(1, 2, 20, 0));
        cols.setBackground(UITheme.CONTENT_BG);

        // ── LEFT: sell form ─────────────────────────────────────────────────
        JPanel sellCard = UITheme.card();
        sellCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);

        JLabel formTitle = UITheme.sectionLabel("Vender Bilhete(s)");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        sellCard.add(formTitle, gbc);
        gbc.gridwidth = 1;

        jogoCombo  = new JComboBox<>();
        setorCombo = new JComboBox<>();
        nifField       = UITheme.formField("");
        quantidadeField = UITheme.formField("1");
        precoLabel = new JLabel("—");
        precoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        precoLabel.setForeground(UITheme.SUCCESS);
        lugaresLabel = new JLabel("—");
        lugaresLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lugaresLabel.setForeground(UITheme.TEXT_SECONDARY);
        totalLabel = new JLabel("Total: —");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalLabel.setForeground(UITheme.PRIMARY);

        jogoCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        setorCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        jogoCombo.addActionListener(e  -> { updateSetores(); updateTotal(); });
        setorCombo.addActionListener(e -> { updatePreco(); updateTotal(); });
        quantidadeField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
        });

        String[] labels = {"Jogo", "Setor", "NIF do Cliente", "Quantidade", "Preço p/ Bilhete", "Lugares Restantes"};
        Component[] comps = {jogoCombo, setorCombo, nifField, quantidadeField, precoLabel, lugaresLabel};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.45;
            sellCard.add(UITheme.formLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.55;
            sellCard.add(comps[i], gbc);
        }

        // Total row
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 2;
        sellCard.add(new JSeparator(), gbc);
        gbc.gridy = labels.length + 2;
        sellCard.add(totalLabel, gbc);

        // Vender button
        JButton venderBtn = new JButton("Confirmar Venda") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(25,145,40) : new Color(50,200,70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        venderBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        venderBtn.setForeground(Color.WHITE);
        venderBtn.setOpaque(false); venderBtn.setContentAreaFilled(false);
        venderBtn.setBorderPainted(false); venderBtn.setFocusPainted(false);
        venderBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy = labels.length + 3;
        sellCard.add(venderBtn, gbc);

        venderBtn.addActionListener(e -> confirmarVenda());
        cols.add(sellCard);

        // ── RIGHT: bilhetes table ────────────────────────────────────────────
        JPanel billCard = UITheme.card();
        billCard.setLayout(new BorderLayout(0, 8));

        JLabel billTitle = UITheme.sectionLabel("Bilhetes Vendidos");
        billTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        billCard.add(billTitle, BorderLayout.NORTH);

        String[] cols2 = {"#", "Jogo", "Setor", "Lugar", "NIF", "Preço (€)"};
        tableModel = new DefaultTableModel(cols2, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(3).setMaxWidth(65);

        // Click on bilhete to see detail
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showBilheteDetail(table.getSelectedRow());
            }
        });

        billCard.add(table.getTableHeader(), BorderLayout.CENTER);
        billCard.add(new JScrollPane(table) {{ setBorder(null); }}, BorderLayout.SOUTH);

        // Summary bar
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        summaryBar.setBackground(UITheme.CONTENT_BG);
        JLabel countLbl = new JLabel(); countLbl.setForeground(UITheme.TEXT_SECONDARY);
        summaryBar.add(countLbl);
        putClientProperty("countLbl", countLbl);
        billCard.add(summaryBar, BorderLayout.NORTH); // will be replaced on refresh

        cols.add(billCard);
        wrapper.add(cols, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    public void refresh() {
        DataStore ds = DataStore.getInstance();

        // Update jogo combo
        jogoCombo.removeAllItems();
        List<Jogo> jogos = ds.getJogosTorneio();
        for (Jogo j : jogos) jogoCombo.addItem(j);
        updateSetores();

        // Rebuild bilhetes table
        tableModel.setRowCount(0);
        for (Bilhete b : ds.getBilhetes()) {
            tableModel.addRow(new Object[]{
                b.getId(),
                b.getJogo() != null ? b.getJogo().toString() : "",
                b.getSetor() != null ? b.getSetor().getNome() : "",
                "L" + b.getNumLugar(),
                b.getNif(),
                String.format("%.2f€", b.getPrecoFinal())
            });
        }
        updateTotal();
    }

    private void updateSetores() {
        setorCombo.removeAllItems();
        Jogo jogo = (Jogo) jogoCombo.getSelectedItem();
        if (jogo != null && jogo.getEstadio() != null)
            jogo.getEstadio().getSetores().forEach(s -> setorCombo.addItem(s));
        updatePreco();
    }

    private void updatePreco() {
        Setor setor = (Setor) setorCombo.getSelectedItem();
        Jogo  jogo  = (Jogo)  jogoCombo.getSelectedItem();
        if (setor != null) {
            if (!jogo.isPrecarioDefined() || setor.getPreco() == 0.0) {
                precoLabel.setText("Sem preçário");
                precoLabel.setForeground(UITheme.DANGER);
            } else {
                precoLabel.setText(String.format("%.2f€", setor.getPreco()));
                precoLabel.setForeground(UITheme.SUCCESS);
            }
            int vendidos = jogo != null
                ? (int) DataStore.getInstance().getBilhetes().stream()
                    .filter(b -> b.getJogo() == jogo && b.getSetor() == setor).count()
                : 0;
            int restantes = setor.getLotacao() - vendidos;
            lugaresLabel.setText(restantes + " / " + setor.getLotacao());
            lugaresLabel.setForeground(restantes > 0 ? UITheme.TEXT_SECONDARY : UITheme.DANGER);
        } else {
            precoLabel.setText("—");
            lugaresLabel.setText("—");
        }
        updateTotal();
    }

    private void updateTotal() {
        Setor setor = (Setor) setorCombo.getSelectedItem();
        int qtd = parseQtd();
        if (setor != null && qtd > 0 && setor.getPreco() > 0) {
            double tot = setor.getPreco() * qtd;
            totalLabel.setText(String.format("Total: %.2f€  (%d bilhetes)", tot, qtd));
            totalLabel.setForeground(UITheme.PRIMARY);
        } else {
            totalLabel.setText("Total: —");
            totalLabel.setForeground(UITheme.TEXT_SECONDARY);
        }
    }

    private int parseQtd() {
        try {
            int q = Integer.parseInt(quantidadeField.getText().trim());
            return q > 0 ? q : 0;
        } catch (NumberFormatException e) { return 0; }
    }

    private void confirmarVenda() {
        Jogo  jogo  = (Jogo)  jogoCombo.getSelectedItem();
        Setor setor = (Setor) setorCombo.getSelectedItem();

        if (jogo == null || setor == null) {
            JOptionPane.showMessageDialog(this, "Selecione um jogo e um setor."); return;
        }
        if (!jogo.isPrecarioDefined() || setor.getPreco() == 0.0) {
            JOptionPane.showMessageDialog(this,
                "Aviso: O preçário deste jogo ainda não foi definido.\nVá à aba Torneio → Definir Preçário.",
                "Sem preçário", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nif = nifField.getText().trim();
        if (nif.isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIF do cliente obrigatório."); return;
        }
        int qtd = parseQtd();
        if (qtd <= 0) {
            JOptionPane.showMessageDialog(this, "Quantidade deve ser maior que zero."); return;
        }

        // Check capacity
        DataStore ds = DataStore.getInstance();
        long vendidos = ds.getBilhetes().stream()
            .filter(b -> b.getJogo() == jogo && b.getSetor() == setor).count();
        int restantes = setor.getLotacao() - (int) vendidos;
        if (qtd > restantes) {
            JOptionPane.showMessageDialog(this,
                "Apenas " + restantes + " lugares disponíveis neste setor.", "Lotação esgotada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm
        double total = setor.getPreco() * qtd;
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Confirmar venda de %d bilhete(s)?\n\nJogo: %s\nSetor: %s\nPreço unitário: %.2f€\nTotal: %.2f€\nNIF: %s",
                qtd, jogo, setor.getNome(), setor.getPreco(), total, nif),
            "Confirmar Venda", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Generate bilhetes with unique seat numbers
        int nextLugar = (int) vendidos + 1;
        List<Bilhete> novos = new ArrayList<>();
        for (int i = 0; i < qtd; i++) {
            Bilhete b = new Bilhete(jogo, setor, nif, setor.getPreco(), nextLugar + i);
            jogo.addBilhete(b);
            ds.addBilhete(b);
            novos.add(b);
        }

        refresh();
        nifField.setText("");
        quantidadeField.setText("1");

        JOptionPane.showMessageDialog(this,
            String.format("%d bilhete(s) emitido(s) com sucesso!\nLugares: %d a %d\nTotal cobrado: %.2f€",
                qtd, nextLugar, nextLugar + qtd - 1, total),
            "Venda Concluída", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showBilheteDetail(int row) {
        if (row < 0) return;
        DataStore ds = DataStore.getInstance();
        List<Bilhete> all = ds.getBilhetes();
        if (row >= all.size()) return;
        Bilhete b = all.get(row);

        String info = String.format(
            "Bilhete #%d\n\nJogo:   %s\nSetor:  %s\nLugar:  %d\nNIF:    %s\nPreço:  %.2f€",
            b.getId(),
            b.getJogo() != null ? b.getJogo().toString() : "—",
            b.getSetor() != null ? b.getSetor().getNome() : "—",
            b.getNumLugar(),
            b.getNif(),
            b.getPrecoFinal()
        );
        JOptionPane.showMessageDialog(this, info, "Detalhe do Bilhete", JOptionPane.INFORMATION_MESSAGE);
    }
}
