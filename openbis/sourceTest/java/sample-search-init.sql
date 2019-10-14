-- Persons

INSERT INTO persons(is_active, user_id, last_name, id, first_name, pers_id_registerer, email)
VALUES (true, 'jbrown', 'Brown', 101, 'John', 2, 'jbrown@example.com');

INSERT INTO persons(is_active, user_id, last_name, id, first_name, pers_id_registerer, email)
VALUES (true, 'jblack', 'Black', 102, 'Jim', 2, 'jblack@example.com');

-- Spaces

INSERT INTO spaces(registration_timestamp, code, frozen_for_proj, description, frozen, id, pers_id_registerer, frozen_for_samp)
VALUES (TIMESTAMP '2019-06-10 10:50:00+02', 'MY_SPACE_UNIQUE_CODE1', false, null, false, 10000, 2, false);

INSERT INTO spaces(registration_timestamp, code, frozen_for_proj, description, frozen, id, pers_id_registerer, frozen_for_samp)
VALUES (TIMESTAMP '2019-06-10 10:50:00+02', 'MY_SPACE_UNIQUE_CODE2', false, null, false, 10001, 101, false);


INSERT INTO projects(registration_timestamp, code, perm_id, description, frozen, frozen_for_exp, version, space_frozen, pers_id_modifier, modification_timestamp, pers_id_leader, id, space_id, pers_id_registerer, frozen_for_samp)
VALUES (TIMESTAMP '2019-06-10 10:50:00+02', 'PROJECT_CODE', '20190301152050019-11', null, false, false, 10, false, null, TIMESTAMP '2019-06-10 10:50:00+02', null, 10002, 10000, 2, false);

INSERT INTO experiment_types(code, modification_timestamp, validation_script_id, description, id)
VALUES ('DEFAULT_UNIQUE_CODE', TIMESTAMP '2019-06-10 10:50:00+02', null, null, 10004);

INSERT INTO experiments_all(orig_del, registration_timestamp, code, perm_id, proj_frozen, del_id, frozen, frozen_for_data, proj_id, version, pers_id_modifier, exty_id, modification_timestamp, is_public, id, pers_id_registerer, frozen_for_samp)
VALUES (null, TIMESTAMP '2019-06-10 10:50:00+02', 'DEFAULT_UNIQUE_CODE', '20190612105000000-0', false, null, false, false, 10002, 10, null, 10004, TIMESTAMP '2019-06-10 10:50:00+02', false, 10003, 2, false);

-- Samples

INSERT INTO samples_all(orig_del, registration_timestamp, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id, frozen_for_data, samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier, modification_timestamp, id, pers_id_registerer, space_id, cont_frozen)
VALUES (null, TIMESTAMP '2019-06-11 10:50:00+02', false, 'MY_UNIQUE_CODE1', null, false, '20190612105000000-1001', null, false, 1, false, null, 101, null, false, false, false, false, 2, TIMESTAMP '2018-06-11 10:50:00+02', 1001, 2, 10000, false);

INSERT INTO samples_all(orig_del, registration_timestamp, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id, frozen_for_data, samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier, modification_timestamp, id, pers_id_registerer, space_id, cont_frozen)
VALUES (null, TIMESTAMP '2019-06-12 10:50:00+02', false, 'ANOTHER_UNIQUE_CODE2', null, false, '20190612105000001-1001', null, false, 1, false, 1001, 102, 10002, false, false, false, false, null, TIMESTAMP '2018-06-12 10:50:00+02', 1002, 101, 10001, false);

INSERT INTO samples_all(orig_del, frozen_for_children, code, expe_id, proj_frozen, perm_id, del_id, frozen, saty_id, frozen_for_data, samp_id_part_of, version, proj_id, space_frozen, frozen_for_comp, frozen_for_parents, expe_frozen, pers_id_modifier, modification_timestamp, id, pers_id_registerer, space_id, cont_frozen)
VALUES (null, false, 'ANOTHER_UNIQUE_CODE3', 10003, false, '20190612105000000-1003', null, false, 1, false, 1001, 103, 10002, false, false, false, false, 102, TIMESTAMP '2018-06-13 10:50:00+02', 1003, 1, null, false);

-- Tables related to samples

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