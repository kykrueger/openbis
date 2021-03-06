-- Migration from 121 to 122

CREATE TABLE METAPROJECTS (
	ID             TECH_ID NOT NULL,
	NAME           CODE NOT NULL,
	DESCRIPTION    DESCRIPTION_2000,
	OWNER          TECH_ID NOT NULL,
	PRIVATE        BOOLEAN_CHAR NOT NULL DEFAULT TRUE,
	CREATION_DATE  TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE METAPROJECT_ID_SEQ;

ALTER TABLE METAPROJECTS ADD CONSTRAINT METAPROJECTS_PK PRIMARY KEY(ID);

ALTER TABLE METAPROJECTS ADD CONSTRAINT METAPROJECTS_OWNER_FK FOREIGN KEY (OWNER) REFERENCES PERSONS(ID) ON DELETE CASCADE;

ALTER TABLE METAPROJECTS ADD CONSTRAINT METAPROJECTS_NAME_OWNER_UK UNIQUE (NAME, OWNER);

CREATE INDEX METAPROJECTS_OWNER_FK_I ON METAPROJECTS (OWNER);
CREATE INDEX METAPROJECTS_NAME_I ON METAPROJECTS (NAME);
CREATE INDEX METAPROJECTS_NAME_OWNER_I ON METAPROJECTS (NAME, OWNER);

CREATE TABLE METAPROJECT_ASSIGNMENTS (
	ID              TECH_ID NOT NULL,
	MEPR_ID         TECH_ID NOT NULL,
	EXPE_ID         TECH_ID,
	SAMP_ID         TECH_ID,
	DATA_ID         TECH_ID,
	MATE_ID         TECH_ID,
	CREATION_DATE  TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE METAPROJECT_ASSIGNMENT_ID_SEQ;

ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_PK PRIMARY KEY(ID);

ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_MEPR_ID_FK FOREIGN KEY (MEPR_ID) REFERENCES METAPROJECTS(ID) ON DELETE CASCADE;
ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_EXPE_ID_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS_ALL(ID) ON DELETE CASCADE;
ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_SAMP_ID_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES_ALL(ID) ON DELETE CASCADE;
ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_DATA_ID_FK FOREIGN KEY (DATA_ID) REFERENCES DATA_ALL(ID) ON DELETE CASCADE;
ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_MATE_ID_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID) ON DELETE CASCADE;

ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_CHECK_NN CHECK (
	(EXPE_ID IS NOT NULL AND SAMP_ID IS NULL AND DATA_ID IS NULL AND MATE_ID IS NULL) OR
	(EXPE_ID IS NULL AND SAMP_ID IS NOT NULL AND DATA_ID IS NULL AND MATE_ID IS NULL) OR
	(EXPE_ID IS NULL AND SAMP_ID IS NULL AND DATA_ID IS NOT NULL AND MATE_ID IS NULL) OR
	(EXPE_ID IS NULL AND SAMP_ID IS NULL AND DATA_ID IS NULL AND MATE_ID IS NOT NULL));

ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_MEPR_ID_EXPE_ID_UK UNIQUE (MEPR_ID, EXPE_ID);
ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_MEPR_ID_SAMP_ID_UK UNIQUE (MEPR_ID, SAMP_ID);
ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_MEPR_ID_DATA_ID_UK UNIQUE (MEPR_ID, DATA_ID);
ALTER TABLE METAPROJECT_ASSIGNMENTS ADD CONSTRAINT METAPROJECT_ASSIGNMENTS_MEPR_ID_MATE_ID_UK UNIQUE (MEPR_ID, MATE_ID);

CREATE INDEX METAPROJECT_ASSIGNMENTS_MEPR_FK_I ON METAPROJECT_ASSIGNMENTS (MEPR_ID);

GRANT SELECT ON TABLE METAPROJECTS TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE METAPROJECT_ID_SEQ TO GROUP OPENBIS_READONLY; 
GRANT SELECT ON TABLE METAPROJECT_ASSIGNMENTS TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE METAPROJECT_ASSIGNMENT_ID_SEQ TO GROUP OPENBIS_READONLY; 
