package cinemax.gestione;

import java.util.UUID;
import java.util.Date;
import java.time.LocalTime;

/**
 * Rappresenta una proiezione cinematografica (spettacolo) all'interno del sistema Cinemax.
 * <p>
 * Gestisce l'associazione tra un film, la data e l'ora dello spettacolo, il prezzo del biglietto
 * e la disponibilità dei posti in sala (capienza massima di 200 posti).
 * </p>
 *
 * @author Oliver Munger , matricola num. 764208 , VA
 * @author Davide Gatti , matricola num. 765949 , VA
 * @author Davide Noe Velasquez Carpio , matricola num. 765163 , VA
 */
public class Proiezione {


    /**
     * Rappresenta l'identificativo univoco della proiezione.
     *
     * <p>L'attributo <code>idProiezione</code> è dichiarato come costante (<code>final</code>)
     * per garantire che il codice identificativo non possa essere modificato
     * dopo la creazione della proiezione.
     */
    private final String idProiezione;

    /**
     * Indica la data in cui si terrà la proiezione cinematografica.
     *
     * <p>L'attributo <code>dataProiezione</code> viene utilizzato per la pianificazione
     * del calendario e per filtrare gli spettacoli disponibili.
     */
    private Date dataProiezione;

    /**
     * Specifica l'orario di inizio della proiezione in sala.
     *
     * <p>L'attributo <code>oraProiezione</code> è memorizzato come oggetto <code>LocalTime</code>
     * per gestire la programmazione oraria giornaliera.
     */
    private LocalTime oraProiezione;

    /**
     * Definisce il prezzo standard per l'acquisto di un biglietto per questa proiezione.
     *
     * <p>L'attributo <code>prezzoBiglietto</code> è costante (<code>final</code>)
     * per mantenere fisso il costo stabilito per lo spettacolo.
     */
    private final double prezzoBiglietto;

    /**
     * Riferimento al film proiettato durante lo spettacolo.
     *
     * <p>L'attributo <code>film</code> punta all'oggetto <code>Film</code> associato
     * per recuperare tutte le informazioni relative all'opera cinematografica.
     */
    private Film film;

    /**
     * Conserva il conteggio dei posti ancora liberi e prenotabili in sala.
     *
     * <p>L'attributo <code>postiDisponibili</code> viene aggiornato dinamicamente
     * a ogni nuova prenotazione o cancellazione per monitorare la capienza residua.
     */
    private int postiDisponibili;



    /**
     * COSTRUTTORE 1: Usato dal Proiezionista per creare un NUOVO spettacolo da zero.
     * <p>
     * Genera automaticamente un ID univoco casuale (prefissato con "P-") e imposta
     * il numero iniziale di posti disponibili a 200 (sala vuota).
     * </p>
     *
     * @param data            La data dello spettacolo.
     * @param ora             L'orario di inizio dello spettacolo.
     * @param prezzoBiglietto Il prezzo base del biglietto.
     * @param film            L'oggetto {@link Film} proiettato.
     */
    public Proiezione(Date data, LocalTime ora, double prezzoBiglietto, Film film) {
        this.idProiezione = "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.dataProiezione = data;
        this.oraProiezione = ora;
        this.prezzoBiglietto = prezzoBiglietto;
        this.film = film;
        this.postiDisponibili = 200;
    }

    /**
     * COSTRUTTORE 2: Usato per ripristinare uno spettacolo esistente con tipi Date e LocalTime.
     * <p>
     * Accetta l'ID, gli oggetti data/ora e lo stato esatto dei posti rimasti.
     * </p>
     *
     * @param idProiezione     L'ID univoco esistente.
     * @param data             La data dello spettacolo (oggetto {@link Date}).
     * @param ora              L'orario di inizio dello spettacolo (oggetto {@link LocalTime}).
     * @param prezzoBiglietto  Il prezzo del biglietto.
     * @param film             L'oggetto {@link Film} associato.
     * @param postiDisponibili Il numero di posti rimasti disponibili.
     */
    public Proiezione(String idProiezione, Date data, LocalTime ora, double prezzoBiglietto, Film film, int postiDisponibili) {
        this.idProiezione = idProiezione != null ? idProiezione.trim() : "";
        this.dataProiezione = data;
        this.oraProiezione = ora;
        this.prezzoBiglietto = prezzoBiglietto;
        this.film = film;
        this.postiDisponibili = postiDisponibili;
    }

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
    public Date getDataProiezione() {
        return dataProiezione;
    }

    /**
     * Restituisce l'orario della proiezione.
     *
     * @return L'ora dello spettacolo.
     */
    public LocalTime getOraProiezione() {
        return oraProiezione;
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
     * Tenta di prenotare un posto per lo spettacolo decrementando i posti disponibili.
     *
     * @return {@code true} se il posto è stato prenotato con successo (posti > 0),
     *         {@code false} se la sala è esaurita.
     */
    public boolean prenotaPosto() {
        if (this.postiDisponibili > 0) {
            this.postiDisponibili--;
            return true;
        }
        return false;
    }



    /**
     * Restituisce una rappresentazione in formato testo e multi-riga della proiezione,
     * inclusa di dettagli sul film, orario, prezzo e posti disponibili.
     *
     * @return La stringa formattata rappresentante lo spettacolo.
     */
    @Override
    public String toString() {
        String titoloFilm = (film != null) ? film.getTitolo() : "Film non specificato";
        Genere genereFilm = (film != null) ? film.getGenere() : null;

        return "ID Proiezione: " + idProiezione +
                "\nFilm: " + titoloFilm + " | Genere: " + genereFilm +
                "\nData: " + dataProiezione + " | Ora: " + oraProiezione +
                "\nPrezzo: " + String.format("%.2f€", prezzoBiglietto) + " | Posti Liberi: " + postiDisponibili + "/200";
    }
}