package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.StatusPredloga;

import java.time.LocalDateTime;

@Entity
@Table(name = "predlog_kategorije")
@Getter
@Setter
@NoArgsConstructor
public class PredlogKategorije {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String element; //namirnica za koju se trazi nova kategorija

    @Column(nullable = false, length = 100)
    private String predlozeniNaziv;

    @Column(length = 1000)
    private String objasnjenje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPredloga status = StatusPredloga.NA_CEKANJU;

    @Column(length = 1000)
    private String odgovorAdministratora;

    @Column(nullable = false)
    private LocalDateTime datumPodnosenja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "podnosilac_id", nullable = false)
    private Korisnik podnosilac;

    //Kategorija koju je admin usvojio ili na koju je uputio korisnika
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usvojena_kategorija_id")
    private KategorijaNamirnice usvojenaKategorija;
}