-- Full text search

-- Samples

ALTER TABLE samples_all ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER samples_all_tsvector_document BEFORE INSERT OR UPDATE
    ON samples_all FOR EACH ROW EXECUTE PROCEDURE
    tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', perm_id, code);

UPDATE samples_all SET code = code;

CREATE INDEX samples_all_search_index ON samples_all USING gin(tsvector_document);

-- Experiments

ALTER TABLE experiments_all ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER experiments_all_tsvector_document BEFORE INSERT OR UPDATE
    ON experiments_all FOR EACH ROW EXECUTE PROCEDURE
    tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', perm_id, code);

UPDATE experiments_all SET code = code;

CREATE INDEX experiments_all_search_index ON experiments_all USING gin(tsvector_document);

-- Data sets

ALTER TABLE data_all ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER data_all_tsvector_document BEFORE INSERT OR UPDATE
    ON data_all FOR EACH ROW EXECUTE PROCEDURE
    tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', data_set_kind, code);

UPDATE data_all SET code = code;

CREATE INDEX data_all_search_index ON data_all USING gin(tsvector_document);

-- Materials

ALTER TABLE materials ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER materials_tsvector_document BEFORE INSERT OR UPDATE
    ON materials FOR EACH ROW EXECUTE PROCEDURE
    tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', code);

UPDATE materials SET code = code;

CREATE INDEX materials_search_index ON materials USING gin(tsvector_document);