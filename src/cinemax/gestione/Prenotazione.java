package cinemax.gestione;

import cinemax.Users.Utente;
import java.util.UUID;

/**
 * Rappresenta una prenotazione (o biglietto digitale) effettuata da un utente per una determinata proiezione.
 * <p>
 * Contiene le informazioni relative al cliente, i dettagli della proiezione selezionata,
 * il numero del posto assegnato e un codice univoco di verifica biglietto (QR Code).
 * </p>
 *
 * @author Cinemax Team
 * @version 1.0
 */
public class Prenotazione {

    private String idPrenotazione;
    private String nomeCliente;
    private String cognomeCliente;
    private String usernameCliente;
    private String passwordHash;
    private Proiezione filmProiezione;
    private int numeroPosto;
    private String codiceBiglietto;

    // ------------------------------------------------------------------------
    // COSTRUTTORI
    // ------------------------------------------------------------------------

    /**
     * Costruttore completo utilizzato principalmente da {@code FileManager}
     * durante il caricamento e la ricostruzione dei dati da file CSV.
     *
     * @param idPrenotazione   L'identificativo univoco della prenotazione.
     * @param nomeCliente      Il nome del cliente intestatario.
     * @param cognomeCliente   Il cognome del cliente intestatario.
     * @param usernameCliente  Lo username del cliente intestatario.
     * @param passwordHash     L'hash della password salvato al momento della prenotazione.
     * @param filmProiezione   L'oggetto {@link Proiezione} associato alla prenotazione.
     * @param numeroPosto      Il numero del posto assegnato in sala.
     * @param codiceBiglietto  Il codice identificativo del biglietto (es. QR code).
     */
    public Prenotazione(String idPrenotazione, String nomeCliente, String cognomeCliente, String usernameCliente,
                        String passwordHash, Proiezione filmProiezione, int numeroPosto, String codiceBiglietto) {
        this.idPrenotazione = idPrenotazione;
        this.nomeCliente = nomeCliente;
        this.cognomeCliente = cognomeCliente;
        this.usernameCliente = usernameCliente;
        this.passwordHash = passwordHash;
        this.filmProiezione = filmProiezione;
        setNumeroPosto(numeroPosto);
        this.codiceBiglietto = codiceBiglietto;
    }

