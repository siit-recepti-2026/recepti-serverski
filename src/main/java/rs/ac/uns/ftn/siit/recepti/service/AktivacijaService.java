package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.exception.NeispravanZahtevException;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.model.VerifikacioniToken;
import rs.ac.uns.ftn.siit.recepti.model.enums.TipTokena;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;
import rs.ac.uns.ftn.siit.recepti.repository.VerifikacioniTokenRepository;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AktivacijaService {

    private final VerifikacioniTokenRepository tokenRepository;
    private final KorisnikRepository korisnikRepository;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Value("${app.aktivacija.trajanje-sati}")
    private long trajanjeAktivacijeSati;

    //Pravi novi aktivacioni token i salje ga mejlom; prethodni aktivacioni tokeni korisnika se brisu
    @Transactional
    public void posaljiAktivacioniLink(Korisnik korisnik) {
        tokenRepository.deleteByKorisnikAndTip(korisnik, TipTokena.AKTIVACIJA);

        String token = tokenService.generisiToken();
        VerifikacioniToken verifikacioniToken = new VerifikacioniToken();
        verifikacioniToken.setToken(tokenService.hesiraj(token));
        verifikacioniToken.setTip(TipTokena.AKTIVACIJA);
        verifikacioniToken.setVaziDo(LocalDateTime.now().plusHours(trajanjeAktivacijeSati));
        verifikacioniToken.setKorisnik(korisnik);
        tokenRepository.save(verifikacioniToken);

        emailService.posaljiAktivacioniMejl(korisnik.getEmail(), korisnik.getKorisnickoIme(), token);
    }

    @Transactional
    public void aktiviraj(String token) {
        VerifikacioniToken verifikacioniToken = tokenRepository
                .findByTokenAndTip(tokenService.hesiraj(token), TipTokena.AKTIVACIJA)
                .orElseThrow(() -> new NeispravanZahtevException(
                        "Link za aktivaciju nije ispravan ili je vec iskoriscen"));

        if (verifikacioniToken.getVaziDo().isBefore(LocalDateTime.now())) {
            throw new NeispravanZahtevException("Link za aktivaciju je istekao. Zatrazite novi link.");
        }

        //Uloga KORISNIK je dodeljena pri registraciji, a vazi tek kada je nalog aktiviran
        Korisnik korisnik = verifikacioniToken.getKorisnik();
        korisnik.setAktiviran(true);

        //Token se brise da link ne bi mogao ponovo da se iskoristi
        tokenRepository.delete(verifikacioniToken);
    }

    //Za korisnika kome je link istekao ili mejl nije stigao.
    //Odgovor je uvek isti, da se preko ovog endpointa ne bi otkrivalo koji emailovi imaju nalog.
    @Transactional
    public void ponovoPosalji(String email) {
        korisnikRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .filter(korisnik -> !korisnik.isAktiviran())
                .ifPresent(this::posaljiAktivacioniLink);
    }
}
