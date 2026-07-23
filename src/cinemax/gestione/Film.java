package cinemax.gestione;

public class Film {

    private String titolo;
    private String genere;
    private String regista;
    private int  anno;
    private int durata;
    private int eta_minima;

    public Film(String titolo, String genere, String regista, int anno, int durata, int eta_minima) {
        this.titolo = titolo;
        this.genere = genere;
        this.regista = regista;
        this.anno = anno;
        this.durata = durata;
        this.eta_minima = eta_minima;
    }

    public String getTitolo() { return titolo; }
    public String getGenere() { return genere; }
    public String getRegista() { return regista; }
    public int getAnno() { return anno; }
    public int getDurata() { return durata; }
    public int getEta_minima() { return eta_minima; }










}
