package rs.ac.uns.ftn.siit.recepti.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rs.ac.uns.ftn.siit.recepti.dto.GreskaOdgovor;

import java.util.LinkedHashMap;
import java.util.Map;

//Pretvara izuzetke u odgovor sa HTTP kodom i porukom, umesto podrazumevane Spring greske
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(SlanjeMejlaException.class)
    public ResponseEntity<GreskaOdgovor> slanjeMejla(SlanjeMejlaException e) {
        log.error("Slanje mejla nije uspelo", e);
        return odgovor(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), Map.of());
    }

    private ResponseEntity<GreskaOdgovor> odgovor(HttpStatus status, String poruka, Map<String, String> greske) {
        return ResponseEntity.status(status).body(new GreskaOdgovor(status.value(), poruka, greske));
    }
}
