package torneoapp.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import torneoapp.model.*;
import java.time.LocalDate;

public class JogadorTest {

    @Test
    public void resetEstatisticasAZero() {
        Jogador j = new Jogador("Diogo", "Di", "Médio", LocalDate.of(1998,3,15), "PT", 175, 8);
        j.setGolos(10); j.setAssistencias(7); j.setCartoesAmarelos(3); j.setCartoesVermelhos(1);
        j.setGolos(0); j.setAssistencias(0); j.setCartoesAmarelos(0); j.setCartoesVermelhos(0);
        assertAll(
            () -> assertEquals(0, j.getGolos()),
            () -> assertEquals(0, j.getAssistencias()),
            () -> assertEquals(0, j.getCartoesAmarelos()),
            () -> assertEquals(0, j.getCartoesVermelhos())
        );
    }
}
