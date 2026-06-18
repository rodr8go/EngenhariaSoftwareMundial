package torneoapp.ui;

import torneoapp.model.*;
import torneoapp.service.DataStore;
import torneoapp.util.Icons;
import torneoapp.util.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VoluntariosPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<Jogo> jogoCombo;
    private JLabel totalLbl;

    public VoluntariosPanel() {
        setBackground(UITheme.CONTENT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(UITheme.CONTENT_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // ── Header ─────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CONTENT_BG);
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setBackground(UITheme.CONTENT_BG);
        ImageIcon ico = Icons.assist(26);
        if (ico != null) titleRow.add(new JLabel(ico));
        titleRow.add(UITheme.titleLabel("Voluntários"));
        header.add(titleRow, BorderLayout.WEST);
        wrapper.add(header, BorderLayout.NORTH);

        // ── Left: form ─────────────────────────────────────────────────────
        JPanel formCard = UITheme.card();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(280, 0));

        JLabel formTitle = UITheme.sectionLabel("Adicionar Voluntários");
        formTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);

        // Jogo selector
        JPanel jogoRow = new JPanel(new BorderLayout(0, 4));
        jogoRow.setBackground(UITheme.CARD_BG);
        jogoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        jogoRow.add(UITheme.formLabel("Jogo"), BorderLayout.NORTH);
        jogoCombo = new JComboBox<>();
        jogoCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jogoRow.add(jogoCombo, BorderLayout.CENTER);
        formCard.add(jogoRow);
        formCard.add(Box.createVerticalStrut(12));

        // Categoria
        JPanel catRow = new JPanel(new BorderLayout(0, 4));
        catRow.setBackground(UITheme.CARD_BG);
        catRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        catRow.add(UITheme.formLabel("Categoria"), BorderLayout.NORTH);
        JTextField catField = UITheme.formField("");
        catRow.add(catField, BorderLayout.CENTER);
        formCard.add(catRow);
        formCard.add(Box.createVerticalStrut(12));

        // Quantidade
        JPanel qtdRow = new JPanel(new BorderLayout(0, 4));
        qtdRow.setBackground(UITheme.CARD_BG);
        qtdRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        qtdRow.add(UITheme.formLabel("Quantidade"), BorderLayout.NORTH);
        JTextField qtdField = UITheme.formField("");
        qtdRow.add(qtdField, BorderLayout.CENTER);
        formCard.add(qtdRow);
        formCard.add(Box.createVerticalStrut(16));

        // Description
        JPanel descRow = new JPanel(new BorderLayout(0, 4));
        descRow.setBackground(UITheme.CARD_BG);
        descRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        descRow.add(UITheme.formLabel("Descrição (opcional)"), BorderLayout.NORTH);
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 230)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        descRow.add(new JScrollPane(descArea) {{ setBorder(null); }}, BorderLayout.CENTER);
        formCard.add(descRow);
        formCard.add(Box.createVerticalStrut(16));

        JButton addBtn = UITheme.successButton("+ Adicionar");
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        formCard.add(addBtn);
        formCard.add(Box.createVerticalGlue());

        // Total card
        formCard.add(Box.createVerticalStrut(20));
        JPanel totalCard = new JPanel(new BorderLayout(0, 4));
        totalCard.setBackground(new Color(240, 245, 255));
        totalCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 240), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        totalCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel totalTitle = UITheme.formLabel("Total de Voluntários");
        totalLbl = new JLabel("0");
        totalLbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        totalLbl.setForeground(UITheme.ACCENT);
        totalCard.add(totalTitle, BorderLayout.NORTH);
        totalCard.add(totalLbl, BorderLayout.CENTER);
        formCard.add(totalCard);

        // ── Right: table ───────────────────────────────────────────────────
        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(UITheme.CARD_BG);
        JLabel tableTitle = UITheme.sectionLabel("Registo de Voluntários");
        tableHeader.add(tableTitle, BorderLayout.WEST);

        // Filter by jogo
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRow.setBackground(UITheme.CARD_BG);
        filterRow.add(UITheme.formLabel("Filtrar por jogo:"));
        JComboBox<String> filterCombo = new JComboBox<>();
        filterCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        filterCombo.setPreferredSize(new Dimension(200, 28));
        filterRow.add(filterCombo);
        tableHeader.add(filterRow, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Jogo", "Categoria", "Quantidade", "Descrição"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        tableCard.add(table.getTableHeader(), BorderLayout.CENTER);
        tableCard.add(new JScrollPane(table) {{ setBorder(null); }}, BorderLayout.SOUTH);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBackground(UITheme.CONTENT_BG);
        JButton removeBtn = UITheme.dangerButton("🗑 Remover");
        actions.add(removeBtn);

        // ── Layout assembly ────────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setBackground(UITheme.CONTENT_BG);
        content.add(formCard, BorderLayout.WEST);

        JPanel rightSide = new JPanel(new BorderLayout(0, 0));
        rightSide.setBackground(UITheme.CONTENT_BG);
        rightSide.add(tableCard, BorderLayout.CENTER);
        rightSide.add(actions, BorderLayout.SOUTH);
        content.add(rightSide, BorderLayout.CENTER);

        wrapper.add(content, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        // ── Listeners ──────────────────────────────────────────────────────
        addBtn.addActionListener(e -> {
            Jogo jogo = (Jogo) jogoCombo.getSelectedItem();
            if (jogo == null) { JOptionPane.showMessageDialog(this, "Selecione um jogo."); return; }
            String cat = catField.getText().trim();
            if (cat.isEmpty()) { JOptionPane.showMessageDialog(this, "Categoria obrigatória."); return; }
            int qtd;
            try { qtd = Integer.parseInt(qtdField.getText().trim()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Quantidade inválida."); return; }
            Voluntario v = new Voluntario(cat, qtd);
            jogo.addVoluntario(v);
            catField.setText("");
            qtdField.setText("");
            descArea.setText("");
            refresh();
        });

        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione uma linha."); return; }
            // find matching voluntario
            String jogoStr = (String) tableModel.getValueAt(row, 0);
            String catStr  = (String) tableModel.getValueAt(row, 1);
            for (Jogo j : DataStore.getInstance().getJogos()) {
                if (j.toString().equals(jogoStr)) {
                    j.getVoluntarios().removeIf(v -> v.getCategoria().equals(catStr));
                    break;
                }
            }
            refresh();
        });

        filterCombo.addActionListener(e -> {
            String sel = (String) filterCombo.getSelectedItem();
            populateTable(sel);
        });

        // store filterCombo reference for refresh
        putClientProperty("filterCombo", filterCombo);
    }

    public void refresh() {
        DataStore ds = DataStore.getInstance();

        // refresh jogoCombo
        jogoCombo.removeAllItems();
        for (Jogo j : ds.getJogos()) jogoCombo.addItem(j);

        // refresh filterCombo
        JComboBox<String> filterCombo = (JComboBox<String>) getClientProperty("filterCombo");
        if (filterCombo != null) {
            String prev = (String) filterCombo.getSelectedItem();
            filterCombo.removeAllItems();
            filterCombo.addItem("Todos os jogos");
            for (Jogo j : ds.getJogos()) filterCombo.addItem(j.toString());
            if (prev != null) filterCombo.setSelectedItem(prev);
        }

        populateTable(filterCombo != null ? (String) filterCombo.getSelectedItem() : null);
    }

    private void populateTable(String filter) {
        tableModel.setRowCount(0);
        int total = 0;
        for (Jogo j : DataStore.getInstance().getJogos()) {
            String jogoStr = j.toString();
            if (filter != null && !filter.equals("Todos os jogos") && !jogoStr.equals(filter)) continue;
            for (Voluntario v : j.getVoluntarios()) {
                tableModel.addRow(new Object[]{jogoStr, v.getCategoria(), v.getQuantidade(), ""});
                total += v.getQuantidade();
            }
        }
        if (totalLbl != null) totalLbl.setText(String.valueOf(total));
    }
}
