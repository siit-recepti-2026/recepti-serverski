package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.MernaJedinica;

@Entity
@Table(name = "stavka_recepta")
@Getter
@Setter
@NoArgsConstructor
public class StavkaRecepta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double kolicina; //moze biti prazno, npr. so po ukusu

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MernaJedinica mernaJedinica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    private Recept recept;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "namirnica_id", nullable = false)
    private Namirnica namirnica;
}