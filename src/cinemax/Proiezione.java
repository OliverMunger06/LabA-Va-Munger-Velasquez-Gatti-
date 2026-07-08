package cinemax;

public class Proiezione {
    // CAMPI
    private final int idProiezione;
    private String dataOraProiezione; // Unico campo stringa
    private final double prezzoBiglietto;
    private Film film;
    private int postiDisponibili = 200;

    // COSTRUTTORE MODIFICATO: accetta data e ora separate
    public Proiezione(int idProiezione, String data, String ora, double prezzoBiglietto, Film film) {
        this.idProiezione = idProiezione;
        // Uniamo data e ora in un'unica stringa (es. "2026-07-08 20:30")
        this.dataOraProiezione = data + " " + ora;
        this.prezzoBiglietto = prezzoBiglietto;
        this.film = film;
    }

    // METODI
    public int getIdProiezione() { return idProiezione; }
    public String getDataOraProiezione() { return dataOraProiezione; }
    public double getPrezzoBiglietto() { return prezzoBiglietto; }
    public Film getFilm() { return film; }
    public int getPostiDisponibili() { return postiDisponibili; }
    public void setPostiDisponibili(int postiDisponibili) { this.postiDisponibili = postiDisponibili; }

    // Setter modificato per accettare data e ora separate se si vuole aggiornare
    public void setDataOraProiezione(String data, String ora) {
        this.dataOraProiezione = data + " " + ora;
    }

    // Se nel main vuoi riprendere SOLO la data o SOLO l'ora, usiamo lo split:
    public String getData() {
        return this.dataOraProiezione.split(" ")[0];
    }

    public String getOra() {
        return this.dataOraProiezione.split(" ")[1];
    }

    public boolean prenotaPosto() {
        if (this.postiDisponibili > 0) {
            this.postiDisponibili--;
            return true;
        }
        return false;
    }

    public void liberaPosto() {
        if (this.postiDisponibili < 200) {
            this.postiDisponibili++;
        }
    }
}