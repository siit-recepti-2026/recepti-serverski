package rs.ac.uns.ftn.siit.recepti.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.siit.recepti.config.SecurityConfig;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPregledController.class)
@Import(SecurityConfig.class)
class AdminPregledControllerTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdminPregledService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void neprijavljeniKorisnikNemaPristup() throws Exception {
        mvc.perform(get("/api/admin/pregled")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void obicanKorisnikNemaPristup() throws Exception {
        pripremiToken("test-korisnik", "KORISNIK");
        mvc.perform(get("/api/admin/pregled").header(HttpHeaders.AUTHORIZATION, "Bearer test-korisnik"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void administratorSaJwtTokenomMozeDaVidiSvihPetListi() throws Exception {
        pripremiToken("test-admin", "ADMINISTRATOR");
        when(service.pregled()).thenReturn(new AdminPregled(
                List.of(new AdminPregled.Stavka(1L, "Pica", true)),
                List.of(new AdminPregled.Stavka(2L, "Ručak", true)),
                List.of(new AdminPregled.Stavka(3L, "Posna", true)),
                List.of(new AdminPregled.Stavka(4L, "Voće", true)),
                List.of(new AdminPregled.Stavka(5L, "Gluten", false, "Pšenica"))
        ));

        mvc.perform(get("/api/admin/pregled").header(HttpHeaders.AUTHORIZATION, "Bearer test-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoviJela[0].naziv").value("Pica"))
                .andExpect(jsonPath("$.tipoviObroka[0].naziv").value("Ručak"))
                .andExpect(jsonPath("$.tipoviIshrane[0].naziv").value("Posna"))
                .andExpect(jsonPath("$.kategorijeNamirnica[0].naziv").value("Voće"))
                .andExpect(jsonPath("$.alergeni[0].naziv").value("Gluten"))
                .andExpect(jsonPath("$.alergeni[0].opis").value("Pšenica"))
                .andExpect(jsonPath("$.alergeni[0].aktivan").value(false));
    }

    private void pripremiToken(String token, String uloga) {
        when(jwtDecoder.decode(token)).thenReturn(Jwt.withTokenValue(token)
                .header("alg", "HS256")
                .subject(token)
                .claim("uloga", uloga)
                .build());
    }
}
