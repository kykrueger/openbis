-- Persons

INSERT INTO persons(id, is_active, user_id, last_name, first_name, pers_id_registerer, email)
VALUES (101, true, 'jbrown', 'Brown', 'John', 2, 'jbrown@example.com');

INSERT INTO persons(id, is_active, user_id, last_name, first_name, pers_id_registerer, email)
VALUES (102, true, 'jblack', 'Black', 'Jim', 2, 'jblack@example.com');

-- Spaces

INSERT INTO spaces(id, registration_timestamp, code, frozen_for_proj, description, frozen, pers_id_registerer, frozen_for_samp)
VALUES (10000, TIMESTAMP '2019-06-10 10:50:00+02', 'MY_SPACE_UNIQUE_CODE_1', false, null, false, 2, false);

INSERT INTO spaces(id, registration_timestamp, code, frozen_for_proj, description, frozen, pers_id_registerer, frozen_for_samp)
VALUES (10001, TIMESTAMP '2019-06-10 10:51:00+02', 'MY_SPACE_UNIQUE_CODE_2', false, null, false, 101, false);

INSERT INTO spaces(id, registration_timestamp, code, frozen_for_proj, description, frozen, pers_id_registerer, frozen_for_samp)
VALUES (10002, TIMESTAMP '2019-06-10 10:52:00+02', 'MY_SPACE_UNIQUE_CODE_3', false, null, false, 101, false);

-- Projects

INSERT INTO projects(id, registration_timestamp, code, perm_id, description, frozen, frozen_for_exp, version, space_frozen, pers_id_modifier,
        modification_timestamp, pers_id_leader, space_id, pers_id_registerer, frozen_for_samp)
VALUES (10001, TIMESTAMP '2019-06-10 10:49:00+02', 'UNIQUE_PROJECT_CODE_FOR_SURE', '20190117152050019-11', null, false, false, 10, false, null,
        TIMESTAMP '2019-06-10 10:49:00+02', null, 10000, 2, false);

INSERT INTO projects(id, registration_timestamp, code, perm_id, description, frozen, frozen_for_exp, version, space_frozen, pers_id_modifier,
        modification_timestamp, pers_id_leader, space_id, pers_id_registerer, frozen_for_samp)
VALUES (10002, TIMESTAMP '2019-06-10 10:50:00+02', 'PROJECT_CODE_2', '20191017152050019-12', null, false, false, 10, false, null,
        TIMESTAMP '2019-06-10 10:50:00+02', null, 10001, 2, false);

INSERT INTO projects(id, registration_timestamp, code, perm_id, description, frozen, frozen_for_exp, version, space_frozen, pers_id_modifier,
        modification_timestamp, pers_id_leader, space_id, pers_id_registerer, frozen_for_samp)
VALUES (10003, TIMESTAMP '2019-06-10 10:51:00+02', 'PROJECT_CODE_3', '20191017152050019-13', null, false, false, 10, false, 102,
        TIMESTAMP '2019-06-10 10:51:00+02', null, 10002, 2, false);

-- Experiment types

INSERT INTO experiment_types(id, code, modification_timestamp, validation_script_id, description)
VALUES (1001, 'DEFAULT_UNIQUE_CODE', TIMESTAMP '2019-06-10 10:50:00+02', null, null);

INSERT INTO experiment_types(id, code, modification_timestamp, validation_script_id, description)
VALUES (1002, 'EXPERIMENT.TYPE.2', TIMESTAMP '2019-06-10 10:50:00+02', null, null);

INSERT INTO experiment_types(id, code, modification_timestamp, validation_script_id, description)
VALUES (1003, 'EXPERIMENT.TYPE.3', TIMESTAMP '2019-06-10 10:50:00+02', null, null);

INSERT INTO experiment_types(id, code, modification_timestamp, validation_script_id, description)
VALUES (1004, 'EXPERIMENT.TYPE.4', TIMESTAMP '2019-06-10 10:50:00+02', null, null);

-- Experiments

INSERT INTO experiments_all(id, perm_id, orig_del, code, exty_id, del_id, proj_id, pers_id_registerer, pers_id_modifier, registration_timestamp,
        modification_timestamp)
VALUES (1001, '20191015134000000-1001', null, 'UNIQUE_EXPERIMENT_CODE_1', 1001, null, 10001, 2, 2, TIMESTAMP '2019-07-11 10:50:00+02',
        TIMESTAMP '2018-07-11 10:50:00+02');

INSERT INTO experiments_all(id, perm_id, orig_del, code, exty_id, del_id, proj_id, pers_id_registerer, pers_id_modifier, registration_timestamp,
        modification_timestamp)
