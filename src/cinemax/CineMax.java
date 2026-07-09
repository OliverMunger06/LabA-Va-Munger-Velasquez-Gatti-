
package cinemax;

import cinemax.Users.Bigliettaio;
import cinemax.Users.Cliente;
import cinemax.Users.Proiezionista;
import cinemax.Users.Utente;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CineMax {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== BANCO DI TEST INTERATTIVO (CON ACCUMULO DATI) ===");

        try {
            // ========================================================
            // FASE 0: CARICAMENTO DEI DATI ESISTENTI (Per non perdere nulla!)
            // ========================================================
            List<Utente> listaUtenti = FileManager.caricaUtenti();
            List<Proiezione> palinsesto = FileManager.caricaPalinsesto();
            List<Prenotazione> listaPrenotazioni = FileManager.caricaPrenotazioni(palinsesto);

            System.out.println(" Record storici caricati: " +
                    listaUtenti.size() + " utenti, " +
                    palinsesto.size() + " proiezioni, " +
                    listaPrenotazioni.size() + " prenotazioni.");

            // ========================================================
            // 1. INSERIMENTO NUOVO UTENTE
            // ========================================================
            System.out.println("\n--- [1] INSERIMENTO NUOVO UTENTE ---");
            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("Cognome: ");
            String cognome = scanner.nextLine();
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password in chiaro (verrà cifrata): ");
            String passwordChiaro = scanner.nextLine();
            System.out.print("Data di Nascita (gg/mm/aaaa, INVIO per saltare): ");
            String dataInput = scanner.nextLine().trim();
            String dataNascita = dataInput.isEmpty() ? "N/D" : dataInput;
            System.out.print("Luogo di Domicilio: ");
            String domicilio = scanner.nextLine();

            System.out.println("Scegli la tipologia (1 = CLIENTE, 2 = BIGLIETTAIO, 3 = PROIEZIONISTA): ");
            int sceltaTipo = Integer.parseInt(scanner.nextLine());

            String hashPassword = FileManager.generaPasswordHash(passwordChiaro);
            Utente nuovoUtente;

            if (sceltaTipo == 2) {
                nuovoUtente = new Bigliettaio(nome, cognome, username, hashPassword, dataNascita, domicilio, true);
            } else if (sceltaTipo == 3) {
                nuovoUtente = new Proiezionista(nome, cognome, username, hashPassword, dataNascita, domicilio, true);
            } else {
                nuovoUtente = new Cliente(nome, cognome, username, hashPassword, dataNascita, domicilio, true);
            }

            // Viene aggiunto alla lista che contiene già i vecchi utenti!
            listaUtenti.add(nuovoUtente);
            FileManager.salvaUtenti(listaUtenti);


            // ========================================================
            // 2. INSERIMENTO NUOVA PROIEZIONE
            // ========================================================
            System.out.println("\n--- [2] INSERIMENTO NUOVA PROIEZIONE ---");
            System.out.print("Titolo del Film: ");
            String titolo = scanner.nextLine();
            System.out.print("Genere: ");
            String genere = scanner.nextLine();
            System.out.print("Regista: ");
            String regista = scanner.nextLine();
            System.out.print("Anno di uscita: ");
            int anno = Integer.parseInt(scanner.nextLine());
            System.out.print("Durata (in minuti): ");
            int durata = Integer.parseInt(scanner.nextLine());
            System.out.print("Età minima consigliata: ");
            int etaMinima = Integer.parseInt(scanner.nextLine());

            Film film = new Film(titolo, genere, regista, anno, durata, etaMinima);

            System.out.print("Data Proiezione (gg/mm/aaaa): ");
            String dataProj = scanner.nextLine();
            System.out.print("Ora Proiezione (hh:mm): ");
            String oraProj = scanner.nextLine();
            System.out.print("Prezzo Biglietto: ");
            String inputPrezzo = scanner.nextLine().trim().replace(",", ".");
            double prezzo = Double.parseDouble(inputPrezzo);

            Proiezione proiezione = new Proiezione(dataProj, oraProj, prezzo, film);

            System.out.println("✨ Spettacolo creato con successo! ID: " + proiezione.getIdProiezione());

            // Viene aggiunto al palinsesto storico esistente!
            palinsesto.add(proiezione);
            FileManager.salvaPalinsesto(palinsesto);


            // ========================================================
            // 3. CREAZIONE PRENOTAZIONE DI TEST
            // ========================================================
            System.out.println("\n--- [3] CREAZIONE PRENOTAZIONE DI TEST ---");

            proiezione.prenotaPosto(); // Scala il posto sulla nuova proiezione

            //  CORRETTO: Passiamo l'intero oggetto 'nuovoUtente' (sfrutta il Costruttore 2 OOP)
            Prenotazione prenotazione = new Prenotazione(nuovoUtente, proiezione);

            System.out.println("-> ID Prenotazione assegnato automaticamente: " + prenotazione.getIdPrenotazione());

            // Viene aggiunta alla lista delle prenotazioni storiche!
            listaPrenotazioni.add(prenotazione);
            FileManager.salvaPrenotazioni(listaPrenotazioni, listaUtenti);


            // ========================================================
            // 4. VERIFICA GENERALE DI TUTTO L'ARCHIVIO STORICO
            // ========================================================
            System.out.println("\n=============================================");
            System.out.println("--- [4] CONTROLLO TOTALE DEL DATABASE CSV ---");

            // Rileggiamo un'ultima volta per dare la certezza matematica del salvataggio
            List<Utente> utentiLetti = FileManager.caricaUtenti();
            List<Proiezione> palinsestoLetto = FileManager.caricaPalinsesto();
            List<Prenotazione> prenotazioniLette = FileManager.caricaPrenotazioni(palinsestoLetto);

            System.out.println("\n>>> TUTTI L'ELENCO UTENTI REGISTRATI (" + utentiLetti.size() + "):");
            for (Utente u : utentiLetti) {
                System.out.println(" - " + u.getUsername() + " [" + u.getClass().getSimpleName() + "]");
            }

            System.out.println("\n>>> TUTTO IL PALINSESTO STORICO (" + palinsestoLetto.size() + "):");
            for (Proiezione p : palinsestoLetto) {
                System.out.println(" - " + p.getIdProiezione() + ": " + p.getFilm().getTitolo() + " | Posti liberi: " + p.getPostiDisponibili() + "/200");
            }

            System.out.println("\n>>> TUTTE LE PRENOTAZIONI EMESSE FINORA (" + prenotazioniLette.size() + "):");
            for (Prenotazione pren : prenotazioniLette) {
                System.out.println("---------------------------------------------");
                //  SICURO: Usiamo i getter specifici o verifichiamo che i campi non siano null prima di stampare
                String intestatario = (pren.getNomeCliente() != null && !pren.getNomeCliente().equals("N/D"))
                        ? pren.getNomeCliente() + " " + pren.getCognomeCliente()
                        : "@" + pren.getUsernameCliente();

                System.out.println("▪️ ID Prenotazione: " + pren.getIdPrenotazione());
                System.out.println("▪️ Cliente:          " + intestatario);
                System.out.println("▪️ Film:             " + pren.getTitoloFilm());
                System.out.println("▪️ Data e Ora:       " + pren.getDataStr() + " ore " + pren.getOraStr());
                System.out.println("▪️ Codice Biglietto: " + pren.getCodiceBiglietto());
            }
            System.out.println("---------------------------------------------");

            System.out.println("\n=== FINE TEST INTERATTIVO ===");


            System.out.println("\n>>> TUTTO L'ELENCO UTENTI REGISTRATI (" + utentiLetti.size() + "):");
            for (Utente u : utentiLetti) {
                System.out.println(" - " + u.getUsername() + " [" + u.getClass().getSimpleName() + "]");
            }

            System.out.println("\n>>> TUTTO IL PALINSESTO STORICO (" + palinsestoLetto.size() + "):");
            for (Proiezione p : palinsestoLetto) {
                System.out.println(" - " + p.getIdProiezione() + ": " + p.getFilm().getTitolo() + " | Posti liberi: " + p.getPostiDisponibili() + "/200");
            }

            System.out.println("\n>>> TUTTE LE PRENOTAZIONI EMESSE FINORA (" + prenotazioniLette.size() + "):");
            for (Prenotazione pren : prenotazioniLette) {
                //  ORA È SICURO AL 100%: Il toString() interno userà nome e cognome caricati dal FileManager
                System.out.println(pren);
            }
            System.out.println("---------------------------------------------");

            System.out.println("\n=== FINE TEST INTERATTIVO ===");
            System.out.println("\n>>> TUTTI L'ELENCO UTENTI REGISTRATI (" + utentiLetti.size() + "):");
            for (Utente u : utentiLetti) {
                System.out.println(" - " + u.getUsername() + " [" + u.getClass().getSimpleName() + "]");
            }

            System.out.println("\n>>> TUTTO IL PALINSESTO STORICO (" + palinsestoLetto.size() + "):");
            for (Proiezione p : palinsestoLetto) {
                System.out.println(" - " + p.getIdProiezione() + ": " + p.getFilm().getTitolo() + " | Posti liberi: " + p.getPostiDisponibili() + "/200");
            }

            System.out.println("\n>>> TUTTE LE PRENOTAZIONI EMESSE FINORA (" + prenotazioniLette.size() + "):");
            for (Prenotazione pren : prenotazioniLette) {
                System.out.println("---------------------------------------------");
                System.out.println(pren);
            }
            System.out.println("---------------------------------------------");

            System.out.println("\n=== FINE TEST INTERATTIVO ===");

        } catch (IOException e) {
            System.err.println(" Errore di lettura/scrittura sui file!");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println(" Errore: Inserimento numerico non valido.");
        } finally {
            scanner.close();
        }
    }
}