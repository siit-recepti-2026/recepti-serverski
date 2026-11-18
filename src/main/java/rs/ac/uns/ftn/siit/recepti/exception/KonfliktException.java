package rs.ac.uns.ftn.siit.recepti.exception;

import lombok.Getter;

//Podatak vec postoji (npr. zauzeto korisnicko ime), vraca se HTTP 409
@Getter
public class KonfliktException extends RuntimeException {

    private final String polje;

    public KonfliktException(String polje, String poruka) {
        super(poruka);
        this.polje = polje;
    }
}
