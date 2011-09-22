-- Migration from 082 to 083

-- In some installations trigger sample_subcode_uniqueness_check does not exists
-- This fixes this problem
DROP TRIGGER IF EXISTS SAMPLE_SUBCODE_UNIQUENESS_CHECK ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_SUBCODE_UNIQUENESS_CHECK BEFORE INSERT OR UPDATE ON SAMPLES_ALL
    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_SUBCODE_UNIQUENESS_CHECK();

--
-- Renaming *_PROPERTIES tables into *_PROPERTIES_ALL tables
-- Adding column VALID_UNTIL_TIMESTAMP
-- Creating views *_PROPERTIES on *_PROPERTIES_ALL showing only rows with no VALID_UNTIL_TIMESTAMP set
--

CREATE TABLE MATERIAL_PROPERTIES_HISTORY (ID TECH_ID NOT NULL, MATE_ID TECH_ID NOT NULL, MTPT_ID TECH_ID NOT NULL, VALUE TEXT_VALUE, CVTE_ID TECH_ID, MATE_PROP_ID TECH_ID, VALID_UNTIL_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE EXPERIMENT_PROPERTIES_HISTORY (ID TECH_ID NOT NULL, EXPE_ID TECH_ID NOT NULL, ETPT_ID TECH_ID NOT NULL, VALUE TEXT_VALUE, CVTE_ID TECH_ID, MATE_PROP_ID TECH_ID, VALID_UNTIL_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE SAMPLE_PROPERTIES_HISTORY (ID TECH_ID NOT NULL, SAMP_ID TECH_ID NOT NULL, STPT_ID TECH_ID NOT NULL, VALUE TEXT_VALUE, CVTE_ID TECH_ID, MATE_PROP_ID TECH_ID, VALID_UNTIL_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE DATA_SET_PROPERTIES_HISTORY (ID TECH_ID NOT NULL, DS_ID TECH_ID NOT NULL, DSTPT_ID TECH_ID NOT NULL, VALUE TEXT_VALUE, CVTE_ID TECH_ID, MATE_PROP_ID TECH_ID, VALID_UNTIL_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);

ALTER TABLE MATERIAL_PROPERTIES_HISTORY ADD CONSTRAINT MAPRH_PK PRIMARY KEY(ID);
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_PK PRIMARY KEY(ID);
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_PK PRIMARY KEY(ID);
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_PK PRIMARY KEY(ID);

ALTER TABLE MATERIAL_PROPERTIES_HISTORY ADD CONSTRAINT MAPRH_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE MATERIAL_PROPERTIES_HISTORY ADD CONSTRAINT MAPRH_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);
ALTER TABLE MATERIAL_PROPERTIES_HISTORY ADD CONSTRAINT MAPRH_MATE_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID) ON DELETE CASCADE;
ALTER TABLE MATERIAL_PROPERTIES_HISTORY ADD CONSTRAINT MAPRH_MTPT_FK FOREIGN KEY (MTPT_ID) REFERENCES MATERIAL_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_ETPT_FK FOREIGN KEY (ETPT_ID) REFERENCES EXPERIMENT_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS_ALL(ID) ON DELETE CASCADE;
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES_ALL(ID) ON DELETE CASCADE;
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_STPT_FK FOREIGN KEY (STPT_ID) REFERENCES SAMPLE_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_DSTPT_FK FOREIGN KEY (DSTPT_ID) REFERENCES DATA_SET_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_DS_FK FOREIGN KEY (DS_ID) REFERENCES DATA_ALL(ID) ON DELETE CASCADE;
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);

ALTER TABLE MATERIAL_PROPERTIES_HISTORY ADD CONSTRAINT MAPRH_CK CHECK 
	((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR 
	 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR
	 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)
	);
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_CK CHECK 
	((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR 
	 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR
	 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)
	);
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_CK CHECK 
	((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR 
	 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR
	 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)
	);
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_CK CHECK 
	((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR 
	 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR
	 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)
	);

