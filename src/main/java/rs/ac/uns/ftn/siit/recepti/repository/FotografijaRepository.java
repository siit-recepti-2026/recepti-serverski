package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.siit.recepti.model.Fotografija;

public interface FotografijaRepository extends JpaRepository<Fotografija, Long> {

    //Fotografija je vec zauzeta ako je profilna slika nekog korisnika, glavna ili dodatna fotografija recepta,
    //fotografija koraka ili komentara. Tada ne sme da se postavi kao nova profilna slika.
    @Query("""
            select count(f) > 0 from Fotografija f
            where f = :fotografija and (
                f.receptDodatne is not null
                or f.korak is not null
                or f.komentar is not null
                or exists (select 1 from Korisnik k where k.profilnaSlika = f)
                or exists (select 1 from Recept r where r.glavnaFotografija = f))
            """)
    boolean jeZauzeta(Fotografija fotografija);
}
