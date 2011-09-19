-- Granting SELECT privilege to group OPENBIS_READONLY

GRANT SELECT ON SEQUENCE attachment_content_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE attachment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE code_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE controlled_vocabulary_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE cvte_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_relationship_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_store_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE DATA_STORE_SERVICES_ID_SEQ TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE database_instance_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE dstpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE etpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE event_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE file_format_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE space_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE deletion_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE locator_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE mtpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE perm_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE person_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE project_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE property_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE role_assignment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE stpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE authorization_group_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE filter_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE query_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE attachment_contents TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE attachments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE controlled_vocabularies TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE controlled_vocabulary_terms TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_all TO GROUP OPENBIS_READONLY;
GRANT SELECT ON data TO GROUP OPENBIS_READONLY;
GRANT SELECT ON data_deleted TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON data_set_relationships TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_relationships_all TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_stores TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE DATA_STORE_SERVICES TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE DATA_STORE_SERVICE_DATA_SET_TYPES TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE database_instances TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE database_version_logs TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE events TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiments_all TO GROUP OPENBIS_READONLY;
GRANT SELECT ON experiments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON experiments_deleted TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE external_data TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE file_format_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE spaces TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE deletions TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE locator_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE materials TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE persons TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE projects TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE role_assignments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE samples_all TO GROUP OPENBIS_READONLY;
GRANT SELECT ON samples TO GROUP OPENBIS_READONLY;
GRANT SELECT ON samples_deleted TO GROUP OPENBIS_READONLY;
GRANT SELECT ON sample_relationships TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_relationships_all TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE authorization_groups TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE authorization_group_persons TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE filters TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE queries TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE scripts TO GROUP OPENBIS_READONLY;
