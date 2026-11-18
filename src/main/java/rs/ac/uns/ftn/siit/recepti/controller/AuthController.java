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
import rs.ac.uns.ftn.siit.recepti.dto.AktivacijaZahtev;
import rs.ac.uns.ftn.siit.recepti.dto.DostupnostOdgovor;
import rs.ac.uns.ftn.siit.recepti.dto.PonovnoSlanjeZahtev;
import rs.ac.uns.ftn.siit.recepti.dto.PorukaOdgovor;
import rs.ac.uns.ftn.siit.recepti.dto.PrijavaOdgovor;
import rs.ac.uns.ftn.siit.recepti.dto.PrijavaZahtev;
import rs.ac.uns.ftn.siit.recepti.dto.RegistracijaZahtev;
import rs.ac.uns.ftn.siit.recepti.service.AktivacijaService;
import rs.ac.uns.ftn.siit.recepti.service.PrijavaService;
import rs.ac.uns.ftn.siit.recepti.service.RegistracijaService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegistracijaService registracijaService;
    private final AktivacijaService aktivacijaService;
    private final PrijavaService prijavaService;

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

    //Poziva ga stranica veb aplikacije na koju vodi link iz mejla
    @PostMapping("/aktivacija")
    public PorukaOdgovor aktivacija(@Valid @RequestBody AktivacijaZahtev zahtev) {
        aktivacijaService.aktiviraj(zahtev.token());
        return new PorukaOdgovor("Nalog je aktiviran. Sada se mozete prijaviti.");
    }

    @PostMapping("/aktivacija/ponovo")
    public PorukaOdgovor ponovoPosaljiAktivaciju(@Valid @RequestBody PonovnoSlanjeZahtev zahtev) {
        aktivacijaService.ponovoPosalji(zahtev.email());
        return new PorukaOdgovor("Ako postoji neaktiviran nalog sa ovim emailom, poslat je novi link za aktivaciju.");
    }

    @PostMapping("/prijava")
    public PrijavaOdgovor prijava(@Valid @RequestBody PrijavaZahtev zahtev) {
        return prijavaService.prijavi(zahtev);
    }
}
