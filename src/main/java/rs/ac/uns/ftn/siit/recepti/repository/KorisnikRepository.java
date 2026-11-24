package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.model.enums.UlogaKorisnika;

import java.util.Optional;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

    boolean existsByKorisnickoImeIgnoreCase(String korisnickoIme);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUloga(UlogaKorisnika uloga);

    Optional<Korisnik> findByEmailIgnoreCase(String email);

    Optional<Korisnik> findByKorisnickoImeIgnoreCase(String korisnickoIme);

    //Broj recepata koje je korisnik objavio, bez uklonjenih (logicki obrisanih)
    @Query("select count(r) from Recept r where r.autor = :korisnik and r.uklonjen = false")
    long brojObjavljenihRecepata(Korisnik korisnik);
}
