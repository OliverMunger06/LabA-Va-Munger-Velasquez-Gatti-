package cinemax.Users;

import cinemax.utils.FileManager;
import cinemax.gestione.Proiezione;
import cinemax.gestione.Film;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Rappresenta la figura del Proiezionista all'interno del sistema Cinemax.
 * <p>
 * Estende la classe {@link Utente} fornendo le funzionalità per la gestione operativa
 * del palinsesto cinematografico direttamente su file CSV: inserimento di nuove proiezioni
 * (verificando la sovrapposizione), modifica di data/ora e cancellazione di uno spettacolo
 * (subordinata all'assenza di prenotazioni attive).
 * </p>
 *
 * @author Cinemax Team
 * @version 2.1
 */
public class Proiezionista extends Utente {

    /**
     * Costruisce un nuovo utente {@code Proiezionista} con le credenziali e i dati anagrafici specificati.
     *
     * @param nome            Il nome del proiezionista.
     * @param cognome         Il cognome del proiezionista.
     * @param username        Lo username per l'autenticazione.
     * @param passwordHash    L'hash della password di sicurezza.
     * @param dataNascita     La data di nascita in formato testo.
     * @param luogoDomicilio  Il luogo di domicilio.
     */
    public Proiezionista(String nome, String cognome, String username, String passwordHash,
                         String dataNascita, String luogoDomicilio) {
        super(nome, cognome, username, passwordHash, dataNascita, luogoDomicilio);
    }

    // ========================================================
    // METODI OPERATIVI DEL PROIEZIONISTA
    // ========================================================

    /**
     * Aggiunge una nuova proiezione al palinsesto, verificando che non si sovrapponga
     * con una proiezione esistente (stessa data, ora e sala o slot temporale).
     *
     * @param scanner Scanner attivo per l'acquisizione dei dati da console.
     */
    public void aggiungiProiezione(Scanner scanner) {
        System.out.println("\n--- INSERIMENTO NUOVA PROIEZIONE ---");

        Date dataProiezione = null;
        while (dataProiezione == null) {
            System.out.print("Data (gg/mm/aaaa): ");
            String dataStr = scanner.nextLine().trim();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                dataProiezione = sdf.parse(dataStr);
            } catch (ParseException e) {
                System.out.println("  Errore: Formato data non valido. Usa il formato gg/mm/aaaa.");
            }
        }

        LocalTime oraProiezione = null;
        while (oraProiezione == null) {
            System.out.print("Ora (hh:mm): ");
            String oraStr = scanner.nextLine().trim();
            try {
                oraProiezione = LocalTime.parse(oraStr);
            } catch (DateTimeParseException e) {
                System.out.println("  Errore: Formato ora non valido. Usa il formato hh:mm.");
            }
        }

        double prezzo = 0.0;
        while (true) {
            System.out.print("Prezzo Biglietto (€): ");
            try {
                prezzo = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
                if (prezzo >= 0) break;
                System.out.println("  Il prezzo non può essere negativo.");
            } catch (NumberFormatException e) {
                System.out.println("  Errore: Inserisci un prezzo numerico valido (es. 7.50).");
            }
        }

        System.out.print("Titolo Film: ");
        String titolo = scanner.nextLine().trim();
        System.out.print("Genere: ");
        String genere = scanner.nextLine().trim();
        System.out.print("Regista: ");
        String regista = scanner.nextLine().trim();

        int anno = 0, durata = 0, etaMin = 0;
        try {
            System.out.print("Anno di uscita: ");
            anno = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Durata (in minuti): ");
            durata = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Età minima consigliata: ");
            etaMin = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Valori numerici non validi. Impostati a 0 di default.");
        }

        Film nuovoFilm = new Film(titolo, genere, regista, anno, durata, etaMin);
        Proiezione nuovaProiezione = new Proiezione(dataProiezione, oraProiezione, prezzo, nuovoFilm);

        boolean salvato = FileManager.salvaProiezione(nuovaProiezione);
        if (salvato) {
            System.out.println("  Proiezione aggiunta con successo su file CSV!");
            System.out.println("  ID Spettacolo assegnato: " + nuovaProiezione.getIdProiezione());
        } else {
            System.out.println("  Errore: Esiste già una proiezione pianificata per la stessa data e orario (sovrapposizione rilevata).");
        }
    }

    /**
     * Modifica data e ora di una proiezione inserita in precedenza.
     * Requisito: L'operazione è consentita a patto che non ci siano prenotazioni attive per quella proiezione.
     *
     * @param scanner Scanner attivo per l'acquisizione dei dati da console.
     */
    public void modificaProiezione(Scanner scanner) {
        System.out.println("\n--- MODIFICA DATA E ORARIO PROIEZIONE ---");
        System.out.print("Titolo del Film da modificare: ");
        String tMod = scanner.nextLine().trim();
        System.out.print("Vecchia Data (gg/mm/aaaa): ");
        String vecchiaData = scanner.nextLine().trim();
        System.out.print("Vecchia Ora (hh:mm): ");
        String vecchiaOra = scanner.nextLine().trim();
        System.out.print("Nuova Data (gg/mm/aaaa): ");
        String nuovaData = scanner.nextLine().trim();
        System.out.print("Nuova Ora (hh:mm): ");
        String nuovaOra = scanner.nextLine().trim();

        boolean modificato = FileManager.modificaProiezione(tMod, vecchiaData, vecchiaOra, nuovaData, nuovaOra);
        if (modificato) {
            System.out.println("  Data e orario modificati con successo sul file CSV.");
        } else {
            System.out.println("  Impossibile modificare: proiezione non trovata o sono presenti prenotazioni attive per questo spettacolo.");
        }
    }

    /**
     * Cancella una proiezione inserita in precedenza dal palinsesto.
     * Requisito: L'operazione è consentita a patto che non ci siano prenotazioni attive per quella proiezione.
     *
     * @param scanner Scanner attivo per l'acquisizione dei dati da console.
     */
    public void eliminaProiezione(Scanner scanner) {
        System.out.println("\n--- ELIMINA PROIEZIONE ---");
        System.out.print("Titolo del Film da eliminare: ");
        String tElimina = scanner.nextLine().trim();
        System.out.print("Data della proiezione (gg/mm/aaaa): ");
        String dataElimina = scanner.nextLine().trim();
        System.out.print("Ora della proiezione (hh:mm): ");
        String oraElimina = scanner.nextLine().trim();

        boolean eliminato = FileManager.eliminaProiezione(tElimina, dataElimina, oraElimina);
        if (eliminato) {
            System.out.println("  Proiezione eliminata con successo dal file CSV.");
        } else {
            System.out.println("  Impossibile eliminare: proiezione non trovata o sono presenti prenotazioni attive per questo spettacolo.");
        }
    }

    // ========================================================
    // INTERFACCIA UTENTE E MENU
    // ========================================================

    /**
     * Restituisce il valore numerico del menu corrispondente all'operazione di logout per il Proiezionista.
     *
     * @return L'intero {@code 4}, rappresentante l'opzione di disconnessione dal sistema.
     */
    @Override
    public int getOpzioneLogout() {
        return 4;
    }

    /**
     * Mostra a schermo le opzioni del menu dedicate alle attività del proiezionista.
     */
    @Override
    public void mostraMenu() {
        System.out.println("\n=== AREA PERSONALE PROIEZIONISTA: " + getNome().toUpperCase() + " ===");
        System.out.println("1. Inserisci una nuova proiezione");
        System.out.println("2. Modifica data e ora di una proiezione");
        System.out.println("3. Elimina una proiezione dal palinsesto");
        System.out.println("4. Logout");
    }

    /**
     * Gestisce la logica e le interazioni da riga di comando relative all'opzione selezionata dal proiezionista.
     *
     * @param scelta L'opzione numerica selezionata dal menu.
     */
    @Override
    public void eseguiAzione(int scelta) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                this.aggiungiProiezione(scanner);
                break;
            case 2:
                this.modificaProiezione(scanner);
                break;
            case 3:
                this.eliminaProiezione(scanner);
                break;
            case 4:
                System.out.println("Disconnessione proiezionista in corso...");
                break;
            default:
                System.out.println("Scelta non valida.");
                break;
        }
    }
}