package torneoapp.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import torneoapp.model.*;
import java.time.LocalDateTime;

public class JogoTest {

    private Equipa casa()   { return new Equipa("Portugal", "", "Lisboa"); }
    private Equipa visit()  { return new Equipa("Espanha",  "", "Madrid"); }
    private Estadio est()   { return new Estadio("Luz", "Av. Lusíada", "Lisboa"); }
    private Jogo novoJogo() { return new Jogo(casa(), visit(), est(), LocalDateTime.now()); }

    @Test
    public void vencedorNormalCasaGanha() {
        Jogo j = novoJogo();
        j.setGolosCasa(2); j.setGolosVisitante(1); j.setTerminado(true);
        assertEquals(j.getEquipaCasa(), j.getVencedor());
    }

    @Test
    public void vencedorNormalVisitanteGanha() {
        Jogo j = novoJogo();
        j.setGolosCasa(0); j.setGolosVisitante(3); j.setTerminado(true);
        assertEquals(j.getEquipaVisitante(), j.getVencedor());
    }

    @Test
    public void empateGruposRetornaNull() {
        Jogo j = novoJogo();
        j.setGolosCasa(1); j.setGolosVisitante(1); j.setTerminado(true);
        assertNull(j.getVencedor());
    }

    @Test
    public void vencedorViaProlongamento() {
        Jogo j = novoJogo();
        j.setGolosCasa(1); j.setGolosVisitante(1);
        j.setFoiProlongamento(true);
        j.setGolosCasaProlongamento(1); j.setGolosVisitanteProlongamento(0);
        j.setTerminado(true);
        assertEquals(j.getEquipaCasa(), j.getVencedor());
    }

    @Test
    public void vencedorViaPenaltis() {
        Jogo j = novoJogo();
        j.setGolosCasa(1); j.setGolosVisitante(1);
        j.setFoiProlongamento(true); j.setFoiPenaltis(true);
        j.setPenaltisCasa(4); j.setPenaltisVisitante(3);
        j.setTerminado(true);
        assertEquals(j.getEquipaCasa(), j.getVencedor());
    }
    @Test
    public void penaltisVisitanteGanha() {
        Jogo j = novoJogo();
        j.setGolosCasa(0); j.setGolosVisitante(0);
        j.setFoiProlongamento(true); j.setFoiPenaltis(true);
        j.setPenaltisCasa(2); j.setPenaltisVisitante(5);
        j.setTerminado(true);
        assertEquals(j.getEquipaVisitante(), j.getVencedor());
    }

    @Test
    public void golosTotalIncluiProlongamento() {
        Jogo j = novoJogo();
        j.setGolosCasa(1); j.setGolosVisitante(1);
        j.setFoiProlongamento(true);
        j.setGolosCasaProlongamento(2); j.setGolosVisitanteProlongamento(1);
        assertEquals(3, j.getGolosTotalCasa());
        assertEquals(2, j.getGolosTotalVisitante());
    }

    @Test
    public void jogoNaoTerminadoVencedorNull() {
        Jogo j = novoJogo();
        j.setGolosCasa(5); j.setGolosVisitante(0);
        assertNull(j.getVencedor());
    }

    @Test
    public void receitaBilheteiraSomaBilhetes() {
        Jogo j = novoJogo();
        Setor s = new Setor("Norte", 5000);
        j.addBilhete(new Bilhete(j, s, "123", 25.0, 1));
        j.addBilhete(new Bilhete(j, s, "456", 35.0, 2));
        assertEquals(60.0, j.getReceitaBilheteira(), 0.001);
    }

    @Test
    public void faseDefaultEGrupos() {
        assertEquals("grupos", novoJogo().getFase());
    }

}
