package rs.ac.uns.ftn.siit.recepti.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

//Slike se sluze na /slike/** iz dva mesta, ovim redom:
//1. folder sa otpremljenim slikama (app.slike.folder), van repozitorijuma
//2. classpath:/static/slike/, gde su test slike koje jesu u repozitorijumu
//Zato u bazi moze da stoji samo putanja (/slike/ime.jpg), bez obzira gde je fajl
@Configuration
public class SlikeConfig implements WebMvcConfigurer {

    public static final String URL_PREFIKS = "/slike/";

    @Value("${app.slike.folder}")
    private String folder;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //Lokacija foldera mora da se zavrsi kosom crtom, inace Spring ne trazi fajlove unutar njega
        String folderUri = Path.of(folder).toAbsolutePath().normalize().toUri().toString();
        if (!folderUri.endsWith("/")) {
            folderUri += "/";
        }
        registry.addResourceHandler(URL_PREFIKS + "**")
                .addResourceLocations(folderUri, "classpath:/static/slike/");
    }
}
