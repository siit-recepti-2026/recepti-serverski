package rs.ac.uns.ftn.siit.recepti.dto;

import java.util.List;
import rs.ac.uns.ftn.siit.recepti.model.Alergen;
import rs.ac.uns.ftn.siit.recepti.model.Recept;
import rs.ac.uns.ftn.siit.recepti.model.TipIshrane;
import rs.ac.uns.ftn.siit.recepti.model.TipObroka;

//Osnovne informacije o receptu koje se prikazuju na kartici recepta (pocetna strana, pretraga, profil)
public record ReceptKarticaOdgovor(
        Long id,
        String naziv,
        String autor,
        String tipJela,
        List<String> tipoviObroka,
        List<String> tipoviIshrane,
        List<String> alergeni,
        int brojPorcija,
        int ukupnoVremePripremeMin,
        String glavnaFotografija,
        double prosecnaOcena,
        long brojOcena) {

    //Pravi karticu od recepta iz baze, ocena se racuna upitom nad ocenama, pa se prosledjuje spolja
    public static ReceptKarticaOdgovor od(Recept recept, double prosecnaOcena, long brojOcena) {
        return new ReceptKarticaOdgovor(
                recept.getId(),
                recept.getNaziv(),
                recept.getAutor().getKorisnickoIme(),
                recept.getTipJela().getNaziv(),
                recept.getTipoviObroka().stream().map(TipObroka::getNaziv).sorted().toList(),
                recept.getTipoviIshrane().stream().map(TipIshrane::getNaziv).sorted().toList(),
                //Alergeni recepta su alergeni svih njegovih namirnica, bez ponavljanja
                recept.getSastojci().stream()
                        .flatMap(sastojak -> sastojak.getNamirnica().getAlergeni().stream())
                        .map(Alergen::getNaziv)
                        .distinct()
                        .sorted()
                        .toList(),
                recept.getBrojPorcija(),
                recept.getUkupnoVremePripremeMin(),
                recept.getGlavnaFotografija().getUrl(),
                prosecnaOcena,
                brojOcena);
    }
}