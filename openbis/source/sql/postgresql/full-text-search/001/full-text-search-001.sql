CREATE OR REPLACE FUNCTION escape_tsvector_string(value VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN REPLACE(
            REPLACE(
                    REPLACE(
                            REPLACE(
                                    REPLACE(
                                            REPLACE(
                                                    REPLACE(
                                                            REPLACE(
                                                                    REPLACE(value, '<', '\<'),
                                                                    '!', '\!'),
                                                            '*', '\*'),
                                                    '&', '\&'),
                                            '|', '\|'),
                                    ')', '\)'),
                            '(', '\('),
                    ':', '\:'),
            ' ', '\ ');
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION properties_tsvector_document_trigger() RETURNS trigger AS $$
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

CREATE OR REPLACE FUNCTION samples_all_tsvector_document_trigger() RETURNS trigger AS $$
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

    IF NEW.samp_id_part_of IS NOT NULL THEN
        SELECT code INTO STRICT container_code FROM samples_all WHERE id = NEW.samp_id_part_of;
        identifier := identifier || container_code || ':' || NEW.code;
    ELSE
        identifier := identifier || NEW.code;
    END IF;

    NEW.sample_identifier := identifier;
    NEW.tsvector_document := (escape_tsvector_string(NEW.perm_id) || ':1')::tsvector ||
            (escape_tsvector_string(NEW.code) || ':1')::tsvector ||
            (escape_tsvector_string(identifier) || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS samples_all_tsvector_document ON samples_all;
CREATE TRIGGER samples_all_tsvector_document BEFORE INSERT OR UPDATE
    ON samples_all FOR EACH ROW EXECUTE PROCEDURE
    samples_all_tsvector_document_trigger();

DROP TRIGGER IF EXISTS sample_properties_tsvector_document ON sample_properties;
CREATE TRIGGER sample_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON sample_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

-- Experiments

CREATE OR REPLACE FUNCTION experiments_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
        space_code VARCHAR;
BEGIN
    SELECT p.code, s.code INTO STRICT proj_code, space_code FROM projects p
            INNER JOIN spaces s ON p.space_id = s.id WHERE p.id = NEW.proj_id;
    NEW.tsvector_document := (escape_tsvector_string(NEW.perm_id) || ':1')::tsvector ||
            (escape_tsvector_string(NEW.code) || ':1')::tsvector ||
            (escape_tsvector_string('/' || space_code || '/' || proj_code || '/' || NEW.code) || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS experiments_all_tsvector_document ON experiments_all;
CREATE TRIGGER experiments_all_tsvector_document BEFORE INSERT OR UPDATE
    ON experiments_all FOR EACH ROW EXECUTE PROCEDURE
    experiments_all_tsvector_document_trigger();

DROP TRIGGER IF EXISTS experiment_properties_tsvector_document ON experiment_properties;
CREATE TRIGGER experiment_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON experiment_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

-- Data sets

CREATE OR REPLACE FUNCTION data_all_tsvector_document_trigger() RETURNS trigger AS $$
BEGIN
    NEW.tsvector_document := (escape_tsvector_string(NEW.data_set_kind) || ':1')::tsvector ||
            (escape_tsvector_string(NEW.code) || ':1')::tsvector ||
            ('/' || escape_tsvector_string(NEW.code) || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS data_all_tsvector_document ON data_all;
CREATE TRIGGER data_all_tsvector_document BEFORE INSERT OR UPDATE
    ON data_all FOR EACH ROW EXECUTE PROCEDURE
    data_all_tsvector_document_trigger();

DROP TRIGGER IF EXISTS data_set_properties_tsvector_document ON data_set_properties;
CREATE TRIGGER data_set_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON data_set_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

-- Materials

CREATE OR REPLACE FUNCTION materials_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE material_type_code VARCHAR;
BEGIN
    SELECT code INTO STRICT material_type_code FROM material_types WHERE id = NEW.maty_id;
    NEW.tsvector_document := (escape_tsvector_string(NEW.code) || ':1')::tsvector ||
                             (escape_tsvector_string(NEW.code || ' (' || material_type_code || ')') || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS materials_tsvector_document ON materials;
CREATE TRIGGER materials_tsvector_document BEFORE INSERT OR UPDATE
    ON materials FOR EACH ROW EXECUTE PROCEDURE
    materials_tsvector_document_trigger();

DROP TRIGGER IF EXISTS material_properties_tsvector_document ON material_properties;
CREATE TRIGGER material_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON material_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

-- Updating tables

UPDATE samples_all SET id = id;
UPDATE sample_properties SET id = id;
UPDATE experiments_all SET id = id;
UPDATE experiment_properties SET id = id;
UPDATE data_all SET id = id;
UPDATE data_set_properties SET id = id;
UPDATE materials SET id = id;
UPDATE material_properties SET id = id;
UPDATE projects SET id = id;
