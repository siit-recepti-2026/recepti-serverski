package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.siit.recepti.dto.NotifikacijaOdgovor;
import rs.ac.uns.ftn.siit.recepti.model.Notifikacija;

import java.util.List;

public interface NotifikacijaRepository extends JpaRepository<Notifikacija, Long> {
    List<NotifikacijaOdgovor> findAllByPrimalac_IdOrderByVremeKreiranjaDescIdDesc(Long primalacId);
}
