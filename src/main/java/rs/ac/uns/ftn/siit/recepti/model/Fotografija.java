package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "fotografija")
@Getter
@Setter
@NoArgsConstructor
public class Fotografija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String url; //putanja do fajla na serveru

    @Column(nullable = false)
    private LocalDateTime datumPostavljanja;

    @Column(nullable = false)
    private boolean uklonjena = false;

    //Popunjeno samo ako je ovo dodatna fotografija recepta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recept_id")
    private Recept receptDodatne;

    //Popunjeno samo ako je ovo fotografija koraka
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "korak_id")
    private KorakPripreme korak;

    // veza ka komentaru se dodaje kad napravimo entitet Komentar
}