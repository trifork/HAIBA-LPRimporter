CREATE TABLE Indlaeggelser (
    IndlaeggelsesID BIGINT NOT NULL IDENTITY PRIMARY KEY,
    CPR VARCHAR(10),
    Sygehuskode VARCHAR(7),
    Afdelingskode VARCHAR(3),
    Indlaeggelsesdatotid datetime,
    Udskrivningsdatotid datetime,
    Aktuel Bit default 0,
    
    INDEX(CPR)
);

CREATE TABLE Diagnoser (
    IndlaeggelsesID BIGINT FOREIGN KEY REFERENCES Indlaeggelser(IndlaeggelsesID),
    Diagnoseskode VARCHAR(10),
    Diagnosetype VARCHAR(1),
    Tillaegsdiagnose VARCHAR(10)
    
);

CREATE TABLE Procedurer (
    IndlaeggelsesID BIGINT FOREIGN KEY REFERENCES Indlaeggelser(IndlaeggelsesID),
    Procedurekode VARCHAR(10),
    Proceduretype VARCHAR(1),
    Tillaegsprocedurekode VARCHAR(10),
    Sygehuskode VARCHAR(7),
    Afdelingskode VARCHAR(3),
    Proceduredatotid datetime
    
);

CREATE TABLE Indlaeggelsesforloeb (
    ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
    IndlaeggelsesforloebID BIGINT,
    IndlaeggelsesID BIGINT FOREIGN KEY REFERENCES Indlaeggelser(IndlaeggelsesID),

    UNIQUE (IndlaeggelsesforloebID, IndlaeggelsesID),

);

CREATE TABLE LPR_Reference (
    ID BIGINT NOT NULL IDENTITY  PRIMARY KEY,
    IndlaeggelsesID BIGINT NOT NULL FOREIGN KEY REFERENCES Indlaeggelser(IndlaeggelsesID),
    LPR_recordnummer BIGINT NOT NULL
);

CREATE TABLE RegelFejlbeskeder (
    ID BIGINT NOT NULL IDENTITY  PRIMARY KEY,
    LPR_dbid BIGINT NOT NULL,
    LPR_recordnummer BIGINT NOT NULL,
    AfbrudtForretningsregel VARCHAR(50),
    Fejlbeskrivelse VARCHAR(500),
    Fejltidspunkt datetime
);

CREATE TABLE ImporterStatus (
    Id BIGINT NOT NULL IDENTITY PRIMARY KEY,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME,
    Outcome VARCHAR(20),
    ErrorMessage VARCHAR(200),

    INDEX (StartTime)
);

CREATE TABLE AmbulantKontakt (
    AmbulantKontaktID BIGINT NOT NULL IDENTITY PRIMARY KEY,
    CPR VARCHAR(10),
    Sygehuskode VARCHAR(7),
    Afdelingskode VARCHAR(3),
    Indlaeggelsesdatotid datetime,
    Udskrivningsdatotid datetime,
    Aktuel Bit default 0
);

CREATE TABLE AmbulantDiagnoser (
    AmbulantKontaktID BIGINT NOT NULL FOREIGN KEY (AmbulantKontaktID) REFERENCES AmbulantKontakt(AmbulantKontaktID),
    Diagnoseskode VARCHAR(10),
    Diagnosetype VARCHAR(1),
    Tillaegsdiagnose VARCHAR(10)
)

CREATE TABLE AmbulantProcedurer (
    AmbulantKontaktID BIGINT NOT NULL FOREIGN KEY (AmbulantKontaktID) REFERENCES AmbulantKontakt(AmbulantKontaktID),
    Procedurekode VARCHAR(10),
    Proceduretype VARCHAR(1),
    Tillaegsprocedurekode VARCHAR(10),
    Sygehuskode VARCHAR(7),
    Afdelingskode VARCHAR(3),
    Proceduredatotid datetime
);

CREATE TABLE AmbulantLPR_Reference (
    ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
    AmbulantKontaktID BIGINT NOT NULL FOREIGN KEY (AmbulantKontaktID) REFERENCES AmbulantKontakt(AmbulantKontaktID),
    LPR_recordnummer BIGINT NOT NULL
);

CREATE TABLE Statistik (
    ID BIGINT NOT NULL IDENTITY PRIMARY KEY,
    KoerselsDato DATETIME NOT NULL,
    AntalKontakter BIGINT NULL,
    AntalCPRNumre BIGINT NULL,
    AntalKontakterFejlet BIGINT NULL,
    AntalCPRNumreEksporteret BIGINT NULL,
    AntalIndlaeggelserEksporteret BIGINT NULL,
    AntalForloebEksporteret BIGINT NULL,
    AntalAmbulanteKontakterEksporteret BIGINT NULL,
    AntalCPRNumreMedSlettedeKontakterBehandlet BIGINT NULL,
    AntalNuvaerendePatienterBehandlet BIGINT NULL,
    Regel1 BIGINT NULL,
    Regel2 BIGINT NULL,
    Regel3 BIGINT NULL,
    Regel4 BIGINT NULL,
    Regel5 BIGINT NULL,
    Regel6 BIGINT NULL,
    Regel7 BIGINT NULL,
    Regel8 BIGINT NULL,
    Regel9 BIGINT NULL,
    Regel10 BIGINT NULL,
    Regel11 BIGINT NULL,
    Regel12 BIGINT NULL,
    Regel13 BIGINT NULL,
    Regel14 BIGINT NULL
);


go;

CREATE VIEW IndlaeggelsesForloebsOversigt
AS SELECT hif.IndlaeggelsesforloebID, min(hi.indlaeggelsesdatotid) as minIndlaeggelsesdatotid, max(hi.udskrivningsdatotid) as maxUdskrivningsdatotid, hi.CPR 
FROM Indlaeggelsesforloeb hif
INNER JOIN Indlaeggelser hi on hif.indlaeggelsesID = hi.indlaeggelsesID
GROUP BY hif.IndlaeggelsesforloebID, hi.cpr;

