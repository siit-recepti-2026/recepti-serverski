package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.dto.PrijavaOdgovor;
import rs.ac.uns.ftn.siit.recepti.dto.PrijavaZahtev;
import rs.ac.uns.ftn.siit.recepti.exception.NeaktiviranNalogException;
import rs.ac.uns.ftn.siit.recepti.exception.NeuspesnaPrijavaException;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrijavaService {

    private static final String POGRESNI_PODACI = "Pogresno korisnicko ime, email ili lozinka";

    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public PrijavaOdgovor prijavi(PrijavaZahtev zahtev) {
        String unos = zahtev.korisnickoImeIliEmail().trim();

        //Korisnicko ime ne sme da sadrzi @, pa je jasno sta je korisnik uneo
        Optional<Korisnik> pronadjen = unos.contains("@")
                ? korisnikRepository.findByEmailIgnoreCase(unos)
                : korisnikRepository.findByKorisnickoImeIgnoreCase(unos);

        //Ista poruka za nepostojeci nalog i pogresnu lozinku, da se ne otkriva koji nalozi postoje
        Korisnik korisnik = pronadjen
                .filter(k -> passwordEncoder.matches(zahtev.lozinka(), k.getLozinka()))
                .orElseThrow(() -> new NeuspesnaPrijavaException(POGRESNI_PODACI));

        //Proverava se tek posle lozinke, da poruku o aktivaciji vidi samo vlasnik naloga
        if (!korisnik.isAktiviran()) {
            throw new NeaktiviranNalogException("Nalog nije aktiviran. Aktivirajte ga preko linka iz mejla.");
        }

        Instant istice = jwtService.vremeIsteka();
        String token = jwtService.napraviToken(korisnik, istice);
        return new PrijavaOdgovor(token, istice, korisnik.getKorisnickoIme(), korisnik.getUloga().name());
    }
}
