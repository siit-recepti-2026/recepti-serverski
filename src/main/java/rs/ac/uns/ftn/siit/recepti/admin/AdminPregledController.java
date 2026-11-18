package rs.ac.uns.ftn.siit.recepti.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminPregledController {
    private final AdminPregledService service;

    public AdminPregledController(AdminPregledService service) {
        this.service = service;
    }

    @GetMapping("/pregled")
    public AdminPregled pregled() {
        return service.pregled();
    }
}
