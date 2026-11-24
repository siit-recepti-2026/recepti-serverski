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


-- T-19 Test korisnici, recepti, fotografije i ocene.
-- Svaka naredba radi samo dok oznaka T-19 ne postoji, a oznaka se upisuje poslednja, u istoj transakciji.
-- Test korisnici: marko, ana i jelena, lozinka za sve je lozinka123.
-- Datumi su relativni (NOW() minus broj dana), pa su ocene sveze u trenutku prvog pokretanja.
-- Pred KT2 obrisati bazu i ponovo pokrenuti server, da bi ocene opet bile iz poslednjih mesec dana.

-- Kategorija za namirnice koje ne spadaju u postojece kategorije (jaja, ulje, secer...)
INSERT INTO kategorija_namirnice (naziv, aktivan)
SELECT 'Ostalo', TRUE
    WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19')
ON CONFLICT (naziv) DO NOTHING;

-- Namirnice potrebne za test recepte, ukljucujuci jednu brendiranu (Vegeta)
INSERT INTO namirnica (
    naziv, vrsta, boja_teksta, podrazumevana_jedinica, dozvoljena_bez_kolicine, aktivan, kategorija_id
)
SELECT podaci.naziv, podaci.vrsta, podaci.boja, podaci.jedinica, podaci.bez_kolicine, TRUE, kategorija.id
FROM (VALUES
          ('Jaja', 'GENERICKA', NULL, 'KOMAD', FALSE, 'Ostalo'),
          ('Suncokretovo ulje', 'GENERICKA', NULL, 'MILILITAR', FALSE, 'Ostalo'),
          ('Maslinovo ulje', 'GENERICKA', NULL, 'MILILITAR', FALSE, 'Ostalo'),
          ('Šećer', 'GENERICKA', NULL, 'GRAM', FALSE, 'Ostalo'),
          ('Suvi kvasac', 'GENERICKA', NULL, 'GRAM', FALSE, 'Ostalo'),
          ('Prašak za pecivo', 'GENERICKA', NULL, 'GRAM', FALSE, 'Ostalo'),
          ('Voda', 'GENERICKA', NULL, 'MILILITAR', TRUE, 'Ostalo'),
          ('Celer', 'GENERICKA', NULL, 'GRAM', FALSE, 'Povrće'),
          ('Vegeta', 'BRENDIRANA', '#B3261E', 'KASICICA', TRUE, 'Začini')
     ) AS podaci(naziv, vrsta, boja, jedinica, bez_kolicine, kategorija)
         JOIN kategorija_namirnice AS kategorija ON kategorija.naziv = podaci.kategorija
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19')
    ON CONFLICT (naziv) DO NOTHING;

-- Veze namirnica sa alergenima, iz kojih se racunaju alergeni recepta
INSERT INTO namirnica_alergen (namirnica_id, alergen_id)
SELECT namirnica.id, alergen.id
FROM (VALUES
          ('Pšenično brašno', 'Gluten'),
          ('Testenina', 'Gluten'),
          ('Ovsene pahuljice', 'Gluten'),
          ('Kus-kus', 'Gluten'),
          ('Jaja', 'Jaja'),
          ('Losos', 'Riba'),
          ('Tuna', 'Riba'),
          ('Pastrmka', 'Riba'),
          ('Oslić', 'Riba'),
          ('Sardina', 'Riba'),
          ('Mleko', 'Mleko'),
          ('Jogurt', 'Mleko'),
          ('Maslac', 'Mleko'),
          ('Kisela pavlaka', 'Mleko'),
          ('Pavlaka za kuvanje', 'Mleko'),
          ('Mocarela', 'Mleko'),
          ('Parmezan', 'Mleko'),
          ('Orah', 'Orašasti plodovi'),
          ('Badem', 'Orašasti plodovi'),
          ('Lešnik', 'Orašasti plodovi'),
          ('Indijski orah', 'Orašasti plodovi'),
          ('Pistać', 'Orašasti plodovi'),
          ('Celer', 'Celer'),
          ('Vegeta', 'Celer')
     ) AS podaci(namirnica, alergen)
         JOIN namirnica ON namirnica.naziv = podaci.namirnica
         JOIN alergen ON alergen.naziv = podaci.alergen
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19')
    ON CONFLICT DO NOTHING;

