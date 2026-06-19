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

}
