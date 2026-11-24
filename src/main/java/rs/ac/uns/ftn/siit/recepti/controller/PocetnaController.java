package rs.ac.uns.ftn.siit.recepti.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.siit.recepti.dto.ReceptKarticaOdgovor;
import rs.ac.uns.ftn.siit.recepti.service.PocetnaService;

import java.util.List;

//Recepti za pocetnu stranu, dostupni i bez prijave
@RestController
@RequestMapping("/api/recepti")
@RequiredArgsConstructor
public class PocetnaController {

    private final PocetnaService pocetnaService;

    //Najveca prosecna ocena u proteklih mesec dana (podrazumevani prikaz pocetne strane)
    @GetMapping("/popularni")
    public List<ReceptKarticaOdgovor> popularni() {
        return pocetnaService.popularni();
    }

    //Od najnovijeg ka starijem, po datumu objave
    @GetMapping("/najnoviji")
    public List<ReceptKarticaOdgovor> najnoviji() {
        return pocetnaService.najnoviji();
    }
}