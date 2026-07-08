package cinemax;

public class CineMax {
    public static void main(String[] args)  {
        Film f = new Film("Inception","Sci-fi", "JKRoling", 2021,130,12);

        Proiezione p = new Proiezione("19","2026-12-09","21:00", 9.5, f);

        System.out.println(f);
    }
}
