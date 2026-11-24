package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.dto.LokacijaOdgovor;
import rs.ac.uns.ftn.siit.recepti.dto.ProfilOdgovor;
import rs.ac.uns.ftn.siit.recepti.exception.NijePronadjenoException;
import rs.ac.uns.ftn.siit.recepti.model.Fotografija;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;

@Service
@RequiredArgsConstructor
public class ProfilService {

    private final KorisnikRepository korisnikRepository;

    //readOnly: samo citanje, a transakcija drzi vezu sa bazom otvorenom dok se ucitavaju region i slika (LAZY)
    @Transactional(readOnly = true)
    public ProfilOdgovor pregled(String korisnickoIme) {
        //Nalog koji jos nije aktiviran nema javni profil
        Korisnik korisnik = korisnikRepository.findByKorisnickoImeIgnoreCase(korisnickoIme)
                .filter(Korisnik::isAktiviran)
                .orElseThrow(() -> new NijePronadjenoException("Korisnik ne postoji"));

        Fotografija slika = korisnik.getProfilnaSlika();
        String profilnaSlika = slika != null && !slika.isUklonjena() ? slika.getUrl() : null;

        return new ProfilOdgovor(
                korisnik.getKorisnickoIme(),
                profilnaSlika,
                LokacijaOdgovor.od(korisnik.getRegion()),
                korisnik.getBiografija(),
                korisnikRepository.brojObjavljenihRecepata(korisnik));
    }
}
