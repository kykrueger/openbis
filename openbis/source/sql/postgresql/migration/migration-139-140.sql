-- Migration from 139-140

-- remove dbin_id from role_assignments
ALTER TABLE role_assignments DROP CONSTRAINT ROAS_PE_INSTANCE_BK_UK;
ALTER TABLE role_assignments DROP CONSTRAINT ROAS_AG_INSTANCE_BK_UK;
ALTER TABLE role_assignments DROP CONSTRAINT ROAS_DBIN_FK;
ALTER TABLE role_assignments DROP CONSTRAINT ROAS_DBIN_SPACE_ARC_CK;
ALTER TABLE role_assignments DROP CONSTRAINT ROAS_AG_PERS_ARC_CK;
DROP INDEX ROAS_DBIN_FK_I;
ALTER TABLE role_assignments DROP COLUMN dbin_id;

ALTER TABLE material_types DROP CONSTRAINT MATY_DBIN_FK;
ALTER TABLE material_types DROP CONSTRAINT MATY_BK_UK;
ALTER TABLE material_types DROP COLUMN dbin_id;

ALTER TABLE experiment_types DROP CONSTRAINT EXTY_BK_UK;
ALTER TABLE experiment_types DROP CONSTRAINT EXTY_DBIN_FK;
ALTER TABLE experiment_types DROP COLUMN dbin_id;

ALTER TABLE sample_types DROP CONSTRAINT SATY_BK_UK;
ALTER TABLE sample_types DROP CONSTRAINT SATY_DBIN_FK;
ALTER TABLE sample_types DROP COLUMN dbin_id;

ALTER TABLE data_set_types DROP CONSTRAINT DSTY_BK_UK;
ALTER TABLE data_set_types DROP CONSTRAINT DSTY_DBIN_FK;
ALTER TABLE data_set_types DROP COLUMN dbin_id;

ALTER TABLE spaces DROP CONSTRAINT SPACE_DBIN_FK;
ALTER TABLE spaces DROP CONSTRAINT SPACE_BK_UK;
ALTER TABLE spaces DROP COLUMN dbin_id;

ALTER TABLE samples_all DROP CONSTRAINT SAMP_DBIN_SPACE_ARC_CK;
