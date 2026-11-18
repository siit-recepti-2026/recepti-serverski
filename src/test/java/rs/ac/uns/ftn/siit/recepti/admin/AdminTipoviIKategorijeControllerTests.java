package rs.ac.uns.ftn.siit.recepti.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.siit.recepti.config.SecurityConfig;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTipoviIKategorijeController.class)
@Import(SecurityConfig.class)
class AdminTipoviIKategorijeControllerTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdminTipoviIKategorijeService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void administratorDodajeStavkuSaObrezanimNazivom() throws Exception {
        when(service.dodaj("tipovi-jela", "Pica")).thenReturn(new AdminPregled.Stavka(1L, "Pica", true));

        mvc.perform(post("/api/admin/tipovi-jela")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"naziv\":\"  Pica  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.naziv").value("Pica"))
                .andExpect(jsonPath("$.aktivan").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void odbijaPrazanNaziv() throws Exception {
        mvc.perform(post("/api/admin/kategorije-namirnica")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"naziv\":\"   \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "KORISNIK")
    void obicanKorisnikNeMozeDaDodaje() throws Exception {
        mvc.perform(post("/api/admin/tipovi-jela")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"naziv\":\"Pica\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void neprijavljeniKorisnikNeMozeDaDodaje() throws Exception {
        mvc.perform(post("/api/admin/tipovi-jela")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"naziv\":\"Pica\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
}
