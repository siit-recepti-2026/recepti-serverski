package rs.ac.uns.ftn.siit.recepti.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//Korisnicko ime namerno nije polje ovog zahteva, pa ne moze da se promeni ni rucno poslatim JSON-om.
//profilnaSlikaId: id slike otpremljene preko POST /api/slike; null znaci da slika ostaje ista.
//ukloniProfilnuSliku: true uklanja trenutnu sliku (tada se profilnaSlikaId ne gleda); ako polje nije poslato, slika se ne uklanja.
//Boolean, a ne boolean, jer Jackson ne prihvata izostavljeno polje za primitivni tip.
public record IzmenaProfilaZahtev(
        @Size(max = 1000, message = "Biografija moze imati najvise 1000 karaktera")
        String biografija,

        @NotNull(message = "Region je obavezan")
        @Valid
        LokacijaZahtev region,

        Long profilnaSlikaId,

        Boolean ukloniProfilnuSliku) {

    public boolean trebaUkloniti() {
        return Boolean.TRUE.equals(ukloniProfilnuSliku);
    }
}
