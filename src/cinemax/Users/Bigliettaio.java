package cinemax.Users;

import cinemax.gestione.Prenotazione;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Rappresenta l'operatore di cassa/biglietteria all'interno del sistema Cinemax.
 * <p>
 * Estende la classe {@link Utente} fornendo le funzionalita' necessarie per la ricerca
 * e la verifica delle prenotazioni tramite molteplici criteri (codice, cliente, film, date)
 * e la visualizzazione del relativo dettaglio fiscale.
 * </p>
 *
 * @author Cinemax Team
 * @version 1.0
 */
public class Bigliettaio extends Utente {

    /**
     * Costruisce un nuovo operatore {@code Bigliettaio} con i dati anagrafici e le credenziali specificate.
     *
     * @param nome           Il nome del bigliettaio.
     * @param cognome        Il cognome del bigliettaio.
     * @param username       Lo username per l'accesso al sistema.
     * @param passwordHash   L'hash della password di sicurezza.
     * @param dataNascita    La data di nascita in formato testo.
     * @param luogoDomicilio Il luogo di domicilio.
     * @param attivo         Lo stato di attivazione dell'account (true/false).
     */
    public Bigliettaio(String nome, String cognome, String username, String passwordHash, String dataNascita, String luogoDomicilio, boolean attivo) {
        super(nome, cognome, username, passwordHash, dataNascita, luogoDomicilio, attivo);
    }

    /**
     * Ricerca le prenotazioni corrispondenti a uno specifico codice univoco (ID).
     *
     * @param prenotazioni La lista di prenotazioni in cui effettuare la ricerca.
     * @param codice       L'ID della prenotazione da cercare.
     * @return Una lista di {@link Prenotazione} contenente i match trovati.
     */
    public List<Prenotazione> cercaPerCodice(List<Prenotazione> prenotazioni, String codice) {
        List<Prenotazione> risultati = new ArrayList<>();
        for (Prenotazione p : prenotazioni) {
            if (p.getIdPrenotazione().equalsIgnoreCase(codice.trim())) {
                risultati.add(p);
            }
        }
        return risultati;
    }

    /**
     * Ricerca le prenotazioni filtrate in base al nome e cognome del cliente intestatario.
     *
     * @param prenotazioni La lista di prenotazioni in cui effettuare la ricerca.
     * @param nome         Il nome (o parte del nome) del cliente.
     * @param cognome      Il cognome (o parte del cognome) del cliente.
     * @return Una lista di {@link Prenotazione} corrispondenti al cliente.
     */
    public List<Prenotazione> cercaPerNomeCognome(List<Prenotazione> prenotazioni, String nome, String cognome) {
        List<Prenotazione> risultati = new ArrayList<>();
        String nomeCercato = nome.trim().toLowerCase();
        String cognomeCercato = cognome.trim().toLowerCase();

        for (Prenotazione p : prenotazioni) {
            String nomeP = p.getNomeCliente() != null ? p.getNomeCliente().toLowerCase() : "";
            String cognomeP = p.getCognomeCliente() != null ? p.getCognomeCliente().toLowerCase() : "";

            if (nomeP.contains(nomeCercato) && cognomeP.contains(cognomeCercato)) {
                risultati.add(p);
            }
        }
        return risultati;
    }

    /**
     * Ricerca le prenotazioni filtrando per titolo (anche parziale) del film.
     *
     * @param prenotazioni   La lista delle prenotazioni da filtrare.
     * @param titoloParziale La stringa da cercare all'interno dei titoli dei film.
     * @return Una lista di {@link Prenotazione} associate al film ricercato.
     */
    public List<Prenotazione> cercaPerTitoloFilm(List<Prenotazione> prenotazioni, String titoloParziale) {
        List<Prenotazione> risultati = new ArrayList<>();
        for (Prenotazione p : prenotazioni) {
            if (p.getTitoloFilm().toLowerCase().contains(titoloParziale.toLowerCase().trim())) {
                risultati.add(p);
            }
        }
        return risultati;
    }

    /**
     * Ricerca le prenotazioni comprese all'interno di un determinato intervallo temporale.
     *
     * @param prenotazioni La lista delle prenotazioni da filtrare.
     * @param inizio       La data iniziale dell'intervallo (se {@code null}, include tutte le date passate).
     * @param fine         La data finale dell'intervallo (se {@code null}, include tutte le date future).
     * @return Una lista di {@link Prenotazione} ricadenti nell'intervallo indicato.
     */
    public List<Prenotazione> cercaPerIntervalloDate(List<Prenotazione> prenotazioni, LocalDate inizio, LocalDate fine) {
        List<Prenotazione> risultati = new ArrayList<>();

        for (Prenotazione p : prenotazioni) {
            if (p.getDataStr().equals("N/D") || p.getDataStr().isEmpty()) continue;

            try {
                LocalDate dataSpec = LocalDate.parse(p.getDataStr(), FMT_ITA);

                boolean dopoInizio = (inizio == null) || !dataSpec.isBefore(inizio);
                boolean primaFine = (fine == null) || !dataSpec.isAfter(fine);

                if (dopoInizio && primaFine) {
                    risultati.add(p);
                }
            } catch (DateTimeParseException e) {
                continue; // Salta record corrotti nel file CSV
            }
        }
        return risultati;
    }

