-- add data type SAMPLE
insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'SAMPLE'
 ,'Reference to a sample'
);

-- add sample tyep FK to property types
alter table PROPERTY_TYPES add column SATY_PROP_ID TECH_ID;
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_SATY_FK FOREIGN KEY (SATY_PROP_ID) REFERENCES SAMPLE_TYPES(ID) ON DELETE CASCADE;

-- add SAMPLE FK to sample properties
alter table SAMPLE_PROPERTIES add column SAMP_PROP_ID TECH_ID;
alter table SAMPLE_PROPERTIES drop constraint SAPR_CK;
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK 
    ((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NOT NULL)
    );
CREATE INDEX SAPR_SAPR_FK_I ON SAMPLE_PROPERTIES (SAMP_PROP_ID);

-- add SAMPLE identifier to sample properties history
alter table SAMPLE_PROPERTIES_HISTORY add column SAMPLE IDENTIFIER;
alter table SAMPLE_PROPERTIES_HISTORY drop constraint SAPRH_CK;
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_CK CHECK 
    ((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL) OR 
     (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL AND SAMPLE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL AND SAMPLE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NOT NULL)
    );

drop view sample_history_view;
CREATE VIEW sample_history_view AS (
  SELECT
    2*id as id,
    main_samp_id,
    relation_type,
    space_id,
    expe_id,
    samp_id,
    proj_id,
    data_id,
    entity_perm_id,
    null as stpt_id,
    null as value,
    null as vocabulary_term,
    null as material,
    null as sample,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp
  FROM
    SAMPLE_RELATIONSHIPS_HISTORY
  WHERE
    valid_until_timestamp IS NOT NULL)
UNION
  SELECT
    2*id+1 as id,
    samp_id as main_samp_id,
    null as relation_type,
    null as space_id,
    null as expe_id,
    null as samp_id,
    null as proj_id,
    null as data_id,
    null as entity_perm_id,
    stpt_id,
    value,
    vocabulary_term,
    material,
    sample,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp
  FROM
    SAMPLE_PROPERTIES_HISTORY;

CREATE OR REPLACE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID)
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
        OR (OLD.SAMP_PROP_ID IS NOT NULL AND OLD.SAMP_PROP_ID != NEW.SAMP_PROP_ID)
    DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL,
         SAMPLE,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         (select perm_id from samples_all where id = OLD.SAMP_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );
CREATE OR REPLACE RULE sample_properties_delete AS
    ON DELETE TO sample_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
        OR OLD.SAMP_PROP_ID IS NOT NULL)
       AND (SELECT DEL_ID FROM SAMPLES_ALL WHERE ID = OLD.SAMP_ID) IS NULL
     DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL,
         SAMPLE,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         (select perm_id from samples_all where id = OLD.SAMP_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );

