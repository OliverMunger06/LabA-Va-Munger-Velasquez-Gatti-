package cinemax.Users;

import cinemax.Prenotazione;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bigliettaio extends Utente {

    public Bigliettaio(String nome, String cognome, String username, String passwordHash, String dataNascita, String luogoDomicilio, boolean attivo) {
        super(nome, cognome, username, passwordHash, dataNascita, luogoDomicilio, attivo);
    }

    /**
     * Ricerca per Codice Prenotazione (ID) - Invariato e funzionante
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
     * 🔥 AGGIORNATO: Ricerca diretta sui campi interni di Prenotazione.
     * Non serve più fare il doppio ciclo incrociato con la lista Utenti!
     */
    public List<Prenotazione> cercaPerNomeCognome(List<Prenotazione> prenotazioni, String nome, String cognome) {
        List<Prenotazione> risultati = new ArrayList<>();
        String nomeCercato = nome.trim().toLowerCase();
        String cognomeCercato = cognome.trim().toLowerCase();

        for (Prenotazione p : prenotazioni) {
            // Controlliamo direttamente i dati salvati nella prenotazione
            String nomeP = p.getNomeCliente() != null ? p.getNomeCliente().toLowerCase() : "";
            String cognomeP = p.getCognomeCliente() != null ? p.getCognomeCliente().toLowerCase() : "";

            if (nomeP.contains(nomeCercato) && cognomeP.contains(cognomeCercato)) {
                risultati.add(p);
            }
        }
        return risultati;
    }

    /**
     * Ricerca per Titolo del Film (anche parziale)
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
     * Ricerca per Intervallo di Date
     */
    public List<Prenotazione> cercaPerIntervalloDate(List<Prenotazione> prenotazioni, LocalDate inizio, LocalDate fine) {
        List<Prenotazione> risultati = new ArrayList<>();
        // Formatter italiano per convertire la stringa dd/MM/yyyy della proiezione in LocalDate
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Prenotazione p : prenotazioni) {
            if (p.getDataStr().equals("N/D") || p.getDataStr().isEmpty()) continue;

            try {
                LocalDate dataSpec = LocalDate.parse(p.getDataStr(), fmt);

                boolean dopoInizio = (inizio == null) || !dataSpec.isBefore(inizio);
                boolean primaFine = (fine == null) || !dataSpec.isAfter(fine);

                if (dopoInizio && primaFine) {
                    risultati.add(p);
                }
            } catch (java.time.format.DateTimeParseException e) {
                continue; // Salta record corrotti nel file
            }
        }
        return risultati;
    }

    /**
     * 🔥 AGGIORNATO: Sfrutta nome e cognome nativi della prenotazione
     */
    public void visualizzaPrenotazione(Prenotazione p) {
        int numeroBiglietti = 1;
        double costoUnitario = p.getFilmProiezione() != null ? p.getFilmProiezione().getPrezzoBiglietto() : 0.0;
        double costoTotale = costoUnitario * numeroBiglietti;

        System.out.println("\n=============================================");
        System.out.println("       DETTAGLIO FISCALE PRENOTAZIONE        ");
        System.out.println("=============================================");
        System.out.println("• Codice Prenotazione: " + p.getIdPrenotazione());
        // Lettura diretta senza cercare nel database utenti
        System.out.println("• Intestatario:        " + p.getNomeCliente() + " " + p.getCognomeCliente() + " (@" + p.getUsernameCliente() + ")");
        System.out.println("• Spettacolo del:      " + p.getDataStr() + " alle ore " + p.getOraStr());
        System.out.println("---------------------------------------------");
        System.out.println("• Quantità Biglietti:  " + numeroBiglietti);
        System.out.printf("• Costo Unitario:      %.2f €\n", costoUnitario);
        System.out.printf("• COSTO TOTALE:        %.2f €\n", costoTotale);
        System.out.println("=============================================");
    }

    @Override
    public void mostraMenu() {
        System.out.println("\n=== AREA PERSONALE BIGLIETTAIO: " + getNome().toUpperCase() + " ===");
        System.out.println("1. Visualizza prenotazioni nella data odierna");
        System.out.println("2. Cercare una prenotazione (Criteri multipli)");
        System.out.println("3. Logout");
    }

    public void eseguiAzione(int scelta, List<Prenotazione> databasePrenotazioni, List<Utente> databaseUtenti) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                System.out.println("\n--- 1. PRENOTAZIONI NELLA DATA ODIERNA ---");
                java.time.LocalDate oggi = java.time.LocalDate.now();
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String oggiFormattato = oggi.format(formatter);

                System.out.println("Data di oggi rilevata (formato archivio): " + oggiFormattato);

                List<Prenotazione> prenotazioniOggi = new ArrayList<>();
                for (Prenotazione p : databasePrenotazioni) {
                    String dataProiezione = p.getFilmProiezione() != null ? p.getFilmProiezione().getDataProiezione() : "";
                    if (dataProiezione.equals(oggiFormattato)) {
                        prenotazioniOggi.add(p);
                    }
                }

                if (prenotazioniOggi.isEmpty()) {
                    System.out.println(" Nessuna prenotazione trovata per la data odierna.");
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
                System.out.println(" Scelta non valida.");
        }
    }

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
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
                // Utilizza il nuovo metodo ottimizzato e diretto
                risultati = cercaPerNomeCognome(prenotazioni, nomeCercato, cognomeCercato);
                break;

            case "c":
                System.out.print("• Titolo del film (anche parziale): ");
                String titoloCercato = scanner.nextLine().trim();
                risultati = cercaPerTitoloFilm(prenotazioni, titoloCercato);
                break;

            case "d":
                java.time.LocalDate dataInizio = null;
                java.time.LocalDate dataFine = null;

                System.out.print("• Data inizio intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                String inizioInput = scanner.nextLine().trim();
                if (!inizioInput.isEmpty()) {
                    try {
                        dataInizio = java.time.LocalDate.parse(inizioInput, fmt);
                    } catch (java.time.format.DateTimeParseException e) {
                        System.out.println(" Errore: Formato data inizio non valido!");
                        return null;
                    }
                }

                System.out.print("• Data fine intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                String fineInput = scanner.nextLine().trim();
                if (!fineInput.isEmpty()) {
                    try {
                        dataFine = java.time.LocalDate.parse(fineInput, fmt);
                    } catch (java.time.format.DateTimeParseException e) {
                        System.out.println(" Errore: Formato data fine non valido!");
                        return null;
                    }
                }

                risultati = cercaPerIntervalloDate(prenotazioni, dataInizio, dataFine);
                break;

            default:
                System.out.println(" Criterio di ricerca non valido.");
                return null;
        }

        if (risultati.isEmpty()) {
            System.out.println(" Nessuna prenotazione corrisponde ai criteri cercati.");
        } else {
            System.out.println(" Trovate " + risultati.size() + " prenotazioni corrispondenti.");
        }
        return risultati;
    }

    private void mostraEResettaSelezione(List<Prenotazione> risultati, Scanner scanner) {
        if (risultati.isEmpty()) {
            System.out.println("Nessuna prenotazione da mostrare.");
            return;
        }

        System.out.println("\n--- RISULTATI FILTRATI ---");
        for (int i = 0; i < risultati.size(); i++) {
            Prenotazione p = risultati.get(i);
            // Mostra nome e cognome reali nell'elenco puntato
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
                System.out.println(" Selezione fuori range.");
            }
        } catch (NumberFormatException e) {
            System.out.println(" Errore: Inserisci un indice numerico valido.");
        }
    }
}