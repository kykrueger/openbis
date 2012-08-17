-- Migration from 117 to 118
-- 
-- Create a new script type - entity type validation
-- add new field validation_script_id to all entity type tables
--

ALTER DOMAIN SCRIPT_TYPE DROP CONSTRAINT script_type_check;
ALTER DOMAIN SCRIPT_TYPE ADD CHECK (VALUE IN ('DYNAMIC_PROPERTY', 'MANAGED_PROPERTY', 'ENTITY_VALIDATION'));

ALTER TABLE DATA_SET_TYPES ADD COLUMN VALIDATION_SCRIPT_ID TECH_ID;
ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT DSTY_SCRIPT_FK FOREIGN KEY (VALIDATION_SCRIPT_ID) REFERENCES SCRIPTS(ID);

ALTER TABLE EXPERIMENT_TYPES ADD COLUMN VALIDATION_SCRIPT_ID TECH_ID;
ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_SCRIPT_FK FOREIGN KEY (VALIDATION_SCRIPT_ID) REFERENCES SCRIPTS(ID);

ALTER TABLE MATERIAL_TYPES ADD COLUMN VALIDATION_SCRIPT_ID TECH_ID;
ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_SCRIPT_FK FOREIGN KEY (VALIDATION_SCRIPT_ID) REFERENCES SCRIPTS(ID);

ALTER TABLE SAMPLE_TYPES ADD COLUMN VALIDATION_SCRIPT_ID TECH_ID;
ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_SCRIPT_FK FOREIGN KEY (VALIDATION_SCRIPT_ID) REFERENCES SCRIPTS(ID);
