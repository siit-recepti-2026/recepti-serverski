package rs.ac.uns.ftn.siit.recepti.exception;

//Poslat je fajl koji nije dozvoljen tip slike (npr. pdf ili gif), vraca se HTTP 415
public class NepodrzanTipFajlaException extends RuntimeException {

    public NepodrzanTipFajlaException(String poruka) {
        super(poruka);
    }
}
