-- Full text search

-- Samples

BEGIN;
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
        ON samples_all FOR EACH ROW EXECUTE PROCEDURE
        tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', perm_id, code);

    UPDATE samples_all SET code = code;
COMMIT;

BEGIN;
    ALTER TABLE samples_all
        ALTER COLUMN tsvector_document SET NOT NULL;

    CREATE INDEX samples_all_search_index ON samples_all USING gin(tsvector_document);
COMMIT;

-- Experiments

BEGIN;
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
        ON experiments_all FOR EACH ROW EXECUTE PROCEDURE
        tsvector_update_trigger(tsvector_document, 'pg_catalog.simple', perm_id, code);

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

-- DROP VIEW data_deleted;
--
-- DROP VIEW data_set_history_view;
--
-- DROP VIEW experiment_history_view;
--
-- DROP VIEW experiments_deleted;
--
-- DROP VIEW samples_deleted;
--
-- ALTER TABLE data_set_properties_history
--     DROP CONSTRAINT dsprh_ck;
--
-- ALTER TABLE data_set_properties
--     DROP CONSTRAINT dspr_ck;
--
-- ALTER TABLE experiment_properties_history
--     DROP CONSTRAINT exprh_ck;
--
-- ALTER TABLE experiment_properties
--     DROP CONSTRAINT expr_ck;
--
-- ALTER TABLE data_set_properties_history
--     ADD COLUMN sample public.identifier;
--
-- ALTER TABLE data_set_properties
--     ADD COLUMN samp_prop_id public.tech_id;
--
-- ALTER TABLE experiment_properties_history
--     ADD COLUMN sample public.identifier;
--
-- ALTER TABLE experiment_properties
--     ADD COLUMN samp_prop_id public.tech_id;
--
-- ALTER TABLE data_set_properties_history
--     ADD CONSTRAINT dsprh_ck CHECK ((((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL))
--                                         OR ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL) AND (sample IS NULL))
--                                         OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL) AND (sample IS NULL))
--                                         OR ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NOT NULL))));
--
-- ALTER TABLE data_set_properties
--     ADD CONSTRAINT dspr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL)) OR
--                                    ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL)) OR
--                                    ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL) AND (samp_prop_id IS NULL)) OR
--                                    ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NOT NULL))));
--
-- ALTER TABLE experiment_properties_history
--     ADD CONSTRAINT exprh_ck CHECK ((((value IS NOT NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NULL)) OR
--                                     ((value IS NULL) AND (vocabulary_term IS NOT NULL) AND (material IS NULL) AND (sample IS NULL)) OR
--                                     ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NOT NULL) AND (sample IS NULL)) OR
--                                     ((value IS NULL) AND (vocabulary_term IS NULL) AND (material IS NULL) AND (sample IS NOT NULL))));
--
-- ALTER TABLE experiment_properties
--     ADD CONSTRAINT expr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL)) OR
--                                    ((value IS NULL) AND (cvte_id IS NOT NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NULL)) OR
--                                    ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NOT NULL) AND (samp_prop_id IS NULL)) OR
--                                    ((value IS NULL) AND (cvte_id IS NULL) AND (mate_prop_id IS NULL) AND (samp_prop_id IS NOT NULL))));
--
-- CREATE INDEX dspr_sapr_fk_i
--     ON data_set_properties USING btree (samp_prop_id);
--
-- CREATE INDEX expr_sapr_fk_i
--     ON experiment_properties USING btree (samp_prop_id);
--
-- CREATE VIEW data_deleted AS
--     SELECT data_all.id, data_all.code, data_all.dsty_id, data_all.dast_id, data_all.expe_id, data_all.data_producer_code,
--            data_all.production_timestamp, data_all.samp_id, data_all.registration_timestamp, data_all.access_timestamp, data_all.pers_id_registerer,
--            data_all.pers_id_modifier, data_all.is_valid, data_all.modification_timestamp, data_all.is_derived, data_all.del_id, data_all.orig_del,
--            data_all.version, data_all.data_set_kind, data_all.tsvector_document FROM public.data_all WHERE (data_all.del_id IS NOT NULL);
--
-- CREATE VIEW data_set_history_view AS
--     SELECT (3 * (data_set_relationships_history.id)::bigint) AS id, data_set_relationships_history.main_data_id,
--            data_set_relationships_history.relation_type, data_set_relationships_history.ordinal, data_set_relationships_history.expe_id,
--            data_set_relationships_history.samp_id, data_set_relationships_history.data_id, data_set_relationships_history.entity_perm_id,
--            NULL::bigint AS dstpt_id, NULL::text AS value, NULL::character varying AS vocabulary_term, NULL::character varying AS material,
--            NULL::character varying AS sample, NULL::text AS external_code, NULL::text AS path, NULL::text AS git_commit_hash,
--            NULL::text AS git_repository_id, (NULL::bigint)::public.tech_id AS edms_id, NULL::text AS edms_code, NULL::text AS edms_label,
--            NULL::text AS edms_address, data_set_relationships_history.pers_id_author, data_set_relationships_history.valid_from_timestamp,
--            data_set_relationships_history.valid_until_timestamp
--     FROM public.data_set_relationships_history
--     WHERE (data_set_relationships_history.valid_until_timestamp IS NOT NULL)
--     UNION
--     SELECT ((3 * (data_set_properties_history.id)::bigint) + 1) AS id, data_set_properties_history.ds_id AS main_data_id, NULL::text AS relation_type,
--            NULL::integer AS ordinal, NULL::bigint AS expe_id, NULL::bigint AS samp_id, NULL::bigint AS data_id, NULL::text AS entity_perm_id,
--            data_set_properties_history.dstpt_id, data_set_properties_history.value, data_set_properties_history.vocabulary_term,
--            data_set_properties_history.material, data_set_properties_history.sample, NULL::text AS external_code, NULL::text AS path,
--            NULL::text AS git_commit_hash, NULL::text AS git_repository_id, NULL::bigint AS edms_id, NULL::text AS edms_code, NULL::text AS edms_label,
--            NULL::text AS edms_address, data_set_properties_history.pers_id_author, data_set_properties_history.valid_from_timestamp,
--            data_set_properties_history.valid_until_timestamp
--     FROM public.data_set_properties_history
--     UNION
--     SELECT ((3 * (data_set_copies_history.id)::bigint) + 2) AS id, data_set_copies_history.data_id AS main_data_id, NULL::text AS relation_type,
--            NULL::integer AS ordinal, NULL::bigint AS expe_id, NULL::bigint AS samp_id, NULL::bigint AS data_id, NULL::text AS entity_perm_id,
--            NULL::bigint AS dstpt_id, NULL::text AS value, NULL::character varying AS vocabulary_term, NULL::character varying AS material,
--            NULL::character varying AS sample, data_set_copies_history.external_code, data_set_copies_history.path,
--            data_set_copies_history.git_commit_hash, data_set_copies_history.git_repository_id, data_set_copies_history.edms_id,
--            data_set_copies_history.edms_code, data_set_copies_history.edms_label, data_set_copies_history.edms_address,
--            data_set_copies_history.pers_id_author, data_set_copies_history.valid_from_timestamp, data_set_copies_history.valid_until_timestamp
--     FROM public.data_set_copies_history
--     WHERE (data_set_copies_history.valid_until_timestamp IS NOT NULL);
--
-- CREATE VIEW experiment_history_view AS
-- SELECT (2 * (experiment_relationships_history.id)::bigint) AS id, experiment_relationships_history.main_expe_id,
--        experiment_relationships_history.relation_type, experiment_relationships_history.proj_id, experiment_relationships_history.samp_id,
--        experiment_relationships_history.data_id, experiment_relationships_history.entity_perm_id, NULL::bigint AS etpt_id, NULL::text AS value,
--        NULL::character varying AS vocabulary_term, NULL::character varying AS material, NULL::character varying AS sample,
--        experiment_relationships_history.pers_id_author, experiment_relationships_history.valid_from_timestamp,
--        experiment_relationships_history.valid_until_timestamp FROM public.experiment_relationships_history
-- WHERE (experiment_relationships_history.valid_until_timestamp IS NOT NULL)
-- UNION
-- SELECT ((2 * (experiment_properties_history.id)::bigint) + 1) AS id, experiment_properties_history.expe_id AS main_expe_id,
--        NULL::text AS relation_type, NULL::bigint AS proj_id, NULL::bigint AS samp_id, NULL::bigint AS data_id, NULL::text AS entity_perm_id,
--        experiment_properties_history.etpt_id, experiment_properties_history.value, experiment_properties_history.vocabulary_term,
--        experiment_properties_history.material, experiment_properties_history.sample, experiment_properties_history.pers_id_author,
--        experiment_properties_history.valid_from_timestamp, experiment_properties_history.valid_until_timestamp
-- FROM public.experiment_properties_history;
--
-- CREATE VIEW experiments_deleted AS
-- SELECT experiments_all.id, experiments_all.perm_id, experiments_all.code, experiments_all.exty_id, experiments_all.pers_id_registerer,
--        experiments_all.pers_id_modifier, experiments_all.registration_timestamp, experiments_all.modification_timestamp,
--        experiments_all.proj_id, experiments_all.del_id, experiments_all.orig_del, experiments_all.is_public, experiments_all.version,
--        experiments_all.tsvector_document FROM public.experiments_all
-- WHERE (experiments_all.del_id IS NOT NULL);
--
-- CREATE VIEW samples_deleted AS
-- SELECT samples_all.id, samples_all.perm_id, samples_all.code, samples_all.expe_id, samples_all.saty_id,
--        samples_all.registration_timestamp, samples_all.modification_timestamp, samples_all.pers_id_registerer,
--        samples_all.pers_id_modifier, samples_all.del_id, samples_all.orig_del, samples_all.space_id, samples_all.proj_id,
--        samples_all.samp_id_part_of, samples_all.version, samples_all.tsvector_document
-- FROM public.samples_all
-- WHERE (samples_all.del_id IS NOT NULL);