VALUES (1002, '20191015134000001-1001', null, 'EXPERIMENT_CODE_2', 1001, null, 10002, 101, null, TIMESTAMP '2019-07-12 10:50:00+02',
        TIMESTAMP '2018-07-12 10:50:00+02');

INSERT INTO experiments_all(id, perm_id, orig_del, code, exty_id, del_id, proj_id, pers_id_registerer, pers_id_modifier)
VALUES (1003, '20191015134000000-1003', null, 'EXPERIMENT_CODE_3', 1001, null, 10001, 102, 102);

INSERT INTO experiments_all(id, perm_id, orig_del, code, exty_id, del_id, proj_id, pers_id_registerer, pers_id_modifier)
VALUES (1004, '20191015134000002-1004', null, 'EXPERIMENT_CODE_4', 1002, null, 10003, 102, null);

-- Samples

INSERT INTO samples_all(id, orig_del, registration_timestamp, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id,
        frozen_for_data, samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier,
        modification_timestamp, pers_id_registerer, space_id, cont_frozen)
VALUES (1001, null, TIMESTAMP '2019-06-11 10:50:00+02', false, 'MY_UNIQUE_CODE_1', null, false, '20190612105000000-1001', null, false, 1, false, null,
        101, null, false, false, false, false, 2, TIMESTAMP '2018-06-11 10:50:00+02', 2, 10000, false);

INSERT INTO samples_all(id, orig_del, registration_timestamp, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id,
        frozen_for_data, samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier,
        modification_timestamp, pers_id_registerer, space_id, cont_frozen)
VALUES (1002, null, TIMESTAMP '2019-06-12 10:50:00+02', false, 'ANOTHER_UNIQUE_CODE_2', null, false, '20190612105000001-1001', null, false, 1, false,
        1001, 102, 10002, false, false, false, false, null, TIMESTAMP '2018-06-12 10:50:00+02', 101, 10001, false);

INSERT INTO samples_all(id, orig_del, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id, frozen_for_data,
        samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier, modification_timestamp,
        pers_id_registerer, space_id, cont_frozen)
VALUES (1003, null, false, 'ANOTHER_UNIQUE_CODE_3', 1003, false, '20190612105000000-1003', null, false, 1, false, 1001, 103, 10002, false, false,
        false, false, 102, TIMESTAMP '2018-06-13 10:50:00+02', 1, null, false);

INSERT INTO samples_all(id, orig_del, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id, frozen_for_data,
        samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier, modification_timestamp,
        pers_id_registerer, space_id, cont_frozen)
VALUES (1004, null, false, 'ANOTHER_UNIQUE_CODE_4', null, false, '20191014130100000-2004', null, false, 1, false, null, 104, 10002, false, false,
        false, false, null, TIMESTAMP '2018-06-13 10:50:00+02', 1, null, false);

INSERT INTO samples_all(id, orig_del, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id, frozen_for_data,
        samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier, modification_timestamp,
        pers_id_registerer, space_id, cont_frozen)
VALUES (1005, null, false, 'ANOTHER_UNIQUE_CODE_5', null, false, '20191014130100000-2005', null, false, 1, false, null, 105, 10002, false, false,
        false, false, null, TIMESTAMP '2018-06-13 10:50:00+02', 1, null, false);

INSERT INTO samples_all(id, orig_del, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id, frozen_for_data,
        samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier, modification_timestamp,
        pers_id_registerer, space_id, cont_frozen)
VALUES (1006, null, false, 'ANOTHER_UNIQUE_CODE_6', null, false, '20191014130100000-2006', null, false, 1, false, null, 105, 10002, false, false,
        false, false, null, TIMESTAMP '2018-06-13 10:50:00+02', 1, null, false);

-- Property tables for samples

INSERT INTO sample_types(code, id)
VALUES ('SAMPLE.TYPE.1', 3001);

INSERT INTO property_types(code, daty_id, is_internal_namespace, description, id, label, pers_id_registerer)
VALUES ('TEST.LONG', 3, false, '', 4002, 'test.long', 2);

INSERT INTO sample_type_property_types(saty_id, id, pers_id_registerer, prty_id, ordinal)
VALUES (3001, 2001, 2, 4002, 2);

INSERT INTO sample_properties(pers_id_author, stpt_id, id, value, pers_id_registerer, samp_id)
VALUES (2, 2001, 1001, 101, 2, 1001);


INSERT INTO sample_types(code, id)
VALUES ('SAMPLE.TYPE.2', 3002);

