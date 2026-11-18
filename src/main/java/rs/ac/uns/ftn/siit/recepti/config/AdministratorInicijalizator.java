package rs.ac.uns.ftn.siit.recepti.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.dto.LokacijaZahtev;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.model.enums.UlogaKorisnika;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;
import rs.ac.uns.ftn.siit.recepti.service.LokacijaService;

import java.time.LocalDateTime;
import java.util.Locale;

//Specifikacija: pri kreiranju sistema jedan korisnik je unapred administrator.
//Pravi se pri pokretanju servera ako administrator jos ne postoji.
//Lozinka se cita iz promenljive okruzenja ADMIN_PASSWORD, pa nije zapisana u repozitorijumu.
@Slf4j
@Component
@RequiredArgsConstructor
public class AdministratorInicijalizator implements ApplicationRunner {

    private final KorisnikRepository korisnikRepository;
    private final LokacijaService lokacijaService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.korisnicko-ime}")
    private String korisnickoIme;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.lozinka}")
    private String lozinka;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        napraviAkoNePostoji();
    }

    @Transactional
    public void napraviAkoNePostoji() {
        if (korisnikRepository.existsByUloga(UlogaKorisnika.ADMINISTRATOR)) {
            return;
        }
        if (lozinka == null || lozinka.isBlank()) {
            log.warn("Administrator ne postoji, a ADMIN_PASSWORD nije postavljen, pa nije napravljen");
            return;
        }
        if (korisnikRepository.existsByKorisnickoImeIgnoreCase(korisnickoIme)
                || korisnikRepository.existsByEmailIgnoreCase(email)) {
            log.warn("Administrator nije napravljen: korisnicko ime ili email su vec zauzeti");
            return;
        }

        Korisnik admin = new Korisnik();
        admin.setKorisnickoIme(korisnickoIme);
        admin.setEmail(email.trim().toLowerCase(Locale.ROOT));
        admin.setLozinka(passwordEncoder.encode(lozinka));
        admin.setUloga(UlogaKorisnika.ADMINISTRATOR);
        admin.setAktiviran(true);
        admin.setDatumRegistracije(LocalDateTime.now());
        admin.setRegion(lokacijaService.pronadjiIliNapravi(
                new LokacijaZahtev("Srbija", null, null, 44.0165, 21.0059)));
        korisnikRepository.save(admin);
        log.info("Napravljen pocetni administrator: {}", korisnickoIme);
    }
}
