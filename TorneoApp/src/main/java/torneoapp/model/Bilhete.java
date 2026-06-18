package torneoapp.model;

public class Bilhete {
    private static int counter = 1;
    private int id;
    private Jogo jogo;
    private Setor setor;
    private String nif;
    private double precoFinal;
    private int numLugar; // número de lugar único dentro do setor

    public Bilhete(Jogo jogo, Setor setor, String nif, double precoFinal, int numLugar) {
        this.id = counter++;
        this.jogo = jogo;
        this.setor = setor;
        this.nif = nif;
        this.precoFinal = precoFinal;
        this.numLugar = numLugar;
    }

    public int getId()              { return id; }
    public Jogo getJogo()           { return jogo; }
    public Setor getSetor()         { return setor; }
    public String getNif()          { return nif; }
    public double getPrecoFinal()   { return precoFinal; }
    public double getDesconto()     { return 0; } // kept for compatibility
    public int getNumLugar()        { return numLugar; }

    @Override
    public String toString() {
        return "Bilhete #" + id + " — " + (jogo != null ? jogo.toString() : "")
             + " [" + (setor != null ? setor.getNome() : "") + " · Lugar " + numLugar + "]";
    }
}
