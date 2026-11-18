package rs.ac.uns.ftn.siit.recepti.admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.siit.recepti.model.KategorijaNamirnice;
import rs.ac.uns.ftn.siit.recepti.model.TipIshrane;
import rs.ac.uns.ftn.siit.recepti.model.TipJela;
import rs.ac.uns.ftn.siit.recepti.model.TipObroka;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class AdminTipoviIKategorijeService {
    private final EntityManager entityManager;

    public AdminTipoviIKategorijeService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public RezultatDodavanja dodaj(String vrsta, String naziv) {
        try {
            return switch (vrsta) {
                case "tipovi-jela" -> {
                    TipJela tip = new TipJela();
                    tip.setNaziv(naziv);
                    yield dodajIliAktiviraj(tip, tip::getId, naziv);
                }
                case "tipovi-obroka" -> {
                    TipObroka tip = new TipObroka();
                    tip.setNaziv(naziv);
                    yield dodajIliAktiviraj(tip, tip::getId, naziv);
                }
                case "tipovi-ishrane" -> {
                    TipIshrane tip = new TipIshrane();
                    tip.setNaziv(naziv);
                    yield dodajIliAktiviraj(tip, tip::getId, naziv);
                }
                case "kategorije-namirnica" -> {
                    KategorijaNamirnice kategorija = new KategorijaNamirnice();
                    kategorija.setNaziv(naziv);
                    yield dodajIliAktiviraj(kategorija, kategorija::getId, naziv);
                }
                default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            };
        } catch (ConstraintViolationException e) {
            throw greskaZaNaziv(e);
        }
    }

    private RezultatDodavanja dodajIliAktiviraj(Object novaStavka, Supplier<Long> noviId, String naziv) {
        String entitet = entityManager.getMetamodel().entity(novaStavka.getClass()).getName();
        int aktivirano = entityManager.createQuery(
                        "update " + entitet + " s set s.aktivan = true where s.naziv = :naziv and s.aktivan = false")
                .setParameter("naziv", naziv)
                .executeUpdate();
        if (aktivirano > 0) {
            Long id = entityManager.createQuery(
                            "select s.id from " + entitet + " s where s.naziv = :naziv", Long.class)
                    .setParameter("naziv", naziv)
                    .getSingleResult();
            return new RezultatDodavanja(new AdminPregled.Stavka(id, naziv, true), true);
        }

        entityManager.persist(novaStavka);
        entityManager.flush();
        return new RezultatDodavanja(new AdminPregled.Stavka(noviId.get(), naziv, true), false);
    }

    @Transactional
    public AdminPregled.Stavka izmeni(String vrsta, long id, String naziv) {
        StavkaZaIzmenu stavka = pronadji(vrsta, id);
        stavka.postaviNaziv().accept(naziv);
        try {
            entityManager.flush();
        } catch (ConstraintViolationException e) {
            throw greskaZaNaziv(e);
        }
        return new AdminPregled.Stavka(id, naziv, stavka.aktivan());
    }

    @Transactional
    public AdminPregled.Stavka deaktiviraj(String vrsta, long id) {
        StavkaZaIzmenu stavka = pronadji(vrsta, id);
        if (stavka.aktivan()) {
            boolean povezana = !entityManager.createQuery(stavka.upitZaVeze(), Integer.class)
                    .setParameter("id", id)
                    .setMaxResults(1)
                    .getResultList().isEmpty();
            if (povezana) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stavka je u upotrebi.");
            }
            stavka.deaktiviraj().run();
        }
        return new AdminPregled.Stavka(id, stavka.naziv(), false);
    }

    private StavkaZaIzmenu pronadji(String vrsta, long id) {
        return switch (vrsta) {
            case "tipovi-jela" -> {
                TipJela tip = pronadji(TipJela.class, id);
                yield new StavkaZaIzmenu(tip.getNaziv(), tip.isAktivan(), tip::setNaziv,
                        () -> tip.setAktivan(false),
                        "select 1 from Recept r where r.tipJela.id = :id");
            }
            case "tipovi-obroka" -> {
                TipObroka tip = pronadji(TipObroka.class, id);
                yield new StavkaZaIzmenu(tip.getNaziv(), tip.isAktivan(), tip::setNaziv,
                        () -> tip.setAktivan(false),
                        "select 1 from Recept r join r.tipoviObroka t where t.id = :id");
            }
            case "tipovi-ishrane" -> {
                TipIshrane tip = pronadji(TipIshrane.class, id);
                yield new StavkaZaIzmenu(tip.getNaziv(), tip.isAktivan(), tip::setNaziv,
                        () -> tip.setAktivan(false),
                        "select 1 from Recept r join r.tipoviIshrane t where t.id = :id");
            }
            case "kategorije-namirnica" -> {
                KategorijaNamirnice kategorija = pronadji(KategorijaNamirnice.class, id);
                yield new StavkaZaIzmenu(kategorija.getNaziv(), kategorija.isAktivan(), kategorija::setNaziv,
                        () -> kategorija.setAktivan(false),
                        "select 1 from Namirnica n where n.kategorija.id = :id");
            }
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
    }

    private <T> T pronadji(Class<T> tip, long id) {
        T entitet = entityManager.find(tip, id, LockModeType.PESSIMISTIC_WRITE);
        if (entitet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stavka nije pronađena.");
        }
        return entitet;
    }

    private RuntimeException greskaZaNaziv(ConstraintViolationException e) {
        if ("23505".equals(e.getSQLState())) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "Naziv već postoji u ovoj listi.", e);
        }
        return e;
    }

    public record RezultatDodavanja(AdminPregled.Stavka stavka, boolean ponovoAktivirana) {
    }

    private record StavkaZaIzmenu(
            String naziv, boolean aktivan, Consumer<String> postaviNaziv, Runnable deaktiviraj, String upitZaVeze
    ) {
    }
}
