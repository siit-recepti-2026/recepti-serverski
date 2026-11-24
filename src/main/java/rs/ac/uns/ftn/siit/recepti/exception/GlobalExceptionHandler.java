package rs.ac.uns.ftn.siit.recepti.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import rs.ac.uns.ftn.siit.recepti.dto.GreskaOdgovor;

import java.util.LinkedHashMap;
import java.util.Map;

//Pretvara izuzetke u odgovor sa HTTP kodom i porukom, umesto podrazumevane Spring greske
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size:5MB}")
    private DataSize maxVelicinaFajla;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GreskaOdgovor> neispravniPodaci(MethodArgumentNotValidException e) {
        Map<String, String> greske = new LinkedHashMap<>();
        for (FieldError greska : e.getBindingResult().getFieldErrors()) {
            greske.putIfAbsent(greska.getField(), greska.getDefaultMessage());
        }
        return odgovor(HttpStatus.BAD_REQUEST, "Podaci nisu ispravni", greske);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GreskaOdgovor> necitljivZahtev(HttpMessageNotReadableException e) {
        return odgovor(HttpStatus.BAD_REQUEST, "Telo zahteva nije ispravan JSON", Map.of());
    }

    @ExceptionHandler(KonfliktException.class)
    public ResponseEntity<GreskaOdgovor> konflikt(KonfliktException e) {
        return odgovor(HttpStatus.CONFLICT, e.getMessage(), Map.of(e.getPolje(), e.getMessage()));
    }

    @ExceptionHandler(NeispravanZahtevException.class)
    public ResponseEntity<GreskaOdgovor> neispravanZahtev(NeispravanZahtevException e) {
        return odgovor(HttpStatus.BAD_REQUEST, e.getMessage(), Map.of());
    }

    @ExceptionHandler(NeuspesnaPrijavaException.class)
    public ResponseEntity<GreskaOdgovor> neuspesnaPrijava(NeuspesnaPrijavaException e) {
        return odgovor(HttpStatus.UNAUTHORIZED, e.getMessage(), Map.of());
    }

    @ExceptionHandler(NeaktiviranNalogException.class)
    public ResponseEntity<GreskaOdgovor> neaktiviranNalog(NeaktiviranNalogException e) {
        return odgovor(HttpStatus.FORBIDDEN, e.getMessage(), Map.of());
    }

    //Dva istovremena zahteva sa istim podacima: provera prodje za oba, ali baza odbije drugi
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GreskaOdgovor> narusenoOgranicenje(DataIntegrityViolationException e) {
        return odgovor(HttpStatus.CONFLICT, "Podatak vec postoji", Map.of());
    }

    @ExceptionHandler(NepodrzanTipFajlaException.class)
    public ResponseEntity<GreskaOdgovor> nepodrzanTipFajla(NepodrzanTipFajlaException e) {
        return odgovor(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage(), Map.of("fajl", e.getMessage()));
    }

    //Fajl je veci od spring.servlet.multipart.max-file-size
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GreskaOdgovor> prevelikFajl(MaxUploadSizeExceededException e) {
        String poruka = "Slika moze imati najvise " + maxVelicinaFajla.toMegabytes() + " MB";
        return odgovor(HttpStatus.CONTENT_TOO_LARGE, poruka, Map.of("fajl", poruka));
    }

    //Multipart zahtev bez dela "fajl"
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<GreskaOdgovor> nedostajeFajl(MissingServletRequestPartException e) {
        String poruka = "Fajl nije poslat";
        return odgovor(HttpStatus.BAD_REQUEST, poruka, Map.of(e.getRequestPartName(), poruka));
    }

    //Telo zahteva nije ispravan multipart (npr. prekinut prenos)
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<GreskaOdgovor> neispravanMultipart(MultipartException e) {
        return odgovor(HttpStatus.BAD_REQUEST, "Fajl nije ispravno poslat", Map.of());
    }

    //Na endpoint za slike poslat je JSON, ili na JSON endpoint nesto drugo
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<GreskaOdgovor> nepodrzanFormat(HttpMediaTypeNotSupportedException e) {
        return odgovor(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Format zahteva nije podrzan", Map.of());
    }

    @ExceptionHandler(SlanjeMejlaException.class)
    public ResponseEntity<GreskaOdgovor> slanjeMejla(SlanjeMejlaException e) {
        log.error("Slanje mejla nije uspelo", e);
        return odgovor(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), Map.of());
    }

    private ResponseEntity<GreskaOdgovor> odgovor(HttpStatus status, String poruka, Map<String, String> greske) {
        return ResponseEntity.status(status).body(new GreskaOdgovor(status.value(), poruka, greske));
    }
}