CREATE INDEX MAPRH_CVTE_FK_I ON MATERIAL_PROPERTIES_HISTORY (CVTE_ID);
CREATE INDEX MAPRH_ETPT_FK_I ON MATERIAL_PROPERTIES_HISTORY (MTPT_ID);
CREATE INDEX MAPRH_EXPE_FK_I ON MATERIAL_PROPERTIES_HISTORY (MATE_ID);
CREATE INDEX MAPRH_MAPR_FK_I ON MATERIAL_PROPERTIES_HISTORY (MATE_PROP_ID);
CREATE INDEX MAPRH_VUTS_FK_I ON MATERIAL_PROPERTIES_HISTORY (VALID_UNTIL_TIMESTAMP);
CREATE INDEX EXPRH_CVTE_FK_I ON EXPERIMENT_PROPERTIES_HISTORY (CVTE_ID);
CREATE INDEX EXPRH_ETPT_FK_I ON EXPERIMENT_PROPERTIES_HISTORY (ETPT_ID);
CREATE INDEX EXPRH_EXPE_FK_I ON EXPERIMENT_PROPERTIES_HISTORY (EXPE_ID);
CREATE INDEX EXPRH_MAPR_FK_I ON EXPERIMENT_PROPERTIES_HISTORY (MATE_PROP_ID);
CREATE INDEX EXPRH_VUTS_FK_I ON EXPERIMENT_PROPERTIES_HISTORY (VALID_UNTIL_TIMESTAMP);
CREATE INDEX SAPRH_CVTE_FK_I ON SAMPLE_PROPERTIES_HISTORY (CVTE_ID);
CREATE INDEX SAPRH_ETPT_FK_I ON SAMPLE_PROPERTIES_HISTORY (STPT_ID);
CREATE INDEX SAPRH_EXPE_FK_I ON SAMPLE_PROPERTIES_HISTORY (SAMP_ID);
CREATE INDEX SAPRH_MAPR_FK_I ON SAMPLE_PROPERTIES_HISTORY (MATE_PROP_ID);
CREATE INDEX SAPRH_VUTS_FK_I ON SAMPLE_PROPERTIES_HISTORY (VALID_UNTIL_TIMESTAMP);
CREATE INDEX DSPRH_CVTE_FK_I ON DATA_SET_PROPERTIES_HISTORY (CVTE_ID);
CREATE INDEX DSPRH_ETPT_FK_I ON DATA_SET_PROPERTIES_HISTORY (DSTPT_ID);
CREATE INDEX DSPRH_EXPE_FK_I ON DATA_SET_PROPERTIES_HISTORY (DS_ID);
CREATE INDEX DSPRH_MAPR_FK_I ON DATA_SET_PROPERTIES_HISTORY (MATE_PROP_ID);
CREATE INDEX DSPRH_VUTS_FK_I ON DATA_SET_PROPERTIES_HISTORY (VALID_UNTIL_TIMESTAMP);

----------------------------------------------------------------------------------------------------
-- Rules for properties history
----------------------------------------------------------------------------------------------------

-- Material Properties --

CREATE OR REPLACE RULE material_properties_update AS
    ON UPDATE TO material_properties DO ALSO 
       INSERT INTO material_properties_history (
         ID, 
         MATE_ID, 
         MTPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         current_timestamp
       );
       
-- Experiment Properties --

CREATE OR REPLACE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties DO ALSO 
       INSERT INTO experiment_properties_history (
         ID, 
         EXPE_ID,
         ETPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         current_timestamp
       );
       
-- Sample Properties --

CREATE OR REPLACE RULE sample_properties_update AS
    ON UPDATE TO sample_properties DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         current_timestamp
       );
       
-- Data Set Properties --

CREATE OR REPLACE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties DO ALSO
       INSERT INTO data_set_properties_history (
         ID, 
         DS_ID,
         DSTPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         current_timestamp
       );
       
GRANT SELECT ON TABLE material_properties_history TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_properties_history TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_properties_history TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_properties_history TO GROUP OPENBIS_READONLY;
       