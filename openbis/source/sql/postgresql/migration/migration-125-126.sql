-- Migration from 125 to 126

ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD COLUMN SHOW_RAW_VALUE BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD COLUMN SHOW_RAW_VALUE BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD COLUMN SHOW_RAW_VALUE BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD COLUMN SHOW_RAW_VALUE BOOLEAN_CHAR NOT NULL DEFAULT 'F';
