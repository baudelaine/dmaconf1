SELECT COUNT(*) as COUNT, (cast(round((20/20.0)*100, 3) as numeric(31,3))) as PERC FROM PROJECT, DEPARTMENT WHERE PROJECT.DEPTNO = DEPARTMENT.DEPTNO


CREATE TABLE RELATIONS
(
   FK_NAME varchar(128) NOT NULL,
   PK_NAME varchar(128) NOT NULL,
   FKTABLE_NAME varchar(128) NOT NULL,
   PKTABLE_NAME varchar(128) NOT NULL,
   KEY_SEQ SMALLINT NOT NULL,
   FKCOLUMN_NAME varchar(128) NOT NULL,
   PKCOLUMN_NAME varchar(128) NOT NULL,
   CONSTRAINT PK_RELATIONS PRIMARY KEY (FK_NAME, PK_NAME, KEY_SEQ)
);

SELECT FK_NAME, PK_NAME, FKTABLE_NAME, PKTABLE_NAME, KEY_SEQ, FKCOLUMN_NAME, PKCOLUMN_NAME FROM relations WHERE FKTABLE_NAME = 'PROJECT'

SELECT FK_NAME, PK_NAME, FKTABLE_NAME, PKTABLE_NAME, KEY_SEQ, FKCOLUMN_NAME, PKCOLUMN_NAME FROM relations WHERE PKTABLE_NAME = 'PROJECT'

