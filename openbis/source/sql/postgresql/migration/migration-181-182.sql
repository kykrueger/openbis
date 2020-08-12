/*
 * Copyright 2016 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

DROP FUNCTION IF EXISTS escape_tsvector_string(VARCHAR) RESTRICT;

CREATE FUNCTION escape_tsvector_string(value VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN REPLACE(
            REPLACE(
                    REPLACE(
                            REPLACE(
                                    REPLACE(
                                            REPLACE(
                                                    REPLACE(
                                                            REPLACE(
                                                                    REPLACE(LOWER(value), '<', '\<'),
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

DROP FUNCTION IF EXISTS properties_tsvector_document_trigger() CASCADE;

CREATE FUNCTION properties_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE cvt RECORD;
BEGIN
    IF NEW.cvte_id IS NOT NULL THEN
        SELECT code, label INTO STRICT cvt FROM controlled_vocabulary_terms WHERE id = NEW.cvte_id;
        NEW.tsvector_document := to_tsvector('english', LOWER(cvt.code)) ||
                                 to_tsvector('english', coalesce(LOWER(cvt.label), ''));
    ELSE
        NEW.tsvector_document := to_tsvector('english', coalesce(LOWER(NEW.value), ''));
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- ALTER TABLE sample_properties
--     ALTER COLUMN tsvector_document DROP NOT NULL;
--
-- UPDATE sample_properties SET tsvector_document = NULL;

CREATE TRIGGER sample_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON sample_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

UPDATE sample_properties SET value = value;

-- ALTER TABLE sample_properties
--     ALTER COLUMN tsvector_document SET NOT NULL;

CREATE TRIGGER experiment_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON experiment_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

UPDATE experiment_properties SET value = value;

CREATE TRIGGER data_set_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON data_set_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

UPDATE data_set_properties SET value = value;

CREATE TRIGGER material_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON material_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

UPDATE material_properties SET value = value;

UPDATE samples_all SET code = code;
UPDATE experiments_all SET code = code;
UPDATE data_all SET code = code;
UPDATE materials SET code = code;

ALTER TABLE persons
    DROP CONSTRAINT pers_space_fk;

ALTER TABLE persons
    ADD CONSTRAINT pers_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE SET NULL;