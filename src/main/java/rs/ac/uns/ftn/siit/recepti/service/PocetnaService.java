package rs.ac.uns.ftn.siit.recepti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.siit.recepti.dto.ReceptKarticaOdgovor;
import rs.ac.uns.ftn.siit.recepti.model.Recept;
import rs.ac.uns.ftn.siit.recepti.repository.OcenaRepository;
import rs.ac.uns.ftn.siit.recepti.repository.OcenaRepository.OceneRecepta;
import rs.ac.uns.ftn.siit.recepti.repository.ReceptRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//Recepti za pocetnu stranu: popularni (podrazumevani prikaz) i najnoviji
@Service
@RequiredArgsConstructor
public class PocetnaService {

    //Koliko recepata se prikazuje u svakom prikazu
    static final int BROJ_RECEPATA = 12;

    private final ReceptRepository receptRepository;
    private final OcenaRepository ocenaRepository;

    //Najveca prosecna ocena u proteklih mesec dana; recepti bez ocena u tom periodu se ne prikazuju
    @Transactional(readOnly = true)
    public List<ReceptKarticaOdgovor> popularni() {
        LocalDateTime preMesecDana = LocalDateTime.now().minusMonths(1);
        List<Long> ids = ocenaRepository.najboljeOcenjeniReceptiOd(
                preMesecDana, PageRequest.of(0, BROJ_RECEPATA));

        //findAllById ne cuva redosled, pa se recepti vracaju u redosled iz upita
        Map<Long, Recept> recepti = receptRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Recept::getId, Function.identity()));
        List<Recept> poredjani = ids.stream().map(recepti::get).toList();

        return kartice(poredjani);
    }

    //Recepti od najnovijeg ka starijem, po datumu objave
    @Transactional(readOnly = true)
    public List<ReceptKarticaOdgovor> najnoviji() {
        List<Recept> recepti = receptRepository.findByUklonjenFalseOrderByDatumObjaveDesc(
                PageRequest.of(0, BROJ_RECEPATA));
        return kartice(recepti);
    }

    //Pravi kartice i dodaje prosecnu ocenu i broj svih ocena, jednim upitom za sve recepte
    private List<ReceptKarticaOdgovor> kartice(List<Recept> recepti) {
        List<Long> ids = recepti.stream().map(Recept::getId).toList();
        Map<Long, OceneRecepta> ocene = ocenaRepository.oceneRecepata(ids).stream()
                .collect(Collectors.toMap(OceneRecepta::getReceptId, Function.identity()));

        return recepti.stream()
                .map(recept -> {
                    OceneRecepta ocena = ocene.get(recept.getId());
                    double prosek = ocena != null ? ocena.getProsek() : 0;
                    long broj = ocena != null ? ocena.getBroj() : 0;
                    return ReceptKarticaOdgovor.od(recept, prosek, broj);
                })
                .toList();
    }
}