-- Full text search

-- Controlled Vocabularies

ALTER TABLE controlled_vocabulary_terms
    ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER controlled_vocabulary_terms_tsvector_document BEFORE INSERT OR UPDATE
    ON controlled_vocabulary_terms FOR EACH ROW EXECUTE PROCEDURE
    tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', code, label, description);

UPDATE controlled_vocabulary_terms SET code = code;

CREATE INDEX controlled_vocabulary_terms_search_index ON controlled_vocabulary_terms USING gin(tsvector_document);

-- Samples

ALTER TABLE sample_properties
    ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER sample_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON sample_properties FOR EACH ROW EXECUTE FUNCTION
    tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', value);

UPDATE samples_all SET value = value;

ALTER TABLE sample_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX sample_properties_search_index ON sample_properties USING gin(tsvector_document);

BEGIN;
    ALTER TABLE sample_properties
        ADD COLUMN tsvector_document TSVECTOR;

    CREATE TRIGGER sample_properties_tsvector_document BEFORE INSERT OR UPDATE
        ON sample_properties FOR EACH ROW EXECUTE PROCEDURE
        tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', value);

    UPDATE sample_properties SET value = value;
COMMIT;

-- Experiments

BEGIN;
    CREATE FUNCTION experiments_all_tsvector_document_trigger() RETURNS trigger AS $$
    begin
        new.tsvector_document :=
                    (concat(new.perm_id, ':1'))::tsvector ||
                    to_tsvector('pg_catalog.simple', coalesce(new.code,''));
        return new;
    end
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
COMMIT;

BEGIN;
    ALTER TABLE experiments_all
        ALTER COLUMN tsvector_document SET NOT NULL;

    CREATE INDEX experiments_all_search_index ON experiments_all USING gin(tsvector_document);
COMMIT;

-- Data sets

BEGIN;
    ALTER TABLE data_all
        ADD COLUMN tsvector_document TSVECTOR;

    CREATE OR REPLACE VIEW data AS
        SELECT id, code, dsty_id, dast_id, expe_id, expe_frozen, data_producer_code, production_timestamp, samp_id, samp_frozen,
               registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp,
               is_derived, del_id, orig_del, version, data_set_kind,
               frozen, frozen_for_children, frozen_for_parents, frozen_for_comps, frozen_for_conts, tsvector_document
        FROM data_all
        WHERE del_id IS NULL;

    CREATE OR REPLACE VIEW data_deleted AS
        SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid,
               modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind, tsvector_document
        FROM data_all
        WHERE del_id IS NOT NULL;

    CREATE TRIGGER data_all_tsvector_document BEFORE INSERT OR UPDATE
        ON data_all FOR EACH ROW EXECUTE PROCEDURE
        tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', data_set_kind, code);

    UPDATE data_all SET code = code;
COMMIT;

BEGIN;
    ALTER TABLE data_all
        ALTER COLUMN tsvector_document SET NOT NULL;

    CREATE INDEX data_all_search_index ON data_all USING gin(tsvector_document);
COMMIT;

-- Materials

BEGIN;
    ALTER TABLE materials ADD COLUMN tsvector_document TSVECTOR;

    CREATE TRIGGER materials_tsvector_document BEFORE INSERT OR UPDATE
        ON materials FOR EACH ROW EXECUTE PROCEDURE
        tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', code);

    UPDATE materials SET code = code;
COMMIT;

BEGIN;
    ALTER TABLE materials
        ALTER COLUMN tsvector_document SET NOT NULL;

    CREATE INDEX materials_search_index ON materials USING gin(tsvector_document);
COMMIT;