package cinemax.Users;

import cinemax.Proiezione;
import cinemax.Film;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Proiezionista extends Utente {

    private static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Costruttore coordinato con FileManager e la classe madre Utente
    public Proiezionista(String username, String passwordHash, String nome, String cognome,
                         String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
        super(username, passwordHash, nome, cognome, dataNascita, luogoDomicilio, isAlreadyHashed);
    }

    // ========================================================
    // METODI DI GESTIONE DEL PALINSESTO (List<Proiezione>)
    // ========================================================

    /**
     * Aggiunge una proiezione al palinsesto verificando che non ci siano sovrapposizioni d'orario.
     */
    public boolean aggiungiProiezione(List<Proiezione> palinsesto, Proiezione nuovaProiezione) {
        for (Proiezione p : palinsesto) {
            // Controllo basilare: stessa data e stessa ora (espandibile calcolando la durata del film)
            if (p.getDataOraProiezione().equals(nuovaProiezione.getDataOraProiezione())) {
                System.out.println("Errore: Esiste già una proiezione pianificata per questo orario!");
                return false;
            }
        }
        palinsesto.add(nuovaProiezione);
        System.out.println("Proiezione aggiunta con successo al palinsesto.");
        return true;
    }

    /**
     * Modifica la data e l'ora di una proiezione esistente cercandola per titolo del film e vecchia data.
     */
    public boolean modificaProiezione(List<Proiezione> palinsesto, String titoloFilm, String vecchiaDataStr, String nuovaDataStr) {
        try {
            String vecchiaData = vecchiaDataStr;
            String nuovaData = nuovaDataStr;

            for (Proiezione p : palinsesto) {
                if (p.getFilm().getTitolo().equalsIgnoreCase(titoloFilm) && p.getDataOraProiezione().equals(vecchiaData)) {
                    p.setDataOraProiezione(nuovaData);
                    System.out.println("Orario della proiezione modificato con successo.");
                    return true;
                }
            }
        } catch (DateTimeParseException e) {
            System.out.println("Errore: Formato data non valido. Usa 'yyyy-MM-dd HH:mm'.");
            return false;
        }

        System.out.println("Errore: Nessuna proiezione trovata per il film \"" + titoloFilm + "\" in data " + vecchiaDataStr);
        return false;
    }

    /**
     * Elimina una proiezione dal palinsesto.
     */
    public boolean eliminaProiezione(List<Proiezione> palinsesto, String titoloFilm, String dataOraStr) {
        try {
            LocalDateTime dataOra = LocalDateTime.parse(dataOraStr, FORMATO_DATA_ORA);

            for (Proiezione p : palinsesto) {
            }
        } catch (DateTimeParseException e) {
            System.out.println("Errore: Formato data non valido.");
            return false;
        }

        System.out.println("Errore: Proiezione non trovata.");
        return false;
    }

    // ========================================================
    // INTERFACCIA UTENTE (MOSTRA MENU INTERATTIVO)
    // ========================================================

    @Override
    public void mostraMenu() {
        System.out.println("\n=== AREA PERSONALE PROIEZIONISTA: " + getNome().toUpperCase() + " ===");
        System.out.println("1. Visualizza Palinsesto Completo");
        System.out.println("2. Inserisci Nuova Proiezione");
        System.out.println("3. Modifica Orario Proiezione");
        System.out.println("4. Elimina Proiezione");
        System.out.println("5. Logout");
    }

    /**
     * Gestisce le azioni del menu (da invocare nel tuo Main Loop passandogli il palinsesto globale)
     */
    public void gestisciAzioni(int scelta, List<Proiezione> palinsesto) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                System.out.println("\n--- PALINSESTO ATTUALE ---");
                if (palinsesto.isEmpty()) {
                    System.out.println("Il palinsesto è vuoto.");
                } else {
                    for (Proiezione p : palinsesto) {
                        System.out.println(p); // Sfrutta il toString() di Proiezione
                    }
                }
                break;

            case 2:
                System.out.println("\n--- INSERIMENTO NUOVA PROIEZIONE ---");
                System.out.print("ID Proiezione univoco: ");
                String id = scanner.nextLine();
                System.out.print("Data e Ora (aaaa-mm-gg hh:mm): ");
                String dataStr = scanner.nextLine();
                System.out.print("Prezzo Biglietto (€): ");
                double prezzo = Double.parseDouble(scanner.nextLine());

                // Dati del Film associato
                System.out.print("Titolo Film: ");
                String titolo = scanner.nextLine();
                System.out.print("Genere: ");
                String genere = scanner.nextLine();
                System.out.print("Regista: ");
                String regista = scanner.nextLine();
                System.out.print("Anno di uscita: ");
                int anno = Integer.parseInt(scanner.nextLine());
                System.out.print("Durata (in minuti): ");
                int durata = Integer.parseInt(scanner.nextLine());
                System.out.print("Età minima consigliata: ");
                int etaMin = Integer.parseInt(scanner.nextLine());

                try {
                    String dataOra = dataStr;
                    Film nuovoFilm = new Film(titolo, genere, regista, anno, durata, etaMin);
                    Proiezione nuovaProiezione = new Proiezione(id, dataOra, prezzo, nuovoFilm);

                    aggiungiProiezione(palinsesto, nuovaProiezione);
                } catch (DateTimeParseException e) {
                    System.out.println("Errore: Impossibile convertire la data. Operazione annullata.");
                }
                break;

            case 3:
                System.out.println("\n--- MODIFICA ORARIO PROIEZIONE ---");
                System.out.print("Titolo del Film da modificare: ");
                String tMod = scanner.nextLine();
                System.out.print("Vecchia Data e Ora (aaaa-mm-gg hh:mm): ");
                String vecchiaData = scanner.nextLine();
                System.out.print("Nuova Data e Ora (aaaa-mm-gg hh:mm): ");
                String nuovaData = scanner.nextLine();

                modificaProiezione(palinsesto, tMod, vecchiaData, nuovaData);
                break;

            case 4:
                System.out.println("\n--- ELIMINA PROIEZIONE ---");
                System.out.print("Titolo del Film da eliminare: ");
                String tElimina = scanner.nextLine();
                System.out.print("Data e Ora della proiezione (aaaa-mm-gg hh:mm): ");
                String dataElimina = scanner.nextLine();

                eliminaProiezione(palinsesto, tElimina, dataElimina);
                break;

            case 5:
                System.out.println("Disconnessione in corso...");
                break;

            default:
                System.out.println("Scelta non valida.");
        }
    }
}