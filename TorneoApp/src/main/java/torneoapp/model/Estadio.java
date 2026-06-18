package torneoapp.model;

import java.util.ArrayList;
import java.util.List;

public class Estadio {
    private static int counter = 1;
    private int id;
    private String nome;
    private String morada;
    private String cidade;
    private List<Setor> setores = new ArrayList<>();
    private String fotoPath;

    public Estadio(String nome, String morada, String cidade, int lotacaoIgnorada) {
        this.id = counter++;
        this.nome = nome;
        this.morada = morada;
        this.cidade = cidade;
    }
    public Estadio(String nome, String morada, String cidade) {
        this(nome, morada, cidade, 0);
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMorada() { return morada; }
    public void setMorada(String morada) { this.morada = morada; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    /** Calculada automaticamente como soma das lotações dos setores */
    public int getLotacao() { return setores.stream().mapToInt(Setor::getLotacao).sum(); }
    /** @deprecated lotação é calculada pelos setores */
    @Deprecated public void setLotacao(int lotacao) { /* ignorado */ }
    public List<Setor> getSetores() { return setores; }
    public void addSetor(Setor s) { setores.add(s); }
    public void removeSetor(Setor s) { setores.remove(s); }
    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }

    @Override
    public String toString() { return nome; }
}
