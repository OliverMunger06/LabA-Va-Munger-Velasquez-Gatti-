package cinemax;

import java.util.UUID;

public class Prenotazione {
    private String usernameCliente;
    private Proiezione filmProiezione;
    private String idPrenotazione;
    private String codiceBiglietto;
    private int numeroPosto;

    /**
     * COSTRUTTORE 1: Usato per il caricamento da FILE (FileManager).
     * Riceve già tutti i campi compilati e l'oggetto Proiezione reale.
     */
    public Prenotazione(String idPrenotazione, String usernameCliente, Proiezione filmProiezione, String codiceBiglietto, int numeroPosto) {
        this.idPrenotazione = idPrenotazione;
        this.usernameCliente = usernameCliente;
        this.filmProiezione = filmProiezione;
        this.codiceBiglietto = codiceBiglietto;
        setNumeroPosto(numeroPosto); // Sfrutta il controllo di validità del setter
    }

    /**
     * COSTRUTTORE 2: Usato quando un CLIENTE effettua una NUOVA prenotazione da terminale.
     * Genera automaticamente gli ID e i codici QR.
     */
    public Prenotazione(String usernameCliente, Proiezione proiezione) {
        this.usernameCliente = usernameCliente;
        this.filmProiezione = proiezione;
        this.idPrenotazione = UUID.randomUUID().toString().substring(0, 8);
        this.codiceBiglietto = "QR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        this.numeroPosto = 0; // Posto di default (o gestito successivamente)
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

    public String getCodiceBiglietto() {
        return this.codiceBiglietto;
    }

    public Proiezione getFilmProiezione() {
        return this.filmProiezione;
    }

    public void setFilmProiezione(Proiezione filmProiezione) {
        this.filmProiezione = filmProiezione;
    }

    public int getNumeroPosto() {
        return numeroPosto;
    }

    public void setNumeroPosto(int numeroPosto) {
        if (numeroPosto >= 0 && numeroPosto < 200) {
            this.numeroPosto = numeroPosto;
        } else {
            System.out.println("Errore: Numero posto non valido (deve essere tra 0 e 199). Legato posto 0 di default.");
            this.numeroPosto = 0;
        }
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
                "\n  Data/Ora: " + getDataOraStr() +
                "\n  Posto Numero: " + (numeroPosto + 1) +
                "\n  Codice QR: " + codiceBiglietto;
    }
}