-- Migration from 134 to 135

ALTER TABLE CONTROLLED_VOCABULARIES DROP CONSTRAINT IF EXISTS COVO_PERS_FK;
ALTER TABLE CONTROLLED_VOCABULARY_TERMS DROP CONSTRAINT IF EXISTS CVTE_PERS_FK;
ALTER TABLE DATA_ALL DROP CONSTRAINT IF EXISTS DATA_PERS_FK;
ALTER TABLE DATA_ALL DROP CONSTRAINT IF EXISTS DATA_PERS_FK_MOD;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL DROP CONSTRAINT IF EXISTS DATA_SET_RELATIONSHIPS_PERS_FK;
ALTER TABLE SAMPLE_RELATIONSHIPS_ALL DROP CONSTRAINT IF EXISTS SAMPLE_RELATIONSHIPS_PERS_FK;
ALTER TABLE EVENTS DROP CONSTRAINT IF EXISTS EVNT_PERS_FK;
ALTER TABLE EXPERIMENTS_ALL DROP CONSTRAINT IF EXISTS EXPE_PERS_FK;
ALTER TABLE EXPERIMENTS_ALL DROP CONSTRAINT IF EXISTS EXPE_PERS_FK_MOD;
ALTER TABLE ATTACHMENTS DROP CONSTRAINT IF EXISTS ATTA_PERS_FK;
ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT IF EXISTS EXPR_PERS_FK;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES DROP CONSTRAINT IF EXISTS ETPT_PERS_FK;
ALTER TABLE SPACES DROP CONSTRAINT IF EXISTS SPACE_PERS_FK_REGISTERER;
ALTER TABLE DELETIONS DROP CONSTRAINT IF EXISTS DEL_PERS_FK;
ALTER TABLE MATERIALS DROP CONSTRAINT IF EXISTS MATE_PERS_FK;
ALTER TABLE MATERIAL_PROPERTIES DROP CONSTRAINT IF EXISTS MAPR_PERS_FK;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES DROP CONSTRAINT IF EXISTS MTPT_PERS_FK;
ALTER TABLE PERSONS DROP CONSTRAINT IF EXISTS PERS_PERS_FK;
ALTER TABLE PROJECTS DROP CONSTRAINT IF EXISTS PROJ_PERS_FK_LEADER;
ALTER TABLE PROJECTS DROP CONSTRAINT IF EXISTS PROJ_PERS_FK_REGISTERER;
ALTER TABLE PROJECTS DROP CONSTRAINT IF EXISTS PROJ_PERS_FK_MOD;
ALTER TABLE PROPERTY_TYPES DROP CONSTRAINT IF EXISTS PRTY_PERS_FK;
ALTER TABLE ROLE_ASSIGNMENTS DROP CONSTRAINT IF EXISTS ROAS_PERS_FK_GRANTEE;
ALTER TABLE ROLE_ASSIGNMENTS DROP CONSTRAINT IF EXISTS ROAS_PERS_FK_REGISTERER;
ALTER TABLE SAMPLES_ALL DROP CONSTRAINT IF EXISTS SAMP_PERS_FK;
ALTER TABLE SAMPLES_ALL DROP CONSTRAINT IF EXISTS SAMP_PERS_FK_MOD;
ALTER TABLE SAMPLE_PROPERTIES DROP CONSTRAINT IF EXISTS SAPR_PERS_FK;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES DROP CONSTRAINT IF EXISTS STPT_PERS_FK;
ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES DROP CONSTRAINT IF EXISTS DSTPT_PERS_FK;
ALTER TABLE DATA_SET_PROPERTIES DROP CONSTRAINT IF EXISTS DSPR_PERS_FK;
ALTER TABLE AUTHORIZATION_GROUP_PERSONS DROP CONSTRAINT IF EXISTS AGP_PERS_FK;
ALTER TABLE AUTHORIZATION_GROUPS DROP CONSTRAINT IF EXISTS AG_PERS_FK;
ALTER TABLE FILTERS DROP CONSTRAINT IF EXISTS FILT_PERS_FK;
ALTER TABLE GRID_CUSTOM_COLUMNS DROP CONSTRAINT IF EXISTS GRID_CUSTOM_COLUMNS_PERS_FK;
ALTER TABLE QUERIES DROP CONSTRAINT IF EXISTS QUER_PERS_FK;
ALTER TABLE SCRIPTS DROP CONSTRAINT IF EXISTS SCRI_PERS_FK;
ALTER TABLE METAPROJECTS DROP CONSTRAINT IF EXISTS METAPROJECTS_OWNER_FK;



ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE DATA_ALL ADD CONSTRAINT DATA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE DATA_ALL ADD CONSTRAINT DATA_PERS_FK_MOD FOREIGN KEY (PERS_ID_MODIFIER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD CONSTRAINT DATA_SET_RELATIONSHIPS_PERS_FK FOREIGN KEY (PERS_ID_AUTHOR) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE SAMPLE_RELATIONSHIPS_ALL ADD CONSTRAINT SAMPLE_RELATIONSHIPS_PERS_FK FOREIGN KEY (PERS_ID_AUTHOR) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE EXPERIMENTS_ALL ADD CONSTRAINT EXPE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE EXPERIMENTS_ALL ADD CONSTRAINT EXPE_PERS_FK_MOD FOREIGN KEY (PERS_ID_MODIFIER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE SPACES ADD CONSTRAINT SPACE_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE DELETIONS ADD CONSTRAINT DEL_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE PERSONS ADD CONSTRAINT PERS_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_MOD FOREIGN KEY (PERS_ID_MODIFIER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_GRANTEE FOREIGN KEY (PERS_ID_GRANTEE) REFERENCES PERSONS(ID) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_PERS_FK_MOD FOREIGN KEY (PERS_ID_MODIFIER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE AUTHORIZATION_GROUP_PERSONS ADD CONSTRAINT AGP_PERS_FK FOREIGN KEY (PERS_ID) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE AUTHORIZATION_GROUPS ADD CONSTRAINT AG_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE FILTERS ADD CONSTRAINT FILT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE GRID_CUSTOM_COLUMNS ADD CONSTRAINT GRID_CUSTOM_COLUMNS_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE QUERIES ADD CONSTRAINT QUER_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE SCRIPTS ADD CONSTRAINT SCRI_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE METAPROJECTS ADD CONSTRAINT METAPROJECTS_OWNER_FK FOREIGN KEY (OWNER) REFERENCES PERSONS(ID) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
