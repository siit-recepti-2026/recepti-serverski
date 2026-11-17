package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable // red se nikad ne menja; pri promeni regiona pravi se nova lokacija
@Table(name = "lokacija")
@Getter
@NoArgsConstructor
public class Lokacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String drzava;

    private String region;

    private String grad;

    @Column(nullable = false)
    private Double geografskaSirina;

    @Column(nullable = false)
    private Double geografskaDuzina;

    public Lokacija(String drzava, String region, String grad,
                    Double geografskaSirina, Double geografskaDuzina) {
        this.drzava = drzava;
        this.region = region;
        this.grad = grad;
        this.geografskaSirina = geografskaSirina;
        this.geografskaDuzina = geografskaDuzina;
    }
}