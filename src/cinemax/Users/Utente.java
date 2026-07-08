package cinemax.Users;

import cinemax.FileManager;
import cinemax.Proiezione;
import cinemax.Film;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Utente {
    private String username;
    private static String passwordHash;
    private String nome;
    private String cognome;
    private String dataNascita;
    private String luogoDomicilio;


    public Utente(String username, String passwordInChiaro, String nome, String cognome,
                  String dataNascita, String luogoDomicilio) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    public Utente(String username, String passwordHash, String nome, String cognome,
                  String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {

        this.username = username;
        this.passwordHash = passwordHash; // Viene assegnata direttamente dal file senza ricalcolare l'hash[cite: 1]
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    public abstract void mostraMenu() ;


    public String getUsername() { return username; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getLuogoDomicilio() { return luogoDomicilio; }
    public String getDataNascita() { return dataNascita; }
    public static String getPasswordHash() { return passwordHash; }

    public static List<Proiezione> cercaProiezione(List<Proiezione> palinsesto,
                                                   String titolo, String genere,
                                                   LocalDate dataInizio, LocalDate dataFine,
                                                   Double prezzoMin, Double prezzoMax) {
        List<Proiezione> risultati = new ArrayList<>();

        for (Proiezione p : palinsesto) {
            Film f = p.getFilm();

            // 1. Filtro Titolo (parziale e case-insensitive)
            if (titolo != null && !f.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                continue;
            }

            // 2. Filtro Tipologia / Genere
            if (genere != null && !f.getGenere().equalsIgnoreCase(genere)) {
                continue;
            }

            // 3. Filtro Intervallo di Date ("yyyy-MM-dd HH:mm" -> prendiamo solo la data)
            String dataStringa = p.getDataOraProiezione().split(" ")[0];
            LocalDate dataP = LocalDate.parse(dataStringa);
            if (dataInizio != null && dataP.isBefore(dataInizio)) {
                continue;
            }
            if (dataFine != null && dataP.isAfter(dataFine)) {
                continue;
            }

            // 4. Filtro Costo del Biglietto
            double prezzo = p.getPrezzoBiglietto();
            if (prezzoMin != null && prezzo < prezzoMin) {
                continue;
            }
            if (prezzoMax != null && prezzo > prezzoMax) {
                continue;
            }

            // Se passa tutti i controlli, aggiungiamo la proiezione ai risultati
            risultati.add(p);
        }

        return risultati;
    }

    public static void visualizzaProiezione(Proiezione p) {
        Film f = p.getFilm();

        // Separiamo data e ora per l'output grafico ordinato
        String[] partiDataOra = p.getDataOraProiezione().split(" ");
        String data = partiDataOra[0];
        String ora = partiDataOra[1];

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
        System.out.println("  • Posti Liberi: " + p.getPostiDisponibili());
        System.out.println("=============================================\n");
    }




}
