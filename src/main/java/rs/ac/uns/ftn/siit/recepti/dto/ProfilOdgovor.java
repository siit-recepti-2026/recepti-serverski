package rs.ac.uns.ftn.siit.recepti.dto;

//Javni podaci profila; profilnaSlika je putanja (/slike/ime.jpg) ili null ako slika nije postavljena
public record ProfilOdgovor(
        String korisnickoIme,
        String profilnaSlika,
        LokacijaOdgovor region,
        String biografija,
        long brojObjavljenihRecepata) {
}
