package cinemax.Users;

import cinemax.gestione.Proiezione;
import cinemax.gestione.Film;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Utente {
    private String username;
    private String passwordHash;
    private String nome;
    private String cognome;
    private String dataNascita;
    private String luogoDomicilio;

    protected static final java.time.format.DateTimeFormatter FMT_ITA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * COSTRUTTORE 1: Registrazione nuovo utente (ALLINEATO AL FILE CSV)
     */
    public Utente(String nome, String cognome, String username, String passwordInChiaro,
                  String dataNascita, String luogoDomicilio) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.passwordHash = passwordInChiaro;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    /**
     * COSTRUTTORE 2: Caricamento da file CSV (ALLINEATO AL FILE CSV)
     */
    public Utente(String nome, String cognome, String username, String passwordHash,
                  String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.passwordHash = passwordHash;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    public abstract void mostraMenu();

    // GETTER
    public String getUsername() { return username; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getLuogoDomicilio() { return luogoDomicilio; }
    public String getDataNascita() { return dataNascita; }
    public String getPasswordHash() { return passwordHash; }

    // ========================================================================
    // METODI STATICI CONDIVISI
    // ========================================================================

    public static List<Proiezione> cercaProiezione(List<Proiezione> palinsesto,
                                                   String titolo, String genere,
                                                   LocalDate dataInizio, LocalDate dataFine,
                                                   Double prezzoMin, Double prezzoMax) {
        List<Proiezione> risultati = new ArrayList<>();

        for (Proiezione p : palinsesto) {
            Film f = p.getFilm();

            if (titolo != null && !titolo.trim().isEmpty() && !f.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                continue;
            }

            if (genere != null && !genere.trim().isEmpty() && !f.getGenere().equalsIgnoreCase(genere)) {
                continue;
            }

            String dataStringa = p.getDataProiezione();
            if (dataStringa != null && !dataStringa.isEmpty()) {
                try {
                    // Creiamo il formattatore italiano coerente con il CSV
                    LocalDate dataP = LocalDate.parse(dataStringa, FMT_ITA);

                    if (dataInizio != null && dataP.isBefore(dataInizio)) {
                        continue;
                    }
                    if (dataFine != null && dataP.isAfter(dataFine)) {
                        continue;
                    }
                } catch (java.time.format.DateTimeParseException e) {
                    // Protezione contro eventuali stringhe corrotte nel palinsesto ("N/D")
                    continue;
                }
            }

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