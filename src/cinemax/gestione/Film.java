package cinemax.gestione;

/**
 * Rappresenta un opera cinematografica all'interno del sistema Cinemax.
 * <p>
 * Contiene le informazioni generali sul film come titolo, genere, regista,
 * anno di uscita, durata in minuti ed eventuale limite di eta' minima.
 * </p>
 *
 * @author Oliver Munger , matricola num. 764208 , VA
 * @author Davide Gatti , matricola num. 765949 , VA
 * @author Davide Noe Velasquez Carpio , matricola num. 765163 , VA
 */
public class Film {


    /**
     * Rappresenta il titolo ufficiale del film.
     *
     * <p>L'attributo <code>titolo</code> viene utilizzato per identificare
     * l'opera all'interno della programmazione e nelle ricerche dei clienti.
     */
    private String titolo;

    /**
     * Indica la categoria o il genere cinematografico di appartenenza.
     *
     * <p>L'attributo <code>genere</code> fa riferimento all'enumerazione
     * o alla classe <code>Genere</code> associata alla proiezione.
     */
    private Genere genere;

    /**
     * Conserva il nome del regista che ha diretto il film.
     *
     * <p>L'attributo <code>regista</code> è memorizzato come stringa di testo
     * per fini informativi e di catalogazione.
     */
    private String regista;

    /**
     * Specifica l'anno di uscita o di produzione della pellicola.
     *
     * <p>L'attributo <code>anno</code> viene rappresentato come valore numerico intero
     * per eventuali ordinamenti o filtri temporali.
     */
    private int anno;

    /**
     * Indica la durata complessiva del film espressa in minuti.
     *
     * <p>L'attributo <code>durata</code> è utile per calcolare gli orari
     * di inizio e fine delle singole proiezioni in sala.
     */
    private int durata;

    /**
     * Definisce il limite di età minimo consigliato o imposto per la visione.
     *
     * <p>L'attributo <code>eta_minima</code> viene utilizzato dai sistemi di controllo
     * per verificare i requisiti d'accesso durante la prenotazione o l'emissione del biglietto.
     */
    private int eta_minima;

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
    public Film(String titolo, Genere genere, String regista, int anno, int durata, int eta_minima) {
        this.titolo = titolo;
        this.genere = genere;
        this.regista = regista;
        this.anno = anno;
        this.durata = durata;
        this.eta_minima = eta_minima;
    }


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
    public Genere getGenere() {
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