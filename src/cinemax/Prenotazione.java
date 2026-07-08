package cinemax;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Prenotazione {
    private String usernameCliente;
    private Proiezione filmProiezione;
    private String idPrenotazione;

    private static final String FORMATO_DATA_ORA = ("yyyy-MM-dd HH:mm");

    /**
     * COSTRUTTORE 1: Usato per il caricamento da FILE (FileManager).
     * Riceve già tutti i campi compilati e l'oggetto Proiezione reale.
     */
    public Prenotazione(String idPrenotazione, String usernameCliente, Proiezione filmProiezione, String codiceBiglietto, int numeroPosto) {
        this.idPrenotazione = idPrenotazione;
        this.usernameCliente = usernameCliente;
        this.filmProiezione = filmProiezione;
    }

    /**
     * COSTRUTTORE 2: Usato quando un CLIENTE effettua una NUOVA prenotazione da terminale.
     * Genera automaticamente gli ID e i codici QR.
     */
    public Prenotazione(String usernameCliente, Proiezione proiezione) {
        this.usernameCliente = usernameCliente;
        this.filmProiezione = proiezione;
        this.idPrenotazione = UUID.randomUUID().toString().substring(0, 8);
    }

    // ========================================================
    // GETTER E SETTER (Puliti e Coerenti)
    // ========================================================

    public String getUsernameCliente() {
        return this.usernameCliente;
    }

    public void setUsernameCliente(String usernameCliente) {
        this.usernameCliente = usernameCliente;
    }

    public String getIdPrenotazione() {
        return this.idPrenotazione;
    }

    public Proiezione getFilmProiezione() {
        return this.filmProiezione;
    }

    public void setFilmProiezione(Proiezione filmProiezione) {
        this.filmProiezione = filmProiezione;
    }

    // Delegazione: Chiede il titolo direttamente al Film dentro la Proiezione
    public String getTitoloFilm() {
        if (filmProiezione != null && filmProiezione.getFilm() != null) {
            return filmProiezione.getFilm().getTitolo();
        }
        return "Film Non Disponibile";
    }

    // Delegazione: Chiede la data alla proiezione e la formatta a dovere
    public String getDataOraStr() {
        if (filmProiezione != null && filmProiezione.getDataOraProiezione() != null) {
            return filmProiezione.getDataOraProiezione();
        }
        return "N/D";
    }

    // ========================================================
    // METODI DI UTILIÀ
    // ========================================================

    @Override
    public String toString() {
        return "Prenotazione ID: [" + idPrenotazione + "] | Utente: @" + usernameCliente +
                "\n  Film: " + getTitoloFilm() +
                "\n  Data/Ora: " + getDataOraStr();
    }
}