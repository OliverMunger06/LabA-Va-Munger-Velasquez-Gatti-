package cinemax.Users;


import cinemax.Prenotazione;
import cinemax.Proiezione;
import cinemax.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

    public class Cliente extends Utente {

        // ------------------------------------------------------------------------
        // COSTRUTTORI (Overloading)
        // ------------------------------------------------------------------------

        /**
         * COSTRUTTORE 1: Usato per la REGISTRAZIONE di un nuovo cliente.
         * Prende la password in chiaro e la cifra attraverso il meccanismo della classe madre.
         */
        public Cliente(String username, String passwordInChiaro, String nome, String cognome,
                       LocalDate dataNascita, String luogoDomicilio) {
            // super(...) chiama il costruttore della classe astratta Utente che effettua l'hashing
            super(username, passwordInChiaro, nome, cognome, dataNascita, luogoDomicilio);
        }

        /**
         * COSTRUTTORE 2: Usato dal FileManager per il CARICAMENTO dal file utenti.csv.
         * Prende la password che è GIÀ un hash memorizzato nel file, senza ricifrarla.
         */
        public Cliente(String username, String passwordHash, String nome, String cognome,
                       LocalDate dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
            // Chiama un costruttore secondario di Utente (che memorizza l'hash direttamente)
            super(username, passwordHash, nome, cognome, dataNascita, luogoDomicilio, isAlreadyHashed);
        }

        // ------------------------------------------------------------------------
        // IMPLEMENTAZIONE METODI ASTRATTI
        // ------------------------------------------------------------------------

        @Override
        public void mostraMenu() {
            System.out.println("\n=== AREA PERSONALE CLIENTE: " + getNome().toUpperCase() + " ===");
            System.out.println("1. Cerca proiezioni e spettacoli");
            System.out.println("2. Inserisci una nuova prenotazione ");
            System.out.println("3. Visualizza le tue prenotazioni attive");
            System.out.println("4. Modifica le tue prenotazioni");
            System.out.println("5. Cancella una prenotazione");
            System.out.println("6. Logout");
        }

        // ------------------------------------------------------------------------
        // LOGICA DI BUSINESS (Prenotazioni)
        // ------------------------------------------------------------------------

        /**
         * Inserisce una nuova prenotazione modificando l'array dei posti in memoria.
         */
        public void inserisciPrenotazione(Proiezione proiezione, List<Prenotazione> databasePrenotazioni) {
            // 1. Tenta di scalare un posto dalla sala (restituisce true se c'è posto, false se è piena)
            boolean esitoPosto = proiezione.prenotaPosto();

            if (!esitoPosto) {
                // Se esitoPosto è false, la sala è sold out
                System.out.println("Errore: Ci dispiace, la sala è al completo per questa proiezione.");
            } else {
                // 2. Crea l'oggetto prenotazione legandolo a questo cliente e alla proiezione specifica
                // NOTA: Verifica che il costruttore di Prenotazione accetti (String username, Proiezione proiezione)
                Prenotazione nuovaPrenotazione = new Prenotazione(this.getUsername(), proiezione);

                // 3. Aggiunge la prenotazione alla lista di sistema
                databasePrenotazioni.add(nuovaPrenotazione);

                System.out.println("Spettacolo prenotato con successo!");
                System.out.println("ID Biglietto: " + nuovaPrenotazione.getIdPrenotazione() +
                        " | Codice di Sicurezza QR: " + nuovaPrenotazione.getCodiceBiglietto());
            }
        }

        /**
         * Filtra la lista globale delle prenotazioni e mostra solo quelle di questo utente.
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
                            " | Orario: " + proiezione.getDataOraProiezione() +
                            " | Pagamento: €" + proiezione.getPrezzoBiglietto());
                }
            }

            if (miePrenotazioni.isEmpty()) {
                System.out.println("Non hai prenotazioni attive al momento.");
            }
            return miePrenotazioni;
        }

        public void modificaPrenotazione(String idPrenotazione, Proiezione nuovaProiezione, List<Prenotazione> databasePrenotazioni) {
            Prenotazione prenotazioneTrovata = null;

            for (Prenotazione p : databasePrenotazioni) {
                if (p.getIdPrenotazione().equals(idPrenotazione) && p.getUsernameCliente().equals(this.getUsername())) {
                    prenotazioneTrovata = p;
                    break;
                }
            }

            if (prenotazioneTrovata == null) {
                System.out.println("Errore: Prenotazione non trovato o permessi insufficienti.");
                return;
            }

            Proiezione vecchiaProiezione = prenotazioneTrovata.getFilmProiezione();

            // Tenta di scalare un posto nella NUOVA proiezione
            if (nuovaProiezione.prenotaPosto()) {
                // Se riesce, restituisce il posto a quella vecchia
                vecchiaProiezione.liberaPosto();

                // Aggiorna la prenotazione
                prenotazioneTrovata.setFilmProiezione(nuovaProiezione);

                System.out.println("Prenotazione spostata con successo sul film: " + nuovaProiezione.getFilm().getTitolo());
            } else {
                System.out.println("Impossibile spostare: la nuova proiezione è esaurita.");
            }
        }

        public void cancellaPrenotazione(String idPrenotazione, List<Prenotazione> databasePrenotazioni) {
            Prenotazione daRimuovere = null;

            for (Prenotazione p : databasePrenotazioni) {
                if (p.getIdPrenotazione().equals(idPrenotazione) && p.getUsernameCliente().equals(this.getUsername())) {
                    daRimuovere = p;
                    break;
                }
            }

            if (daRimuovere != null) {
                // Ridiamo un posto disponibile alla proiezione
                daRimuovere.getFilmProiezione().liberaPosto();
                databasePrenotazioni.remove(daRimuovere);
                System.out.println("Prenotazione annullata. Un posto è tornato disponibile in sala.");
            } else {
                System.out.println("Errore: Prenotazione non trovata.");
            }
        }
    }