-- Lokacije: regioni test korisnika (isti kao na registraciji) i Italija kao poreklo recepata
INSERT INTO lokacija (drzava, region, grad, geografska_sirina, geografska_duzina)
SELECT podaci.drzava, podaci.region, NULL, podaci.sirina, podaci.duzina
FROM (VALUES
          ('Srbija', 'Vojvodina', 45.2671, 19.8335),
          ('Srbija', 'Beograd', 44.8125, 20.4612),
          ('Srbija', 'Južna i Istočna Srbija', 43.3209, 21.8958),
          ('Italija', NULL, 41.9028, 12.4964)
     ) AS podaci(drzava, region, sirina, duzina)
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19')
  AND NOT EXISTS (
    SELECT 1 FROM lokacija
    WHERE lokacija.drzava = podaci.drzava
      AND lokacija.region IS NOT DISTINCT FROM podaci.region
        AND lokacija.grad IS NULL
);

-- Test korisnici, svi aktivirani, lozinka lozinka123 (cuva se BCrypt hes)
INSERT INTO korisnik (
    korisnicko_ime, email, lozinka, uloga, aktiviran, biografija, datum_registracije, region_id
)
SELECT podaci.korisnicko_ime, podaci.email, podaci.lozinka, 'KORISNIK', TRUE, podaci.biografija,
       NOW() - INTERVAL '70 days',
    (SELECT MIN(lokacija.id) FROM lokacija
    WHERE lokacija.drzava = 'Srbija' AND lokacija.region = podaci.region AND lokacija.grad IS NULL)
FROM (VALUES
    ('marko', 'marko@recepti.test', '$2a$10$/W2RV9GIBBWqAMMrdma9IeY2XxsDlv1W4/y70QAfxERQUyMVFHhna',
    'Volim domaću kuhinju i recepte iz Vojvodine.', 'Vojvodina'),
    ('ana', 'ana@recepti.test', '$2a$10$MlW0z3h5PR5NpFQ2/dXpvOM8MjW5tGpMfI/r88r8EOt5igejjkUtK',
    'Najviše volim brze doručke i salate.', 'Beograd'),
    ('jelena', 'jelena@recepti.test', '$2a$10$kq6cixSuWqawRfjMhN7ZMOgWS0Xj9ZLth117bQ/qNv4rRPmmsysh2',
    'Pečem kolače vikendom i isprobavam italijanska jela.', 'Južna i Istočna Srbija')
    ) AS podaci(korisnicko_ime, email, lozinka, biografija, region)
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19')
ON CONFLICT DO NOTHING;

-- Glavne fotografije recepata, fajlovi su u src/main/resources/static/slike
INSERT INTO fotografija (url, datum_postavljanja, uklonjena)
SELECT podaci.url, NOW() - podaci.dana * INTERVAL '1 day', FALSE
FROM (VALUES
    ('/slike/palacinke.jpg', 12),
    ('/slike/pileca-supa.jpg', 60),
    ('/slike/spagete-bolonjeze.jpg', 25),
    ('/slike/losos-krompir.jpg', 18),
    ('/slike/kaprese-salata.jpg', 8),
    ('/slike/pica-margarita.jpg', 5),
    ('/slike/proja.jpg', 3),
    ('/slike/pita-jabuke.jpg', 1)
    ) AS podaci(url, dana)
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19');

