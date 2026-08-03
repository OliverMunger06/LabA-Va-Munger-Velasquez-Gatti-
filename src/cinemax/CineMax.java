package cinemax;

import cinemax.Users.Cliente;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CineMax {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Benvenuto nell'applicazione CineMax");
        System.out.println("1. Registrati ");
        System.out.println("2. accedi ");
        System.out.println("3. Continua come guest ");
        System.out.println("4. Esci ");

        String scelta = sc.nextLine();
        switch (scelta) {
            case "1":
                String nome = "";
                boolean nomevalido = false;
                while (!nomevalido) {
                    System.out.println("Inserisci il tuo nome: ");
                    nome = sc.nextLine();
                    if (nome == null || nome.trim().isEmpty())
                        System.out.println("Errore: il campo 'Nome' non può essere vuoto. Riprova:");
                    else nomevalido = true;
                }

                String cognome = "";
                boolean cognomevalido = false;
                while (!cognomevalido) {
                    System.out.println("Inserisci il tuo cognome: ");
                    cognome = sc.nextLine();
                    if (cognome == null || cognome.trim().isEmpty())
                        System.out.println("Errore: il campo 'Cognome' non può essere vuoto. Riprova:");
                    else cognomevalido = true;
                }

                String username = "";
                boolean usernameValido = false;
                while (!usernameValido) {
                    System.out.println("Inserisci Username: ");
                    username = sc.nextLine().trim();
                    if (username.isEmpty()) {
                        System.out.println("Errore: L'username non può essere vuoto. Riprova.");
                    } else if (isUsernameEsistenteSuFile(username)) {
                        System.out.println("Errore: L'username '" + username + "' è già preso! Scegline un altro.");
                    } else {
                        usernameValido = true;
                    }
                }

                String password = "";
                boolean passwordvalida = false;
                while (!passwordvalida) {
                    System.out.println("Inserisci la password: ");
                    password = sc.nextLine();
                    if (password == null || password.trim().isEmpty())
                        System.out.println("Errore: il campo 'password' non può essere vuoto. Riprova:");
                    else passwordvalida = true;
                }

                System.out.println("Inserisci la data di nascita gg/mm/aaaa (Facoltativo - Premi invio per saltare):");
                String data_nascita = sc.nextLine().trim();
                if (data_nascita.isEmpty())
                    data_nascita = "N/D";
                System.out.println("Inserisci il luogo di domicilio (Facoltativo - Premi invio per saltare):");
                String domicilio = sc.nextLine().trim();

                if (domicilio.isEmpty())
                    domicilio = "N/D";
                Cliente nuovoCliente = new Cliente(nome, cognome, username, password, data_nascita, domicilio);
                System.out.println("\n Registrazione completata per l'utente " + nuovoCliente.getUsername() + "!");
                salvaUtenteSuFile(nuovoCliente);
                break;
            case "2":
                // Logica di Login
                break;
            case "3":
                // Logica Guest
                break;
            case "4":
                System.out.println("Arrivederci!");
                break;
            default:
                System.out.println("Scelta non valida.");
        }
    }
    private static boolean isUsernameEsistenteSuFile(String usernameDaCercare) {
        String percorsoFile = "data/utenti.csv";
        List<String> righe = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(percorsoFile))) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (!riga.trim().isEmpty()) {
                    righe.add(riga);
                }
            }
        } catch (IOException e) {
            return false;
        }

        for (String riga : righe) {
            String[] campi = riga.split(",");

            if (campi.length > 2) {
                String usernameNelFile = campi[2].trim();

                if (usernameNelFile.equalsIgnoreCase(usernameDaCercare.trim())) {
                    return true; //  Username è un duplicato!
                }
            }
        }
        return false; //  Username è unico e disponibile
    }

    private static void salvaUtenteSuFile(Cliente cliente) {
        String percorsoFile = "data/utenti.csv";
        String rigaCsv = String.format("%s,%s,%s,%s,%s,%s",
                cliente.getNome(),
                cliente.getCognome(),
                cliente.getUsername(),
                cliente.getPasswordHash(),
                cliente.getDataNascita(),
                cliente.getLuogoDomicilio()
        );

        try (FileWriter fw = new FileWriter(percorsoFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(rigaCsv);
            System.out.println(" Utente salvato definitivamente su 'data/utenti.csv'!");

        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file: " + e.getMessage());
        }
    }
}