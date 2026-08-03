package cinemax.gestione;

/**
 * Rappresenta un opera cinematografica all'interno del sistema Cinemax.
 * <p>
 * Contiene le informazioni generali sul film come titolo, genere, regista,
 * anno di uscita, durata in minuti ed eventuale limite di eta' minima.
 * </p>
 *
 * @author Cinemax Team
 * @version 1.0
 */
public class Film {

    /** Il titolo del film. */
    private String titolo;

    /** Il genere cinematografico (es. Azione, Drammatico, Commedia). */
    private String genere;

    /** Il nome e cognome del regista. */
    private String regista;

    /** L'anno di pubblicazione o uscita del film. */
    private int anno;

    /** La durata del film espressa in minuti. */
    private int durata;

    /** L'eta' minima richiesta per la visione del film (es. 0, 14, 18). */
    private int eta_minima;

    // ------------------------------------------------------------------------
    // COSTRUTTORI
    // ------------------------------------------------------------------------

    /**
     * Costruisce un nuovo oggetto {@code Film} con le informazioni specificate.
     *
     * @param titolo     Il titolo del film.
     * @param genere     Il genere del film.
     * @param regista    Il regista del film.
     * @param anno       L'anno di uscita.
     * @param durata     La durata in minuti.
     * @param eta_minima L'eta' minima consona/richiesta per la visione.
     */
    public Film(String titolo, String genere, String regista, int anno, int durata, int eta_minima) {
        this.titolo = titolo;
        this.genere = genere;
        this.regista = regista;
        this.anno = anno;
        this.durata = durata;
        this.eta_minima = eta_minima;
    }

    // ------------------------------------------------------------------------
    // GETTER
    // ------------------------------------------------------------------------

    /**
     * Restituisce il titolo del film.
     *
     * @return Il titolo del film.
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Restituisce il genere del film.
     *
     * @return Il genere cinematografico.
     */
    public String getGenere() {
        return genere;
    }

    /**
     * Restituisce il nome del regista.
     *
     * @return Il regista del film.
     */
    public String getRegista() {
        return regista;
    }

    /**
     * Restituisce l'anno di uscita del film.
     *
     * @return L'anno di pubblicazione.
     */
    public int getAnno() {
        return anno;
    }

    /**
     * Restituisce la durata del film.
     *
     * @return La durata espressa in minuti.
     */
    public int getDurata() {
        return durata;
    }

    /**
     * Restituisce l'eta' minima raccomandata o vincolante per la visione del film.
     *
     * @return L'eta' minima consentita.
     */
    public int getEta_minima() {
        return eta_minima;
    }
}