package rs.ac.uns.ftn.siit.recepti.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistracijaZahtev(
        @NotBlank(message = "Korisnicko ime je obavezno")
        @Size(min = 3, max = 50, message = "Korisnicko ime mora imati od 3 do 50 karaktera")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                message = "Korisnicko ime moze sadrzati samo slova bez kvacica, brojeve i znakove . _ -")
        String korisnickoIme,

        @NotBlank(message = "Email je obavezan")
        @Email(message = "Email nije u ispravnom formatu")
        @Size(max = 255, message = "Email moze imati najvise 255 karaktera")
        String email,

        //BCrypt uzima u obzir najvise 72 bajta, zato je to gornja granica
        @NotBlank(message = "Lozinka je obavezna")
        @Size(min = 8, max = 72, message = "Lozinka mora imati od 8 do 72 karaktera")
        @Pattern(regexp = "^(?=.*\\p{L})(?=.*\\d).+$",
                message = "Lozinka mora sadrzati bar jedno slovo i bar jedan broj")
        String lozinka,

        @NotNull(message = "Region je obavezan")
        @Valid
        LokacijaZahtev region
) {
}