-- Recepti, datum objave je pre zadatog broja dana, a region autora je trenutni region autora
INSERT INTO recept (
    naziv, broj_porcija, ukupno_vreme_pripreme_min, prica, datum_objave, uklonjen,
    autor_id, region_autora_id, poreklo_id, tip_jela_id, glavna_fotografija_id
)
SELECT podaci.naziv, podaci.porcije, podaci.vreme, podaci.prica, NOW() - podaci.dana * INTERVAL '1 day', FALSE,
    autor.id, autor.region_id,
    (SELECT MIN(lokacija.id) FROM lokacija
    WHERE lokacija.drzava = podaci.poreklo AND lokacija.region IS NULL AND lokacija.grad IS NULL),
    tip_jela.id,
    (SELECT MAX(fotografija.id) FROM fotografija WHERE fotografija.url = podaci.slika)
FROM (VALUES
    ('Palačinke', 'ana', 'Palačinke', 4, 30, 12, NULL, '/slike/palacinke.jpg',
    'Recept moje bake. Najlepše su sa domaćim džemom od šljiva.'),
    ('Pileća supa', 'marko', 'Supa', 4, 60, 60, NULL, '/slike/pileca-supa.jpg',
    'Supa koja se kod nas kuva svake nedelje.'),
    ('Špagete bolonjeze', 'marko', 'Testenina', 4, 45, 25, 'Italija', '/slike/spagete-bolonjeze.jpg', NULL),
    ('Losos sa krompirom', 'jelena', 'Jelo od ribe', 2, 40, 18, NULL, '/slike/losos-krompir.jpg', NULL),
    ('Kaprese salata', 'ana', 'Jelo od povrća', 2, 10, 8, 'Italija', '/slike/kaprese-salata.jpg', NULL),
    ('Pica margarita', 'jelena', 'Pica', 2, 90, 5, 'Italija', '/slike/pica-margarita.jpg',
    'Najjednostavnija pica, a i dalje najbolja.'),
    ('Proja', 'marko', 'Pekarski proizvod', 6, 50, 3, NULL, '/slike/proja.jpg', NULL),
    ('Pita sa jabukama', 'jelena', 'Kolač', 8, 75, 1, NULL, '/slike/pita-jabuke.jpg', NULL)
    ) AS podaci(naziv, autor, tip_jela, porcije, vreme, dana, poreklo, slika, prica)
    JOIN korisnik AS autor ON autor.korisnicko_ime = podaci.autor
    JOIN tip_jela ON tip_jela.naziv = podaci.tip_jela
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19');

-- Tipovi obroka recepata
INSERT INTO recept_tip_obroka (recept_id, tip_obroka_id)
SELECT recept.id, tip_obroka.id
FROM (VALUES
          ('Palačinke', 'Doručak'),
          ('Palačinke', 'Deserti'),
          ('Pileća supa', 'Ručak'),
          ('Špagete bolonjeze', 'Ručak'),
          ('Špagete bolonjeze', 'Glavna jela'),
          ('Losos sa krompirom', 'Večera'),
          ('Losos sa krompirom', 'Glavna jela'),
          ('Kaprese salata', 'Predjela'),
          ('Kaprese salata', 'Salate'),
          ('Pica margarita', 'Večera'),
          ('Proja', 'Doručak'),
          ('Pita sa jabukama', 'Deserti')
     ) AS podaci(recept, tip)
         JOIN recept ON recept.naziv = podaci.recept
         JOIN korisnik AS autor ON autor.id = recept.autor_id AND autor.email LIKE '%@recepti.test'
         JOIN tip_obroka ON tip_obroka.naziv = podaci.tip
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19');

-- Tipovi ishrane recepata
INSERT INTO recept_tip_ishrane (recept_id, tip_ishrane_id)
SELECT recept.id, tip_ishrane.id
FROM (VALUES
          ('Palačinke', 'Vegetarijanska'),
          ('Pileća supa', 'Mrsna'),
          ('Špagete bolonjeze', 'Mrsna'),
          ('Losos sa krompirom', 'Pesketarijanska'),
          ('Kaprese salata', 'Vegetarijanska'),
          ('Pica margarita', 'Vegetarijanska'),
          ('Proja', 'Vegetarijanska'),
          ('Pita sa jabukama', 'Vegetarijanska')
     ) AS podaci(recept, tip)
         JOIN recept ON recept.naziv = podaci.recept
         JOIN korisnik AS autor ON autor.id = recept.autor_id AND autor.email LIKE '%@recepti.test'
         JOIN tip_ishrane ON tip_ishrane.naziv = podaci.tip
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19');

