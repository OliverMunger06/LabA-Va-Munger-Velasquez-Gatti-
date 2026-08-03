package cinemax.Users;

import cinemax.gestione.Proiezione;
import cinemax.gestione.Film;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta la classe astratta base per tutti gli utenti del sistema Cinemax.
 * <p>
 * Definisce i campi anagrafici e di autenticazione comuni (nome, cognome, username, password, ecc.),
 * forzando l'implementazione del menu utente specifico tramite il metodo astratto {@link #mostraMenu()}.
 * Contiene inoltre costanti per la formattazione delle date e metodi statici di utilita' condivisi
 * per la ricerca e la visualizzazione delle proiezioni.
 * </p>
 *
 * @author Cinemax Team
 * @version 1.0
 */
public abstract class Utente {

    /** Lo username identificativo dell'utente per il login. */
    private String username;

    /** L'hash della password di sicurezza dell'utente. */
    private String passwordHash;

    /** Il nome dell'utente. */
    private String nome;

    /** Il cognome dell'utente. */
    private String cognome;

    /** La data di nascita dell'utente in formato testo. */
    private String dataNascita;

    /** Il luogo di domicilio dell'utente. */
    private String luogoDomicilio;

    /**
     * Formattatore standard per la gestione e il parsing delle date in formato italiano (GG/MM/AAAA).
     */
    protected static final java.time.format.DateTimeFormatter FMT_ITA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ------------------------------------------------------------------------
    // COSTRUTTORI
    // ------------------------------------------------------------------------

    /**
     * COSTRUTTORE 1: Usato per la registrazione di un nuovo utente nel sistema.
     *
     * @param nome             Il nome dell'utente.
     * @param cognome          Il cognome dell'utente.
     * @param username         Lo username scelto per l'accesso.
     * @param passwordInChiaro La password iniziale fornita dall'utente.
     * @param dataNascita      La data di nascita in formato testo.
     * @param luogoDomicilio   Il comune/luogo di domicilio.
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
     * COSTRUTTORE 2: Usato dal {@code FileManager} per ricostruire un utente esistente dal file CSV.
     *
     * @param nome            Il nome dell'utente.
     * @param cognome         Il cognome dell'utente.
     * @param username        Lo username univoco dell'utente.
     * @param passwordHash    L'hash della password gia' cifrata.
     * @param dataNascita     La data di nascita letta da file.
     * @param luogoDomicilio  Il luogo di domicilio letto da file.
     * @param isAlreadyHashed Flag di controllo per verificare se la password e' gia' sottoposta ad hashing.
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

    // ------------------------------------------------------------------------
    // METODI ASTRATTI
    // ------------------------------------------------------------------------

    /**
     * Mostra l'interfaccia o il menu di opzioni specifico relativo al ruolo dell'utente
     * (es. Cliente, Bigliettaio, Proiezionista, Amministratore).
     */
    public abstract void mostraMenu();

    // ------------------------------------------------------------------------
    // GETTER
    // ------------------------------------------------------------------------

    /**
     * Restituisce lo username dell'utente.
     *
     * @return Lo username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce il nome dell'utente.
     *
     * @return Il nome.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce il cognome dell'utente.
     *
     * @return Il cognome.
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Restituisce il luogo di domicilio dell'utente.
     *
     * @return Il luogo di domicilio.
     */
    public String getLuogoDomicilio() {
        return luogoDomicilio;
    }

    /**
     * Restituisce la data di nascita dell'utente.
     *
     * @return La data di nascita in formato testo.
     */
    public String getDataNascita() {
        return dataNascita;
    }

    /**
     * Restituisce l'hash della password dell'utente.
     *
     * @return La password cifrata/hash.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    // ========================================================================
    // METODI STATICI CONDIVISI
    // ========================================================================

    /**
     * Cerca e filtra le proiezioni presenti nel palinsesto secondo parametri di ricerca opzionali.
     * <p>
     * E' possibile filtrare per titolo (anche parziale), genere, intervallo di date
     * e fascia di prezzo del biglietto. I parametri impostati a {@code null} o vuoti vengono ignorati.
     * </p>
     *
     * @param palinsesto La lista completa di proiezioni disponibili.
     * @param titolo     Il titolo (o parte del titolo) del film da cercare.
     * @param genere     Il genere del film.
     * @param dataInizio La data di partenza dell'intervallo di ricerca (inclusa).
     * @param dataFine   La data limite dell'intervallo di ricerca (inclusa).
     * @param prezzoMin  Il costo minimo del biglietto.
     * @param prezzoMax  Il costo massimo del biglietto.
     * @return Una lista di oggetti {@link Proiezione} che soddisfano tutti i criteri specificati.
     */
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

    /**
     * Stampa a schermo i dettagli completi ed elegantemente formattati di una specifica {@link Proiezione},
     * includendo dati sul film, la programmazione e lo stato della sala.
     *
     * @param p La proiezione di cui mostrare i dettagli.
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