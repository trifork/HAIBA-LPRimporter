CREATE DATABASE IF NOT EXISTS LPR;
USE LPR;

CREATE TABLE IF NOT EXISTS LPR_Administration (
    Recordnummer BIGINT(15) NOT NULL PRIMARY KEY,
    CPR VARCHAR(10),
    Sygehuskode VARCHAR(10),
    Afdelingskode VARCHAR(10),
    Indlaeggelsesdato VARCHAR(10),
    Indlaeggelsestidspunkt VARCHAR(10),
    Udskrivningsdato VARCHAR(10),
    Udskrivningstidspunkt VARCHAR(10)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS LPR_Diagnoser (
    Recordnummer BIGINT(15) NOT NULL PRIMARY KEY,
    Diagnoseskode VARCHAR(10),
    Diagnosetype VARCHAR(10),
    Tillaegsdiagnose VARCHAR(10)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS LPR_Procedurer (
    Recordnummer BIGINT(15) NOT NULL PRIMARY KEY,
    Procedurekode VARCHAR(10),
    Proceduretype VARCHAR(10),
    Tillaegsprocedurekode VARCHAR(10),
    Sygehuskode VARCHAR(10),
    Afdelingskode VARCHAR(10),
    Proceduredato VARCHAR(10),
    Proceduretidspunkt VARCHAR(10)
) ENGINE=InnoDB COLLATE=utf8_bin;
