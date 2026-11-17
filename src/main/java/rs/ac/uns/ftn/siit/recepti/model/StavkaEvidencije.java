package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.MernaJedinica;

import java.time.LocalDate;

@Entity
@Table(name = "stavka_evidencije")
@Getter
@Setter
@NoArgsConstructor
public class StavkaEvidencije {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double kolicina;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MernaJedinica mernaJedinica;

    @Column(length = 100)
    private String brend;

    private LocalDate rokTrajanja; //samo datum, bez vremena

    @Column(nullable = false)
    private boolean obavestenjePoslato = false; //da obavestenje o roku ne stigne vise puta

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vlasnik_id", nullable = false)
    private Korisnik vlasnik;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "namirnica_id", nullable = false)
    private Namirnica namirnica;
}