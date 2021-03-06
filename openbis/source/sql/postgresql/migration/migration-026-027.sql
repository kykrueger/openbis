-- remove the bug from previous db version
ALTER TABLE ROLE_ASSIGNMENTS DROP CONSTRAINT ROAS_BK_UK;
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_GROUP_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,GROU_ID);
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_INSTANCE_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,DBIN_ID);

-- add display properties to sample type

ALTER TABLE SAMPLE_TYPES ADD COLUMN IS_LISTABLE BOOLEAN_CHAR NOT NULL DEFAULT 'T';
ALTER TABLE SAMPLE_TYPES ADD COLUMN GENERATED_FROM_DEPTH INTEGER NOT NULL DEFAULT 0;
ALTER TABLE SAMPLE_TYPES ADD COLUMN PART_OF_DEPTH INTEGER NOT NULL DEFAULT 0;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD COLUMN IS_DISPLAYED BOOLEAN_CHAR NOT NULL DEFAULT 'T';

update sample_types
set generated_from_depth = 1
where code = 'DILUTION_PLATE';

update sample_types
set generated_from_depth = 2
where code = 'CELL_PLATE';

update sample_types
set generated_from_depth = 3
where code = 'REINFECT_PLATE';

update sample_types
set is_listable = 'F', generated_from_depth = 0, part_of_depth = 1
where code = 'WELL';

----------------------------------------------------
-- Unify the schema of database v018 migrated to the current version with the original schema.
-- This code has been generated by the external tool.
-- Note that sometimes the changes are not at all significant, but they let us unify the schemas.
----------------------------------------------------

DROP FUNCTION move_exp_samples_to_group();

CREATE OR REPLACE FUNCTION controlled_vocabulary_check() RETURNS "trigger"
    AS $$
DECLARE
   v_code  CODE;
BEGIN

   select code into v_code from data_types where id = NEW.daty_id;

   -- Check if the data is of type "CONTROLLEDVOCABULARY"
   if v_code = 'CONTROLLEDVOCABULARY' then
      if NEW.covo_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;
      end if;
   end if;

   RETURN NEW;

END;
$$
    LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION external_data_storage_format_check() RETURNS "trigger"
    AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$
    LANGUAGE plpgsql;


CREATE SEQUENCE data_set_relationship_id_seq
        START WITH 1
        INCREMENT BY 1
        NO MAXVALUE
        NO MINVALUE
        CACHE 1;

ALTER SEQUENCE group_id_seq
        RESTART WITH 1;

ALTER SEQUENCE material_batch_id_seq
        RESTART WITH 1;

ALTER SEQUENCE material_id_seq
        RESTART WITH 1;

ALTER SEQUENCE material_property_id_seq
        RESTART WITH 1;

ALTER SEQUENCE project_id_seq
        RESTART WITH 1;

ALTER SEQUENCE sample_id_seq
        RESTART WITH 1;

ALTER SEQUENCE sample_property_id_seq
        RESTART WITH 1;

ALTER TABLE controlled_vocabularies
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE controlled_vocabulary_terms
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE data
        ALTER COLUMN code DROP NOT NULL,
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL,
        ALTER COLUMN is_placeholder SET DEFAULT false,
        ALTER COLUMN is_placeholder DROP NOT NULL;

ALTER TABLE experiment_attachments
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE experiment_properties
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE experiment_type_property_types
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE experiments
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE invalidations
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE material_batches
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE material_properties
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE material_type_property_types
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE materials
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE procedures
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE property_types
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE sample_properties
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE sample_type_property_types
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE samples
        ALTER COLUMN registration_timestamp SET DEFAULT now(),
        ALTER COLUMN registration_timestamp SET NOT NULL;

ALTER TABLE data_set_relationships
        DROP CONSTRAINT dsre_pk;

ALTER TABLE sample_inputs
        DROP CONSTRAINT sain_pk;

ALTER TABLE sample_inputs
        ADD CONSTRAINT sain_pk PRIMARY KEY (proc_id, samp_id);

ALTER TABLE data
        DROP CONSTRAINT data_proc_fk;

ALTER TABLE data
        DROP CONSTRAINT data_samp_derived_from_fk;

ALTER TABLE data
        DROP CONSTRAINT data_samp_fk;

ALTER TABLE data
        ADD CONSTRAINT data_proc_produced_by_fk FOREIGN KEY (proc_id_produced_by) REFERENCES procedures(id);

ALTER TABLE data
        ADD CONSTRAINT data_samp_fk_acquired_from FOREIGN KEY (samp_id_acquired_from) REFERENCES samples(id);

ALTER TABLE data
        ADD CONSTRAINT data_samp_fk_derived_from FOREIGN KEY (samp_id_derived_from) REFERENCES samples(id);

ALTER TABLE data_set_relationships
        ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent);

-- There was a bug in migration from db 7 to 18 - the constraint was not created. So we create it if it does not exist.
create function add_mapr_bk_uk_constraint() returns void AS $$
begin
   perform *
     FROM information_schema.table_constraints WHERE constraint_name='mapr_bk_uk';
   if not found
   then
			ALTER TABLE material_properties
        ADD CONSTRAINT mapr_bk_uk UNIQUE (mate_id, mtpt_id);
   end if;
end;
$$ language 'plpgsql';
select add_mapr_bk_uk_constraint();
drop function add_mapr_bk_uk_constraint();

-- There was a bug in migration from db 7 to 18 - the constraint was not dropped. So we drop it now if it exists.
create function remove_maty_uk_constraint() returns void AS $$
begin
   perform *
     FROM information_schema.table_constraints WHERE constraint_name='maty_uk';
   if found
   then
			ALTER TABLE material_types DROP CONSTRAINT maty_uk;
   end if;
end;
$$ language 'plpgsql';
select remove_maty_uk_constraint();
drop function remove_maty_uk_constraint();

ALTER TABLE persons
        ADD CONSTRAINT pers_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);

ALTER TABLE sample_material_batches
        ADD CONSTRAINT samb_bk_uk UNIQUE (maba_id, samp_id);

ALTER TABLE sample_material_batches
        ADD CONSTRAINT samb_maba_fk FOREIGN KEY (maba_id) REFERENCES material_batches(id);

ALTER TABLE sample_material_batches
        ADD CONSTRAINT samb_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);

CREATE INDEX samb_maba_fk_i ON sample_material_batches USING btree (maba_id);

CREATE INDEX samb_samp_fk_i ON sample_material_batches USING btree (samp_id);

----------------------------------------------------
-- END Unify the schema
----------------------------------------------------
