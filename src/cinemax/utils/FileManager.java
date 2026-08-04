package cinemax.utils;

import cinemax.gestione.Film;
import cinemax.gestione.Prenotazione;
import cinemax.gestione.Proiezione;
import cinemax.Users.Bigliettaio;
import cinemax.Users.Cliente;
import cinemax.Users.Proiezionista;
import cinemax.Users.Utente;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static cinemax.Users.Utente.FMT_ITA;

/**
 * Gestore dell'I/O su file e della sicurezza per il sistema Cinemax.
 * <p>
 * Fornisce metodi statici per la persistenza su file CSV di utenti, proiezioni e prenotazioni,
 * oltre a funzioni di hashing e verifica delle password tramite PBKDF2 e SHA-256.
 * </p>
 *
 * @author Cinemax Team
 * @version 1.0
 */
public class FileManager {

    /** Separatore di percorso specifico del sistema operativo in uso. */
    private static final String SEP = File.separator;

    /** Percorso relativo del file CSV degli utenti. */
    private static final String FILE_UTENTI = "." + SEP + "data" + SEP + "utenti.csv";

    /** Percorso relativo del file CSV del palinsesto proiezioni. */
    private static final String FILE_PALINSESTO = "." + SEP + "data" + SEP + "palinsesto.csv";

    /** Percorso relativo del file CSV delle prenotazioni. */
    private static final String FILE_PRENOTAZIONI = "." + SEP + "data" + SEP + "prenotazioni.csv";

    /** Chiave segreta utilizzata per l'algoritmo di hashing delle password. */
    private static final String CHIAVE_SEGRETA = "c8f391b4a2e5d790f61284a37b9015e14d3f28e6c710a9f5d301b894e2a6c712";

    /** Separatore standard utilizzato per i file CSV. */
    private static final String SEPARATORE = ",";

    /**
     * Costruttore privato per impedire l'istanziamento di una classe di utilita'.
     */
    private FileManager() {}

    // ========================================================
    // LETTURA E SCRITTURA UTENTI (Gestione del Polimorfismo)
    // ========================================================

