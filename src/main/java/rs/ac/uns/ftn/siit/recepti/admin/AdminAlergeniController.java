package rs.ac.uns.ftn.siit.recepti.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/alergeni")
public class AdminAlergeniController {
    private final AdminAlergeniService service;

    public AdminAlergeniController(AdminAlergeniService service) {
        this.service = service;
    }

    @GetMapping("/namirnice")
    public List<AdminAlergeniService.OpcijaNamirnice> namirnice() {
        return service.namirnice();
    }

    @PostMapping
    public ResponseEntity<AdminPregled.Stavka> dodaj(@Valid @RequestBody PodaciAlergena zahtev) {
        var rezultat = service.dodaj(zahtev.naziv(), zahtev.opis(), zahtev.namirniceIds());
        HttpStatus status = rezultat.ponovoAktivirana() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(rezultat.stavka());
    }

    @PutMapping("/{id}")
    public AdminPregled.Stavka izmeni(@PathVariable long id, @Valid @RequestBody PodaciAlergena zahtev) {
        return service.izmeni(id, zahtev.naziv(), zahtev.opis(), zahtev.namirniceIds());
    }

    @DeleteMapping("/{id}")
    public AdminPregled.Stavka deaktiviraj(@PathVariable long id) {
        return service.deaktiviraj(id);
    }

    public record PodaciAlergena(
            @NotBlank @Size(max = 100) String naziv,
            @Size(max = 500) String opis,
            @NotNull Set<@NotNull @Positive Long> namirniceIds
    ) {
        public PodaciAlergena {
            naziv = naziv == null ? null : naziv.strip();
            opis = opis == null || opis.isBlank() ? null : opis.strip();
        }
    }
}
