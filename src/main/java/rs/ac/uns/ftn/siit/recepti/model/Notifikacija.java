package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.TipNotifikacije;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifikacija")
@Getter
@Setter
@NoArgsConstructor
public class Notifikacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String naslov;

    @Column(nullable = false, length = 1000)
    private String poruka;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipNotifikacije tip;

    @Column(nullable = false)
    private LocalDateTime vremeKreiranja;

    @Column(nullable = false)
    private boolean procitana = false;

    //Uz tip odredjuje na sta klik vodi (prijava, predlog, stavka evidencije ili lista)
    private Long idSadrzaja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primalac_id", nullable = false)
    private Korisnik primalac;
}