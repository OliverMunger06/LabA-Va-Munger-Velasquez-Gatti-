package cinemax;

import cinemax.Users.Cliente;
import cinemax.Users.Utente;
import cinemax.gestione.Film;
import cinemax.gestione.Proiezione;
import cinemax.utils.FileManager;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CineMax {

    private static final DateTimeFormatter FMT_ITA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int scelta = 0;

        do {
            System.out.println("\n=== BENVENUTO IN CINEMAX ===");
            System.out.println("1. Registrati");
            System.out.println("2. Log In");
            System.out.println("3. Entra come Guest");
            System.out.println("4. Esci dall'applicazione");
            System.out.print("Scelta: ");

            try {
                scelta = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Errore: Inserisci un numero valido.");
                continue;
            }
            switch (scelta) {
                case 1:
                    registraCliente(sc);
                    break;
                case 2:
                    gestisciLogin(sc);
                    break;
                case 3:
                    gestisciGuest(sc);
                    break;
                case 4:
                    System.out.println("Arrivederci!");
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }while (scelta != 4);

        sc.close();
    }


    /**
     * Gestisce il flusso di registrazione di un nuovo utente.
     * Registra l'utente su file e avvia immediatamente la sua sessione di lavoro.
     *
     * @param sc Scanner condiviso per la lettura dell'input da console.
     */
    public static void registraCliente(Scanner sc) {
        System.out.println("\n--- REGISTRAZIONE NUOVO UTENTE ---");

        // 2. ACQUISIZIONE DATI ANAGRAFICI CON VALIDAZIONE
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

            // Se l'utente preme invio senza scrivere nulla, consideriamo il campo valido ("N/D")
            if (dataNascita.isEmpty()) {
                dataNascita = "N/D";
                dataValida = true;
            } else {
                try {
                    // Tenta di parsare la stringa usando il formato corretto con 'uuuu' e controlla che la data esista realmente
                    LocalDate.parse(dataNascita, FMT_ITA);
                    dataValida = true; // Se non lancia eccezioni, la data è formattata bene ed esiste
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

        // 3. CREAZIONE DELL'ISTANZA POLIMORFICA
        Utente nuovoUtente = new Cliente(nome, cognome, username, passwordHash, dataNascita, domicilio);

        // 4. PERSISTENZA
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
     * Gestisce il ciclo di interazione dell'utente autenticato (Cliente, Proiezionista, Bigliettaio).
     */
    private static void avviaSessioneUtente(Utente utente, Scanner sc) {
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
     * Gestisce l'interazione per l'utente non autenticato (Guest).
     * Permette di cercare proiezioni, visualizzarne i dettagli, registrarsi (avviando subito la sessione) o uscire.
     *
     * @param sc         Lo scanner utilizzato per leggere l'input da console.
     */
    public static void gestisciGuest(Scanner sc) {
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
                    String titolo = sc.nextLine().trim();

                    System.out.print("- Genere/Tipologia (INVIO per tutti): ");
                    String genere = sc.nextLine().trim();

                    System.out.print("- Data inizio intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                    String dataInizioStr = sc.nextLine().trim();

                    System.out.print("- Data fine intervallo (gg/mm/aaaa, INVIO per nessuna): ");
                    String dataFineStr = sc.nextLine().trim();

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

                    List<Proiezione> risultatiFiltrati = new ArrayList<>();
                    for (Proiezione p : palinsestoCaso1) {
                        Film f = p.getFilm();

                        if (!titolo.isEmpty() && !f.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                            continue;
                        }

                        if (!genere.isEmpty() && !f.getGenere().equalsIgnoreCase(genere)) {
                            continue;
                        }

                        if (p.getDataProiezione() != null) {
                            LocalDate dataProiezioneObj = p.getDataProiezione().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            if (filterInizio != null && dataProiezioneObj.isBefore(filterInizio)) continue;
                            if (filterFine != null && dataProiezioneObj.isAfter(filterFine)) continue;
                        } else {
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
                    // Esegue la registrazione
                    registraCliente(sc);

                    // Imposta continua a false per uscire dal loop del Guest
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
     * Gestisce il flusso interattivo di Login per gli utenti registrati.
     * Verifica l'esistenza dell'username e la correttezza della password,
     * avviando successivamente la sessione dedicata in caso di successo.
     *
     * @param sc Scanner condiviso per la lettura dell'input da console.
     */
    public static void gestisciLogin(Scanner sc) {
        boolean loginEffettuato = false;

        while (!loginEffettuato) {
            System.out.println("\n=============================================");
            System.out.println("                 LOG IN                      ");
            System.out.println("=============================================");
            System.out.print("Inserisci Username (o premi INVIO per tornare indietro): ");
            String usernameLogin = sc.nextLine().trim();

            // Se l'utente preme invio senza mettere l'username, esce dal login e torna al menu principale
            if (usernameLogin.isEmpty()) {
                System.out.println("Ritorno al menu principale...");
                return;
            }

            System.out.print("Inserisci Password: ");
            String passwordLogin = sc.nextLine().trim();

            try {
                // Cerca l'utente salvato su file tramite l'username
                java.util.Optional<Utente> utenteOpt = FileManager.caricaUtentePerUsername(usernameLogin);

                if (utenteOpt.isPresent()) {
                    Utente u = utenteOpt.get();

                    // Verifica se la password inserita corrisponde all'hash salvato
                    if (FileManager.verificaPassword(passwordLogin, u.getPasswordHash())) {
                        System.out.println("\n  Accesso effettuato con successo!");
                        System.out.println("  Benvenuto, " + u.getNome() + " " + u.getCognome() + " (" + u.getClass().getSimpleName() + ").");

                        // Avvia la sessione specifica dell'utente
                        avviaSessioneUtente(u, sc);

                        loginEffettuato = true; // Imposta a true per uscire dal ciclo una volta fatto il logout

                    } else {
                        System.out.println("\n  [ERRORE] Password errata. Riprova.");
                    }
                } else {
                    System.out.println("\n  [ERRORE] Nessun utente registrato con l'username '" + usernameLogin + "'. Riprova.");
                }
            } catch (IOException e) {
                System.err.println("\n  [ERRORE I/O] Impossibile completare il login: " + e.getMessage());
                break; // In caso di errore grave di I/O usciamo dal ciclo
            }
        }
    }

}

