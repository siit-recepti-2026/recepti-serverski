package rs.ac.uns.ftn.siit.recepti.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.siit.recepti.dto.PrijavaOdgovor;
import rs.ac.uns.ftn.siit.recepti.service.JwtService;
import rs.ac.uns.ftn.siit.recepti.service.PrijavaService;

//Sesija prijavljenog korisnika. Nije pod /api/auth, jer se tamo token namerno ne cita,
//a za osvezavanje je potreban vazeci token.
@RestController
@RequestMapping("/api/sesija")
@RequiredArgsConstructor
public class SesijaController {

    private final PrijavaService prijavaService;

    //Vazeci token se menja novim, sa punim trajanjem; istekao ili neispravan token dobija 401
    @PostMapping("/osvezi")
    public PrijavaOdgovor osvezi(@AuthenticationPrincipal Jwt jwt) {
        Long id = ((Number) jwt.getClaim(JwtService.CLAIM_ID)).longValue();
        return prijavaService.osvezi(id);
    }
}
