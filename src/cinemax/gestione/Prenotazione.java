package cinemax.gestione;

import cinemax.Users.Utente;

import java.util.UUID;

public class Prenotazione {
    private String idPrenotazione;
    private String nomeCliente;   // oliver
    private String cognomeCliente;// Rossi (AGGIUNTO)
    private String usernameCliente;// olly06
    private String passwordHash;  // hnxuBQYpvUZ...
    private Proiezione filmProiezione; // Gestito tramite ID P-C6014BBC
    private int numeroPosto;      // 0
    private String codiceBiglietto;// QR-7ECE8


    /**
     * COSTRUTTORE 1: Usato dal FileManager per il caricamento da FILE CSV (Ora a 8 parametri)
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
     * COSTRUTTORE 2: Usato quando un CLIENTE effettua una NUOVA prenotazione da terminale
     */
    public Prenotazione(Utente cliente, Proiezione proiezione) {
        this.idPrenotazione = UUID.randomUUID().toString().substring(0, 8);
        this.nomeCliente = cliente.getNome();
        this.cognomeCliente = cliente.getCognome();
        this.usernameCliente = cliente.getUsername();
        this.passwordHash = cliente.getPasswordHash(); // Mantiene la persistenza dei dati utente nella prenotazione
        this.filmProiezione = proiezione;
        this.numeroPosto = 200 - proiezione.getPostiDisponibili();
        this.codiceBiglietto = "QR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }

    // ========================================================
    // GETTER E SETTER (Puliti e Coerenti)
    // ========================================================

    public String getNomeCliente() { return nomeCliente; }

    public String getCognomeCliente() { return cognomeCliente; }

    public String getPasswordHash() { return passwordHash; }

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

    // Delegazione: Chiede la DATA alla proiezione
    public String getDataStr() {
        if (filmProiezione != null && filmProiezione.getDataProiezione() != null) {
            return filmProiezione.getDataProiezione();
        }
        return "N/D";
    }

    // Delegazione: Chiede l'ORA alla proiezione
    public String getOraStr() {
        if (filmProiezione != null && filmProiezione.getOraProiezione() != null) {
            return filmProiezione.getOraProiezione();
        }
        return "N/D";
    }

    // ========================================================
    // METODI DI UTILIÀ
    // ========================================================

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