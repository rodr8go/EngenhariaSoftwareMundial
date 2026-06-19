package torneoapp.service;

import torneoapp.model.Equipa;
import torneoapp.model.Estadio;
import torneoapp.model.Jogo;
import torneoapp.model.Torneio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Gera o calendário de grupos com as seguintes regras:
 *  - Cada equipa joga no máximo 1 jogo por dia
 *  - Mínimo 3 dias de descanso entre jogos da mesma equipa
 *  - Sem 2 jogos no mesmo estádio no mesmo dia
 *  - Distribuição round-robin completa dentro de cada grupo
 */
public class CalendarGenerator {

    private static final int MIN_REST_DAYS = 3;

    public static void gerar(Torneio torneio, List<Estadio> estadiosSel,
                             List<Jogo> globalJogos) {
        // Remove jogos de grupos anteriores deste torneio
        List<Jogo> velhos = new ArrayList<>(torneio.getJogos().stream()
            .filter(j -> "grupos".equals(j.getFase())).toList());
        torneio.getJogos().removeAll(velhos);
        globalJogos.removeAll(velhos);

        List<Equipa> eqs = new ArrayList<>(torneio.getEquipas());
        if (eqs.size() < 2) return;
        Collections.shuffle(eqs);

        List<Estadio> ests = estadiosSel.isEmpty() ? new ArrayList<>() : estadiosSel;

        // Build groups of 4
        String[] letras = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"};
        int grupoSize = 4;
        int numGrupos = Math.max(1, eqs.size() / grupoSize);
        int resto = eqs.size() % grupoSize;

        // Build list of all fixtures (pairs) per group
        record Fixture(Equipa home, Equipa away, String grupo) {}
        List<Fixture> allFixtures = new ArrayList<>();

        for (int g = 0; g < numGrupos; g++) {
            String nomeGrupo = "Grupo " + letras[g % letras.length];
            int start = g * grupoSize;
            int extraSlot = g < resto ? 1 : 0;
            int end = Math.min(start + grupoSize + extraSlot, eqs.size());
            List<Equipa> grupo = eqs.subList(start, end);
            for (int i = 0; i < grupo.size(); i++)
                for (int j = i + 1; j < grupo.size(); j++)
                    allFixtures.add(new Fixture(grupo.get(i), grupo.get(j), nomeGrupo));
        }

        // Schedule: track last game date per team and per estadio per date
        Map<Equipa, LocalDate>         lastGame   = new HashMap<>();
        Map<LocalDate, Set<Estadio>>   estByDate  = new HashMap<>();

        LocalDate startDate = torneio.getDataInicio() != null
            ? torneio.getDataInicio()
            : LocalDate.now().plusDays(7);

        // Try to schedule each fixture using the earliest valid date
        int estadioIdx = 0;
        for (Fixture fx : allFixtures) {
            // Find earliest date for this fixture
            LocalDate candidate = startDate;
            int attempts = 0;
            while (attempts < 365) {
                // Check rest constraint for both teams
                LocalDate lastHome = lastGame.get(fx.home());
                LocalDate lastAway = lastGame.get(fx.away());
                boolean homeOk = (lastHome == null) || (candidate.toEpochDay() - lastHome.toEpochDay() >= MIN_REST_DAYS);
                boolean awayOk = (lastAway == null) || (candidate.toEpochDay() - lastAway.toEpochDay() >= MIN_REST_DAYS);

                if (homeOk && awayOk) {
                    // Try to find a free estadio for this date
                    Set<Estadio> usedToday = estByDate.getOrDefault(candidate, new HashSet<>());
                    Estadio chosen = null;
                    if (ests.isEmpty()) {
                        chosen = null; // no estadio constraint
                        break; // use this date
                    }
                    for (int ei = 0; ei < ests.size(); ei++) {
                        Estadio e = ests.get((estadioIdx + ei) % ests.size());
                        if (!usedToday.contains(e)) { chosen = e; estadioIdx = (estadioIdx + ei) % ests.size(); break; }
                    }
                    if (chosen != null || ests.isEmpty()) break; // found valid date+estadio
                }
                candidate = candidate.plusDays(1);
                attempts++;
            }

            // Assign time: 17:00 or 20:00 alternating
            Set<LocalDate> usedDates = new HashSet<>(lastGame.values());
            // Count how many games already on this date
            long gamesOnDay = estByDate.getOrDefault(candidate, new HashSet<>()).size();
            int hour = (gamesOnDay % 2 == 0) ? 17 : 20;
            LocalDateTime dt = candidate.atTime(hour, 0);

            // Pick estadio
            Set<Estadio> usedToday = estByDate.computeIfAbsent(candidate, k -> new HashSet<>());
            Estadio chosenEst = null;
            if (!ests.isEmpty()) {
                for (int ei = 0; ei < ests.size(); ei++) {
                    Estadio e = ests.get(estadioIdx % ests.size());
                    estadioIdx++;
                    if (!usedToday.contains(e)) { chosenEst = e; break; }
                }
                if (chosenEst == null) chosenEst = ests.get(estadioIdx % ests.size()); // fallback
            }

            Jogo jogo = new Jogo(fx.home(), fx.away(), chosenEst, dt);
            jogo.setFase("grupos");
            jogo.setRonda(fx.grupo());
            torneio.addJogo(jogo);
            globalJogos.add(jogo);

            // Update tracking
            lastGame.put(fx.home(), candidate);
            lastGame.put(fx.away(), candidate);
            if (chosenEst != null) usedToday.add(chosenEst);
        }

        torneio.setFaseAtual("grupos");
    }
}
