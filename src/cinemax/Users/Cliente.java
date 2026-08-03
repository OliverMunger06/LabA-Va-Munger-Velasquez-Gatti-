package cinemax.Users;

import cinemax.utils.FileManager;
import cinemax.gestione.Proiezione;
import cinemax.gestione.Film;
import cinemax.gestione.Prenotazione;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Rappresenta l'utente di tipo Cliente all'interno del sistema Cinemax.
 * <p>
 * Un cliente puo' ricercare spettacoli in palinsesto, effettuare nuove prenotazioni,
 * visualizzare le proprie prenotazioni attive, modificarne la proiezione o cancellarle.
 * Le operazioni di persistenza avvengono direttamente su file CSV riga per riga.
 * </p>
 *
 * @author Cinemax Team
 */
public class Cliente extends Utente {

    // ------------------------------------------------------------------------
    // COSTRUTTORI
    // ------------------------------------------------------------------------

    /**
     * Costruisce un nuovo oggetto {@code Cliente} registrando una password in chiaro.
     * La password verra' sottoposta ad hashing dalla classe padre {@link Utente}.
     *
     * @param nome             Il nome del cliente.
     * @param cognome          Il cognome del cliente.
     * @param username         Lo username unico per l'accesso.
     * @param passwordInChiaro La password non cifrata.
     * @param dataNascita      La data di nascita nel formato gg/mm/aaaa.
     * @param luogoDomicilio   Il luogo di domicilio del cliente.
     */
    public Cliente(String nome, String cognome, String username, String passwordInChiaro,
                   String dataNascita, String luogoDomicilio) {
        super(nome, cognome, username, passwordInChiaro, dataNascita, luogoDomicilio);
    }

    /**
     * Costruisce un oggetto {@code Cliente} gia' esistente caricando l'hash della password.
     *
     * @param nome           Il nome del cliente.
     * @param cognome        Il cognome del cliente.
     * @param username       Lo username unico del cliente.
     * @param passwordHash   L'hash della password gia' calcolato.
     * @param dataNascita    La data di nascita nel formato gg/mm/aaaa.
     * @param luogoDomicilio Il luogo di domicilio.
     * @param isAlreadyHashed {@code true} se la password fornita e' gia' un hash.
     */
    public Cliente(String nome, String cognome, String username, String passwordHash,
                   String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
        super(nome, cognome, username, passwordHash, dataNascita, luogoDomicilio, isAlreadyHashed);
    }

    // ------------------------------------------------------------------------
    // METODI OPERATIVI
    // ------------------------------------------------------------------------

    /**
     * Crea una nuova prenotazione per una determinata proiezione, scalando i posti
     * disponibili ed effettuando il salvataggio diretto sul file CSV.
     *
     * @param proiezione La proiezione oggetto della prenotazione.
     */
    public void creaPrenotazione(Proiezione proiezione) {
        if (proiezione.getPostiDisponibili() <= 0) {
            System.out.println("  Errore: Ci dispiace, la sala e' al completo per questa proiezione.");
            return;
        }

        if (proiezione.prenotaPosto()) {
            Prenotazione nuovaPrenotazione = new Prenotazione(this, proiezione);

            try {
                FileManager.salvaPrenotazione(nuovaPrenotazione);
                FileManager.aggiornaPostiProiezioneSuFile(proiezione.getIdProiezione(), proiezione.getPostiDisponibili());
            } catch (Exception e) {
                System.out.println("  Avviso: Errore durante il salvataggio dei dati su file: " + e.getMessage());
            }

            System.out.println("  Spettacolo prenotato con successo!");
            System.out.println("  ID Biglietto: " + nuovaPrenotazione.getIdPrenotazione() +
                    " | Codice di Sicurezza QR: " + nuovaPrenotazione.getCodiceBiglietto());
        }
    }

    /**
     * Annulla una prenotazione appartenente al cliente corrente rimuovendola
     * direttamente dal file CSV di persistenza.
     *
     * @param idPrenotazione L'identificativo unico della prenotazione da eliminare.
     */
    public void eliminaPrenotazione(String idPrenotazione) {
        try {
            boolean rimossa = FileManager.rimuoviPrenotazioneDaFile(idPrenotazione, this.getUsername());
            if (rimossa) {
                System.out.println("  Prenotazione annullata con successo dal file CSV.");
            } else {
                System.out.println("  Errore: Prenotazione non trovata o ID non valido per questo utente.");
            }
        } catch (Exception e) {
            System.out.println("  Errore durante l'eliminazione della prenotazione dal file.");
        }
    }

    // ------------------------------------------------------------------------
    // INTERFACCIA UTENTE E MENU
    // ------------------------------------------------------------------------

