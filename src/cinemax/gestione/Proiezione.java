package cinemax.gestione;

import java.util.UUID;

/**
 * Rappresenta una proiezione cinematografica (spettacolo) all'interno del sistema Cinemax.
 * <p>
 * Gestisce l'associazione tra un film, la data e l'ora dello spettacolo, il prezzo del biglietto
 * e la disponibilità dei posti in sala (capienza massima di 200 posti).
 * </p>
 *
 * @author Cinemax Team
 * @version 1.0
 */
public class Proiezione {

    // ------------------------------------------------------------------------
    // CAMPI
    // ------------------------------------------------------------------------

    /** L'identificativo univoco della proiezione (es. "P-A1B2C3D4"). */
    private final String idProiezione;

    /** La data della proiezione in formato stringa (es. "DD/MM/YYYY"). */
    private String dataProiezione;

    /** L'ora della proiezione in formato stringa (es. "HH:mm"). */
    private String oraProiezione;

    /** Il prezzo del singolo biglietto espresso in Euro. */
    private final double prezzoBiglietto;

    /** Il film associato a questa proiezione. */
    private Film film;

    /** Il numero di posti ancora disponibili in sala per questa proiezione. */
    private int postiDisponibili;

    // ------------------------------------------------------------------------
    // COSTRUTTORI
    // ------------------------------------------------------------------------

    /**
     * COSTRUTTORE 1: Usato dal Proiezionista per creare un NUOVO spettacolo da zero.
     * <p>
     * Genera automaticamente un ID univoco casuale (prefissato con "P-") e imposta
     * il numero iniziale di posti disponibili a 200 (sala vuota).
     * </p>

     * @param data            La data dello spettacolo.
     * @param ora             L'orario di inizio dello spettacolo.
     * @param prezzoBiglietto Il prezzo base del biglietto.
     * @param film            L'oggetto {@link Film} proiettato.
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
     * COSTRUTTORE 2: Usato dal {@code FileManager} per ripristinare uno spettacolo esistente da File.
     * <p>
     * Accetta l'ID e lo stato esatto dei posti rimasti letti direttamente dal file di persistenza CSV.
     * </p>

     * @param idProiezione     L'ID univoco esistente letto da file.
     * @param data            La data dello spettacolo.
     * @param ora             L'orario di inizio dello spettacolo.
     * @param prezzoBiglietto Il prezzo del biglietto.
     * @param film            L'oggetto {@link Film} associato.
     * @param postiDisponibili Il numero di posti rimasti disponibili.
     */
    public Proiezione(String idProiezione, String data, String ora, double prezzoBiglietto, Film film, int postiDisponibili) {
        this.idProiezione = idProiezione != null ? idProiezione.trim() : "";
        this.dataProiezione = data != null ? data.trim() : "";
        this.oraProiezione = ora != null ? ora.trim() : "";
        this.prezzoBiglietto = prezzoBiglietto;
        this.film = film;
        this.postiDisponibili = postiDisponibili; // Carica lo stato reale salvato
    }

    // ------------------------------------------------------------------------
    // METODI GETTER E SETTER
    // ------------------------------------------------------------------------

    /**
     * Restituisce l'ID univoco della proiezione.
     *
     * @return L'identificativo {@code idProiezione}.
     */
    public String getIdProiezione() {
        return idProiezione;
    }

    /**
     * Restituisce la data della proiezione.
     *
     * @return La data dello spettacolo.
     */
    public String getDataProiezione() {
        return dataProiezione;
    }

    /**
     * Imposta o aggiorna la data della proiezione.
     *
     * @param dataProiezione La nuova data dello spettacolo.
     */
    public void setDataProiezione(String dataProiezione) {
        this.dataProiezione = dataProiezione;
    }

    /**
     * Restituisce l'orario della proiezione.
     *
     * @return L'ora dello spettacolo.
     */
    public String getOraProiezione() {
        return oraProiezione;
    }

    /**
     * Imposta o aggiorna l'orario della proiezione.
     *
     * @param oraProiezione Il nuovo orario dello spettacolo.
     */
    public void setOraProiezione(String oraProiezione) {
        this.oraProiezione = oraProiezione;
    }

    /**
     * Restituisce il prezzo del biglietto per questa proiezione.
     *
     * @return Il prezzo in Euro.
     */
    public double getPrezzoBiglietto() {
        return prezzoBiglietto;
    }

    /**
     * Restituisce il film associato alla proiezione.
     *
     * @return L'oggetto {@link Film}.
     */
    public Film getFilm() {
        return film;
    }

    /**
     * Imposta o modifica il film associato a questa proiezione.
     *
     * @param film Il nuovo oggetto {@link Film}.
     */
    public void setFilm(Film film) {
        this.film = film;
    }

    /**
     * Restituisce il numero attuale di posti disponibili in sala.
     *
     * @return Il numero di posti liberi.
     */
    public int getPostiDisponibili() {
        return postiDisponibili;
    }

    /**
     * Imposta manualmente il numero di posti disponibili.
     *
     * @param postiDisponibili Il nuovo totale di posti liberi.
     */
    public void setPostiDisponibili(int postiDisponibili) {
        this.postiDisponibili = postiDisponibili;
    }

    // ------------------------------------------------------------------------
    // METODI DI BUSINESS LOGIC (Gestione Posti)
    // ------------------------------------------------------------------------

    /**
     * Tenta di prenotare un posto per lo spettacolo decrementando i posti disponibili.
     *
     * @return {@code true} se il posto e' stato prenotato con successo (posti > 0),
     *         {@code false} se la sala e' esaurita.
     */
    public boolean prenotaPosto() {
        if (this.postiDisponibili > 0) {
            this.postiDisponibili--;
            return true;
        }
        return false;
    }

    /**
     * Incrementa i posti disponibili a seguito dell'annullamento di una prenotazione,
     * garantendo di non superare la capienza massima della sala (200 posti).
     */
    public void liberaPosto() {
        if (this.postiDisponibili < 200) {
            this.postiDisponibili++;
        }
    }

    /**
     * Restituisce una rappresentazione in formato testo e multi-riga della proiezione,
     * inclusa di dettagli sul film, orario, prezzo e posti disponibili.
     *
     * @return La stringa formattata rappresentante lo spettacolo.
     */
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