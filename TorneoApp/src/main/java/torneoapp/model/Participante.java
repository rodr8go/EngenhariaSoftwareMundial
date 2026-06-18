package torneoapp.model;

import java.time.LocalDate;

public abstract class Participante {
    private static int counter = 1;
    protected int id;
    protected String nomeCompleto;
    protected LocalDate dataNascimento;
    protected String nacionalidade;

    public Participante(String nomeCompleto, LocalDate dataNascimento, String nacionalidade) {
        this.id = counter++;
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.nacionalidade = nacionalidade;
    }

    public int getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }

    public int getIdade() {
        if (dataNascimento == null) return 0;
        return LocalDate.now().getYear() - dataNascimento.getYear();
    }
}
