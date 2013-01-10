CREATE DATABASE IF NOT EXISTS LPR;
USE LPR;

CREATE TABLE IF NOT EXISTS T_ADM (
       K_RECNUM BIGINT(15) NOT NULL PRIMARY KEY,
       C_SGH varchar(4) NULL,
       C_AFD varchar(3) NULL,
       C_PATTYPE varchar(1) NULL,
       V_CPR varchar(10) NULL,
       D_INDDTO datetime NULL,
       D_UDDTO datetime NULL,
       V_INDTIME int NULL,
       V_UDTIME int NULL,
       D_IMPORTDTO datetime NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS T_DIAG (
       V_RECNUM bigint(15) NULL,
       C_DIAG varchar(10) NULL,
       C_TILDIAG varchar(10) NULL,
       C_DIAGTYPE varchar(1) NOT NULL,

       FOREIGN KEY (V_RECNUM) REFERENCES T_ADM(K_RECNUM)
       
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS T_PROCEDURER(
       V_RECNUM bigint(15) NULL,
       C_OPR varchar(10) NULL,
       C_TILOPR varchar(10) NULL,
       C_OPRART varchar(1) NULL,
       D_ODTO datetime NULL,
       V_OTIME int NULL,
       C_OSGH varchar(4) NULL,
       C_OAFD varchar(3) NULL,
       
       FOREIGN KEY (V_RECNUM) REFERENCES T_ADM(K_RECNUM)
       
) ENGINE=InnoDB COLLATE=utf8_bin;
