package rs.ac.uns.ftn.siit.recepti.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.siit.recepti.config.SlikeConfig;
import rs.ac.uns.ftn.siit.recepti.dto.SlikaOdgovor;
import rs.ac.uns.ftn.siit.recepti.exception.NeispravanZahtevException;
import rs.ac.uns.ftn.siit.recepti.exception.NepodrzanTipFajlaException;
import rs.ac.uns.ftn.siit.recepti.model.Fotografija;
import rs.ac.uns.ftn.siit.recepti.repository.FotografijaRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

//Zajednicko otpremanje slika: profilna slika, fotografije recepta, koraka i komentara
@Service
public class SlikaService {

    static final String PORUKA_TIP = "Dozvoljene su samo slike u formatu jpg, png ili webp";

    private final FotografijaRepository fotografijaRepository;
    private final Path folder;

    public SlikaService(FotografijaRepository fotografijaRepository,
                        @Value("${app.slike.folder}") String folder) {
        this.fotografijaRepository = fotografijaRepository;
        this.folder = Path.of(folder).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.folder);
        } catch (IOException e) {
            throw new IllegalStateException("Folder za slike ne moze da se napravi: " + this.folder, e);
        }
    }

    @Transactional
    public SlikaOdgovor sacuvaj(MultipartFile fajl) {
        if (fajl == null || fajl.isEmpty()) {
            throw new NeispravanZahtevException("Fajl nije poslat ili je prazan");
        }

        //Ekstenzija i Content-Type stizu od klijenta, pa se proveravaju oba
        TipSlike tip = TipSlike.poEkstenziji(ekstenzija(fajl.getOriginalFilename()))
                .orElseThrow(() -> new NepodrzanTipFajlaException(PORUKA_TIP));
        TipSlike tipIzZaglavlja = TipSlike.poContentType(fajl.getContentType())
                .orElseThrow(() -> new NepodrzanTipFajlaException(PORUKA_TIP));
        if (tip != tipIzZaglavlja) {
            throw new NeispravanZahtevException("Ekstenzija fajla i tip slike se ne poklapaju");
        }

        //Oba podatka mogu da se lazno postave, zato se proveravaju i prvi bajtovi samog fajla
        byte[] sadrzaj = procitaj(fajl);
        if (!tip.odgovaraSadrzaju(sadrzaj)) {
            throw new NeispravanZahtevException("Sadrzaj fajla nije ispravna slika");
        }

        //Originalno ime se ne koristi, pa ne moze da sadrzi putanju (npr. ../) ni da se poklopi sa drugim fajlom
        String ime = UUID.randomUUID() + "." + tip.ekstenzija;

        Fotografija fotografija = new Fotografija();
        fotografija.setUrl(SlikeConfig.URL_PREFIKS + ime);
        fotografija.setDatumPostavljanja(LocalDateTime.now());
        fotografijaRepository.save(fotografija);

        //Ako upis na disk ne uspe, izuzetak ponistava i upis u bazu, pa ne ostaje red bez fajla
        try {
            Files.write(folder.resolve(ime), sadrzaj, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("Slika ne moze da se sacuva na disk", e);
        }
        return new SlikaOdgovor(fotografija.getId(), fotografija.getUrl());
    }

    private static byte[] procitaj(MultipartFile fajl) {
        try {
            return fajl.getBytes();
        } catch (IOException e) {
            throw new NeispravanZahtevException("Fajl ne moze da se procita");
        }
    }

    private static String ekstenzija(String imeFajla) {
        if (imeFajla == null || !imeFajla.contains(".")) {
            return "";
        }
        return imeFajla.substring(imeFajla.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    //Dozvoljeni tipovi slika, sa ekstenzijama, Content-Type vrednostima i pocetnim bajtovima fajla
    enum TipSlike {
        JPG("jpg", Set.of("jpg", "jpeg"), Set.of("image/jpeg", "image/jpg")),
        PNG("png", Set.of("png"), Set.of("image/png")),
        WEBP("webp", Set.of("webp"), Set.of("image/webp"));

        private static final byte[] JPG_POTPIS = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        private static final byte[] PNG_POTPIS = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
        private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
        private static final byte[] WEBP_OZNAKA = {'W', 'E', 'B', 'P'};

        private final String ekstenzija;
        private final Set<String> ekstenzije;
        private final Set<String> contentTypes;

        TipSlike(String ekstenzija, Set<String> ekstenzije, Set<String> contentTypes) {
            this.ekstenzija = ekstenzija;
            this.ekstenzije = ekstenzije;
            this.contentTypes = contentTypes;
        }

        static Optional<TipSlike> poEkstenziji(String ekstenzija) {
            return Arrays.stream(values()).filter(t -> t.ekstenzije.contains(ekstenzija)).findFirst();
        }

        static Optional<TipSlike> poContentType(String contentType) {
            if (contentType == null) {
                return Optional.empty();
            }
            //"image/jpeg; charset=..." -> "image/jpeg"
            String tip = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
            return Arrays.stream(values()).filter(t -> t.contentTypes.contains(tip)).findFirst();
        }

        boolean odgovaraSadrzaju(byte[] sadrzaj) {
            return switch (this) {
                case JPG -> pocinjeNa(sadrzaj, 0, JPG_POTPIS);
                case PNG -> pocinjeNa(sadrzaj, 0, PNG_POTPIS);
                //WebP: "RIFF", 4 bajta velicine, pa "WEBP"
                case WEBP -> pocinjeNa(sadrzaj, 0, RIFF) && pocinjeNa(sadrzaj, 8, WEBP_OZNAKA);
            };
        }

        private static boolean pocinjeNa(byte[] sadrzaj, int od, byte[] ocekivano) {
            if (sadrzaj.length < od + ocekivano.length) {
                return false;
            }
            return Arrays.equals(sadrzaj, od, od + ocekivano.length, ocekivano, 0, ocekivano.length);
        }
    }
}
