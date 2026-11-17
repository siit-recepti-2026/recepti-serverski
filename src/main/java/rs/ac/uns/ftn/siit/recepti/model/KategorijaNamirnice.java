package rs.ac.uns.ftn.siit.recepti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kategorija_namirnice")
@Getter
@Setter
@NoArgsConstructor
public class KategorijaNamirnice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String naziv;

    @Column(nullable = false)
    private boolean aktivan = true; //admin deaktivira umesto da brise
}