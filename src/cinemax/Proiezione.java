package cinemax;

public class Proiezione {
    // CAMPI
    private final String idProiezione;
    private String dataProiezione;
    private String oraProiezione;
    private final double prezzoBiglietto;
    private Film film;
    private int postiDisponibili; // Inizializzato dentro i singoli costruttori

    /**
     * COSTRUTTORE 1: Usato dal Proiezionista (Crea un NUOVO spettacolo da zero)
     * Imposta automaticamente i posti al massimo della capacità (200)
     */
    public Proiezione(String idProiezione, String data, String ora, double prezzoBiglietto, Film film) {
        this.idProiezione = idProiezione != null ? idProiezione.trim() : "";
        this.dataProiezione = data != null ? data.trim() : "";
        this.oraProiezione = ora != null ? ora.trim() : "";
        this.prezzoBiglietto = prezzoBiglietto;
        this.film = film;
        this.postiDisponibili = 200; // Nuovo spettacolo = Sala vuota
    }

    /**
     * COSTRUTTORE 2: Usato dal FileManager (Ripristina uno spettacolo esistente da File)
     * Accetta il numero di posti rimasti letto direttamente dal file CSV
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
        return "ID Proiezione: " + idProiezione +
                "\nFilm: " + film.getTitolo() + " | Genere: " + film.getGenere() +
                "\nData: " + dataProiezione + " | Ora: " + oraProiezione +
                "\nPrezzo: " + String.format("%.2f€", prezzoBiglietto) + " | Posti Liberi: " + postiDisponibili + "/200";
    }
}