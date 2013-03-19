CREATE DATABASE IF NOT EXISTS LPR;
USE LPR;

CREATE TABLE IF NOT EXISTS T_ADM (
	   CONTACT_IDENTIFICATION_ID BIGINT(15) NOT NULL,
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

CREATE TABLE IF NOT EXISTS T_DIAG (
       V_RECNUM bigint(15) NULL,
       C_DIAG varchar(10) NULL,
       C_TILDIAG varchar(10) NULL,
       C_DIAGTYPE varchar(1) NOT NULL
       
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS T_PROCEDURER(
       V_RECNUM bigint(15) NULL,
       C_OPR varchar(10) NULL,
       C_TILOPR varchar(10) NULL,
       C_OPRART varchar(1) NULL,
       D_ODTO datetime NULL,
       C_OSGH varchar(4) NULL,
       C_OAFD varchar(3) NULL
       
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS T_LOG_SYNC (
       V_SYNC_ID bigint(15) NOT NULL,
       START_TIME datetime NOT NULL,
       END_TIME datetime NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS T_LOG_SYNC_HISTORY (
       V_SYNC_ID bigint(15) NOT NULL,
       V_RECNUM bigint(15) NOT NULL,
       AFFEDTED_V_RECNUM bigint(15) NULL,
       C_ACTION_TYPE varchar(128) NULL
) ENGINE=InnoDB COLLATE=utf8_bin;
