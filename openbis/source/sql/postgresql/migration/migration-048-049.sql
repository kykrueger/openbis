CREATE TABLE QUERIES (ID TECH_ID NOT NULL, DBIN_ID TECH_ID NOT NULL, NAME VARCHAR(200) NOT NULL, DESCRIPTION DESCRIPTION_2000,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, PERS_ID_REGISTERER TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, EXPRESSION VARCHAR(2000) NOT NULL, IS_PUBLIC BOOLEAN NOT NULL);
CREATE SEQUENCE QUERY_ID_SEQ;
ALTER TABLE QUERIES ADD CONSTRAINT QUER_PK PRIMARY KEY(ID);
ALTER TABLE QUERIES ADD CONSTRAINT QUER_BK_UK UNIQUE(NAME, DBIN_ID);
ALTER TABLE QUERIES ADD CONSTRAINT QUER_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE QUERIES ADD CONSTRAINT QUER_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
GRANT SELECT ON SEQUENCE query_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE queries TO GROUP OPENBIS_READONLY;
