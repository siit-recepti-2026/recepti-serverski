package rs.ac.uns.ftn.siit.recepti.exception;

//Mejl server nije dostupan ili je odbio poruku, vraca se HTTP 503
public class SlanjeMejlaException extends RuntimeException {

    public SlanjeMejlaException(String poruka, Throwable uzrok) {
        super(poruka, uzrok);
    }
}