    /**
     * Salva un oggetto {@link Utente} in coda al file CSV degli utenti.
     * <p>
     * Il formato della riga e': {@code nome,cognome,username,passwordhash,data_di_nascita,luogo_del_domicilio,ruolo}.
     * </p>
     *
     * @param u L'utente da salvare nel file.
     * @throws IOException Se si verifica un errore durante la scrittura su file.
     */
    public static void salvaUtente(Utente u) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_UTENTI),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            String tipo = u.getClass().getSimpleName().toUpperCase();
            String dataNascitaStr = (u.getDataNascita() != null && !u.getDataNascita().trim().isEmpty())
                    ? u.getDataNascita()
                    : "N/D";

            String riga = u.getNome() + SEPARATORE +
                    u.getCognome() + SEPARATORE +
                    u.getUsername() + SEPARATORE +
                    u.getPasswordHash() + SEPARATORE +
                    dataNascitaStr + SEPARATORE +
                    u.getLuogoDomicilio() + SEPARATORE +
                    tipo;

            writer.write(riga);
            writer.newLine();
        }
    }

    /**
     * Cerca e carica un utente dal file CSV in base allo username specificato.
     * Instanzia la sottoclasse concreta corretta ({@link Cliente}, {@link Bigliettaio}, {@link Proiezionista})
     * sfruttando il polimorfismo.
     *
     * @param usernameTarget Lo username dell'utente da ricercare.
     * @return Un {@link Optional} contenente l'oggetto {@link Utente} se trovato, o vuoto altrimenti.
     * @throws IOException Se si verifica un errore durante la lettura del file.
     */
    public static Optional<Utente> caricaUtentePerUsername(String usernameTarget) throws IOException {
        Path path = Paths.get(FILE_UTENTI);
        if (!Files.exists(path)) return Optional.empty();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;
                String[] elementi = riga.split(SEPARATORE);

                // Controllo di sicurezza per righe corrotte
                if (elementi.length < 7) continue;

                String username = elementi[2].trim();

                // Se l'username corrisponde, creiamo l'oggetto e lo restituiamo subito
                if (username.equalsIgnoreCase(usernameTarget.trim())) {
                    String nome        = elementi[0].trim();
                    String cognome     = elementi[1].trim();
                    String passHash    = elementi[3].trim();
                    String dataNascita = elementi[4].trim().equals("N/D") ? null : elementi[4].trim();
                    String domicilio   = elementi[5].trim();
                    String tipo        = elementi[6].trim().toUpperCase();

                    switch (tipo) {
                        case "CLIENTE":
                            return Optional.of(new Cliente(nome, cognome, username, passHash, dataNascita, domicilio));
                        case "BIGLIETTAIO":
                            return Optional.of(new Bigliettaio(nome, cognome, username, passHash, dataNascita, domicilio));
                        case "PROIEZIONISTA":
                            return Optional.of(new Proiezionista(nome, cognome, username, passHash, dataNascita, domicilio));
                        default:
                            System.err.println("Ruolo sconosciuto saltato nel CSV: " + tipo);
                            return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Verifica se un determinato username è già presente all'interno del file CSV degli utenti.
     * <p>
     * La lettura viene effettuata "al volo" riga per riga per massimizzare le prestazioni:
     * se l'username viene trovato, il flusso di lettura si interrompe immediatamente.
     * </p>
     *
     * @param usernameDaCercare l'username di cui verificare la presenza
     * @return {@code true} se l'username è già registrato,
     *         {@code false} se è disponibile o se il file non esiste/non è leggibile
     */
    public static boolean isUsernameEsistenteSuFile(String usernameDaCercare) {
        if (usernameDaCercare == null || usernameDaCercare.trim().isEmpty()) {
            return false;
        }

        Path path = Paths.get(FILE_UTENTI);

        // Se il file non esiste ancora (es. primo avvio dell'app), l'username è libero
        if (!Files.exists(path)) {
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) {
                    continue;
                }

                String[] campi = riga.split(SEPARATORE);
                if (campi.length > 2) {
                    String usernameNelFile = campi[2].trim();

                    // Interrompe la lettura e restituisce true al primo match trovato
                    if (usernameNelFile.equalsIgnoreCase(usernameDaCercare.trim())) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file utenti: " + e.getMessage());
            return false;
        }

        return false; // Scansionato tutto il file senza trovare corrispondenze
    }

    // ========================================================
    // LETTURA E SCRITTURA PALINSESTO (Preservazione ID e Posti)
    // ========================================================

    /**
     * Salva una nuova {@link Proiezione} accodandola nel file CSV del palinsesto.
     *
     * @param p La proiezione da salvare.
     * @return {@code true} se la proiezione è stata salvata con successo, {@code false} in caso di errore I/O.
     */
    public static boolean salvaProiezione(Proiezione p) {
        if (p == null || p.getFilm() == null) {
            return false;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PALINSESTO),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            Film f = p.getFilm();

            String riga = p.getIdProiezione() + SEPARATORE +
                    p.getDataProiezione() + SEPARATORE +
                    p.getOraProiezione() + SEPARATORE +
                    f.getTitolo() + SEPARATORE +
                    f.getGenere() + SEPARATORE +
                    f.getRegista() + SEPARATORE +
                    f.getAnno() + SEPARATORE +
                    f.getDurata() + SEPARATORE +
                    f.getEta_minima() + SEPARATORE +
                    p.getPrezzoBiglietto() + SEPARATORE +
                    p.getPostiDisponibili();

            writer.write(riga);
            writer.newLine();
            return true;

        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio della proiezione su file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carica l'intero palinsesto delle proiezioni dal file CSV.
     * <p>
     * In caso di righe malformate o con errori nel formato numerico, la singola
     * riga viene ignorata per non compromettere il caricamento delle altre proiezioni.
     * </p>
     *
     * @return Una {@link List} contenente tutte le {@link Proiezione} caricate da file.
     *         Restituisce una lista vuota se il file non esiste o non contiene proiezioni valide.
     * @throws IOException Se si verifica un errore durante la lettura del file.
     */
    public static List<Proiezione> caricaPalinsesto() throws IOException {
        List<Proiezione> palinsesto = new ArrayList<>();
        Path path = Paths.get(FILE_PALINSESTO);

        // Se il file non esiste ancora, restituisce una lista vuota
        if (!Files.exists(path)) {
            return palinsesto;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;

                // Usa una regex per dividere la riga ignorando le virgole racchiuse tra virgolette (es. nei titoli)
                String[] elementi = riga.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (elementi.length < 11) continue;

                // Pulisce le virgolette doppie e gli spazi dai campi stringa
                String idProiezione = elementi[0].replace("\"", "").trim();
                String dataStringa  = elementi[1].replace("\"", "").trim();
                String oraStringa   = elementi[2].replace("\"", "").trim();
                String titolo       = elementi[3].replace("\"", "").trim();
                String genere       = elementi[4].replace("\"", "").trim();
                String regista      = elementi[5].replace("\"", "").trim();

                try {
                    int anno         = Integer.parseInt(elementi[6].replace("\"", "").trim());
                    int durata       = Integer.parseInt(elementi[7].replace("\"", "").trim());
                    int etaMinima    = Integer.parseInt(elementi[8].replace("\"", "").trim());
                    double prezzo    = Double.parseDouble(elementi[9].replace("\"", "").trim().replace(",", "."));
                    int postiRimasti = Integer.parseInt(elementi[10].replace("\"", "").trim());

                    // Conversione della stringa (es. "dd/MM/yyyy") in java.util.Date tramite LocalDate
                    LocalDate localDate = LocalDate.parse(dataStringa, FMT_ITA);
                    java.util.Date dataProiezione = java.util.Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                    // Conversione della stringa in LocalTime
                    LocalTime oraProiezione = LocalTime.parse(oraStringa);

                    Film film = new Film(titolo, genere, regista, anno, durata, etaMinima);
                    Proiezione p = new Proiezione(idProiezione, dataProiezione, oraProiezione, prezzo, film, postiRimasti);

                    palinsesto.add(p);

                } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
                    System.err.println("Errore nel formato dei dati della proiezione ID " + idProiezione + ": " + e.getMessage());
                }
            }
        }

        return palinsesto;
    }

    /**
     * Modifica la data e l'ora di una proiezione nel file CSV.
     * <p>
     * La modifica è consentita solo se la sala è completamente vuota (200 posti disponibili).
     * </p>
     *
     * @param titoloFilm  Il titolo del film proiettato.
     * @param vecchiaData La data corrente dello spettacolo (gg/mm/aaaa).
     * @param vecchiaOra  L'orario corrente dello spettacolo (hh:mm).
     * @param nuovaData   La nuova data da assegnare.
     * @param nuovaOra    Il nuovo orario da assegnare.
     * @return {@code true} se la modifica è avvenuta con successo, {@code false} se ci sono prenotazioni o se la proiezione non esiste.
     */
    public static boolean modificaProiezione(String titoloFilm, String vecchiaData, String vecchiaOra,
                                             String nuovaData, String nuovaOra) {
        Path path = Paths.get(FILE_PALINSESTO);
        if (!Files.exists(path)) {
            System.err.println("Errore: Il file del palinsesto non esiste.");
            return false;
        }

        boolean modificato = false;

        try {
            List<String> righe = Files.readAllLines(path);
            List<String> nuoveRighe = new ArrayList<>();

            for (String riga : righe) {
                if (riga.trim().isEmpty()) continue;

                String[] elementi = riga.split(SEPARATORE);
                if (elementi.length >= 11) {
                    String dataCorrente = elementi[1];
                    String oraCorrente = elementi[2];
                    String titoloCorrente = elementi[3];
                    int postiDisponibili = Integer.parseInt(elementi[10]);

                    if (titoloCorrente.equalsIgnoreCase(titoloFilm) &&
                            dataCorrente.equals(vecchiaData) &&
                            oraCorrente.equals(vecchiaOra)) {

                        if (postiDisponibili < 200) {
                            System.out.println("  Errore: Impossibile modificare. Ci sono già delle prenotazioni!");
                            return false;
                        }

                        elementi[1] = nuovaData;
                        elementi[2] = nuovaOra;
                        riga = String.join(SEPARATORE, elementi);
                        modificato = true;
                    }
                }
                nuoveRighe.add(riga);
            }

            if (modificato) {
                Files.write(path, nuoveRighe);
                return true;
            } else {
                System.out.println("  Errore: Nessuna proiezione trovata con i parametri specificati.");
                return false;
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Errore durante la modifica della proiezione: " + e.getMessage());
            return false;
        }
    }

    /**
     * Rimuove una proiezione dal file CSV del palinsesto.
     * <p>
     * L'eliminazione è consentita solo se non vi sono prenotazioni effettuate per tale spettacolo (200 posti disponibili).
     * </p>
     *
     * @param titoloFilm Il titolo del film da eliminare.
     * @param dataStr    La data della proiezione (gg/mm/aaaa).
     * @param oraStr     L'orario della proiezione (hh:mm).
     * @return {@code true} se la proiezione è stata eliminata, {@code false} se vi sono prenotazioni attive o se la proiezione non è stata trovata.
     */
    public static boolean eliminaProiezione(String titoloFilm, String dataStr, String oraStr) {
        Path path = Paths.get(FILE_PALINSESTO);
        if (!Files.exists(path)) {
            System.err.println("Errore: Il file del palinsesto non esiste.");
            return false;
        }

        boolean eliminato = false;

        try {
            List<String> righe = Files.readAllLines(path);
            List<String> nuoveRighe = new ArrayList<>();

            for (String riga : righe) {
                if (riga.trim().isEmpty()) continue;

                String[] elementi = riga.split(SEPARATORE);
                if (elementi.length >= 11) {
                    String dataCorrente = elementi[1];
                    String oraCorrente = elementi[2];
                    String titoloCorrente = elementi[3];
                    int postiDisponibili = Integer.parseInt(elementi[10]);

                    if (titoloCorrente.equalsIgnoreCase(titoloFilm) &&
                            dataCorrente.equals(dataStr) &&
                            oraCorrente.equals(oraStr)) {

                        if (postiDisponibili < 200) {
                            System.out.println("  Errore: Impossibile eliminare. Ci sono già delle prenotazioni!");
                            return false;
                        }

                        eliminato = true;
                        continue;
                    }
                }
                nuoveRighe.add(riga);
            }

            if (eliminato) {
                Files.write(path, nuoveRighe);
                return true;
            } else {
                System.out.println("  Errore: Nessuna proiezione trovata con i parametri specificati.");
                return false;
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Errore durante l'eliminazione della proiezione: " + e.getMessage());
            return false;
        }
    }

    // ========================================================
    // LETTURA E SCRITTURA PRENOTAZIONI
    // ========================================================

    /**
     * Salva una singola prenotazione accodandola in fondo al file CSV.
     * Se il file non esiste, viene creato automaticamente.
     *
     * @param p La prenotazione da persistere su file.
     * @throws IOException Se si verifica un errore durante l'accesso al file.
     */
    public static void salvaPrenotazione(Prenotazione p) throws IOException {
        if (p == null) return;

        Path path = Paths.get(FILE_PRENOTAZIONI);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            String nome = (p.getNomeCliente() != null) ? p.getNomeCliente() : "N/D";
            String cognome = (p.getCognomeCliente() != null) ? p.getCognomeCliente() : "N/D";
            String passHash = (p.getPasswordHash() != null) ? p.getPasswordHash() : "N/D";

            String riga = p.getIdPrenotazione() + SEPARATORE +
                    nome + SEPARATORE +
                    cognome + SEPARATORE +
                    p.getUsernameCliente() + SEPARATORE +
                    passHash + SEPARATORE +
                    p.getFilmProiezione().getIdProiezione() + SEPARATORE +
                    p.getNumeroPosto() + SEPARATORE +
                    p.getCodiceBiglietto();

            writer.write(riga);
            writer.newLine();
        }
    }

    /**
     * Legge dal file CSV tutte le prenotazioni registrate e le ricostruisce
     * collegando ciascuna alla rispettiva proiezione presente nel palinsesto.
     *
     * @param palinsesto La lista delle proiezioni disponibili usata per associare la proiezione corretta.
     * @return Una {@link List} contenente tutte le prenotazioni caricate da file.
     * @throws IOException Se si verifica un errore durante la lettura del file.
     */
    public static List<Prenotazione> caricaPrenotazioni(List<Proiezione> palinsesto) throws IOException {
        List<Prenotazione> listaPrenotazioni = new ArrayList<>();
        Path path = Paths.get(FILE_PRENOTAZIONI);

        if (!Files.exists(path) || palinsesto == null || palinsesto.isEmpty()) {
            return listaPrenotazioni;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;

                String[] elementi = riga.split(SEPARATORE);
                if (elementi.length < 8) continue;

                String idLetto         = elementi[0].trim();
                String nomeCliente     = elementi[1].trim();
                String cognomeCliente  = elementi[2].trim();
                String usernameCliente = elementi[3].trim();
                String passwordHash    = elementi[4].trim();
                String idProiezione    = elementi[5].trim();

                int numeroPosto;
                try {
                    numeroPosto = Integer.parseInt(elementi[6].trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                String codiceBiglietto = elementi[7].trim();

                Proiezione proiezioneTrovata = null;
                for (Proiezione proj : palinsesto) {
                    if (proj.getIdProiezione().equalsIgnoreCase(idProiezione)) {
                        proiezioneTrovata = proj;
                        break;
                    }
                }

                if (proiezioneTrovata != null) {
                    Prenotazione pren = new Prenotazione(
                            idLetto,
                            nomeCliente,
                            cognomeCliente,
                            usernameCliente,
                            passwordHash,
                            proiezioneTrovata,
                            numeroPosto,
                            codiceBiglietto
                    );
                    listaPrenotazioni.add(pren);
                }
            }
        }

        return listaPrenotazioni;
    }

    /**
     * Modifica l'ID della proiezione associata a una specifica prenotazione di un utente.
     *
     * @param idPrenotazione    L'ID unico della prenotazione da modificare.
     * @param usernameUtente    Lo username dell'utente proprietario della prenotazione.
     * @param idNuovaProiezione L'ID della nuova proiezione da associare.
     * @return {@code true} se la modifica e' andata a buon fine, {@code false} altrimenti.
     */
    public static boolean modificaProiezioneInPrenotazione(String idPrenotazione, String usernameUtente, String idNuovaProiezione) {
        File fileOriginale = new File(FILE_PRENOTAZIONI);
        if (!fileOriginale.exists()) {
            return false;
        }

        File fileTemporaneo = new File(fileOriginale.getParent(), "prenotazioni_temp.csv");
        boolean trovato = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileOriginale));
             BufferedWriter writer = new BufferedWriter(new FileWriter(fileTemporaneo))) {

            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) {
                    continue;
                }

                // Usa la stessa regex per evitare problemi con eventuali virgole e pulisci le virgolette
                String[] elementi = riga.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (elementi.length >= 8) {
                    // Rimuoviamo le virgolette per un confronto sicuro
                    String id = elementi[0].replace("\"", "").trim();
                    String user = elementi[3].replace("\"", "").trim();

                    if (id.equalsIgnoreCase(idPrenotazione.trim()) && user.equalsIgnoreCase(usernameUtente.trim())) {
                        // Sostituisce l'ID proiezione mantenendo le virgolette se lo desideri, oppure pulito
                        elementi[5] = "\"" + idNuovaProiezione.replace("\"", "").trim() + "\"";
                        riga = String.join(SEPARATORE, elementi);
                        trovato = true;
                    }
                }

                writer.write(riga);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Errore di I/O durante la modifica della prenotazione: " + e.getMessage());
            if (fileTemporaneo.exists()) {
                fileTemporaneo.delete();
            }
            return false;
        }

        if (trovato) {
            if (!fileOriginale.delete()) {
                System.out.println("Impossibile eliminare il file originale delle prenotazioni.");
                return false;
            }
            if (!fileTemporaneo.renameTo(fileOriginale)) {
                System.out.println("Impossibile rinominare il file temporaneo.");
                return false;
            }
            return true;
        } else {
            fileTemporaneo.delete();
            return false;
        }
    }

    /**
     * Legge il file delle prenotazioni e stampa a schermo tutte le prenotazioni
     * attive appartenenti allo username dell'utente specificato.
     *
     * @param usernameUtente Lo username dell'utente di cui mostrare le prenotazioni.
     */
    public static void stampaPrenotazioniUtente(String usernameUtente) {
        File filePrenotazioni = new File(FILE_PRENOTAZIONI);

        if (!filePrenotazioni.exists()) {
            System.out.println("  Nessun archivio prenotazioni trovato.");
            return;
        }

        boolean trovataAlmenoUna = false;
        int contatore = 0;

        System.out.println("\n------------------------------------------------");
        System.out.println("     LE TUE PRENOTAZIONI ATTIVE (@" + usernameUtente + ")");
        System.out.println("------------------------------------------------");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePrenotazioni))) {
            String riga;

            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) {
                    continue;
                }

                String[] elementi = riga.split(SEPARATORE);

                if (elementi.length >= 8) {
                    String idPrenotazione = elementi[0].trim();
                    String nome = elementi[1].trim();
                    String cognome = elementi[2].trim();
                    String username = elementi[3].trim();
                    String idProiezione = elementi[5].trim();
                    String numeroPosto = elementi[6].trim();
                    String codiceBiglietto = elementi[7].trim();

                    if (username.equalsIgnoreCase(usernameUtente)) {
                        contatore++;
                        trovataAlmenoUna = true;

                        System.out.println("\n [" + contatore + "] BIGLIETTO CINEMAX");
                        System.out.println("  ▪️ ID Prenotazione : " + idPrenotazione);
                        System.out.println("  ▪️ Codice QR       : " + codiceBiglietto);
                        System.out.println("  ▪️ Intestatario    : " + nome + " " + cognome);
                        System.out.println("  ▪️ ID Proiezione   : " + idProiezione);
                        System.out.println("  ▪️ Posto N.        : " + numeroPosto);
                        System.out.println(" ----------------------------------------------");
                    }
                } else if (elementi.length >= 4) {
                    String idPrenotazione = elementi[0].trim();
                    String username = elementi[1].trim();
                    String idProiezione = elementi[2].trim();
                    String codiceBiglietto = elementi[3].trim();

                    if (username.equalsIgnoreCase(usernameUtente)) {
                        contatore++;
                        trovataAlmenoUna = true;

                        System.out.println("\n [" + contatore + "] BIGLIETTO CINEMAX");
                        System.out.println("  ▪️ ID Prenotazione : " + idPrenotazione);
                        System.out.println("  ▪️ Codice QR       : " + codiceBiglietto);
                        System.out.println("  ▪️ ID Proiezione   : " + idProiezione);
                        System.out.println(" ----------------------------------------------");
                    }
                }
            }

            if (!trovataAlmenoUna) {
                System.out.println("  Non hai ancora effettuato alcuna prenotazione.");
            }

            System.out.println("\n------------------------------------------------\n");

        } catch (IOException e) {
            System.out.println("  Errore durante la lettura delle prenotazioni: " + e.getMessage());
        }
    }

    /**
     * Rimuove una prenotazione dal file CSV in base al suo ID e allo username del cliente.
     *
     * @param idPrenotazione L'ID unico della prenotazione da rimuovere.
     * @param usernameUtente Lo username dell'utente richiedente.
     * @return {@code true} se la prenotazione e' stata trovata ed eliminata con successo, {@code false} altrimenti.
     */
    public static boolean rimuoviPrenotazioneDaFile(String idPrenotazione, String usernameUtente) {
        File fileOriginale = new File(FILE_PRENOTAZIONI);

        if (!fileOriginale.exists()) {
            System.out.println("  Archivio prenotazioni non trovato.");
            return false;
        }

        File fileTemporaneo = new File(fileOriginale.getParent(), "prenotazioni_temp.csv");
        boolean eliminato = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileOriginale));
             BufferedWriter writer = new BufferedWriter(new FileWriter(fileTemporaneo))) {

            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) {
                    continue;
                }

                String[] elementi = riga.split(SEPARATORE);

                if (elementi.length >= 8) {
                    String id = elementi[0].trim();
                    String username = elementi[3].trim();

                    if (id.equalsIgnoreCase(idPrenotazione) && username.equalsIgnoreCase(usernameUtente)) {
                        eliminato = true;
                        continue;
                    }
                } else if (elementi.length >= 4) {
                    String id = elementi[0].trim();
                    String username = elementi[1].trim();

                    if (id.equalsIgnoreCase(idPrenotazione) && username.equalsIgnoreCase(usernameUtente)) {
                        eliminato = true;
                        continue;
                    }
                }

                writer.write(riga);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("  Errore durante la cancellazione della prenotazione: " + e.getMessage());
            if (fileTemporaneo.exists()) {
                fileTemporaneo.delete();
            }
            return false;
        }

        if (eliminato) {
            if (!fileOriginale.delete()) {
                System.out.println("  Impossibile aggiornare l'archivio prenotazioni originale.");
                return false;
            }
            if (!fileTemporaneo.renameTo(fileOriginale)) {
                System.out.println("  Impossibile finalizzare la cancellazione.");
                return false;
            }
            return true;
        } else {
            fileTemporaneo.delete();
            return false;
        }
    }

    /**
     * Aggiorna il numero di posti disponibili per una specifica proiezione nel file del palinsesto.
     *
     * @param idProiezione     L'ID della proiezione da aggiornare.
     * @param nuoviDisponibili Il nuovo numero di posti disponibili.
     */
    public static void aggiornaPostiProiezioneSuFile(String idProiezione, int nuoviDisponibili) {
        File filePalinsesto = new File(FILE_PALINSESTO);
        if (!filePalinsesto.exists()) return;

        File fileTemp = new File(filePalinsesto.getParent(), "palinsesto_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePalinsesto));
             BufferedWriter writer = new BufferedWriter(new FileWriter(fileTemp))) {

            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;

                String[] elementi = riga.split(SEPARATORE);
                if (elementi.length >= 11) {
                    String id = elementi[0].trim();
                    if (id.equalsIgnoreCase(idProiezione.trim())) {
                        elementi[10] = String.valueOf(nuoviDisponibili);
                        riga = String.join(SEPARATORE, elementi);
                    }
                }
                writer.write(riga);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'aggiornamento dei posti su file: " + e.getMessage());
            if (fileTemp.exists()) fileTemp.delete();
            return;
        }

        if (filePalinsesto.delete()) {
            fileTemp.renameTo(filePalinsesto);
        }
    }

    // ========================================================
    // SICUREZZA E HASHING (PBKDF2)
    // ========================================================

    /**
     * Esegue l'hashing di una password in chiaro utilizzando l'algoritmo PBKDF2WithHmacSHA256.
     *
     * @param passwordInChiaro La password non cifrata inserita dall'utente.
     * @return Una stringa formattata contenente il salt e l'hash risultanti, codificati in Base64.
     */
    public static String generaPasswordHash(String passwordInChiaro) {
        try {
            byte[] salt = CHIAVE_SEGRETA.getBytes(StandardCharsets.UTF_8);
            KeySpec spec = new PBEKeySpec(passwordInChiaro.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Errore critico durante l'hashing della password", e);
        }
    }

    /**
     * Verifica la corrispondenza tra una password in chiaro e un hash memorizzato.
     *
     * @param passwordInChiaro La password inserita dall'utente in fase di login.
     * @param passwordHash     L'hash salvato nel file di persistenza.
     * @return {@code true} se la password corrisponde, {@code false} altrimenti.
     */
    public static boolean verificaPassword(String passwordInChiaro, String passwordHash) {
        String hashTentativo = generaPasswordHash(passwordInChiaro);
        return hashTentativo.equals(passwordHash);
    }
}