package cinemax.Users;

import cinemax.gestione.Proiezione;
import cinemax.gestione.Film;
import java.util.List;
import java.util.Scanner;

public class Proiezionista extends Utente {

    public Proiezionista(String nome, String cognome, String username, String passwordHash,
                         String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {
        super(nome, cognome, username, passwordHash, dataNascita, luogoDomicilio, isAlreadyHashed);
    }

    // ========================================================
    // METODI DI GESTIONE DEL PALINSESTO
    // ========================================================

    public boolean aggiungiProiezione(List<Proiezione> palinsesto, Proiezione nuovaProiezione) {
        for (Proiezione p : palinsesto) {
            if (p.getDataProiezione().equals(nuovaProiezione.getDataProiezione()) &&
                    p.getOraProiezione().equals(nuovaProiezione.getOraProiezione())) {

                System.out.println("  Errore: Esiste già una proiezione pianificata per questa data e orario!");
                return false;
            }
        }
        palinsesto.add(nuovaProiezione);
        System.out.println("  Proiezione aggiunta con successo al palinsesto.");
        return true;
    }

    public boolean modificaProiezione(List<Proiezione> palinsesto, String titoloFilm,
                                      String vecchiaData, String vecchiaOra,
                                      String nuovaData, String nuovaOra) {

        for (Proiezione p : palinsesto) {
            if (p.getFilm().getTitolo().equalsIgnoreCase(titoloFilm) &&
                    p.getDataProiezione().equals(vecchiaData) &&
                    p.getOraProiezione().equals(vecchiaOra)) {

                if (p.getPostiDisponibili() < 200) {
                    System.out.println("  Errore: Impossibile modificare. Ci sono già delle prenotazioni!");
                    return false;
                }

                p.setDataProiezione(nuovaData);
                p.setOraProiezione(nuovaOra);

                System.out.println("  Data e orario della proiezione modificati con successo.");
                return true;
            }
        }

        System.out.println("  Errore: Nessuna proiezione trovata per il film \"" + titoloFilm + "\" in data " + vecchiaData);
        return false;
    }

    public boolean eliminaProiezione(List<Proiezione> palinsesto, String titoloFilm, String dataStr, String oraStr) {
        java.util.Iterator<Proiezione> iterator = palinsesto.iterator();

        while (iterator.hasNext()) {
            Proiezione p = iterator.next();

            if (p.getFilm().getTitolo().equalsIgnoreCase(titoloFilm) &&
                    p.getDataProiezione().equals(dataStr) &&
                    p.getOraProiezione().equals(oraStr)) {

                if (p.getPostiDisponibili() < 200) {
                    System.out.println("  Errore: Impossibile eliminare. Ci sono già delle prenotazioni!");
                    return false;
                }

                iterator.remove();
                System.out.println("  Proiezione eliminata con successo.");
                return true;
            }
        }

        System.out.println("  Errore: Nessuna proiezione trovata per il film \"" + titoloFilm + "\" in data " + dataStr);
        return false;
    }

    // ========================================================
    // INTERFACCIA UTENTE (MENU AGGIORNATO CON FORMATO GG/MM/AAAA)
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

                // CORRETTO: Ora la richiesta a terminale è coerente!
                System.out.print("Data (gg/mm/aaaa): ");
                String dataStr = scanner.nextLine().trim();
                System.out.print("Ora (hh:mm): ");
                String oraStr = scanner.nextLine().trim();

                double prezzo = 0.0;
                while (true) {
                    System.out.print("Prezzo Biglietto (€): ");
                    try {
                        prezzo = Double.parseDouble(scanner.nextLine().trim());
                        if (prezzo >= 0) break;
                        System.out.println(" Il prezzo non può essere negativo.");
                    } catch (NumberFormatException e) {
                        System.out.println("  Errore: Inserisci un prezzo numerico valido (es. 7.50).");
                    }
                }

                System.out.print("Titolo Film: ");
                String titolo = scanner.nextLine().trim();
                System.out.print("Genere: ");
                String genere = scanner.nextLine().trim();
                System.out.print("Regista: ");
                String regista = scanner.nextLine().trim();

                int anno = 0, durata = 0, etaMin = 0;
                try {
                    System.out.print("Anno di uscita: ");
                    anno = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Durata (in minuti): ");
                    durata = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Età minima consigliata: ");
                    etaMin = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("  Valori numerici non validi. Impostati a 0 di default.");
                }

                Film nuovoFilm = new Film(titolo, genere, regista, anno, durata, etaMin);
                Proiezione nuovaProiezione = new Proiezione(dataStr, oraStr, prezzo, nuovoFilm);

                if (this.aggiungiProiezione(palinsesto, nuovaProiezione)) {
                    System.out.println("ID Spettacolo assegnato dal sistema: " + nuovaProiezione.getIdProiezione());
                }
                break;

            case 2:
                System.out.println("\n--- MODIFICA DATA E ORARIO PROIEZIONE ---");
                System.out.print("Titolo del Film da modificare: ");
                String tMod = scanner.nextLine().trim();
                // CORRETTO: Input in formato italiano
                System.out.print("Vecchia Data (gg/mm/aaaa): ");
                String vecchiaData = scanner.nextLine().trim();
                System.out.print("Vecchia Ora (hh:mm): ");
                String vecchiaOra = scanner.nextLine().trim();
                System.out.print("Nuova Data (gg/mm/aaaa): ");
                String nuovaData = scanner.nextLine().trim();
                System.out.print("Nuova Ora (hh:mm): ");
                String nuovaOra = scanner.nextLine().trim();

                this.modificaProiezione(palinsesto, tMod, vecchiaData, vecchiaOra, nuovaData, nuovaOra);
                break;

            case 3:
                System.out.println("\n--- ELIMINA PROIEZIONE ---");
                System.out.print("Titolo del Film da eliminare: ");
                String tElimina = scanner.nextLine().trim();
                // CORRETTO: Input in formato italiano
                System.out.print("Data della proiezione (gg/mm/aaaa): ");
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