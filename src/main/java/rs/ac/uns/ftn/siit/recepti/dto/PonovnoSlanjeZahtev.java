package rs.ac.uns.ftn.siit.recepti.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PonovnoSlanjeZahtev(
        @NotBlank(message = "Email je obavezan")
        @Email(message = "Email nije u ispravnom formatu")
        String email
) {
}
