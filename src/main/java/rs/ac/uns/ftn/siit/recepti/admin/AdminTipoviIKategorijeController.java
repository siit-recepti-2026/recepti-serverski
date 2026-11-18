package rs.ac.uns.ftn.siit.recepti.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminTipoviIKategorijeController {
    private static final String LISTA = "/{vrsta:tipovi-jela|tipovi-obroka|tipovi-ishrane|kategorije-namirnica}";

    private final AdminTipoviIKategorijeService service;

    public AdminTipoviIKategorijeController(AdminTipoviIKategorijeService service) {
        this.service = service;
    }

    @PostMapping(LISTA)
    public ResponseEntity<AdminPregled.Stavka> dodaj(
            @PathVariable String vrsta, @Valid @RequestBody NazivStavke zahtev
    ) {
        AdminTipoviIKategorijeService.RezultatDodavanja rezultat = service.dodaj(vrsta, zahtev.naziv());
        HttpStatus status = rezultat.ponovoAktivirana() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(rezultat.stavka());
    }

    @PutMapping(LISTA + "/{id}")
    public AdminPregled.Stavka izmeni(
            @PathVariable String vrsta, @PathVariable long id, @Valid @RequestBody NazivStavke zahtev
    ) {
        return service.izmeni(vrsta, id, zahtev.naziv());
    }

    @DeleteMapping(LISTA + "/{id}")
    public AdminPregled.Stavka deaktiviraj(@PathVariable String vrsta, @PathVariable long id) {
        return service.deaktiviraj(vrsta, id);
    }

    public record NazivStavke(@NotBlank @Size(max = 100) String naziv) {
        public NazivStavke {
            if (naziv != null) {
                naziv = naziv.strip();
            }
        }
    }
}
