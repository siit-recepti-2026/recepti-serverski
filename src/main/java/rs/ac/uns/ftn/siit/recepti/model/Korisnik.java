package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.UlogaKorisnika;

import java.time.LocalDateTime;

@Entity
@Table(name = "korisnik")
@Getter
@Setter
@NoArgsConstructor
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String korisnickoIme;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String lozinka; //cuva se hes lozinke, nikad cista lozinka

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UlogaKorisnika uloga = UlogaKorisnika.KORISNIK;

    @Column(nullable = false)
    private boolean aktiviran = false;

    @Column(length = 1000)
    private String biografija;

    @Column(nullable = false)
    private LocalDateTime datumRegistracije;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Lokacija region;

    // profilnaSlika se dodaje kad napravimo entitet Fotografija
}