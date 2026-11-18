package rs.ac.uns.ftn.siit.recepti.dto;

import java.util.Map;

//greske: polje forme -> poruka, da bi veb forma mogla da prikaze gresku ispod tog polja
public record GreskaOdgovor(int status, String poruka, Map<String, String> greske) {
}
