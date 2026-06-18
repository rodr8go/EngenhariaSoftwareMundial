package torneoapp.model;

public class EventoPessoal extends EventoJogo {
    private Jogador jogador;
    private String tipoEvento; // Golo, Cartão Amarelo, Cartão Vermelho, Assistência

    public EventoPessoal(Jogo jogo, int minuto, String descricao, Jogador jogador, String tipoEvento) {
        super(jogo, minuto, descricao);
        this.jogador = jogador;
        this.tipoEvento = tipoEvento;
    }

    public Jogador getJogador() { return jogador; }
    public void setJogador(Jogador jogador) { this.jogador = jogador; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    @Override
    public String toString() { return minuto + "' " + tipoEvento + " - " + (jogador != null ? jogador.toString() : ""); }
}
