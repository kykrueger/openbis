--
-- Feature: Unique property values
--

-- Columns used to store information regarding what properties should have unique values.

ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD COLUMN IS_UNIQUE BOOLEAN_CHAR NOT NULL DEFAULT FALSE;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD COLUMN IS_UNIQUE BOOLEAN_CHAR NOT NULL DEFAULT FALSE;
ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD COLUMN IS_UNIQUE BOOLEAN_CHAR NOT NULL DEFAULT FALSE;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD COLUMN IS_UNIQUE BOOLEAN_CHAR NOT NULL DEFAULT FALSE;

ALTER TABLE SAMPLE_PROPERTIES ADD COLUMN IS_UNIQUE BOOLEAN_CHAR NOT NULL DEFAULT FALSE;
ALTER TABLE EXPERIMENT_PROPERTIES ADD COLUMN IS_UNIQUE BOOLEAN_CHAR NOT NULL DEFAULT FALSE;
ALTER TABLE DATA_SET_PROPERTIES ADD COLUMN IS_UNIQUE BOOLEAN_CHAR NOT NULL DEFAULT FALSE;
ALTER TABLE MATERIAL_PROPERTIES ADD COLUMN IS_UNIQUE BOOLEAN_CHAR NOT NULL DEFAULT FALSE;

-- These references exist to ensure referential integrity when modifying the database directly, they add some overhead to insert operations.
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT SAMPLE_TYPE_PROPERTY_TYPES_UNIQUE UNIQUE (ID, IS_UNIQUE);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT EXPERIMENT_TYPE_PROPERTY_TYPES_UNIQUE UNIQUE (ID, IS_UNIQUE);
ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DATA_SET_TYPE_PROPERTY_TYPES_UNIQUE UNIQUE (ID, IS_UNIQUE);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MATERIAL_TYPE_PROPERTY_TYPES_UNIQUE UNIQUE (ID, IS_UNIQUE);

ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAMPLE_PROPERTIES_UNIQUE_FK FOREIGN KEY (STPT_ID, IS_UNIQUE) REFERENCES SAMPLE_TYPE_PROPERTY_TYPES(ID, IS_UNIQUE);
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPERIMENT_PROPERTIES_UNIQUE_FK FOREIGN KEY (ETPT_ID, IS_UNIQUE) REFERENCES EXPERIMENT_TYPE_PROPERTY_TYPES(ID, IS_UNIQUE);
ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DATA_SET_PROPERTIES_UNIQUE_FK FOREIGN KEY (DSTPT_ID, IS_UNIQUE) REFERENCES DATA_SET_TYPE_PROPERTY_TYPES(ID, IS_UNIQUE);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MATERIAL_PROPERTIES_UNIQUE_FK FOREIGN KEY (MTPT_ID, IS_UNIQUE) REFERENCES MATERIAL_TYPE_PROPERTY_TYPES(ID, IS_UNIQUE);

-- These constraints ensure uniqueness only when is_unique is TRUE, partial unique indices can't have NULL values, so one index per column is needed
CREATE UNIQUE INDEX SAMPLE_PROPERTIES_UNIQUE_VALUE ON SAMPLE_PROPERTIES (STPT_ID, VALUE) WHERE IS_UNIQUE AND VALUE IS NOT NULL;
CREATE UNIQUE INDEX SAMPLE_PROPERTIES_UNIQUE_CVTE ON SAMPLE_PROPERTIES (STPT_ID, CVTE_ID) WHERE IS_UNIQUE AND CVTE_ID IS NOT NULL;
CREATE UNIQUE INDEX SAMPLE_PROPERTIES_UNIQUE_MATE ON SAMPLE_PROPERTIES (STPT_ID, MATE_PROP_ID) WHERE IS_UNIQUE AND MATE_PROP_ID IS NOT NULL;
CREATE UNIQUE INDEX SAMPLE_PROPERTIES_UNIQUE_SAMP ON SAMPLE_PROPERTIES (STPT_ID, SAMP_PROP_ID) WHERE IS_UNIQUE AND SAMP_PROP_ID IS NOT NULL;

CREATE UNIQUE INDEX EXPERIMENT_PROPERTIES_UNIQUE_VALUE ON EXPERIMENT_PROPERTIES (ETPT_ID, VALUE) WHERE IS_UNIQUE AND VALUE IS NOT NULL;
CREATE UNIQUE INDEX EXPERIMENT_PROPERTIES_UNIQUE_CVTE ON EXPERIMENT_PROPERTIES (ETPT_ID, CVTE_ID) WHERE IS_UNIQUE AND CVTE_ID IS NOT NULL;
CREATE UNIQUE INDEX EXPERIMENT_PROPERTIES_UNIQUE_MATE ON EXPERIMENT_PROPERTIES (ETPT_ID, MATE_PROP_ID) WHERE IS_UNIQUE AND MATE_PROP_ID IS NOT NULL;
CREATE UNIQUE INDEX EXPERIMENT_PROPERTIES_UNIQUE_SAMP ON EXPERIMENT_PROPERTIES (ETPT_ID, SAMP_PROP_ID) WHERE IS_UNIQUE AND SAMP_PROP_ID IS NOT NULL;

CREATE UNIQUE INDEX DATA_SET_PROPERTIES_UNIQUE_VALUE ON DATA_SET_PROPERTIES (DSTPT_ID, VALUE) WHERE IS_UNIQUE AND VALUE IS NOT NULL;
CREATE UNIQUE INDEX DATA_SET_PROPERTIES_UNIQUE_CVTE ON DATA_SET_PROPERTIES (DSTPT_ID, CVTE_ID) WHERE IS_UNIQUE AND CVTE_ID IS NOT NULL;
CREATE UNIQUE INDEX DATA_SET_PROPERTIES_UNIQUE_MATE ON DATA_SET_PROPERTIES (DSTPT_ID, MATE_PROP_ID) WHERE IS_UNIQUE AND MATE_PROP_ID IS NOT NULL;
CREATE UNIQUE INDEX DATA_SET_PROPERTIES_UNIQUE_SAMP ON DATA_SET_PROPERTIES (DSTPT_ID, SAMP_PROP_ID) WHERE IS_UNIQUE AND SAMP_PROP_ID IS NOT NULL;

CREATE UNIQUE INDEX MATERIAL_PROPERTIES_UNIQUE_VALUE ON MATERIAL_PROPERTIES (MTPT_ID, VALUE) WHERE IS_UNIQUE AND VALUE IS NOT NULL;
CREATE UNIQUE INDEX MATERIAL_PROPERTIES_UNIQUE_CVTE ON MATERIAL_PROPERTIES (MTPT_ID, CVTE_ID) WHERE IS_UNIQUE AND CVTE_ID IS NOT NULL;
CREATE UNIQUE INDEX MATERIAL_PROPERTIES_UNIQUE_MATE ON MATERIAL_PROPERTIES (MTPT_ID, MATE_PROP_ID) WHERE IS_UNIQUE AND MATE_PROP_ID IS NOT NULL;