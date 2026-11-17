package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.StatusPrijave;

import java.time.LocalDateTime;

@Entity
@Table(name = "prijava")
@Getter
@Setter
@NoArgsConstructor
public class Prijava {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String razlog;

    @Column(nullable = false)
    private LocalDateTime datumPodnosenja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPrijave status = StatusPrijave.NOVA;

    @Column(length = 1000)
    private String razlogOdbijanja;

    private LocalDateTime datumObrade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "podnosilac_id", nullable = false)
    private Korisnik podnosilac;

    //Tacno jedno od sledeca tri je popunjeno (proverava se u servisu)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijavljen_recept_id")
    private Recept prijavljenRecept;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijavljen_komentar_id")
    private Komentar prijavljenKomentar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prijavljena_fotografija_id")
    private Fotografija prijavljenaFotografija;
}