package rs.ac.uns.ftn.siit.recepti.admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.exception.KonfliktException;
import rs.ac.uns.ftn.siit.recepti.exception.NeispravanZahtevException;
import rs.ac.uns.ftn.siit.recepti.exception.NijePronadjenoException;
import rs.ac.uns.ftn.siit.recepti.model.Alergen;
import rs.ac.uns.ftn.siit.recepti.model.Namirnica;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminAlergeniService {
    private final EntityManager entityManager;

    public AdminAlergeniService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<OpcijaNamirnice> namirnice() {
        return entityManager.createQuery("select n from Namirnica n order by n.naziv, n.id", Namirnica.class)
                .getResultList().stream()
                .map(n -> new OpcijaNamirnice(n.getId(), n.getNaziv()))
                .toList();
    }

    @Transactional
    public RezultatDodavanja dodaj(String naziv, String opis, Set<Long> namirniceIds) {
        Alergen postojeci = entityManager.createQuery("select a from Alergen a where a.naziv = :naziv", Alergen.class)
                .setParameter("naziv", naziv)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResultOrNull();
        if (postojeci != null && postojeci.isAktivan()) {
            throw nazivZauzet();
        }

        Set<Long> veze = new HashSet<>(namirniceIds);
        Alergen alergen = postojeci == null ? new Alergen() : postojeci;
        if (postojeci != null) {
            povezaneNamirnice(alergen).forEach(n -> veze.add(n.getId()));
        }
        alergen.setNaziv(naziv);
        if (postojeci == null || opis != null) {
            alergen.setOpis(opis);
        }
        alergen.setAktivan(true);
        sacuvaj(alergen, veze);
        return new RezultatDodavanja(stavka(alergen, veze), postojeci != null);
    }

    @Transactional
    public AdminPregled.Stavka izmeni(long id, String naziv, String opis, Set<Long> namirniceIds) {
        Alergen alergen = pronadji(id);
        alergen.setNaziv(naziv);
        alergen.setOpis(opis);
        sacuvaj(alergen, namirniceIds);
        return stavka(alergen, namirniceIds);
    }

    @Transactional
    public AdminPregled.Stavka deaktiviraj(long id) {
        Alergen alergen = pronadji(id);
        List<Long> namirniceIds = povezaneNamirnice(alergen).stream().map(Namirnica::getId).toList();
        // Veze ostaju sačuvane jer se iz njih određuju alergeni postojećih recepata.
        alergen.setAktivan(false);
        return stavka(alergen, namirniceIds);
    }

    private void sacuvaj(Alergen alergen, Set<Long> namirniceIds) {
        try {
            List<Namirnica> noveVeze = namirniceIds.isEmpty() ? List.of() : entityManager.createQuery(
                            "select n from Namirnica n where n.id in :ids order by n.id", Namirnica.class)
                    .setParameter("ids", namirniceIds)
                    .getResultList();
            if (noveVeze.size() != namirniceIds.size()) {
                throw new NeispravanZahtevException("Neke izabrane namirnice ne postoje.");
            }
            if (alergen.getId() == null) {
                entityManager.persist(alergen);
            } else {
                povezaneNamirnice(alergen).stream()
                        .filter(n -> !namirniceIds.contains(n.getId()))
                        .forEach(n -> n.getAlergeni().remove(alergen));
            }
            noveVeze.forEach(n -> n.getAlergeni().add(alergen));
            entityManager.flush();
        } catch (ConstraintViolationException e) {
            if ("23505".equals(e.getSQLState())) {
                throw nazivZauzet();
            }
            throw e;
        }
    }

    private Alergen pronadji(long id) {
        Alergen alergen = entityManager.find(Alergen.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (alergen == null) {
            throw new NijePronadjenoException("Alergen nije pronađen.");
        }
        return alergen;
    }

    private List<Namirnica> povezaneNamirnice(Alergen alergen) {
        return entityManager.createQuery("select n from Namirnica n where :alergen member of n.alergeni",
                        Namirnica.class)
                .setParameter("alergen", alergen)
                .getResultList();
    }

    private AdminPregled.Stavka stavka(Alergen alergen, Collection<Long> namirniceIds) {
        return new AdminPregled.Stavka(alergen.getId(), alergen.getNaziv(), alergen.isAktivan(),
                alergen.getOpis(), namirniceIds.stream().sorted().toList());
    }

    private KonfliktException nazivZauzet() {
        return new KonfliktException("naziv", "Alergen sa ovim nazivom već postoji.");
    }

    public record OpcijaNamirnice(Long id, String naziv) {
    }

    public record RezultatDodavanja(AdminPregled.Stavka stavka, boolean ponovoAktivirana) {
    }
}
