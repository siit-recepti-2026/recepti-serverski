package rs.ac.uns.ftn.siit.recepti.exception;

//Pogresno korisnicko ime, email ili lozinka, vraca se HTTP 401
public class NeuspesnaPrijavaException extends RuntimeException {

    public NeuspesnaPrijavaException(String poruka) {
        super(poruka);
    }
}