    /**
     * Stampa a schermo il prospetto ed il dettaglio fiscale di una specifica prenotazione.
     *
     * @param p La {@link Prenotazione} di cui mostrare i dettagli.
     */
    public void visualizzaPrenotazione(Prenotazione p) {
        int numeroBiglietti = 1;
        double costoUnitario = p.getFilmProiezione() != null ? p.getFilmProiezione().getPrezzoBiglietto() : 0.0;
        double costoTotale = costoUnitario * numeroBiglietti;

        System.out.println("\n=============================================");
        System.out.println("       DETTAGLIO FISCALE PRENOTAZIONE        ");
        System.out.println("=============================================");
        System.out.println("• Codice Prenotazione: " + p.getIdPrenotazione());
        System.out.println("• Intestatario:        " + p.getNomeCliente() + " " + p.getCognomeCliente() + " (@" + p.getUsernameCliente() + ")");
        System.out.println("• Spettacolo del:      " + p.getDataStr() + " alle ore " + p.getOraStr());
        System.out.println("---------------------------------------------");
        System.out.println("• Quantità Biglietti:  " + numeroBiglietti);
        System.out.printf("• Costo Unitario:      %.2f €\n", costoUnitario);
        System.out.printf("• COSTO TOTALE:        %.2f €\n", costoTotale);
        System.out.println("=============================================");
    }

    /**
     * Mostra le opzioni disponibili nel menu testuale dell'area personale del bigliettaio.
     */
    @Override
    public void mostraMenu() {
        System.out.println("\n=== AREA PERSONALE BIGLIETTAIO: " + getNome().toUpperCase() + " ===");
        System.out.println("1. Visualizza prenotazioni nella data odierna");
        System.out.println("2. Cercare una prenotazione (Criteri multipli)");
        System.out.println("3. Logout");
    }

    /**
     * Gestisce l'esecuzione dell'operazione scelta dall'utente a terminale.
     *
     * @param scelta               L'opzione numerica selezionata nel menu.
     * @param databasePrenotazioni L'elenco complessivo delle prenotazioni registrate.
     * @param databaseUtenti       L'elenco complessivo degli utenti del sistema.
     */
    public void eseguiAzione(int scelta, List<Prenotazione> databasePrenotazioni, List<Utente> databaseUtenti) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                System.out.println("\n--- 1. PRENOTAZIONI NELLA DATA ODIERNA ---");
                LocalDate oggi = LocalDate.now();
                String oggiFormattato = oggi.format(FMT_ITA);

                System.out.println("Data odierna di sistema: " + oggiFormattato);

                List<Prenotazione> prenotazioniOggi = new ArrayList<>();
                for (Prenotazione p : databasePrenotazioni) {
                    if (p.getDataStr().equals(oggiFormattato)) {
                        prenotazioniOggi.add(p);
                    }
                }

                if (prenotazioniOggi.isEmpty()) {
                    System.out.println("  Nessuna prenotazione trovata per la data odierna.");
                } else {
                    mostraEResettaSelezione(prenotazioniOggi, scanner);
                }
                break;

            case 2:
                List<Prenotazione> risultatiRicerca = cercaPrenotazione(databasePrenotazioni, databaseUtenti, scanner);
                if (risultatiRicerca != null && !risultatiRicerca.isEmpty()) {
                    mostraEResettaSelezione(risultatiRicerca, scanner);
                }
                break;

            case 3:
                System.out.println("Disconnessione bigliettaio in corso...");
                break;

