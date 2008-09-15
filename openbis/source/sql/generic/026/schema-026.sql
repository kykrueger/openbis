-- D:\DDL\postgresql\schema-023.sql
--
-- Generated for ANSI SQL92 on Fri Jul 04  15:13:22 2008 by Server Generator 10.1.2.6.18
------------------------------------------------------------------------------------
--
--  Post-Generation Modifications:
--
--  1. Changed domain FILE from BIT(32000) to BYTEA
--  2. Changed domain TECH_ID from NUMERIC(20) to BIGINT
--  3. Changed domain BOOLEAN_CHAR from CHAR(1) DEFAULT F to BOOLEAN DEFAULT FALSE
--  4. Removed the check constraints to handle boolean values in Oracle for the
--     tables MATERIAL_TYPE_PROPERTY_TYPES, EXPERIMENT_TYPE_PROPERTY_TYPES and
--     SAMPLE_TYPE_PROPERTY_TYPES (AVCON_%)
--  5. Added the ON DELETE CASCADE qualifier to the foreign keys MAPR_MTPT_FK,
--     EXPR_ETPT_FK and SAPR_STPT_FK
--  6. Add the check constraint directly on the domain BOOLEAN_CHAR_OR_UNKNOWN
--     CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE in ('F', 'T', 'U')) DEFAULT 'U';
--  7. Add the WITH TIMEZONE qualifier to the domain TIME_STAMP
--     CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;
--  8. Add the WITH TIMEZONE and NOT NULL qualifiers to the domain TIME_STAMP_DFL
--     CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
--  9. Extend the domain EVENT_TYPE by adding the CHECK constraint
--     CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE in ('DELETION', 'INVALIDATION', 'MOVEMENT'));
--  10. Extend the domain AUTHORIZATION_ROLE by adding the CHECK constraint
--     CREATE DOMAIN AUTHORIZATION_ROLE as VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'USER', 'OBSERVER', 'ETL_SERVER'));
--  11. Added the Sequence and Index sections
--  12. Added DATABASE_INSTANCES.GLOBAL_CODE column for UUID
--  13. DATABASE_INSTANCES.GLOBAL_CODE renamed to DATABASE_INSTANCES.UUID
--  14. OBSERVABLE_TYPES renamed to DATA_SET_TYPES
--  15. OBSERVABLE_TYPE_ID_SEQ renamed to DATA_SET_TYPE_ID_SEQ
--  16. DATA.OBTY_ID renamed to DATA.DSTY_ID;
------------------------------------------------------------------------------------

-- Creating domains

CREATE DOMAIN AUTHORIZATION_ROLE AS VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'USER', 'OBSERVER', 'ETL_SERVER'));
CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;
CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE IN ('F', 'T', 'U')) DEFAULT 'U';
CREATE DOMAIN CODE AS VARCHAR(40);
CREATE DOMAIN COLUMN_LABEL AS VARCHAR(40);
CREATE DOMAIN DESCRIPTION_1000 AS VARCHAR(1000);
CREATE DOMAIN DESCRIPTION_250 AS VARCHAR(250);
CREATE DOMAIN DESCRIPTION_80 AS VARCHAR(80);
CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE IN ('DELETION', 'INVALIDATION', 'MOVEMENT'));
CREATE DOMAIN FILE AS BYTEA;
CREATE DOMAIN FILE_NAME AS VARCHAR(100);
CREATE DOMAIN GENERIC_VALUE AS VARCHAR(1024);
CREATE DOMAIN OBJECT_NAME AS VARCHAR(50);
CREATE DOMAIN REAL_VALUE AS REAL;
CREATE DOMAIN TECH_ID AS BIGINT;
CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;
CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE DOMAIN USER_ID AS VARCHAR(20);

-- Creating tables

