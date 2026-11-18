package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.siit.recepti.exception.SlanjeMejlaException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.posiljalac}")
    private String posiljalac;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.aktivacija.trajanje-sati}")
    private long trajanjeAktivacijeSati;

    public void posaljiAktivacioniMejl(String email, String korisnickoIme, String token) {
        //Link vodi na stranicu veb aplikacije, a ona salje token serveru
        String link = frontendUrl + "/auth/aktivacija?token=" + token;

        SimpleMailMessage poruka = new SimpleMailMessage();
        poruka.setFrom(posiljalac);
        poruka.setTo(email);
        poruka.setSubject("Aktivacija naloga - Recepti");
        poruka.setText("""
                Zdravo %s,

                hvala na registraciji. Nalog aktiviras klikom na link:
                %s

                Link vazi %d sati. Ako se nisi ti registrovao, slobodno ignorisi ovaj mejl.
                """.formatted(korisnickoIme, link, trajanjeAktivacijeSati));

        try {
            mailSender.send(poruka);
        } catch (MailException e) {
            throw new SlanjeMejlaException("Slanje aktivacionog mejla nije uspelo, pokusajte ponovo kasnije", e);
        }
    }
}
