package cinemax;

import java.util.UUID;

public class Proiezione {
    // CAMPI
    private final String idProiezione;
    private String dataProiezione;
    private String oraProiezione;
    private final double prezzoBiglietto;
    private Film film;
    private int postiDisponibili;

    /**
     * COSTRUTTORE 1: Usato dal Proiezionista (Crea un NUOVO spettacolo da zero)
     * Genera AUTOMATICAMENTE l'ID univoco e imposta i posti a 200
     */
    public Proiezione(String data, String ora, double prezzoBiglietto, Film film) {
        // Genera un ID compatto del tipo "P-A1B2C3D4"
        this.idProiezione = "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.dataProiezione = data != null ? data.trim() : "";
        this.oraProiezione = ora != null ? ora.trim() : "";
        this.prezzoBiglietto = prezzoBiglietto;
        this.film = film;
        this.postiDisponibili = 200; // Nuovo spettacolo = Sala vuota
    }

    /**
     * COSTRUTTORE 2: Usato dal FileManager (Ripristina uno spettacolo esistente da File)
     * Accetta l'ID e il numero di posti rimasti letti direttamente dal file CSV
     */
    public Proiezione(String idProiezione, String data, String ora, double prezzoBiglietto, Film film, int postiDisponibili) {
        this.idProiezione = idProiezione != null ? idProiezione.trim() : "";
        this.dataProiezione = data != null ? data.trim() : "";
        this.oraProiezione = ora != null ? ora.trim() : "";
        this.prezzoBiglietto = prezzoBiglietto;
        this.film = film;
        this.postiDisponibili = postiDisponibili; // Carica lo stato reale salvato
    }

    // METODI GETTER E SETTER
    public String getIdProiezione() { return idProiezione; }

    public String getDataProiezione() { return dataProiezione; }
    public void setDataProiezione(String dataProiezione) { this.dataProiezione = dataProiezione; }

    public String getOraProiezione() { return oraProiezione; }
    public void setOraProiezione(String oraProiezione) { this.oraProiezione = oraProiezione; }

    public double getPrezzoBiglietto() { return prezzoBiglietto; }

    public Film getFilm() { return film; }
    public void setFilm(Film film) { this.film = film; }

    public int getPostiDisponibili() { return postiDisponibili; }
    public void setPostiDisponibili(int postiDisponibili) { this.postiDisponibili = postiDisponibili; }

    // METODI DI BUSINESS LOGIC (Gestione Posti)
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

    @Override
    public String toString() {
        // 1. Definiamo le stringhe sicure controllando se l'oggetto film esiste
        String titoloFilm = (film != null) ? film.getTitolo() : "Film non specificato";
        String genereFilm = (film != null) ? film.getGenere() : "N/D";

        // 2. Usiamo le variabili sicure nella stringa finale
        return "ID Proiezione: " + idProiezione +
                "\nFilm: " + titoloFilm + " | Genere: " + genereFilm +
                "\nData: " + dataProiezione + " | Ora: " + oraProiezione +
                "\nPrezzo: " + String.format("%.2f€", prezzoBiglietto) + " | Posti Liberi: " + postiDisponibili + "/200";
    }
}