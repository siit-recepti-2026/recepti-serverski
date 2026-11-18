package rs.ac.uns.ftn.siit.recepti.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//Postoji samo u testovima, da bi se proverilo pravilo za /api/admin/** pre nego sto postoje pravi admin endpointi
@RestController
class TestAdminController {

    @GetMapping("/api/admin/test")
    String test() {
        return "ok";
    }
}
