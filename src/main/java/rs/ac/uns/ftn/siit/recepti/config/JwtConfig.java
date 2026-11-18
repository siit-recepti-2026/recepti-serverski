package rs.ac.uns.ftn.siit.recepti.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

//JWT se potpisuje i proverava istim tajnim kljucem (HMAC SHA-256).
//Kljuc se cita iz promenljive okruzenja JWT_SECRET i nikad ne ulazi u repozitorijum.
@Configuration
public class JwtConfig {

    private final SecretKey kljuc;

    public JwtConfig(@Value("${app.jwt.tajna}") String tajna) {
        byte[] bajtovi = tajna.getBytes(StandardCharsets.UTF_8);
        if (bajtovi.length < 32) {
            throw new IllegalStateException("JWT_SECRET mora imati najmanje 32 karaktera");
        }
        this.kljuc = new SecretKeySpec(bajtovi, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(kljuc));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(kljuc).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
