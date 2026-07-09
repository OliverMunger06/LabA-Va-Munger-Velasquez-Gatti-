package cinemax.Users;

import cinemax.FileManager;
import cinemax.Proiezione;
import cinemax.Film;
import cinemax.Prenotazione;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Cliente extends Utente {

    private static final DateTimeFormatter FMT_ITA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ------------------------------------------------------------------------
    // COSTRUTTORI AGGIORNATI CON L'ORDINE CORRETTO
    // ------------------------------------------------------------------------

    public Cliente(String nome, String cognome, String username, String passwordInChiaro,
                   String dataNascita, String luogoDomicilio) {
        super(nome, cognome, username, passwordInChiaro, dataNascita, luogoDomicilio);
    }

    public Cliente(String nome, String cognome, String username, String passwordHash,
                   String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
        super(nome, cognome, username, passwordHash, dataNascita, luogoDomicilio, isAlreadyHashed);
    }

    /**
     * Funzionalità di inserimento di una prenotazione
     */
    public void creaPrenotazione(Proiezione proiezione, List<Prenotazione> databasePrenotazioni) {
        if (proiezione.getPostiDisponibili() <= 0) {
            System.out.println("  Errore: Ci dispiace, la sala è al completo per questa proiezione.");
            return;
        }

        if (proiezione.prenotaPosto()) {
            // 🔥 AGGIORNATO: Passa 'this' per fornire nome, cognome, username e passwordHash
            Prenotazione nuovaPrenotazione = new Prenotazione(this, proiezione);
            databasePrenotazioni.add(nuovaPrenotazione);

            System.out.println("  Spettacolo prenotato con successo!");
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
            System.out.println("  Errore: Prenotazione non trovata o permessi insufficienti.");
            return;
        }

        Proiezione vecchiaProiezione = prenotazioneTrovata.getFilmProiezione();
        LocalDate oggi = LocalDate.now();

        try {
            LocalDate dataVecchia = LocalDate.parse(vecchiaProiezione.getDataProiezione(), FMT_ITA);
            LocalDate dataNuova = LocalDate.parse(nuovaProiezione.getDataProiezione(), FMT_ITA);

            if (!dataVecchia.isAfter(oggi) || !dataNuova.isAfter(oggi)) {
                System.out.println("  Errore: Puoi modificare solo prenotazioni future con proiezioni future (Data odierna: " + oggi.format(FMT_ITA) + ").");
                return;
            }
        } catch (DateTimeParseException e) {
            System.out.println("  Errore interno nel parsing delle date del database.");
            return;
        }

        if (nuovaProiezione.getPostiDisponibili() <= 0) {
            System.out.println("  Impossibile spostare: la nuova proiezione è esaurita.");
            return;
        }

        if (nuovaProiezione.prenotaPosto()) {
            vecchiaProiezione.liberaPosto();
            prenotazioneTrovata.setFilmProiezione(nuovaProiezione);
            System.out.println("  Prenotazione spostata con successo sul film: " + nuovaProiezione.getFilm().getTitolo());
        }
    }

    /**
     * Funzionalità di cancellazione di una prenotazione
     */
    public void eliminaPrenotazione(String idPrenotazione, List<Prenotazione> databasePrenotazioni, List<Utente> databaseUtenti) {
        Iterator<Prenotazione> iterator = databasePrenotazioni.iterator();
        LocalDate oggi = LocalDate.now();
        boolean trovataERimossa = false;

        while (iterator.hasNext()) {
            Prenotazione p = iterator.next();

            if (p.getIdPrenotazione().equalsIgnoreCase(idPrenotazione) && p.getUsernameCliente().equalsIgnoreCase(this.getUsername())) {
                try {
                    LocalDate dataProiezione = LocalDate.parse(p.getFilmProiezione().getDataProiezione(), FMT_ITA);

                    if (!dataProiezione.isAfter(oggi)) {
                        System.out.println("  Errore CineMax: Puoi cancellare solo prenotazioni di spettacoli futuri (Successivi a: " + oggi.format(FMT_ITA) + "). Non puoi cancellare spettacoli passati o di oggi.");
                        return;
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("  Errore nel controllo della data dello spettacolo.");
                    return;
                }

                p.getFilmProiezione().liberaPosto();
                iterator.remove();
                trovataERimossa = true;
                System.out.println("  Prenotazione annullata con successo. Il posto è stato liberato.");

                try {
                    FileManager.salvaPrenotazioni(databasePrenotazioni, databaseUtenti);
                } catch (Exception e) {
                    System.out.println("⚠️ Avviso: Errore durante l'aggiornamento del file delle prenotazioni.");
                }
                break;
            }
        }

        if (!trovataERimossa) {
            System.out.println("  Errore: Prenotazione non trovata nel tuo archivio o ID errato.");
        }
    }

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

    public void eseguiAzione(int scelta, List<Prenotazione> databasePrenotazioni, List<Proiezione> palinsesto, List<Utente> databaseUtenti) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                System.out.println("\n--- 1. RICERCA E VISUALIZZAZIONE PROIEZIONI ---");

                System.out.print("• Titolo film (INVIO per tutti): ");
                String titolo = scanner.nextLine().trim();

                System.out.print("• Genere/Tipologia (INVIO per tutti): ");
                String genere = scanner.nextLine().trim();

                System.out.print("• Data inizio intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                String dataInizioStr = scanner.nextLine().trim();

                System.out.print("• Data fine intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                String dataFineStr = scanner.nextLine().trim();

                LocalDate filterInizio = null;
                LocalDate filterFine = null;

                try {
                    if (!dataInizioStr.isEmpty()) filterInizio = LocalDate.parse(dataInizioStr, FMT_ITA);
                    if (!dataFineStr.isEmpty()) filterFine = LocalDate.parse(dataFineStr, FMT_ITA);
                } catch (DateTimeParseException e) {
                    System.out.println(" Errore: Uno o entrambi i formati data inseriti non sono validi (usa gg/mm/aaaa).");
                    break;
                }

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
                    System.out.println(" Nessuna proiezione corrisponde ai criteri cercati.");
                    break;
                }

                System.out.println("\n--- RISULTATI TROVATI (Seleziona un numero) ---");
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
                        System.out.println(" Numero non valido.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println(" Errore: Inserisci un numero valido.");
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
                    System.out.println(" Nessuno spettacolo trovato per il titolo inserito.");
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
                        System.out.println(" Numero di selezione non valido. Riprova.");
                    } catch (NumberFormatException e) {
                        System.out.println(" Errore: Inserisci un numero intero valido.");
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
                        if (postiRichiesti == 0) {
                            System.out.println("Operazione annullata.");
                            break;
                        }
                        if (postiRichiesti > 0) {
                            break;
                        } else {
                            System.out.println(" Errore: Il numero di posti deve essere maggiore di zero.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(" Errore: Non hai inserito un numero intero! Riprova.");
                    }
                }

                if (postiRichiesti == 0) {
                    break;
                }

                if (postiRichiesti <= proiezioneScelta.getPostiDisponibili()) {
                    for (int k = 0; k < postiRichiesti; k++) {
                        proiezioneScelta.prenotaPosto();

                        // 🔥 AGGIORNATO: Anche qui passiamo 'this' invece di 'this.getUsername()'
                        Prenotazione nuova = new Prenotazione(this, proiezioneScelta);
                        databasePrenotazioni.add(nuova);

                        System.out.println(" Posto " + (k + 1) + " prenotato! Codice Biglietto: " + nuova.getIdPrenotazione());
                    }
                    System.out.println(" Prenotazione di " + postiRichiesti + " posti completata!");

                    try {
                        FileManager.salvaPrenotazioni(databasePrenotazioni, databaseUtenti);
                        FileManager.salvaPalinsesto(palinsesto);
                    } catch (Exception e) {
                        System.out.println("⚠️ Errore durante il salvataggio dei file.");
                    }
                } else {
                    System.out.println(" Errore: Non ci sono abbastanza posti disponibili. Posti rimasti: " + proiezioneScelta.getPostiDisponibili());
                }
                break;

            case 3:
                System.out.println("\n--- 3. LE TUE PRENOTAZIONI ---");
                List<Prenotazione> miePrenotazioni = new ArrayList<>();
                for (Prenotazione p : databasePrenotazioni) {
                    if (p.getUsernameCliente().equalsIgnoreCase(this.getUsername())) {
                        miePrenotazioni.add(p);
                    }
                }
                this.visualizzaPrenotazioni(miePrenotazioni);
                break;

            case 4:
                System.out.println("\n--- 4. MODIFICA PRENOTAZIONE (CAMBIO DATA) ---");
                List<Prenotazione> suePrenotazioniMod = new ArrayList<>();
                for (Prenotazione p : databasePrenotazioni) {
                    if (p.getUsernameCliente().equalsIgnoreCase(this.getUsername())) {
                        suePrenotazioniMod.add(p);
                    }
                }
                this.visualizzaPrenotazioni(suePrenotazioniMod);

                System.out.print("Inserisci l'ID della prenotazione da spostare: ");
                String idPrenotazioneMod = scanner.nextLine().trim();

                boolean prenotazionePropriaMod = false;
                for (Prenotazione pren : suePrenotazioniMod) {
                    if (pren.getIdPrenotazione().equalsIgnoreCase(idPrenotazioneMod)) {
                        prenotazionePropriaMod = true;
                        break;
                    }
                }

                if (!prenotazionePropriaMod) {
                    System.out.println(" Errore: Non puoi modificare questa prenotazione (ID errato o non tuo).");
                    break;
                }

                System.out.print("Inserisci l'ID della NUOVA proiezione su cui vuoi spostarti: ");
                String idNuovaProiezione = scanner.nextLine().trim();

                boolean trovatoNuova = false;
                for (Proiezione p : palinsesto) {
                    if (p.getIdProiezione().equalsIgnoreCase(idNuovaProiezione)) {
                        this.modificaPrenotazione(idPrenotazioneMod, p, databasePrenotazioni);
                        trovatoNuova = true;

                        try {
                            FileManager.salvaPrenotazioni(databasePrenotazioni, databaseUtenti);
                            FileManager.salvaPalinsesto(palinsesto);
                        } catch (Exception e) {
                            System.out.println("⚠️ Errore durante il salvataggio dei file.");
                        }
                        break;
                    }
                }

                if (!trovatoNuova) {
                    System.out.println(" Errore: La nuova proiezione selezionata non esiste.");
                }
                break;

            case 5:
                System.out.println("\n--- 5. CANCELLA PRENOTAZIONE ---");
                List<Prenotazione> suePrenotazioniDel = new ArrayList<>();
                for (Prenotazione p : databasePrenotazioni) {
                    if (p.getUsernameCliente().equalsIgnoreCase(this.getUsername())) {
                        suePrenotazioniDel.add(p);
                    }
                }
                this.visualizzaPrenotazioni(suePrenotazioniDel);

                System.out.print("Inserisci l'ID della prenotazione da annullare: ");
                String idPrenotazioneCanc = scanner.nextLine().trim();

                boolean prenotazionePropriaDel = false;
                for (Prenotazione pren : suePrenotazioniDel) {
                    if (pren.getIdPrenotazione().equalsIgnoreCase(idPrenotazioneCanc)) {
                        prenotazionePropriaDel = true;
                        break;
                    }
                }

                if (prenotazionePropriaDel) {
                    this.eliminaPrenotazione(idPrenotazioneCanc, databasePrenotazioni, databaseUtenti);
                    try {
                        FileManager.salvaPalinsesto(palinsesto);
                    } catch (Exception e) { /**/ }
                } else {
                    System.out.println(" Errore: Non puoi eliminare questa prenotazione.");
                }
                break;

            case 6:
                System.out.println("Disconnessione cliente in corso...");
                break;

            default:
                System.out.println("Scelta non valida.");
        }
    }
}