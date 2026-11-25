package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.siit.recepti.model.Ocena;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OcenaRepository extends JpaRepository<Ocena, Long> {

    //Id-jevi recepata sa najvecom prosecnom ocenom od zadatog trenutka; kod iste prosecne ocene prednost ima recept sa vise ocena
    @Query("""
            select o.recept.id from Ocena o
            where o.recept.uklonjen = false and o.datum >= :od
            group by o.recept.id
            order by avg(o.vrednost) desc, count(o) desc
            """)
    List<Long> najboljeOcenjeniReceptiOd(LocalDateTime od, Pageable pageable);

    //Prosecna ocena i broj svih ocena za svaki od zadatih recepata
    @Query("""
            select o.recept.id as receptId, avg(o.vrednost) as prosek, count(o) as broj
            from Ocena o
            where o.recept.id in :receptIds
            group by o.recept.id
            """)
    List<OceneRecepta> oceneRecepata(Collection<Long> receptIds);

    //Jedan red rezultata upita oceneRecepata
    interface OceneRecepta {
        Long getReceptId();

        Double getProsek();

        Long getBroj();
    }
}