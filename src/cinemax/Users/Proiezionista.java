package cinemax.Users;

import cinemax.Proiezione;
import cinemax.Film;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Proiezionista extends Utente {

    private static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 🔥 COSTRUTTORE CORRETTO: Allineato alla sequenza (nome, cognome, username...)
    public Proiezionista(String nome, String cognome, String username, String passwordHash,
                         String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
        super(nome, cognome, username, passwordHash, dataNascita, luogoDomicilio, isAlreadyHashed);
    }

    // ========================================================
    // METODI DI GESTIONE DEL PALINSESTO (List<Proiezione>)
    // ========================================================

    /**
     * Aggiunge una proiezione al palinsesto verificando che non ci siano sovrapposizioni d'orario.
     */
    public boolean aggiungiProiezione(List<Proiezione> palinsesto, Proiezione nuovaProiezione) {
        for (Proiezione p : palinsesto) {
            // CORRETTO: Controllo basato sui nuovi campi separati
            if (p.getDataProiezione().equals(nuovaProiezione.getDataProiezione()) &&
                    p.getOraProiezione().equals(nuovaProiezione.getOraProiezione())) {

                System.out.println(" Errore: Esiste già una proiezione pianificata per questa data e orario!");
                return false;
            }
        }
        palinsesto.add(nuovaProiezione);
        System.out.println(" Proiezione aggiunta con successo al palinsesto.");
        return true;
    }

    /**
     * b. Funzionalità di modifica di una proiezione
     * Vincolo: A patto che NON ci siano prenotazioni per quella proiezione (posti disponibili deve essere 200)
     */
    public boolean modificaProiezione(List<Proiezione> palinsesto, String titoloFilm,
                                      String vecchiaData, String vecchiaOra,
                                      String nuovaData, String nuovaOra) {

        for (Proiezione p : palinsesto) {
            if (p.getFilm().getTitolo().equalsIgnoreCase(titoloFilm) &&
                    p.getDataProiezione().equals(vecchiaData) &&
                    p.getOraProiezione().equals(vecchiaOra)) {

                // AGGIUNTO VINCOLO DA SPECIFICA: controllo prenotazioni esistenti
                if (p.getPostiDisponibili() < 200) {
                    System.out.println(" Errore: Impossibile modificare. Ci sono già delle prenotazioni per questa proiezione!");
                    return false;
                }

                p.setDataProiezione(nuovaData);
                p.setOraProiezione(nuovaOra);

                System.out.println(" Data e orario della proiezione modificati con successo.");
                return true;
            }
        }

        System.out.println(" Errore: Nessuna proiezione trovata per il film \"" + titoloFilm + "\" in data " + vecchiaData);
        return false;
    }

    /**
     * c. Funzionalità di cancellazione di una proiezione
     * Vincolo: A patto che NON ci siano prenotazioni per quella proiezione (posti disponibili deve essere 200)
     */
    public boolean eliminaProiezione(List<Proiezione> palinsesto, String titoloFilm, String dataStr, String oraStr) {
        java.util.Iterator<Proiezione> iterator = palinsesto.iterator();

        while (iterator.hasNext()) {
            Proiezione p = iterator.next();

            if (p.getFilm().getTitolo().equalsIgnoreCase(titoloFilm) &&
                    p.getDataProiezione().equals(dataStr) &&
                    p.getOraProiezione().equals(oraStr)) {

                // AGGIUNTO VINCOLO DA SPECIFICA: controllo prenotazioni esistenti
                if (p.getPostiDisponibili() < 200) {
                    System.out.println(" Errore: Impossibile eliminare. Ci sono già delle prenotazioni per questa proiezione!");
                    return false;
                }

                iterator.remove();
                System.out.println(" Proiezione eliminata con successo.");
                return true;
            }
        }

        System.out.println(" Errore: Nessuna proiezione trovata per il film \"" + titoloFilm + "\" in data " + dataStr);
        return false;
    }

    private String generaIdUnivocoProiezione(List<Proiezione> palinsesto) {
        String caratteri = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        java.util.Random random = new java.util.Random();
        String idGenerato = "";
        boolean duplicato = true;

        // Continua a rigenerare finché non ne trova uno veramente libero
        while (duplicato) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                int index = random.nextInt(caratteri.length());
                sb.append(caratteri.charAt(index));
            }
            idGenerato = sb.toString();

            // Verifica se l'ID è già presente nella lista globale
            duplicato = false;
            for (Proiezione p : palinsesto) {
                if (p.getIdProiezione().equalsIgnoreCase(idGenerato)) {
                    duplicato = true; // Trovata collisione, il ciclo while continuerà
                    break;
                }
            }
        }

        return idGenerato;
    }

    // ========================================================
    // INTERFACCIA UTENTE (MOSTRA MENU INTERATTIVO)
    // ========================================================

    @Override
    public void mostraMenu() {
        System.out.println("\n=== AREA PERSONALE PROIEZIONISTA: " + getNome().toUpperCase() + " ===");
        System.out.println("1. Inserisci una nuova proiezione ");
        System.out.println("2. Modifica data e ora di una proiezione");
        System.out.println("3. Elimina una proiezione dal palinsesto");
        System.out.println("4. Logout");
    }

    public void eseguiAzione(int scelta, List<Proiezione> palinsesto) {
        Scanner scanner = new Scanner(System.in);

        switch (scelta) {
            case 1:
                System.out.println("\n--- INSERIMENTO NUOVA PROIEZIONE ---");

                System.out.print("Data (aaaa-mm-gg): ");
                String dataStr = scanner.nextLine().trim();
                System.out.print("Ora (hh:mm): ");
                String oraStr = scanner.nextLine().trim();

                // Gestione robusta del prezzo del biglietto
                double prezzo = 0.0;
                while (true) {
                    System.out.print("Prezzo Biglietto (€): ");
                    try {
                        prezzo = Double.parseDouble(scanner.nextLine().trim());
                        if (prezzo >= 0) break;
                        System.out.println(" Il prezzo non può essere negativo.");
                    } catch (NumberFormatException e) {
                        System.out.println(" Errore: Inserisci un prezzo numerico valido (es. 7.50).");
                    }
                }

                System.out.print("Titolo Film: ");
                String titolo = scanner.nextLine().trim();
                System.out.print("Genere: ");
                String genere = scanner.nextLine().trim();
                System.out.print("Regista: ");
                String regista = scanner.nextLine().trim();

                // Gestione robusta per i numeri interi del film
                int anno = 0, durata = 0, etaMin = 0;
                try {
                    System.out.print("Anno di uscita: ");
                    anno = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Durata (in minuti): ");
                    durata = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Età minima consigliata: ");
                    etaMin = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println(" Errore nei dati numerici del film. Impostati valori di default (0).");
                }

                Film nuovoFilm = new Film(titolo, genere, regista, anno, durata, etaMin);

                //  L'ID viene generato da solo internamente qui dentro!
                Proiezione nuovaProiezione = new Proiezione(dataStr, oraStr, prezzo, nuovoFilm);

                if (this.aggiungiProiezione(palinsesto, nuovaProiezione)) {
                    System.out.println("ID Spettacolo assegnato dal sistema: " + nuovaProiezione.getIdProiezione());
                }
                break;
            case 2:
                System.out.println("\n--- MODIFICA DATA E ORARIO PROIEZIONE ---");
                System.out.print("Titolo del Film da modificare: ");
                String tMod = scanner.nextLine().trim();
                System.out.print("Vecchia Data (aaaa-mm-gg): ");
                String vecchiaData = scanner.nextLine().trim();
                System.out.print("Vecchia Ora (hh:mm): ");
                String vecchiaOra = scanner.nextLine().trim();
                System.out.print("Nuova Data (aaaa-mm-gg): ");
                String nuovaData = scanner.nextLine().trim();
                System.out.print("Nuova Ora (hh:mm): ");
                String nuovaOra = scanner.nextLine().trim();

                this.modificaProiezione(palinsesto, tMod, vecchiaData, vecchiaOra, nuovaData, nuovaOra);
                break;

            case 3:
                System.out.println("\n--- ELIMINA PROIEZIONE ---");
                System.out.print("Titolo del Film da eliminare: ");
                String tElimina = scanner.nextLine().trim();
                System.out.print("Data della proiezione (aaaa-mm-gg): ");
                String dataElimina = scanner.nextLine().trim();
                System.out.print("Ora della proiezione (hh:mm): ");
                String oraElimina = scanner.nextLine().trim();

                this.eliminaProiezione(palinsesto, tElimina, dataElimina, oraElimina);
                break;

            case 4:
                System.out.println("Disconnessione proiezionista in corso...");
                break;

            default:
                System.out.println("Scelta non valida.");
        }
    }
}



