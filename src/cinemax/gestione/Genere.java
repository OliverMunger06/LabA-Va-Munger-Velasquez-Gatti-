package cinemax.gestione;

/**
 * Rappresenta i generi cinematografici disponibili per le proiezioni
 * all'interno del sistema CineMax. Ciascun genere è associato a un codice
 * numerico univoco e a una descrizione testuale.
 *
 * @author Oliver Munger , matricola num. 764208 , VA
 * @author Davide Gatti , matricola num. 765949 , VA
 * @author Davide Noe Velasquez Carpio , matricola num. 765163 , VA
 */
public enum Genere {
    AZIONE(1, "Azione"),
    ANIMAZIONE(2, "Animazione"),
    AVVENTURA(3, "Avventura"),
    BIOGRAFICO(4, "Biografico"),
    COMMEDIA(5, "Commedia"),
    DRAMMATICO(6, "Drammatico"),
    FANTASCIENZA(7, "Fantascienza"),
    FANTASY(8, "Fantasy"),
    HORROR(9, "Horror"),
    MISTERO(10, "Mistero"),
    ROMANTICO(11, "Romantico"),
    STORICO(12, "Storico"),
    THRILLER(13, "Thriller");

    private final int codice;
    private final String descrizione;

    /**
     * Costruttore per l'enum Genere.
     *
     * @param codice      il codice numerico univoco associato al genere
     * @param descrizione la descrizione testuale del genere
     */
    Genere(int codice, String descrizione) {
        this.codice = codice;
        this.descrizione = descrizione;
    }

    /**
     * Restituisce il codice numerico associato al genere.
     *
     * @return il codice intero del genere
     */
    public int getCodice() {
        return codice;
    }

    /**
     * Restituisce la descrizione testuale del genere.
     *
     * @return una stringa contenente il nome del genere
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Cerca e restituisce l'istanza di {@link Genere} corrispondente al codice numerico fornito.
     *
     * @param codice il codice numerico da cercare
     * @return l'oggetto {@link Genere} corrispondente, oppure {@code null} se non viene trovato
     */
    public static Genere daCodice(int codice) {
        for (Genere g : values()) {
            if (g.codice == codice) {
                return g;
            }
        }
        return null;
    }

    /**
     * Cerca e restituisce l'istanza di {@link Genere} corrispondente alla descrizione testuale fornita.
     * <p>
     * La ricerca è case-insensitive e ignora gli spazi bianchi iniziali e finali.
     * </p>
     *
     * @param descrizione la descrizione testuale del genere da cercare
     * @return l'oggetto {@link Genere} corrispondente, oppure {@code null} se non viene trovato
     *         alcun genere con tale descrizione
     */
    public static Genere daDescrizione(String descrizione) {
        for (Genere g : values()) {
            if (g.descrizione.equalsIgnoreCase(descrizione.trim())) {
                return g;
            }
        }
        return null;
    }
}