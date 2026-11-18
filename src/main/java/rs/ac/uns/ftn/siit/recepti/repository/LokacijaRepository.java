package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.siit.recepti.model.Lokacija;

import java.util.Optional;

public interface LokacijaRepository extends JpaRepository<Lokacija, Long> {

    //Ako je region ili grad null, Spring Data pravi uslov "IS NULL"
    Optional<Lokacija> findFirstByDrzavaAndRegionAndGrad(String drzava, String region, String grad);
}