CREATE TABLE CONTROLLED_VOCABULARIES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);
CREATE TABLE CONTROLLED_VOCABULARY_TERMS (ID TECH_ID NOT NULL,CODE OBJECT_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,COVO_ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL);
CREATE TABLE DATA (ID TECH_ID NOT NULL,CODE CODE,DSTY_ID TECH_ID NOT NULL,PROC_ID_PRODUCED_BY TECH_ID NOT NULL,DATA_PRODUCER_CODE CODE,PRODUCTION_TIMESTAMP TIME_STAMP,SAMP_ID_ACQUIRED_FROM TECH_ID,SAMP_ID_DERIVED_FROM TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,IS_PLACEHOLDER BOOLEAN_CHAR DEFAULT 'F',IS_DELETED BOOLEAN_CHAR DEFAULT 'F',IS_VALID BOOLEAN_CHAR DEFAULT 'T');
CREATE TABLE DATABASE_INSTANCES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,UUID CODE NOT NULL,IS_ORIGINAL_SOURCE BOOLEAN_CHAR NOT NULL DEFAULT 'F',REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DAST_ID TECH_ID);
CREATE TABLE DATA_SET_RELATIONSHIPS (DATA_ID_PARENT TECH_ID NOT NULL,DATA_ID_CHILD TECH_ID NOT NULL);
CREATE TABLE DATA_STORES (ID TECH_ID NOT NULL,DBIN_ID TECH_ID NOT NULL,CODE CODE NOT NULL,DOWNLOAD_URL VARCHAR(1024) NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE DATA_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL);
CREATE TABLE EVENTS (ID TECH_ID NOT NULL,EVENT_TYPE EVENT_TYPE NOT NULL,DESCRIPTION DESCRIPTION_250,DATA_ID TECH_ID,REASON DESCRIPTION_250,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE EXPERIMENTS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,EXTY_ID TECH_ID NOT NULL,MATE_ID_STUDY_OBJECT TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PROJ_ID TECH_ID NOT NULL,INVA_ID TECH_ID,IS_PUBLIC BOOLEAN_CHAR NOT NULL DEFAULT 'F',DAST_ID TECH_ID);
CREATE TABLE EXPERIMENT_ATTACHMENTS (ID TECH_ID NOT NULL,EXPE_ID TECH_ID NOT NULL,FILE_NAME FILE_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,VALUE FILE NOT NULL,VERSION INTEGER NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL);
CREATE TABLE EXPERIMENT_PROPERTIES (ID TECH_ID NOT NULL,EXPE_ID TECH_ID NOT NULL,ETPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE EXPERIMENT_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);
CREATE TABLE EXPERIMENT_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,EXTY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE EXTERNAL_DATA (DATA_ID TECH_ID NOT NULL,LOCATION VARCHAR(1024) NOT NULL,FFTY_ID TECH_ID NOT NULL,LOTY_ID TECH_ID NOT NULL,CVTE_ID_STOR_FMT TECH_ID NOT NULL,IS_COMPLETE BOOLEAN_CHAR_OR_UNKNOWN NOT NULL DEFAULT 'U',CVTE_ID_STORE TECH_ID);
CREATE TABLE FILE_FORMAT_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);
CREATE TABLE GROUPS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DBIN_ID TECH_ID NOT NULL,GROU_ID_PARENT TECH_ID,PERS_ID_LEADER TECH_ID,DESCRIPTION DESCRIPTION_250,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,DAST_ID TECH_ID);
CREATE TABLE INVALIDATIONS (ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,REASON DESCRIPTION_250);
CREATE TABLE LOCATOR_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80);
CREATE TABLE MATERIALS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,MATY_ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,MATE_ID_INHIBITOR_OF TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DBIN_ID TECH_ID NOT NULL);
CREATE TABLE MATERIAL_BATCHES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,AMOUNT REAL_VALUE,MATE_ID TECH_ID NOT NULL,PROC_ID TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL);
CREATE TABLE MATERIAL_PROPERTIES (ID TECH_ID NOT NULL,MATE_ID TECH_ID NOT NULL,MTPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,CVTE_ID TECH_ID);
CREATE TABLE MATERIAL_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);
CREATE TABLE MATERIAL_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,MATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL);
CREATE TABLE DATA_SET_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);
CREATE TABLE PERSONS (ID TECH_ID NOT NULL,FIRST_NAME VARCHAR(30),LAST_NAME VARCHAR(30),USER_ID USER_ID NOT NULL,EMAIL OBJECT_NAME,DBIN_ID TECH_ID NOT NULL,GROU_ID TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID);
CREATE TABLE PROCEDURES (ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,EXPE_ID TECH_ID NOT NULL,PCTY_ID TECH_ID NOT NULL);
CREATE TABLE PROCEDURE_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL,IS_DATA_ACQUISITION BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);
CREATE TABLE PROJECTS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,GROU_ID TECH_ID NOT NULL,PERS_ID_LEADER TECH_ID,DESCRIPTION DESCRIPTION_1000,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DAST_ID TECH_ID);
CREATE TABLE PROPERTY_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL,LABEL COLUMN_LABEL NOT NULL,DATY_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,COVO_ID TECH_ID,IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);
CREATE TABLE ROLE_ASSIGNMENTS (ID TECH_ID NOT NULL,ROLE_CODE AUTHORIZATION_ROLE NOT NULL,GROU_ID TECH_ID,DBIN_ID TECH_ID,PERS_ID_GRANTEE TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE SAMPLES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,SAMP_ID_TOP TECH_ID,SAMP_ID_GENERATED_FROM TECH_ID,SATY_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,INVA_ID TECH_ID,SAMP_ID_CONTROL_LAYOUT TECH_ID,DBIN_ID TECH_ID,GROU_ID TECH_ID,SAMP_ID_PART_OF TECH_ID);
CREATE TABLE SAMPLE_INPUTS (SAMP_ID TECH_ID NOT NULL,PROC_ID TECH_ID NOT NULL);
CREATE TABLE SAMPLE_MATERIAL_BATCHES (SAMP_ID TECH_ID NOT NULL,MABA_ID TECH_ID NOT NULL);
CREATE TABLE SAMPLE_PROPERTIES (ID TECH_ID NOT NULL,SAMP_ID TECH_ID NOT NULL,STPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE SAMPLE_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);
CREATE TABLE SAMPLE_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,SATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);

