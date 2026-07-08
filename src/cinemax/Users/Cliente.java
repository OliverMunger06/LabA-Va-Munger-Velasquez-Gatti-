package cinemax.Users;

import cinemax.FileManager;
import cinemax.Proiezione;
import cinemax.Film;
import cinemax.Prenotazione;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Cliente extends Utente {

    // ------------------------------------------------------------------------
    // COSTRUTTORI (Overloading)
    // ------------------------------------------------------------------------

    /**
     * COSTRUTTORE 1: Usato per la REGISTRAZIONE di un nuovo cliente.
     */
    public Cliente(String username, String passwordInChiaro, String nome, String cognome,
                   String dataNascita, String luogoDomicilio) {
        super(username, passwordInChiaro, nome, cognome, dataNascita, luogoDomicilio);
    }

    /**
     * COSTRUTTORE 2: Usato dal FileManager per il CARICAMENTO dal file utenti.csv.
     */
    public Cliente(String username, String passwordHash, String nome, String cognome,
                   String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
        super(username, passwordHash, nome, cognome, dataNascita, luogoDomicilio, isAlreadyHashed);
    }


    /**
     * Funzionalità di inserimento di una prenotazione
     */
    public void creaPrenotazione(Proiezione proiezione, List<Prenotazione> databasePrenotazioni) {
        // Verifica che il numero di posti richiesti (1 in questo caso) sia minore dei disponibili
        if (proiezione.getPostiDisponibili() <= 0) {
            System.out.println("❌ Errore: Ci dispiace, la sala è al completo per questa proiezione.");
            return;
        }

        // Tenta di scalare un posto dalla sala
        if (proiezione.prenotaPosto()) {
            Prenotazione nuovaPrenotazione = new Prenotazione(this.getUsername(), proiezione);
            databasePrenotazioni.add(nuovaPrenotazione);

            System.out.println("🎉 Spettacolo prenotato con successo!");
            System.out.println("ID Biglietto: " + nuovaPrenotazione.getIdPrenotazione() +
                    " | Codice di Sicurezza QR: " + nuovaPrenotazione.getCodiceBiglietto());
        }
    }

    /**
     * Funzionalità di visualizzazione delle proprie prenotazioni
     */
    public List<Prenotazione> visualizzaPrenotazioni(List<Prenotazione> databasePrenotazioni) {
        List<Prenotazione> miePrenotazioni = new ArrayList<>();
        System.out.println("\n--- Riepilogo Prenotazioni di @" + getUsername() + " ---");

        for (Prenotazione p : databasePrenotazioni) {
            if (p.getUsernameCliente().equals(this.getUsername())) {
                miePrenotazioni.add(p);

                Proiezione proiezione = p.getFilmProiezione();
                Film film = proiezione.getFilm();

                System.out.println("ID: [" + p.getIdPrenotazione() + "] " +
                        "Film: " + film.getTitolo() +
                        " | Data: " + proiezione.getDataProiezione() +
                        " | Ora: " + proiezione.getOraProiezione() +
                        " | Pagamento: €" + proiezione.getPrezzoBiglietto());
            }
        }

        if (miePrenotazioni.isEmpty()) {
            System.out.println("Non hai prenotazioni attive al momento.");
        }
        return miePrenotazioni;
    }

    /**
     * Funzionalità di modifica (cambio data) di una prenotazione
     * Vincolo: sia la vecchia che la nuova data devono essere successive alla data odierna.
     */
    public void modificaPrenotazione(String idPrenotazione, Proiezione nuovaProiezione, List<Prenotazione> databasePrenotazioni) {
        Prenotazione prenotazioneTrovata = null;

        for (Prenotazione p : databasePrenotazioni) {
            if (p.getIdPrenotazione().equals(idPrenotazione) && p.getUsernameCliente().equals(this.getUsername())) {
                prenotazioneTrovata = p;
                break;
            }
        }

        if (prenotazioneTrovata == null) {
            System.out.println("❌ Errore: Prenotazione non trovata o permessi insufficienti.");
            return;
        }

        Proiezione vecchiaProiezione = prenotazioneTrovata.getFilmProiezione();
        LocalDate oggi = LocalDate.now();

        // Parsing delle date delle proiezioni per il controllo
        LocalDate dataVecchia = LocalDate.parse(vecchiaProiezione.getDataProiezione());
        LocalDate dataNuova = LocalDate.parse(nuovaProiezione.getDataProiezione());

        // Controllo vincolo: entrambe le date devono essere successive a oggi
        if (!dataVecchia.isAfter(oggi) || !dataNuova.isAfter(oggi)) {
            System.out.println("❌ Errore: Puoi modificare solo prenotazioni future con proiezioni future (Data odierna: " + oggi + ").");
            return;
        }

        if (nuovaProiezione.getPostiDisponibili() <= 0) {
            System.out.println("❌ Impossibile spostare: la nuova proiezione è esaurita.");
            return;
        }

        // Tenta lo spostamento
        if (nuovaProiezione.prenotaPosto()) {
            vecchiaProiezione.liberaPosto();
            prenotazioneTrovata.setFilmProiezione(nuovaProiezione);
            System.out.println("🔄 Prenotazione spostata con successo sul film: " + nuovaProiezione.getFilm().getTitolo());
        }
    }

    /**
     * Funzionalità di cancellazione di una prenotazione
     * Vincolo : la data di proiezione deve essere precedente alla data odierna.
     */
    public void eliminaPrenotazione(String idPrenotazione, List<Prenotazione> databasePrenotazioni) {
        Iterator<Prenotazione> iterator = databasePrenotazioni.iterator();
        LocalDate oggi = LocalDate.now(); // Cattura la data reale di oggi (es. 2026-07-08)
        boolean trovataERimossa = false;

        while (iterator.hasNext()) {
            Prenotazione p = iterator.next();

            // Verifica che la prenotazione appartenga al cliente loggato e coincida con l'ID inserito
            if (p.getIdPrenotazione().equalsIgnoreCase(idPrenotazione) && p.getUsernameCliente().equalsIgnoreCase(this.getUsername())) {
                LocalDate dataProiezione = LocalDate.parse(p.getFilmProiezione().getDataProiezione());

                // APPLICAZIONE VINCOLO DA SPECIFICA: deve essere precedente a oggi
                if (!dataProiezione.isBefore(oggi)) {
                    System.out.println("❌ Errore da Specifica CineMax: Puoi cancellare/archiviare solo prenotazioni di film già passati (Antecedenti a: " + oggi + ").");
                    return;
                }

                // Se superiamo il controllo, liberiamo il posto e rimuoviamo la prenotazione
                p.getFilmProiezione().liberaPosto();
                iterator.remove(); // Rimozione sicura dall'ArrayList
                trovataERimossa = true;
                System.out.println("🗑️ Prenotazione passata rimossa/archiviata con successo.");

                // Salva le modifiche su file per mantenere la persistenza
                try {
                    FileManager.salvaPrenotazioni(databasePrenotazioni);
                } catch (Exception e) {
                    System.out.println("⚠️ Avviso: Errore durante l'aggiornamento del file delle prenotazioni.");
                }
                break;
            }
        }

        if (!trovataERimossa) {
            System.out.println("❌ Errore: Prenotazione non trovata nel tuo archivio o ID errato.");
        }
    }

    // ------------------------------------------------------------------------
    // IMPLEMENTAZIONE METODI ASTRATTI
    // ------------------------------------------------------------------------

    @Override
    public void mostraMenu() {
        System.out.println("\n=== AREA PERSONALE CLIENTE: " + getNome().toUpperCase() + " ===");
        System.out.println("1. Cerca proiezioni ");
        System.out.println("2. Inserisci una nuova prenotazione");
        System.out.println("3. Visualizza le tue prenotazioni attive");
        System.out.println("4. Modifica le tue prenotazioni (Cambio Data)");
        System.out.println("5. Cancella una prenotazione");
        System.out.println("6. Logout");
    }

    public void eseguiAzione(int scelta, List<Prenotazione> databasePrenotazioni, List<Proiezione> palinsesto) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                System.out.println("\n--- 1. RICERCA E VISUALIZZAZIONE PROIEZIONI ---");

                // 1. ACQUISIZIONE FILTRI (Stringhe vuote "" se l'utente salta con INVIO)
                System.out.print("• Titolo film (INVIO per tutti): ");
                String titolo = scanner.nextLine().trim();

                System.out.print("• Genere/Tipologia (INVIO per tutti): ");
                String genere = scanner.nextLine().trim();

                System.out.print("• Data inizio intervallo (aaaa-mm-gg, INVIO per nessuna): ");
                String dataInizioStr = scanner.nextLine().trim();

                System.out.print("• Data fine intervallo (aaaa-mm-gg, INVIO per nessuna): ");
                String dataFineStr = scanner.nextLine().trim();

                // Gestione filtri prezzo con valori di default estremi (senza null)
                double prezzoMinimo = 0.0;
                System.out.print("• Prezzo minimo (€, INVIO per 0€): ");
                String pMinInput = scanner.nextLine().trim();
                if (!pMinInput.isEmpty()) {
                    prezzoMinimo = Double.parseDouble(pMinInput);
                }

                double prezzoMassimo = 9999.0;
                System.out.print("• Prezzo massimo (€, INVIO per nessun limite): ");
                String pMaxInput = scanner.nextLine().trim();
                if (!pMaxInput.isEmpty()) {
                    prezzoMassimo = Double.parseDouble(pMaxInput);
                }

                // 2. ESECUZIONE DELLA RICERCA (Logica interna priva di null)
                List<Proiezione> risultatiFiltrati = new ArrayList<>();
                for (Proiezione p : palinsesto) {
                    Film f = p.getFilm();

                    // Filtro Titolo (parziale e case-insensitive)
                    if (!titolo.isEmpty() && !f.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                        continue;
                    }

                    // Filtro Genere
                    if (!genere.isEmpty() && !f.getGenere().equalsIgnoreCase(genere)) {
                        continue;
                    }

                    // Filtro Intervallo di Date (Confronto stringhe ISO aaaa-mm-gg)
                    String dataP = p.getDataProiezione();
                    if (!dataInizioStr.isEmpty() && dataP.compareTo(dataInizioStr) < 0) {
                        continue; // Se la data della proiezione è precedente a dataInizio
                    }
                    if (!dataFineStr.isEmpty() && dataP.compareTo(dataFineStr) > 0) {
                        continue; // Se la data della proiezione è successiva a dataFine
                    }

                    // Filtro Fascia di Costo Biglietto
                    if (p.getPrezzoBiglietto() < prezzoMinimo || p.getPrezzoBiglietto() > prezzoMassimo) {
                        continue;
                    }

                    risultatiFiltrati.add(p);
                }

                // Se la ricerca fallisce, interrompiamo il case
                if (risultatiFiltrati.isEmpty()) {
                    System.out.println("❌ Nessuna proiezione corrisponde ai criteri cercati.");
                    break;
                }

                // 3. MOSTRA I RISULTATI NUMERATI PER LA SELEZIONE
                System.out.println("\n--- RISULTATI TROVATI (Seleziona un numero) ---");
                for (int i = 0; i < risultatiFiltrati.size(); i++) {
                    Proiezione p = risultatiFiltrati.get(i);
                    System.out.println((i + 1) + ". " + p.getFilm().getTitolo() + " (" + p.getDataProiezione() + " ore " + p.getOraProiezione() + ")");
                }
                System.out.println("0. Torna al menu");

                // 4. SELEZIONE E VISUALIZZAZIONE IMMEDIATA DOPO LA RICERCA
                System.out.print("\nInserisci il numero della proiezione per vederne i dettagli (0 per annullare): ");
                try {
                    int indiceScelto = Integer.parseInt(scanner.nextLine().trim());

                    if (indiceScelto == 0) {
                        System.out.println("Selezione annullata.");
                    } else if (indiceScelto > 0 && indiceScelto <= risultatiFiltrati.size()) {

                        Proiezione proiezioneSelezionata = risultatiFiltrati.get(indiceScelto - 1);

                        // CHIAMATA ALLA VISUALIZZAZIONE DOPO LA RICERCA
                        Utente.visualizzaProiezione(proiezioneSelezionata);

                        System.out.println("Premi INVIO per tornare al menu principale...");
                        scanner.nextLine();

                    } else {
                        System.out.println("❌ Numero non valido.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Errore: Inserisci un numero valido.");
                }
                break;

            case 2:
                System.out.println("\n--- 2. INSERISCI UNA NUOVA PRENOTAZIONE ---");
                System.out.print("Digita il titolo del film che vuoi prenotare (INVIO per vederli tutti): ");
                String titoloCercato = scanner.nextLine().trim();

                // 1. Fase di ricerca preliminare (richiesta dalla specifica)
                List<Proiezione> proiezioniTrovate = new ArrayList<>();
                for (Proiezione p : palinsesto) {
                    if (titoloCercato.isEmpty() || p.getFilm().getTitolo().toLowerCase().contains(titoloCercato.toLowerCase())) {
                        proiezioniTrovate.add(p);
                    }
                }

                if (proiezioniTrovate.isEmpty()) {
                    System.out.println("❌ Nessuno spettacolo trovato per il titolo inserito.");
                    break;
                }

                // Mostriamo l'elenco dei risultati trovati
                System.out.println("\nSpettacoli disponibili:");
                for (int i = 0; i < proiezioniTrovate.size(); i++) {
                    Proiezione p = proiezioniTrovate.get(i);
                    System.out.println((i + 1) + ". " + p.getFilm().getTitolo() +
                            " | Data: " + p.getDataProiezione() + " ore " + p.getOraProiezione() +
                            " [Posti disponibili: " + p.getPostiDisponibili() + "]");
                }
                System.out.println("0. Annulla l'operazione");

                // 2. Selezione dello spettacolo con controllo robusto
                int sceltaSpettacolo = -1;
                while (true) {
                    System.out.print("\nSeleziona il numero dello spettacolo da prenotare: ");
                    try {
                        sceltaSpettacolo = Integer.parseInt(scanner.nextLine().trim());
                        if (sceltaSpettacolo >= 0 && sceltaSpettacolo <= proiezioniTrovate.size()) {
                            break; // Input valido
                        }
                        System.out.println("❌ Numero di selezione non valido. Riprova.");
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Errore: Inserisci un numero intero valido.");
                    }
                }

                if (sceltaSpettacolo == 0) {
                    System.out.println("Operazione annullata.");
                    break;
                }

                Proiezione proiezioneScelta = proiezioniTrovate.get(sceltaSpettacolo - 1);

                // 3. Richiesta posti con ciclo continuo in caso di errore di digitazione
                int postiRichiesti = 0;
                while (true) {
                    System.out.print("Quanti posti desideri richiedere? (0 per annullare): ");
                    try {
                        postiRichiesti = Integer.parseInt(scanner.nextLine().trim());

                        if (postiRichiesti == 0) {
                            System.out.println("Operazione annullata.");
                            break; // Esce dal ciclo, l'operazione si interromperà sotto
                        }

                        if (postiRichiesti > 0) {
                            break; // Input numerico valido, esce dal ciclo di inserimento
                        } else {
                            System.out.println("❌ Errore: Il numero di posti deve essere maggiore di zero.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Errore: Non hai inserito un numero intero! Usa solo cifre. Riprova.");
                    }
                }

                // Se l'utente ha digitato 0 per uscire dal ciclo posti, interrompiamo il case
                if (postiRichiesti == 0) {
                    break;
                }

                // 4. Controllo vincolo: posti richiesti STRETTAMENTE MINORI dei posti disponibili
                if (postiRichiesti < proiezioneScelta.getPostiDisponibili()) {

                    // Generiamo le singole prenotazioni in base ai posti richiesti
                    for (int k = 0; k < postiRichiesti; k++) {
                        proiezioneScelta.prenotaPosto(); // Scala il posto in memoria riducendo la disponibilità
                        Prenotazione nuova = new Prenotazione(this.getUsername(), proiezioneScelta); // Genera codice univoco
                        databasePrenotazioni.add(nuova);

                        System.out.println("🎉 Posto " + (k + 1) + " prenotato! Codice Biglietto Univoco: " + nuova.getIdPrenotazione());
                    }
                    System.out.println("✅ Prenotazione di " + postiRichiesti + " posti completata con successo!");

                } else {
                    // Se i posti richiesti sono maggiori o UGUALI a quelli rimasti, l'operazione fallisce
                    System.out.println("❌ Errore: Il numero di posti richiesti deve essere MINORE dei posti disponibili (" + proiezioneScelta.getPostiDisponibili() + ").");
                }
                break;

            case 3:
                this.visualizzaPrenotazioni(databasePrenotazioni);
                break;

            case 4:
                System.out.println("\n--- 4. MODIFICA PRENOTAZIONE (CAMBIO DATA) ---");
                this.visualizzaPrenotazioni(databasePrenotazioni);
                System.out.print("Inserisci l'ID della prenotazione da spostare: ");
                String idPrenotazioneMod = scanner.nextLine().trim();

                System.out.print("Inserisci l'ID della NUOVA proiezione su cui vuoi spostarti: ");
                String idNuovaProiezione = scanner.nextLine().trim();

                boolean trovatoNuova = false;
                for (Proiezione p : palinsesto) {
                    if (p.getIdProiezione().equalsIgnoreCase(idNuovaProiezione)) {
                        this.modificaPrenotazione(idPrenotazioneMod, p, databasePrenotazioni);
                        trovatoNuova = true;
                        break;
                    }
                }

                if (!trovatoNuova) {
                    System.out.println("❌ Errore: La nuova proiezione selezionata non esiste.");
                }
                break;

            case 5:
                System.out.println("\n--- 5. CANCELLA PRENOTAZIONE ---");
                this.visualizzaPrenotazioni(databasePrenotazioni);
                System.out.print("Inserisci l'ID della prenotazione da annullare: ");
                String idPrenotazioneCanc = scanner.nextLine().trim();

                this.eliminaPrenotazione(idPrenotazioneCanc, databasePrenotazioni);
                break;

            case 6:
                System.out.println("Disconnessione cliente in corso...");
                break;

            default:
                System.out.println("Scelta non valida.");
        }
    }
}