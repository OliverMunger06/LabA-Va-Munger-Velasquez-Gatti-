
package cinemax;

import cinemax.Users.Bigliettaio;
import cinemax.Users.Cliente;
import cinemax.Users.Proiezionista;
import cinemax.Users.Utente;
import cinemax.utils.FileManager;
import cinemax.gestione.Film;
import cinemax.gestione.Prenotazione;
import cinemax.gestione.Proiezione;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class CineMax {

    public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    List<Utente> utenti = new Li

    System.out.println("Benvenuto nell'applicazione CineMax");
    System.out.println("1. Registrati ");
    System.out.println("2. accedi ");
    System.out.println("3. Continua come guest ");
    System.out.println("4. Esci ");

    String scelta = sc.nextLine();

    switch(scelta){
        case "1" :
        System.out.println("Inserisci il tuo nome ");
        String nome = sc.nextLine();
        System.out.println("Inserisci il cognome ");
        String cognome = sc.nextLine();
        System.out.println("Inserisci il nome utente ");
        String nome_utente = sc.nextLine();
        System.out.println("Inserisci la password");
        String password = sc.nextLine();
        System.out.println("Inserisci la data di nascita gg/mm/aaaa ");
        String data_nascita = sc.nextLine();
        System.out.println("Inserisci il luogo di domicilio");
        String domicilio = sc.nextLine();

        Cliente nuovo_cliente = new Cliente(nome,cognome, nome_utente,password,data_nascita,domicilio);

    }




    }
}