-- Creating sequences

CREATE SEQUENCE CONTROLLED_VOCABULARY_ID_SEQ;
CREATE SEQUENCE CVTE_ID_SEQ;
CREATE SEQUENCE DATABASE_INSTANCE_ID_SEQ;
CREATE SEQUENCE DATA_ID_SEQ;
CREATE SEQUENCE DATA_SET_RELATIONSHIP_ID_SEQ;
CREATE SEQUENCE DATA_STORE_ID_SEQ;
CREATE SEQUENCE DATA_TYPE_ID_SEQ;
CREATE SEQUENCE ETPT_ID_SEQ;
CREATE SEQUENCE EVENT_ID_SEQ;
CREATE SEQUENCE EXPERIMENT_ATTACHMENT_ID_SEQ;
CREATE SEQUENCE EXPERIMENT_ID_SEQ;
CREATE SEQUENCE EXPERIMENT_PROPERTY_ID_SEQ;
CREATE SEQUENCE EXPERIMENT_TYPE_ID_SEQ;
CREATE SEQUENCE FILE_FORMAT_TYPE_ID_SEQ;
CREATE SEQUENCE GROUP_ID_SEQ;
CREATE SEQUENCE INVALIDATION_ID_SEQ;
CREATE SEQUENCE LOCATOR_TYPE_ID_SEQ;
CREATE SEQUENCE MATERIAL_BATCH_ID_SEQ;
CREATE SEQUENCE MATERIAL_ID_SEQ;
CREATE SEQUENCE MATERIAL_PROPERTY_ID_SEQ;
CREATE SEQUENCE MATERIAL_TYPE_ID_SEQ;
CREATE SEQUENCE MTPT_ID_SEQ;
CREATE SEQUENCE DATA_SET_TYPE_ID_SEQ;
CREATE SEQUENCE PERSON_ID_SEQ;
CREATE SEQUENCE PROCEDURE_ID_SEQ;
CREATE SEQUENCE PROCEDURE_TYPE_ID_SEQ;
CREATE SEQUENCE PROJECT_ID_SEQ;
CREATE SEQUENCE PROPERTY_TYPE_ID_SEQ;
CREATE SEQUENCE ROLE_ASSIGNMENT_ID_SEQ;
CREATE SEQUENCE SAMPLE_ID_SEQ;
CREATE SEQUENCE SAMPLE_PROPERTY_ID_SEQ;
CREATE SEQUENCE SAMPLE_TYPE_ID_SEQ;
CREATE SEQUENCE STPT_ID_SEQ;

