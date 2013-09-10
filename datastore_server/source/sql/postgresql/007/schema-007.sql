
/* ---------------------------------------------------------------------- */
/* Domains                                                                */
/* ---------------------------------------------------------------------- */

CREATE DOMAIN TECH_ID AS BIGINT;

CREATE DOMAIN CODE AS VARCHAR(60);

CREATE DOMAIN FILE_PATH AS VARCHAR(1000);

CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;

CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;


/* ---------------------------------------------------------------------- */
/* Tables                                                                 */
/* ---------------------------------------------------------------------- */

CREATE TABLE DATA_SETS (
  ID BIGSERIAL NOT NULL,
  CODE CODE NOT NULL,
  LOCATION FILE_PATH NOT NULL,

  PRIMARY KEY (ID),
  UNIQUE (CODE)
);

CREATE INDEX DATA_SETS_CODE_IDX ON DATA_SETS (CODE);

CREATE TABLE DATA_SET_FILES (
  ID BIGSERIAL NOT NULL,
  DASE_ID TECH_ID NOT NULL,
  PARENT_ID TECH_ID,
  RELATIVE_PATH FILE_PATH NOT NULL,
  FILE_NAME FILE_PATH NOT NULL,
  SIZE_IN_BYTES BIGINT NOT NULL,
  CHECKSUM_CRC32 INTEGER,
  IS_DIRECTORY BOOLEAN_CHAR NOT NULL,
  LAST_MODIFIED TIME_STAMP NOT NULL DEFAULT NOW(),

  PRIMARY KEY (ID),
  CONSTRAINT FK_DATA_SET_FILES_DATA_SETS FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_DATA_SET_FILES_DATA_SET_FILES FOREIGN KEY (PARENT_ID) REFERENCES DATA_SET_FILES (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX DATA_SET_FILES_DASE_ID_IDX ON DATA_SET_FILES (DASE_ID);
CREATE INDEX DATA_SET_FILES_DASE_ID_PARENT_ID_IDX ON DATA_SET_FILES (DASE_ID, PARENT_ID);
CREATE INDEX DATA_SET_FILES_DASE_ID_RELATIVE_PATH_IDX ON DATA_SET_FILES (DASE_ID, RELATIVE_PATH);
CREATE INDEX DATA_SET_FILES_DASE_ID_FILE_NAME_IDX ON DATA_SET_FILES (DASE_ID, FILE_NAME);

CREATE TABLE EVENTS (
  LAST_SEEN_DELETION_EVENT_ID TECH_ID NOT NULL
);

CREATE TABLE LAST_FEEDING_EVENT (
  REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL
);

