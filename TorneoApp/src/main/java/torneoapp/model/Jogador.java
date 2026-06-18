package torneoapp.model;

import java.time.LocalDate;

public class Jogador extends Participante {
    private String alcunha;
    private String posicao;
    private int altura;
    private int numeroCamisola;
    private int golos;
    private int assistencias;
    private int cartoesAmarelos;
    private int cartoesVermelhos;
    private Equipa equipa;
    private String fotoPath;

    public Jogador(String nomeCompleto, String alcunha, String posicao,
                   LocalDate dataNascimento, String nacionalidade, int altura, int numeroCamisola) {
        super(nomeCompleto, dataNascimento, nacionalidade);
        this.alcunha = alcunha;
        this.posicao = posicao;
        this.altura = altura;
        this.numeroCamisola = numeroCamisola;
    }

    public String getAlcunha() { return alcunha; }
    public void setAlcunha(String a) { this.alcunha = a; }
    public String getPosicao() { return posicao; }
    public void setPosicao(String p) { this.posicao = p; }
    public int getAltura() { return altura; }
    public void setAltura(int a) { this.altura = a; }
    public int getNumeroCamisola() { return numeroCamisola; }
    public void setNumeroCamisola(int n) { this.numeroCamisola = n; }
    public int getGolos() { return golos; }
    public void setGolos(int g) { this.golos = g; }
    public int getAssistencias() { return assistencias; }
    public void setAssistencias(int a) { this.assistencias = a; }
    public int getCartoesAmarelos() { return cartoesAmarelos; }
    public void setCartoesAmarelos(int c) { this.cartoesAmarelos = c; }
    public int getCartoesVermelhos() { return cartoesVermelhos; }
    public void setCartoesVermelhos(int c) { this.cartoesVermelhos = c; }
    public Equipa getEquipa() { return equipa; }
    public void setEquipa(Equipa e) { this.equipa = e; }
    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }

    @Override
    public String toString() {
        return (alcunha != null && !alcunha.isBlank()) ? alcunha : nomeCompleto;
    }
}
