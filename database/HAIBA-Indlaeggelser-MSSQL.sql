CREATE TABLE Indlaeggelser (
    IndlaeggelsesID BIGINT NOT NULL IDENTITY PRIMARY KEY,
    CPR VARCHAR(10),
    Sygehuskode VARCHAR(4),
    Afdelingskode VARCHAR(3),
    Indlaeggelsesdatotid datetime,
    Udskrivningsdatotid datetime
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
    Sygehuskode VARCHAR(4),
    Afdelingskode VARCHAR(3),
    Proceduredatotid datetime
    
);

CREATE TABLE Indlaeggelsesforloeb (
    IndlaeggelsesforloebID BIGINT NOT NULL IDENTITY,
    IndlaeggelsesID BIGINT FOREIGN KEY REFERENCES Indlaeggelser(IndlaeggelsesID),

    PRIMARY KEY (IndlaeggelsesforloebID, IndlaeggelsesID),

);

CREATE TABLE LPR_Reference (
    IndlaeggelsesID BIGINT NOT NULL FOREIGN KEY REFERENCES Indlaeggelser(IndlaeggelsesID),
    LPR_recordnummer BIGINT NOT NULL,
    PRIMARY KEY (IndlaeggelsesID,LPR_recordnummer)
);

CREATE TABLE RegelFejlbeskeder (
    ID BIGINT NOT NULL IDENTITY  PRIMARY KEY,
    LPR_recordnummer BIGINT NOT NULL,
    AfbrudtForretningsregel VARCHAR(50),
    Fejlbeskrivelse VARCHAR(500),
    Fejltidspunkt datetime
);

go;

CREATE VIEW IndlaeggelsesForloebsOversigt
AS SELECT hif.IndlaeggelsesforloebID, min(hi.indlaeggelsesdatotid) as minIndlaeggelsesdatotid, max(hi.udskrivningsdatotid) as maxUdskrivningsdatotid, hi.CPR 
FROM Indlaeggelsesforloeb hif
INNER JOIN Indlaeggelser hi on hif.indlaeggelsesID = hi.indlaeggelsesID
GROUP BY hif.IndlaeggelsesforloebID, hi.cpr;