    /**
     * Stampa a schermo le opzioni disponibili nel menu dell'area personale del cliente.
     */
    @Override
    public void mostraMenu() {
        System.out.println("\n=== AREA PERSONALE CLIENTE: " + getNome().toUpperCase() + " ===");
        System.out.println("1. Cerca proiezioni");
        System.out.println("2. Inserisci una nuova prenotazione");
        System.out.println("3. Visualizza le tue prenotazioni attive");
        System.out.println("4. Modifica le tue prenotazioni (Cambio Data/Spettacolo)");
        System.out.println("5. Cancella una prenotazione");
        System.out.println("6. Logout");
    }

    /**
     * Gestisce la logica e le interazioni da riga di comando relative all'opzione selezionata dal cliente.
     *
     * @param scelta    L'opzione numerica selezionata dal menu.
     * @param palinsesto La lista di proiezioni attualmente disponibili nel palinsesto.
     */
    public void eseguiAzione(int scelta, List<Proiezione> palinsesto) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                System.out.println("\n--- 1. RICERCA E VISUALIZZAZIONE PROIEZIONI ---");

                System.out.print("- Titolo film (INVIO per tutti): ");
                String titolo = scanner.nextLine().trim();

                System.out.print("- Genere/Tipologia (INVIO per tutti): ");
                String genere = scanner.nextLine().trim();

