package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.MernaJedinica;

@Entity
@Table(name = "stavka_liste")
@Getter
@Setter
@NoArgsConstructor
public class StavkaListe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double kolicina;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MernaJedinica mernaJedinica;

    @Column(nullable = false)
    private boolean kupljena = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lista_id", nullable = false)
    private ListaZaKupovinu lista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "namirnica_id", nullable = false)
    private Namirnica namirnica;
}