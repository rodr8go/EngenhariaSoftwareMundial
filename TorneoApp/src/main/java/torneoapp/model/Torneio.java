package torneoapp.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Torneio {
    private static int counter = 1;
    private int id;
    private String nome;
    private LocalDate dataInicio;
    private int numEquipas;
    private List<Jogo> jogos = new ArrayList<>();
    private List<Equipa> equipas = new ArrayList<>();
    // fase atual: "grupos" ou "eliminacao"
    private String faseAtual = "grupos";

    public Torneio(String nome, LocalDate dataInicio, int numEquipas) {
        this.id = counter++;
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.numEquipas = numEquipas;
    }

    public int getId() { return id; }
    public String getNome() { return nome; } public void setNome(String n) { this.nome = n; }
    public LocalDate getDataInicio() { return dataInicio; } public void setDataInicio(LocalDate d) { this.dataInicio = d; }
    public int getNumEquipas() { return numEquipas; } public void setNumEquipas(int n) { this.numEquipas = n; }
    public List<Jogo> getJogos() { return jogos; } public void addJogo(Jogo j) { jogos.add(j); }
    public List<Equipa> getEquipas() { return equipas; } public void addEquipa(Equipa e) { equipas.add(e); }
    public String getFaseAtual() { return faseAtual; } public void setFaseAtual(String f) { this.faseAtual = f; }

    /** Verifica se todos os jogos de grupos estão terminados */
    public boolean faseGruposCompleta() {
        List<Jogo> gr = jogos.stream().filter(j -> "grupos".equals(j.getFase())).toList();
        return !gr.isEmpty() && gr.stream().allMatch(Jogo::isTerminado);
    }

    @Override public String toString() { return nome; }
}