-- Sastojci, kolicina i merna jedinica su prazni kod namirnica "po ukusu"
INSERT INTO stavka_recepta (kolicina, merna_jedinica, recept_id, namirnica_id)
SELECT podaci.kolicina, podaci.jedinica, recept.id, namirnica.id
FROM (VALUES
          ('Palačinke', 'Pšenično brašno', 250, 'GRAM'),
          ('Palačinke', 'Mleko', 500, 'MILILITAR'),
          ('Palačinke', 'Jaja', 2, 'KOMAD'),
          ('Palačinke', 'Šećer', 20, 'GRAM'),
          ('Palačinke', 'Suncokretovo ulje', 50, 'MILILITAR'),
          ('Palačinke', 'So', NULL, NULL),
          ('Pileća supa', 'Pileći file', 400, 'GRAM'),
          ('Pileća supa', 'Šargarepa', 2, 'KOMAD'),
          ('Pileća supa', 'Crni luk', 1, 'KOMAD'),
          ('Pileća supa', 'Celer', 100, 'GRAM'),
          ('Pileća supa', 'Voda', 2, 'LITAR'),
          ('Pileća supa', 'Vegeta', 1, 'KASICICA'),
          ('Pileća supa', 'Peršun', NULL, NULL),
          ('Pileća supa', 'So', NULL, NULL),
          ('Špagete bolonjeze', 'Testenina', 400, 'GRAM'),
          ('Špagete bolonjeze', 'Mlevena junetina', 400, 'GRAM'),
          ('Špagete bolonjeze', 'Paradajz', 4, 'KOMAD'),
          ('Špagete bolonjeze', 'Crni luk', 1, 'KOMAD'),
          ('Špagete bolonjeze', 'Beli luk', 10, 'GRAM'),
          ('Špagete bolonjeze', 'Maslinovo ulje', 30, 'MILILITAR'),
          ('Špagete bolonjeze', 'Parmezan', 50, 'GRAM'),
          ('Špagete bolonjeze', 'So', NULL, NULL),
          ('Špagete bolonjeze', 'Crni biber', NULL, NULL),
          ('Losos sa krompirom', 'Losos', 300, 'GRAM'),
          ('Losos sa krompirom', 'Krompir', 500, 'GRAM'),
          ('Losos sa krompirom', 'Limun', 1, 'KOMAD'),
          ('Losos sa krompirom', 'Maslac', 30, 'GRAM'),
          ('Losos sa krompirom', 'Beli luk', 5, 'GRAM'),
          ('Losos sa krompirom', 'So', NULL, NULL),
          ('Losos sa krompirom', 'Crni biber', NULL, NULL),
          ('Kaprese salata', 'Paradajz', 2, 'KOMAD'),
          ('Kaprese salata', 'Mocarela', 125, 'GRAM'),
          ('Kaprese salata', 'Maslinovo ulje', 20, 'MILILITAR'),
          ('Kaprese salata', 'Bosiljak', NULL, NULL),
          ('Kaprese salata', 'So', NULL, NULL),
          ('Pica margarita', 'Pšenično brašno', 300, 'GRAM'),
          ('Pica margarita', 'Voda', 200, 'MILILITAR'),
          ('Pica margarita', 'Suvi kvasac', 7, 'GRAM'),
          ('Pica margarita', 'Maslinovo ulje', 20, 'MILILITAR'),
          ('Pica margarita', 'Paradajz', 3, 'KOMAD'),
          ('Pica margarita', 'Mocarela', 200, 'GRAM'),
          ('Pica margarita', 'Bosiljak', NULL, NULL),
          ('Pica margarita', 'Origano', NULL, NULL),
          ('Pica margarita', 'So', NULL, NULL),
          ('Proja', 'Kukuruzno brašno', 300, 'GRAM'),
          ('Proja', 'Jaja', 3, 'KOMAD'),
          ('Proja', 'Jogurt', 250, 'MILILITAR'),
          ('Proja', 'Suncokretovo ulje', 100, 'MILILITAR'),
          ('Proja', 'Prašak za pecivo', 10, 'GRAM'),
          ('Proja', 'So', NULL, NULL),
          ('Pita sa jabukama', 'Pšenično brašno', 400, 'GRAM'),
          ('Pita sa jabukama', 'Maslac', 200, 'GRAM'),
          ('Pita sa jabukama', 'Jaja', 2, 'KOMAD'),
          ('Pita sa jabukama', 'Šećer', 150, 'GRAM'),
          ('Pita sa jabukama', 'Jabuka', 6, 'KOMAD'),
          ('Pita sa jabukama', 'Cimet', 1, 'KASICICA'),
          ('Pita sa jabukama', 'Orah', 100, 'GRAM')
     ) AS podaci(recept, namirnica, kolicina, jedinica)
         JOIN recept ON recept.naziv = podaci.recept
         JOIN korisnik AS autor ON autor.id = recept.autor_id AND autor.email LIKE '%@recepti.test'
         JOIN namirnica ON namirnica.naziv = podaci.namirnica
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19');

