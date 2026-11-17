package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alergen")
@Getter
@Setter
@NoArgsConstructor
public class Alergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String naziv;

    @Column(length = 500)
    private String opis;

    @Column(nullable = false)
    private boolean aktivan = true;
}