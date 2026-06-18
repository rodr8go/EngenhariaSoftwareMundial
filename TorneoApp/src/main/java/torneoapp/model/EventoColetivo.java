package torneoapp.model;

public class EventoColetivo extends EventoJogo {
    private Equipa equipa;
    private String tipoEvento; // Canto, Falta, etc.

    public EventoColetivo(Jogo jogo, int minuto, String descricao, Equipa equipa, String tipoEvento) {
        super(jogo, minuto, descricao);
        this.equipa = equipa;
        this.tipoEvento = tipoEvento;
    }

    public Equipa getEquipa() { return equipa; }
    public void setEquipa(Equipa equipa) { this.equipa = equipa; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    @Override
    public String toString() { return minuto + "' " + tipoEvento + " (" + (equipa != null ? equipa.getNome() : "") + ")"; }
}
