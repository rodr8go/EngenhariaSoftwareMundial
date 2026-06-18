package torneoapp.model;

public class Setor {
    private static int counter = 1;
    private int id;
    private String nome;
    private int lotacao;
    private double preco;
    private double desconto; // percentage 0-100

    public Setor(String nome, int lotacao) {
        this.id = counter++;
        this.nome = nome;
        this.lotacao = lotacao;
        this.preco = 0.0;
        this.desconto = 0.0;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getLotacao() { return lotacao; }
    public void setLotacao(int lotacao) { this.lotacao = lotacao; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public double getDesconto() { return desconto; }
    public void setDesconto(double desconto) { this.desconto = desconto; }

    @Override
    public String toString() { return nome + " (" + lotacao + " lugares)"; }
}
