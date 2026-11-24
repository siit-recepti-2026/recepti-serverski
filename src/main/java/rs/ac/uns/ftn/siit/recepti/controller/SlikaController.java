package rs.ac.uns.ftn.siit.recepti.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.siit.recepti.dto.SlikaOdgovor;
import rs.ac.uns.ftn.siit.recepti.service.SlikaService;

//Otpremanje slika (samo prijavljeni korisnici). Sacuvane slike se citaju na /slike/** bez prijave.
@RestController
@RequestMapping("/api/slike")
@RequiredArgsConstructor
public class SlikaController {

    private final SlikaService slikaService;

    //Zahtev je multipart/form-data, a fajl je u delu sa imenom "fajl"
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SlikaOdgovor otpremi(@RequestPart("fajl") MultipartFile fajl) {
        return slikaService.sacuvaj(fajl);
    }
}
