package torneoapp.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import torneoapp.model.*;
import java.time.LocalDate;

public class EquipaTest {

    private Jogador j(String nome, int num) {
        return new Jogador(nome, nome, "Avançado", LocalDate.of(1995,1,1), "PT", 180, num);
    }

    @Test
    public void addJogadorAtribuiEquipa() {
        Equipa e = new Equipa("Sporting", "", "Lisboa");
        Jogador jg = j("Rúben", 7);
        e.addJogador(jg);
        assertEquals(1, e.getJogadores().size());
        assertEquals(e, jg.getEquipa());
    }

    @Test
    public void removeJogadorLimpaEquipa() {
        Equipa e = new Equipa("Benfica", "", "Lisboa");
        Jogador jg = j("Gonçalo", 9);
        e.addJogador(jg); e.removeJogador(jg);
        assertEquals(0, e.getJogadores().size());
        assertNull(jg.getEquipa());
    }

    @Test
    public void toStringComAlcunha() {
        Jogador jg = j("Cristiano Ronaldo", 7);
        jg.setAlcunha("CR7");
        assertEquals("CR7", jg.toString());
    }

    @Test
    public void toStringSemAlcunhaRetornaNome() {
        Jogador jg = new Jogador("Luis Figo", "", "Avançado", LocalDate.of(1972,11,4), "PT", 178, 10);
        assertEquals("Luis Figo", jg.toString());
    }

    @Test
    public void estatisticasJogador() {
        Jogador jg = j("Pedro", 11);
        jg.setGolos(5); jg.setAssistencias(3);
        jg.setCartoesAmarelos(2); jg.setCartoesVermelhos(1);
        assertAll(
            () -> assertEquals(5, jg.getGolos()),
            () -> assertEquals(3, jg.getAssistencias()),
            () -> assertEquals(2, jg.getCartoesAmarelos()),
            () -> assertEquals(1, jg.getCartoesVermelhos())
        );
    }
}
