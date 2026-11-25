package rs.ac.uns.ftn.siit.recepti.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.service.JwtService;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotifikacijaControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void prikazujeSamoNotifikacijePrimaocaOdNajnovijih() throws Exception {
        Long admin = jdbc.queryForObject("select id from korisnik where korisnicko_ime = 'admin'", Long.class);
        jdbc.update("""
                insert into korisnik
                  (korisnicko_ime, email, lozinka, uloga, aktiviran, datum_registracije, region_id)
                select 's316_korisnik', 's316@example.invalid', lozinka, 'KORISNIK', true,
                       datum_registracije, region_id
                from korisnik where id = ?
                """, admin);
        Long korisnik = jdbc.queryForObject(
                "select id from korisnik where korisnicko_ime = 's316_korisnik'", Long.class);
        LocalDateTime vreme = LocalDateTime.of(2026, 11, 20, 9, 30);
        sacuvaj(korisnik, "Ranija notifikacija", vreme.minusHours(1));
        sacuvaj(korisnik, "Novija notifikacija", vreme);
        sacuvaj(admin, "Notifikacija za administratora", vreme.plusHours(1));
        sacuvaj(korisnik, "Poslednja u istom trenutku", vreme);

        mvc.perform(get("/api/notifikacije").param("primalacId", admin.toString())
                        .with(jwt().jwt(token -> token.claim(JwtService.CLAIM_ID, korisnik))
                                .authorities(new SimpleGrantedAuthority("ROLE_KORISNIK"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].naslov").value("Poslednja u istom trenutku"))
                .andExpect(jsonPath("$[1].naslov").value("Novija notifikacija"))
                .andExpect(jsonPath("$[2].naslov").value("Ranija notifikacija"))
                .andExpect(jsonPath("$[0].poruka").value("Sadržaj notifikacije"))
                .andExpect(jsonPath("$[0].vremeKreiranja").isString());

        mvc.perform(get("/api/notifikacije")
                        .with(jwt().jwt(token -> token.claim(JwtService.CLAIM_ID, admin))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].naslov").value("Notifikacija za administratora"));
    }

    @Test
    void zahtevaPrijavu() throws Exception {
        mvc.perform(get("/api/notifikacije")).andExpect(status().isUnauthorized());
    }

    private void sacuvaj(long primalac, String naslov, LocalDateTime vreme) {
        jdbc.update("""
                insert into notifikacija (primalac_id, naslov, poruka, tip, vreme_kreiranja, procitana)
                values (?, ?, 'Sadržaj notifikacije', 'NOVI_PREDLOG', ?, false)
                """, primalac, naslov, vreme);
    }
}
