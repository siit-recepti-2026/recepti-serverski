package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocena",
        uniqueConstraints = @UniqueConstraint(columnNames = {"korisnik_id", "recept_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Ocena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int vrednost; //1 do 5, proverava se u servisu

    @Column(nullable = false)
    private LocalDateTime datum; //azurira se i kad korisnik izmeni ocenu

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "korisnik_id", nullable = false)
    private Korisnik korisnik;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    private Recept recept;
}