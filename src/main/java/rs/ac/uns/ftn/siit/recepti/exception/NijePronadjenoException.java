package rs.ac.uns.ftn.siit.recepti.exception;

//Trazeni podatak (npr. korisnik po korisnickom imenu) ne postoji, vraca se HTTP 404
public class NijePronadjenoException extends RuntimeException {

    public NijePronadjenoException(String poruka) {
        super(poruka);
    }
}