-- Koraci pripreme, trajanje je prazno za pripremu bez termicke obrade
INSERT INTO korak_pripreme (redni_broj, naslov, opis, tip_pripreme, trajanje_min, obrisan, recept_id)
SELECT podaci.redni_broj, podaci.naslov, podaci.opis, podaci.tip, podaci.trajanje, FALSE, recept.id
FROM (VALUES
          ('Palačinke', 1, 'Smesa', 'Umutiti jaja sa mlekom, pa dodati brašno, šećer i prstohvat soli i mešati dok smesa ne bude glatka.', 'OSTALO', 5),
          ('Palačinke', 2, 'Odmaranje smese', 'Ostaviti smesu da odstoji nekoliko minuta.', 'OSTALO', 5),
          ('Palačinke', 3, 'Pečenje', 'Zagrejati tiganj sa malo ulja, sipati tanak sloj smese i peći sa obe strane.', 'PRZENJE', 20),
          ('Pileća supa', 1, 'Priprema povrća', 'Oljuštiti i iseckati šargarepu, crni luk i celer.', 'BEZ_TERMICKE_OBRADE', NULL),
          ('Pileća supa', 2, 'Kuvanje', 'Meso i povrće staviti u hladnu vodu i kuvati na laganoj vatri.', 'KUVANJE', 50),
          ('Pileća supa', 3, 'Začinjavanje', 'Dodati Vegetu, so i seckani peršun i kuvati još nekoliko minuta.', 'KUVANJE', 5),
          ('Špagete bolonjeze', 1, 'Meso', 'Na maslinovom ulju propržiti crni i beli luk, dodati meso i pržiti dok ne promeni boju.', 'PRZENJE', 10),
          ('Špagete bolonjeze', 2, 'Sos', 'Dodati iseckan paradajz, začiniti solju i biberom i dinstati.', 'DINSTANJE', 20),
          ('Špagete bolonjeze', 3, 'Testenina', 'Skuvati testeninu u slanoj vodi prema uputstvu sa pakovanja.', 'KUVANJE', 10),
          ('Špagete bolonjeze', 4, 'Serviranje', 'Testeninu preliti sosom i posuti rendanim parmezanom.', 'BEZ_TERMICKE_OBRADE', NULL),
          ('Losos sa krompirom', 1, 'Krompir', 'Krompir iseći na kriške, posoliti i peći u rerni na 200 °C.', 'PECENJE', 25),
          ('Losos sa krompirom', 2, 'Losos', 'Losos posoliti i pobiberiti, pa ga peći na maslacu sa belim lukom sa obe strane.', 'PRZENJE', 10),
          ('Losos sa krompirom', 3, 'Serviranje', 'Poslužiti uz krompir i kriške limuna.', 'BEZ_TERMICKE_OBRADE', NULL),
          ('Kaprese salata', 1, 'Seckanje', 'Paradajz i mocarelu iseći na kolutove i ređati ih naizmenično.', 'BEZ_TERMICKE_OBRADE', NULL),
          ('Kaprese salata', 2, 'Začinjavanje', 'Dodati listove bosiljka, posoliti i preliti maslinovim uljem.', 'BEZ_TERMICKE_OBRADE', NULL),
          ('Pica margarita', 1, 'Testo', 'Pomešati brašno, kvasac, so, vodu i ulje i mesiti dok testo ne postane glatko.', 'OSTALO', 10),
          ('Pica margarita', 2, 'Dizanje testa', 'Pokriti testo i ostaviti ga na toplom da naraste.', 'OSTALO', 60),
          ('Pica margarita', 3, 'Pečenje', 'Razvući testo, premazati seckanim paradajzom, dodati mocarelu i origano i peći na 250 °C.', 'PECENJE', 15),
          ('Pica margarita', 4, 'Serviranje', 'Pre serviranja dodati sveže listove bosiljka.', 'BEZ_TERMICKE_OBRADE', NULL),
          ('Proja', 1, 'Smesa', 'Umutiti jaja, dodati jogurt i ulje, pa kukuruzno brašno, prašak za pecivo i so.', 'OSTALO', 10),
          ('Proja', 2, 'Pečenje', 'Smesu sipati u podmazan pleh i peći na 200 °C dok ne porumeni.', 'PECENJE', 40),
          ('Pita sa jabukama', 1, 'Testo', 'Od brašna, maslaca, jaja i polovine šećera umesiti glatko testo.', 'OSTALO', 15),
          ('Pita sa jabukama', 2, 'Fil', 'Jabuke izrendati i pomešati sa ostatkom šećera, cimetom i mlevenim orasima.', 'OSTALO', 10),
          ('Pita sa jabukama', 3, 'Pečenje', 'Polovinu testa razvući u pleh, preko staviti fil, pa drugu polovinu testa i peći na 180 °C.', 'PECENJE', 45)
     ) AS podaci(recept, redni_broj, naslov, opis, tip, trajanje)
         JOIN recept ON recept.naziv = podaci.recept
         JOIN korisnik AS autor ON autor.id = recept.autor_id AND autor.email LIKE '%@recepti.test'
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19');

