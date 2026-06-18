package torneoapp.model;

public class Voluntario {
    private static int counter = 1;
    private int id;
    private String categoria;
    private int quantidade;
    private Jogo jogo;

    public Voluntario(String categoria, int quantidade) {
        this.id = counter++;
        this.categoria = categoria;
        this.quantidade = quantidade;
    }

    public int getId() { return id; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public Jogo getJogo() { return jogo; }
    public void setJogo(Jogo jogo) { this.jogo = jogo; }

    @Override
    public String toString() { return categoria + " (" + quantidade + ")"; }
}
