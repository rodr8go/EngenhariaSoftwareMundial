package torneoapp.model;

public abstract class EventoJogo {
    private static int counter = 1;
    protected int id;
    protected Jogo jogo;
    protected int minuto;
    protected String descricao;

    public EventoJogo(Jogo jogo, int minuto, String descricao) {
        this.id = counter++;
        this.jogo = jogo;
        this.minuto = minuto;
        this.descricao = descricao;
    }

    public int getId() { return id; }
    public Jogo getJogo() { return jogo; }
    public int getMinuto() { return minuto; }
    public void setMinuto(int minuto) { this.minuto = minuto; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
