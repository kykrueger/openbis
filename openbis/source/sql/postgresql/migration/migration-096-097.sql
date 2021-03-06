-- Migration from 096 to 097

--
-- Add a new "POST_REGISTRATION_DATASET_QUEUE" table with containts
--
CREATE SEQUENCE POST_REGISTRATION_DATASET_QUEUE_ID_SEQ;

CREATE TABLE POST_REGISTRATION_DATASET_QUEUE (
    ID TECH_ID NOT NULL,
    DS_ID TECH_ID NOT NULL 
);    
    
ALTER TABLE POST_REGISTRATION_DATASET_QUEUE ADD CONSTRAINT PRDQ_PK PRIMARY KEY(ID);
    
ALTER TABLE ONLY POST_REGISTRATION_DATASET_QUEUE
    ADD CONSTRAINT prdq_ds_fk FOREIGN KEY (ds_id) REFERENCES data_all(id);

GRANT SELECT ON SEQUENCE POST_REGISTRATION_DATASET_QUEUE_ID_SEQ TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE POST_REGISTRATION_DATASET_QUEUE TO GROUP OPENBIS_READONLY;

