package rs.ac.uns.ftn.siit.recepti.admin;

import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.siit.recepti.model.KategorijaNamirnice;
import rs.ac.uns.ftn.siit.recepti.model.TipIshrane;
import rs.ac.uns.ftn.siit.recepti.model.TipJela;
import rs.ac.uns.ftn.siit.recepti.model.TipObroka;

@Service
public class AdminTipoviIKategorijeService {
    private final EntityManager entityManager;

    public AdminTipoviIKategorijeService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public AdminPregled.Stavka dodaj(String vrsta, String naziv) {
        try {
            Long id = switch (vrsta) {
                case "tipovi-jela" -> {
                    TipJela tip = new TipJela();
                    tip.setNaziv(naziv);
                    entityManager.persist(tip);
                    yield tip.getId();
                }
                case "tipovi-obroka" -> {
                    TipObroka tip = new TipObroka();
                    tip.setNaziv(naziv);
                    entityManager.persist(tip);
                    yield tip.getId();
                }
                case "tipovi-ishrane" -> {
                    TipIshrane tip = new TipIshrane();
                    tip.setNaziv(naziv);
                    entityManager.persist(tip);
                    yield tip.getId();
                }
                case "kategorije-namirnica" -> {
                    KategorijaNamirnice kategorija = new KategorijaNamirnice();
                    kategorija.setNaziv(naziv);
                    entityManager.persist(kategorija);
                    yield kategorija.getId();
                }
                default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            };
            entityManager.flush();
            return new AdminPregled.Stavka(id, naziv, true);
        } catch (ConstraintViolationException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Naziv već postoji u ovoj listi.", e);
            }
            throw e;
        }
    }
}
