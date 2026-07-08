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
    private static final String FILE_UTENTI = "utenti.csv";
    private static final String FILE_PALINSESTO = "palinsesto.csv";
    private static final String FILE_PRENOTAZIONI = "prenotazioni.csv";
    private static final String SEPARATORE = ",";

    // ========================================================
    // LETTURA E SCRITTURA UTENTI (Gestione del Polimorfismo)
    // ========================================================

    public static void salvaUtenti(List<Utente> utenti) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_UTENTI))) {
            for (Utente u : utenti) {
                String tipo = u.getClass().getSimpleName().toUpperCase(); // CLIENTE, PROIEZIONISTA, BIGLIETTAIO
                String dataNascitaStr = (u.getDataNascita() != null && !u.getDataNascita().trim().isEmpty()) ? u.getDataNascita() : "null";

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

                String nome       = token[0].trim();
                String cognome    = token[1].trim();
                String username   = token[2].trim();
                String passHash   = token[3].trim();
                String dataNascita = token[4].trim().equals("null") ? null : token[4].trim();
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
    // LETTURA E SCRITTURA PALINSESTO (Data e Ora Separate)
    // ========================================================

    public static void salvaPalinsesto(List<Proiezione> palinsesto) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PALINSESTO))) {
            for (Proiezione p : palinsesto) {
                Film f = p.getFilm();

                String soloData = p.getDataProiezione();
                String soloOra = p.getOraProiezione();

                String riga = p.getIdProiezione() + SEPARATORE +
                        soloData + SEPARATORE +
                        soloOra + SEPARATORE +
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

                // Se hai implementato il secondo costruttore a 6 parametri che accetta i postiRimasti:
                // Proiezione p = new Proiezione(idProiezione, soloData, soloOra, prezzo, film, postiRimasti);

                // Altrimenti usiamo il primo costruttore e aggiorniamo i posti subito dopo:
                Proiezione p = new Proiezione(idProiezione, soloData, soloOra, prezzo, film);
                p.setPostiDisponibili(postiRimasti);

                palinsesto.add(p);
            }
        }
        return palinsesto;
    }

    public static void salvaPrenotazioni(List<Prenotazione> prenotazioni) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PRENOTAZIONI))) {
            for (Prenotazione p : prenotazioni) {
                // Ordine richiesto: id_Prenotazione, username_Cliente, id_Proiezione, numero_Posto, codice_Biglietto
                String riga = p.getIdPrenotazione() + SEPARATORE +
                        p.getUsernameCliente() + SEPARATORE +
                        p.getFilmProiezione().getIdProiezione() + SEPARATORE +
                        p.getNumeroPosto() + SEPARATORE +
                        p.getCodiceBiglietto();

                writer.write(riga);
                writer.newLine();
            }
        }
    }

    /**
     * Carica le prenotazioni dal file e le ricostruisce legandole agli oggetti Proiezione esistenti.
     * Riceve il palinsesto per poter cercare la proiezione corretta tramite l'id_Proiezione.
     */
    public static List<Prenotazione> caricaPrenotazioni(List<Proiezione> palinsesto) throws IOException {
        List<Prenotazione> prenotazioni = new ArrayList<>();
        Path path = Paths.get(FILE_PRENOTAZIONI);
        if (!Files.exists(path)) return prenotazioni;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;
                String[] token = riga.split(SEPARATORE);

                // Mappatura secondo l'ordine esatto del file
                String idPrenotazione  = token[0].trim();
                String usernameCliente = token[1].trim();
                String idProiezione    = token[2].trim();
                int numeroPosto        = Integer.parseInt(token[3].trim());
                String codiceBiglietto = token[4].trim();

                // COSTRUTTORE DI COLLEGAMENTO (Risoluzione del riferimento a Proiezione)
                Proiezione proiezioneTrovata = null;
                for (Proiezione proj : palinsesto) {
                    if (proj.getIdProiezione().equalsIgnoreCase(idProiezione)) {
                        proiezioneTrovata = proj;
                        break;
                    }
                }

                // Se la proiezione esiste ancora a palinsesto, ricostruiamo l'oggetto Prenotazione
                if (proiezioneTrovata != null) {
                    Prenotazione p = new Prenotazione(
                            idPrenotazione,
                            usernameCliente,
                            proiezioneTrovata,
                            codiceBiglietto,
                            numeroPosto
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

    /**
     * MODIFICATO: Aggiunto 'static' per consentire la chiamata diretta senza istanziare FileManager.
     */
    public static boolean verificaPassword(Utente utente, String passwordDaVerificare) {
        return utente.getPasswordHash().equals(generaPasswordHash(passwordDaVerificare));
    }
}