-- Creating primary key constraints

ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PK PRIMARY KEY(ID);
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PK PRIMARY KEY(ID);
ALTER TABLE DATA ADD CONSTRAINT DATA_PK PRIMARY KEY(ID);
ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_PK PRIMARY KEY(ID);
ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_PK PRIMARY KEY(ID);
ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_PK PRIMARY KEY(ID);
ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PK PRIMARY KEY(ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PK PRIMARY KEY(ID);
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_PK PRIMARY KEY(ID);
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PK PRIMARY KEY(ID);
ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_PK PRIMARY KEY(ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PK PRIMARY KEY(ID);
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_PK PRIMARY KEY(DATA_ID);
ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_PK PRIMARY KEY(ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_PK PRIMARY KEY(ID);
ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PK PRIMARY KEY(ID);
ALTER TABLE LOCATOR_TYPES ADD CONSTRAINT LOTY_PK PRIMARY KEY(ID);
ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PK PRIMARY KEY(ID);
ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PK PRIMARY KEY(ID);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PK PRIMARY KEY(ID);
ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_PK PRIMARY KEY(ID);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PK PRIMARY KEY(ID);
ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_PK PRIMARY KEY(ID);
ALTER TABLE PERSONS ADD CONSTRAINT PERS_PK PRIMARY KEY(ID);
ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_PK PRIMARY KEY(ID);
ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_PK PRIMARY KEY(ID);
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PK PRIMARY KEY(ID);
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PK PRIMARY KEY(ID);
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PK PRIMARY KEY(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PK PRIMARY KEY(ID);
ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_PK PRIMARY KEY(PROC_ID,SAMP_ID);
ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_PK PRIMARY KEY(SAMP_ID,MABA_ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PK PRIMARY KEY(ID);
ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_PK PRIMARY KEY(ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PK PRIMARY KEY(ID);

-- Creating unique constraints

ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE,DBIN_ID);
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_BK_UK UNIQUE(CODE,COVO_ID);
ALTER TABLE DATA ADD CONSTRAINT DATA_BK_UK UNIQUE(CODE);
ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_BK_UK UNIQUE(CODE);
ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_UUID_UK UNIQUE(UUID);
ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_BK_UK UNIQUE(DATA_ID_CHILD,DATA_ID_PARENT);
ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_BK_UK UNIQUE(CODE);
ALTER TABLE EVENTS ADD CONSTRAINT EVNT_BK_UK UNIQUE(EVENT_TYPE,DATA_ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_BK_UK UNIQUE(CODE,PROJ_ID);
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_BK_UK UNIQUE(EXPE_ID,FILE_NAME,VERSION);
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_BK_UK UNIQUE(EXPE_ID,ETPT_ID);
ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_BK_UK UNIQUE(EXTY_ID,PRTY_ID);
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_BK_UK UNIQUE(LOCATION,LOTY_ID);
ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE LOCATOR_TYPES ADD CONSTRAINT LOTY_BK_UK UNIQUE(CODE);
ALTER TABLE MATERIALS ADD CONSTRAINT MATE_BK_UK UNIQUE(CODE,MATY_ID,DBIN_ID);
ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_BK_UK UNIQUE(CODE,MATE_ID);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_BK_UK UNIQUE(MATE_ID,MTPT_ID);
ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_BK_UK UNIQUE(MATY_ID,PRTY_ID);
ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE PERSONS ADD CONSTRAINT PERS_BK_UK UNIQUE(DBIN_ID,USER_ID);
ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_BK_UK UNIQUE(CODE,GROU_ID);
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE,DBIN_ID);
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,GROU_ID,DBIN_ID);
ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_BK_UK UNIQUE(SAMP_ID,PROC_ID);
ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_BK_UK UNIQUE(MABA_ID,SAMP_ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_BK_UK UNIQUE(SAMP_ID,STPT_ID);
ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_BK_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_BK_UK UNIQUE(SATY_ID,PRTY_ID);

-- Creating foreign key constraints

ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE DATA ADD CONSTRAINT DATA_OBTY_FK FOREIGN KEY (DSTY_ID) REFERENCES DATA_SET_TYPES(ID);
ALTER TABLE DATA ADD CONSTRAINT DATA_PROC_PRODUCED_BY_FK FOREIGN KEY (PROC_ID_PRODUCED_BY) REFERENCES PROCEDURES(ID);
ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_FK_ACQUIRED_FROM FOREIGN KEY (SAMP_ID_ACQUIRED_FROM) REFERENCES SAMPLES(ID);
ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_FK_DERIVED_FROM FOREIGN KEY (SAMP_ID_DERIVED_FROM) REFERENCES SAMPLES(ID);
ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);
ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_CHILD FOREIGN KEY (DATA_ID_CHILD) REFERENCES DATA(ID);
ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_PARENT FOREIGN KEY (DATA_ID_PARENT) REFERENCES DATA(ID);
ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE EVENTS ADD CONSTRAINT EVNT_DATA_FK FOREIGN KEY (DATA_ID) REFERENCES DATA(ID);
ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_MATE_FK FOREIGN KEY (MATE_ID_STUDY_OBJECT) REFERENCES MATERIALS(ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PROJ_FK FOREIGN KEY (PROJ_ID) REFERENCES PROJECTS(ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_ETPT_FK FOREIGN KEY (ETPT_ID) REFERENCES EXPERIMENT_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_CVTE_FK FOREIGN KEY (CVTE_ID_STOR_FMT) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_CVTE_STORED_ON_FK FOREIGN KEY (CVTE_ID_STORE) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_DATA_FK FOREIGN KEY (DATA_ID) REFERENCES DATA(ID);
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_FFTY_FK FOREIGN KEY (FFTY_ID) REFERENCES FILE_FORMAT_TYPES(ID);
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_LOTY_FK FOREIGN KEY (LOTY_ID) REFERENCES LOCATOR_TYPES(ID);
ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_GROU_FK FOREIGN KEY (GROU_ID_PARENT) REFERENCES GROUPS(ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE GROUPS ADD CONSTRAINT GROU_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);
ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE MATERIALS ADD CONSTRAINT MATE_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE MATERIALS ADD CONSTRAINT MATE_MATE_FK FOREIGN KEY (MATE_ID_INHIBITOR_OF) REFERENCES MATERIALS(ID);
ALTER TABLE MATERIALS ADD CONSTRAINT MATE_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID);
ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_MATE_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID);
ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PROC_FK FOREIGN KEY (PROC_ID) REFERENCES PROCEDURES(ID);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MATE_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MTPT_FK FOREIGN KEY (MTPT_ID) REFERENCES MATERIAL_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);
ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE PERSONS ADD CONSTRAINT PERS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE PERSONS ADD CONSTRAINT PERS_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);
ALTER TABLE PERSONS ADD CONSTRAINT PERS_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);
ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_PCTY_FK FOREIGN KEY (PCTY_ID) REFERENCES PROCEDURE_TYPES(ID);
ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID);
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DATY_FK FOREIGN KEY (DATY_ID) REFERENCES DATA_TYPES(ID);
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_GRANTEE FOREIGN KEY (PERS_ID_GRANTEE) REFERENCES PERSONS(ID);
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_CONTROL_LAYOUT FOREIGN KEY (SAMP_ID_CONTROL_LAYOUT) REFERENCES SAMPLES(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_GENERATED_FROM FOREIGN KEY (SAMP_ID_GENERATED_FROM) REFERENCES SAMPLES(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_PART_OF FOREIGN KEY (SAMP_ID_PART_OF) REFERENCES SAMPLES(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_TOP FOREIGN KEY (SAMP_ID_TOP) REFERENCES SAMPLES(ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID);
ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_PROC_FK FOREIGN KEY (PROC_ID) REFERENCES PROCEDURES(ID);
ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);
ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_MABA_FK FOREIGN KEY (MABA_ID) REFERENCES MATERIAL_BATCHES(ID);
ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_STPT_FK FOREIGN KEY (STPT_ID) REFERENCES SAMPLE_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID);

-- Creating check constraints

ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_ARC_CK CHECK ((SAMP_ID_ACQUIRED_FROM IS NOT NULL AND SAMP_ID_DERIVED_FROM IS NULL) OR (SAMP_ID_ACQUIRED_FROM IS NULL AND SAMP_ID_DERIVED_FROM IS NOT NULL));
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_DBIN_GROU_ARC_CK CHECK ((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL));
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_DBIN_GROU_ARC_CK CHECK ((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL));
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));

