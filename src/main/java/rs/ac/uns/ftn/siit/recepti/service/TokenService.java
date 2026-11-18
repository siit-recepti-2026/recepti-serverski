package rs.ac.uns.ftn.siit.recepti.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

//Pravi tokene za aktivaciju i reset lozinke.
//Korisnik dobija token mejlom, a u bazi se cuva samo njegov SHA-256 hes,
//pa neko ko vidi bazu ne moze da iskoristi tokene iz nje.
@Service
public class TokenService {

    private final SecureRandom random = new SecureRandom();

    public String generisiToken() {
        byte[] bajtovi = new byte[32];
        random.nextBytes(bajtovi);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bajtovi);
    }

    public String hesiraj(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nije dostupan", e);
        }
    }
}
