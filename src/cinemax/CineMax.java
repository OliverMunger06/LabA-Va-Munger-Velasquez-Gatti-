package cinemax;

import cinemax.controls.GestoreMenu;
import java.io.*;
import java.util.Scanner;

public class CineMax {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GestoreMenu gm = new GestoreMenu(sc);
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
                    gm.registraCliente();
                    break;
                case 2:
                    gm.gestisciLogin();
                    break;
                case 3:
                    gm.gestisciGuest();
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
}

