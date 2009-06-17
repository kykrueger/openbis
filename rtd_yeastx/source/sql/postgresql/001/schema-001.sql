-----------------------------------
-- Version 001
-----------------------------------

-- Create domain types section -------------------------------------------------

CREATE DOMAIN CODE AS VARCHAR(40);

CREATE DOMAIN FILE_NAME as VARCHAR(255);

CREATE DOMAIN FILE_PATH as VARCHAR(1000);

CREATE DOMAIN SHORT_LABEL as VARCHAR(20); 

CREATE DOMAIN LONG_LABEL as VARCHAR(100); 

CREATE DOMAIN CHAR as VARCHAR(1);

-- Create tables section -------------------------------------------------

CREATE TABLE EIC_MS_RUNS (
  EIC_MS_RUN_ID BIGSERIAL NOT NULL,
  DATA_SET_PERM_ID CODE NOT NULL,
  RAW_DATA_FILE_NAME FILE_NAME NOT NULL,
  RAW_DATA_FILE_PATH FILE_PATH DEFAULT NULL,
  ACQUISITION_DATE TIMESTAMP DEFAULT NULL,
  INSTRUMENT_TYPE SHORT_LABEL DEFAULT NULL,
  INSTRUMENT_MANUFACTURER LONG_LABEL DEFAULT NULL,
  INSTRUMENT_MODEL LONG_LABEL DEFAULT NULL,
  METHOD_IONISATION SHORT_LABEL DEFAULT NULL,
  METHOD_SEPARATION LONG_LABEL DEFAULT NULL,
  OPERATOR SHORT_LABEL DEFAULT NULL,
  SET_ID BIGINT DEFAULT NULL,
  START_TIME REAL NOT NULL,
  END_TIME REAL NOT NULL,
  PRIMARY KEY (EIC_MS_RUN_ID),
  UNIQUE(DATA_SET_PERM_ID)
);

