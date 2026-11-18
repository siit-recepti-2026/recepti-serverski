package rs.ac.uns.ftn.siit.recepti.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.siit.recepti.dto.DostupnostOdgovor;
import rs.ac.uns.ftn.siit.recepti.dto.PorukaOdgovor;
import rs.ac.uns.ftn.siit.recepti.dto.RegistracijaZahtev;
import rs.ac.uns.ftn.siit.recepti.service.RegistracijaService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegistracijaService registracijaService;

    @PostMapping("/registracija")
    @ResponseStatus(HttpStatus.CREATED)
    public PorukaOdgovor registracija(@Valid @RequestBody RegistracijaZahtev zahtev) {
        registracijaService.registruj(zahtev);
        return new PorukaOdgovor("Registracija je uspesna. Proverite email i aktivirajte nalog.");
    }

    //Koriste ih asinhroni validatori veb forme dok korisnik kuca
    @GetMapping("/dostupnost/korisnicko-ime")
    public DostupnostOdgovor dostupnostKorisnickogImena(@RequestParam String vrednost) {
        return new DostupnostOdgovor(registracijaService.dostupnoKorisnickoIme(vrednost));
    }

    @GetMapping("/dostupnost/email")
    public DostupnostOdgovor dostupnostEmaila(@RequestParam String vrednost) {
        return new DostupnostOdgovor(registracijaService.dostupanEmail(vrednost));
    }
}
