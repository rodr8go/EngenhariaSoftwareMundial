package torneoapp.model;

import java.time.LocalDate;

public class Treinador extends Participante {
    private String email;
    private String telemovel;
    private Equipa equipa;

    public Treinador(String nomeCompleto, LocalDate dataNascimento, String nacionalidade,
                     String email, String telemovel) {
        super(nomeCompleto, dataNascimento, nacionalidade);
        this.email = email;
        this.telemovel = telemovel;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelemovel() { return telemovel; }
    public void setTelemovel(String telemovel) { this.telemovel = telemovel; }
    public Equipa getEquipa() { return equipa; }
    public void setEquipa(Equipa equipa) { this.equipa = equipa; }

    @Override
    public String toString() { return nomeCompleto; }
}
