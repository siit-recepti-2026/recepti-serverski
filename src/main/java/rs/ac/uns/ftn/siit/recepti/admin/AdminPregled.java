package rs.ac.uns.ftn.siit.recepti.admin;

import java.util.List;

public record AdminPregled(
        List<Stavka> tipoviJela,
        List<Stavka> tipoviObroka,
        List<Stavka> tipoviIshrane,
        List<Stavka> kategorijeNamirnica,
        List<Stavka> alergeni
) {
    public record Stavka(Long id, String naziv, boolean aktivan, String opis) {
        public Stavka(Long id, String naziv, boolean aktivan) {
            this(id, naziv, aktivan, null);
        }
    }
}
