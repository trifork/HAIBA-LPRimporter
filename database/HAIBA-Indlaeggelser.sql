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

CREATE TABLE LPR_Reference (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    IndlaeggelsesID BIGINT(15) NOT NULL,
    LPR_recordnummer BIGINT(15) NOT NULL,
    FOREIGN KEY (IndlaeggelsesID) REFERENCES Indlaeggelser(IndlaeggelsesID)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS RegelFejlbeskeder (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    LPR_recordnummer BIGINT(15) NOT NULL,
    AfbrudtForretningsregel VARCHAR(50),
    Fejlbeskrivelse VARCHAR(500),
    Fejltidspunkt datetime
    
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS ImporterStatus (
    Id BIGINT(15) AUTO_INCREMENT NOT NULL PRIMARY KEY,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME,
    Outcome VARCHAR(20),
    ErrorMessage VARCHAR(200),

    INDEX (StartTime)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE VIEW IndlaeggelsesForloebsOversigt
AS SELECT hif.IndlaeggelsesforloebID, min(hi.indlaeggelsesdatotid) as minIndlaeggelsesdatotid, max(hi.udskrivningsdatotid) as maxUdskrivningsdatotid, hi.CPR 
FROM Indlaeggelsesforloeb hif
INNER JOIN Indlaeggelser hi on hif.indlaeggelsesID = hi.indlaeggelsesID
GROUP BY hif.IndlaeggelsesforloebID, hi.cpr;

