-- Migration from 122 to 123

ALTER TABLE events DROP CONSTRAINT evnt_et_enum_ck;
ALTER TABLE events ADD CONSTRAINT evnt_et_enum_ck CHECK 
	(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'SPACE', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY', 'AUTHORIZATION_GROUP', 'METAPROJECT')); 
