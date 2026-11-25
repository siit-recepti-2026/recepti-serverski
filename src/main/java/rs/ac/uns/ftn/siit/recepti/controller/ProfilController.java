package rs.ac.uns.ftn.siit.recepti.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.siit.recepti.dto.IzmenaProfilaZahtev;
import rs.ac.uns.ftn.siit.recepti.dto.ProfilOdgovor;
import rs.ac.uns.ftn.siit.recepti.service.JwtService;
import rs.ac.uns.ftn.siit.recepti.service.ProfilService;

//Profil je vidljiv svim prijavljenim korisnicima, i vlasniku i ostalima
@RestController
@RequestMapping("/api/profili")
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilService profilService;

    @GetMapping("/{korisnickoIme}")
    public ProfilOdgovor pregled(@PathVariable String korisnickoIme) {
        return profilService.pregled(korisnickoIme);
    }

    //Izmena svog profila; ciji je profil, odredjuje se iz tokena, a ne iz adrese
    @PutMapping("/ja")
    public ProfilOdgovor izmeni(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody IzmenaProfilaZahtev zahtev) {
        Long id = ((Number) jwt.getClaim(JwtService.CLAIM_ID)).longValue();
        return profilService.izmeni(id, zahtev);
    }
}
