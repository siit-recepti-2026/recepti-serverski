package rs.ac.uns.ftn.siit.recepti.exception;

//Podaci su tacni, ali nalog jos nije aktiviran preko mejla, vraca se HTTP 403
public class NeaktiviranNalogException extends RuntimeException {

    public NeaktiviranNalogException(String poruka) {
        super(poruka);
    }
}
