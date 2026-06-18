package torneoapp.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import torneoapp.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TorneioTest {

    private Equipa eq(String n) { return new Equipa(n, "", "PT"); }
    private Estadio est() { return new Estadio("Luz", "Av.", "Lisboa"); }

    private Jogo jogoTerminado(Equipa c, Equipa v) {
        Jogo j = new Jogo(c, v, est(), LocalDateTime.now());
        j.setGolosCasa(1); j.setFase("grupos"); j.setTerminado(true);
        return j;
    }

    @Test
    public void faseGruposCompletaQuandoTodosTerminados() {
        Torneio t = new Torneio("T", LocalDate.now(), 4);
        Equipa a=eq("A"), b=eq("B"), c=eq("C"), d=eq("D");
        t.addJogo(jogoTerminado(a,b)); t.addJogo(jogoTerminado(c,d));
        t.addJogo(jogoTerminado(a,c)); t.addJogo(jogoTerminado(b,d));
        t.addJogo(jogoTerminado(a,d)); t.addJogo(jogoTerminado(b,c));
        assertTrue(t.faseGruposCompleta());
    }

    @Test
    public void faseGruposNaoCompletaComPendente() {
        Torneio t = new Torneio("T", LocalDate.now(), 2);
        t.addJogo(jogoTerminado(eq("A"), eq("B")));
        Jogo p = new Jogo(eq("C"), eq("D"), est(), LocalDateTime.now());
        p.setFase("grupos"); t.addJogo(p);
        assertFalse(t.faseGruposCompleta());
    }

    @Test
    public void faseGruposVaziaRetornaFalso() {
        assertFalse(new Torneio("T", LocalDate.now(), 0).faseGruposCompleta());
    }

    @Test
    public void addEquipaFicaNaLista() {
        Torneio t = new Torneio("T", LocalDate.now(), 2);
        Equipa e = eq("Brasil");
        t.addEquipa(e);
        assertEquals(1, t.getEquipas().size());
        assertTrue(t.getEquipas().contains(e));
    }
}
