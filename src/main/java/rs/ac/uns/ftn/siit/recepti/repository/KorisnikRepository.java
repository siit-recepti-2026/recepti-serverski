package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

    boolean existsByKorisnickoImeIgnoreCase(String korisnickoIme);

    boolean existsByEmailIgnoreCase(String email);
}
