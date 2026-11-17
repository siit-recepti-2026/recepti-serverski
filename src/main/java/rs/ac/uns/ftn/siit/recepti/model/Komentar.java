package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "komentar")
@Getter
@Setter
@NoArgsConstructor
public class Komentar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String tekst;

    @Column(nullable = false)
    private LocalDateTime datumObjave;

    private LocalDateTime datumIzmene;

    @Column(nullable = false)
    private boolean uklonjen = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Korisnik autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    private Recept recept;

    //Popunjeno ako je ovo odgovor na drugi komentar
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roditelj_id")
    private Komentar roditelj;

    @OneToMany(mappedBy = "roditelj")
    private List<Komentar> odgovori = new ArrayList<>();

    @OneToMany(mappedBy = "komentar")
    private List<Fotografija> fotografije = new ArrayList<>();
}