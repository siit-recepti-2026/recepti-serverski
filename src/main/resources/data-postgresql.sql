BEGIN;

-- Istovremena pokretanja servera dele isti početni unos za ovu bazu.
SELECT pg_advisory_xact_lock(hashtext('recepti:T-16:pocetni-podaci'));

CREATE TABLE IF NOT EXISTS pocetni_unosi (
    oznaka VARCHAR(50) PRIMARY KEY
);

-- Oznaka i podaci se čuvaju u istoj transakciji. Kasnije izmene se ne prepisuju.
WITH prvi_unos AS (
    INSERT INTO pocetni_unosi (oznaka)
    VALUES ('T-16')
    ON CONFLICT (oznaka) DO NOTHING
    RETURNING oznaka
), tipovi_jela AS (
    INSERT INTO tip_jela (naziv, aktivan)
    SELECT podaci.naziv, TRUE
    FROM (VALUES
        ('Supa'),
        ('Čorba'),
        ('Testenina'),
        ('Pica'),
        ('Sendvič'),
        ('Burger'),
        ('Rižoto'),
        ('Jelo od mesa'),
        ('Jelo od ribe'),
        ('Jelo od povrća'),
        ('Pekarski proizvod'),
        ('Kolač'),
        ('Torta'),
        ('Palačinke')
    ) AS podaci(naziv)
    CROSS JOIN prvi_unos
    ON CONFLICT (naziv) DO NOTHING
), tipovi_obroka AS (
    INSERT INTO tip_obroka (naziv, aktivan)
    SELECT podaci.naziv, TRUE
    FROM (VALUES
        ('Doručak'),
        ('Ručak'),
        ('Večera'),
        ('Glavna jela'),
        ('Predjela'),
        ('Salate'),
        ('Deserti'),
        ('Pića')
    ) AS podaci(naziv)
    CROSS JOIN prvi_unos
    ON CONFLICT (naziv) DO NOTHING
), tipovi_ishrane AS (
    INSERT INTO tip_ishrane (naziv, aktivan)
    SELECT podaci.naziv, TRUE
    FROM (VALUES
        ('Vegetarijanska'),
        ('Veganska'),
        ('Posna'),
        ('Mrsna'),
        ('Hrono'),
        ('Pesketarijanska')
    ) AS podaci(naziv)
    CROSS JOIN prvi_unos
    ON CONFLICT (naziv) DO NOTHING
)
INSERT INTO kategorija_namirnice (naziv, aktivan)
SELECT podaci.naziv, TRUE
FROM (VALUES
    ('Povrće'),
    ('Voće'),
    ('Meso'),
    ('Riba'),
    ('Mlečni proizvodi'),
    ('Žitarice'),
    ('Začini'),
    ('Orašasti plodovi')
) AS podaci(naziv)
CROSS JOIN prvi_unos
ON CONFLICT (naziv) DO NOTHING;

COMMIT;
