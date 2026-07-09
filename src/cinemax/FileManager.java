package cinemax;

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

public class FileManager {
    private static final String FILE_UTENTI = "data/utenti.csv";
    private static final String FILE_PALINSESTO = "data/palinsesto.csv";
    private static final String FILE_PRENOTAZIONI = "data/prenotazioni.csv";

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
                String[] token = riga.split(SEPARATORE);

                // Controllo di sicurezza per righe corrotte
                if (token.length < 7) continue;

                String nome       = token[0].trim();
                String cognome    = token[1].trim();
                String username   = token[2].trim();
                String passHash   = token[3].trim();
                String dataNascita = token[4].trim().equals("N/D") ? null : token[4].trim();
                String domicilio  = token[5].trim();
                String tipo       = token[6].trim().toUpperCase();

                if (tipo.equals("CLIENTE")) {
                    utenti.add(new Cliente(nome, cognome, username, passHash, dataNascita, domicilio, true));
                } else if (tipo.equals("BIGLIETTAIO")) {
                    utenti.add(new Bigliettaio(nome, cognome, username, passHash, dataNascita, domicilio, true));
                } else if (tipo.equals("PROIEZIONISTA")) {
                    utenti.add(new Proiezionista(nome, cognome, username, passHash, dataNascita, domicilio, true));
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
                String[] token = riga.split(SEPARATORE);

                if (token.length < 11) continue;

                String idProiezione = token[0].trim();
                String soloData     = token[1].trim();
                String soloOra      = token[2].trim();
                String titolo       = token[3].trim();
                String genere       = token[4].trim();
                String regista      = token[5].trim();
                int anno            = Integer.parseInt(token[6].trim());
                int durata          = Integer.parseInt(token[7].trim());
                int etaMinima       = Integer.parseInt(token[8].trim());
                double prezzo       = Double.parseDouble(token[9].trim());
                int postiRimasti    = Integer.parseInt(token[10].trim());

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

                // ✅ Sfrutta i campi nativi interni senza fare cicli for superflui
                String nome = (p.getNomeCliente() != null) ? p.getNomeCliente() : "N/D";
                String cognome = (p.getCognomeCliente() != null) ? p.getCognomeCliente() : "N/D";
                String passHash = (p.getPasswordHash() != null) ? p.getPasswordHash() : "N/D";

                // ✅ Ordine coerente con lo standard a 8 colonne del file
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
                String[] token = riga.split(SEPARATORE);

                // ✅ Ora il controllo di sicurezza verifica la presenza di tutte e 8 le colonne
                if (token.length < 8) continue;

                // ✅ Mappatura speculare degli indici basata sul salvataggio precedente
                String idPrenotazione  = token[0].trim();
                String nomeCliente     = token[1].trim();
                String cognomeCliente  = token[2].trim();
                String usernameCliente = token[3].trim();
                String passwordHash    = token[4].trim();
                String idProiezione    = token[5].trim();
                int numeroPosto        = Integer.parseInt(token[6].trim());
                String codiceBiglietto = token[7].trim();

                Proiezione proiezioneTrovata = null;
                for (Proiezione proj : palinsesto) {
                    if (proj.getIdProiezione().equalsIgnoreCase(idProiezione)) {
                        proiezioneTrovata = proj;
                        break;
                    }
                }

                if (proiezioneTrovata != null) {
                    // ✅ Invoca il Costruttore 1 di Prenotazione aggiornato a 8 parametri
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
            byte[] salt = "SaltSegretoCinema2026".getBytes();
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Errore nella cifratura", e);
        }
    }

    public static boolean verificaPassword(Utente utente, String passwordDaVerificare) {
        return utente.getPasswordHash().equals(generaPasswordHash(passwordDaVerificare));
    }
}