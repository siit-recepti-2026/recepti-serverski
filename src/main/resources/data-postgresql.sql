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

-- T-18 koristi kategorije iz T-16. Postojeći unosi zadržavaju svoje vrednosti.
WITH prvi_unos AS (
    INSERT INTO pocetni_unosi (oznaka)
    VALUES ('T-18')
    ON CONFLICT (oznaka) DO NOTHING
    RETURNING oznaka
), alergeni AS (
    INSERT INTO alergen (naziv, opis, aktivan)
    SELECT podaci.naziv, podaci.opis, TRUE
    FROM (VALUES
        ('Gluten', 'Žitarice koje sadrže gluten, poput pšenice, raži i ječma.'),
        ('Ljuskari', 'Ljuskari, poput kozica, rakova i jastoga.'),
        ('Jaja', 'Jaja i proizvodi od jaja.'),
        ('Riba', 'Riba i proizvodi od ribe.'),
        ('Kikiriki', 'Kikiriki i proizvodi od kikirikija.'),
        ('Soja', 'Soja i proizvodi od soje.'),
        ('Mleko', 'Mleko i mlečni proizvodi.'),
        ('Orašasti plodovi', 'Orašasti plodovi, poput oraha, badema i lešnika.'),
        ('Celer', 'Celer i proizvodi od celera.'),
        ('Slačica', 'Slačica i proizvodi od slačice, poput senfa.'),
        ('Susam', 'Susam i proizvodi od susama.'),
        ('Sumpor-dioksid i sulfiti', 'Sumpor-dioksid i sulfiti u hrani i piću.'),
        ('Lupina', 'Lupina i proizvodi od lupine.'),
        ('Mekušci', 'Mekušci, poput dagnji, lignji i hobotnice.')
    ) AS podaci(naziv, opis)
    CROSS JOIN prvi_unos
    ON CONFLICT (naziv) DO NOTHING
)
INSERT INTO namirnica (
    naziv, vrsta, podrazumevana_jedinica, dozvoljena_bez_kolicine, aktivan, kategorija_id
)
SELECT podaci.naziv, 'GENERICKA', podaci.jedinica, podaci.bez_kolicine, TRUE, kategorija.id
FROM (VALUES
    ('Krompir', 'Povrće', 'GRAM', FALSE),
    ('Crni luk', 'Povrće', 'KOMAD', FALSE),
    ('Beli luk', 'Povrće', 'GRAM', FALSE),
    ('Šargarepa', 'Povrće', 'KOMAD', FALSE),
    ('Paradajz', 'Povrće', 'KOMAD', FALSE),
    ('Paprika', 'Povrće', 'KOMAD', FALSE),
    ('Krastavac', 'Povrće', 'KOMAD', FALSE),
    ('Tikvica', 'Povrće', 'KOMAD', FALSE),
    ('Jabuka', 'Voće', 'KOMAD', FALSE),
    ('Banana', 'Voće', 'KOMAD', FALSE),
    ('Limun', 'Voće', 'KOMAD', FALSE),
    ('Pomorandža', 'Voće', 'KOMAD', FALSE),
    ('Jagoda', 'Voće', 'GRAM', FALSE),
    ('Borovnica', 'Voće', 'GRAM', FALSE),
    ('Pileći file', 'Meso', 'GRAM', FALSE),
    ('Ćureći file', 'Meso', 'GRAM', FALSE),
    ('Mlevena junetina', 'Meso', 'GRAM', FALSE),
    ('Svinjski file', 'Meso', 'GRAM', FALSE),
    ('Slanina', 'Meso', 'GRAM', FALSE),
    ('Šunka', 'Meso', 'GRAM', FALSE),
    ('Losos', 'Riba', 'GRAM', FALSE),
    ('Tuna', 'Riba', 'GRAM', FALSE),
    ('Pastrmka', 'Riba', 'GRAM', FALSE),
    ('Oslić', 'Riba', 'GRAM', FALSE),
    ('Sardina', 'Riba', 'GRAM', FALSE),
    ('Mleko', 'Mlečni proizvodi', 'MILILITAR', FALSE),
    ('Jogurt', 'Mlečni proizvodi', 'MILILITAR', FALSE),
    ('Maslac', 'Mlečni proizvodi', 'GRAM', FALSE),
    ('Kisela pavlaka', 'Mlečni proizvodi', 'GRAM', FALSE),
    ('Pavlaka za kuvanje', 'Mlečni proizvodi', 'MILILITAR', FALSE),
    ('Mocarela', 'Mlečni proizvodi', 'GRAM', FALSE),
    ('Parmezan', 'Mlečni proizvodi', 'GRAM', FALSE),
    ('Pšenično brašno', 'Žitarice', 'GRAM', FALSE),
    ('Kukuruzno brašno', 'Žitarice', 'GRAM', FALSE),
    ('Pirinač', 'Žitarice', 'GRAM', FALSE),
    ('Ovsene pahuljice', 'Žitarice', 'GRAM', FALSE),
    ('Kus-kus', 'Žitarice', 'GRAM', FALSE),
    ('Testenina', 'Žitarice', 'GRAM', FALSE),
    ('So', 'Začini', 'KASICICA', TRUE),
    ('Crni biber', 'Začini', 'KASICICA', TRUE),
    ('Mlevena paprika', 'Začini', 'KASICICA', TRUE),
    ('Origano', 'Začini', 'KASICICA', TRUE),
    ('Bosiljak', 'Začini', 'KASICICA', TRUE),
    ('Peršun', 'Začini', 'GRAM', TRUE),
    ('Cimet', 'Začini', 'KASICICA', TRUE),
    ('Orah', 'Orašasti plodovi', 'GRAM', FALSE),
    ('Badem', 'Orašasti plodovi', 'GRAM', FALSE),
    ('Lešnik', 'Orašasti plodovi', 'GRAM', FALSE),
    ('Indijski orah', 'Orašasti plodovi', 'GRAM', FALSE),
    ('Pistać', 'Orašasti plodovi', 'GRAM', FALSE)
) AS podaci(naziv, kategorija, jedinica, bez_kolicine)
JOIN kategorija_namirnice AS kategorija ON kategorija.naziv = podaci.kategorija
CROSS JOIN prvi_unos
ON CONFLICT (naziv) DO NOTHING;

COMMIT;