                System.out.print("- Data inizio intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                String dataInizioStr = scanner.nextLine().trim();

                System.out.print("- Data fine intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                String dataFineStr = scanner.nextLine().trim();

                LocalDate filterInizio = null;
                LocalDate filterFine = null;

                try {
                    if (!dataInizioStr.isEmpty()) filterInizio = LocalDate.parse(dataInizioStr, FMT_ITA);
                    if (!dataFineStr.isEmpty()) filterFine = LocalDate.parse(dataFineStr, FMT_ITA);
                } catch (DateTimeParseException e) {
                    System.out.println("  Errore: Uno o entrambi i formati data inseriti non sono validi (usa gg/mm/aaaa).");
                    break;
                }

                double prezzoMinimo = 3.50;
                System.out.print("- Prezzo minimo (EUR, INVIO per 3.50 EUR): ");
                String pMinInput = scanner.nextLine().trim();
                if (!pMinInput.isEmpty()) {
                    try {
                        prezzoMinimo = Double.parseDouble(pMinInput.replace(",", "."));
                    } catch (NumberFormatException e) {
                        System.out.println("  Prezzo minimo non valido, impostato a valore predefinito (3.50 EUR).");
                    }
                }

                double prezzoMassimo = 9999.0;
                System.out.print("- Prezzo massimo (EUR, INVIO per nessun limite): ");
                String pMaxInput = scanner.nextLine().trim();
                if (!pMaxInput.isEmpty()) {
                    try {
                        prezzoMassimo = Double.parseDouble(pMaxInput.replace(",", "."));
                    } catch (NumberFormatException e) {
                        System.out.println("  Prezzo massimo non valido, impostato a nessun limite.");
                    }
                }

                List<Proiezione> risultatiFiltrati = new ArrayList<>();
                for (Proiezione p : palinsesto) {
                    Film f = p.getFilm();

                    if (!titolo.isEmpty() && !f.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                        continue;
                    }

                    if (!genere.isEmpty() && !f.getGenere().equalsIgnoreCase(genere)) {
                        continue;
                    }

                    String dataPStr = p.getDataProiezione();
                    try {
                        LocalDate dataProiezioneObj = LocalDate.parse(dataPStr, FMT_ITA);
                        if (filterInizio != null && dataProiezioneObj.isBefore(filterInizio)) continue;
                        if (filterFine != null && dataProiezioneObj.isAfter(filterFine)) continue;
                    } catch (DateTimeParseException e) {
                        continue;
                    }

                    if (p.getPrezzoBiglietto() < prezzoMinimo || p.getPrezzoBiglietto() > prezzoMassimo) {
                        continue;
                    }

                    risultatiFiltrati.add(p);
                }

                if (risultatiFiltrati.isEmpty()) {
                    System.out.println("  Nessuna proiezione corrisponde ai criteri cercati.");
                    break;
                }

                System.out.println("\n--- RISULTATI TROVATI ---");
                for (int i = 0; i < risultatiFiltrati.size(); i++) {
                    Proiezione p = risultatiFiltrati.get(i);
                    System.out.println((i + 1) + ". " + p.getFilm().getTitolo() + " (" + p.getDataProiezione() + " ore " + p.getOraProiezione() + ")");
                }
                System.out.println("0. Torna al menu");

                System.out.print("\nInserisci il numero della proiezione per vederne i dettagli (0 per annullare): ");
                try {
                    int indiceScelto = Integer.parseInt(scanner.nextLine().trim());

                    if (indiceScelto == 0) {
                        System.out.println("Selezione annullata.");
                    } else if (indiceScelto > 0 && indiceScelto <= risultatiFiltrati.size()) {
                        Proiezione proiezioneSelezionata = risultatiFiltrati.get(indiceScelto - 1);
                        Utente.visualizzaProiezione(proiezioneSelezionata);
                        System.out.println("Premi INVIO per tornare al menu principale...");
                        scanner.nextLine();
                    } else {
                        System.out.println("  Numero non valido.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("  Errore: Inserisci un numero valido.");
                }
                break;

            case 2:
                System.out.println("\n--- 2. INSERISCI UNA NUOVA PRENOTAZIONE ---");
                System.out.print("Digita il titolo del film che vuoi prenotare (INVIO per vederli tutti): ");
                String titoloCercato = scanner.nextLine().trim();

                List<Proiezione> proiezioniTrovate = new ArrayList<>();
                for (Proiezione p : palinsesto) {
                    if (titoloCercato.isEmpty() || p.getFilm().getTitolo().toLowerCase().contains(titoloCercato.toLowerCase())) {
                        proiezioniTrovate.add(p);
                    }
                }

                if (proiezioniTrovate.isEmpty()) {
                    System.out.println("  Nessuno spettacolo trovato per il titolo inserito.");
                    break;
                }

                System.out.println("\nSpettacoli disponibili:");
                for (int i = 0; i < proiezioniTrovate.size(); i++) {
                    Proiezione p = proiezioniTrovate.get(i);
                    System.out.println((i + 1) + ". " + p.getFilm().getTitolo() +
                            " | Data: " + p.getDataProiezione() + " ore " + p.getOraProiezione() +
                            " [Posti disponibili: " + p.getPostiDisponibili() + "]");
                }
                System.out.println("0. Annulla l'operazione");

                int sceltaSpettacolo = -1;
                while (true) {
                    System.out.print("\nSeleziona il numero dello spettacolo da prenotare: ");
                    try {
                        sceltaSpettacolo = Integer.parseInt(scanner.nextLine().trim());
                        if (sceltaSpettacolo >= 0 && sceltaSpettacolo <= proiezioniTrovate.size()) {
                            break;
                        }
                        System.out.println("  Numero di selezione non valido. Riprova.");
                    } catch (NumberFormatException e) {
                        System.out.println("  Errore: Inserisci un numero intero valido.");
                    }
                }

                if (sceltaSpettacolo == 0) {
                    System.out.println("Operazione annullata.");
                    break;
                }

                Proiezione proiezioneScelta = proiezioniTrovate.get(sceltaSpettacolo - 1);

                int postiRichiesti = 0;
                while (true) {
                    System.out.print("Quanti posti desideri richiedere? (0 per annullare): ");
                    try {
                        postiRichiesti = Integer.parseInt(scanner.nextLine().trim());
                        if (postiRichiesti >= 0) {
                            break;
                        }
                        System.out.println("  Errore: Il numero di posti deve essere positivo.");
                    } catch (NumberFormatException e) {
                        System.out.println("  Errore: Inserisci un numero intero valido.");
                    }
                }

                if (postiRichiesti == 0) {
                    System.out.println("Operazione annullata.");
                    break;
                }

                if (postiRichiesti <= proiezioneScelta.getPostiDisponibili()) {
                    for (int k = 0; k < postiRichiesti; k++) {
                        this.creaPrenotazione(proiezioneScelta);
                    }
                } else {
                    System.out.println("  Errore: Non ci sono abbastanza posti disponibili. Posti rimasti: " + proiezioneScelta.getPostiDisponibili());
                }
                break;

            case 3:
                System.out.println("\n--- 3. LE TUE PRENOTAZIONI ---");
                try {
                    FileManager.stampaPrenotazioniUtente(this.getUsername());
                } catch (Exception e) {
                    System.out.println("  Errore durante la lettura delle prenotazioni.");
                }
                break;

            case 4:
                System.out.println("\n--- 4. MODIFICA PRENOTAZIONE (CAMBIO DATA) ---");
                System.out.print("Inserisci l'ID della prenotazione da spostare: ");
                String idPrenotazioneMod = scanner.nextLine().trim();

                System.out.print("Inserisci l'ID della NUOVA proiezione su cui vuoi spostarti: ");
                String idNuovaProiezione = scanner.nextLine().trim();

                try {
                    boolean modificato = FileManager.modificaProiezioneInPrenotazione(idPrenotazioneMod, this.getUsername(), idNuovaProiezione);
                    if (modificato) {
                        System.out.println("  Prenotazione aggiornata con successo su file!");
                    } else {
                        System.out.println("  Impossibile modificare: ID prenotazione errato o non tuo.");
                    }
                } catch (Exception e) {
                    System.out.println("  Errore durante la modifica del file CSV.");
                }
                break;

            case 5:
                System.out.println("\n--- 5. CANCELLA PRENOTAZIONE ---");
                System.out.print("Inserisci l'ID della prenotazione da annullare: ");
                String idPrenotazioneCanc = scanner.nextLine().trim();
                this.eliminaPrenotazione(idPrenotazioneCanc);
                break;

            case 6:
                System.out.println("Disconnessione cliente in corso...");
                break;

            default:
                System.out.println("  Scelta non valida.");
        }
    }
}