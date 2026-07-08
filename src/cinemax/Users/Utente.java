package cinemax.Users;

import cinemax.FileManager;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public abstract class Utente {
    private String username;
    private static String passwordHash;
    private String nome;
    private String cognome;
    private String dataNascita;
    private String luogoDomicilio;


    public Utente(String username, String passwordInChiaro, String nome, String cognome,
                  String dataNascita, String luogoDomicilio) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    public Utente(String username, String passwordHash, String nome, String cognome,
                  String dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {

        this.username = username;
        this.passwordHash = passwordHash; // Viene assegnata direttamente dal file senza ricalcolare l'hash[cite: 1]
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    public abstract void mostraMenu() ;




    public String getUsername() { return username; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getLuogoDomicilio() { return luogoDomicilio; }
    public String getDataNascita() { return dataNascita; }
    public static String getPasswordHash() { return passwordHash; }
}
