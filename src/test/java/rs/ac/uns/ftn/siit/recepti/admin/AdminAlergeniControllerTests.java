package rs.ac.uns.ftn.siit.recepti.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAlergeniControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void cuvaOpisIVezeIDeaktiviraAlergenBezGubitkaVeza() throws Exception {
        jdbc.update("insert into kategorija_namirnice (naziv, aktivan) values ('S3-04 test', true)");
        Long kategorija = jdbc.queryForObject(
                "select id from kategorija_namirnice where naziv = 'S3-04 test'", Long.class);
        long mleko = dodajNamirnicu("S304 mleko", kategorija);
        long jogurt = dodajNamirnicu("S304 jogurt", kategorija);

        mvc.perform(get("/api/admin/alergeni/namirnice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.naziv == 'S304 mleko')].id").isNotEmpty());
        mvc.perform(post("/api/admin/alergeni").contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("S304 alergen", "Prvi opis", mleko)))
                .andExpect(status().isCreated());
        Long id = jdbc.queryForObject("select id from alergen where naziv = 'S304 alergen'", Long.class);

        mvc.perform(put("/api/admin/alergeni/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("S304 alergen", "Novi opis", jogurt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opis").value("Novi opis"));
        assertEquals(jogurt, jdbc.queryForObject(
                "select namirnica_id from namirnica_alergen where alergen_id = ?", Long.class, id));
        mvc.perform(get("/api/admin/pregled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alergeni[?(@.naziv == 'S304 alergen')].namirniceIds[0]").isNotEmpty());

        mvc.perform(delete("/api/admin/alergeni/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aktivan").value(false));
        assertEquals(jogurt, jdbc.queryForObject(
                "select namirnica_id from namirnica_alergen where alergen_id = ?", Long.class, id));
        mvc.perform(post("/api/admin/alergeni").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"naziv":"S304 alergen","namirniceIds":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aktivan").value(true))
                .andExpect(jsonPath("$.opis").value("Novi opis"))
                .andExpect(jsonPath("$.namirniceIds").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void odbijaNepostojecuNamirnicu() throws Exception {
        mvc.perform(post("/api/admin/alergeni").contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("S304 neispravan", "Opis", Long.MAX_VALUE)))
                .andExpect(status().isBadRequest());
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from alergen where naziv = 'S304 neispravan'", Integer.class));
    }

    @Test
    @WithMockUser(roles = "KORISNIK")
    void obicanKorisnikNemaPristupUpravljanjuAlergenima() throws Exception {
        mvc.perform(get("/api/admin/alergeni/namirnice")).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/alergeni").contentType(MediaType.APPLICATION_JSON)
                        .content(zahtev("S304 zabranjen", "Opis", 1L)))
                .andExpect(status().isForbidden());
    }

    private long dodajNamirnicu(String naziv, long kategorija) {
        jdbc.update("""
                insert into namirnica
                  (naziv, vrsta, podrazumevana_jedinica, dozvoljena_bez_kolicine, aktivan, kategorija_id)
                values (?, 'GENERICKA', 'GRAM', false, true, ?)
                """, naziv, kategorija);
        return jdbc.queryForObject("select id from namirnica where naziv = ?", Long.class, naziv);
    }

    private String zahtev(String naziv, String opis, long namirnica) {
        return """
                {"naziv":"%s","opis":"%s","namirniceIds":[%d]}
                """.formatted(naziv, opis, namirnica);
    }
}
