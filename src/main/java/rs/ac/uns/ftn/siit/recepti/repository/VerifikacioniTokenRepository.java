package rs.ac.uns.ftn.siit.recepti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.model.VerifikacioniToken;
import rs.ac.uns.ftn.siit.recepti.model.enums.TipTokena;

import java.util.Optional;

public interface VerifikacioniTokenRepository extends JpaRepository<VerifikacioniToken, Long> {

    //token je SHA-256 hes, isti kao sto je sacuvan pri pravljenju
    Optional<VerifikacioniToken> findByTokenAndTip(String token, TipTokena tip);

    void deleteByKorisnikAndTip(Korisnik korisnik, TipTokena tip);
}
