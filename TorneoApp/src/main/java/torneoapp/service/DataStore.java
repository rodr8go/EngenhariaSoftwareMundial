package torneoapp.service;

import torneoapp.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore {
    private static DataStore instance;

    private List<Estadio>  estadios     = new ArrayList<>();
    private List<Equipa>   equipas      = new ArrayList<>();
    private List<Jogador>  jogadores    = new ArrayList<>();
    private List<Treinador> treinadores = new ArrayList<>();
    private List<Patrocinador> patrocinadores = new ArrayList<>();
    private List<ContratoPatrocinio> contratos = new ArrayList<>();
    private List<Torneio>  torneios     = new ArrayList<>();
    private List<Jogo>     jogos        = new ArrayList<>();
    private List<Bilhete>  bilhetes     = new ArrayList<>();
    private Torneio torneioAtual = null;

    private DataStore() {
        if (PersistenceManager.hasSavedData()) {
            PersistenceManager.load(this);
        } else {
            loadSampleData();
        }
    }

    public void save() {
        PersistenceManager.save(this);
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public Torneio getTorneioAtual() { return torneioAtual; }
    public void setTorneioAtual(Torneio t) { this.torneioAtual = t; }

    public Torneio findTorneioByName(String nome) {
        if (nome == null) return null;
        return torneios.stream()
            .filter(t -> t.getNome().equalsIgnoreCase(nome.trim()))
            .findFirst().orElse(null);
    }

    // ── Geração do calendário completo round-robin ────────────────────────────
    /**
     * Gera TODOS os jogos da fase de grupos.
     * - 4 equipas por grupo → 6 jogos por grupo (cada par joga 1x)
     * - 8 grupos × 6 = 48 jogos para 32 equipas
     * - Distribui estádios de forma rotativa
     * - Espaça os jogos em slots de 3 dias
     */
    public void gerarCalendarioGrupos(Torneio torneio) {
        CalendarGenerator.gerar(torneio, estadios, jogos);
    }

    // ── Fase de eliminação ───────────────────────────────────────────────────
    public void gerarFaseEliminacao(Torneio torneio) {
        // Top 2 de cada grupo
        List<Equipa> classificados = new ArrayList<>();
        Map<String, List<Jogo>> porGrupo = new LinkedHashMap<>();
        for (Jogo j : torneio.getJogos()) {
            if ("grupos".equals(j.getFase()))
                porGrupo.computeIfAbsent(j.getRonda(), k -> new ArrayList<>()).add(j);
        }

        for (Map.Entry<String, List<Jogo>> entry : porGrupo.entrySet()) {
            Map<Equipa, int[]> stats = new LinkedHashMap<>();
            for (Jogo j : entry.getValue()) {
                if (j.getEquipaCasa() != null) stats.putIfAbsent(j.getEquipaCasa(), new int[2]);
                if (j.getEquipaVisitante() != null) stats.putIfAbsent(j.getEquipaVisitante(), new int[2]);
                if (j.isTerminado()) {
                    int[] c = stats.get(j.getEquipaCasa());
                    int[] v = stats.get(j.getEquipaVisitante());
                    if (c != null) { c[1] += j.getGolosCasa() - j.getGolosVisitante(); if (j.getGolosCasa() > j.getGolosVisitante()) c[0] += 3; else if (j.getGolosCasa() == j.getGolosVisitante()) c[0]++; }
                    if (v != null) { v[1] += j.getGolosVisitante() - j.getGolosCasa(); if (j.getGolosVisitante() > j.getGolosCasa()) v[0] += 3; else if (j.getGolosCasa() == j.getGolosVisitante()) v[0]++; }
                }
            }
            stats.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0] != a.getValue()[0] ? b.getValue()[0]-a.getValue()[0] : b.getValue()[1]-a.getValue()[1])
                .limit(2).map(Map.Entry::getKey).forEach(classificados::add);
        }

        if (classificados.size() < 2) classificados = new ArrayList<>(torneio.getEquipas());
        if (classificados.size() < 2) return;

        // Remove eliminação anterior
        List<Jogo> velhos = new ArrayList<>(torneio.getJogos().stream()
            .filter(j -> "eliminacao".equals(j.getFase())).toList());
        torneio.getJogos().removeAll(velhos);
        jogos.removeAll(velhos);

        int n = classificados.size();
        String ronda = n <= 2 ? "Final" : n <= 4 ? "Meias-Finais" : n <= 8 ? "Quartos" : "Oitavos";

        LocalDateTime dt = LocalDateTime.now().plusDays(21).withHour(20).withMinute(0);
        int pos = 0; int estadioIdx = 0;
        for (int i = 0; i + 1 < classificados.size(); i += 2) {
            Estadio est = estadios.isEmpty() ? null : estadios.get(estadioIdx++ % estadios.size());
            Jogo jogo = new Jogo(classificados.get(i), classificados.get(i+1), est, dt.plusDays(pos * 4L));
            jogo.setFase("eliminacao");
            jogo.setRonda(ronda);
            jogo.setPosicaoBracket(pos++);
            torneio.addJogo(jogo);
            jogos.add(jogo);
        }
        torneio.setFaseAtual("eliminacao");
    }

    private Setor makeSetor(String nome, int lotacao, double preco) {
        Setor s = new Setor(nome, lotacao);
        s.setPreco(preco);
        return s;
    }

    private void loadSampleData() {
        // ── Estádios ─────────────────────────────────────────────────────────
        Estadio luz = new Estadio("Estádio da Luz", "Av. Lusíada, Lisboa", "Lisboa");
        luz.addSetor(makeSetor("Bancada Principal", 40000, 50.0));
        luz.addSetor(makeSetor("Camarote", 5000, 150.0));
        luz.addSetor(makeSetor("Bancada Topo", 15000, 30.0));
        Estadio alvalade = new Estadio("Estádio José Alvalade", "Campo Grande, Lisboa", "Lisboa");
        alvalade.addSetor(makeSetor("Bancada Norte", 20000, 45.0));
        alvalade.addSetor(makeSetor("Bancada Sul", 20000, 45.0));
        alvalade.addSetor(makeSetor("Camarote VIP", 5000, 120.0));
        Estadio dragao = new Estadio("Estádio do Dragão", "Via Futebol Clube do Porto, Porto", "Porto");
        dragao.addSetor(makeSetor("Bancada", 45000, 40.0));
        dragao.addSetor(makeSetor("Camarote", 5000, 100.0));
        Estadio bernabeu = new Estadio("Santiago Bernabéu", "Av. de Concha Espina, Madrid", "Madrid");
        bernabeu.addSetor(makeSetor("Bancada Principal", 70000, 80.0));
        bernabeu.addSetor(makeSetor("Camarote", 11044, 200.0));
        estadios.addAll(java.util.List.of(luz, alvalade, dragao, bernabeu));

        // ── Treinadores ──────────────────────────────────────────────────────
        Treinador portugalCoach = new Treinador("Roberto Martínez", LocalDate.of(1972,1,1), "Espanha", "roberto.martínez@torneio.com", "901000000");
        treinadores.add(portugalCoach);
        Treinador francaCoach = new Treinador("Didier Deschamps", LocalDate.of(1972,1,1), "França", "didier.deschamps@torneio.com", "911000010");
        treinadores.add(francaCoach);
        Treinador espanhaCoach = new Treinador("Luis de la Fuente", LocalDate.of(1972,1,1), "Espanha", "luis.de.la.fuente@torneio.com", "921000020");
        treinadores.add(espanhaCoach);
        Treinador brasilCoach = new Treinador("Dorival Júnior", LocalDate.of(1972,1,1), "Brasil", "dorival.júnior@torneio.com", "931000030");
        treinadores.add(brasilCoach);
        Treinador argentinaCoach = new Treinador("Lionel Scaloni", LocalDate.of(1972,1,1), "Argentina", "lionel.scaloni@torneio.com", "941000040");
        treinadores.add(argentinaCoach);
        Treinador alemanhaCoach = new Treinador("Julian Nagelsmann", LocalDate.of(1972,1,1), "Alemanha", "julian.nagelsmann@torneio.com", "951000050");
        treinadores.add(alemanhaCoach);
        Treinador inglaterraCoach = new Treinador("Gareth Southgate", LocalDate.of(1972,1,1), "Inglaterra", "gareth.southgate@torneio.com", "961000060");
        treinadores.add(inglaterraCoach);
        Treinador marrocosCoach = new Treinador("Walid Regragui", LocalDate.of(1972,1,1), "Marrocos", "walid.regragui@torneio.com", "971000070");
        treinadores.add(marrocosCoach);

        // ── Equipas e Jogadores ──────────────────────────────────────────────
        Equipa portugal = new Equipa("Portugal", "Seleção Nacional de Portugal", "Portugal");
        portugal.setTreinador(portugalCoach); portugalCoach.setEquipa(portugal);
        Jogador p0_0 = new Jogador("Rui Patrício", "Patrício", "Guarda-Redes", LocalDate.of(1995,1,1), "Portugal", 175, 1);
        portugal.addJogador(p0_0); jogadores.add(p0_0);
        Jogador p0_1 = new Jogador("João Cancelo", "Cancelo", "Defesa Central", LocalDate.of(1995,2,2), "Portugal", 176, 2);
        portugal.addJogador(p0_1); jogadores.add(p0_1);
        Jogador p0_2 = new Jogador("Pepe", "Pepe", "Defesa Central", LocalDate.of(1995,3,3), "Portugal", 177, 3);
        portugal.addJogador(p0_2); jogadores.add(p0_2);
        Jogador p0_3 = new Jogador("Rúben Dias", "R.Dias", "Defesa Esquerdo", LocalDate.of(1995,4,4), "Portugal", 178, 4);
        portugal.addJogador(p0_3); jogadores.add(p0_3);
        Jogador p0_4 = new Jogador("Nuno Mendes", "N.Mendes", "Defesa Direito", LocalDate.of(1995,5,5), "Portugal", 179, 5);
        portugal.addJogador(p0_4); jogadores.add(p0_4);
        Jogador p0_5 = new Jogador("João Palhinha", "Palhinha", "Médio Defensivo", LocalDate.of(1995,6,6), "Portugal", 180, 6);
        portugal.addJogador(p0_5); jogadores.add(p0_5);
        Jogador p0_6 = new Jogador("Bruno Fernandes", "B.Fernandes", "Médio Central", LocalDate.of(1995,7,7), "Portugal", 181, 7);
        portugal.addJogador(p0_6); jogadores.add(p0_6);
        Jogador p0_7 = new Jogador("Bernardo Silva", "Bernardo", "Médio Central", LocalDate.of(1995,8,8), "Portugal", 182, 8);
        portugal.addJogador(p0_7); jogadores.add(p0_7);
        Jogador p0_8 = new Jogador("João Félix", "J.Félix", "Médio Ofensivo", LocalDate.of(1995,9,9), "Portugal", 183, 9);
        portugal.addJogador(p0_8); jogadores.add(p0_8);
        Jogador p0_9 = new Jogador("Rafael Leão", "Leão", "Extremo Esquerdo", LocalDate.of(1995,10,10), "Portugal", 184, 10);
        portugal.addJogador(p0_9); jogadores.add(p0_9);
        Jogador p0_10 = new Jogador("Cristiano Ronaldo", "Ronaldo", "Extremo Direito", LocalDate.of(1995,11,11), "Portugal", 185, 11);
        portugal.addJogador(p0_10); jogadores.add(p0_10);
        Jogador p0_11 = new Jogador("Gonçalo Ramos", "G.Ramos", "Avançado", LocalDate.of(1995,12,12), "Portugal", 186, 12);
        portugal.addJogador(p0_11); jogadores.add(p0_11);
        Jogador p0_12 = new Jogador("Diogo Costa", "D.Costa", "Defesa Central", LocalDate.of(1995,1,13), "Portugal", 187, 13);
        portugal.addJogador(p0_12); jogadores.add(p0_12);
        Jogador p0_13 = new Jogador("Pedro Neto", "P.Neto", "Defesa Direito", LocalDate.of(1995,2,14), "Portugal", 188, 14);
        portugal.addJogador(p0_13); jogadores.add(p0_13);
        Jogador p0_14 = new Jogador("Vitinha", "Vitinha", "Médio Defensivo", LocalDate.of(1995,3,15), "Portugal", 189, 15);
        portugal.addJogador(p0_14); jogadores.add(p0_14);
        Jogador p0_15 = new Jogador("Rúben Neves", "R.Neves", "Médio Central", LocalDate.of(1995,4,16), "Portugal", 175, 16);
        portugal.addJogador(p0_15); jogadores.add(p0_15);
        Jogador p0_16 = new Jogador("Otávio", "Otávio", "Extremo Esquerdo", LocalDate.of(1995,5,17), "Brasil", 176, 17);
        portugal.addJogador(p0_16); jogadores.add(p0_16);
        Jogador p0_17 = new Jogador("André Silva", "A.Silva", "Avançado", LocalDate.of(1995,6,18), "Portugal", 177, 18);
        portugal.addJogador(p0_17); jogadores.add(p0_17);
        Jogador p0_18 = new Jogador("José Sá", "J.Sá", "Guarda-Redes", LocalDate.of(1995,7,19), "Portugal", 178, 19);
        portugal.addJogador(p0_18); jogadores.add(p0_18);
        Jogador p0_19 = new Jogador("Nélson Semedo", "Semedo", "Defesa Esquerdo", LocalDate.of(1995,8,20), "Portugal", 179, 20);
        portugal.addJogador(p0_19); jogadores.add(p0_19);
        Jogador p0_20 = new Jogador("Matheus Nunes", "M.Nunes", "Médio Ofensivo", LocalDate.of(1995,9,21), "Portugal", 180, 21);
        portugal.addJogador(p0_20); jogadores.add(p0_20);
        Jogador p0_21 = new Jogador("Francisco Conceição", "F.Conceição", "Extremo Direito", LocalDate.of(1995,10,22), "Portugal", 181, 22);
        portugal.addJogador(p0_21); jogadores.add(p0_21);
        Jogador p0_22 = new Jogador("Gonçalo Guedes", "Guedes", "Avançado", LocalDate.of(1995,11,23), "Portugal", 182, 23);
        portugal.addJogador(p0_22); jogadores.add(p0_22);
        Jogador p0_23 = new Jogador("Danilo Pereira", "Danilo", "Defesa Central", LocalDate.of(1995,12,24), "Portugal", 183, 24);
        portugal.addJogador(p0_23); jogadores.add(p0_23);
        Jogador p0_24 = new Jogador("William Carvalho", "W.Carvalho", "Médio Central", LocalDate.of(1995,1,25), "Portugal", 184, 25);
        portugal.addJogador(p0_24); jogadores.add(p0_24);
        Jogador p0_25 = new Jogador("Francisco Trincão", "Trincão", "Avançado", LocalDate.of(1995,2,26), "Portugal", 185, 26);
        portugal.addJogador(p0_25); jogadores.add(p0_25);
        equipas.add(portugal);

        Equipa franca = new Equipa("França", "Seleção Nacional de França", "França");
        franca.setTreinador(francaCoach); francaCoach.setEquipa(franca);
        Jogador p1_0 = new Jogador("Mike Maignan", "Maignan", "Guarda-Redes", LocalDate.of(1995,1,1), "França", 175, 1);
        franca.addJogador(p1_0); jogadores.add(p1_0);
        Jogador p1_1 = new Jogador("Jules Koundé", "Koundé", "Defesa Central", LocalDate.of(1995,2,2), "França", 176, 2);
        franca.addJogador(p1_1); jogadores.add(p1_1);
        Jogador p1_2 = new Jogador("Dayot Upamecano", "Upamecano", "Defesa Central", LocalDate.of(1995,3,3), "França", 177, 3);
        franca.addJogador(p1_2); jogadores.add(p1_2);
        Jogador p1_3 = new Jogador("William Saliba", "Saliba", "Defesa Esquerdo", LocalDate.of(1995,4,4), "França", 178, 4);
        franca.addJogador(p1_3); jogadores.add(p1_3);
        Jogador p1_4 = new Jogador("Théo Hernández", "T.Hernández", "Defesa Direito", LocalDate.of(1995,5,5), "França", 179, 5);
        franca.addJogador(p1_4); jogadores.add(p1_4);
        Jogador p1_5 = new Jogador("Aurélien Tchouaméni", "Tchouaméni", "Médio Defensivo", LocalDate.of(1995,6,6), "França", 180, 6);
        franca.addJogador(p1_5); jogadores.add(p1_5);
        Jogador p1_6 = new Jogador("Adrien Rabiot", "Rabiot", "Médio Central", LocalDate.of(1995,7,7), "França", 181, 7);
        franca.addJogador(p1_6); jogadores.add(p1_6);
        Jogador p1_7 = new Jogador("Eduardo Camavinga", "Camavinga", "Médio Central", LocalDate.of(1995,8,8), "França", 182, 8);
        franca.addJogador(p1_7); jogadores.add(p1_7);
        Jogador p1_8 = new Jogador("Antoine Griezmann", "Griezmann", "Médio Ofensivo", LocalDate.of(1995,9,9), "França", 183, 9);
        franca.addJogador(p1_8); jogadores.add(p1_8);
        Jogador p1_9 = new Jogador("Ousmane Dembélé", "Dembélé", "Extremo Esquerdo", LocalDate.of(1995,10,10), "França", 184, 10);
        franca.addJogador(p1_9); jogadores.add(p1_9);
        Jogador p1_10 = new Jogador("Kylian Mbappé", "Mbappé", "Extremo Direito", LocalDate.of(1995,11,11), "França", 185, 11);
        franca.addJogador(p1_10); jogadores.add(p1_10);
        Jogador p1_11 = new Jogador("Olivier Giroud", "Giroud", "Avançado", LocalDate.of(1995,12,12), "França", 186, 12);
        franca.addJogador(p1_11); jogadores.add(p1_11);
        Jogador p1_12 = new Jogador("Alphonse Areola", "Areola", "Defesa Central", LocalDate.of(1995,1,13), "França", 187, 13);
        franca.addJogador(p1_12); jogadores.add(p1_12);
        Jogador p1_13 = new Jogador("Benjamin Pavard", "Pavard", "Defesa Direito", LocalDate.of(1995,2,14), "França", 188, 14);
        franca.addJogador(p1_13); jogadores.add(p1_13);
        Jogador p1_14 = new Jogador("N'Golo Kanté", "Kanté", "Médio Defensivo", LocalDate.of(1995,3,15), "França", 189, 15);
        franca.addJogador(p1_14); jogadores.add(p1_14);
        Jogador p1_15 = new Jogador("Matteo Guendouzi", "Guendouzi", "Médio Central", LocalDate.of(1995,4,16), "França", 175, 16);
        franca.addJogador(p1_15); jogadores.add(p1_15);
        Jogador p1_16 = new Jogador("Kingsley Coman", "Coman", "Extremo Esquerdo", LocalDate.of(1995,5,17), "França", 176, 17);
        franca.addJogador(p1_16); jogadores.add(p1_16);
        Jogador p1_17 = new Jogador("Randal Kolo Muani", "Kolo Muani", "Avançado", LocalDate.of(1995,6,18), "França", 177, 18);
        franca.addJogador(p1_17); jogadores.add(p1_17);
        Jogador p1_18 = new Jogador("Steve Mandanda", "Mandanda", "Guarda-Redes", LocalDate.of(1995,7,19), "França", 178, 19);
        franca.addJogador(p1_18); jogadores.add(p1_18);
        Jogador p1_19 = new Jogador("Lucas Digne", "Digne", "Defesa Esquerdo", LocalDate.of(1995,8,20), "França", 179, 20);
        franca.addJogador(p1_19); jogadores.add(p1_19);
        Jogador p1_20 = new Jogador("Youssouf Fofana", "Fofana", "Médio Ofensivo", LocalDate.of(1995,9,21), "França", 180, 21);
        franca.addJogador(p1_20); jogadores.add(p1_20);
        Jogador p1_21 = new Jogador("Marcus Thuram", "M.Thuram", "Extremo Direito", LocalDate.of(1995,10,22), "França", 181, 22);
        franca.addJogador(p1_21); jogadores.add(p1_21);
        Jogador p1_22 = new Jogador("Wissam Ben Yedder", "Ben Yedder", "Avançado", LocalDate.of(1995,11,23), "França", 182, 23);
        franca.addJogador(p1_22); jogadores.add(p1_22);
        Jogador p1_23 = new Jogador("Raphaël Varane", "Varane", "Defesa Central", LocalDate.of(1995,12,24), "França", 183, 24);
        franca.addJogador(p1_23); jogadores.add(p1_23);
        Jogador p1_24 = new Jogador("Paul Pogba", "Pogba", "Médio Central", LocalDate.of(1995,1,25), "França", 184, 25);
        franca.addJogador(p1_24); jogadores.add(p1_24);
        Jogador p1_25 = new Jogador("Lamine Yamal", "Yamal", "Avançado", LocalDate.of(1995,2,26), "Espanha", 185, 26);
        franca.addJogador(p1_25); jogadores.add(p1_25);
        equipas.add(franca);

        Equipa espanha = new Equipa("Espanha", "Seleção Nacional de Espanha", "Espanha");
        espanha.setTreinador(espanhaCoach); espanhaCoach.setEquipa(espanha);
        Jogador p2_0 = new Jogador("Unai Simón", "U.Simón", "Guarda-Redes", LocalDate.of(1995,1,1), "Espanha", 175, 1);
        espanha.addJogador(p2_0); jogadores.add(p2_0);
        Jogador p2_1 = new Jogador("Daniel Carvajal", "Carvajal", "Defesa Central", LocalDate.of(1995,2,2), "Espanha", 176, 2);
        espanha.addJogador(p2_1); jogadores.add(p2_1);
        Jogador p2_2 = new Jogador("Pau Cubarsí", "Cubarsí", "Defesa Central", LocalDate.of(1995,3,3), "Espanha", 177, 3);
        espanha.addJogador(p2_2); jogadores.add(p2_2);
        Jogador p2_3 = new Jogador("Aymeric Laporte", "Laporte", "Defesa Esquerdo", LocalDate.of(1995,4,4), "Espanha", 178, 4);
        espanha.addJogador(p2_3); jogadores.add(p2_3);
        Jogador p2_4 = new Jogador("Alejandro Balde", "Balde", "Defesa Direito", LocalDate.of(1995,5,5), "Espanha", 179, 5);
        espanha.addJogador(p2_4); jogadores.add(p2_4);
        Jogador p2_5 = new Jogador("Rodri", "Rodri", "Médio Defensivo", LocalDate.of(1995,6,6), "Espanha", 180, 6);
        espanha.addJogador(p2_5); jogadores.add(p2_5);
        Jogador p2_6 = new Jogador("Pedri", "Pedri", "Médio Central", LocalDate.of(1995,7,7), "Espanha", 181, 7);
        espanha.addJogador(p2_6); jogadores.add(p2_6);
        Jogador p2_7 = new Jogador("Gavi", "Gavi", "Médio Central", LocalDate.of(1995,8,8), "Espanha", 182, 8);
        espanha.addJogador(p2_7); jogadores.add(p2_7);
        Jogador p2_8 = new Jogador("Dani Olmo", "D.Olmo", "Médio Ofensivo", LocalDate.of(1995,9,9), "Espanha", 183, 9);
        espanha.addJogador(p2_8); jogadores.add(p2_8);
        Jogador p2_9 = new Jogador("Lamine Yamal", "Yamal", "Extremo Esquerdo", LocalDate.of(1995,10,10), "Espanha", 184, 10);
        espanha.addJogador(p2_9); jogadores.add(p2_9);
        Jogador p2_10 = new Jogador("Álvaro Morata", "Morata", "Extremo Direito", LocalDate.of(1995,11,11), "Espanha", 185, 11);
        espanha.addJogador(p2_10); jogadores.add(p2_10);
        Jogador p2_11 = new Jogador("Mikel Oyarzabal", "Oyarzabal", "Avançado", LocalDate.of(1995,12,12), "Espanha", 186, 12);
        espanha.addJogador(p2_11); jogadores.add(p2_11);
        Jogador p2_12 = new Jogador("David Raya", "Raya", "Defesa Central", LocalDate.of(1995,1,13), "Espanha", 187, 13);
        espanha.addJogador(p2_12); jogadores.add(p2_12);
        Jogador p2_13 = new Jogador("Jesús Navas", "J.Navas", "Defesa Direito", LocalDate.of(1995,2,14), "Espanha", 188, 14);
        espanha.addJogador(p2_13); jogadores.add(p2_13);
        Jogador p2_14 = new Jogador("Fabián Ruiz", "F.Ruiz", "Médio Defensivo", LocalDate.of(1995,3,15), "Espanha", 189, 15);
        espanha.addJogador(p2_14); jogadores.add(p2_14);
        Jogador p2_15 = new Jogador("Mikel Merino", "Merino", "Médio Central", LocalDate.of(1995,4,16), "Espanha", 175, 16);
        espanha.addJogador(p2_15); jogadores.add(p2_15);
        Jogador p2_16 = new Jogador("Nico Williams", "N.Williams", "Extremo Esquerdo", LocalDate.of(1995,5,17), "Espanha", 176, 17);
        espanha.addJogador(p2_16); jogadores.add(p2_16);
        Jogador p2_17 = new Jogador("Joselu", "Joselu", "Avançado", LocalDate.of(1995,6,18), "Espanha", 177, 18);
        espanha.addJogador(p2_17); jogadores.add(p2_17);
        Jogador p2_18 = new Jogador("Kepa Arrizabalaga", "Kepa", "Guarda-Redes", LocalDate.of(1995,7,19), "Espanha", 178, 19);
        espanha.addJogador(p2_18); jogadores.add(p2_18);
        Jogador p2_19 = new Jogador("Marcos Alonso", "M.Alonso", "Defesa Esquerdo", LocalDate.of(1995,8,20), "Espanha", 179, 20);
        espanha.addJogador(p2_19); jogadores.add(p2_19);
        Jogador p2_20 = new Jogador("Martín Zubimendi", "Zubimendi", "Médio Ofensivo", LocalDate.of(1995,9,21), "Espanha", 180, 21);
        espanha.addJogador(p2_20); jogadores.add(p2_20);
        Jogador p2_21 = new Jogador("Yeremy Pino", "Pino", "Extremo Direito", LocalDate.of(1995,10,22), "Espanha", 181, 22);
        espanha.addJogador(p2_21); jogadores.add(p2_21);
        Jogador p2_22 = new Jogador("Ayoze Pérez", "Ayoze", "Avançado", LocalDate.of(1995,11,23), "Espanha", 182, 23);
        espanha.addJogador(p2_22); jogadores.add(p2_22);
        Jogador p2_23 = new Jogador("Eric García", "E.García", "Defesa Central", LocalDate.of(1995,12,24), "Espanha", 183, 24);
        espanha.addJogador(p2_23); jogadores.add(p2_23);
        Jogador p2_24 = new Jogador("Dani Vivian", "Vivian", "Médio Central", LocalDate.of(1995,1,25), "Espanha", 184, 25);
        espanha.addJogador(p2_24); jogadores.add(p2_24);
        Jogador p2_25 = new Jogador("Ferran Torres", "F.Torres", "Avançado", LocalDate.of(1995,2,26), "Espanha", 185, 26);
        espanha.addJogador(p2_25); jogadores.add(p2_25);
        equipas.add(espanha);

        Equipa brasil = new Equipa("Brasil", "Seleção Nacional de Brasil", "Brasil");
        brasil.setTreinador(brasilCoach); brasilCoach.setEquipa(brasil);
        Jogador p3_0 = new Jogador("Alisson", "Alisson", "Guarda-Redes", LocalDate.of(1995,1,1), "Brasil", 175, 1);
        brasil.addJogador(p3_0); jogadores.add(p3_0);
        Jogador p3_1 = new Jogador("Danilo", "Danilo", "Defesa Central", LocalDate.of(1995,2,2), "Brasil", 176, 2);
        brasil.addJogador(p3_1); jogadores.add(p3_1);
        Jogador p3_2 = new Jogador("Marquinhos", "Marquinhos", "Defesa Central", LocalDate.of(1995,3,3), "Brasil", 177, 3);
        brasil.addJogador(p3_2); jogadores.add(p3_2);
        Jogador p3_3 = new Jogador("Gabriel Magalhães", "G.Magalhães", "Defesa Esquerdo", LocalDate.of(1995,4,4), "Brasil", 178, 4);
        brasil.addJogador(p3_3); jogadores.add(p3_3);
        Jogador p3_4 = new Jogador("Alex Sandro", "A.Sandro", "Defesa Direito", LocalDate.of(1995,5,5), "Brasil", 179, 5);
        brasil.addJogador(p3_4); jogadores.add(p3_4);
        Jogador p3_5 = new Jogador("Casemiro", "Casemiro", "Médio Defensivo", LocalDate.of(1995,6,6), "Brasil", 180, 6);
        brasil.addJogador(p3_5); jogadores.add(p3_5);
        Jogador p3_6 = new Jogador("Lucas Paquetá", "Paquetá", "Médio Central", LocalDate.of(1995,7,7), "Brasil", 181, 7);
        brasil.addJogador(p3_6); jogadores.add(p3_6);
        Jogador p3_7 = new Jogador("Bruno Guimarães", "B.Guimarães", "Médio Central", LocalDate.of(1995,8,8), "Brasil", 182, 8);
        brasil.addJogador(p3_7); jogadores.add(p3_7);
        Jogador p3_8 = new Jogador("Rodrygo", "Rodrygo", "Médio Ofensivo", LocalDate.of(1995,9,9), "Brasil", 183, 9);
        brasil.addJogador(p3_8); jogadores.add(p3_8);
        Jogador p3_9 = new Jogador("Vinícius Júnior", "Vini Jr", "Extremo Esquerdo", LocalDate.of(1995,10,10), "Brasil", 184, 10);
        brasil.addJogador(p3_9); jogadores.add(p3_9);
        Jogador p3_10 = new Jogador("Richarlison", "Richarlison", "Extremo Direito", LocalDate.of(1995,11,11), "Brasil", 185, 11);
        brasil.addJogador(p3_10); jogadores.add(p3_10);
        Jogador p3_11 = new Jogador("Gabriel Jesus", "G.Jesus", "Avançado", LocalDate.of(1995,12,12), "Brasil", 186, 12);
        brasil.addJogador(p3_11); jogadores.add(p3_11);
        Jogador p3_12 = new Jogador("Weverton", "Weverton", "Defesa Central", LocalDate.of(1995,1,13), "Brasil", 187, 13);
        brasil.addJogador(p3_12); jogadores.add(p3_12);
        Jogador p3_13 = new Jogador("Éder Militão", "Militão", "Defesa Direito", LocalDate.of(1995,2,14), "Brasil", 188, 14);
        brasil.addJogador(p3_13); jogadores.add(p3_13);
        Jogador p3_14 = new Jogador("Bremer", "Bremer", "Médio Defensivo", LocalDate.of(1995,3,15), "Brasil", 189, 15);
        brasil.addJogador(p3_14); jogadores.add(p3_14);
        Jogador p3_15 = new Jogador("Fabinho", "Fabinho", "Médio Central", LocalDate.of(1995,4,16), "Brasil", 175, 16);
        brasil.addJogador(p3_15); jogadores.add(p3_15);
        Jogador p3_16 = new Jogador("Fred", "Fred", "Extremo Esquerdo", LocalDate.of(1995,5,17), "Brasil", 176, 17);
        brasil.addJogador(p3_16); jogadores.add(p3_16);
        Jogador p3_17 = new Jogador("Raphinha", "Raphinha", "Avançado", LocalDate.of(1995,6,18), "Brasil", 177, 18);
        brasil.addJogador(p3_17); jogadores.add(p3_17);
        Jogador p3_18 = new Jogador("Ederson", "Ederson", "Guarda-Redes", LocalDate.of(1995,7,19), "Brasil", 178, 19);
        brasil.addJogador(p3_18); jogadores.add(p3_18);
        Jogador p3_19 = new Jogador("Renan Lodi", "R.Lodi", "Defesa Esquerdo", LocalDate.of(1995,8,20), "Brasil", 179, 20);
        brasil.addJogador(p3_19); jogadores.add(p3_19);
        Jogador p3_20 = new Jogador("Matheus Cunha", "M.Cunha", "Médio Ofensivo", LocalDate.of(1995,9,21), "Brasil", 180, 21);
        brasil.addJogador(p3_20); jogadores.add(p3_20);
        Jogador p3_21 = new Jogador("Antony", "Antony", "Extremo Direito", LocalDate.of(1995,10,22), "Brasil", 181, 22);
        brasil.addJogador(p3_21); jogadores.add(p3_21);
        Jogador p3_22 = new Jogador("Pedro", "Pedro", "Avançado", LocalDate.of(1995,11,23), "Brasil", 182, 23);
        brasil.addJogador(p3_22); jogadores.add(p3_22);
        Jogador p3_23 = new Jogador("Thiago Silva", "T.Silva", "Defesa Central", LocalDate.of(1995,12,24), "Brasil", 183, 24);
        brasil.addJogador(p3_23); jogadores.add(p3_23);
        Jogador p3_24 = new Jogador("Endrick", "Endrick", "Médio Central", LocalDate.of(1995,1,25), "Brasil", 184, 25);
        brasil.addJogador(p3_24); jogadores.add(p3_24);
        Jogador p3_25 = new Jogador("Savinho", "Savinho", "Avançado", LocalDate.of(1995,2,26), "Brasil", 185, 26);
        brasil.addJogador(p3_25); jogadores.add(p3_25);
        equipas.add(brasil);

        Equipa argentina = new Equipa("Argentina", "Seleção Nacional de Argentina", "Argentina");
        argentina.setTreinador(argentinaCoach); argentinaCoach.setEquipa(argentina);
        Jogador p4_0 = new Jogador("Emiliano Martínez", "E.Martínez", "Guarda-Redes", LocalDate.of(1995,1,1), "Argentina", 175, 1);
        argentina.addJogador(p4_0); jogadores.add(p4_0);
        Jogador p4_1 = new Jogador("Nahuel Molina", "Molina", "Defesa Central", LocalDate.of(1995,2,2), "Argentina", 176, 2);
        argentina.addJogador(p4_1); jogadores.add(p4_1);
        Jogador p4_2 = new Jogador("Cristian Romero", "Romero", "Defesa Central", LocalDate.of(1995,3,3), "Argentina", 177, 3);
        argentina.addJogador(p4_2); jogadores.add(p4_2);
        Jogador p4_3 = new Jogador("Lisandro Martínez", "L.Martínez", "Defesa Esquerdo", LocalDate.of(1995,4,4), "Argentina", 178, 4);
        argentina.addJogador(p4_3); jogadores.add(p4_3);
        Jogador p4_4 = new Jogador("Nicolás Tagliafico", "Tagliafico", "Defesa Direito", LocalDate.of(1995,5,5), "Argentina", 179, 5);
        argentina.addJogador(p4_4); jogadores.add(p4_4);
        Jogador p4_5 = new Jogador("Rodrigo De Paul", "De Paul", "Médio Defensivo", LocalDate.of(1995,6,6), "Argentina", 180, 6);
        argentina.addJogador(p4_5); jogadores.add(p4_5);
        Jogador p4_6 = new Jogador("Leandro Paredes", "Paredes", "Médio Central", LocalDate.of(1995,7,7), "Argentina", 181, 7);
        argentina.addJogador(p4_6); jogadores.add(p4_6);
        Jogador p4_7 = new Jogador("Enzo Fernández", "E.Fernández", "Médio Central", LocalDate.of(1995,8,8), "Argentina", 182, 8);
        argentina.addJogador(p4_7); jogadores.add(p4_7);
        Jogador p4_8 = new Jogador("Alexis Mac Allister", "Mac Allister", "Médio Ofensivo", LocalDate.of(1995,9,9), "Argentina", 183, 9);
        argentina.addJogador(p4_8); jogadores.add(p4_8);
        Jogador p4_9 = new Jogador("Angel Di María", "Di María", "Extremo Esquerdo", LocalDate.of(1995,10,10), "Argentina", 184, 10);
        argentina.addJogador(p4_9); jogadores.add(p4_9);
        Jogador p4_10 = new Jogador("Lionel Messi", "Messi", "Extremo Direito", LocalDate.of(1995,11,11), "Argentina", 185, 11);
        argentina.addJogador(p4_10); jogadores.add(p4_10);
        Jogador p4_11 = new Jogador("Julián Álvarez", "J.Álvarez", "Avançado", LocalDate.of(1995,12,12), "Argentina", 186, 12);
        argentina.addJogador(p4_11); jogadores.add(p4_11);
        Jogador p4_12 = new Jogador("Geronimo Rulli", "Rulli", "Defesa Central", LocalDate.of(1995,1,13), "Argentina", 187, 13);
        argentina.addJogador(p4_12); jogadores.add(p4_12);
        Jogador p4_13 = new Jogador("Gonzalo Montiel", "Montiel", "Defesa Direito", LocalDate.of(1995,2,14), "Argentina", 188, 14);
        argentina.addJogador(p4_13); jogadores.add(p4_13);
        Jogador p4_14 = new Jogador("Nicolás Otamendi", "Otamendi", "Médio Defensivo", LocalDate.of(1995,3,15), "Argentina", 189, 15);
        argentina.addJogador(p4_14); jogadores.add(p4_14);
        Jogador p4_15 = new Jogador("Guido Rodríguez", "G.Rodríguez", "Médio Central", LocalDate.of(1995,4,16), "Argentina", 175, 16);
        argentina.addJogador(p4_15); jogadores.add(p4_15);
        Jogador p4_16 = new Jogador("Giovani Lo Celso", "Lo Celso", "Extremo Esquerdo", LocalDate.of(1995,5,17), "Argentina", 176, 17);
        argentina.addJogador(p4_16); jogadores.add(p4_16);
        Jogador p4_17 = new Jogador("Lautaro Martínez", "Lautaro", "Avançado", LocalDate.of(1995,6,18), "Argentina", 177, 18);
        argentina.addJogador(p4_17); jogadores.add(p4_17);
        Jogador p4_18 = new Jogador("Franco Armani", "Armani", "Guarda-Redes", LocalDate.of(1995,7,19), "Argentina", 178, 19);
        argentina.addJogador(p4_18); jogadores.add(p4_18);
        Jogador p4_19 = new Jogador("Marcos Acuña", "Acuña", "Defesa Esquerdo", LocalDate.of(1995,8,20), "Argentina", 179, 20);
        argentina.addJogador(p4_19); jogadores.add(p4_19);
        Jogador p4_20 = new Jogador("Exequiel Palacios", "Palacios", "Médio Ofensivo", LocalDate.of(1995,9,21), "Argentina", 180, 21);
        argentina.addJogador(p4_20); jogadores.add(p4_20);
        Jogador p4_21 = new Jogador("Paulo Dybala", "Dybala", "Extremo Direito", LocalDate.of(1995,10,22), "Argentina", 181, 22);
        argentina.addJogador(p4_21); jogadores.add(p4_21);
        Jogador p4_22 = new Jogador("Ángel Correa", "Correa", "Avançado", LocalDate.of(1995,11,23), "Argentina", 182, 23);
        argentina.addJogador(p4_22); jogadores.add(p4_22);
        Jogador p4_23 = new Jogador("Nicolás González", "N.González", "Defesa Central", LocalDate.of(1995,12,24), "Argentina", 183, 24);
        argentina.addJogador(p4_23); jogadores.add(p4_23);
        Jogador p4_24 = new Jogador("Thiago Almada", "Almada", "Médio Central", LocalDate.of(1995,1,25), "Argentina", 184, 25);
        argentina.addJogador(p4_24); jogadores.add(p4_24);
        Jogador p4_25 = new Jogador("Valentín Carboni", "Carboni", "Avançado", LocalDate.of(1995,2,26), "Argentina", 185, 26);
        argentina.addJogador(p4_25); jogadores.add(p4_25);
        equipas.add(argentina);

        Equipa alemanha = new Equipa("Alemanha", "Seleção Nacional de Alemanha", "Alemanha");
        alemanha.setTreinador(alemanhaCoach); alemanhaCoach.setEquipa(alemanha);
        Jogador p5_0 = new Jogador("Manuel Neuer", "Neuer", "Guarda-Redes", LocalDate.of(1995,1,1), "Alemanha", 175, 1);
        alemanha.addJogador(p5_0); jogadores.add(p5_0);
        Jogador p5_1 = new Jogador("Joshua Kimmich", "Kimmich", "Defesa Central", LocalDate.of(1995,2,2), "Alemanha", 176, 2);
        alemanha.addJogador(p5_1); jogadores.add(p5_1);
        Jogador p5_2 = new Jogador("Antonio Rüdiger", "Rüdiger", "Defesa Central", LocalDate.of(1995,3,3), "Alemanha", 177, 3);
        alemanha.addJogador(p5_2); jogadores.add(p5_2);
        Jogador p5_3 = new Jogador("Nico Schlotterbeck", "Schlotterbeck", "Defesa Esquerdo", LocalDate.of(1995,4,4), "Alemanha", 178, 4);
        alemanha.addJogador(p5_3); jogadores.add(p5_3);
        Jogador p5_4 = new Jogador("David Raum", "Raum", "Defesa Direito", LocalDate.of(1995,5,5), "Alemanha", 179, 5);
        alemanha.addJogador(p5_4); jogadores.add(p5_4);
        Jogador p5_5 = new Jogador("Ilkay Gündogan", "Gündogan", "Médio Defensivo", LocalDate.of(1995,6,6), "Alemanha", 180, 6);
        alemanha.addJogador(p5_5); jogadores.add(p5_5);
        Jogador p5_6 = new Jogador("Leon Goretzka", "Goretzka", "Médio Central", LocalDate.of(1995,7,7), "Alemanha", 181, 7);
        alemanha.addJogador(p5_6); jogadores.add(p5_6);
        Jogador p5_7 = new Jogador("Kai Havertz", "Havertz", "Médio Central", LocalDate.of(1995,8,8), "Alemanha", 182, 8);
        alemanha.addJogador(p5_7); jogadores.add(p5_7);
        Jogador p5_8 = new Jogador("Thomas Müller", "Müller", "Médio Ofensivo", LocalDate.of(1995,9,9), "Alemanha", 183, 9);
        alemanha.addJogador(p5_8); jogadores.add(p5_8);
        Jogador p5_9 = new Jogador("Leroy Sané", "Sané", "Extremo Esquerdo", LocalDate.of(1995,10,10), "Alemanha", 184, 10);
        alemanha.addJogador(p5_9); jogadores.add(p5_9);
        Jogador p5_10 = new Jogador("Serge Gnabry", "Gnabry", "Extremo Direito", LocalDate.of(1995,11,11), "Alemanha", 185, 11);
        alemanha.addJogador(p5_10); jogadores.add(p5_10);
        Jogador p5_11 = new Jogador("Niclas Füllkrug", "Füllkrug", "Avançado", LocalDate.of(1995,12,12), "Alemanha", 186, 12);
        alemanha.addJogador(p5_11); jogadores.add(p5_11);
        Jogador p5_12 = new Jogador("Marc-André ter Stegen", "Ter Stegen", "Defesa Central", LocalDate.of(1995,1,13), "Alemanha", 187, 13);
        alemanha.addJogador(p5_12); jogadores.add(p5_12);
        Jogador p5_13 = new Jogador("Benjamin Henrichs", "Henrichs", "Defesa Direito", LocalDate.of(1995,2,14), "Alemanha", 188, 14);
        alemanha.addJogador(p5_13); jogadores.add(p5_13);
        Jogador p5_14 = new Jogador("Matthias Ginter", "Ginter", "Médio Defensivo", LocalDate.of(1995,3,15), "Alemanha", 189, 15);
        alemanha.addJogador(p5_14); jogadores.add(p5_14);
        Jogador p5_15 = new Jogador("Toni Kroos", "Kroos", "Médio Central", LocalDate.of(1995,4,16), "Alemanha", 175, 16);
        alemanha.addJogador(p5_15); jogadores.add(p5_15);
        Jogador p5_16 = new Jogador("Florian Wirtz", "Wirtz", "Extremo Esquerdo", LocalDate.of(1995,5,17), "Alemanha", 176, 17);
        alemanha.addJogador(p5_16); jogadores.add(p5_16);
        Jogador p5_17 = new Jogador("Jamal Musiala", "Musiala", "Avançado", LocalDate.of(1995,6,18), "Alemanha", 177, 18);
        alemanha.addJogador(p5_17); jogadores.add(p5_17);
        Jogador p5_18 = new Jogador("Bernd Leno", "Leno", "Guarda-Redes", LocalDate.of(1995,7,19), "Alemanha", 178, 19);
        alemanha.addJogador(p5_18); jogadores.add(p5_18);
        Jogador p5_19 = new Jogador("Robin Gosens", "Gosens", "Defesa Esquerdo", LocalDate.of(1995,8,20), "Alemanha", 179, 20);
        alemanha.addJogador(p5_19); jogadores.add(p5_19);
        Jogador p5_20 = new Jogador("Jonas Hofmann", "Hofmann", "Médio Ofensivo", LocalDate.of(1995,9,21), "Alemanha", 180, 21);
        alemanha.addJogador(p5_20); jogadores.add(p5_20);
        Jogador p5_21 = new Jogador("Maximilian Beier", "Beier", "Extremo Direito", LocalDate.of(1995,10,22), "Alemanha", 181, 22);
        alemanha.addJogador(p5_21); jogadores.add(p5_21);
        Jogador p5_22 = new Jogador("Tim Kleindienst", "Kleindienst", "Avançado", LocalDate.of(1995,11,23), "Alemanha", 182, 23);
        alemanha.addJogador(p5_22); jogadores.add(p5_22);
        Jogador p5_23 = new Jogador("Thilo Kehrer", "Kehrer", "Defesa Central", LocalDate.of(1995,12,24), "Alemanha", 183, 24);
        alemanha.addJogador(p5_23); jogadores.add(p5_23);
        Jogador p5_24 = new Jogador("Pascal Groß", "Groß", "Médio Central", LocalDate.of(1995,1,25), "Alemanha", 184, 25);
        alemanha.addJogador(p5_24); jogadores.add(p5_24);
        Jogador p5_25 = new Jogador("Chris Führich", "Führich", "Avançado", LocalDate.of(1995,2,26), "Alemanha", 185, 26);
        alemanha.addJogador(p5_25); jogadores.add(p5_25);
        equipas.add(alemanha);

        Equipa inglaterra = new Equipa("Inglaterra", "Seleção Nacional de Inglaterra", "Inglaterra");
        inglaterra.setTreinador(inglaterraCoach); inglaterraCoach.setEquipa(inglaterra);
        Jogador p6_0 = new Jogador("Jordan Pickford", "Pickford", "Guarda-Redes", LocalDate.of(1995,1,1), "Inglaterra", 175, 1);
        inglaterra.addJogador(p6_0); jogadores.add(p6_0);
        Jogador p6_1 = new Jogador("Trent Alexander-Arnold", "T.Alexander-Arnold", "Defesa Central", LocalDate.of(1995,2,2), "Inglaterra", 176, 2);
        inglaterra.addJogador(p6_1); jogadores.add(p6_1);
        Jogador p6_2 = new Jogador("John Stones", "Stones", "Defesa Central", LocalDate.of(1995,3,3), "Inglaterra", 177, 3);
        inglaterra.addJogador(p6_2); jogadores.add(p6_2);
        Jogador p6_3 = new Jogador("Harry Maguire", "Maguire", "Defesa Esquerdo", LocalDate.of(1995,4,4), "Inglaterra", 178, 4);
        inglaterra.addJogador(p6_3); jogadores.add(p6_3);
        Jogador p6_4 = new Jogador("Luke Shaw", "Shaw", "Defesa Direito", LocalDate.of(1995,5,5), "Inglaterra", 179, 5);
        inglaterra.addJogador(p6_4); jogadores.add(p6_4);
        Jogador p6_5 = new Jogador("Declan Rice", "Rice", "Médio Defensivo", LocalDate.of(1995,6,6), "Inglaterra", 180, 6);
        inglaterra.addJogador(p6_5); jogadores.add(p6_5);
        Jogador p6_6 = new Jogador("Jude Bellingham", "Bellingham", "Médio Central", LocalDate.of(1995,7,7), "Inglaterra", 181, 7);
        inglaterra.addJogador(p6_6); jogadores.add(p6_6);
        Jogador p6_7 = new Jogador("Phil Foden", "Foden", "Médio Central", LocalDate.of(1995,8,8), "Inglaterra", 182, 8);
        inglaterra.addJogador(p6_7); jogadores.add(p6_7);
        Jogador p6_8 = new Jogador("Marcus Rashford", "Rashford", "Médio Ofensivo", LocalDate.of(1995,9,9), "Inglaterra", 183, 9);
        inglaterra.addJogador(p6_8); jogadores.add(p6_8);
        Jogador p6_9 = new Jogador("Bukayo Saka", "Saka", "Extremo Esquerdo", LocalDate.of(1995,10,10), "Inglaterra", 184, 10);
        inglaterra.addJogador(p6_9); jogadores.add(p6_9);
        Jogador p6_10 = new Jogador("Harry Kane", "Kane", "Extremo Direito", LocalDate.of(1995,11,11), "Inglaterra", 185, 11);
        inglaterra.addJogador(p6_10); jogadores.add(p6_10);
        Jogador p6_11 = new Jogador("Ollie Watkins", "Watkins", "Avançado", LocalDate.of(1995,12,12), "Inglaterra", 186, 12);
        inglaterra.addJogador(p6_11); jogadores.add(p6_11);
        Jogador p6_12 = new Jogador("Aaron Ramsdale", "Ramsdale", "Defesa Central", LocalDate.of(1995,1,13), "Inglaterra", 187, 13);
        inglaterra.addJogador(p6_12); jogadores.add(p6_12);
        Jogador p6_13 = new Jogador("Kyle Walker", "Walker", "Defesa Direito", LocalDate.of(1995,2,14), "Inglaterra", 188, 14);
        inglaterra.addJogador(p6_13); jogadores.add(p6_13);
        Jogador p6_14 = new Jogador("Eric Dier", "Dier", "Médio Defensivo", LocalDate.of(1995,3,15), "Inglaterra", 189, 15);
        inglaterra.addJogador(p6_14); jogadores.add(p6_14);
        Jogador p6_15 = new Jogador("Kalvin Phillips", "K.Phillips", "Médio Central", LocalDate.of(1995,4,16), "Inglaterra", 175, 16);
        inglaterra.addJogador(p6_15); jogadores.add(p6_15);
        Jogador p6_16 = new Jogador("Jack Grealish", "Grealish", "Extremo Esquerdo", LocalDate.of(1995,5,17), "Inglaterra", 176, 17);
        inglaterra.addJogador(p6_16); jogadores.add(p6_16);
        Jogador p6_17 = new Jogador("Raheem Sterling", "Sterling", "Avançado", LocalDate.of(1995,6,18), "Inglaterra", 177, 18);
        inglaterra.addJogador(p6_17); jogadores.add(p6_17);
        Jogador p6_18 = new Jogador("Nick Pope", "Pope", "Guarda-Redes", LocalDate.of(1995,7,19), "Inglaterra", 178, 19);
        inglaterra.addJogador(p6_18); jogadores.add(p6_18);
        Jogador p6_19 = new Jogador("Ben Chilwell", "Chilwell", "Defesa Esquerdo", LocalDate.of(1995,8,20), "Inglaterra", 179, 20);
        inglaterra.addJogador(p6_19); jogadores.add(p6_19);
        Jogador p6_20 = new Jogador("Conor Gallagher", "Gallagher", "Médio Ofensivo", LocalDate.of(1995,9,21), "Inglaterra", 180, 21);
        inglaterra.addJogador(p6_20); jogadores.add(p6_20);
        Jogador p6_21 = new Jogador("Cole Palmer", "Palmer", "Extremo Direito", LocalDate.of(1995,10,22), "Inglaterra", 181, 22);
        inglaterra.addJogador(p6_21); jogadores.add(p6_21);
        Jogador p6_22 = new Jogador("Ivan Toney", "Toney", "Avançado", LocalDate.of(1995,11,23), "Inglaterra", 182, 23);
        inglaterra.addJogador(p6_22); jogadores.add(p6_22);
        Jogador p6_23 = new Jogador("Marc Guéhi", "Guéhi", "Defesa Central", LocalDate.of(1995,12,24), "Inglaterra", 183, 24);
        inglaterra.addJogador(p6_23); jogadores.add(p6_23);
        Jogador p6_24 = new Jogador("Kobbie Mainoo", "Mainoo", "Médio Central", LocalDate.of(1995,1,25), "Inglaterra", 184, 25);
        inglaterra.addJogador(p6_24); jogadores.add(p6_24);
        Jogador p6_25 = new Jogador("Anthony Gordon", "Gordon", "Avançado", LocalDate.of(1995,2,26), "Inglaterra", 185, 26);
        inglaterra.addJogador(p6_25); jogadores.add(p6_25);
        equipas.add(inglaterra);

        Equipa marrocos = new Equipa("Marrocos", "Seleção Nacional de Marrocos", "Marrocos");
        marrocos.setTreinador(marrocosCoach); marrocosCoach.setEquipa(marrocos);
        Jogador p7_0 = new Jogador("Yassine Bounou", "Bono", "Guarda-Redes", LocalDate.of(1995,1,1), "Marrocos", 175, 1);
        marrocos.addJogador(p7_0); jogadores.add(p7_0);
        Jogador p7_1 = new Jogador("Achraf Hakimi", "Hakimi", "Defesa Central", LocalDate.of(1995,2,2), "Marrocos", 176, 2);
        marrocos.addJogador(p7_1); jogadores.add(p7_1);
        Jogador p7_2 = new Jogador("Romain Saïss", "Saïss", "Defesa Central", LocalDate.of(1995,3,3), "Marrocos", 177, 3);
        marrocos.addJogador(p7_2); jogadores.add(p7_2);
        Jogador p7_3 = new Jogador("Nayef Aguerd", "Aguerd", "Defesa Esquerdo", LocalDate.of(1995,4,4), "Marrocos", 178, 4);
        marrocos.addJogador(p7_3); jogadores.add(p7_3);
        Jogador p7_4 = new Jogador("Noussair Mazraoui", "Mazraoui", "Defesa Direito", LocalDate.of(1995,5,5), "Marrocos", 179, 5);
        marrocos.addJogador(p7_4); jogadores.add(p7_4);
        Jogador p7_5 = new Jogador("Sofyan Amrabat", "Amrabat", "Médio Defensivo", LocalDate.of(1995,6,6), "Marrocos", 180, 6);
        marrocos.addJogador(p7_5); jogadores.add(p7_5);
        Jogador p7_6 = new Jogador("Azzedine Ounahi", "Ounahi", "Médio Central", LocalDate.of(1995,7,7), "Marrocos", 181, 7);
        marrocos.addJogador(p7_6); jogadores.add(p7_6);
        Jogador p7_7 = new Jogador("Selim Amallah", "Amallah", "Médio Central", LocalDate.of(1995,8,8), "Marrocos", 182, 8);
        marrocos.addJogador(p7_7); jogadores.add(p7_7);
        Jogador p7_8 = new Jogador("Hakim Ziyech", "Ziyech", "Médio Ofensivo", LocalDate.of(1995,9,9), "Marrocos", 183, 9);
        marrocos.addJogador(p7_8); jogadores.add(p7_8);
        Jogador p7_9 = new Jogador("Sofiane Boufal", "Boufal", "Extremo Esquerdo", LocalDate.of(1995,10,10), "Marrocos", 184, 10);
        marrocos.addJogador(p7_9); jogadores.add(p7_9);
        Jogador p7_10 = new Jogador("Youssef En-Nesyri", "En-Nesyri", "Extremo Direito", LocalDate.of(1995,11,11), "Marrocos", 185, 11);
        marrocos.addJogador(p7_10); jogadores.add(p7_10);
        Jogador p7_11 = new Jogador("Abdessamad Ezzalzouli", "Ez-Abde", "Avançado", LocalDate.of(1995,12,12), "Marrocos", 186, 12);
        marrocos.addJogador(p7_11); jogadores.add(p7_11);
        Jogador p7_12 = new Jogador("Munir Mohamedi", "Munir", "Defesa Central", LocalDate.of(1995,1,13), "Marrocos", 187, 13);
        marrocos.addJogador(p7_12); jogadores.add(p7_12);
        Jogador p7_13 = new Jogador("Mohamed Chibi", "Chibi", "Defesa Direito", LocalDate.of(1995,2,14), "Marrocos", 188, 14);
        marrocos.addJogador(p7_13); jogadores.add(p7_13);
        Jogador p7_14 = new Jogador("Jawad El Yamiq", "El Yamiq", "Médio Defensivo", LocalDate.of(1995,3,15), "Marrocos", 189, 15);
        marrocos.addJogador(p7_14); jogadores.add(p7_14);
        Jogador p7_15 = new Jogador("Bilal El Khannous", "El Khannous", "Médio Central", LocalDate.of(1995,4,16), "Marrocos", 175, 16);
        marrocos.addJogador(p7_15); jogadores.add(p7_15);
        Jogador p7_16 = new Jogador("Ilias Chair", "Chair", "Extremo Esquerdo", LocalDate.of(1995,5,17), "Marrocos", 176, 17);
        marrocos.addJogador(p7_16); jogadores.add(p7_16);
        Jogador p7_17 = new Jogador("Zakaria Aboukhlal", "Aboukhlal", "Avançado", LocalDate.of(1995,6,18), "Marrocos", 177, 18);
        marrocos.addJogador(p7_17); jogadores.add(p7_17);
        Jogador p7_18 = new Jogador("Ahmed Tagnaouti", "Tagnaouti", "Guarda-Redes", LocalDate.of(1995,7,19), "Marrocos", 178, 19);
        marrocos.addJogador(p7_18); jogadores.add(p7_18);
        Jogador p7_19 = new Jogador("Adam Masina", "Masina", "Defesa Esquerdo", LocalDate.of(1995,8,20), "Marrocos", 179, 20);
        marrocos.addJogador(p7_19); jogadores.add(p7_19);
        Jogador p7_20 = new Jogador("Amine Harit", "Harit", "Médio Ofensivo", LocalDate.of(1995,9,21), "Marrocos", 180, 21);
        marrocos.addJogador(p7_20); jogadores.add(p7_20);
        Jogador p7_21 = new Jogador("Walid Cheddira", "Cheddira", "Extremo Direito", LocalDate.of(1995,10,22), "Marrocos", 181, 22);
        marrocos.addJogador(p7_21); jogadores.add(p7_21);
        Jogador p7_22 = new Jogador("Youssef Aït Bennasser", "Aït Bennasser", "Avançado", LocalDate.of(1995,11,23), "Marrocos", 182, 23);
        marrocos.addJogador(p7_22); jogadores.add(p7_22);
        Jogador p7_23 = new Jogador("Badr Benoun", "Benoun", "Defesa Central", LocalDate.of(1995,12,24), "Marrocos", 183, 24);
        marrocos.addJogador(p7_23); jogadores.add(p7_23);
        Jogador p7_24 = new Jogador("Yahia Attiat-Allah", "Attiat-Allah", "Médio Central", LocalDate.of(1995,1,25), "Marrocos", 184, 25);
        marrocos.addJogador(p7_24); jogadores.add(p7_24);
        Jogador p7_25 = new Jogador("Ibrahim Salah", "I.Salah", "Avançado", LocalDate.of(1995,2,26), "Marrocos", 185, 26);
        marrocos.addJogador(p7_25); jogadores.add(p7_25);
        equipas.add(marrocos);

        // ── Torneio de exemplo ────────────────────────────────────────────────
        Patrocinador budweiser = new Patrocinador("Budweiser","512345678","USA","bud@bud.com","Bebidas","987001","Cerveja oficial");
        Patrocinador adidas = new Patrocinador("Adidas","598765432","Alemanha","info@adidas.com","Desporto","912002","Equipamento");
        patrocinadores.add(budweiser); patrocinadores.add(adidas);

        Torneio t = new Torneio("Champions League 2026", LocalDate.of(2026, 6, 1), 8);
        t.addEquipa(portugal);
        t.addEquipa(franca);
        t.addEquipa(espanha);
        t.addEquipa(brasil);
        t.addEquipa(argentina);
        t.addEquipa(alemanha);
        t.addEquipa(inglaterra);
        t.addEquipa(marrocos);
        torneios.add(t);
        gerarCalendarioGrupos(t);
    }

    public List<Estadio> getEstadios() { return estadios; }
    public void addEstadio(Estadio e) { estadios.add(e); }
    public void removeEstadio(Estadio e) { estadios.remove(e); }
    public List<Equipa> getEquipas() { return equipas; }
    public void addEquipa(Equipa e) { equipas.add(e); }
    public void removeEquipa(Equipa e) { equipas.remove(e); }
    public List<Jogador> getJogadores() { return jogadores; }
    public void addJogador(Jogador j) { jogadores.add(j); }
    public void removeJogador(Jogador j) { jogadores.remove(j); }
    public List<Treinador> getTreinadores() { return treinadores; }
    public void addTreinador(Treinador t) { treinadores.add(t); }
    public void removeTreinador(Treinador t) { treinadores.remove(t); }
    public List<Patrocinador> getPatrocinadores() { return patrocinadores; }
    public void addPatrocinador(Patrocinador p) { patrocinadores.add(p); }
    public void removePatrocinador(Patrocinador p) { patrocinadores.remove(p); }
    public List<ContratoPatrocinio> getContratos() { return contratos; }
    public void addContrato(ContratoPatrocinio c) { contratos.add(c); }
    public List<Torneio> getTorneios() { return torneios; }
    public void addTorneio(Torneio t) { torneios.add(t); }
    public List<Jogo> getJogos() { return jogos; }
    public void addJogo(Jogo j) { jogos.add(j); }
    public void removeJogo(Jogo j) { jogos.remove(j); }
    public List<Bilhete> getBilhetes() { return bilhetes; }
    public void addBilhete(Bilhete b) { bilhetes.add(b); }
    public boolean jogoCalendarizado(Equipa e) {
        // Block if the active tournament has ANY game (finished or not) with this team
        if (torneioAtual == null || torneioAtual.getJogos().isEmpty()) return false;
        return torneioAtual.getJogos().stream().anyMatch(j ->
            j.getEquipaCasa() == e || j.getEquipaVisitante() == e);
    }
    public boolean jogoCalendarizadoEstadio(Estadio est) {
        // Only block if the active tournament has games with this stadium
        if (torneioAtual == null || torneioAtual.getJogos().isEmpty()) return false;
        return torneioAtual.getJogos().stream().anyMatch(j -> j.getEstadio() == est);
    }
    public double getTotalReceitaPatrocinios() { return contratos.stream().mapToDouble(ContratoPatrocinio::getValor).sum(); }
    public double getTotalReceitaBilheteira()  { return bilhetes.stream().mapToDouble(Bilhete::getPrecoFinal).sum(); }
    public int getJogosRealizados() { return (int)jogos.stream().filter(Jogo::isTerminado).count(); }
    public List<Jogador> getJogadoresTorneio() {
        if (torneioAtual==null) return jogadores;
        return torneioAtual.getEquipas().stream().flatMap(e->e.getJogadores().stream()).distinct().collect(Collectors.toList());
    }
    public void gerarCalendarioGruposComEstadios(Torneio torneio, List<Estadio> estadiosSel) {
        CalendarGenerator.gerar(torneio, estadiosSel, jogos);
    }


    /**
     * Verifica se todos os jogos de uma ronda estão terminados e gera a próxima ronda.
     * Ordem: Oitavos → Quartos → Meias-Finais → Final
     * Retorna o nome da nova ronda criada, ou null se nada foi gerado.
     */
    public String verificarEAvancarEliminacao(Torneio torneio) {
        if (torneio == null) return null;

        String[] ordemRondas = {"Oitavos", "Quartos", "Meias-Finais", "Final"};
        String[] proximaRonda = {"Quartos", "Meias-Finais", "Final", null};

        for (int r = 0; r < ordemRondas.length; r++) {
            final String ronda = ordemRondas[r];
            final String proxima = proximaRonda[r];
            if (proxima == null) continue; // Final — não há próxima

            List<Jogo> jogosRonda = torneio.getJogos().stream()
                .filter(j -> "eliminacao".equals(j.getFase()) && ronda.equals(j.getRonda()))
                .sorted(java.util.Comparator.comparingInt(Jogo::getPosicaoBracket))
                .collect(java.util.stream.Collectors.toList());

            if (jogosRonda.isEmpty()) continue;

            // Check if all finished
            boolean todosTerminados = jogosRonda.stream().allMatch(Jogo::isTerminado);
            if (!todosTerminados) continue;

            // Check if next round already exists
            boolean proximaJaExiste = torneio.getJogos().stream()
                .anyMatch(j -> "eliminacao".equals(j.getFase()) && proxima.equals(j.getRonda()));
            if (proximaJaExiste) continue;

            // Generate next round with winners
            List<Equipa> vencedores = new ArrayList<>();
            for (Jogo j : jogosRonda) {
                Equipa v = j.getVencedor();
                if (v != null) vencedores.add(v);
            }
            if (vencedores.size() < 2) continue;

            java.time.LocalDateTime dt = java.time.LocalDateTime.now().plusDays(7).withHour(20).withMinute(0);
            int pos = 0, estIdx = 0;
            for (int i = 0; i + 1 < vencedores.size(); i += 2) {
                Estadio est = estadios.isEmpty() ? null : estadios.get(estIdx++ % estadios.size());
                Jogo novoJogo = new Jogo(vencedores.get(i), vencedores.get(i + 1), est, dt.plusDays(pos * 4L));
                novoJogo.setFase("eliminacao");
                novoJogo.setRonda(proxima);
                novoJogo.setPosicaoBracket(pos++);
                torneio.addJogo(novoJogo);
                jogos.add(novoJogo);
            }
            return proxima;
        }
        return null;
    }
    public List<Jogo> getJogosTorneio() {
        if (torneioAtual==null) return jogos;
        return torneioAtual.getJogos();
    }
}
