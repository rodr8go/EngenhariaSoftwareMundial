package torneoapp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Jogo {
    private static int counter = 1;
    private int id;
    private Equipa equipaCasa;
    private Equipa equipaVisitante;
    private Estadio estadio;
    private LocalDateTime dataHora;

    // Resultado normal (90 min)
    private int golosCasa;
    private int golosVisitante;

    // Prolongamento (30 min extra — só eliminação)
    private boolean foiProlongamento = false;
    private int golosCasaProlongamento;
    private int golosVisitanteProlongamento;

    // Penaltis
    private boolean foiPenaltis = false;
    private int penaltisCasa;
    private int penaltisVisitante;

    private boolean terminado;
    private double posseCasa;
    private double posseVisitante;
    private int cantosCasa;
    private int cantosVisitante;
    private int cartoesAmarelosCasa;
    private int cartoesAmarelosVisitante;
    private int cartoesVermelhosCasa;
    private int cartoesVermelhosVisitante;
    private String fase = "grupos";
    private String ronda = "";
    private int posicaoBracket = -1;
    private List<Voluntario> voluntarios = new ArrayList<>();
    private List<EventoJogo> eventos     = new ArrayList<>();
    private List<Bilhete>    bilhetes    = new ArrayList<>();
    private boolean precarioDefined = false;

    public Jogo(Equipa equipaCasa, Equipa equipaVisitante, Estadio estadio, LocalDateTime dataHora) {
        this.id = counter++;
        this.equipaCasa = equipaCasa;
        this.equipaVisitante = equipaVisitante;
        this.estadio = estadio;
        this.dataHora = dataHora;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public int getId()                    { return id; }
    public Equipa getEquipaCasa()         { return equipaCasa; }
    public void setEquipaCasa(Equipa e)   { this.equipaCasa = e; }
    public Equipa getEquipaVisitante()    { return equipaVisitante; }
    public void setEquipaVisitante(Equipa e) { this.equipaVisitante = e; }
    public Estadio getEstadio()           { return estadio; }
    public void setEstadio(Estadio e)     { this.estadio = e; }
    public LocalDateTime getDataHora()    { return dataHora; }
    public void setDataHora(LocalDateTime d) { this.dataHora = d; }

    public int getGolosCasa()             { return golosCasa; }
    public void setGolosCasa(int v)       { this.golosCasa = v; }
    public int getGolosVisitante()        { return golosVisitante; }
    public void setGolosVisitante(int v)  { this.golosVisitante = v; }

    public boolean isFoiProlongamento()           { return foiProlongamento; }
    public void setFoiProlongamento(boolean v)    { this.foiProlongamento = v; }
    public int getGolosCasaProlongamento()         { return golosCasaProlongamento; }
    public void setGolosCasaProlongamento(int v)   { this.golosCasaProlongamento = v; }
    public int getGolosVisitanteProlongamento()    { return golosVisitanteProlongamento; }
    public void setGolosVisitanteProlongamento(int v) { this.golosVisitanteProlongamento = v; }

    public boolean isFoiPenaltis()        { return foiPenaltis; }
    public void setFoiPenaltis(boolean v) { this.foiPenaltis = v; }
    public int getPenaltisCasa()          { return penaltisCasa; }
    public void setPenaltisCasa(int v)    { this.penaltisCasa = v; }
    public int getPenaltisVisitante()     { return penaltisVisitante; }
    public void setPenaltisVisitante(int v) { this.penaltisVisitante = v; }

    public boolean isTerminado()          { return terminado; }
    public void setTerminado(boolean v)   { this.terminado = v; }
    public double getPosseCasa()          { return posseCasa; }
    public void setPosseCasa(double v)    { this.posseCasa = v; }
    public double getPosseVisitante()     { return posseVisitante; }
    public void setPosseVisitante(double v){ this.posseVisitante = v; }
    public int getCantosCasa()            { return cantosCasa; }
    public void setCantosCasa(int v)      { this.cantosCasa = v; }
    public int getCantosVisitante()       { return cantosVisitante; }
    public void setCantosVisitante(int v) { this.cantosVisitante = v; }
    public int getCartoesAmarelosCasa()   { return cartoesAmarelosCasa; }
    public void setCartoesAmarelosCasa(int v) { this.cartoesAmarelosCasa = v; }
    public int getCartoesAmarelosVisitante() { return cartoesAmarelosVisitante; }
    public void setCartoesAmarelosVisitante(int v) { this.cartoesAmarelosVisitante = v; }
    public int getCartoesVermelhosCasa()  { return cartoesVermelhosCasa; }
    public void setCartoesVermelhosCasa(int v) { this.cartoesVermelhosCasa = v; }
    public int getCartoesVermelhosVisitante() { return cartoesVermelhosVisitante; }
    public void setCartoesVermelhosVisitante(int v) { this.cartoesVermelhosVisitante = v; }
    public String getFase()               { return fase; }
    public void setFase(String f)         { this.fase = f; }
    public String getRonda()              { return ronda; }
    public void setRonda(String r)        { this.ronda = r; }
    public int getPosicaoBracket()        { return posicaoBracket; }
    public void setPosicaoBracket(int v)  { this.posicaoBracket = v; }
    public List<Voluntario> getVoluntarios() { return voluntarios; }
    public void addVoluntario(Voluntario v) { v.setJogo(this); voluntarios.add(v); }
    public List<EventoJogo> getEventos()  { return eventos; }
    public void addEvento(EventoJogo e)   { eventos.add(e); }
    public List<Bilhete> getBilhetes()    { return bilhetes; }
    public void addBilhete(Bilhete b)     { bilhetes.add(b); }
    public boolean isPrecarioDefined()    { return precarioDefined; }
    public void setPrecarioDefined(boolean v) { this.precarioDefined = v; }

    /**
     * Retorna o vencedor do jogo (para eliminatórias).
     * Considera resultado normal, prolongamento e penaltis.
     * Retorna null se empate (só pode acontecer em fase de grupos).
     */
    public Equipa getVencedor() {
        if (!terminado) return null;
        int totalCasa  = golosCasa  + golosCasaProlongamento;
        int totalVisit = golosVisitante + golosVisitanteProlongamento;
        if (foiPenaltis) {
            return penaltisCasa > penaltisVisitante ? equipaCasa : equipaVisitante;
        }
        if (totalCasa > totalVisit) return equipaCasa;
        if (totalVisit > totalCasa) return equipaVisitante;
        return null; // empate (grupos)
    }

    /** Resultado total incluindo prolongamento */
    public int getGolosTotalCasa()     { return golosCasa + golosCasaProlongamento; }
    public int getGolosTotalVisitante(){ return golosVisitante + golosVisitanteProlongamento; }

    public double getReceitaBilheteira() {
        return bilhetes.stream().mapToDouble(Bilhete::getPrecoFinal).sum();
    }

    @Override
    public String toString() {
        String casa  = equipaCasa != null ? equipaCasa.getNome() : "?";
        String visit = equipaVisitante != null ? equipaVisitante.getNome() : "?";
        if (terminado) {
            String res = getGolosTotalCasa() + " - " + getGolosTotalVisitante();
            if (foiPenaltis) res += " (p: " + penaltisCasa + "-" + penaltisVisitante + ")";
            else if (foiProlongamento) res += " (p.e.)";
            return casa + " " + res + " " + visit;
        }
        return casa + " vs " + visit + (dataHora != null ? " (" + dataHora.toLocalDate() + ")" : "");
    }
}
