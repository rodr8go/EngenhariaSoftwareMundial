package torneoapp.ui;

import torneoapp.model.Torneio;
import torneoapp.service.DataStore;
import torneoapp.util.Icons;
import torneoapp.util.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;

public class WelcomeScreen extends JFrame {

    public WelcomeScreen() {
        setTitle("Gestor de Torneio");
        setSize(560, 460);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY_DARK, 0, getHeight(), new Color(10, 28, 70)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;

        // Trophy
        ImageIcon tacaIco = Icons.sidebar("tacac.png", 110);
        JLabel tacaLbl = tacaIco != null ? new JLabel(tacaIco) : new JLabel("🏆");
        tacaLbl.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0; gbc.insets = new Insets(30, 0, 4, 0);
        bg.add(tacaLbl, gbc);

        // Title
        JLabel titleLbl = new JLabel("Gestor de Torneio", SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 20, 0);
        bg.add(titleLbl, gbc);

        // Nome label
        JLabel nomeLbl = roundedLabel("Nome do Torneio");
        gbc.gridy = 2; gbc.insets = new Insets(4, 0, 4, 0);
        bg.add(nomeLbl, gbc);

        // Input
        JTextField nomeField = buildTextField();
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 14, 0);
        bg.add(nomeField, gbc);

        // Entrar button
        JButton entrarBtn = roundedGreenButton("Entrar");
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 12, 0);
        bg.add(entrarBtn, gbc);

        // Hint
        JLabel hint = new JLabel("Cria um novo torneio ou continua um existente", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(140, 165, 210));
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 0, 0);
        bg.add(hint, gbc);

        ActionListener onEntrar = e -> {
            String nome = nomeField.getText().trim();
            if (nome.isEmpty()) { shake(nomeField); return; }
            DataStore ds = DataStore.getInstance();
            // Only one torneio — create if doesn't exist, or load existing
            Torneio t = ds.findTorneioByName(nome);
            if (t == null) {
                t = new Torneio(nome, LocalDate.now(), 0);
                ds.addTorneio(t);
            }
            ds.setTorneioAtual(t);
            final Torneio torneio = t;
            // Save torneio selection
            DataStore.getInstance().save();
            dispose();
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                frame.setTorneiName(torneio.getNome());
                if (torneio.getJogos().isEmpty()) {
                    frame.showPanel("Torneio");
                } else {
                    frame.showPanel("Dashboard");
                }
            });
        };

        entrarBtn.addActionListener(onEntrar);
        nomeField.addActionListener(onEntrar);
        setContentPane(bg);
    }

    private JTextField buildTextField() {
        JTextField tf = new JTextField(22) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 15));
        tf.setForeground(UITheme.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        tf.setPreferredSize(new Dimension(290, 42));
        tf.setCaretColor(UITheme.TEXT_PRIMARY);
        return tf;
    }

    private JLabel roundedLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 20, 20, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(false);
        lbl.setPreferredSize(new Dimension(180, 34));
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
        return lbl;
    }

    private JButton roundedGreenButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(30,160,50) : new Color(50,200,70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 42));
        return btn;
    }

    private void shake(JComponent c) {
        Point orig = c.getLocation();
        int[] steps = {-8,8,-6,6,-4,4,-2,2,0};
        int[] idx = {0};
        Timer t = new Timer(30, null);
        t.addActionListener(e -> {
            if (idx[0] >= steps.length) { t.stop(); c.setLocation(orig); return; }
            c.setLocation(orig.x + steps[idx[0]++], orig.y);
        });
        t.start();
    }
}
