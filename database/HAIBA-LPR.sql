CREATE DATABASE IF NOT EXISTS LPR;
USE LPR;

CREATE TABLE IF NOT EXISTS T_ADM (
	   CONTACT_IDENTIFICATION_ID BIGINT(15) AUTO_INCREMENT NOT NULL PRIMARY KEY,
       V_RECNUM BIGINT(15) NOT NULL,
       C_SGH varchar(4) NULL,
       C_AFD varchar(3) NULL,
       C_PATTYPE varchar(1) NULL,
       V_CPR varchar(10) NULL,
       D_INDDTO datetime NULL,
       D_UDDTO datetime NULL,
       D_IMPORTDTO datetime NULL,
       V_SYNC_ID BIGINT(15) NULL,
       V_STATUS varchar(10) NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS T_KODER(
       V_RECNUM bigint(15) NULL,
       C_KODE varchar(10) NULL,
       C_TILKODE varchar(10) NULL,
       C_KODEART varchar(1) NULL,
       D_PDTO datetime NULL,
       C_PSGH varchar(4) NULL,
       C_PAFD varchar(3) NULL,
       V_TYPE varchar(3) NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS T_LOG_SYNC (
       V_SYNC_ID bigint(15) NOT NULL,
       START_TIME datetime NOT NULL,
       END_TIME datetime NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS T_LOG_SYNC_HISTORY (
       V_SYNC_ID bigint(15) NOT NULL,
       V_RECNUM bigint(15) NOT NULL,
       AFFECTED_V_RECNUM bigint(15) NULL,
       C_ACTION_TYPE varchar(128) NULL
) ENGINE=InnoDB COLLATE=utf8_bin;