            default:
                System.out.println("  Scelta non valida.");
        }
    }

    /**
     * Sottomenu guida per l'acquisizione dei criteri di ricerca forniti dall'operatore.
     *
     * @param prenotazioni La lista completa delle prenotazioni da interrogare.
     * @param utenti       La lista degli utenti del sistema.
     * @param scanner      L'oggetto {@link Scanner} per la lettura dell'input.
     * @return La lista delle prenotazioni corrispondenti al criterio scelto, oppure {@code null} in caso di errori di input.
     */
    private List<Prenotazione> cercaPrenotazione(List<Prenotazione> prenotazioni, List<Utente> utenti, Scanner scanner) {
        System.out.println("\n--- 2. CERCA UNA PRENOTAZIONE ---");
        System.out.println("Seleziona il criterio di ricerca:");
        System.out.println("a. Per codice prenotazione");
        System.out.println("b. Per nome e cognome del cliente");
        System.out.println("c. Per titolo (anche parziale) del film");
        System.out.println("d. Per intervallo di date");
        System.out.print("Scegli un'opzione (a-d): ");
        String criterio = scanner.nextLine().trim().toLowerCase();

        List<Prenotazione> risultati = new ArrayList<>();

        switch (criterio) {
            case "a":
                System.out.print("• Inserisci Codice Prenotazione (ID): ");
                String codiceCercato = scanner.nextLine().trim();
                risultati = cercaPerCodice(prenotazioni, codiceCercato);
                break;

            case "b":
                System.out.print("• Nome cliente: ");
                String nomeCercato = scanner.nextLine().trim();
                System.out.print("• Cognome cliente: ");
                String cognomeCercato = scanner.nextLine().trim();
                risultati = cercaPerNomeCognome(prenotazioni, nomeCercato, cognomeCercato);
                break;

            case "c":
                System.out.print("• Titolo del film (anche parziale): ");
                String titoloCercato = scanner.nextLine().trim();
                risultati = cercaPerTitoloFilm(prenotazioni, titoloCercato);
                break;

            case "d":
                LocalDate dataInizio = null;
                LocalDate dataFine = null;

                System.out.print("• Data inizio intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                String inizioInput = scanner.nextLine().trim();
                if (!inizioInput.isEmpty()) {
                    try {
                        dataInizio = LocalDate.parse(inizioInput, FMT_ITA);
                    } catch (DateTimeParseException e) {
                        System.out.println("  Errore: Formato data inizio non valido!");
                        return null;
                    }
                }

                System.out.print("• Data fine intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                String fineInput = scanner.nextLine().trim();
                if (!fineInput.isEmpty()) {
                    try {
                        dataFine = LocalDate.parse(fineInput, FMT_ITA);
                    } catch (DateTimeParseException e) {
                        System.out.println("  Errore: Formato data fine non valido!");
                        return null;
                    }
                }

                risultati = cercaPerIntervalloDate(prenotazioni, dataInizio, dataFine);
                break;

            default:
                System.out.println("  Criterio di ricerca non valido.");
                return null;
        }

        if (risultati.isEmpty()) {
            System.out.println(" Nessuna prenotazione corrisponde ai criteri cercati.");
        } else {
            System.out.println(" Trovate " + risultati.size() + " prenotazioni corrispondenti.");
        }
        return risultati;
    }

    /**
     * Metodo helper privato per reindirizzare al metodo di gestione selezione.
     *
     * @param risultati Lista dei risultati da mostrare.
     * @param scanner   L'oggetto scanner.
     */
    private void montreEResettaSelezione(List<Prenotazione> risultati, Scanner scanner) {
        mostraEResettaSelezione(risultati, scanner);
    }

    /**
     * Mostra l'elenco numerato dei risultati filtrati e permette all'utente di selezionarne
     * uno per visualizzarne il dettaglio fiscale.
     *
     * @param risultati Lista delle prenotazioni trovate.
     * @param scanner   L'oggetto {@link Scanner} per la gestione dell'input da terminale.
     */
    private void mostraEResettaSelezione(List<Prenotazione> risultati, Scanner scanner) {
        if (risultati.isEmpty()) {
            System.out.println("Nessuna prenotazione da mostrare.");
            return;
        }

        System.out.println("\n--- RISULTATI FILTRATI ---");
        for (int i = 0; i < risultati.size(); i++) {
            Prenotazione p = risultati.get(i);
            System.out.println((i + 1) + ". ID: [" + p.getIdPrenotazione() + "] Film: " + p.getTitoloFilm() + " | Cliente: " + p.getNomeCliente() + " " + p.getCognomeCliente());
        }
        System.out.println("0. Torna al menu principale");

        System.out.print("\nInserisci il numero della prenotazione per visualizzare il dettaglio fiscale: ");
        try {
            int indiceScelto = Integer.parseInt(scanner.nextLine().trim());

            if (indiceScelto == 0) {
                System.out.println("Operazione annullata.");
            } else if (indiceScelto > 0 && indiceScelto <= risultati.size()) {
                Prenotazione selezionata = risultati.get(indiceScelto - 1);
                this.visualizzaPrenotazione(selezionata);

                System.out.println("Premi INVIO per continuare...");
                scanner.nextLine();
            } else {
                System.out.println("  Selezione fuori range.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  Errore: Inserisci un indice numerico valido.");
        }
    }
}