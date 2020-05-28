-- Full text search

-- Controlled Vocabularies

ALTER TABLE controlled_vocabulary_terms
    ADD COLUMN tsvector_document TSVECTOR;

CREATE FUNCTION properties_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE cvt RECORD;
BEGIN
    IF NEW.cvte_id IS NOT NULL THEN
        SELECT code, label INTO STRICT cvt FROM controlled_vocabulary_terms WHERE id = NEW.cvte_id;
        NEW.tsvector_document := to_tsvector('pg_catalog.simple', cvt.code) ||
                to_tsvector('pg_catalog.simple', coalesce(cvt.label, ''));
    ELSE
        NEW.tsvector_document := to_tsvector('pg_catalog.simple', coalesce(NEW.value, ''));
        RETURN NEW;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Samples

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

ALTER TABLE material_properties ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER material_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON material_properties FOR EACH ROW EXECUTE FUNCTION
    properties_tsvector_document_trigger();

UPDATE material_properties SET value = value;

ALTER TABLE material_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX material_properties_search_index ON material_properties USING gin(tsvector_document);