CREATE TABLE EIC_CHROMATOGRAMS (
  CHROM_ID BIGSERIAL NOT NULL,
  EIC_MS_RUN_ID BIGINT NOT NULL,
  Q1_MZ REAL NOT NULL,
  Q3_LOW_MZ REAL NOT NULL,
  Q3_HIGH_MZ REAL NOT NULL,
  LABEL LONG_LABEL DEFAULT NULL,
  POLARITY CHAR DEFAULT NULL,
  RUN_TIMES TEXT NOT NULL,
  INTENSITIES TEXT NOT NULL,
  PRIMARY KEY (CHROM_ID),
  CONSTRAINT FK_CHROMATOGRAM_1 FOREIGN KEY (EIC_MS_RUN_ID) REFERENCES EIC_MS_RUNS (EIC_MS_RUN_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX EIC_CHROMATOGRAM_FK_IDX ON EIC_CHROMATOGRAMS(EIC_MS_RUN_ID);

CREATE TABLE FIA_MS_RUNS (
  FIA_MS_RUN_ID BIGSERIAL NOT NULL,
  DATA_SET_PERM_ID CODE,
  RAW_DATA_FILE_NAME FILE_NAME NOT NULL,
  RAW_DATA_FILE_PATH FILE_PATH DEFAULT NULL,
  ACQUISITION_DATE TIMESTAMP DEFAULT NULL,
  INSTRUMENT_TYPE SHORT_LABEL DEFAULT NULL,
  INSTRUMENT_MANUFACTURER LONG_LABEL DEFAULT NULL,
  INSTRUMENT_MODEL LONG_LABEL DEFAULT NULL,
  METHOD_IONISATION SHORT_LABEL DEFAULT NULL,
  METHOD_SEPARATION LONG_LABEL DEFAULT NULL,
  POLARITY VARCHAR(1) DEFAULT NULL,
  LOW_MZ REAL NOT NULL,
  HIGH_MZ REAL NOT NULL,
  INTERNAL_STANDARD REAL NOT NULL,
  -- Is this a good name?
  OD REAL NOT NULL,
  OPERATOR SHORT_LABEL DEFAULT NULL,
  PRIMARY KEY (FIA_MS_RUN_ID),
  UNIQUE(DATA_SET_PERM_ID)
);

CREATE TABLE FIA_PROFILES (
  PROFILE_ID BIGSERIAL NOT NULL,
  FIA_MS_RUN_ID BIGINT NOT NULL,
  LOW_MZ REAL NOT NULL,
  HIGH_MZ REAL NOT NULL,
  MZ TEXT NOT NULL,
  INTENSITIES TEXT NOT NULL,
  PRIMARY KEY (PROFILE_ID),
  CONSTRAINT FK_EIC_PROFILE_1 FOREIGN KEY (FIA_MS_RUN_ID) REFERENCES FIA_MS_RUNS (FIA_MS_RUN_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX FIA_PROFILE_I_ID on FIA_PROFILES(FIA_MS_RUN_ID);
CREATE INDEX FIA_PROFILE_I_ID_MZ on FIA_PROFILES(FIA_MS_RUN_ID, LOW_MZ, HIGH_MZ);

CREATE TABLE FIA_CENTROIDS (
  CENTROID_ID BIGSERIAL NOT NULL,
  FIA_MS_RUN_ID BIGINT NOT NULL,
  MZ REAL NOT NULL,
  INTENSITY REAL NOT NULL,
  CORRELATION REAL NOT NULL,
  PRIMARY KEY (CENTROID_ID),
  CONSTRAINT FK_EIC_CENTROID_1 FOREIGN KEY (FIA_MS_RUN_ID) REFERENCES FIA_MS_RUNS (FIA_MS_RUN_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX FIA_CENTROID_I_ID on FIA_CENTROIDS(FIA_MS_RUN_ID);
CREATE INDEX FIA_CENTROID_I_ID_MZ on FIA_CENTROIDS(FIA_MS_RUN_ID, MZ);

GRANT SELECT ON TABLE EIC_MS_RUNS TO GROUP metabol_readonly;
GRANT SELECT ON TABLE EIC_CHROMATOGRAMS TO GROUP metabol_readonly;
GRANT SELECT ON TABLE FIA_MS_RUNS TO GROUP metabol_readonly;
GRANT SELECT ON TABLE FIA_PROFILES TO GROUP metabol_readonly;
GRANT SELECT ON TABLE FIA_CENTROIDS TO GROUP metabol_readonly;
GRANT SELECT ON SEQUENCE EIC_MS_RUNS_EIC_MS_RUN_ID_SEQ TO GROUP metabol_readonly;
GRANT SELECT ON SEQUENCE EIC_CHROMATOGRAMS_CHROM_ID_SEQ TO GROUP metabol_readonly;
GRANT SELECT ON SEQUENCE FIA_MS_RUNS_FIA_MS_RUN_ID_SEQ TO GROUP metabol_readonly;
GRANT SELECT ON SEQUENCE FIA_PROFILES_PROFILE_ID_SEQ TO GROUP metabol_readonly;
GRANT SELECT ON SEQUENCE FIA_CENTROIDS_CENTROID_ID_SEQ TO GROUP metabol_readonly;

GRANT ALL PRIVILEGES ON TABLE EIC_MS_RUNS TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE EIC_CHROMATOGRAMS TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE FIA_MS_RUNS TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE FIA_PROFILES TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON TABLE FIA_CENTROIDS TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE EIC_MS_RUNS_EIC_MS_RUN_ID_SEQ TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE EIC_CHROMATOGRAMS_CHROM_ID_SEQ TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE FIA_MS_RUNS_FIA_MS_RUN_ID_SEQ TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE FIA_PROFILES_PROFILE_ID_SEQ TO GROUP metabol_readwrite;
GRANT ALL PRIVILEGES ON SEQUENCE FIA_CENTROIDS_CENTROID_ID_SEQ TO GROUP metabol_readwrite;
