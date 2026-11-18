package rs.ac.uns.ftn.siit.recepti.exception;

//Zahtev je formalno ispravan, ali ne moze da se izvrsi (npr. istekao link), vraca se HTTP 400
public class NeispravanZahtevException extends RuntimeException {

    public NeispravanZahtevException(String poruka) {
        super(poruka);
    }
}
