package cinemax;

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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FileManager {
    private static final String FILE_UTENTI = "utenti.csv";
    private static final String FILE_PALINSESTO = "palinsesto.csv";
    private static final String FILE_PRENOTAZIONI = "prenotazioni.csv";
    private static final String SEPARATORE = ",";
    private static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ========================================================
    // LENTURA E SCRITTURA UTENTI (Gestione del Polimorfismo)
    // ========================================================

    public static void salvaUtenti(List<Utente> utenti) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_UTENTI))) {
            for (Utente u : utenti) {
                String tipo = u.getClass().getSimpleName().toUpperCase(); // CLIENTE, BIGLIETTAIO, ecc.
                String dataNascitaStr = (u.getDataNascita() != null) ? u.getDataNascita().toString() : "null";

                String riga =  u.getNome()  + SEPARATORE +
                        u.getCognome()  + SEPARATORE +
                        u.getUsername() + SEPARATORE +
                        u.getPasswordHash() + SEPARATORE +
                        dataNascitaStr + SEPARATORE +
                        u.getLuogoDomicilio() + SEPARATORE +
                         tipo ;
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

                // Lettura speculare a come hai salvato in salvaUtenti
                String nome       = token[0];
                String cognome    = token[1];
                String username   = token[2];
                String passHash   = token[3];
                String dataNascita = token[4].equals("null") ? null : token[4]; // Rimane String
                String domicilio  = token[5];
                String tipo       = token[6];

                // Ricostruiamo la sottoclasse esatta passando la String dataNascita
                if (tipo.equals("CLIENTE")) {
                    utenti.add(new Cliente(nome, cognome, username, passHash, dataNascita, domicilio, true));
                } else if (tipo.equals("BIGLIETTAIO")) {
                    utenti.add(new Bigliettaio(nome, cognome, username, passHash, dataNascita, domicilio, true));
                } else if (tipo.equals("PROIEZIONISTA")) {
                    // Allineato l'ordine dei parametri (nome, cognome, username...) come gli altri
                    utenti.add(new Proiezionista(nome, cognome, username, passHash, dataNascita, domicilio, true));
                }
            }
        }
        return utenti;
    }

    // ========================================================
    // LETTURA E SCRITTURA PALINSESTO (Mappa 200 Posti compressa)
    // ========================================================

    public static void salvaPalinsesto(List<Proiezione> palinsesto) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PALINSESTO))) {
            for (Proiezione p : palinsesto) {
                Film f = p.getFilm();

                // Dividiamo la stringa "yyyy-MM-dd HH:mm" in due parti
                String dataOraCompleta = p.getDataOraProiezione();
                String[] partiDataOra = dataOraCompleta.split(" ");
                String soloData = partiDataOra[0];
                String soloOra = partiDataOra[1];

                // Ora scriviamo soloData e soloOra come campi separati nel CSV
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

                // Recuperiamo data e ora separate dal file
                String data = token[1];
                String ora = token[2];
                // Le uniamo di nuovo in un'unica stringa come richiesto dal costruttore di Proiezione
                String dataOraRicostruita = data + " " + ora;

                // Gli indici del Film sono scalati di +1 perché ora data e ora occupano due colonne differenti
                Film film = new Film(
                        token[3], // titolo
                        token[4], // genere
                        token[5], // regista
                        Integer.parseInt(token[6]), // anno
                        Integer.parseInt(token[7]), // durata
                        Integer.parseInt(token[8])  // eta minima
                );

                Proiezione p = new Proiezione(
                        token[0],                     // idProiezione
                        dataOraRicostruita,           // dataOraProiezione (stringa unita)
                        Double.parseDouble(token[9]), // prezzoBiglietto
                        film
                );

                int postiRimasti = Integer.parseInt(token[10]);
                p.setPostiDisponibili(postiRimasti);

                // CORREZIONE: Aggiungiamo la proiezione 'p' alla lista del palinsesto, non il film
                palinsesto.add(p);
            }
        }
        return palinsesto;



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

    public boolean verificaPassword(String passwordDaVerificare) {
        return Utente.getPasswordHash().equals(generaPasswordHash(passwordDaVerificare));
    }
}
