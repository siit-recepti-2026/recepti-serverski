package rs.ac.uns.ftn.siit.recepti.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LokacijaZahtev(
        @NotBlank(message = "Drzava je obavezna")
        @Size(max = 100, message = "Drzava moze imati najvise 100 karaktera")
        String drzava,

        @Size(max = 100, message = "Region moze imati najvise 100 karaktera")
        String region,

        @Size(max = 100, message = "Grad moze imati najvise 100 karaktera")
        String grad,

        @NotNull(message = "Geografska sirina je obavezna")
        @DecimalMin(value = "-90.0", message = "Geografska sirina mora biti izmedju -90 i 90")
        @DecimalMax(value = "90.0", message = "Geografska sirina mora biti izmedju -90 i 90")
        Double geografskaSirina,

        @NotNull(message = "Geografska duzina je obavezna")
        @DecimalMin(value = "-180.0", message = "Geografska duzina mora biti izmedju -180 i 180")
        @DecimalMax(value = "180.0", message = "Geografska duzina mora biti izmedju -180 i 180")
        Double geografskaDuzina
) {
}
