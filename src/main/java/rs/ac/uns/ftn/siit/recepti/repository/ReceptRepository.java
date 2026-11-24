package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.siit.recepti.model.Recept;

import java.util.List;

public interface ReceptRepository extends JpaRepository<Recept, Long> {

    //Najnoviji recepti, bez uklonjenih (logicki obrisanih); koliko ih vraca odredjuje Pageable
    List<Recept> findByUklonjenFalseOrderByDatumObjaveDesc(Pageable pageable);
}