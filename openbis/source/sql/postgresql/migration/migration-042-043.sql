-- Migration from 042 to 043
CREATE DOMAIN GRID_EXPRESSION AS VARCHAR(2000);
CREATE DOMAIN GRID_ID AS VARCHAR(200);

CREATE TABLE GRID_CUSTOM_COLUMNS (ID TECH_ID NOT NULL, DBIN_ID TECH_ID NOT NULL, CODE VARCHAR(200) NOT NULL, LABEL column_label NOT NULL, DESCRIPTION DESCRIPTION_1000,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, PERS_ID_REGISTERER TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, EXPRESSION GRID_EXPRESSION NOT NULL, IS_PUBLIC BOOLEAN NOT NULL, GRID_ID GRID_ID NOT NULL);
CREATE SEQUENCE GRID_CUSTOM_COLUMNS_ID_SEQ;
ALTER TABLE GRID_CUSTOM_COLUMNS ADD CONSTRAINT GRID_CUSTOM_COLUMNS_PK PRIMARY KEY(ID);
ALTER TABLE GRID_CUSTOM_COLUMNS ADD CONSTRAINT GRID_CUSTOM_COLUMNS_BK_UK UNIQUE(CODE, DBIN_ID, GRID_ID);

ALTER TABLE GRID_CUSTOM_COLUMNS ADD CONSTRAINT GRID_CUSTOM_COLUMNS_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE GRID_CUSTOM_COLUMNS ADD CONSTRAINT GRID_CUSTOM_COLUMNS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);

CREATE INDEX GRID_CUSTOM_COLUMNS_PERS_FK_I ON GRID_CUSTOM_COLUMNS (PERS_ID_REGISTERER);
CREATE INDEX GRID_CUSTOM_COLUMNS_DBIN_FK_I ON GRID_CUSTOM_COLUMNS (DBIN_ID);