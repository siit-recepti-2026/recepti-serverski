package rs.ac.uns.ftn.siit.recepti.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.siit.recepti.dto.NotifikacijaOdgovor;
import rs.ac.uns.ftn.siit.recepti.repository.NotifikacijaRepository;
import rs.ac.uns.ftn.siit.recepti.service.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/notifikacije")
@RequiredArgsConstructor
public class NotifikacijaController {
    private final NotifikacijaRepository notifikacije;

    @GetMapping
    public List<NotifikacijaOdgovor> moje(@AuthenticationPrincipal Jwt jwt) {
        Long id = ((Number) jwt.getClaim(JwtService.CLAIM_ID)).longValue();
        return notifikacije.findAllByPrimalac_IdOrderByVremeKreiranjaDescIdDesc(id);
    }
}