-- Creating indices

CREATE INDEX COVO_PERS_FK_I ON CONTROLLED_VOCABULARIES (PERS_ID_REGISTERER);
CREATE INDEX CVTE_COVO_FK_I ON CONTROLLED_VOCABULARY_TERMS (COVO_ID);
CREATE INDEX CVTE_PERS_FK_I ON CONTROLLED_VOCABULARY_TERMS (PERS_ID_REGISTERER);
CREATE INDEX DATA_OBTY_FK_I ON DATA (DSTY_ID);
CREATE INDEX DATA_PROC_FK_I ON DATA (PROC_ID_PRODUCED_BY);
CREATE INDEX DATA_SAMP_FK_I_ACQUIRED_FROM ON DATA (SAMP_ID_ACQUIRED_FROM);
CREATE INDEX DATA_SAMP_FK_I_DERIVED_FROM ON DATA (SAMP_ID_DERIVED_FROM);
CREATE INDEX DAST_DBIN_FK_I ON DATA_STORES (DBIN_ID);
CREATE INDEX DSRE_DATA_FK_I_CHILD ON DATA_SET_RELATIONSHIPS (DATA_ID_CHILD);
CREATE INDEX DSRE_DATA_FK_I_PARENT ON DATA_SET_RELATIONSHIPS (DATA_ID_PARENT);
CREATE INDEX ETPT_EXTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (EXTY_ID);
CREATE INDEX ETPT_PERS_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);
CREATE INDEX ETPT_PRTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PRTY_ID);
CREATE INDEX EVNT_DATA_FK_I ON EVENTS (DATA_ID);
CREATE INDEX EVNT_PERS_FK_I ON EVENTS (PERS_ID_REGISTERER);
CREATE INDEX EXAT_EXPE_FK_I ON EXPERIMENT_ATTACHMENTS (EXPE_ID);
CREATE INDEX EXAT_PERS_FK_I ON EXPERIMENT_ATTACHMENTS (PERS_ID_REGISTERER);
CREATE INDEX EXDA_CVTE_FK_I ON EXTERNAL_DATA (CVTE_ID_STOR_FMT);
CREATE INDEX EXDA_CVTE_STORED_ON_FK_I ON EXTERNAL_DATA (CVTE_ID_STORE);
CREATE INDEX EXDA_FFTY_FK_I ON EXTERNAL_DATA (FFTY_ID);
CREATE INDEX EXDA_LOTY_FK_I ON EXTERNAL_DATA (LOTY_ID);
CREATE INDEX EXPE_EXTY_FK_I ON EXPERIMENTS (EXTY_ID);
CREATE INDEX EXPE_INVA_FK_I ON EXPERIMENTS (INVA_ID);
CREATE INDEX EXPE_MATE_FK_I ON EXPERIMENTS (MATE_ID_STUDY_OBJECT);
CREATE INDEX EXPE_PERS_FK_I ON EXPERIMENTS (PERS_ID_REGISTERER);
CREATE INDEX EXPE_PROJ_FK_I ON EXPERIMENTS (PROJ_ID);
CREATE INDEX EXPR_CVTE_FK_I ON EXPERIMENT_PROPERTIES (CVTE_ID);
CREATE INDEX EXPR_ETPT_FK_I ON EXPERIMENT_PROPERTIES (ETPT_ID);
CREATE INDEX EXPR_EXPE_FK_I ON EXPERIMENT_PROPERTIES (EXPE_ID);
CREATE INDEX EXPR_PERS_FK_I ON EXPERIMENT_PROPERTIES (PERS_ID_REGISTERER);
CREATE INDEX GROU_DBIN_FK_I ON GROUPS (DBIN_ID);
CREATE INDEX GROU_GROU_FK_I ON GROUPS (GROU_ID_PARENT);
CREATE INDEX GROU_PERS_FK_I ON GROUPS (PERS_ID_LEADER);
CREATE INDEX GROU_PERS_REGISTERED_BY_FK_I ON GROUPS (PERS_ID_REGISTERER);
CREATE INDEX INVA_PERS_FK_I ON INVALIDATIONS (PERS_ID_REGISTERER);
CREATE INDEX MABA_MATE_FK_I ON MATERIAL_BATCHES (MATE_ID);
CREATE INDEX MABA_PERS_FK_I ON MATERIAL_BATCHES (PERS_ID_REGISTERER);
CREATE INDEX MABA_PROC_FK_I ON MATERIAL_BATCHES (PROC_ID);
CREATE INDEX MAPR_CVTE_FK_I ON MATERIAL_PROPERTIES (CVTE_ID);
CREATE INDEX MAPR_MATE_FK_I ON MATERIAL_PROPERTIES (MATE_ID);
CREATE INDEX MAPR_MTPT_FK_I ON MATERIAL_PROPERTIES (MTPT_ID);
CREATE INDEX MAPR_PERS_FK_I ON MATERIAL_PROPERTIES (PERS_ID_REGISTERER);
CREATE INDEX MATE_MATE_FK_I ON MATERIALS (MATE_ID_INHIBITOR_OF);
CREATE INDEX MATE_MATY_FK_I ON MATERIALS (MATY_ID);
CREATE INDEX MATE_PERS_FK_I ON MATERIALS (PERS_ID_REGISTERER);
CREATE INDEX MTPT_MATY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (MATY_ID);
CREATE INDEX MTPT_PERS_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);
CREATE INDEX MTPT_PRTY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (PRTY_ID);
CREATE INDEX PERS_GROU_FK_I ON PERSONS (GROU_ID);
CREATE INDEX PROC_EXPE_FK_I ON PROCEDURES (EXPE_ID);
CREATE INDEX PROC_PCTY_FK_I ON PROCEDURES (PCTY_ID);
CREATE INDEX PROJ_GROU_FK_I ON PROJECTS (GROU_ID);
CREATE INDEX PROJ_PERS_FK_I_LEADER ON PROJECTS (PERS_ID_LEADER);
CREATE INDEX PROJ_PERS_FK_I_REGISTERER ON PROJECTS (PERS_ID_REGISTERER);
CREATE INDEX PRTY_COVO_FK_I ON PROPERTY_TYPES (COVO_ID);
CREATE INDEX PRTY_DATY_FK_I ON PROPERTY_TYPES (DATY_ID);
CREATE INDEX PRTY_PERS_FK_I ON PROPERTY_TYPES (PERS_ID_REGISTERER);
CREATE INDEX ROAS_DBIN_FK_I ON ROLE_ASSIGNMENTS (DBIN_ID);
CREATE INDEX ROAS_GROU_FK_I ON ROLE_ASSIGNMENTS (GROU_ID);
CREATE INDEX ROAS_PERS_FK_I_GRANTEE ON ROLE_ASSIGNMENTS (PERS_ID_GRANTEE);
CREATE INDEX ROAS_PERS_FK_I_REGISTERER ON ROLE_ASSIGNMENTS (PERS_ID_REGISTERER);
CREATE INDEX SAIN_PROC_FK_I ON SAMPLE_INPUTS (PROC_ID);
CREATE INDEX SAIN_SAMP_FK_I ON SAMPLE_INPUTS (SAMP_ID);
CREATE INDEX SAMB_MABA_FK_I ON SAMPLE_MATERIAL_BATCHES (MABA_ID);
CREATE INDEX SAMB_SAMP_FK_I ON SAMPLE_MATERIAL_BATCHES (SAMP_ID);
CREATE INDEX SAMP_INVA_FK_I ON SAMPLES (INVA_ID);
CREATE INDEX SAMP_PERS_FK_I ON SAMPLES (PERS_ID_REGISTERER);
CREATE INDEX SAMP_SAMP_FK_I_CONTROL_LAYOUT ON SAMPLES (SAMP_ID_CONTROL_LAYOUT);
CREATE INDEX SAMP_SAMP_FK_I_GENERATED_FROM ON SAMPLES (SAMP_ID_GENERATED_FROM);
CREATE INDEX SAMP_SAMP_FK_I_PART_OF ON SAMPLES (SAMP_ID_PART_OF);
CREATE INDEX SAMP_SAMP_FK_I_TOP ON SAMPLES (SAMP_ID_TOP);
CREATE INDEX SAMP_CODE_I ON SAMPLES (CODE);
CREATE INDEX SAMP_SATY_FK_I ON SAMPLES (SATY_ID);
CREATE INDEX SAPR_CVTE_FK_I ON SAMPLE_PROPERTIES (CVTE_ID);
CREATE INDEX SAPR_PERS_FK_I ON SAMPLE_PROPERTIES (PERS_ID_REGISTERER);
CREATE INDEX SAPR_SAMP_FK_I ON SAMPLE_PROPERTIES (SAMP_ID);
CREATE INDEX SAPR_STPT_FK_I ON SAMPLE_PROPERTIES (STPT_ID);
CREATE INDEX STPT_PERS_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);
CREATE INDEX STPT_PRTY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PRTY_ID);
CREATE INDEX STPT_SATY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (SATY_ID);
