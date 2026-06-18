package torneoapp;

import torneoapp.model.Torneio;
import torneoapp.service.DataStore;
import torneoapp.ui.MainFrame;
import java.time.LocalDate;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Save on JVM shutdown (window close, Ctrl+C, etc.)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DataStore.getInstance().save();
                System.out.println("Dados guardados.");
            } catch (Exception e) {
                System.err.println("Erro ao guardar: " + e.getMessage());
            }
        }));

        SwingUtilities.invokeLater(() -> {
            DataStore ds = DataStore.getInstance();
            // Ensure there is always exactly one torneio
            Torneio t = ds.getTorneioAtual();
            if (t == null) {
                if (!ds.getTorneios().isEmpty()) {
                    t = ds.getTorneios().get(0);
                } else {
                    t = new Torneio("Mundial", LocalDate.now(), 0);
                    ds.addTorneio(t);
                }
                ds.setTorneioAtual(t);
            }
            final Torneio torneio = t;
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            frame.setTorneiName(torneio.getNome());
            if (torneio.getJogos().isEmpty()) {
                frame.showPanel("Torneio");
            } else {
                frame.showPanel("Dashboard");
            }
        });
    }
}
