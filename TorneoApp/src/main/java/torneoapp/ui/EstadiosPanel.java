package torneoapp.ui;

import torneoapp.model.Estadio;
import torneoapp.model.Setor;
import torneoapp.service.DataStore;
import torneoapp.util.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class EstadiosPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public EstadiosPanel() {
        setBackground(UITheme.CONTENT_BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UITheme.CONTENT_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CONTENT_BG);
        header.add(UITheme.titleLabel("Estádios"), BorderLayout.WEST);
        JButton addBtn = UITheme.successButton("+ Adicionar");
        addBtn.addActionListener(e -> showEstadioDialog(null));
        header.add(addBtn, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        String[] cols = {"Foto", "Nome", "Cidade", "Lotação", "Nº Setores", "Morada"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return c == 0 ? ImageIcon.class : String.class; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(48);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(56);
        table.getColumnModel().getColumn(0).setMinWidth(56);

        JPanel tableCard = UITheme.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCard.add(new JScrollPane(table) {{ setBorder(null); }}, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBackground(UITheme.CONTENT_BG);
        JButton editBtn   = UITheme.grayButton("✏ Editar");
        JButton removeBtn = UITheme.dangerButton("🗑 Remover");
        JButton setoresBtn = UITheme.grayButton("📋 Ver Setores");
        actions.add(editBtn); actions.add(removeBtn); actions.add(setoresBtn);

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um estádio."); return; }
            if (DataStore.getInstance().jogoCalendarizadoEstadio(DataStore.getInstance().getEstadios().get(row))) {
                JOptionPane.showMessageDialog(this, "Não é possível editar um estádio com jogos já calendarizados.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            showEstadioDialog(DataStore.getInstance().getEstadios().get(row));
        });
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um estádio."); return; }
            Estadio est = DataStore.getInstance().getEstadios().get(row);
            if (DataStore.getInstance().jogoCalendarizadoEstadio(est)) {
                JOptionPane.showMessageDialog(this, "Não é possível remover um estádio com jogos já calendarizados.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Remover " + est.getNome() + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DataStore.getInstance().removeEstadio(est);
                refresh();
            }
        });
        setoresBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione um estádio."); return; }
            showSetoresDialog(DataStore.getInstance().getEstadios().get(row));
        });

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UITheme.CONTENT_BG);
        content.add(tableCard, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        wrapper.add(content, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        for (Estadio e : DataStore.getInstance().getEstadios()) {
            ImageIcon thumb = loadThumb(e.getFotoPath(), 40, 40);
            tableModel.addRow(new Object[]{thumb, e.getNome(), e.getCidade(), e.getLotacao(), e.getSetores().size(), e.getMorada()});
        }
    }

    private ImageIcon loadThumb(String path, int w, int h) {
        if (path == null || path.isBlank()) return null;
        try {
            java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(new java.io.File(path));
            java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, w, h));
            double scale = Math.max((double)w/src.getWidth(), (double)h/src.getHeight());
            int sw = (int)(src.getWidth()*scale), sh = (int)(src.getHeight()*scale);
            g2.drawImage(src, (w-sw)/2, (h-sh)/2, sw, sh, null);
            g2.dispose();
            return new ImageIcon(out);
        } catch (Exception ex) { return null; }
    }

    private void showEstadioDialog(Estadio existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Adicionar Estádio" : "Editar Estádio", true);
        dlg.setSize(500, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Photo picker centrado no topo
        String[] pathHolder = {existing != null ? existing.getFotoPath() : null};
        JLabel photoPicker = UITheme.photoPickerLabel(pathHolder, 90, dlg);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(photoPicker, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        JTextField nomeF   = UITheme.formField(""); if (existing != null) nomeF.setText(existing.getNome());
        JTextField moradaF = UITheme.formField(""); if (existing != null) moradaF.setText(existing.getMorada());
        JTextField cidadeF = UITheme.formField(""); if (existing != null) cidadeF.setText(existing.getCidade());

        // Lotação é calculada automaticamente pelos setores (só leitura)
        JLabel lotacaoValLbl = new JLabel(existing != null ? existing.getLotacao() + " lugares (soma dos setores)" : "Definida pelos setores");
        lotacaoValLbl.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 12));
        lotacaoValLbl.setForeground(UITheme.TEXT_SECONDARY);

        String[] labels = {"Nome", "Morada", "Cidade"};
        JTextField[] tfs = {nomeF, moradaF, cidadeF};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.35; p.add(UITheme.formLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65; p.add(tfs[i], gbc);
        }
        // Lotação row
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.weightx = 0.35;
        p.add(UITheme.formLabel("Lotação Total"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        p.add(lotacaoValLbl, gbc);

        // Spacer
        gbc.gridx = 0; gbc.gridy = labels.length + 2; gbc.gridwidth = 2;
        p.add(Box.createVerticalStrut(4), gbc);
        gbc.gridwidth = 1;

        JButton saveBtn = UITheme.primaryButton(existing == null ? "Adicionar" : "Confirmar");
        gbc.gridx = 0; gbc.gridy = labels.length + 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String nome = nomeF.getText().trim();
                if (nome.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Nome obrigatório."); return; }
                if (existing == null) {
                    Estadio est = new Estadio(nome, moradaF.getText().trim(), cidadeF.getText().trim());
                    est.setFotoPath(pathHolder[0]);
                    DataStore.getInstance().addEstadio(est);
                } else {
                    existing.setNome(nome); existing.setMorada(moradaF.getText().trim());
                    existing.setCidade(cidadeF.getText().trim());                     existing.setFotoPath(pathHolder[0]);
                }
                refresh(); dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Erro: " + ex.getMessage()); }
        });

        dlg.add(new JScrollPane(p) {{ setBorder(null); getViewport().setBackground(UITheme.CONTENT_BG); }});
        dlg.setVisible(true);
    }

    private void showSetoresDialog(Estadio est) {
        boolean bloqueado = DataStore.getInstance().jogoCalendarizadoEstadio(est);
        String titulo = bloqueado ? "Setores (só leitura) — " + est.getNome() : "Setores — " + est.getNome();
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titulo, true);
        dlg.setSize(480, 400);
        dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Nome", "Lotação"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m); UITheme.styleTable(t);
        est.getSetores().forEach(s -> m.addRow(new Object[]{s.getNome(), s.getLotacao()}));

        p.add(new JScrollPane(t), BorderLayout.CENTER);

        if (bloqueado) {
            JLabel aviso = new JLabel("⚠ Estádio com jogos marcados — setores em modo só leitura.");
            aviso.setFont(new Font("SansSerif", Font.ITALIC, 12));
            aviso.setForeground(new Color(180, 100, 0));
            aviso.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            p.add(aviso, BorderLayout.SOUTH);
        } else {
            JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField nomeF = UITheme.formField(""); nomeF.setColumns(10);
            JTextField lotF  = UITheme.formField(""); lotF.setColumns(6);
            JButton addBtn    = UITheme.successButton("+ Setor");
            JButton removeBtn = UITheme.dangerButton("Remover");
            addRow.add(UITheme.formLabel("Nome:")); addRow.add(nomeF);
            addRow.add(UITheme.formLabel("Lotação:")); addRow.add(lotF);
            addRow.add(addBtn); addRow.add(removeBtn);

            addBtn.addActionListener(e -> {
                try {
                    String nome = nomeF.getText().trim(); int lot = Integer.parseInt(lotF.getText().trim());
                    if (nome.isEmpty()) return;
                    Setor s = new Setor(nome, lot); est.addSetor(s);
                    m.addRow(new Object[]{s.getNome(), s.getLotacao()});
                    nomeF.setText(""); lotF.setText("");
                } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dlg, "Lotação inválida."); }
            });
            removeBtn.addActionListener(e -> {
                int row = t.getSelectedRow();
                if (row >= 0 && row < est.getSetores().size()) {
                    est.removeSetor(est.getSetores().get(row)); m.removeRow(row);
                }
            });
            p.add(addRow, BorderLayout.SOUTH);
        }

        dlg.add(p); dlg.setVisible(true);
    }
}