INSERT INTO property_types(code, daty_id, is_internal_namespace, description, id, label, pers_id_registerer)
VALUES ('TEST.STRING', 1, false, '', 4001, 'test.string', 2);

INSERT INTO sample_type_property_types(saty_id, id, pers_id_registerer, prty_id, ordinal)
VALUES (3002, 2002, 2, 4001, 2);

INSERT INTO sample_properties(pers_id_author, stpt_id, id, value, pers_id_registerer, samp_id)
VALUES (2, 2002, 1002, 'Test property value', 2, 1002);


INSERT INTO sample_types(code, id)
VALUES ('SAMPLE.TYPE.3', 3003);

INSERT INTO property_types(code, daty_id, is_internal_namespace, description, id, label, pers_id_registerer)
VALUES ('TEST.DOUBLE', 4, false, '', 4003, 'test.double', 2);

INSERT INTO sample_type_property_types(saty_id, id, pers_id_registerer, prty_id, ordinal)
VALUES (3003, 2003, 2, 4003, 2);

INSERT INTO sample_properties(pers_id_author, stpt_id, id, value, pers_id_registerer, samp_id)
VALUES (2, 2003, 1003, 90.25, 2, 1003);


INSERT INTO sample_types(code, id)
VALUES ('SAMPLE.TYPE.4', 3004);

INSERT INTO property_types(code, daty_id, is_internal_namespace, description, id, label, pers_id_registerer)
VALUES ('TEST.DATE', 6, false, '', 4004, 'test.date', 2);

INSERT INTO sample_type_property_types(saty_id, id, pers_id_registerer, prty_id, ordinal)
VALUES (3004, 2004, 2, 4004, 2);

INSERT INTO sample_properties(pers_id_author, stpt_id, id, value, pers_id_registerer, samp_id)
VALUES (2, 2004, 1004, TIMESTAMP '2019-09-17 15:58:00+02', 2, 1002);


INSERT INTO sample_types(code, id)
VALUES ('SAMPLE.TYPE.5', 3005);

INSERT INTO property_types(code, daty_id, is_internal_namespace, description, id, label, pers_id_registerer)
VALUES ('TEST.STRING', 1, true, '', 4005, 'test.string', 2);

INSERT INTO sample_type_property_types(saty_id, id, pers_id_registerer, prty_id, ordinal)
VALUES (3005, 2005, 2, 4005, 2);

INSERT INTO sample_properties(pers_id_author, stpt_id, id, value, pers_id_registerer, samp_id)
VALUES (2, 2005, 1005, 'Internal value', 2, 1001);

-- Sample types

INSERT INTO sample_types(IS_LISTABLE, code, id)
VALUES (true, 'MY.SAMPLE.TYPE.LISTABLE', 3101);

INSERT INTO sample_types(IS_LISTABLE, code, id)
VALUES (false, 'MY.SAMPLE.TYPE.NOT.LISTABLE', 3102);

-- Sample relationships

INSERT INTO relationship_types(id, code, label, parent_label, child_label, description, pers_id_registerer)
VALUES (10001, 'PARENT_CHILD_TEST', 'Parent - Child for testing', 'Parent', 'Child', 'Test', 101);

INSERT INTO sample_relationships_all(id, sample_id_parent, sample_id_child, relationship_id)
VALUES (10001, 1001, 1004, 10001);

INSERT INTO sample_relationships_all(id, sample_id_parent, sample_id_child, relationship_id)
VALUES (10002, 1001, 1005, 10001);

INSERT INTO sample_relationships_all(id, sample_id_parent, sample_id_child, relationship_id)
VALUES (10003, 1002, 1003, 10001);

-- Data stores

INSERT INTO data_stores(id, uuid, code, download_url, remote_url, session_token, data_source_definitions)
VALUES (101, '94D5B32C-1CCB-437E-82BD-17693496051E', 'TEST', 'http://localhost:0', 'http://127.0.0.1:0', '1234-12345',
        'driverClassName=org.postgresql.Driver');

-- Data set types

INSERT INTO data_set_types(id, code)
VALUES (1001, 'DS_TYPE_CODE_1');

INSERT INTO data_set_types(id, code)
VALUES (1002, 'DS_TYPE_CODE_2');

INSERT INTO data_set_types(id, code)
VALUES (1003, 'DS_TYPE_CODE_3');

-- Data sets

INSERT INTO data_all(id, code, dsty_id, dast_id, expe_id, samp_id, pers_id_registerer, pers_id_modifier, is_derived,
        registration_timestamp, modification_timestamp)
