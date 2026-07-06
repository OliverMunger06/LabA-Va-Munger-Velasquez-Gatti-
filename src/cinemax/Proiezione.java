package cinemax;

public class Proiezione {
    private String idProiezione;
    private String dataOraProiezione;
    private double prezzoBiglietto;
    private Film film; // Il collegamento al film
    private int postiDisponibili = 200;

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
