package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.dto.LokacijaZahtev;
import rs.ac.uns.ftn.siit.recepti.model.Lokacija;
import rs.ac.uns.ftn.siit.recepti.repository.LokacijaRepository;

@Service
@RequiredArgsConstructor
public class LokacijaService {

    private final LokacijaRepository lokacijaRepository;

    //Lokacija se ne menja (@Immutable), pa se ista drzava/region/grad koristi ponovo umesto da se pravi duplikat
    @Transactional
    public Lokacija pronadjiIliNapravi(LokacijaZahtev zahtev) {
        String drzava = zahtev.drzava().trim();
        String region = prazanUNull(zahtev.region());
        String grad = prazanUNull(zahtev.grad());

        return lokacijaRepository.findFirstByDrzavaAndRegionAndGrad(drzava, region, grad)
                .orElseGet(() -> lokacijaRepository.save(new Lokacija(
                        drzava, region, grad, zahtev.geografskaSirina(), zahtev.geografskaDuzina())));
    }

    private String prazanUNull(String vrednost) {
        return vrednost == null || vrednost.isBlank() ? null : vrednost.trim();
    }
}
