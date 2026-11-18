package rs.ac.uns.ftn.siit.recepti.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.siit.recepti.dto.KorisnikOdgovor;
import rs.ac.uns.ftn.siit.recepti.exception.NeuspesnaPrijavaException;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;
import rs.ac.uns.ftn.siit.recepti.service.JwtService;

@RestController
@RequestMapping("/api/korisnici")
@RequiredArgsConstructor
public class KorisnikController {

    private final KorisnikRepository korisnikRepository;

    //Trenutno prijavljen korisnik, odredjen iz JWT-a koji je poslat u zaglavlju Authorization
    @GetMapping("/ja")
    public KorisnikOdgovor ja(@AuthenticationPrincipal Jwt jwt) {
        Long id = ((Number) jwt.getClaim(JwtService.CLAIM_ID)).longValue();
        return korisnikRepository.findById(id)
                .map(k -> new KorisnikOdgovor(k.getId(), k.getKorisnickoIme(), k.getEmail(), k.getUloga().name()))
                .orElseThrow(() -> new NeuspesnaPrijavaException("Korisnik vise ne postoji"));
    }
}
