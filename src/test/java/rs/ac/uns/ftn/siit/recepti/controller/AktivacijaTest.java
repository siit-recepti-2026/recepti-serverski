package rs.ac.uns.ftn.siit.recepti.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rs.ac.uns.ftn.siit.recepti.model.Korisnik;
import rs.ac.uns.ftn.siit.recepti.model.VerifikacioniToken;
import rs.ac.uns.ftn.siit.recepti.model.enums.UlogaKorisnika;
import rs.ac.uns.ftn.siit.recepti.repository.KorisnikRepository;
import rs.ac.uns.ftn.siit.recepti.repository.LokacijaRepository;
import rs.ac.uns.ftn.siit.recepti.repository.VerifikacioniTokenRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AktivacijaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KorisnikRepository korisnikRepository;

    @Autowired
    private VerifikacioniTokenRepository tokenRepository;

    @Autowired
    private LokacijaRepository lokacijaRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void ocistiBazu() {
        tokenRepository.deleteAll();
        korisnikRepository.deleteAll();
        lokacijaRepository.deleteAll();
    }

    //Registruje korisnika i vraca token iz poslatog mejla
    private String registrujIUzmiToken(String korisnickoIme, String email) throws Exception {
        mockMvc.perform(post("/api/auth/registracija")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "korisnickoIme": "%s",
                                  "email": "%s",
                                  "lozinka": "lozinka123",
                                  "region": {"drzava": "Srbija", "geografskaSirina": 44.0, "geografskaDuzina": 21.0}
                                }
                                """.formatted(korisnickoIme, email)))
                .andExpect(status().isCreated());
        return poslednjiTokenIzMejla();
    }

    private String poslednjiTokenIzMejla() {
        ArgumentCaptor<SimpleMailMessage> mejl = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(mejl.capture());
        List<SimpleMailMessage> mejlovi = mejl.getAllValues();
        Matcher link = Pattern.compile("token=([A-Za-z0-9_-]+)")
                .matcher(mejlovi.getLast().getText());
        assertThat(link.find()).isTrue();
        return link.group(1);
    }

    private ResultActions aktiviraj(String token) throws Exception {
        return mockMvc.perform(post("/api/auth/aktivacija")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"%s\"}".formatted(token)));
    }

    private ResultActions ponovoPosalji(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/aktivacija/ponovo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"%s\"}".formatted(email)));
    }

    private Korisnik korisnik() {
        return korisnikRepository.findByEmailIgnoreCase("pera@primer.com").orElseThrow();
    }

    @Test
    void ispravanLinkAktiviraNalogSaUlogomKorisnikIBriseToken() throws Exception {
        String token = registrujIUzmiToken("pera", "pera@primer.com");
        assertThat(korisnik().isAktiviran()).isFalse();

        aktiviraj(token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poruka").exists());

        assertThat(korisnik().isAktiviran()).isTrue();
        assertThat(korisnik().getUloga()).isEqualTo(UlogaKorisnika.KORISNIK);
        assertThat(tokenRepository.count()).isZero();
    }

    @Test
    void linkNeMozeDaSeIskoristiDvaPuta() throws Exception {
        String token = registrujIUzmiToken("pera", "pera@primer.com");
        aktiviraj(token).andExpect(status().isOk());

        aktiviraj(token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.poruka").value("Link za aktivaciju nije ispravan ili je vec iskoriscen"));
    }

    @Test
    void nepostojeciTokenVraca400() throws Exception {
        registrujIUzmiToken("pera", "pera@primer.com");

        aktiviraj("izmisljen-token").andExpect(status().isBadRequest());

        assertThat(korisnik().isAktiviran()).isFalse();
    }

    @Test
    void prazanTokenVraca400SaGreskomZaPolje() throws Exception {
        aktiviraj("")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greske.token").exists());
    }

    @Test
    void istekaoLinkNeAktiviraNalog() throws Exception {
        String token = registrujIUzmiToken("pera", "pera@primer.com");
        VerifikacioniToken sacuvan = tokenRepository.findAll().getFirst();
        sacuvan.setVaziDo(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(sacuvan);

        aktiviraj(token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.poruka").value("Link za aktivaciju je istekao. Zatrazite novi link."));

        assertThat(korisnik().isAktiviran()).isFalse();
    }

    @Test
    void ponovnoSlanjePraviNoviLinkAStariViseNeVazi() throws Exception {
        String stariToken = registrujIUzmiToken("pera", "pera@primer.com");

        ponovoPosalji("PERA@primer.com").andExpect(status().isOk());
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
        String noviToken = poslednjiTokenIzMejla();
        assertThat(noviToken).isNotEqualTo(stariToken);
        assertThat(tokenRepository.count()).isEqualTo(1);

        aktiviraj(stariToken).andExpect(status().isBadRequest());
        aktiviraj(noviToken).andExpect(status().isOk());
        assertThat(korisnik().isAktiviran()).isTrue();
    }

    @Test
    void ponovnoSlanjeZaNepostojeciEmailNeSaljeMejlAliVracaIstiOdgovor() throws Exception {
        ponovoPosalji("nepostoji@primer.com")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poruka").exists());

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void ponovnoSlanjeZaVecAktiviranNalogNeSaljeMejl() throws Exception {
        String token = registrujIUzmiToken("pera", "pera@primer.com");
        aktiviraj(token).andExpect(status().isOk());
        clearInvocations(mailSender);

        ponovoPosalji("pera@primer.com").andExpect(status().isOk());

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
