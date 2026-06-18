package torneoapp.model;

public class Patrocinador {
    private static int counter = 1;
    private int id;
    private String nome;
    private String nif;
    private String morada;
    private String email;
    private String setorAtividade;
    private String telemovel;
    private String descricao;
    private String fotoPath;

    public Patrocinador(String nome, String nif, String morada, String email,
                        String setorAtividade, String telemovel, String descricao) {
        this.id = counter++;
        this.nome = nome; this.nif = nif; this.morada = morada; this.email = email;
        this.setorAtividade = setorAtividade; this.telemovel = telemovel; this.descricao = descricao;
    }

    public int getId() { return id; }
    public String getNome() { return nome; } public void setNome(String n) { this.nome = n; }
    public String getNif() { return nif; } public void setNif(String n) { this.nif = n; }
    public String getMorada() { return morada; } public void setMorada(String m) { this.morada = m; }
    public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
    public String getSetorAtividade() { return setorAtividade; } public void setSetorAtividade(String s) { this.setorAtividade = s; }
    public String getTelemovel() { return telemovel; } public void setTelemovel(String t) { this.telemovel = t; }
    public String getDescricao() { return descricao; } public void setDescricao(String d) { this.descricao = d; }
    public String getFotoPath() { return fotoPath; } public void setFotoPath(String f) { this.fotoPath = f; }

    @Override public String toString() { return nome; }
}
