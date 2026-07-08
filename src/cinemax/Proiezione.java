package cinemax;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Proiezione {
    private final String idProiezione;
    private String dataOraProiezione;
    private double prezzoBiglietto;
    private Film film; // Il collegamento al film
    private int postiDisponibili = 200;

    private static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Proiezione(String idProiezione, String dataOraProiezione, double prezzoBiglietto, Film film) {
        this.idProiezione = idProiezione;
        this.dataOraProiezione = dataOraProiezione;
        this.prezzoBiglietto = prezzoBiglietto;
        this.film = film;
    }




    // Altri Getter
    public String getIdProiezione() { return idProiezione; }
    public String getDataOraProiezione() { return dataOraProiezione; }
    public double getPrezzoBiglietto() { return prezzoBiglietto; }
    public Film getFilm() { return film; }
    public int getPostiDisponibili() { return postiDisponibili; }
    public void setPostiDisponibili(int postiDisponibili) { this.postiDisponibili = postiDisponibili; }
    public void setDataOraProiezione(String dataOraProiezione) { this.dataOraProiezione = dataOraProiezione;}

    public boolean prenotaPosto() {
        if (this.postiDisponibili > 0) {
            this.postiDisponibili--; // Un posto in meno
            return true; // Prenotazione riuscita
        }
        return false; // Sala piena
    }


    public void liberaPosto() {
        if (this.postiDisponibili < 200) {
            this.postiDisponibili++; // Un posto in più
        }
    }
}
