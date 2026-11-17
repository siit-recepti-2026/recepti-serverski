package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "recept")
@Getter
@Setter
@NoArgsConstructor
public class Recept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String naziv;

    @Column(nullable = false)
    private int brojPorcija;

    @Column(nullable = false)
    private int ukupnoVremePripremeMin;

    @Column(length = 5000)
    private String prica;

    @Column(length = 500)
    private String videoUrl;

    @Column(nullable = false)
    private LocalDateTime datumObjave;

    @Column(length = 2000)
    private String objasnjenjeIzmena; //samo kod modifikacija

    @Column(nullable = false)
    private boolean uklonjen = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Korisnik autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_autora_id", nullable = false)
    private Lokacija regionAutora; //region autora u trenutku objave

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poreklo_id")
    private Lokacija poreklo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_id")
    private Recept original; //popunjeno ako je ovo modifikacija drugog recepta

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tip_jela_id", nullable = false)
    private TipJela tipJela;

    @ManyToMany
    @JoinTable(
            name = "recept_tip_obroka",
            joinColumns = @JoinColumn(name = "recept_id"),
            inverseJoinColumns = @JoinColumn(name = "tip_obroka_id"))
    private Set<TipObroka> tipoviObroka = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "recept_tip_ishrane",
            joinColumns = @JoinColumn(name = "recept_id"),
            inverseJoinColumns = @JoinColumn(name = "tip_ishrane_id"))
    private Set<TipIshrane> tipoviIshrane = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "glavna_fotografija_id", nullable = false)
    private Fotografija glavnaFotografija;

    @OneToMany(mappedBy = "receptDodatne")
    private List<Fotografija> dodatneFotografije = new ArrayList<>();

    //Koraci se brisu logicki (polje obrisan), zato nema orphanRemoval
    @OneToMany(mappedBy = "recept", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("redniBroj ASC")
    private List<KorakPripreme> koraci = new ArrayList<>();

    //Sastojci se brisu fizicki zajedno sa receptom ili kad ih autor ukloni
    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StavkaRecepta> sastojci = new ArrayList<>();
}