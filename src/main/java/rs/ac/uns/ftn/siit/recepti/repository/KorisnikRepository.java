package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;

import java.util.Optional;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

    boolean existsByKorisnickoImeIgnoreCase(String korisnickoIme);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Korisnik> findByEmailIgnoreCase(String email);
}
