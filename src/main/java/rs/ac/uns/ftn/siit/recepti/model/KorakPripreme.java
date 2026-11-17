package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.siit.recepti.model.enums.TipPripreme;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "korak_pripreme")
@Getter
@Setter
@NoArgsConstructor
public class KorakPripreme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int redniBroj;

    @Column(length = 200)
    private String naslov;

    @Column(nullable = false, length = 4000)
    private String opis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipPripreme tipPripreme;

    //obavezno osim za BEZ_TERMICKE_OBRADE i OSTALO (proverava se u servisu)
    private Integer trajanjeMin;

    @Column(nullable = false)
    private boolean obrisan = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    private Recept recept;

    @OneToMany(mappedBy = "korak")
    private List<Fotografija> fotografije = new ArrayList<>();
}