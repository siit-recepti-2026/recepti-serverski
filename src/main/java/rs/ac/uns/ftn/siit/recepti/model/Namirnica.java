package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.MernaJedinica;
import rs.ac.uns.ftn.siit.recepti.model.enums.VrstaNamirnice;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "namirnica")
@Getter
@Setter
@NoArgsConstructor
public class Namirnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String naziv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VrstaNamirnice vrsta;

    @Column(length = 20)
    private String bojaTeksta; // npr. "#E53935", samo za brendirane

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MernaJedinica podrazumevanaJedinica;

    @Column(nullable = false)
    private boolean dozvoljenaBezKolicine = false; // npr. "so po ukusu"

    @Column(nullable = false)
    private boolean aktivan = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kategorija_id", nullable = false)
    private KategorijaNamirnice kategorija;

    @ManyToMany
    @JoinTable(
            name = "namirnica_alergen",
            joinColumns = @JoinColumn(name = "namirnica_id"),
            inverseJoinColumns = @JoinColumn(name = "alergen_id"))
    private Set<Alergen> alergeni = new HashSet<>();
}