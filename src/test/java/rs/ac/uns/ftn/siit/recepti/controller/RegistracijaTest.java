package rs.ac.uns.ftn.siit.recepti.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.model.VerifikacioniToken;
import rs.ac.uns.ftn.siit.recepti.model.enums.TipTokena;
import rs.ac.uns.ftn.siit.recepti.model.enums.UlogaKorisnika;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;
import rs.ac.uns.ftn.siit.recepti.repository.LokacijaRepository;
import rs.ac.uns.ftn.siit.recepti.repository.VerifikacioniTokenRepository;
import rs.ac.uns.ftn.siit.recepti.service.TokenService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegistracijaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KorisnikRepository korisnikRepository;

    @Autowired
    private VerifikacioniTokenRepository tokenRepository;

    @Autowired
    private LokacijaRepository lokacijaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    //Pravi mejl se ne salje, samo se proverava sta bi bilo poslato
    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void ocistiBazu() {
        tokenRepository.deleteAll();
        korisnikRepository.deleteAll();
        lokacijaRepository.deleteAll();
    }

    private String zahtev(String korisnickoIme, String email, String lozinka) {
        return """
                {
                  "korisnickoIme": "%s",
                  "email": "%s",
                  "lozinka": "%s",
                  "region": {
                    "drzava": "Srbija",
                    "region": "Vojvodina",
                    "grad": "Novi Sad",
                    "geografskaSirina": 45.2671,
                    "geografskaDuzina": 19.8335
                  }
                }
                """.formatted(korisnickoIme, email, lozinka);
    }

    private void registruj(String korisnickoIme, String email) throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev(korisnickoIme, email, "lozinka123")))
                .andExpect(status().isCreated());
    }

    @Test
    void uspesnaRegistracijaCuvaNeaktiviranogKorisnikaISaljeAktivacioniMejl() throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("pera", "Pera@Primer.com", "lozinka123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.poruka").exists());

        List<Korisnik> korisnici = korisnikRepository.findAll();
        assertThat(korisnici).hasSize(1);
        Korisnik korisnik = korisnici.getFirst();
        assertThat(korisnik.getKorisnickoIme()).isEqualTo("pera");
        assertThat(korisnik.getEmail()).isEqualTo("pera@primer.com");
        assertThat(korisnik.isAktiviran()).isFalse();
        assertThat(korisnik.getUloga()).isEqualTo(UlogaKorisnika.KORISNIK);
        assertThat(korisnik.getLozinka()).isNotEqualTo("lozinka123");
        assertThat(passwordEncoder.matches("lozinka123", korisnik.getLozinka())).isTrue();
        assertThat(lokacijaRepository.count()).isEqualTo(1);

        ArgumentCaptor<SimpleMailMessage> mejl = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mejl.capture());
        assertThat(mejl.getValue().getTo()).containsExactly("pera@primer.com");

        //U mejlu je token, a u bazi samo njegov hes
        Matcher link = Pattern.compile("token=([A-Za-z0-9_-]+)").matcher(mejl.getValue().getText());
        assertThat(link.find()).isTrue();
        String tokenIzMejla = link.group(1);

        List<VerifikacioniToken> tokeni = tokenRepository.findAll();
        assertThat(tokeni).hasSize(1);
        VerifikacioniToken token = tokeni.getFirst();
        assertThat(token.getTip()).isEqualTo(TipTokena.AKTIVACIJA);
        assertThat(token.getToken()).isEqualTo(tokenService.hesiraj(tokenIzMejla));
        assertThat(token.getToken()).isNotEqualTo(tokenIzMejla);
        assertThat(token.getVaziDo()).isAfter(LocalDateTime.now().plusHours(23));
        assertThat(token.getKorisnik().getId()).isEqualTo(korisnik.getId());
    }

    @Test
    void zauzetoKorisnickoImeVraca409() throws Exception {
        registruj("pera", "pera@primer.com");

        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("PERA", "drugi@primer.com", "lozinka123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.greske.korisnickoIme").value("Korisnicko ime je zauzeto"));

        assertThat(korisnikRepository.count()).isEqualTo(1);
    }

    @Test
    void zauzetEmailVraca409() throws Exception {
        registruj("pera", "pera@primer.com");

        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("mika", "PERA@primer.com", "lozinka123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.greske.email").exists());
    }

    @Test
    void neispravniPodaciVracaju400SaGreskomZaSvakoPolje() throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("p", "nije-email", "kratka")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greske.korisnickoIme").exists())
                .andExpect(jsonPath("$.greske.email").exists())
                .andExpect(jsonPath("$.greske.lozinka").exists());

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        assertThat(korisnikRepository.count()).isZero();
    }

    @Test
    void lozinkaBezBrojaNijeDozvoljena() throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("pera", "pera@primer.com", "samoslova")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greske.lozinka").value("Lozinka mora sadrzati bar jedno slovo i bar jedan broj"));
    }

    @Test
    void regionJeObavezan() throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"korisnickoIme": "pera", "email": "pera@primer.com", "lozinka": "lozinka123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greske.region").exists());
    }

    @Test
    void istaLokacijaSeKoristiPonovo() throws Exception {
        registruj("pera", "pera@primer.com");
        registruj("mika", "mika@primer.com");

        assertThat(korisnikRepository.count()).isEqualTo(2);
        assertThat(lokacijaRepository.count()).isEqualTo(1);
    }

    @Test
    void akoMejlNeMozeDaSePosaljeKorisnikSeNeCuva() throws Exception {
        doThrow(new MailSendException("SMTP nije dostupan")).when(mailSender).send(any(SimpleMailMessage.class));

        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("pera", "pera@primer.com", "lozinka123")))
                .andExpect(status().isServiceUnavailable());

        assertThat(korisnikRepository.count()).isZero();
        assertThat(tokenRepository.count()).isZero();
    }

    @Test
    void proveraDostupnostiKorisnickogImenaIEmaila() throws Exception {
        registruj("pera", "pera@primer.com");

        mockMvc.perform(get("/api/auth/dostupnost/korisnicko-ime").param("vrednost", "Pera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dostupno").value(false));
        mockMvc.perform(get("/api/auth/dostupnost/korisnicko-ime").param("vrednost", "mika"))
                .andExpect(jsonPath("$.dostupno").value(true));
        mockMvc.perform(get("/api/auth/dostupnost/email").param("vrednost", "PERA@primer.com"))
                .andExpect(jsonPath("$.dostupno").value(false));
        mockMvc.perform(get("/api/auth/dostupnost/email").param("vrednost", "mika@primer.com"))
                .andExpect(jsonPath("$.dostupno").value(true));
    }

    @Test
    void zasticenaAdresaBezPrijaveVraca401() throws Exception {
        mockMvc.perform(get("/api/admin/tipovi-jela"))
                .andExpect(status().isUnauthorized());
    }
}
