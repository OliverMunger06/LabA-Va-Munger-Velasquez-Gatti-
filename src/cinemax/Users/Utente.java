package cinemax.Users;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public abstract class Utente {
    private String username;
    private String passwordHash;
    private String nome;
    private String cognome;
    private LocalDate dataNascita;
    private String luogoDomicilio;


    public Utente(String username, String passwordInChiaro, String nome, String cognome,
                  LocalDate dataNascita, String luogoDomicilio) {
        this.username = username;
        this.passwordHash = generaPasswordHash(passwordInChiaro);
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    public Utente(String username, String passwordHash, String nome, String cognome,
                  LocalDate dataNascita, String luogoDomicilio, boolean isAlreadyHashed) {

        this.username = username;
        this.passwordHash = passwordHash; // Viene assegnata direttamente dal file senza ricalcolare l'hash[cite: 1]
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
    }

    public abstract void mostraMenu() ;

    // da spostare nella classe MenuPrincipale
    private String generaPasswordHash(String password) {
        try {
            byte[] salt = "SaltSegretoCinema2026".getBytes();
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Errore nella cifratura", e);
        }
    }

    public boolean verificaPassword(String passwordDaVerificare) {
        return this.passwordHash.equals(generaPasswordHash(passwordDaVerificare));
    }


    public String getUsername() { return username; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getLuogoDomicilio() { return luogoDomicilio; }
    public LocalDate getDataNascita() { return dataNascita; }
    public String getPasswordHash() { return passwordHash; }
}
