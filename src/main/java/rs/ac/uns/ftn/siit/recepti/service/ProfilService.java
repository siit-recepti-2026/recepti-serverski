package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.dto.IzmenaProfilaZahtev;
import rs.ac.uns.ftn.siit.recepti.dto.LokacijaOdgovor;
import rs.ac.uns.ftn.siit.recepti.dto.ProfilOdgovor;
import rs.ac.uns.ftn.siit.recepti.exception.NeispravanZahtevException;
import rs.ac.uns.ftn.siit.recepti.exception.NeuspesnaPrijavaException;
import rs.ac.uns.ftn.siit.recepti.exception.NijePronadjenoException;
import rs.ac.uns.ftn.siit.recepti.model.Fotografija;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.repository.FotografijaRepository;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfilService {

    private final KorisnikRepository korisnikRepository;
    private final FotografijaRepository fotografijaRepository;
    private final LokacijaService lokacijaService;

    //readOnly: samo citanje, a transakcija drzi vezu sa bazom otvorenom dok se ucitavaju region i slika (LAZY)
    @Transactional(readOnly = true)
    public ProfilOdgovor pregled(String korisnickoIme) {
        //Nalog koji jos nije aktiviran nema javni profil
        Korisnik korisnik = korisnikRepository.findByKorisnickoImeIgnoreCase(korisnickoIme)
                .filter(Korisnik::isAktiviran)
                .orElseThrow(() -> new NijePronadjenoException("Korisnik ne postoji"));
        return odgovor(korisnik);
    }

    //Korisnik se odredjuje iz tokena, pa svako moze da menja samo svoj profil.
    //Korisnicko ime se ne menja, a region recepata koji su vec objavljeni ostaje isti,
    //jer svaki recept cuva svoj region autora iz trenutka objave.
    @Transactional
    public ProfilOdgovor izmeni(Long idKorisnika, IzmenaProfilaZahtev zahtev) {
        Korisnik korisnik = korisnikRepository.findById(idKorisnika)
                .filter(Korisnik::isAktiviran)
                .orElseThrow(() -> new NeuspesnaPrijavaException("Sesija vise ne vazi. Prijavite se ponovo."));

        korisnik.setBiografija(prazanUNull(zahtev.biografija()));
        korisnik.setRegion(lokacijaService.pronadjiIliNapravi(zahtev.region()));

        if (zahtev.trebaUkloniti()) {
            zameniProfilnuSliku(korisnik, null);
        } else if (zahtev.profilnaSlikaId() != null) {
            zameniProfilnuSliku(korisnik, novaProfilnaSlika(korisnik, zahtev.profilnaSlikaId()));
        }
        return odgovor(korisnik);
    }

    private Fotografija novaProfilnaSlika(Korisnik korisnik, Long id) {
        Fotografija trenutna = korisnik.getProfilnaSlika();
        if (trenutna != null && Objects.equals(trenutna.getId(), id)) {
            return trenutna;
        }
        Fotografija slika = fotografijaRepository.findById(id)
                .filter(f -> !f.isUklonjena())
                .orElseThrow(() -> new NeispravanZahtevException("Izabrana slika ne postoji"));
        //Fotografija nema podatak o tome ko ju je otpremio, pa se bar ne dozvoljava tudja slika koja je vec u upotrebi
        if (fotografijaRepository.jeZauzeta(slika)) {
            throw new NeispravanZahtevException("Izabrana slika je vec u upotrebi");
        }
        return slika;
    }

    //Stara profilna slika se brise logicki: fajl ostaje na disku, a slika se vise ne prikazuje
    private void zameniProfilnuSliku(Korisnik korisnik, Fotografija nova) {
        Fotografija stara = korisnik.getProfilnaSlika();
        if (stara == nova) {
            return;
        }
        if (stara != null) {
            stara.setUklonjena(true);
        }
        korisnik.setProfilnaSlika(nova);
    }

    private ProfilOdgovor odgovor(Korisnik korisnik) {
        Fotografija slika = korisnik.getProfilnaSlika();
        String profilnaSlika = slika != null && !slika.isUklonjena() ? slika.getUrl() : null;

        return new ProfilOdgovor(
                korisnik.getKorisnickoIme(),
                profilnaSlika,
                LokacijaOdgovor.od(korisnik.getRegion()),
                korisnik.getBiografija(),
                korisnikRepository.brojObjavljenihRecepata(korisnik));
    }

    private static String prazanUNull(String vrednost) {
        return vrednost == null || vrednost.isBlank() ? null : vrednost.trim();
    }
}
