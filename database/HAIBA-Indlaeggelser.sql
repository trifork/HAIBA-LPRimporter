CREATE DATABASE IF NOT EXISTS HAIBA;
USE HAIBA;
 
CREATE TABLE IF NOT EXISTS Klass_SHAK (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sygehuskode VARCHAR(10),
    afdelingskode VARCHAR(10),
    H_ITA_gruppe float,
    H_kir_gruppe float,
    H_med_gruppe float,
    H_MiBa_prefix float,
    H_SOR_mappet float

) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS klass_procedurer (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    procedurekode VARCHAR(10),
    tillaegskode VARCHAR(10),
    H_HOFTE_PRO float NULL,
    H_HOFTE_IND float NULL,
    H_HOFTE_SPE float NULL,
    H_HOFTE_REO float NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS klass_diagnoser (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Diagnoseskode VARCHAR(10),
    tillaegskode VARCHAR(10),
    H_BAKT_D float NULL,
    H_UVI_D float NULL,
    H_SAAR_D float NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS Indlaeggelser (
    IndlaeggelsesID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    CPR VARCHAR(10),
    Sygehuskode VARCHAR(7),
    Afdelingskode VARCHAR(3),
    Indlaeggelsesdatotid datetime,
    Udskrivningsdatotid datetime,
    Aktuel tinyint(1) default 0,
    
    INDEX (CPR)
 
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
    Sygehuskode VARCHAR(7),
    Afdelingskode VARCHAR(3),
    Proceduredatotid datetime,
    
    FOREIGN KEY (IndlaeggelsesID) REFERENCES Indlaeggelser(IndlaeggelsesID)
    
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS Indlaeggelsesforloeb (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    IndlaeggelsesforloebID BIGINT(15),
    IndlaeggelsesID BIGINT(15) NOT NULL,
    UNIQUE (`IndlaeggelsesforloebID`,`IndlaeggelsesID`),
    FOREIGN KEY (IndlaeggelsesID) REFERENCES Indlaeggelser(IndlaeggelsesID)

) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE LPR_Reference (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    IndlaeggelsesID BIGINT(15) NOT NULL,
    LPR_recordnummer varchar(255) NOT NULL,
    LPR_dbid BIGINT(15) NOT NULL,
    FOREIGN KEY (IndlaeggelsesID) REFERENCES Indlaeggelser(IndlaeggelsesID)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS RegelFejlbeskeder (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    LPR_dbid BIGINT(15) NOT NULL,
    LPR_recordnummer varchar(255) NOT NULL,
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

CREATE TABLE IF NOT EXISTS AmbulantKontakt (
    AmbulantKontaktID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    CPR VARCHAR(10),
    Sygehuskode VARCHAR(7),
    Afdelingskode VARCHAR(3),
    Indlaeggelsesdatotid datetime,
    Udskrivningsdatotid datetime,
    Aktuel varchar(1)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS AmbulantDiagnoser (
    AmbulantKontaktID BIGINT(15) NOT NULL,
    Diagnoseskode VARCHAR(10),
    Diagnosetype VARCHAR(1),
    Tillaegsdiagnose VARCHAR(10),
    
    FOREIGN KEY (AmbulantKontaktID) REFERENCES AmbulantKontakt(AmbulantKontaktID)
    
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS AmbulantProcedurer (
    AmbulantKontaktID BIGINT(15) NOT NULL,
    Procedurekode VARCHAR(10),
    Proceduretype VARCHAR(1),
    Tillaegsprocedurekode VARCHAR(10),
    Sygehuskode VARCHAR(7),
    Afdelingskode VARCHAR(3),
    Proceduredatotid datetime,
    
    FOREIGN KEY (AmbulantKontaktID) REFERENCES AmbulantKontakt(AmbulantKontaktID)
    
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE AmbulantLPR_Reference (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    AmbulantKontaktID BIGINT(15) NOT NULL,
    LPR_recordnummer varchar(255) NOT NULL,
    LPR_dbid BIGINT(15) NOT NULL,
    FOREIGN KEY (AmbulantKontaktID) REFERENCES AmbulantKontakt(AmbulantKontaktID)
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE StatistikDataProcesseret (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    KoerselsDato DATETIME NOT NULL,
    AntalKontakter BIGINT(15) NULL,
    AntalCPRNumre BIGINT(15) NULL,
    AntalKontakterFejlet BIGINT(15) NULL,
    AntalCPRNumreEksporteret BIGINT(15) NULL,
    AntalIndlaeggelserEksporteret BIGINT(15) NULL,
    AntalForloebEksporteret BIGINT(15) NULL,
    AntalAmbulanteKontakterEksporteret BIGINT(15) NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE Statistik (
    ID BIGINT(15) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    KoerselsDato DATETIME NOT NULL,
    AntalKontakter BIGINT(15) NULL,
    AntalCPRNumre BIGINT(15) NULL,
    AntalKontakterFejlet BIGINT(15) NULL,
    AntalCPRNumreEksporteret BIGINT(15) NULL,
    AntalIndlaeggelserEksporteret BIGINT(15) NULL,
    AntalForloebEksporteret BIGINT(15) NULL,
    AntalAmbulanteKontakterEksporteret BIGINT(15) NULL,
    AntalCPRNumreMedSlettedeKontakterBehandlet BIGINT(15) NULL,
    AntalNuvaerendePatienterBehandlet BIGINT(15) NULL,
    Regel1 BIGINT(15) NULL,
    Regel2 BIGINT(15) NULL,
    Regel3 BIGINT(15) NULL,
    Regel4 BIGINT(15) NULL,
    Regel5 BIGINT(15) NULL,
    Regel6 BIGINT(15) NULL,
    Regel7 BIGINT(15) NULL,
    Regel8 BIGINT(15) NULL,
    Regel9 BIGINT(15) NULL,
    Regel10 BIGINT(15) NULL,
    Regel11 BIGINT(15) NULL,
    Regel12 BIGINT(15) NULL,
    Regel13 BIGINT(15) NULL,
    Regel14 BIGINT(15) NULL
) ENGINE=InnoDB COLLATE=utf8_bin;
