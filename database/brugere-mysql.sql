CREATE USER 'haiba'@'localhost' IDENTIFIED BY 'haiba';
GRANT SELECT,UPDATE, INSERT ON HAIBA.* TO 'haiba'@'localhost';

CREATE USER 'lpr'@'localhost' IDENTIFIED BY 'lpr';
GRANT SELECT,UPDATE, INSERT ON LPR.* TO 'lpr'@'localhost';

