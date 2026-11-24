package rs.ac.uns.ftn.siit.recepti.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.siit.recepti.dto.ProfilOdgovor;
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
}
