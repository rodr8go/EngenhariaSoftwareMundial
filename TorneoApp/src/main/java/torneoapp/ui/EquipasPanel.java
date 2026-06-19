package torneoapp.ui;

import torneoapp.model.*;
import torneoapp.service.DataStore;
import torneoapp.util.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class EquipasPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public EquipasPanel() {
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
        header.add(UITheme.titleLabel("Equipas"), BorderLayout.WEST);
        JButton addBtn = UITheme.successButton("+ Adicionar Equipa");
        addBtn.addActionListener(e -> showEquipaDialog(null));
        header.add(addBtn, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        String[] cols = {"Emblema", "Nome", "Localização", "Treinador", "Nº Jogadores"};
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
        JButton jogBtn    = UITheme.grayButton("👟 Jogadores");
        JButton trBtn     = UITheme.grayButton("🧑‍💼 Treinador");
        actions.add(editBtn); actions.add(removeBtn); actions.add(jogBtn); actions.add(trBtn);

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione uma equipa."); return; }
            Equipa eq = DataStore.getInstance().getEquipas().get(row);
            if (DataStore.getInstance().jogoCalendarizado(eq)) {
                JOptionPane.showMessageDialog(this, "Não é possível editar uma equipa com jogos já calendarizados.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            showEquipaDialog(eq);
        });
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione uma equipa."); return; }
            Equipa eq = DataStore.getInstance().getEquipas().get(row);
            if (DataStore.getInstance().jogoCalendarizado(eq)) {
                JOptionPane.showMessageDialog(this, "Não é possível remover uma equipa com jogos já calendarizados.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Remover " + eq.getNome() + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DataStore.getInstance().removeEquipa(eq); refresh();
            }
        });
        jogBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione uma equipa."); return; }
            showJogadoresDialog(DataStore.getInstance().getEquipas().get(row));
        });
        trBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecione uma equipa."); return; }
            showTreinadorDialog(DataStore.getInstance().getEquipas().get(row));
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
        for (Equipa e : DataStore.getInstance().getEquipas()) {
            ImageIcon thumb = loadThumb(e.getFotoPath(), 40, 40);
            String treinador = e.getTreinador() != null ? e.getTreinador().getNomeCompleto() : "-";
            tableModel.addRow(new Object[]{thumb, e.getNome(), e.getLocalizacao(), treinador, e.getJogadores().size()});
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
            // Clip to circle
            g2.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, w, h));
            // Scale maintaining aspect ratio
            double scale = Math.max((double)w/src.getWidth(), (double)h/src.getHeight());
            int sw = (int)(src.getWidth() * scale), sh = (int)(src.getHeight() * scale);
            int ox = (w - sw) / 2, oy = (h - sh) / 2;
            g2.drawImage(src, ox, oy, sw, sh, null);
            g2.dispose();
            return new ImageIcon(out);
        } catch (Exception ex) { return null; }
    }

    // ── Equipa dialog ────────────────────────────────────────────────────────
    private void showEquipaDialog(Equipa existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Adicionar Equipa" : "Editar Equipa", true);
        dlg.setSize(500, 400);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 4, 6, 4);

        // Photo picker (emblema da equipa)
        String[] pathHolder = {existing != null ? existing.getFotoPath() : null};
        JLabel photoPicker = UITheme.photoPickerLabel(pathHolder, 90, dlg);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        p.add(photoPicker, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        JTextField nomeF  = UITheme.formField(""); if (existing != null) nomeF.setText(existing.getNome());
        JTextField descF  = UITheme.formField(""); if (existing != null) descF.setText(existing.getDescricao());
        JTextField locF   = UITheme.formField(""); if (existing != null) locF.setText(existing.getLocalizacao());

        String[] labels = {"Nome", "Descrição", "Localização"};
        JTextField[] tfs = {nomeF, descF, locF};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.35; p.add(UITheme.formLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65; p.add(tfs[i], gbc);
        }

        JButton saveBtn = UITheme.primaryButton(existing == null ? "Adicionar" : "Confirmar");
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 2;
        p.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String nome = nomeF.getText().trim();
            if (nome.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Nome obrigatório."); return; }
            if (existing == null) {
                Equipa eq = new Equipa(nome, descF.getText().trim(), locF.getText().trim());
                eq.setFotoPath(pathHolder[0]);
                DataStore.getInstance().addEquipa(eq);
            } else {
                existing.setNome(nome); existing.setDescricao(descF.getText().trim());
                existing.setLocalizacao(locF.getText().trim()); existing.setFotoPath(pathHolder[0]);
            }
            refresh(); dlg.dispose();
        });
        dlg.add(new JScrollPane(p) {{ setBorder(null); getViewport().setBackground(UITheme.CONTENT_BG); }});
        dlg.setVisible(true);
    }

    // ── Jogadores dialog ─────────────────────────────────────────────────────
    private void showJogadoresDialog(Equipa equipa) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Jogadores - " + equipa.getNome(), true);
        dlg.setSize(760, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Foto", "Nome Completo", "Alcunha", "Posição", "Nº", "Golos", "Assist.", "CA", "CV"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return c == 0 ? ImageIcon.class : Object.class; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(44);
        UITheme.styleTable(t);
        t.getColumnModel().getColumn(0).setMaxWidth(50);
        t.getColumnModel().getColumn(0).setMinWidth(50);

        equipa.getJogadores().forEach(j -> m.addRow(new Object[]{
            loadThumb(j.getFotoPath(), 36, 36),
            j.getNomeCompleto(), j.getAlcunha(), j.getPosicao(), j.getNumeroCamisola(),
            j.getGolos(), j.getAssistencias(), j.getCartoesAmarelos(), j.getCartoesVermelhos()
        }));

        boolean temJogos = DataStore.getInstance().jogoCalendarizado(equipa);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        if (temJogos) {
            JLabel aviso = new JLabel("⚠ Equipa com jogos marcados — jogadores em modo só leitura.");
            aviso.setFont(new Font("SansSerif", Font.ITALIC, 12));
            aviso.setForeground(new Color(180, 100, 0));
            btnRow.add(aviso);
        } else {
            JButton addBtn    = UITheme.successButton("+ Adicionar");
            JButton editBtn   = UITheme.grayButton("✏ Editar");
            JButton removeBtn = UITheme.dangerButton("Remover");
            btnRow.add(addBtn); btnRow.add(editBtn); btnRow.add(removeBtn);

            addBtn.addActionListener(e -> showAddJogadorDialog(equipa, dlg, m));

            editBtn.addActionListener(e -> {
                int row = t.getSelectedRow();
                if (row < 0 || row >= equipa.getJogadores().size()) {
                    JOptionPane.showMessageDialog(dlg, "Selecione um jogador para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showEditJogadorDialog(equipa, equipa.getJogadores().get(row), dlg, m, row);
            });

            removeBtn.addActionListener(e -> {
                int row = t.getSelectedRow();
                if (row >= 0 && row < equipa.getJogadores().size()) {
                    Jogador j = equipa.getJogadores().get(row);
                    equipa.removeJogador(j); DataStore.getInstance().removeJogador(j); m.removeRow(row);
                }
            });
        }

        p.add(new JScrollPane(t), BorderLayout.CENTER);
        p.add(btnRow, BorderLayout.SOUTH);
        dlg.add(p); dlg.setVisible(true);
    }

    private void showAddJogadorDialog(Equipa equipa, JDialog parent, DefaultTableModel m) {
        JDialog dlg = new JDialog(parent, "Adicionar Jogador", true);
        dlg.setSize(500, 500);
        dlg.setLocationRelativeTo(parent);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 4, 5, 4);

        // Photo picker
        String[] pathHolder = {null};
        JLabel photoPicker = UITheme.photoPickerLabel(pathHolder, 90, dlg);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        p.add(photoPicker, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        JTextField nomeF = UITheme.formField("");
        JTextField alcF  = UITheme.formField("");
        JTextField posF  = UITheme.formField("");
        JTextField natF  = UITheme.formField("");
        JTextField altF  = UITheme.formField("");
        JTextField numF  = UITheme.formField("");

        String[] labels = {"Nome Completo", "Alcunha", "Posição", "Nacionalidade", "Altura (cm)", "Nº Camisola"};
        JTextField[] tfs = {nomeF, alcF, posF, natF, altF, numF};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.4; p.add(UITheme.formLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.6; p.add(tfs[i], gbc);
        }

        JButton saveBtn = UITheme.successButton("Criar e Adicionar");
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 2;
        p.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String nome = nomeF.getText().trim();
                if (nome.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Nome obrigatório."); return; }
                int alt = altF.getText().trim().isEmpty() ? 0 : Integer.parseInt(altF.getText().trim());
                int num = numF.getText().trim().isEmpty() ? 0 : Integer.parseInt(numF.getText().trim());
                Jogador j = new Jogador(nome, alcF.getText().trim(), posF.getText().trim(),
                        LocalDate.now().minusYears(20), natF.getText().trim(), alt, num);
                j.setFotoPath(pathHolder[0]);
                equipa.addJogador(j);
                DataStore.getInstance().addJogador(j);
                m.addRow(new Object[]{
                    loadThumb(j.getFotoPath(), 36, 36),
                    j.getNomeCompleto(), j.getAlcunha(), j.getPosicao(), j.getNumeroCamisola(), 0, 0, 0, 0
                });
                dlg.dispose();
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dlg, "Altura e número devem ser inteiros."); }
        });

        dlg.add(new JScrollPane(p) {{ setBorder(null); getViewport().setBackground(UITheme.CONTENT_BG); }});
        dlg.setVisible(true);
    }

    private void showEditJogadorDialog(Equipa equipa, Jogador jogador, JDialog parent, DefaultTableModel m, int row) {
        JDialog dlg = new JDialog(parent, "Editar Jogador — " + jogador.getNomeCompleto(), true);
        dlg.setSize(500, 520);
        dlg.setLocationRelativeTo(parent);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 4, 5, 4);

        // Photo picker — inicia com foto actual
        String[] pathHolder = {jogador.getFotoPath()};
        JLabel photoPicker = UITheme.photoPickerLabel(pathHolder, 90, dlg);
        // Pre-load existing photo into the label
        if (jogador.getFotoPath() != null) {
            try {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(jogador.getFotoPath()));
                if (img != null) {
                    java.awt.Image scaled = img.getScaledInstance(90, 90, java.awt.Image.SCALE_SMOOTH);
                    photoPicker.setIcon(new ImageIcon(scaled));
                    photoPicker.setText("");
                }
            } catch (Exception ignored) {}
        }
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        p.add(photoPicker, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        JTextField nomeF = UITheme.formField(""); nomeF.setText(jogador.getNomeCompleto());
        JTextField alcF  = UITheme.formField(""); alcF.setText(jogador.getAlcunha() != null ? jogador.getAlcunha() : "");
        JTextField posF  = UITheme.formField(""); posF.setText(jogador.getPosicao() != null ? jogador.getPosicao() : "");
        JTextField natF  = UITheme.formField(""); natF.setText(jogador.getNacionalidade() != null ? jogador.getNacionalidade() : "");
        JTextField altF  = UITheme.formField(""); altF.setText(jogador.getAltura() > 0 ? String.valueOf(jogador.getAltura()) : "");
        JTextField numF  = UITheme.formField(""); numF.setText(String.valueOf(jogador.getNumeroCamisola()));

        String[] labels = {"Nome Completo", "Alcunha", "Posição", "Nacionalidade", "Altura (cm)", "Nº Camisola"};
        JTextField[] tfs = {nomeF, alcF, posF, natF, altF, numF};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.4; p.add(UITheme.formLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.6; p.add(tfs[i], gbc);
        }

        JButton saveBtn = UITheme.successButton("Guardar Alterações");
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 2;
        p.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String nome = nomeF.getText().trim();
                if (nome.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Nome obrigatório."); return; }
                int alt = altF.getText().trim().isEmpty() ? 0 : Integer.parseInt(altF.getText().trim());
                int num = numF.getText().trim().isEmpty() ? 0 : Integer.parseInt(numF.getText().trim());

                jogador.setNomeCompleto(nome);
                jogador.setAlcunha(alcF.getText().trim());
                jogador.setPosicao(posF.getText().trim());
                jogador.setNacionalidade(natF.getText().trim());
                jogador.setAltura(alt);
                jogador.setNumeroCamisola(num);
                jogador.setFotoPath(pathHolder[0]);

                // Refresh table row
                m.setValueAt(loadThumb(jogador.getFotoPath(), 36, 36), row, 0);
                m.setValueAt(jogador.getNomeCompleto(), row, 1);
                m.setValueAt(jogador.getAlcunha(),      row, 2);
                m.setValueAt(jogador.getPosicao(),      row, 3);
                m.setValueAt(jogador.getNumeroCamisola(), row, 4);

                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Altura e número devem ser inteiros.");
            }
        });

        dlg.add(new JScrollPane(p) {{ setBorder(null); getViewport().setBackground(UITheme.CONTENT_BG); }});
        dlg.setVisible(true);
    }

    // ── Treinador dialog ─────────────────────────────────────────────────────
    private void showTreinadorDialog(Equipa equipa) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Treinador - " + equipa.getNome(), true);
        dlg.setSize(500, 440);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.CONTENT_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 4, 5, 4);

        Treinador existing = equipa.getTreinador();

        // Photo picker
        String[] pathHolder = {existing != null ? null : null}; // treinador still can have a photo if model is extended
        JLabel photoPicker = UITheme.photoPickerLabel(pathHolder, 90, dlg);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        p.add(photoPicker, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        JTextField nomeF  = UITheme.formField(""); if (existing != null) nomeF.setText(existing.getNomeCompleto());
        JTextField emailF = UITheme.formField(""); if (existing != null) emailF.setText(existing.getEmail());
        JTextField telF   = UITheme.formField(""); if (existing != null) telF.setText(existing.getTelemovel());
        JTextField natF   = UITheme.formField(""); if (existing != null) natF.setText(existing.getNacionalidade());

        String[] labels = {"Nome Completo", "E-mail", "Telemóvel", "Nacionalidade"};
        JTextField[] tfs = {nomeF, emailF, telF, natF};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.4; p.add(UITheme.formLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.6; p.add(tfs[i], gbc);
        }

        JButton saveBtn = UITheme.primaryButton(existing == null ? "Criar e Adicionar" : "Confirmar");
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 2;
        p.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String nome = nomeF.getText().trim();
            if (nome.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Nome obrigatório."); return; }
            if (existing != null && DataStore.getInstance().jogoCalendarizado(equipa)) {
                JOptionPane.showMessageDialog(dlg, "Não é possível editar o treinador.\nA equipa já tem jogos agendados.", "Bloqueado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (existing == null) {
                Treinador t = new Treinador(nome, LocalDate.now().minusYears(40),
                        natF.getText().trim(), emailF.getText().trim(), telF.getText().trim());
                equipa.setTreinador(t); t.setEquipa(equipa);
                DataStore.getInstance().addTreinador(t);
            } else {
                existing.setNomeCompleto(nome); existing.setEmail(emailF.getText().trim());
                existing.setTelemovel(telF.getText().trim()); existing.setNacionalidade(natF.getText().trim());
            }
            refresh(); dlg.dispose();
        });

        dlg.add(new JScrollPane(p) {{ setBorder(null); getViewport().setBackground(UITheme.CONTENT_BG); }});
        dlg.setVisible(true);
    }
}
