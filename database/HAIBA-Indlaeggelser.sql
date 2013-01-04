CREATE DATABASE IF NOT EXISTS HAIBA;
USE HAIBA;
 
CREATE TABLE IF NOT EXISTS Indlaeggelser (
    IndlaeggelsesID BIGINT(15) NOT NULL PRIMARY KEY,
    CPR VARCHAR(10),
    Sygehuskode VARCHAR(10),
    Afdelingskode VARCHAR(10),
    Indlaeggelsesdato VARCHAR(10),
    Indlaeggelsestidspunkt VARCHAR(10),
    Udskrivningsdato VARCHAR(10),
    Udskrivningstidspunkt VARCHAR(10)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS Diagnoser (
    InlaeggelsesID BIGINT(15) NOT NULL PRIMARY KEY,
    Diagnoseskode VARCHAR(10),
    Diagnosetype VARCHAR(10),
    Tillaegsdiagnose VARCHAR(10)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS Procedurer (
    IndlaeggelsesID BIGINT(15) NOT NULL PRIMARY KEY,
    Procedurekode VARCHAR(10),
    Proceduretype VARCHAR(10),
    Tillaegsprocedurekode VARCHAR(10),
    Sygehuskode VARCHAR(10),
    Afdelingskode VARCHAR(10),
    Proceduredato VARCHAR(10),
    Proceduretidspunkt VARCHAR(10)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS Indlaeggelsesforloeb (
    IndlaeggelsesforloebID BIGINT(15) NOT NULL,
    IndlaeggelsesID BIGINT(15) NOT NULL,
    PRIMARY KEY (`IndlaeggelsesforloebID`,`IndlaeggelsesID`)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS LPR_Reference (
    IndlaeggelsesID BIGINT(15) NOT NULL,
    LPR_recordnummer BIGINT(15) NOT NULL,
    PRIMARY KEY (`IndlaeggelsesID`,`LPR_recordnummer`)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE VIEW IndlaeggelsesForloebsOversigt
AS SELECT hif.IndlaeggelsesforloebID, min(hi.indlaeggelsesdato) as minIndlaeggelsesdato, max(hi.udskrivningsdato) as maxUdskrivningsdato, hi.CPR 
FROM Indlaeggelsesforloeb hif
INNER JOIN Indlaeggelser hi on hif.indlaeggelsesID = hi.indlaeggelsesID
GROUP BY hif.IndlaeggelsesforloebID, hi.cpr;