-- Ocene: niko ne ocenjuje svoj recept. Pileca supa ima samo stare ocene, pa nije medju popularnima.
INSERT INTO ocena (vrednost, datum, korisnik_id, recept_id)
SELECT podaci.vrednost, NOW() - podaci.dana * INTERVAL '1 day', korisnik.id, recept.id
FROM (VALUES
    ('Pica margarita', 'marko', 5, 4),
    ('Pica margarita', 'ana', 5, 2),
    ('Palačinke', 'marko', 5, 10),
    ('Palačinke', 'jelena', 4, 6),
    ('Špagete bolonjeze', 'ana', 4, 14),
    ('Špagete bolonjeze', 'jelena', 4, 9),
    ('Pita sa jabukama', 'marko', 4, 0.5),
    ('Losos sa krompirom', 'ana', 3, 15),
    ('Pileća supa', 'ana', 5, 55),
    ('Pileća supa', 'jelena', 5, 50)
    ) AS podaci(recept, korisnik, vrednost, dana)
    JOIN recept ON recept.naziv = podaci.recept
    JOIN korisnik AS autor ON autor.id = recept.autor_id AND autor.email LIKE '%@recepti.test'
    JOIN korisnik ON korisnik.korisnicko_ime = podaci.korisnik
WHERE NOT EXISTS (SELECT 1 FROM pocetni_unosi WHERE oznaka = 'T-19')
ON CONFLICT DO NOTHING;

-- Oznaka se upisuje poslednja, pa se ceo blok izvrsava samo pri prvom pokretanju
INSERT INTO pocetni_unosi (oznaka)
VALUES ('T-19')
    ON CONFLICT (oznaka) DO NOTHING;
COMMIT;