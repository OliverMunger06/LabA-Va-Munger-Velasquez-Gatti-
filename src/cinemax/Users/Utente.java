package cinemax.Users;

import cinemax.Proiezione;
import cinemax.Film;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Utente {
    private String username;
    // CORRETTO: rimosso 'static' altrimenti tutti gli utenti condividono la stessa password!
    private String passwordHash;
    private String nome;
    private String cognome;
    private String dataNascita;
    private String luogoDomicilio;

    /**
     * COSTRUTTORE 1: Registrazione nuovo utente
     */
    public Utente(String username, String passwordInChiaro, String nome, String cognome,
                  String dataNascita, String luogoDomicilio) {
        this.username = username;
        // CORRETTO: assegna il parametro corretto (se hai un metodo di hashing, usalo qui, es: FileManager.generaHash(passwordInChiaro))
        this.passwordHash = passwordInChiaro;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    /**
     * COSTRUTTORE 2: Caricamento da file CSV
     */
    public Utente(String username, String passwordHash, String nome, String cognome,
                  String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    public abstract void mostraMenu();

    // GETTER (Rimosso static da getPasswordHash)
    public String getUsername() { return username; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getLuogoDomicilio() { return luogoDomicilio; }
    public String getDataNascita() { return dataNascita; }
    public String getPasswordHash() { return passwordHash; }

    // ========================================================================
    // METODI STATICI CONDIVISI (Accessibili da Guest, Cliente, Bigliettaio)
    // ========================================================================

    /**
     * Funzionalità di ricerca filtri combinati (Specifica a)
     */
    public static List<Proiezione> cercaProiezione(List<Proiezione> palinsesto,
                                                   String titolo, String genere,
                                                   LocalDate dataInizio, LocalDate dataFine,
                                                   Double prezzoMin, Double prezzoMax) {
        List<Proiezione> risultati = new ArrayList<>();

        for (Proiezione p : palinsesto) {
            Film f = p.getFilm();

            // 1. Filtro Titolo (parziale, case-insensitive e ignora i vuoti)
            if (titolo != null && !titolo.trim().isEmpty() && !f.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                continue;
            }

            // 2. Filtro Tipologia / Genere
            if (genere != null && !genere.trim().isEmpty() && !f.getGenere().equalsIgnoreCase(genere)) {
                continue;
            }

            // 3. CORRETTO: Filtro date basato sui nuovi metodi ad hoc senza split manuali
            String dataStringa = p.getDataProiezione();
            if (dataStringa != null && !dataStringa.isEmpty()) {
                LocalDate dataP = LocalDate.parse(dataStringa);
                if (dataInizio != null && dataP.isBefore(dataInizio)) {
                    continue;
                }
                if (dataFine != null && dataP.isAfter(dataFine)) {
                    continue;
                }
            }

            // 4. Filtro Costo del Biglietto
            double prezzo = p.getPrezzoBiglietto();
            if (prezzoMin != null && prezzo < prezzoMin) {
                continue;
            }
            if (prezzoMax != null && prezzo > prezzoMax) {
                continue;
            }

            risultati.add(p);
        }

        return risultati;
    }

    /**
     * Funzionalità di visualizzazione dettagliata (Specifica b)
     */
    public static void visualizzaProiezione(Proiezione p) {
        Film f = p.getFilm();
        String data = p.getDataProiezione();
        String ora = p.getOraProiezione();

        System.out.println("\n=============================================");
        System.out.println("          DETTAGLI PROIEZIONE CINEMAX        ");
        System.out.println("=============================================");
        System.out.println("CARATTERISTICHE FILM:");
        System.out.println("  • Titolo:    " + f.getTitolo());
        System.out.println("  • Genere:    " + f.getGenere());
        System.out.println("  • Regista:   " + f.getRegista());
        System.out.println("  • Anno:      " + f.getAnno());
        System.out.println("  • Durata:    " + f.getDurata() + " minuti");
        System.out.println("  • Età Min:   " + (f.getEta_minima() == 0 ? "Tutti" : f.getEta_minima() + "+"));
        System.out.println("---------------------------------------------");
        System.out.println("PROGRAMMAZIONE:");
        System.out.println("  • Data:      " + data);
        System.out.println("  • Ora:       " + ora);
        System.out.println("---------------------------------------------");
        System.out.println("INFO BIGLIETTI & SALA:");
        System.out.println("  • Costo:     " + String.format("%.2f€", p.getPrezzoBiglietto()));
        System.out.println("  • Posti Liberi: " + p.getPostiDisponibili() + " / 200");
        System.out.println("=============================================\n");
    }
}