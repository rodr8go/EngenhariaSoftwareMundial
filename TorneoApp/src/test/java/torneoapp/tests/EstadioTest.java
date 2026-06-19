package torneoapp.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import torneoapp.model.*;

public class EstadioTest {

    @Test
    public void addERemoveSetor() {
        Estadio e = new Estadio("Dragão", "Via FC Porto", "Porto");
        Setor s1 = new Setor("Norte", 12000);
        Setor s2 = new Setor("Sul",   10000);
        e.addSetor(s1); e.addSetor(s2);
        assertEquals(2, e.getSetores().size());
        e.removeSetor(s1);
        assertEquals(1, e.getSetores().size());
        assertTrue(e.getSetores().contains(s2));
    }

    @Test
    public void bilhetePrecoESetor() {
        Setor s = new Setor("Tribuna", 5000);
        Bilhete b = new Bilhete(null, s, "111222333", 45.50, 10);
        assertEquals(45.50, b.getPrecoFinal(), 0.001);
        assertEquals(s, b.getSetor());
    }
}