    /**
     * Costruttore utilizzato quando un cliente effettua una nuova prenotazione da terminale.
     * Genera automaticamente l'ID di prenotazione, il codice del biglietto univoco
     * e calcola il numero di posto assegnato in base alla disponibilita' rimanente.
     *
     * @param cliente    L'oggetto {@link Utente} che effettua l'acquisto.
     * @param proiezione La {@link Proiezione} da prenotare.
     */
    public Prenotazione(Utente cliente, Proiezione proiezione) {
        this.idPrenotazione = UUID.randomUUID().toString().substring(0, 8);
        this.nomeCliente = cliente.getNome();
        this.cognomeCliente = cliente.getCognome();
        this.usernameCliente = cliente.getUsername();
        this.passwordHash = cliente.getPasswordHash();
        this.filmProiezione = proiezione;
        this.numeroPosto = 200 - proiezione.getPostiDisponibili();
        this.codiceBiglietto = "QR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }

    // ------------------------------------------------------------------------
    // GETTER E SETTER
    // ------------------------------------------------------------------------

    /**
     * Restituisce il nome del cliente intestatario.
     *
     * @return Il nome del cliente.
     */
    public String getNomeCliente() {
        return nomeCliente;
    }

    /**
     * Restituisce il cognome del cliente intestatario.
     *
     * @return Il cognome del cliente.
     */
    public String getCognomeCliente() {
        return cognomeCliente;
    }

    /**
     * Restituisce l'hash della password memorizzato nella prenotazione.
     *
     * @return L'hash della password.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Restituisce lo username del cliente.
     *
     * @return Lo username dell'utente.
     */
    public String getUsernameCliente() {
        return this.usernameCliente;
    }

    /**
     * Imposta lo username del cliente associato alla prenotazione.
     *
     * @param usernameCliente Il nuovo username.
     */
    public void setUsernameCliente(String usernameCliente) {
        this.usernameCliente = usernameCliente;
    }

    /**
     * Restituisce l'ID univoco della prenotazione.
     *
     * @return L'ID della prenotazione.
     */
    public String getIdPrenotazione() {
        return this.idPrenotazione;
    }

    /**
     * Restituisce il codice del biglietto (QR Code).
     *
     * @return Il codice unico del biglietto.
     */
    public String getCodiceBiglietto() {
        return this.codiceBiglietto;
    }

    /**
     * Restituisce la proiezione associata a questa prenotazione.
     *
     * @return La {@link Proiezione} prenotata.
     */
    public Proiezione getFilmProiezione() {
        return this.filmProiezione;
    }

    /**
     * Associa una nuova proiezione a questa prenotazione.
     *
     * @param filmProiezione La nuova {@link Proiezione} da assegnare.
     */
    public void setFilmProiezione(Proiezione filmProiezione) {
        this.filmProiezione = filmProiezione;
    }

    /**
     * Restituisce il numero del posto assegnato.
     *
     * @return Il numero di posto (da 0 a 199).
     */
    public int getNumeroPosto() {
        return numeroPosto;
    }

    /**
     * Imposta il numero del posto assegnato con controllo di validita' (da 0 a 199).
     * Se il valore inserito non e' valido, imposta il posto predefinito a 0.
     *
     * @param numeroPosto Il numero di posto da assegnare.
     */
    public void setNumeroPosto(int numeroPosto) {
        if (numeroPosto >= 0 && numeroPosto < 200) {
            this.numeroPosto = numeroPosto;
        } else {
            System.out.println("Errore: Numero posto non valido (deve essere tra 0 e 199). Legato posto 0 di default.");
            this.numeroPosto = 0;
        }
    }

    // ------------------------------------------------------------------------
    // METODI DELEGATI E DI UTILITÀ
    // ------------------------------------------------------------------------

    /**
     * Recupera il titolo del film associato alla proiezione corrente.
     *
     * @return Il titolo del film, oppure {@code "Film Non Disponibile"} se non presente.
     */
    public String getTitoloFilm() {
        if (filmProiezione != null && filmProiezione.getFilm() != null) {
            return filmProiezione.getFilm().getTitolo();
        }
        return "Film Non Disponibile";
    }

    /**
     * Recupera la data di proiezione in formato stringa.
     *
     * @return La data della proiezione, oppure {@code "N/D"} se non disponibile.
     */
    public String getDataStr() {
        if (filmProiezione != null && filmProiezione.getDataProiezione() != null) {
            return filmProiezione.getDataProiezione();
        }
        return "N/D";
    }

    /**
     * Recupera l'orario di proiezione in formato stringa.
     *
     * @return L'ora della proiezione, oppure {@code "N/D"} se non disponibile.
     */
    public String getOraStr() {
        if (filmProiezione != null && filmProiezione.getOraProiezione() != null) {
            return filmProiezione.getOraProiezione();
        }
        return "N/D";
    }

    /**
     * Restituisce una rappresentazione formattata in formato testuale del biglietto/prenotazione.
     *
     * @return Una stringa contenente il riepilogo dettagliato della prenotazione.
     */
    @Override
    public String toString() {
        double prezzo = (filmProiezione != null) ? filmProiezione.getPrezzoBiglietto() : 0.0;

        return " BIGLIETTO CINEMAX \n" +
                "▪️ ID Prenotazione: " + idPrenotazione + "\n" +
                "▪️ Codice QR:        " + codiceBiglietto + "\n" +
                "▪️ Cliente:          " + nomeCliente + " " + cognomeCliente + " (@" + usernameCliente + ")\n" +
                "▪️ Film:             " + getTitoloFilm() + "\n" +
                "▪️ Data e Ora:       " + getDataStr() + " ore " + getOraStr() + "\n" +
                "▪️ Biglietto N.:     " + numeroPosto + "\n" +
                "▪️ Prezzo:           " + String.format("%.2f", prezzo) + " €\n" +
                "---------------------------------------------";
    }
}