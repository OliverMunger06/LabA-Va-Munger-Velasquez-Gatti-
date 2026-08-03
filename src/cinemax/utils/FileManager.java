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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

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

    /** Chiave segreta/Salt utilizzata per l'algoritmo di hashing delle password. */
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
                            return Optional.of(new Cliente(nome, cognome, username, passHash, dataNascita, domicilio, true));
                        case "BIGLIETTAIO":
                            return Optional.of(new Bigliettaio(nome, cognome, username, passHash, dataNascita, domicilio, true));
                        case "PROIEZIONISTA":
                            return Optional.of(new Proiezionista(nome, cognome, username, passHash, dataNascita, domicilio, true));
                        default:
                            System.err.println("Ruolo sconosciuto saltato nel CSV: " + tipo);
                            return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }

    // ========================================================
    // LETTURA E SCRITTURA PALINSESTO (Preservazione ID e Posti)
    // ========================================================

    /**
     * Salva una nuova {@link Proiezione} accodandola nel file CSV del palinsesto.
     *
     * @param p La proiezione da salvare.
     * @throws IOException Se si verifica un errore durante la scrittura su file.
     */
    public static void salvaProiezione(Proiezione p) throws IOException {
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
        }
    }

    /**
     * Ricerca e carica una {@link Proiezione} specifica dal file del palinsesto mediante il suo ID.
     *
     * @param idCercato L'identificatore unico della proiezione da cercare.
     * @return Un {@link Optional} contenente la {@link Proiezione} trovata, o vuoto in caso contrario.
     * @throws IOException Se si verifica un errore di I/O.
     */
    public static Optional<Proiezione> caricaProiezionePerId(String idCercato) throws IOException {
        Path path = Paths.get(FILE_PALINSESTO);
        if (!Files.exists(path)) return Optional.empty();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;
                String[] elementi = riga.split(SEPARATORE);

                if (elementi.length < 11) continue;

                String idProiezione = elementi[0].trim();

                // Verifichiamo subito se l'ID corrisponde
                if (idProiezione.equals(idCercato.trim())) {
                    String soloData  = elementi[1].trim();
                    String soloOra   = elementi[2].trim();
                    String titolo    = elementi[3].trim();
                    String genere    = elementi[4].trim();
                    String regista   = elementi[5].trim();

                    try {
                        int anno         = Integer.parseInt(elementi[6].trim());
                        int durata       = Integer.parseInt(elementi[7].trim());
                        int etaMinima    = Integer.parseInt(elementi[8].trim());
                        double prezzo    = Double.parseDouble(elementi[9].trim());
                        int postiRimasti = Integer.parseInt(elementi[10].trim());

                        Film film = new Film(titolo, genere, regista, anno, durata, etaMinima);
                        Proiezione p = new Proiezione(idProiezione, soloData, soloOra, prezzo, film, postiRimasti);

                        return Optional.of(p); // Trovata! La restituiamo ed usciamo subito dal ciclo
                    } catch (NumberFormatException e) {
                        System.err.println("Errore nel formato numerico della proiezione ID " + idProiezione + ": " + e.getMessage());
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty(); // Nessuna proiezione trovata con questo ID
    }

    // ========================================================
    // LETTURA E SCRITTURA PRENOTAZIONI (Sincronizzato a 7 Campi)
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

        // Se il file non esiste, lo crea; se esiste, aggiunge la riga in coda (APPEND)
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
     * Cerca e carica una singola prenotazione dal file CSV tramite il suo ID.
     *
     * @param idPrenotazione L'ID unico della prenotazione da cercare.
     * @param palinsesto Il palinsesto delle proiezioni per riassociare l'oggetto Proiezione.
     * @return L'oggetto {@link Prenotazione} se trovato, {@code null} altrimenti.
     * @throws IOException Se si verifica un errore durante la lettura del file.
     */
    public static Prenotazione caricaPrenotazioneDaId(String idPrenotazione, List<Proiezione> palinsesto) throws IOException {
        Path path = Paths.get(FILE_PRENOTAZIONI);
        if (!Files.exists(path) || idPrenotazione == null) return null;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;
                String[] elementi = riga.split(SEPARATORE);

                if (elementi.length < 8) continue;

                String idLetto = elementi[0].trim();

                // Confronta l'ID del file con quello cercato
                if (idLetto.equalsIgnoreCase(idPrenotazione.trim())) {
                    String nomeCliente     = elementi[1].trim();
                    String cognomeCliente  = elementi[2].trim();
                    String usernameCliente = elementi[3].trim();
                    String passwordHash    = elementi[4].trim();
                    String idProiezione    = elementi[5].trim();
                    int numeroPosto        = Integer.parseInt(elementi[6].trim());
                    String codiceBiglietto = elementi[7].trim();

                    // Cerca la proiezione corrispondente nel palinsesto
                    Proiezione proiezioneTrovata = null;
                    for (Proiezione proj : palinsesto) {
                        if (proj.getIdProiezione().equalsIgnoreCase(idProiezione)) {
                            proiezioneTrovata = proj;
                            break;
                        }
                    }

                    if (proiezioneTrovata != null) {
                        return new Prenotazione(
                                idLetto,
                                nomeCliente,
                                cognomeCliente,
                                usernameCliente,
                                passwordHash,
                                proiezioneTrovata,
                                numeroPosto,
                                codiceBiglietto
                        );
                    }
                }
            }
        }
        return null; // Restituisce null se la prenotazione non viene trovata
    }

    /**
     * Modifica l'ID della proiezione associata a una specifica prenotazione di un utente.
     * <p>
     * Il metodo elabora il file CSV riga per riga utilizzando un file temporaneo per la scrittura,
     * evitando di caricare l'intera struttura dati in memoria.
     * </p>
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

        // Creiamo un file temporaneo nello stesso percorso
        File fileTemporaneo = new File(fileOriginale.getParent(), "prenotazioni_temp.csv");
        boolean trovato = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileOriginale));
             BufferedWriter writer = new BufferedWriter(new FileWriter(fileTemporaneo))) {

            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) {
                    continue;
                }

                String[] campi = riga.split(";");
                if (campi.length >= 3) {
                    String id = campi[0].trim();
                    String user = campi[1].trim();

                    // Se trovi la riga corrispondente all'ID e all'Utente
                    if (id.equals(idPrenotazione) && user.equalsIgnoreCase(usernameUtente)) {
                        campi[2] = idNuovaProiezione; // Aggiorna l'ID proiezione
                        riga = String.join(";", campi);
                        trovato = true;
                    }
                }

                // Scrivi la riga (modificata o originale) nel file temporaneo
                writer.write(riga);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Errore di I/O durante la modifica della prenotazione: " + e.getMessage());
            if (fileTemporaneo.exists()) {
                fileTemporaneo.delete(); // Pulisce il file temp in caso di errore
            }
            return false;
        }

        // Se la prenotazione e' stata trovata e modificata, sostituiamo il file originale con quello temporaneo
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
            // Se non e' stato trovato nulla, rimuoviamo semplicemente il file temporaneo
            fileTemporaneo.delete();
            return false;
        }
    }

    /**
     * Legge il file delle prenotazioni e stampa a schermo tutte le prenotazioni
     * attive appartenenti allo username dell'utente specificato.
     * <p>
     * Processa il file riga per riga senza caricare l'intera struttura in memoria.
     * </p>
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

                String[] campi = riga.split(";");

                if (campi.length >= 8) {
                    String idPrenotazione = campi[0].trim();
                    String nome = campi[1].trim();
                    String cognome = campi[2].trim();
                    String username = campi[3].trim();
                    String idProiezione = campi[5].trim();
                    String numeroPosto = campi[6].trim();
                    String codiceBiglietto = campi[7].trim();

                    // Verifica se la prenotazione appartiene all'utente specificato
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
                } else if (campi.length >= 4) {
                    // Formato fallback semplificato
                    String idPrenotazione = campi[0].trim();
                    String username = campi[1].trim();
                    String idProiezione = campi[2].trim();
                    String codiceBiglietto = campi[3].trim();

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
     * <p>
     * Processa il file riga per riga scrivendo su un file temporaneo tutte le righe
     * tranne quella corrispondente all'ID e allo username specificati.
     * </p>
     *
     * @param idPrenotazione L'ID unico della prenotazione da rimuovere.
     * @param usernameUtente Lo username dell'utente richiedente (per sicurezza e verifica proprieta').
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

                String[] campi = riga.split(";");

                // Verifica formato completo (8 campi) o ridotto
                if (campi.length >= 8) {
                    String id = campi[0].trim();
                    String username = campi[3].trim(); // Indice 3 = usernameCliente

                    if (id.equalsIgnoreCase(idPrenotazione) && username.equalsIgnoreCase(usernameUtente)) {
                        eliminato = true;
                        continue; // Salta la scrittura sul file temp
                    }
                } else if (campi.length >= 4) {
                    // Formato fallback
                    String id = campi[0].trim();
                    String username = campi[1].trim();

                    if (id.equalsIgnoreCase(idPrenotazione) && username.equalsIgnoreCase(usernameUtente)) {
                        eliminato = true;
                        continue; // Salta la scrittura sul file temp
                    }
                }

                // Scrive la riga non eliminata sul file temporaneo
                writer.write(riga);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("  Errore di lettura/scrittura durante la rimozione della prenotazione: " + e.getMessage());
            if (fileTemporaneo.exists()) {
                fileTemporaneo.delete();
            }
            return false;
        }

        // Sostituzione del file originale con quello aggiornato
        if (eliminato) {
            if (!fileOriginale.delete()) {
                System.out.println("  Errore: impossibile eliminare il vecchio file di prenotazioni.");
                return false;
            }
            if (!fileTemporaneo.renameTo(fileOriginale)) {
                System.out.println("  Errore: impossibile rinominare il file temporaneo.");
                return false;
            }
            return true;
        } else {
            fileTemporaneo.delete();
            return false;
        }
    }

    /**
     * Aggiorna il numero di posti disponibili per una determinata proiezione nel file CSV.
     * <p>
     * Processa il file delle proiezioni riga per riga scrivendo su un file temporaneo
     * e aggiorna il valore dei posti quando individua l'ID della proiezione cercata.
     * </p>
     *
     * @param idProiezione     L'ID unico della proiezione da aggiornare (es. P-C6014BBC).
     * @param nuoviPostiDisp Il nuovo numero di posti disponibili da salvare.
     * @return {@code true} se l'aggiornamento e' andato a buon fine, {@code false} altrimenti.
     */
    public static boolean aggiornaPostiProiezioneSuFile(String idProiezione, int nuoviPostiDisp) {
        File fileOriginale = new File(FILE_PALINSESTO);

        if (!fileOriginale.exists()) {
            System.out.println("  Archivio proiezioni non trovato.");
            return false;
        }

        File fileTemporaneo = new File(fileOriginale.getParent(), "proiezioni_temp.csv");
        boolean aggiornato = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileOriginale));
             BufferedWriter writer = new BufferedWriter(new FileWriter(fileTemporaneo))) {

            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) {
                    continue;
                }

                String[] campi = riga.split(";");

                if (campi.length >= 6) {
                    String idLetto = campi[0].trim();

                    if (idLetto.equalsIgnoreCase(idProiezione.trim())) {
                        // Modifica il campo dei posti disponibili (indice 5)
                        campi[5] = String.valueOf(nuoviPostiDisp);
                        riga = String.join(";", campi);
                        aggiornato = true;
                    }
                }

                // Scrive la riga (aggiornata o invariata) sul file temporaneo
                writer.write(riga);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("  Errore di I/O durante l'aggiornamento dei posti: " + e.getMessage());
            if (fileTemporaneo.exists()) {
                fileTemporaneo.delete();
            }
            return false;
        }

        // Sostituisce il file originale con quello aggiornato
        if (aggiornato) {
            if (!fileOriginale.delete()) {
                System.out.println("  Errore: impossibile eliminare il vecchio file delle proiezioni.");
                return false;
            }
            if (!fileTemporaneo.renameTo(fileOriginale)) {
                System.out.println("  Errore: impossibile rinominare il file temporaneo delle proiezioni.");
                return false;
            }
            return true;
        } else {
            fileTemporaneo.delete();
            return false;
        }
    }

    // ========================================================
    // SICUREZZA & PASSWORD
    // ========================================================

    /**
     * Genera l'hash crittografico di una password in chiaro utilizzando l'algoritmo
     * PBKDF2WithHmacSHA256 e una chiave segreta come salt.
     *
     * @param password La password in chiaro da cifrare.
     * @return La stringa codificata in Base64 dell'hash generato.
     * @throws RuntimeException Se si verificano errori nell'algoritmo di cifratura.
     */
    public static String generaPasswordHash(String password) {
        try {
            byte[] chiaveInByte = CHIAVE_SEGRETA.getBytes();
            KeySpec spec = new PBEKeySpec(password.toCharArray(), chiaveInByte, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); // Password-Based Key Derivation Function 2
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Errore nella cifratura", e);
        }
    }

    /**
     * Verifica la correttezza di una password inserita rispetto all'hash memorizzato nell'oggetto {@link Utente}.
     * <p>
     * Il confronto viene eseguito in tempo costante mediante {@link MessageDigest#isEqual(byte[], byte[])}
     * per prevenire vulnerabilita' a attacchi di tipo <i>Timing Attack</i>.
     * </p>
     *
     * @param utente L'utente proprietario dell'account.
     * @param passwordDaVerificare La password fornita in chiaro per il tentativo di login.
     * @return {@code true} se la password fornita corrisponde all'hash salvato, {@code false} altrimenti.
     */
    public static boolean verificaPassword(Utente utente, String passwordDaVerificare) {
        if (utente == null || utente.getPasswordHash() == null || passwordDaVerificare == null) {
            return false;
        }

        String hashCalcolato = generaPasswordHash(passwordDaVerificare);

        // Convertiamo in byte per il confronto a tempo costante
        byte[] hashInDB = utente.getPasswordHash().getBytes(StandardCharsets.UTF_8);
        byte[] hashGenerato = hashCalcolato.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(hashInDB, hashGenerato);
    }
}