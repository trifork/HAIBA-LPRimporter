CREATE DATABASE IF NOT EXISTS HAIBA;
USE HAIBA;
 
CREATE TABLE IF NOT EXISTS Indlaeggelser (
    IndlaeggelsesID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    CPR VARCHAR(10),
    Sygehuskode VARCHAR(4),
    Afdelingskode VARCHAR(3),
    Indlaeggelsesdatotid datetime,
    Udskrivningsdatotid datetime
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS Diagnoser (
    IndlaeggelsesID BIGINT(15) NOT NULL,
    Diagnoseskode VARCHAR(10),
    Diagnosetype VARCHAR(1),
    Tillaegsdiagnose VARCHAR(10),
    
    FOREIGN KEY (IndlaeggelsesID) REFERENCES Indlaeggelser(IndlaeggelsesID)
    
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS Procedurer (
    IndlaeggelsesID BIGINT(15) NOT NULL,
    Procedurekode VARCHAR(10),
    Proceduretype VARCHAR(1),
    Tillaegsprocedurekode VARCHAR(10),
    Sygehuskode VARCHAR(4),
    Afdelingskode VARCHAR(3),
    Proceduredatotid datetime,
    
    FOREIGN KEY (IndlaeggelsesID) REFERENCES Indlaeggelser(IndlaeggelsesID)
    
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS Indlaeggelsesforloeb (
    IndlaeggelsesforloebID BIGINT(15) NOT NULL AUTO_INCREMENT,
    IndlaeggelsesID BIGINT(15) NOT NULL,
    PRIMARY KEY (`IndlaeggelsesforloebID`,`IndlaeggelsesID`),
    FOREIGN KEY (IndlaeggelsesID) REFERENCES Indlaeggelser(IndlaeggelsesID)

) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS LPR_Reference (
    IndlaeggelsesID BIGINT(15) NOT NULL,
    LPR_recordnummer BIGINT(15) NOT NULL,
    PRIMARY KEY (`IndlaeggelsesID`,`LPR_recordnummer`),
    FOREIGN KEY (IndlaeggelsesID) REFERENCES Indlaeggelser(IndlaeggelsesID)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE VIEW IndlaeggelsesForloebsOversigt
AS SELECT hif.IndlaeggelsesforloebID, min(hi.indlaeggelsesdatotid) as minIndlaeggelsesdatotid, max(hi.udskrivningsdatotid) as maxUdskrivningsdatotid, hi.CPR 
FROM Indlaeggelsesforloeb hif
INNER JOIN Indlaeggelser hi on hif.indlaeggelsesID = hi.indlaeggelsesID
GROUP BY hif.IndlaeggelsesforloebID, hi.cpr;

