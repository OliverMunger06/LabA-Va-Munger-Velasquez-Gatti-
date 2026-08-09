package cinemax.Users;

import cinemax.utils.FileManager;
import cinemax.gestione.Proiezione;
import cinemax.gestione.Film;
import cinemax.gestione.Prenotazione;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
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
 * @author Oliver Munger , matricola num. 764208 , VA
 * @author Davide Gatti , matricola num. 765949 , VA
 * @author Davide Noe Velasquez Carpio , matricola num. 765163 , VA
 */
public class Cliente extends Utente {


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
     * Modifica la proiezione associata a una prenotazione esistente del cliente.
     * <p>
     * Requisito fondamentale: sia la data della vecchia proiezione che la data
     * della nuova proiezione scelta devono essere strettamente successive alla data odierna.
     * </p>
     *
     * @param scanner Lo scanner attivo per la lettura dell'input da console.
     */
    public void modificaPrenotazione(Scanner scanner) {
        try {
            System.out.print("Inserisci l'ID della prenotazione da modificare: ");
            String idPrenotazione = scanner.nextLine().trim();

            List<Proiezione> palinsesto = FileManager.caricaPalinsesto();
            if (palinsesto.isEmpty()) {
                System.out.println("  Errore: Impossibile accedere al palinsesto.");
                return;
            }

            System.out.println("\nSpettacoli disponibili nel palinsesto:");
            for (int i = 0; i < palinsesto.size(); i++) {
                Proiezione p = palinsesto.get(i);
                String dataStr = p.getDataProiezione() != null ? FMT_ITA.format(p.getDataProiezione().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) : "N/D";
                System.out.println((i + 1) + ". [ID: " + p.getIdProiezione() + "] " +
                        p.getFilm().getTitolo() + " - Data: " + dataStr + " ore " + p.getOraProiezione());
            }

            System.out.print("\nInserisci l'ID della NUOVA proiezione su cui vuoi spostarti: ");
            String idNuovaProiezione = scanner.nextLine().trim();

            Proiezione nuovaProiezione = null;
            for (Proiezione p : palinsesto) {
                if (p.getIdProiezione().equalsIgnoreCase(idNuovaProiezione)) {
                    nuovaProiezione = p;
                    break;
                }
            }

            if (nuovaProiezione == null) {
                System.out.println("  Errore: La nuova proiezione con ID '" + idNuovaProiezione + "' non esiste.");
                return;
            }

            if (nuovaProiezione.getDataProiezione() == null) {
                System.out.println("  Errore: La data della nuova proiezione non è valida.");
                return;
            }

            LocalDate oggi = LocalDate.now();
            LocalDate dataNuovaObj = nuovaProiezione.getDataProiezione().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (!dataNuovaObj.isAfter(oggi)) {
                System.out.println("  Errore: Impossibile effettuare la modifica.");
                System.out.println("  Motivo: La nuova data non è successiva alla data odierna (" + oggi.format(FMT_ITA) + ").");
                return;
            }

            boolean modificato = FileManager.modificaProiezioneInPrenotazione(idPrenotazione, this.getUsername(), idNuovaProiezione);

            if (modificato) {
                System.out.println("  Prenotazione aggiornata con successo! Nuova data: " + dataNuovaObj.format(FMT_ITA));
            } else {
                System.out.println("  Impossibile modificare: ID prenotazione errato, non tuo, oppure la proiezione passata è già trascorsa.");
            }

        } catch (Exception e) {
            System.out.println("  Errore durante l'elaborazione della modifica: " + e.getMessage());
        }
    }

    /**
     * Visualizza a schermo l'elenco di tutte le prenotazioni attive effettuate
     * dal cliente corrente, leggendole direttamente dal file di persistenza.
     */
    public void visualizzaPrenotazioni() {
        System.out.println("\n==================================================");
        System.out.println("          LE MIE PRENOTAZIONI - @" + getUsername().toUpperCase());
        System.out.println("==================================================");

        try {
            FileManager.stampaPrenotazioniUtente(this.getUsername());
        } catch (Exception e) {
            System.out.println("  Errore durante il recupero delle prenotazioni da file: " + e.getMessage());
        }

        System.out.println("==================================================\n");
    }

    /**
     * Annulla una prenotazione appartenente al cliente corrente rimuovendola
     * direttamente dal file CSV, a patto che la data di proiezione sia strettamente
     * successiva alla data odierna.
     *
     * @param idPrenotazione L'identificativo unico della prenotazione da eliminare.
     */
    public void eliminaPrenotazione(String idPrenotazione) {
        try {
            List<Proiezione> palinsesto = FileManager.caricaPalinsesto();
            List<Prenotazione> prenotazioni = FileManager.caricaPrenotazioni(palinsesto);

            Prenotazione target = null;
            for (Prenotazione p : prenotazioni) {
                if (p.getIdPrenotazione().equalsIgnoreCase(idPrenotazione.trim()) &&
                        p.getUsernameCliente().equalsIgnoreCase(this.getUsername())) {
                    target = p;
                    break;
                }
            }

            if (target == null) {
                System.out.println("  Errore: Prenotazione non trovata o non associata al tuo account.");
                return;
            }

            if (target.getDataProiezione() == null) {
                System.out.println("  Errore: Impossibile determinare la data della proiezione.");
                return;
            }

            LocalDate oggi = LocalDate.now();
            LocalDate dataProiezione = target.getDataProiezione().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (!dataProiezione.isAfter(oggi)) {
                System.out.println("  Errore: Impossibile cancellare la prenotazione. La data della proiezione deve essere successiva a quella odierna.");
                return;
            }

            boolean rimossa = FileManager.rimuoviPrenotazioneDaFile(idPrenotazione, this.getUsername());
            if (rimossa) {
                System.out.println("  Prenotazione annullata con successo dal file CSV.");
            } else {
                System.out.println("  Errore durante la rimozione della prenotazione.");
            }

        } catch (Exception e) {
            System.out.println("  Errore durante l'elaborazione della cancellazione: " + e.getMessage());
        }
    }


