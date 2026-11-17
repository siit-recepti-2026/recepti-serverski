package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.StatusListe;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lista_za_kupovinu")
@Getter
@Setter
@NoArgsConstructor
public class ListaZaKupovinu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String naziv;

    @Column(nullable = false)
    private LocalDateTime datumKreiranja;

    private LocalDateTime planiranoVremeKupovine;

    @Column(nullable = false)
    private boolean trajna = false; //trajna lista se ne arhivira automatski

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusListe status = StatusListe.AKTIVNA;

    @Column(nullable = false)
    private boolean podsetnikPoslat = false; //da podsetnik ne stigne vise puta

    private LocalDateTime datumArhiviranja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vlasnik_id", nullable = false)
    private Korisnik vlasnik;

    //Stavke se brisu fizicki zajedno sa listom ili kad ih korisnik ukloni
    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StavkaListe> stavke = new ArrayList<>();
}