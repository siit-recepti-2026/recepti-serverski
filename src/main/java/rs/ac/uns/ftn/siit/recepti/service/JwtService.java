package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_ID = "id";
    public static final String CLAIM_ULOGA = "uloga";

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.trajanje-sati}")
    private long trajanjeSati;

    public String napraviToken(Korisnik korisnik, Instant istice) {
        Instant sada = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("recepti")
                .subject(korisnik.getKorisnickoIme())
                .issuedAt(sada)
                .expiresAt(istice)
                .claim(CLAIM_ID, korisnik.getId())
                .claim(CLAIM_ULOGA, korisnik.getUloga().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Instant vremeIsteka() {
        return Instant.now().plus(trajanjeSati, ChronoUnit.HOURS);
    }
}
