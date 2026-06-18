package torneoapp.model;

import java.util.ArrayList;
import java.util.List;

public class Equipa {
    private static int counter = 1;
    private int id;
    private String nome;
    private String descricao;
    private String localizacao;
    private Treinador treinador;
    private List<Jogador> jogadores = new ArrayList<>();
    private String fotoPath; // caminho para a foto/emblema

    public Equipa(String nome, String descricao, String localizacao) {
        this.id = counter++;
        this.nome = nome;
        this.descricao = descricao;
        this.localizacao = localizacao;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public Treinador getTreinador() { return treinador; }
    public void setTreinador(Treinador treinador) { this.treinador = treinador; }
    public List<Jogador> getJogadores() { return jogadores; }
    public void addJogador(Jogador j) { jogadores.add(j); j.setEquipa(this); }
    public void removeJogador(Jogador j) { jogadores.remove(j); j.setEquipa(null); }
    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }

    @Override
    public String toString() { return nome; }
}