VALUES (1001, '10000101113999999-1001', 1001, 101, null, 1003, 101, null, true,
        TIMESTAMP '2019-08-11 10:50:00+02', TIMESTAMP '2019-08-01 10:50:00+02');

INSERT INTO data_all(id, code, dsty_id, dast_id, expe_id, samp_id, pers_id_registerer, pers_id_modifier, is_derived,
        registration_timestamp, modification_timestamp)
VALUES (1002, '20191018113900001-1002', 1002, 101, 1003, null, 2, 2, true,
        TIMESTAMP '2019-08-12 10:50:00+02', TIMESTAMP '2019-08-02 10:50:00+02');

INSERT INTO data_all(id, code, dsty_id, dast_id, expe_id, samp_id, pers_id_registerer, pers_id_modifier, is_derived,
        registration_timestamp, modification_timestamp)
VALUES (1003, '20191018113900000-1003', 1003, 101, 1003, 1003, 1, 102, true,
        TIMESTAMP '2019-08-13 10:50:00+02', TIMESTAMP '2019-08-03 10:50:00+02');

-- Property tables for data sets

INSERT INTO data_set_types(id, code)
VALUES (3001, 'DATASET.TYPE.1');

INSERT INTO property_types(id, code, daty_id, is_internal_namespace, description, label, pers_id_registerer)
VALUES (5002, 'TEST.DATASET.LONG', 3, false, '', 'test.dataset.long', 2);

INSERT INTO data_set_type_property_types(id, dsty_id, pers_id_registerer, prty_id, ordinal)
VALUES (3001, 3001, 101, 5002, 2);

INSERT INTO data_set_properties(id, pers_id_author, dstpt_id, value, pers_id_registerer, ds_id)
VALUES (2001, 101, 3001, 102, 101, 1001);


INSERT INTO data_set_types(id, code)
VALUES (3002, 'DATASET.TYPE.2');

INSERT INTO property_types(id, code, daty_id, is_internal_namespace, description, label, pers_id_registerer)
VALUES (5001, 'TEST.DATASET.STRING', 1, false, '', 'test.dataset.string', 2);

INSERT INTO data_set_type_property_types(id, dsty_id, pers_id_registerer, prty_id, ordinal)
VALUES (3002, 3002, 2, 5001, 2);

INSERT INTO data_set_properties(id, pers_id_author, dstpt_id, value, pers_id_registerer, ds_id)
VALUES (2002, 2, 3002, 'Test data set property value', 2, 1002);


INSERT INTO data_set_types(id, code)
VALUES (3003, 'DATASET.TYPE.3');

INSERT INTO property_types(id, code, daty_id, is_internal_namespace, description, label, pers_id_registerer)
VALUES (5003, 'TEST.DATASET.DOUBLE', 4, false, '', 'test.dataset.double', 2);

INSERT INTO data_set_type_property_types(id, dsty_id, pers_id_registerer, prty_id, ordinal)
VALUES (3003, 3003, 2, 5003, 2);

INSERT INTO data_set_properties(id, pers_id_author, dstpt_id, value, pers_id_registerer, ds_id)
VALUES (2003, 2, 3003, 90.15, 2, 1003);


INSERT INTO data_set_types(id, code)
VALUES (3004, 'DATASET.TYPE.4');

INSERT INTO property_types(id, code, daty_id, is_internal_namespace, description, label, pers_id_registerer)
VALUES (5004, 'TEST.DATASET.DATE', 6, false, '', 'test.dataset.date', 2);

INSERT INTO data_set_type_property_types(id, dsty_id, pers_id_registerer, prty_id, ordinal)
VALUES (3004, 3004, 2, 5004, 2);

INSERT INTO data_set_properties(id, pers_id_author, dstpt_id, value, pers_id_registerer, ds_id)
VALUES (2004, 2, 3004, TIMESTAMP '2019-10-17 15:58:00+02', 2, 1002);


INSERT INTO data_set_types(id, code)
VALUES (3005, 'DATASET.TYPE.5');

INSERT INTO property_types(id, code, daty_id, is_internal_namespace, description, label, pers_id_registerer)
VALUES (5005, 'TEST.DATASET.STRING', 1, true, '', 'test.dataset.string', 2);

INSERT INTO data_set_type_property_types(id, dsty_id, pers_id_registerer, prty_id, ordinal)
VALUES (3005, 3005, 2, 5005, 2);

INSERT INTO data_set_properties(id, pers_id_author, dstpt_id, value, pers_id_registerer, ds_id)
VALUES (2005, 2, 3005, 'Internal data set value', 2, 1001);