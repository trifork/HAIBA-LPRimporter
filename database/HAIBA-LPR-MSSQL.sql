CREATE TABLE T_ADM (
       K_RECNUM BIGINT NOT NULL,
       C_SGH varchar(4) NULL,
       C_AFD varchar(3) NULL,
       C_PATTYPE varchar(1) NULL,
       V_CPR varchar(10) NULL,
       D_INDDTO datetime NULL,
       D_UDDTO datetime NULL,
       V_INDTIME int NULL,
       V_UDTIME int NULL,
       D_IMPORTDTO datetime NULL
);

CREATE TABLE T_DIAG (
       V_RECNUM bigint,
       C_DIAG varchar(10) NULL,
       C_TILDIAG varchar(10) NULL,
       C_DIAGTYPE varchar(1) NOT NULL
);

CREATE TABLE T_PROCEDURER(
       V_RECNUM bigint,
       C_OPR varchar(10) NULL,
       C_TILOPR varchar(10) NULL,
       C_OPRART varchar(1) NULL,
       D_ODTO datetime NULL,
       V_OTIME int NULL,
       C_OSGH varchar(4) NULL,
       C_OAFD varchar(3) NULL
); 
