package rs.ac.uns.ftn.siit.recepti.dto;

import java.time.LocalDateTime;

public record NotifikacijaOdgovor(Long id, String naslov, String poruka, LocalDateTime vremeKreiranja) {
}
