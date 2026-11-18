package rs.ac.uns.ftn.siit.recepti.admin;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.model.Alergen;
import rs.ac.uns.ftn.siit.recepti.model.KategorijaNamirnice;
import rs.ac.uns.ftn.siit.recepti.model.TipIshrane;
import rs.ac.uns.ftn.siit.recepti.model.TipJela;
import rs.ac.uns.ftn.siit.recepti.model.TipObroka;

import java.util.List;

@Service
public class AdminPregledService {
    private final EntityManager entityManager;

    public AdminPregledService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public AdminPregled pregled() {
        List<AdminPregled.Stavka> alergeni = entityManager
                .createQuery("select a from Alergen a order by a.naziv", Alergen.class)
                .getResultList()
                .stream()
                .map(a -> new AdminPregled.Stavka(a.getId(), a.getNaziv(), a.isAktivan(), a.getOpis()))
                .toList();

        return new AdminPregled(
                entityManager.createQuery("select t from TipJela t order by t.naziv", TipJela.class)
                        .getResultList().stream()
                        .map(t -> new AdminPregled.Stavka(t.getId(), t.getNaziv(), t.isAktivan())).toList(),
                entityManager.createQuery("select t from TipObroka t order by t.naziv", TipObroka.class)
                        .getResultList().stream()
                        .map(t -> new AdminPregled.Stavka(t.getId(), t.getNaziv(), t.isAktivan())).toList(),
                entityManager.createQuery("select t from TipIshrane t order by t.naziv", TipIshrane.class)
                        .getResultList().stream()
                        .map(t -> new AdminPregled.Stavka(t.getId(), t.getNaziv(), t.isAktivan())).toList(),
                entityManager.createQuery(
                                "select k from KategorijaNamirnice k order by k.naziv", KategorijaNamirnice.class)
                        .getResultList().stream()
                        .map(k -> new AdminPregled.Stavka(k.getId(), k.getNaziv(), k.isAktivan())).toList(),
                alergeni
        );
    }
}
