package cinemax.controls;

import cinemax.Users.Cliente;
import cinemax.Users.Utente;
import cinemax.gestione.Genere;
import cinemax.gestione.Proiezione;
import cinemax.utils.FileManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;


    /**
     * La classe {@code GestoreMenu} si occupa di gestire l'interfaccia a riga di comando (CLI)
     * per il sistema Cinemax. Gestisce i flussi di interazione principali tra cui la registrazione
     * dei nuovi utenti, le operazioni consentite agli ospiti (Guest) e il processo di autenticazione (Login)
     * con il conseguente avvio delle sessioni di lavoro specifiche per ciascun ruolo.
     *
     * @author Oliver Munger , matricola num. 764208 , VA
     * @author Davide Gatti , matricola num. 765949 , VA
     * @author Davide Noe Velasquez Carpio , matricola num. 765163 , VA
     */
public class GestoreMenu {


private Scanner sc;
private final DateTimeFormatter FMT_ITA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);

    /**
     * Costruisce una nuova istanza di {@code GestoreMenu} associata a uno scanner per la lettura
     * dell'input da console.
     *
     * @param sc lo {@link Scanner} utilizzato per acquisire i dati inseriti dall'utente.
     */
public GestoreMenu(Scanner sc){
    this.sc=sc;
}

    /**
     *  Gestisce il flusso interattivo di registrazione di un nuovo utente (Cliente).
     *
     * Il metodo guida l'utente nell'inserimento dei dati anagrafici (nome, cognome, username,
     * password, data di nascita facoltativa e domicilio), validando ciascun campo in tempo reale.
     * Verifica inoltre l'unicità dell'username tramite il {@link FileManager}, genera l'hash della password
     * e procede alla persistenza dei dati su file creando un'istanza polimorfica di {@link Cliente}.
     * */
    public void registraCliente() {
        System.out.println("\n--- REGISTRAZIONE NUOVO UTENTE ---");

        String nome = "";
        boolean nomeValido = false;
        while (!nomeValido) {
            System.out.print("Inserisci il tuo nome: ");
            nome = sc.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("Errore: il campo 'Nome' non può essere vuoto.");
            } else {
                nomeValido = true;
            }
        }

        String cognome = "";
        boolean cognomeValido = false;
        while (!cognomeValido) {
            System.out.print("Inserisci il tuo cognome: ");
            cognome = sc.nextLine().trim();
            if (cognome.isEmpty()) {
                System.out.println("Errore: il campo 'Cognome' non può essere vuoto.");
            } else {
                cognomeValido = true;
            }
        }

        String username = "";
        boolean usernameValido = false;
        while (!usernameValido) {
            System.out.print("Inserisci Username: ");
            username = sc.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Errore: L'username non può essere vuoto.");
            } else if (FileManager.isUsernameEsistenteSuFile(username)) {
                System.out.println("Errore: L'username '" + username + "' è già preso! Scegline un altro.");
            } else {
                usernameValido = true;
            }
        }

        String passwordInChiaro = "";
        boolean passwordValida = false;
        while (!passwordValida) {
            System.out.print("Inserisci la password: ");
            passwordInChiaro = sc.nextLine().trim();
            if (passwordInChiaro.isEmpty()) {
                System.out.println("Errore: il campo 'Password' non può essere vuoto.");
            } else {
                passwordValida = true;
            }
        }

        String passwordHash = FileManager.generaPasswordHash(passwordInChiaro);

        String dataNascita = "";
        boolean dataValida = false;

        while (!dataValida) {
            System.out.print("Inserisci la data di nascita gg/mm/aaaa (Facoltativo - Premi invio per saltare): ");
            dataNascita = sc.nextLine().trim();


            if (dataNascita.isEmpty()) {
                dataNascita = "N/D";
                dataValida = true;
            } else {
                try {
                    LocalDate.parse(dataNascita, FMT_ITA);
                    dataValida = true;
                } catch (DateTimeParseException e) {
                    System.out.println("Errore: Data non valida o inesistente (es. 32/01/2020 o 29/02/2023). Usa il formato gg/mm/aaaa.");
                }
            }
        }

        String domicilio = "";
        boolean domicilioValido = false;
        while (!domicilioValido) {
            System.out.print("Inserisci il luogo di domicilio: ");
            domicilio = sc.nextLine().trim();
            if (domicilio.isEmpty()) {
                System.out.println("Errore: il campo 'Domicilio' non può essere vuoto.");
            } else {
                domicilioValido = true;
            }
        }

        Utente nuovoUtente = new Cliente(nome, cognome, username, passwordHash, dataNascita, domicilio);

        try {
            FileManager.salvaUtente(nuovoUtente);
            System.out.println("\nRegistrazione completata con successo per @" + nuovoUtente.getUsername() + "!");
            System.out.println("Ora puoi effettuare il Log In dal menu principale.");

        } catch (IOException e) {
            System.err.println("\nErrore durante il salvataggio dell'utente su file: " + e.getMessage());
            System.out.println("La registrazione non è stata completata. Riprova più tardi.");
        }
    }

        /**
         * Gestisce il ciclo di interazione della sessione di lavoro per un utente autenticato
         * (che sia Cliente, Proiezionista o Bigliettaio).
         *
         * Mantiene attivo il menu specifico dell'utente finché non viene richiesta la disconnessione (logout).
         *
         * @param utente l'oggetto {@link Utente} che ha effettuato l'accesso e di cui viene avviata la sessione.
         */
    private void avviaSessioneUtente(Utente utente) {
        int opzioneLogout = utente.getOpzioneLogout();
        int sceltaAzione = 0;

        do {
            utente.mostraMenu();
            System.out.print("\nInserisci la tua scelta: ");

            try {
                sceltaAzione = Integer.parseInt(sc.nextLine().trim());
                utente.eseguiAzione(sceltaAzione);
            } catch (NumberFormatException e) {
                System.out.println("Errore: Inserisci un numero intero valido.");
            }

        } while (sceltaAzione != opzioneLogout);

        System.out.println("\nDisconnessione completata. Ritorno al menu principale...");
    }

        /**
         * Gestisce l'interfaccia e il flusso di navigazione per l'utente non autenticato (Ospite/Guest).
         *
         * Consente di:
         * <ul>
         *   * Cercare e filtrare le proiezioni presenti nel palinsesto in base a titolo, genere, intervallo di date e prezzo.
         *   * Visualizzare i dettagli di una proiezione specifica.
         *   * Avviare la procedura di registrazione come nuovo cliente.
         *   * Uscire per tornare al menu principale.
         * </ul>
         */
    public void gestisciGuest() {
        boolean continua = true;

        while (continua) {
            System.out.println("\n=============================================");
            System.out.println("              MENU OSPITE (GUEST)            ");
            System.out.println("=============================================");
            System.out.println("  1. Cerca proiezioni");
            System.out.println("  2. Registrati come Cliente");
            System.out.println("  3. Esci (Torna al menu principale)");
            System.out.println("=============================================");
            System.out.print("Seleziona un'opzione: ");

            String inputScelta = sc.nextLine();
            int scelta;

            try {
                scelta = Integer.parseInt(inputScelta.trim());
            } catch (NumberFormatException e) {
                System.out.println("  Errore: Inserisci un numero valido.");
                continue;
            }

            switch (scelta) {
                case 1:
                    System.out.println("\n--- 1. RICERCA E VISUALIZZAZIONE PROIEZIONI ---");

                    List<Proiezione> palinsesto;
                    try {
                        palinsesto = FileManager.caricaPalinsesto();
                    } catch (IOException e) {
                        System.err.println("  [ERRORE I/O] Impossibile caricare il palinsesto: " + e.getMessage());
                        break;
                    }

                    if (palinsesto.isEmpty()) {
                        System.out.println("  Nessuna proiezione presente in archivi.");
                        break;
                    }

                    System.out.print("- Titolo film (INVIO per tutti): ");
                    String titolo = sc.nextLine().trim();

                    Genere genereSelezionato = null;
                    boolean inputGenereValido = false;

                    while (!inputGenereValido) {
                        System.out.println("  Seleziona il genere (inserisci il numero, oppure INVIO per tutti):");

                        for (Genere g : Genere.values()) {
                            System.out.println("  " + g.getCodice() + ". " + g.getDescrizione());
                        }

                        System.out.print("  Scelta: ");
                        String inputGenereStr = sc.nextLine().trim();

                        if (inputGenereStr.isEmpty()) {
                            inputGenereValido = true;
                        } else {
                            try {
                                int sceltaGenereNum = Integer.parseInt(inputGenereStr);
                                genereSelezionato = Genere.daCodice(sceltaGenereNum);

                                if (genereSelezionato != null) {
                                    inputGenereValido = true;
                                } else {
                                    System.out.println("  Errore: Selezione non valida. Riprova.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("  Errore: Inserisci un numero valido o premi INVIO.");
                            }
                        }
                    }

                    Genere genere = genereSelezionato;

                    System.out.print("- Data inizio intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                    String dataInizioStr = sc.nextLine().trim();

                    System.out.print("- Data fine intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                    String dataFineStr = sc.nextLine().trim();

                    LocalDate filtroInizio = null;
                    LocalDate filtroFine = null;

                    try {
                        if (!dataInizioStr.isEmpty()) filtroInizio = LocalDate.parse(dataInizioStr, FMT_ITA);
                        if (!dataFineStr.isEmpty()) filtroFine = LocalDate.parse(dataFineStr, FMT_ITA);
                    } catch (DateTimeParseException e) {
                        System.out.println("  Errore: Uno o entrambi i formati data inseriti non sono validi (usa gg/mm/aaaa).");
                        break;
                    }

                    double prezzoMinimo = 3.50;
                    System.out.print("- Prezzo minimo (EUR, INVIO per 3.50 EUR): ");
                    String pMinInput = sc.nextLine().trim();
                    if (!pMinInput.isEmpty()) {
                        try {
                            prezzoMinimo = Double.parseDouble(pMinInput.replace(",", "."));
                        } catch (NumberFormatException e) {
                            System.out.println("  Prezzo minimo non valido, impostato a valore predefinito (3.50 EUR).");
                        }
                    }

                    double prezzoMassimo = 9999.0;
                    System.out.print("- Prezzo massimo (EUR, INVIO per nessun limite): ");
                    String pMaxInput = sc.nextLine().trim();
                    if (!pMaxInput.isEmpty()) {
                        try {
                            prezzoMassimo = Double.parseDouble(pMaxInput.replace(",", "."));
                        } catch (NumberFormatException e) {
                            System.out.println("  Prezzo massimo non valido, impostato a nessun limite.");
                        }
                    }

                    List<Proiezione> risultatiFiltrati = Utente.cercaProiezione(palinsesto, titolo, genere, filtroInizio, filtroFine, prezzoMinimo, prezzoMassimo);

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
                        int indiceScelto = Integer.parseInt(sc.nextLine().trim());

                        if (indiceScelto == 0) {
                            System.out.println("Selezione annullata.");
                        } else if (indiceScelto > 0 && indiceScelto <= risultatiFiltrati.size()) {
                            Proiezione proiezioneSelezionata = risultatiFiltrati.get(indiceScelto - 1);
                            Utente.visualizzaProiezione(proiezioneSelezionata);
                            System.out.println("Premi INVIO per tornare al menu principale...");
                            sc.nextLine();
                        } else {
                            System.out.println("  Numero non valido.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("  Errore: Inserisci un numero valido.");
                    }
                    break;


                case 2:
                    registraCliente();
                    continua = false;
                    break;
                case 3:
                    System.out.println("Uscita dal menu Ospite...");
                    continua = false;
                    break;

                default:
                    System.out.println("  Opzione non valida. Riprova.");
            }
        }
    }
        /**
         * Gestisce il flusso interattivo di Login per gli utenti già registrati.
         *
         * Richiede l'inserimento delle credenziali (username e password), verifica l'esistenza
         * dell'utente tramite il {@link FileManager} e controlla la correttezza della password tramite hash.
         * In caso di successo, mostra un messaggio di benvenuto e invoca {@link #avviaSessioneUtente(Utente)}.
         */
    public void gestisciLogin() {
        boolean loginEffettuato = false;

        while (!loginEffettuato) {
            System.out.println("\n=============================================");
            System.out.println("                 LOG IN                      ");
            System.out.println("=============================================");
            System.out.print("Inserisci Username (o premi INVIO per tornare indietro): ");
            String usernameLogin = sc.nextLine().trim();

            if (usernameLogin.isEmpty()) {
                System.out.println("Ritorno al menu principale...");
                return;
            }

            System.out.print("Inserisci Password: ");
            String passwordLogin = sc.nextLine().trim();

            try {
                java.util.Optional<Utente> utenteOpt = FileManager.caricaUtentePerUsername(usernameLogin);

                if (utenteOpt.isPresent()) {
                    Utente u = utenteOpt.get();

                    if (FileManager.verificaPassword(passwordLogin, u.getPasswordHash())) {
                        System.out.println("\n  Accesso effettuato con successo!");
                        System.out.println("  Benvenuto, " + u.getNome() + " " + u.getCognome() + " (" + u.getClass().getSimpleName() + ").");

                        avviaSessioneUtente(u);
                        loginEffettuato = true;

                    } else {
                        System.out.println("\n  [ERRORE] Password errata. Riprova.");
                    }
                } else {
                    System.out.println("\n  [ERRORE] Nessun utente registrato con l'username '" + usernameLogin + "'. Riprova.");
                }
            } catch (IOException e) {
                System.err.println("\n  [ERRORE I/O] Impossibile completare il login: " + e.getMessage());
                break;
            }
        }
    }





















}
