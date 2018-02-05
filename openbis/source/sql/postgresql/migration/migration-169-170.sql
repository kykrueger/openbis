-- Migration from 169 to 170

CREATE OR REPLACE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind, id as data_id 
       FROM data_all 
      WHERE del_id IS NULL;

      
