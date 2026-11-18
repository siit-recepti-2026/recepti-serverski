package rs.ac.uns.ftn.siit.recepti.dto;

import jakarta.validation.constraints.NotBlank;

public record AktivacijaZahtev(
        @NotBlank(message = "Token je obavezan")
        String token
) {
}
