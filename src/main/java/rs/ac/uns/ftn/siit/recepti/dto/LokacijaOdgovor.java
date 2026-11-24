package rs.ac.uns.ftn.siit.recepti.dto;

import rs.ac.uns.ftn.siit.recepti.model.Lokacija;

//Lokacija za prikaz; region i grad mogu biti prazni
public record LokacijaOdgovor(String drzava, String region, String grad) {

    public static LokacijaOdgovor od(Lokacija lokacija) {
        return new LokacijaOdgovor(lokacija.getDrzava(), lokacija.getRegion(), lokacija.getGrad());
    }
}
