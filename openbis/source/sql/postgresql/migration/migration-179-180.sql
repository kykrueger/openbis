-- Full text search

-- Controlled Vocabularies

ALTER TABLE controlled_vocabulary_terms
    ADD COLUMN tsvector_document TSVECTOR;

CREATE FUNCTION properties_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE cvt RECORD;
BEGIN
    IF NEW.cvte_id IS NOT NULL THEN
        SELECT code, label INTO STRICT cvt FROM controlled_vocabulary_terms WHERE id = NEW.cvte_id;
        NEW.tsvector_document := to_tsvector('english', cvt.code) ||
                to_tsvector('english', coalesce(cvt.label, ''));
    ELSE
        NEW.tsvector_document := to_tsvector('english', coalesce(NEW.value, ''));
        RETURN NEW;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Samples

CREATE FUNCTION samples_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
    space_code VARCHAR;
    container_code VARCHAR;
    identifier VARCHAR := '/';
BEGIN
    IF NEW.space_id IS NOT NULL THEN
        SELECT code INTO STRICT space_code FROM spaces WHERE id = NEW.space_id;
        identifier := identifier || space_code || '/';
    END IF;

    IF NEW.proj_id IS NOT NULL THEN
        IF NEW.space_id IS NOT NULL THEN
            SELECT code INTO STRICT proj_code FROM projects WHERE id = NEW.proj_id;
        ELSE
            SELECT p.code, s.code INTO STRICT proj_code, space_code FROM projects p
                INNER JOIN spaces s ON p.space_id = s.id WHERE id = NEW.proj_id;
            identifier := identifier || space_code || '/';
        END IF;

        identifier := identifier || proj_code || '/';
    END IF;

    identifier := identifier || NEW.code;

    IF NEW.samp_id_part_of IS NOT NULL THEN
        SELECT code INTO STRICT container_code FROM samples_all WHERE id = NEW.samp_id_part_of;
        identifier := identifier || '\:' || container_code;
    END IF;

    NEW.tsvector_document := (NEW.perm_id || ':1')::tsvector || (NEW.code || ':1')::tsvector
                || (identifier || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE samples_all
    ADD COLUMN tsvector_document TSVECTOR;

CREATE OR REPLACE VIEW samples AS
    SELECT id, perm_id, code, proj_id, proj_frozen, expe_id, expe_frozen, saty_id, registration_timestamp,
           modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, space_frozen,
           samp_id_part_of, cont_frozen, version, frozen, frozen_for_comp, frozen_for_children, frozen_for_parents,
           frozen_for_data, tsvector_document
    FROM samples_all
    WHERE del_id IS NULL;

CREATE OR REPLACE VIEW samples_deleted AS
    SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, proj_id, samp_id_part_of, version,
           tsvector_document
    FROM samples_all
    WHERE del_id IS NOT NULL;

CREATE TRIGGER samples_all_tsvector_document BEFORE INSERT OR UPDATE
    ON samples_all FOR EACH ROW EXECUTE FUNCTION
    samples_all_tsvector_document_trigger();

UPDATE samples_all SET code = code;

ALTER TABLE sample_properties
    ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER sample_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON sample_properties FOR EACH ROW EXECUTE FUNCTION
    properties_tsvector_document_trigger();

UPDATE sample_properties SET value = value;

ALTER TABLE sample_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX sample_properties_search_index ON sample_properties USING gin(tsvector_document);

-- Experiments

CREATE FUNCTION experiments_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
        space_code VARCHAR;
BEGIN
    SELECT p.code, s.code INTO STRICT proj_code, space_code FROM projects p
        INNER JOIN spaces s ON p.space_id = s.id WHERE p.id = NEW.proj_id;
    NEW.tsvector_document := (NEW.perm_id || ':1')::tsvector || (NEW.code || ':1')::tsvector
                || ('/' || space_code || '/' || proj_code || '/' || NEW.code || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE experiments_all
    ADD COLUMN tsvector_document TSVECTOR;

CREATE OR REPLACE VIEW experiments AS
    SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp,
           proj_id, proj_frozen, del_id, orig_del, is_public, version, frozen, frozen_for_samp, frozen_for_data, tsvector_document
    FROM experiments_all
    WHERE del_id IS NULL;

CREATE OR REPLACE VIEW experiments_deleted AS
    SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id,
           orig_del, is_public, version, tsvector_document
    FROM experiments_all
    WHERE del_id IS NOT NULL;

CREATE TRIGGER experiments_all_tsvector_document BEFORE INSERT OR UPDATE
    ON experiments_all FOR EACH ROW EXECUTE FUNCTION
    experiments_all_tsvector_document_trigger();

UPDATE experiments_all SET code = code;

ALTER TABLE experiment_properties
    ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER experiment_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON experiment_properties FOR EACH ROW EXECUTE FUNCTION
    properties_tsvector_document_trigger();

UPDATE experiment_properties SET value = value;

ALTER TABLE experiment_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX experiment_properties_search_index ON experiment_properties USING gin(tsvector_document);

-- Data sets

CREATE FUNCTION data_all_tsvector_document_trigger() RETURNS trigger AS $$
BEGIN
    NEW.tsvector_document := (NEW.data_set_kind || ':1')::tsvector || (NEW.code || ':1')::tsvector
        || ('/' || NEW.code || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE data_all
    ADD COLUMN tsvector_document TSVECTOR;

CREATE OR REPLACE VIEW data AS
    SELECT id, code, dsty_id, dast_id, expe_id, expe_frozen, data_producer_code, production_timestamp, samp_id,
           samp_frozen, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid,
           modification_timestamp,is_derived, del_id, orig_del, version, data_set_kind,
           frozen, frozen_for_children, frozen_for_parents, frozen_for_comps, frozen_for_conts, tsvector_document
    FROM data_all
    WHERE del_id IS NULL;

CREATE OR REPLACE VIEW data_deleted AS
    SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id,
           registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid,
           modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind, tsvector_document
    FROM data_all
    WHERE del_id IS NOT NULL;

CREATE TRIGGER data_all_tsvector_document BEFORE INSERT OR UPDATE
    ON data_all FOR EACH ROW EXECUTE FUNCTION
    data_all_tsvector_document_trigger();

UPDATE data_all SET code = code;

ALTER TABLE data_set_properties
    ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER data_set_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON data_set_properties FOR EACH ROW EXECUTE FUNCTION
    properties_tsvector_document_trigger();

UPDATE data_set_properties SET value = value;

ALTER TABLE data_set_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX data_set_properties_search_index ON data_set_properties USING gin(tsvector_document);

-- Materials

CREATE FUNCTION materials_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE material_type_code VARCHAR;
BEGIN
    SELECT code INTO STRICT material_type_code FROM material_types WHERE id = NEW.maty_id;
    NEW.tsvector_document := (NEW.code || ':1')::tsvector
        || (NEW.code || '\ (' || material_type_code || '):1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE materials ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER materials_tsvector_document BEFORE INSERT OR UPDATE
    ON materials FOR EACH ROW EXECUTE PROCEDURE
    materials_tsvector_document_trigger();

UPDATE materials SET code = code;

ALTER TABLE material_properties ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER material_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON material_properties FOR EACH ROW EXECUTE FUNCTION
    properties_tsvector_document_trigger();

UPDATE material_properties SET value = value;

ALTER TABLE material_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX material_properties_search_index ON material_properties USING gin(tsvector_document);