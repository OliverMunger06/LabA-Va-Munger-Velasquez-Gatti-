package cinemax.Users;

import cinemax.gestione.Proiezione;
import cinemax.gestione.Film;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe astratta base che rappresenta un generico utente del sistema Cinemax.
 * <p>
 * Fornisce gli attributi anagrafici fondamentali, i metodi di accesso ai dati (getter),
 * la firma per il menu specifico dell'utente, la gestione del logout polimorfico
 * e i metodi statici condivisi per la ricerca e la visualizzazione delle proiezioni.
 * </p>
 *
 * @author Oliver Munger , matricola num. 764208 , VA
 * @author Davide Gatti , matricola num. 765949 , VA
 * @author Davide Noe Velasquez Carpio , matricola num. 765163 , VA
 */
public abstract class Utente {
    private String username;
    private String passwordHash;
    private String nome;
    private String cognome;
    private String dataNascita;
    private String luogoDomicilio;

    /**
     * Formattatore standard per le date nel formato italiano (dd/MM/yyyy).
     */
    public static final java.time.format.DateTimeFormatter FMT_ITA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Costruisce un nuovo oggetto {@code Utente} con le informazioni anagrafiche e le credenziali.
     *
     * @param nome           il nome dell'utente
     * @param cognome        il cognome dell'utente
     * @param username       l'username unico dell'utente
     * @param passwordHash   l'hash della password già cifrata
     * @param dataNascita    la data di nascita nel formato gg/mm/aaaa (o "N/D" se non specificata)
     * @param luogoDomicilio il luogo di domicilio dell'utente
     */
    public Utente(String nome, String cognome, String username, String passwordHash,
                  String dataNascita, String luogoDomicilio) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.passwordHash = passwordHash;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    /**
     * Mostra a schermo il menu principale specifico per il ruolo dell'utente corrente.
     */
    public abstract void mostraMenu();

    /**
     * Gestisce l'interazione da riga di comando ed esegue l'operazione associata
     * all'opzione selezionata dal menu.
     *
     * @param scelta L'opzione numerica selezionata dal menu.
     */
    public abstract void eseguiAzione(int scelta);

    /**
     * Restituisce l'opzione numerica associata al Logout nel menu dell'utente.
     *
     * @return L'intero corrispondente all'azione di logout (es. 3 per Bigliettaio, 4 per Proiezionista, 6 per Cliente).
     */
    public abstract int getOpzioneLogout();

    /**
     * Restituisce l'username dell'utente.
     *
     * @return una stringa contenente l'username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce il nome dell'utente.
     *
     * @return una stringa contenente il nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce il cognome dell'utente.
     *
     * @return una stringa contenente il cognome
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Restituisce il luogo di domicilio dell'utente.
     *
     * @return una stringa contenente il domicilio
     */
    public String getLuogoDomicilio() {
        return luogoDomicilio;
    }

    /**
     * Restituisce la data di nascita dell'utente.
     *
     * @return una stringa contenente la data di nascita (nel formato gg/mm/aaaa o "N/D")
     */
    public String getDataNascita() {
        return dataNascita;
    }

    /**
     * Restituisce l'hash crittografico della password dell'utente.
     *
     * @return una stringa contenente l'hash della password
     */
    public String getPasswordHash() {
        return passwordHash;
    }


    /**
     * Filtra la lista del palinsesto in base a molteplici criteri di ricerca.
     *
     * @     * @param palinsesto la lista completa delle proiezioni disponibili
     * @param titolo     il titolo (anche parziale) del film da cercare (può essere vuoto o null)
     * @param genere     il genere del film da cercare (può essere vuoto o null)
     * @param dataInizio la data di inizio dell'intervallo di ricerca (può essere null)
     * @param dataFine   la data di fine dell'intervallo di ricerca (può essere null)
     * @param prezzoMin  il prezzo minimo del biglietto (può essere null)
     * @param prezzoMax  il prezzo massimo del biglietto (può essere null)
     * @return una lista di {@link Proiezione} che soddisfano tutti i criteri di ricerca
     */
    public static  List<Proiezione> cercaProiezione(List<Proiezione> palinsesto,
                                                   String titolo, String genere,
                                                   LocalDate dataInizio, LocalDate dataFine,
                                                   Double prezzoMin, Double prezzoMax) {
        List<Proiezione> risultati = new ArrayList<>();

        if (palinsesto == null) {
            return risultati;
        }

        for (Proiezione p : palinsesto) {
            Film f = p.getFilm();


            if (titolo != null && !titolo.trim().isEmpty()) {
                if (f == null || f.getTitolo() == null || !f.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                    continue;
                }
            }


            if (genere != null && !genere.trim().isEmpty()) {
                if (f == null || f.getGenere() == null || !f.getGenere().equalsIgnoreCase(genere)) {
                    continue;
                }
            }


            Date dateObj = p.getDataProiezione();
            if (dateObj != null) {
                LocalDate dataP = dateObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                if (dataInizio != null && dataP.isBefore(dataInizio)) {
                    continue;
                }
                if (dataFine != null && dataP.isAfter(dataFine)) {
                    continue;
                }
            } else {
                if (dataInizio != null || dataFine != null) {
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

    /**
     * Stampa a consolle una scheda formattata con la descrizione completa
     * di una proiezione e del film associato.
     */
    public static void visualizzaProiezione(Proiezione p) {
        Film f = p.getFilm();

        String dataStr = "N/D";
        if (p.getDataProiezione() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            dataStr = sdf.format(p.getDataProiezione());
        }

        LocalTime oraObj = p.getOraProiezione();
        String oraStr = oraObj != null ? oraObj.toString() : "N/D";

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
        System.out.println("  • Data:      " + dataStr);
        System.out.println("  • Ora:       " + oraStr);
        System.out.println("---------------------------------------------");
        System.out.println("INFO BIGLIETTI & SALA:");
        System.out.println("  • Costo:     " + String.format("%.2f€", p.getPrezzoBiglietto()));
        System.out.println("  • Posti Liberi: " + p.getPostiDisponibili() + " / 200");
        System.out.println("=============================================\n");
    }
}