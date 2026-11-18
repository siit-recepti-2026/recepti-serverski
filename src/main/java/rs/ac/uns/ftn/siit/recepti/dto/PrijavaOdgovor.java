package rs.ac.uns.ftn.siit.recepti.dto;

import java.time.Instant;

//Veb aplikacija cuva token u localStorage i iz uloge zna da li da prikaze administratorske opcije
public record PrijavaOdgovor(String token, Instant istice, String korisnickoIme, String uloga) {
}
