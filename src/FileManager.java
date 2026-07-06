import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

                String nome = token[0];
                String cognome = token[1];
                String username = token[2];
                String passHash = token[3];
                LocalDate dataNascita = token[4].equals("null") ? null : LocalDate.parse(token[4]);;
                String domicilio = token[5];
                String tipo = token[6];

                // Ricostruiamo la sottoclasse esatta (visto che Utente è astratta)
                if (tipo.equals("CLIENTE")) {
                    utenti.add(new Cliente(username, passHash, nome, cognome, dataNascita, domicilio, true));
                } else if (tipo.equals("BIGLIETTAIO")) {
                    utenti.add(new Bigliettaio(username, passHash, nome, cognome, dataNascita, domicilio, true));
                } else if (tipo.equals("PROIEZIONISTA")) {
                    utenti.add(new Proiezionista(username, passHash, nome, cognome, dataNascita, domicilio, true));
                }
            }
        }
        return utenti;
    }

    // ========================================================
    // LETTURA E SCRITTURA PALINSESTO (Mappa 200 Posti compressa)
    // ========================================================

    public static void salvaPalinsesto(List<Film> palinsesto) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PALINSESTO))) {
            for (Film f : palinsesto) {
                // Convertiamo l'array boolean[200] in una stringa di '0' e '1'
                StringBuilder mappaStr = new StringBuilder();
                for (boolean postoOccupato : f.getMappaPosti()) {
                    mappaStr.append(postoOccupato ? '1' : '0');
                }

                String riga = f.getIdProiezione() + SEPARATORE +
                        f.getDataOraProiezione().format(FORMATO_DATA_ORA) + SEPARATORE +
                        f.getTitoloFilm() + SEPARATORE +
                        f.getGenere() + SEPARATORE +
                        f.getRegista() + SEPARATORE +
                        f.getAnno() + SEPARATORE +
                        f.getDurataMinuti() + SEPARATORE +
                        f.getEtaMinima() + SEPARATORE +
                        f.getPrezzoBiglietto() + SEPARATORE +
                        mappaStr.toString();
                writer.write(riga);
                writer.newLine();
            }
        }
    }

    public static List<Film> caricaPalinsesto() throws IOException {
        List<Film> palinsesto = new ArrayList<>();
        Path path = Paths.get(FILE_PALINSESTO);
        if (!Files.exists(path)) return palinsesto;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.trim().isEmpty()) continue;
                String[] token = riga.split(SEPARATORE);

                Film film = new Film(
                        token[0], // idProiezione
                        LocalDateTime.parse(token[1], FORMATO_DATA_ORA),
                        token[2], // titolo
                        token[3], // genere
                        token[4], // regista
                        Integer.parseInt(token[5]),
                        Integer.parseInt(token[6]), // durata
                        Integer.parseInt(token[7]), // eta minima
                        Double.parseDouble(token[8]) // prezzo
                );

                // Ripristiniamo lo stato dei 200 posti leggendo i singoli caratteri '0'/'1'
                String mappaStr = token[9];
                boolean[] mappa = new boolean[200];
                for (int i = 0; i < 200; i++) {
                    mappa[i] = (mappaStr.charAt(i) == '1');
                }
                film.setMappaPosti(mappa);

                palinsesto.add(film);
            }
        }
        return palinsesto;
    }
}
