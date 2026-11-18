package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.dto.RegistracijaZahtev;
import rs.ac.uns.ftn.siit.recepti.exception.KonfliktException;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.model.enums.UlogaKorisnika;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegistracijaService {

    private final KorisnikRepository korisnikRepository;
    private final LokacijaService lokacijaService;
    private final AktivacijaService aktivacijaService;
    private final PasswordEncoder passwordEncoder;

    //Sve je u jednoj transakciji: ako slanje mejla ne uspe, korisnik se ne cuva,
    //pa moze ponovo da se registruje sa istim podacima
    @Transactional
    public void registruj(RegistracijaZahtev zahtev) {
        String korisnickoIme = zahtev.korisnickoIme().trim();
        String email = normalizujEmail(zahtev.email());

        if (!dostupnoKorisnickoIme(korisnickoIme)) {
            throw new KonfliktException("korisnickoIme", "Korisnicko ime je zauzeto");
        }
        if (!dostupanEmail(email)) {
            throw new KonfliktException("email", "Nalog sa ovim emailom vec postoji");
        }

        Korisnik korisnik = new Korisnik();
        korisnik.setKorisnickoIme(korisnickoIme);
        korisnik.setEmail(email);
        korisnik.setLozinka(passwordEncoder.encode(zahtev.lozinka()));
        korisnik.setUloga(UlogaKorisnika.KORISNIK);
        korisnik.setAktiviran(false);
        korisnik.setDatumRegistracije(LocalDateTime.now());
        korisnik.setRegion(lokacijaService.pronadjiIliNapravi(zahtev.region()));
        korisnikRepository.save(korisnik);

        aktivacijaService.posaljiAktivacioniLink(korisnik);
    }

    @Transactional(readOnly = true)
    public boolean dostupnoKorisnickoIme(String korisnickoIme) {
        return !korisnikRepository.existsByKorisnickoImeIgnoreCase(korisnickoIme.trim());
    }

    @Transactional(readOnly = true)
    public boolean dostupanEmail(String email) {
        return !korisnikRepository.existsByEmailIgnoreCase(normalizujEmail(email));
    }

    private String normalizujEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
