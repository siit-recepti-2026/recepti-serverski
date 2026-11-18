package rs.ac.uns.ftn.siit.recepti.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminTipoviIKategorijeController {
    private final AdminTipoviIKategorijeService service;

    public AdminTipoviIKategorijeController(AdminTipoviIKategorijeService service) {
        this.service = service;
    }

    @PostMapping("/{vrsta:tipovi-jela|tipovi-obroka|tipovi-ishrane|kategorije-namirnica}")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminPregled.Stavka dodaj(@PathVariable String vrsta, @Valid @RequestBody NovaStavka zahtev) {
        return service.dodaj(vrsta, zahtev.naziv());
    }

    public record NovaStavka(@NotBlank @Size(max = 100) String naziv) {
        public NovaStavka {
            if (naziv != null) {
                naziv = naziv.strip();
            }
        }
    }
}
