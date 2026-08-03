package cinemax;

import cinemax.Users.Bigliettaio;
import cinemax.Users.Cliente;
import cinemax.Users.Proiezionista;
import cinemax.Users.Utente;
import cinemax.utils.FileManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CineMax {

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
                    gestisciRegistrazione(sc);
                    break;
                case 2:
                    // Logica di Login
                    break;
                case 3:
                    // Logica Guest
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
    public static void gestisciRegistrazione(Scanner sc) {
        System.out.println("\n--- REGISTRAZIONE NUOVO UTENTE ---");

        // 1. SELEZIONE RUOLO (con controllo d'errore sull'input intero)
        int sceltaRuolo = 0;
        boolean ruoloValido = false;

        while (!ruoloValido) {
            System.out.println("Seleziona il tipo di account da creare:");
            System.out.println("1. Cliente");
            System.out.println("2. Proiezionista");
            System.out.println("3. Bigliettaio");
            System.out.print("Scelta: ");

            try {
                sceltaRuolo = Integer.parseInt(sc.nextLine().trim());
                if (sceltaRuolo >= 1 && sceltaRuolo <= 3) {
                    ruoloValido = true;
                } else {
                    System.out.println("Errore: Scegli un opzione tra 1 e 3.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Errore: Inserisci un numero intero valido.\n");
            }
        }

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

        System.out.print("Inserisci la data di nascita gg/mm/aaaa (Facoltativo - Premi invio per saltare): ");
        String dataNascita = sc.nextLine().trim();
        if (dataNascita.isEmpty()) {
            dataNascita = "N/D";
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
        Utente nuovoUtente = null;
        switch (sceltaRuolo) {
            case 1:
                nuovoUtente = new Cliente(nome, cognome, username, passwordHash, dataNascita, domicilio);
                break;
            case 2:
                nuovoUtente = new Proiezionista(nome, cognome, username, passwordHash, dataNascita, domicilio);
                break;
            case 3:
                nuovoUtente = new Bigliettaio(nome, cognome, username, passwordHash, dataNascita, domicilio);
                break;
        }

        // 4. PERSISTENZA E AVVIO SESSIONE
        if (nuovoUtente != null) {
            try {
                FileManager.salvaUtente(nuovoUtente);
                System.out.println("\nRegistrazione completata con successo per @" + nuovoUtente.getUsername() + "!");

                // Richiamo del metodo riutilizzabile per la sessione dell'utente!
                avviaSessioneUtente(nuovoUtente, sc);

            } catch (IOException e) {
                System.err.println("\nErrore durante il salvataggio dell'utente su file: " + e.getMessage());
                System.out.println("La registrazione non è stata completata. Riprova più tardi.");
            }
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
}

