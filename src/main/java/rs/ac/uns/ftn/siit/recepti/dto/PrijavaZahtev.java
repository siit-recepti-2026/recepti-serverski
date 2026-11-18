package rs.ac.uns.ftn.siit.recepti.dto;

import jakarta.validation.constraints.NotBlank;

public record PrijavaZahtev(
        @NotBlank(message = "Korisnicko ime ili email je obavezno")
        String korisnickoImeIliEmail,

        @NotBlank(message = "Lozinka je obavezna")
        String lozinka
) {
}
