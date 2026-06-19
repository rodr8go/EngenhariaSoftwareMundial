package torneoapp.service;

import com.google.gson.*;
import torneoapp.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Guarda e carrega todos os dados da aplicação em ficheiros JSON
 * na pasta do utilizador: ~/.torneoapp/data/
 *
 * Estrutura de ficheiros:
 *   estadios.json
 *   equipas.json       (inclui jogadores e treinadores dentro)
 *   torneios.json      (inclui jogos, eventos, bilhetes, voluntários)
 *   patrocinadores.json
 *   contratos.json
 *   config.json        (torneio atual)
 */
public class PersistenceManager {

    private static final Path DATA_DIR = Path.of(
        System.getProperty("user.home"), ".torneoapp", "data"
    );

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        // LocalDate adapter
        .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>)
            (src, type, ctx) -> new JsonPrimitive(src.toString()))
        .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
            (json, type, ctx) -> LocalDate.parse(json.getAsString()))
        // LocalDateTime adapter
        .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
            (src, type, ctx) -> new JsonPrimitive(src.toString()))
        .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
            (json, type, ctx) -> LocalDateTime.parse(json.getAsString()))
        .create();

    // ── Save ─────────────────────────────────────────────────────────────────
    public static void save(DataStore ds) {
        try {
            Files.createDirectories(DATA_DIR);
            SaveData data = buildSaveData(ds);
            write("data.json", GSON.toJson(data));
        } catch (Exception e) {
            System.err.println("Erro ao guardar dados: " + e.getMessage());
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────────
    public static boolean load(DataStore ds) {
        Path file = DATA_DIR.resolve("data.json");
        if (!Files.exists(file)) return false;
        try {
            String json = Files.readString(file);
            SaveData data = GSON.fromJson(json, SaveData.class);
            if (data == null) return false;
            restoreData(ds, data);
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
            return false;
        }
    }

    public static boolean hasSavedData() {
        return Files.exists(DATA_DIR.resolve("data.json"));
    }

    // ── Internal data model for serialization ────────────────────────────────
    private static class SaveData {
        List<EstadioData>     estadios     = new ArrayList<>();
        List<EquipaData>      equipas      = new ArrayList<>();
        List<TorneioData>     torneios     = new ArrayList<>();
        List<PatrocinadorData>patrocinadores = new ArrayList<>();
        List<ContratoData>    contratos    = new ArrayList<>();
        String                torneioAtualNome;
    }

    // Flat data classes (avoid circular references)
    private static class EstadioData {
        int id; String nome, morada, cidade; String fotoPath;
        List<SetorData> setores = new ArrayList<>();
    }
    private static class SetorData {
        int id; String nome; int lotacao; double preco;
    }
    private static class JogadorData {
        int id; String nomeCompleto, alcunha, posicao, nacionalidade, fotoPath;
        LocalDate dataNascimento;
        int altura, numeroCamisola, golos, assistencias, cartoesAmarelos, cartoesVermelhos;
    }
    private static class TreinadorData {
        int id; String nomeCompleto, email, telemovel, nacionalidade;
        LocalDate dataNascimento;
    }
    private static class EquipaData {
        int id; String nome, descricao, localizacao, fotoPath;
        TreinadorData treinador;
        List<JogadorData> jogadores = new ArrayList<>();
    }
    private static class JogoData {
        int id;
        int equipaCasaId, equipaVisitanteId, estadioId;
        LocalDateTime dataHora;
        int golosCasa, golosVisitante;
        boolean terminado;
        double posseCasa, posseVisitante;
        int cantosCasa, cantosVisitante;
        int amarCasa, amarVisit, vermCasa, vermVisit;
        String fase, ronda;
        int posicaoBracket;
        boolean precarioDefined;
        List<VoluntarioData> voluntarios = new ArrayList<>();
        List<EventoData>     eventos     = new ArrayList<>();
        List<BilheteData>    bilhetes    = new ArrayList<>();
    }
    private static class VoluntarioData {
        int id; String categoria; int quantidade;
    }
    private static class EventoData {
        int id, minuto; String tipo, descricao; int jogadorId; boolean isColetivo; int equipaId;
    }
    private static class BilheteData {
        int id; int setorId; String nif; double precoFinal; int numLugar;
    }
    private static class TorneioData {
        int id; String nome, faseAtual; LocalDate dataInicio; int numEquipas;
        List<Integer> equipaIds = new ArrayList<>();
        List<JogoData> jogos    = new ArrayList<>();
    }
    private static class PatrocinadorData {
        int id; String nome, nif, morada, email, setorAtividade, telemovel, descricao, fotoPath;
    }
    private static class ContratoData {
        int id; String numero; int patrocinadorId; double valor;
        String tipoPatrocinio, direitos; int jogoId; LocalDate dataContrato;
    }

    // ── Build save data ───────────────────────────────────────────────────────
    private static SaveData buildSaveData(DataStore ds) {
        SaveData sd = new SaveData();

        // Estadios
        for (Estadio e : ds.getEstadios()) {
            EstadioData ed = new EstadioData();
            ed.id = e.getId(); ed.nome = e.getNome(); ed.morada = e.getMorada();
            ed.cidade = e.getCidade(); ed.fotoPath = e.getFotoPath();
            for (Setor s : e.getSetores()) {
                SetorData sd2 = new SetorData();
                sd2.id = s.getId(); sd2.nome = s.getNome();
                sd2.lotacao = s.getLotacao(); sd2.preco = s.getPreco();
                ed.setores.add(sd2);
            }
            sd.estadios.add(ed);
        }

        // Equipas (includes jogadores and treinador)
        for (Equipa eq : ds.getEquipas()) {
            EquipaData eqd = new EquipaData();
            eqd.id = eq.getId(); eqd.nome = eq.getNome();
            eqd.descricao = eq.getDescricao(); eqd.localizacao = eq.getLocalizacao();
            eqd.fotoPath = eq.getFotoPath();
            if (eq.getTreinador() != null) {
                Treinador t = eq.getTreinador();
                TreinadorData td = new TreinadorData();
                td.id = t.getId(); td.nomeCompleto = t.getNomeCompleto();
                td.email = t.getEmail(); td.telemovel = t.getTelemovel();
                td.nacionalidade = t.getNacionalidade(); td.dataNascimento = t.getDataNascimento();
                eqd.treinador = td;
            }
            for (Jogador j : eq.getJogadores()) {
                JogadorData jd = new JogadorData();
                jd.id = j.getId(); jd.nomeCompleto = j.getNomeCompleto();
                jd.alcunha = j.getAlcunha(); jd.posicao = j.getPosicao();
                jd.nacionalidade = j.getNacionalidade(); jd.dataNascimento = j.getDataNascimento();
                jd.altura = j.getAltura(); jd.numeroCamisola = j.getNumeroCamisola();
                jd.golos = j.getGolos(); jd.assistencias = j.getAssistencias();
                jd.cartoesAmarelos = j.getCartoesAmarelos(); jd.cartoesVermelhos = j.getCartoesVermelhos();
                jd.fotoPath = j.getFotoPath();
                eqd.jogadores.add(jd);
            }
            sd.equipas.add(eqd);
        }

        // Torneios
        for (Torneio t : ds.getTorneios()) {
            TorneioData td = new TorneioData();
            td.id = t.getId(); td.nome = t.getNome();
            td.dataInicio = t.getDataInicio(); td.numEquipas = t.getNumEquipas();
            td.faseAtual = t.getFaseAtual();
            for (Equipa eq : t.getEquipas()) td.equipaIds.add(eq.getId());

            for (Jogo j : t.getJogos()) {
                JogoData jd = new JogoData();
                jd.id = j.getId();
                jd.equipaCasaId      = j.getEquipaCasa()       != null ? j.getEquipaCasa().getId()       : -1;
                jd.equipaVisitanteId = j.getEquipaVisitante()  != null ? j.getEquipaVisitante().getId()  : -1;
                jd.estadioId         = j.getEstadio()          != null ? j.getEstadio().getId()          : -1;
                jd.dataHora = j.getDataHora();
                jd.golosCasa = j.getGolosCasa(); jd.golosVisitante = j.getGolosVisitante();
                jd.terminado = j.isTerminado();
                jd.posseCasa = j.getPosseCasa(); jd.posseVisitante = j.getPosseVisitante();
                jd.cantosCasa = j.getCantosCasa(); jd.cantosVisitante = j.getCantosVisitante();
                jd.amarCasa = j.getCartoesAmarelosCasa(); jd.amarVisit = j.getCartoesAmarelosVisitante();
                jd.vermCasa = j.getCartoesVermelhosCasa(); jd.vermVisit = j.getCartoesVermelhosVisitante();
                jd.fase = j.getFase(); jd.ronda = j.getRonda();
                jd.posicaoBracket = j.getPosicaoBracket(); jd.precarioDefined = j.isPrecarioDefined();

                for (Voluntario v : j.getVoluntarios()) {
                    VoluntarioData vd = new VoluntarioData();
                    vd.id = v.getId(); vd.categoria = v.getCategoria(); vd.quantidade = v.getQuantidade();
                    jd.voluntarios.add(vd);
                }
                for (EventoJogo ev : j.getEventos()) {
                    EventoData evd = new EventoData();
                    evd.id = ev.getId(); evd.minuto = ev.getMinuto(); evd.descricao = ev.getDescricao();
                    if (ev instanceof EventoPessoal ep) {
                        evd.tipo = ep.getTipoEvento(); evd.isColetivo = false;
                        evd.jogadorId = ep.getJogador() != null ? ep.getJogador().getId() : -1;
                    } else if (ev instanceof EventoColetivo ec) {
                        evd.tipo = ec.getTipoEvento(); evd.isColetivo = true;
                        evd.equipaId = ec.getEquipa() != null ? ec.getEquipa().getId() : -1;
                    }
                    jd.eventos.add(evd);
                }
                for (Bilhete b : j.getBilhetes()) {
                    BilheteData bd = new BilheteData();
                    bd.id = b.getId(); bd.setorId = b.getSetor() != null ? b.getSetor().getId() : -1;
                    bd.nif = b.getNif(); bd.precoFinal = b.getPrecoFinal(); bd.numLugar = b.getNumLugar();
                    jd.bilhetes.add(bd);
                }
                td.jogos.add(jd);
            }
            sd.torneios.add(td);
        }

        // Patrocinadores
        for (Patrocinador p : ds.getPatrocinadores()) {
            PatrocinadorData pd = new PatrocinadorData();
            pd.id = p.getId(); pd.nome = p.getNome(); pd.nif = p.getNif();
            pd.morada = p.getMorada(); pd.email = p.getEmail();
            pd.setorAtividade = p.getSetorAtividade(); pd.telemovel = p.getTelemovel();
            pd.descricao = p.getDescricao(); pd.fotoPath = p.getFotoPath();
            sd.patrocinadores.add(pd);
        }

        // Contratos
        for (ContratoPatrocinio c : ds.getContratos()) {
            ContratoData cd = new ContratoData();
            cd.id = c.getId(); cd.numero = c.getNumero();
            cd.patrocinadorId = c.getPatrocinador() != null ? c.getPatrocinador().getId() : -1;
            cd.valor = c.getValor(); cd.tipoPatrocinio = c.getTipoPatrocinio();
            cd.direitos = c.getDireitos(); cd.dataContrato = c.getDataContrato();
            // Find jogo id across all torneios
            cd.jogoId = -1;
            if (c.getJogo() != null) cd.jogoId = c.getJogo().getId();
            sd.contratos.add(cd);
        }

        sd.torneioAtualNome = ds.getTorneioAtual() != null ? ds.getTorneioAtual().getNome() : null;
        return sd;
    }

    // ── Restore data ──────────────────────────────────────────────────────────
    private static void restoreData(DataStore ds, SaveData sd) {
        // Clear all existing data
        ds.getEstadios().clear();
        ds.getEquipas().clear();
        ds.getJogadores().clear();
        ds.getTreinadores().clear();
        ds.getPatrocinadores().clear();
        ds.getContratos().clear();
        ds.getTorneios().clear();
        ds.getJogos().clear();
        ds.getBilhetes().clear();

        // Build lookup maps
        Map<Integer, Estadio>  estadioMap  = new HashMap<>();
        Map<Integer, Setor>    setorMap    = new HashMap<>();
        Map<Integer, Equipa>   equipaMap   = new HashMap<>();
        Map<Integer, Jogador>  jogadorMap  = new HashMap<>();
        Map<Integer, Jogo>     jogoMap     = new HashMap<>();
        Map<Integer, Patrocinador> patMap  = new HashMap<>();

        // Restore estadios
        for (EstadioData ed : sd.estadios) {
            Estadio e = new Estadio(ed.nome, ed.morada != null ? ed.morada : "", ed.cidade != null ? ed.cidade : "");
            setId(e, "id", ed.id);
            e.setFotoPath(ed.fotoPath);
            for (SetorData sd2 : ed.setores) {
                Setor s = new Setor(sd2.nome, sd2.lotacao);
                setId(s, "id", sd2.id);
                s.setPreco(sd2.preco);
                e.addSetor(s);
                setorMap.put(sd2.id, s);
            }
            ds.getEstadios().add(e);
            estadioMap.put(ed.id, e);
        }

        // Restore equipas, treinadores, jogadores
        for (EquipaData eqd : sd.equipas) {
            Equipa eq = new Equipa(eqd.nome, eqd.descricao != null ? eqd.descricao : "", eqd.localizacao != null ? eqd.localizacao : "");
            setId(eq, "id", eqd.id);
            eq.setFotoPath(eqd.fotoPath);

            if (eqd.treinador != null) {
                TreinadorData td = eqd.treinador;
                Treinador t = new Treinador(td.nomeCompleto, td.dataNascimento,
                    td.nacionalidade != null ? td.nacionalidade : "",
                    td.email != null ? td.email : "", td.telemovel != null ? td.telemovel : "");
                setId(t, "id", td.id);
                eq.setTreinador(t); t.setEquipa(eq);
                ds.getTreinadores().add(t);
            }
            for (JogadorData jd : eqd.jogadores) {
                Jogador j = new Jogador(jd.nomeCompleto, jd.alcunha != null ? jd.alcunha : "",
                    jd.posicao != null ? jd.posicao : "", jd.dataNascimento,
                    jd.nacionalidade != null ? jd.nacionalidade : "", jd.altura, jd.numeroCamisola);
                setId(j, "id", jd.id);
                j.setGolos(jd.golos); j.setAssistencias(jd.assistencias);
                j.setCartoesAmarelos(jd.cartoesAmarelos); j.setCartoesVermelhos(jd.cartoesVermelhos);
                j.setFotoPath(jd.fotoPath);
                eq.addJogador(j);
                ds.getJogadores().add(j);
                jogadorMap.put(jd.id, j);
            }
            ds.getEquipas().add(eq);
            equipaMap.put(eqd.id, eq);
        }

        // Restore torneios and jogos
        for (TorneioData td : sd.torneios) {
            Torneio t = new Torneio(td.nome, td.dataInicio, td.numEquipas);
            setId(t, "id", td.id);
            t.setFaseAtual(td.faseAtual != null ? td.faseAtual : "grupos");
            for (int eqId : td.equipaIds) {
                Equipa eq = equipaMap.get(eqId);
                if (eq != null) t.addEquipa(eq);
            }
            for (JogoData jd : td.jogos) {
                Jogo j = new Jogo(
                    equipaMap.get(jd.equipaCasaId),
                    equipaMap.get(jd.equipaVisitanteId),
                    estadioMap.get(jd.estadioId),
                    jd.dataHora
                );
                setId(j, "id", jd.id);
                j.setGolosCasa(jd.golosCasa); j.setGolosVisitante(jd.golosVisitante);
                j.setTerminado(jd.terminado);
                j.setPosseCasa(jd.posseCasa); j.setPosseVisitante(jd.posseVisitante);
                j.setCantosCasa(jd.cantosCasa); j.setCantosVisitante(jd.cantosVisitante);
                j.setCartoesAmarelosCasa(jd.amarCasa); j.setCartoesAmarelosVisitante(jd.amarVisit);
                j.setCartoesVermelhosCasa(jd.vermCasa); j.setCartoesVermelhosVisitante(jd.vermVisit);
                j.setFase(jd.fase != null ? jd.fase : "grupos");
                j.setRonda(jd.ronda != null ? jd.ronda : "");
                j.setPosicaoBracket(jd.posicaoBracket);
                j.setPrecarioDefined(jd.precarioDefined);

                for (VoluntarioData vd : jd.voluntarios) {
                    Voluntario v = new Voluntario(vd.categoria, vd.quantidade);
                    setId(v, "id", vd.id);
                    j.addVoluntario(v);
                }
                for (EventoData evd : jd.eventos) {
                    if (!evd.isColetivo) {
                        Jogador jog = jogadorMap.get(evd.jogadorId);
                        EventoPessoal ep = new EventoPessoal(j, evd.minuto,
                            evd.descricao != null ? evd.descricao : "",
                            jog, evd.tipo != null ? evd.tipo : "");
                        setId(ep, "id", evd.id);
                        j.addEvento(ep);
                    } else {
                        Equipa eq = equipaMap.get(evd.equipaId);
                        EventoColetivo ec = new EventoColetivo(j, evd.minuto,
                            evd.descricao != null ? evd.descricao : "",
                            eq, evd.tipo != null ? evd.tipo : "");
                        setId(ec, "id", evd.id);
                        j.addEvento(ec);
                    }
                }
                for (BilheteData bd : jd.bilhetes) {
                    Setor s = setorMap.get(bd.setorId);
                    Bilhete b = new Bilhete(j, s, bd.nif, bd.precoFinal, bd.numLugar);
                    setId(b, "id", bd.id);
                    j.addBilhete(b);
                    ds.getBilhetes().add(b);
                }
                t.addJogo(j);
                ds.getJogos().add(j);
                jogoMap.put(jd.id, j);
            }
            ds.getTorneios().add(t);
        }

        // Restore patrocinadores
        for (PatrocinadorData pd : sd.patrocinadores) {
            Patrocinador p = new Patrocinador(pd.nome, pd.nif != null ? pd.nif : "",
                pd.morada != null ? pd.morada : "", pd.email != null ? pd.email : "",
                pd.setorAtividade != null ? pd.setorAtividade : "",
                pd.telemovel != null ? pd.telemovel : "", pd.descricao != null ? pd.descricao : "");
            setId(p, "id", pd.id);
            p.setFotoPath(pd.fotoPath);
            ds.getPatrocinadores().add(p);
            patMap.put(pd.id, p);
        }

        // Restore contratos
        for (ContratoData cd : sd.contratos) {
            ContratoPatrocinio c = new ContratoPatrocinio(
                cd.numero, patMap.get(cd.patrocinadorId), cd.valor,
                cd.tipoPatrocinio != null ? cd.tipoPatrocinio : "",
                cd.direitos != null ? cd.direitos : "",
                jogoMap.get(cd.jogoId), cd.dataContrato
            );
            setId(c, "id", cd.id);
            ds.getContratos().add(c);
        }

        // Restore torneio atual
        if (sd.torneioAtualNome != null) {
            ds.setTorneioAtual(ds.findTorneioByName(sd.torneioAtualNome));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static void write(String filename, String json) throws IOException {
        Files.writeString(DATA_DIR.resolve(filename), json);
    }

    /** Uses reflection to set the private id field, bypassing the auto-increment counter */
    private static void setId(Object obj, String fieldName, int value) {
        try {
            // Walk up class hierarchy to find the field
            Class<?> cls = obj.getClass();
            while (cls != null) {
                try {
                    var field = cls.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(obj, value);
                    return;
                } catch (NoSuchFieldException e) {
                    cls = cls.getSuperclass();
                }
            }
        } catch (Exception e) {
            // non-critical
        }
    }
}
