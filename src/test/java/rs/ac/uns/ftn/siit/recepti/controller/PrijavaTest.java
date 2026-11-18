package rs.ac.uns.ftn.siit.recepti.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rs.ac.uns.ftn.siit.recepti.config.AdministratorInicijalizator;
import rs.ac.uns.ftn.siit.recepti.dto.LokacijaZahtev;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.model.enums.UlogaKorisnika;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;
import rs.ac.uns.ftn.siit.recepti.repository.LokacijaRepository;
import rs.ac.uns.ftn.siit.recepti.repository.VerifikacioniTokenRepository;
import rs.ac.uns.ftn.siit.recepti.service.LokacijaService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PrijavaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KorisnikRepository korisnikRepository;

    @Autowired
    private VerifikacioniTokenRepository tokenRepository;

    @Autowired
    private LokacijaRepository lokacijaRepository;

    @Autowired
    private LokacijaService lokacijaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdministratorInicijalizator administratorInicijalizator;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void ocistiBazu() {
        tokenRepository.deleteAll();
        korisnikRepository.deleteAll();
        lokacijaRepository.deleteAll();
    }

    private void napraviKorisnika(String korisnickoIme, String email, boolean aktiviran) {
        Korisnik korisnik = new Korisnik();
        korisnik.setKorisnickoIme(korisnickoIme);
        korisnik.setEmail(email);
        korisnik.setLozinka(passwordEncoder.encode("lozinka123"));
        korisnik.setUloga(UlogaKorisnika.KORISNIK);
        korisnik.setAktiviran(aktiviran);
        korisnik.setDatumRegistracije(LocalDateTime.now());
        korisnik.setRegion(lokacijaService.pronadjiIliNapravi(new LokacijaZahtev("Srbija", null, null, 44.0, 21.0)));
        korisnikRepository.save(korisnik);
    }

    private ResultActions prijavi(String korisnickoImeIliEmail, String lozinka) throws Exception {
        return mockMvc.perform(post("/api/auth/prijava")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"korisnickoImeIliEmail": "%s", "lozinka": "%s"}
                        """.formatted(korisnickoImeIliEmail, lozinka)));
    }

    private String token(String korisnickoImeIliEmail, String lozinka) throws Exception {
        String odgovor = prijavi(korisnickoImeIliEmail, lozinka)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(odgovor, "$.token");
    }

    @Test
    void prijavaKorisnickimImenomVracaTokenKojiRadi() throws Exception {
        napraviKorisnika("pera", "pera@primer.com", true);

        prijavi("pera", "lozinka123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.istice").isNotEmpty())
                .andExpect(jsonPath("$.korisnickoIme").value("pera"))
                .andExpect(jsonPath("$.uloga").value("KORISNIK"));

        mockMvc.perform(get("/api/korisnici/ja")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("pera", "lozinka123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.korisnickoIme").value("pera"))
                .andExpect(jsonPath("$.email").value("pera@primer.com"));
    }

    @Test
    void prijavaEmailomBezObziraNaVelikaSlova() throws Exception {
        napraviKorisnika("pera", "pera@primer.com", true);

        prijavi("PERA@Primer.com", "lozinka123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.korisnickoIme").value("pera"));
    }

    @Test
    void pogresnaLozinkaINepostojeciNalogDajuIstuGresku() throws Exception {
        napraviKorisnika("pera", "pera@primer.com", true);

        prijavi("pera", "pogresna123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.poruka").value("Pogresno korisnicko ime, email ili lozinka"));
        prijavi("nepostoji", "lozinka123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.poruka").value("Pogresno korisnicko ime, email ili lozinka"));
    }

    @Test
    void neaktiviranNalogNeMozeDaSePrijavi() throws Exception {
        napraviKorisnika("pera", "pera@primer.com", false);

        prijavi("pera", "lozinka123")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void neaktiviranNalogSaPogresnomLozinkomNeOtkrivaDaNalogPostoji() throws Exception {
        napraviKorisnika("pera", "pera@primer.com", false);

        prijavi("pera", "pogresna123").andExpect(status().isUnauthorized());
    }

    @Test
    void praznaPoljaVracaju400() throws Exception {
        prijavi("", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greske.korisnickoImeIliEmail").exists())
                .andExpect(jsonPath("$.greske.lozinka").exists());
    }

    @Test
    void zasticenaAdresaBezTokenaIliSaLosimTokenomVraca401() throws Exception {
        mockMvc.perform(get("/api/korisnici/ja"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/korisnici/ja").header(HttpHeaders.AUTHORIZATION, "Bearer nije.pravi.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stariTokenUZaglavljuNeSmetaPrijavi() throws Exception {
        napraviKorisnika("pera", "pera@primer.com", true);

        mockMvc.perform(post("/api/auth/prijava")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer istekao.ili.neispravan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"korisnickoImeIliEmail\": \"pera\", \"lozinka\": \"lozinka123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void obicanKorisnikNemaPristupAdministratorskomDelu() throws Exception {
        napraviKorisnika("pera", "pera@primer.com", true);

        mockMvc.perform(get("/api/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("pera", "lozinka123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void pocetniAdministratorSePrijavljujeIstoIImaPristupAdministratorskomDelu() throws Exception {
        administratorInicijalizator.napraviAkoNePostoji();

        prijavi("admin", "admin12345")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uloga").value("ADMINISTRATOR"));

        mockMvc.perform(get("/api/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("admin", "admin12345")))
                .andExpect(status().isOk());
    }

    @Test
    void administratorSePraviSamoJednom() {
        administratorInicijalizator.napraviAkoNePostoji();
        administratorInicijalizator.napraviAkoNePostoji();

        assertThat(korisnikRepository.findAll().stream()
                .filter(k -> k.getUloga() == UlogaKorisnika.ADMINISTRATOR)).hasSize(1);
    }
}
