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

public class FileManager {
    private static final String SEP = File.separator;
    private static final String FILE_UTENTI = "." + SEP + "data" + SEP + "utenti.csv";
    private static final String FILE_PALINSESTO ="." + SEP + "data" + SEP + "palinsesto.csv";
    private static final String FILE_PRENOTAZIONI ="." + SEP + "data" + SEP + "prenotazioni.csv";
    // costante definita cosi se la si vuola cambiare non bisogna cercare nel codice
    private static final String CHIAVE_SEGRETA = "c8f391b4a2e5d790f61284a37b9015e14d3f28e6c710a9f5d301b894e2a6c712";

    private static final String SEPARATORE = ",";

    // ========================================================
    // LETTURA E SCRITTURA UTENTI (Gestione del Polimorfismo)
    // ========================================================

    // Ordine CSV: nome,cognome,username,passwordhash,data_di_nascita,luogo_del_domicilio,ruolo
    public static void salvaUtenti(List<Utente> utenti) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_UTENTI), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            for (Utente u : utenti) {
                String tipo = u.getClass().getSimpleName().toUpperCase();
                String dataNascitaStr = (u.getDataNascita() != null && !u.getDataNascita().trim().isEmpty()) ? u.getDataNascita() : "N/D";

                String riga =  u.getNome()  + SEPARATORE +
                        u.getCognome()  + SEPARATORE +
                        u.getUsername() + SEPARATORE +
                        u.getPasswordHash() + SEPARATORE +
                        dataNascitaStr + SEPARATORE +
                        u.getLuogoDomicilio() + SEPARATORE +
                        tipo;
                writer.write(riga);
                writer.newLine();
            }
        }
    }

    public static List<Utente> caricaUtenti() throws IOException {
        List<Utente> utenti = new ArrayList<>();
        Path path = Paths.get(FILE_UTENTI);
        if (!Files.exists(path)) return utenti;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;
                String[] elementi = riga.split(SEPARATORE);

                // Controllo di sicurezza per righe corrotte
                if (elementi.length < 7) continue;

                String nome       = elementi[0].trim();
                String cognome    = elementi[1].trim();
                String username   = elementi[2].trim();
                String passHash   = elementi[3].trim();
                String dataNascita = elementi[4].trim().equals("N/D") ? null : elementi[4].trim();
                String domicilio  = elementi[5].trim();
                String tipo       = elementi[6].trim().toUpperCase();

                switch (tipo) {
                    case "CLIENTE":
                        utenti.add(new Cliente(nome, cognome, username, passHash, dataNascita, domicilio, true));
                        break;
                    case "BIGLIETTAIO":
                        utenti.add(new Bigliettaio(nome, cognome, username, passHash, dataNascita, domicilio, true));
                        break;
                    case "PROIEZIONISTA":
                        utenti.add(new Proiezionista(nome, cognome, username, passHash, dataNascita, domicilio, true));
                        break;
                    // serve se qualcuno modifica file utenti.csv o se il file si corrompe
                    default:
                        System.err.println("Ruolo sconosciuto saltato nel CSV: " + tipo);
                        break;
                }
            }
        }
        return utenti;
    }

    // ========================================================
    // LETTURA E SCRITTURA PALINSESTO (Preservazione ID e Posti)
    // ========================================================

    public static void salvaPalinsesto(List<Proiezione> palinsesto) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PALINSESTO), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            for (Proiezione p : palinsesto) {
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
    }

    public static List<Proiezione> caricaPalinsesto() throws IOException {
        List<Proiezione> palinsesto = new ArrayList<>();
        Path path = Paths.get(FILE_PALINSESTO);
        if (!Files.exists(path)) return palinsesto;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;
                String[] elementi = riga.split(SEPARATORE);

                if (elementi.length < 11) continue;

                String idProiezione = elementi[0].trim();
                String soloData     = elementi[1].trim();
                String soloOra      = elementi[2].trim();
                String titolo       = elementi[3].trim();
                String genere       = elementi[4].trim();
                String regista      = elementi[5].trim();
                int anno            = Integer.parseInt(elementi[6].trim());
                int durata          = Integer.parseInt(elementi[7].trim());
                int etaMinima       = Integer.parseInt(elementi[8].trim());
                double prezzo       = Double.parseDouble(elementi[9].trim());
                int postiRimasti    = Integer.parseInt(elementi[10].trim());

                Film film = new Film(titolo, genere, regista, anno, durata, etaMinima);
                Proiezione p = new Proiezione(idProiezione, soloData, soloOra, prezzo, film, postiRimasti);

                palinsesto.add(p);
            }
        }
        return palinsesto;
    }

    // ========================================================
    // LETTURA E SCRITTURA PRENOTAZIONI (Sincronizzato a 7 Campi)
    // ========================================================

    public static void salvaPrenotazioni(List<Prenotazione> prenotazioni, List<Utente> utenti) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PRENOTAZIONI), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            for (Prenotazione p : prenotazioni) {

                //  Sfrutta i campi nativi interni senza fare cicli for superflui
                String nome = (p.getNomeCliente() != null) ? p.getNomeCliente() : "N/D";
                String cognome = (p.getCognomeCliente() != null) ? p.getCognomeCliente() : "N/D";
                String passHash = (p.getPasswordHash() != null) ? p.getPasswordHash() : "N/D";

                //  Ordine coerente con lo standard a 8 colonne del file
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
    }

    public static List<Prenotazione> caricaPrenotazioni(List<Proiezione> palinsesto) throws IOException {
        List<Prenotazione> prenotazioni = new ArrayList<>();
        Path path = Paths.get(FILE_PRENOTAZIONI);
        if (!Files.exists(path)) return prenotazioni;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;
                String[] elementi = riga.split(SEPARATORE);

                //  Ora il controllo di sicurezza verifica la presenza di tutte e 8 le colonne
                if (elementi.length < 8) continue;

                //  Mappatura speculare degli indici basata sul salvataggio precedente
                String idPrenotazione  = elementi[0].trim();
                String nomeCliente     = elementi[1].trim();
                String cognomeCliente  = elementi[2].trim();
                String usernameCliente = elementi[3].trim();
                String passwordHash    = elementi[4].trim();
                String idProiezione    = elementi[5].trim();
                int numeroPosto        = Integer.parseInt(elementi[6].trim());
                String codiceBiglietto = elementi[7].trim();

                Proiezione proiezioneTrovata = null;
                for (Proiezione proj : palinsesto) {
                    if (proj.getIdProiezione().equalsIgnoreCase(idProiezione)) {
                        proiezioneTrovata = proj;
                        break;
                    }
                }

                if (proiezioneTrovata != null) {
                    //  Invoca il Costruttore 1 di Prenotazione aggiornato a 8 parametri
                    Prenotazione p = new Prenotazione(
                            idPrenotazione,
                            nomeCliente,
                            cognomeCliente,
                            usernameCliente,
                            passwordHash,
                            proiezioneTrovata,
                            numeroPosto,
                            codiceBiglietto
                    );
                    prenotazioni.add(p);
                }
            }
        }
        return prenotazioni;
    }

    // ========================================================
    // SICUREZZA & PASSWORD
    // ========================================================

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
// password utenti già inseriti Cinema2026