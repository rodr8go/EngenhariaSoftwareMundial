package torneoapp.model;

import java.time.LocalDate;

public class ContratoPatrocinio {
    private static int counter = 1;
    private int id;
    private String numero;
    private Patrocinador patrocinador;
    private double valor;
    private String tipoPatrocinio;
    private String direitos;
    private Jogo jogo;
    private LocalDate dataContrato;

    public ContratoPatrocinio(String numero, Patrocinador patrocinador, double valor,
                               String tipoPatrocinio, String direitos, Jogo jogo, LocalDate dataContrato) {
        this.id = counter++;
        this.numero = numero;
        this.patrocinador = patrocinador;
        this.valor = valor;
        this.tipoPatrocinio = tipoPatrocinio;
        this.direitos = direitos;
        this.jogo = jogo;
        this.dataContrato = dataContrato;
    }

    public int getId() { return id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public Patrocinador getPatrocinador() { return patrocinador; }
    public void setPatrocinador(Patrocinador patrocinador) { this.patrocinador = patrocinador; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public String getTipoPatrocinio() { return tipoPatrocinio; }
    public void setTipoPatrocinio(String tipoPatrocinio) { this.tipoPatrocinio = tipoPatrocinio; }
    public String getDireitos() { return direitos; }
    public void setDireitos(String direitos) { this.direitos = direitos; }
    public Jogo getJogo() { return jogo; }
    public void setJogo(Jogo jogo) { this.jogo = jogo; }
    public LocalDate getDataContrato() { return dataContrato; }
    public void setDataContrato(LocalDate dataContrato) { this.dataContrato = dataContrato; }

    @Override
    public String toString() { return numero + " - " + (patrocinador != null ? patrocinador.getNome() : ""); }
}
