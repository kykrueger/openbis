
/* ---------------------------------------------------------------------- */
/* Domains                                                                */
/* ---------------------------------------------------------------------- */

CREATE DOMAIN TECH_ID AS BIGINT;

CREATE DOMAIN CODE AS VARCHAR(40);

CREATE DOMAIN FILE_PATH AS VARCHAR(1000);

CREATE DOMAIN LOCATION AS VARCHAR(40) CHECK (VALUE IN ('STAGE', 'FINAL'));

/* ---------------------------------------------------------------------- */
/* Tables                                                                 */
/* ---------------------------------------------------------------------- */

CREATE TABLE CONTAINERS (
  ID bigserial NOT NULL,
  CODE CODE NOT NULL,
  PATH FILE_PATH,
  LOCATION LOCATION NOT NULL,
  
  PRIMARY KEY (ID)
);

CREATE TABLE DATA_SETS (
  ID bigserial NOT NULL,
  CODE CODE NOT NULL,
  CTNR_ID TECH_ID NOT NULL,
  SIZE_IN_BYTES BIGINT NOT NULL, 

  PRIMARY KEY (ID),
  UNIQUE (CODE),
  CONSTRAINT FK_DATA_SET_CONTAINERS FOREIGN KEY (CTNR_ID) REFERENCES CONTAINERS (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX DATA_SETS_CODE_IDX ON DATA_SETS (CODE);