    /**
     * Restituisce il valore numerico del menu corrispondente all'operazione di logout per il Cliente.
     *
     * @return L'intero {@code 6}, rappresentante l'opzione di disconnessione dal sistema.
     */
    @Override
    public int getOpzioneLogout() {
        return 6;
    }

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
     * Gestisce la logica di business e le interazioni da riga di comando per il Cliente.
     *
     * @param scelta L'opzione numerica selezionata dal menu.
     */
    @Override
    public void eseguiAzione(int scelta) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                System.out.println("\n--- 1. RICERCA E VISUALIZZAZIONE PROIEZIONI ---");

                List<Proiezione> palinsestoCaso1;
                try {
                    palinsestoCaso1 = FileManager.caricaPalinsesto();
                } catch (IOException e) {
                    System.err.println("  [ERRORE I/O] Impossibile caricare il palinsesto: " + e.getMessage());
                    break;
                }

                if (palinsestoCaso1.isEmpty()) {
                    System.out.println("  Nessuna proiezione presente in archivi.");
                    break;
                }

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

                Double prezzoMinimo = 3.50;
                System.out.print("- Prezzo minimo (EUR, INVIO per 3.50 EUR): ");
                String pMinInput = scanner.nextLine().trim();
                if (!pMinInput.isEmpty()) {
                    try {
                        prezzoMinimo = Double.parseDouble(pMinInput.replace(",", "."));
                    } catch (NumberFormatException e) {
                        System.out.println("  Prezzo minimo non valido, impostato a valore predefinito (3.50 EUR).");
                    }
                }

                Double prezzoMassimo = 9999.0;
                System.out.print("- Prezzo massimo (EUR, INVIO per nessun limite): ");
                String pMaxInput = scanner.nextLine().trim();
                if (!pMaxInput.isEmpty()) {
                    try {
                        prezzoMassimo = Double.parseDouble(pMaxInput.replace(",", "."));
                    } catch (NumberFormatException e) {
                        System.out.println("  Prezzo massimo non valido, impostato a nessun limite.");
                    }
                }


                List<Proiezione> risultatiFiltrati = Utente.cercaProiezione(
                        palinsestoCaso1, titolo, genere, filterInizio, filterFine, prezzoMinimo, prezzoMassimo
                );

                if (risultatiFiltrati.isEmpty()) {
                    System.out.println("  Nessuna proiezione corrisponde ai criteri cercati.");
                    break;
                }

                System.out.println("\n--- RISULTATI TROVATI ---");
                for (int i = 0; i < risultatiFiltrati.size(); i++) {
                    Proiezione p = risultatiFiltrati.get(i);
                    String dataStr = p.getDataProiezione() != null ? FMT_ITA.format(p.getDataProiezione().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) : "N/D";
                    System.out.println((i + 1) + ". " + p.getFilm().getTitolo() + " (" + dataStr + " ore " + p.getOraProiezione() + ")");
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

                List<Proiezione> palinsestoCaso2;
                try {
                    palinsestoCaso2 = FileManager.caricaPalinsesto();
                } catch (IOException e) {
                    System.err.println("  [ERRORE I/O] Impossibile accedere al palinsesto su file: " + e.getMessage());
                    break;
                }

                if (palinsestoCaso2.isEmpty()) {
                    System.out.println("  Impossibile effettuare prenotazioni: il palinsesto e' vuoto.");
                    break;
                }

                System.out.print("Digita il titolo del film che vuoi prenotare (INVIO per vederli tutti): ");
                String titoloCercato = scanner.nextLine().trim();

                List<Proiezione> proiezioniTrovate = new ArrayList<>();
                for (Proiezione p : palinsestoCaso2) {
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
                    String dataStr = p.getDataProiezione() != null ? FMT_ITA.format(p.getDataProiezione().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) : "N/D";
                    System.out.println((i + 1) + ". " + p.getFilm().getTitolo() +
                            " | Data: " + dataStr + " ore " + p.getOraProiezione() +
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
                        FileManager.scalaPostoDisponibile(proiezioneScelta.getIdProiezione(), postiRichiesti);
                    }
                } else {
                    System.out.println("  Errore: Non ci sono abbastanza posti disponibili. Posti rimasti: " + proiezioneScelta.getPostiDisponibili());
                }
                break;

            case 3:
                System.out.println("\n--- 3. LE TUE PRENOTAZIONI ---");
                this.visualizzaPrenotazioni();
                break;

            case 4:
                System.out.println("\n--- 4. MODIFICA PRENOTAZIONE (CAMBIO DATA) ---");
                this.modificaPrenotazione(scanner);
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