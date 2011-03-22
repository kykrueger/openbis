--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: -
--

CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

--
-- Name: archiving_status; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN archiving_status AS character varying(100)
	CONSTRAINT archiving_status_check CHECK (((VALUE)::text = ANY ((ARRAY['LOCKED'::character varying, 'AVAILABLE'::character varying, 'ARCHIVED'::character varying, 'ARCHIVE_PENDING'::character varying, 'UNARCHIVE_PENDING'::character varying, 'BACKUP_PENDING'::character varying])::text[])));


--
-- Name: authorization_role; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN authorization_role AS character varying(40)
	CONSTRAINT authorization_role_check CHECK (((VALUE)::text = ANY (ARRAY[('ADMIN'::character varying)::text, ('POWER_USER'::character varying)::text, ('USER'::character varying)::text, ('OBSERVER'::character varying)::text, ('ETL_SERVER'::character varying)::text])));


--
-- Name: boolean_char; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char AS boolean DEFAULT false;


--
-- Name: boolean_char_or_unknown; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char_or_unknown AS character(1) DEFAULT 'U'::bpchar
	CONSTRAINT boolean_char_or_unknown_check CHECK ((VALUE = ANY (ARRAY['F'::bpchar, 'T'::bpchar, 'U'::bpchar])));


--
-- Name: code; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN code AS character varying(60);


--
-- Name: column_label; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN column_label AS character varying(128);


--
-- Name: data_store_service_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN data_store_service_kind AS character varying(40)
	CONSTRAINT data_store_service_kind_check CHECK (((VALUE)::text = ANY (ARRAY[('PROCESSING'::character varying)::text, ('QUERIES'::character varying)::text])));


--
-- Name: data_store_service_reporting_plugin_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN data_store_service_reporting_plugin_type AS character varying(40)
	CONSTRAINT data_store_service_reporting_plugin_type_check CHECK (((VALUE)::text = ANY (ARRAY[('TABLE_MODEL'::character varying)::text, ('DSS_LINK'::character varying)::text])));


--
-- Name: description_2000; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_2000 AS character varying(2000);


--
-- Name: entity_kind; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN entity_kind AS character varying(40)
	CONSTRAINT entity_kind_check CHECK (((VALUE)::text = ANY ((ARRAY['SAMPLE'::character varying, 'EXPERIMENT'::character varying, 'DATA_SET'::character varying, 'MATERIAL'::character varying])::text[])));


--
-- Name: event_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN event_type AS character varying(40)
	CONSTRAINT event_type_check CHECK (((VALUE)::text = ANY (ARRAY[('DELETION'::character varying)::text, ('INVALIDATION'::character varying)::text, ('MOVEMENT'::character varying)::text])));


--
-- Name: file; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file AS bytea;


--
-- Name: file_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file_name AS character varying(100);


--
-- Name: grid_expression; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN grid_expression AS character varying(2000);


--
-- Name: grid_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN grid_id AS character varying(200);


--
-- Name: object_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN object_name AS character varying(50);


--
-- Name: ordinal_int; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN ordinal_int AS bigint
	CONSTRAINT ordinal_int_check CHECK ((VALUE > 0));


--
-- Name: query_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN query_type AS character varying(40)
	CONSTRAINT query_type_check CHECK (((VALUE)::text = ANY (ARRAY[('GENERIC'::character varying)::text, ('EXPERIMENT'::character varying)::text, ('SAMPLE'::character varying)::text, ('DATA_SET'::character varying)::text, ('MATERIAL'::character varying)::text])));


--
-- Name: real_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN real_value AS real;


--
-- Name: script_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN script_type AS character varying(40)
	CONSTRAINT script_type_check CHECK (((VALUE)::text = ANY ((ARRAY['DYNAMIC_PROPERTY'::character varying, 'MANAGED_PROPERTY'::character varying])::text[])));


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN tech_id AS bigint;


--
-- Name: text_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN text_value AS text;


--
-- Name: time_stamp; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp AS timestamp with time zone;


--
-- Name: time_stamp_dfl; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp_dfl AS timestamp with time zone NOT NULL DEFAULT now();


--
-- Name: title_100; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN title_100 AS character varying(100);


--
-- Name: user_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN user_id AS character varying(50);


--
-- Name: controlled_vocabulary_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION controlled_vocabulary_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_code  CODE;
BEGIN

   select code into v_code from data_types where id = NEW.daty_id;

   -- Check if the data is of type "CONTROLLEDVOCABULARY"
   if v_code = 'CONTROLLEDVOCABULARY' then
      if NEW.covo_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;
      end if;
   end if;

   RETURN NEW;

END;
$$;


--
-- Name: data_set_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION data_set_property_with_material_data_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from data_set_type_property_types dstpt, property_types pt 
			 where NEW.dstpt_id = dstpt.id AND dstpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$;


--
-- Name: experiment_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION experiment_property_with_material_data_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from experiment_type_property_types etpt, property_types pt 
			 where NEW.etpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$;


--
-- Name: external_data_storage_format_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION external_data_storage_format_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$;


--
-- Name: material_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION material_property_with_material_data_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from material_type_property_types etpt, property_types pt 
			 where NEW.mtpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
							 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$;


--
-- Name: rename_sequence(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION rename_sequence(old_name character varying, new_name character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  CURR_SEQ_VAL   INTEGER;
BEGIN
  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);
  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;
  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;
  RETURN CURR_SEQ_VAL;
END;
$$;


--
-- Name: sample_code_uniqueness_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_code_uniqueness_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   counter  INTEGER;
BEGIN
  LOCK TABLE samples IN EXCLUSIVE MODE;
  
	  IF (NEW.samp_id_part_of is NULL) THEN
		  IF (NEW.dbin_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples 
		      where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;
        IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;
        END IF;
		  ELSIF (NEW.space_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples 
				  where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and space_id = NEW.space_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample with the same code already exists.', NEW.code;
			  END IF;
      END IF;
    ELSE
		  IF (NEW.dbin_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples 
				  where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same container already exists.', NEW.code;
			  END IF;
		  ELSIF (NEW.space_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples 
				  where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and space_id = NEW.space_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample with the same code and being the part of the same container already exists.', NEW.code;
			  END IF;
		  END IF;
     END IF;   
  
  RETURN NEW;
END;
$$;


--
-- Name: sample_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_property_with_material_data_type_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from sample_type_property_types etpt, property_types pt 
			 where NEW.stpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$;


--
-- Name: sample_subcode_uniqueness_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_subcode_uniqueness_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   counter  INTEGER;
   unique_subcode  BOOLEAN_CHAR;
BEGIN
  LOCK TABLE samples IN EXCLUSIVE MODE;
  
  SELECT is_subcode_unique into unique_subcode FROM sample_types WHERE id = NEW.saty_id;
  
  IF (unique_subcode) THEN
    IF (NEW.dbin_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and saty_id = NEW.saty_id and dbin_id = NEW.dbin_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample of the same type with the same subcode already exists.', NEW.code;
			END IF;
		ELSIF (NEW.space_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and saty_id = NEW.saty_id and space_id = NEW.space_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample of the same type with the same subcode already exists.', NEW.code;
			END IF;
		END IF;
  END IF;
  
  RETURN NEW;
END;
$$;


--
-- Name: attachment_content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE attachment_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: attachment_content_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('attachment_content_id_seq', 1, false);


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: attachment_contents; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE attachment_contents (
    id tech_id NOT NULL,
    value file NOT NULL
);


--
-- Name: attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: attachment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('attachment_id_seq', 1, false);


--
-- Name: attachments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE attachments (
    id tech_id NOT NULL,
    expe_id tech_id,
    samp_id tech_id,
    proj_id tech_id,
    exac_id tech_id NOT NULL,
    file_name file_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    version integer NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    title title_100,
    description description_2000,
    CONSTRAINT atta_arc_ck CHECK ((((((expe_id IS NOT NULL) AND (proj_id IS NULL)) AND (samp_id IS NULL)) OR (((expe_id IS NULL) AND (proj_id IS NOT NULL)) AND (samp_id IS NULL))) OR (((expe_id IS NULL) AND (proj_id IS NULL)) AND (samp_id IS NOT NULL))))
);


--
-- Name: authorization_group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE authorization_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: authorization_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('authorization_group_id_seq', 1, false);


--
-- Name: authorization_group_persons; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE authorization_group_persons (
    ag_id tech_id NOT NULL,
    pers_id tech_id NOT NULL
);


--
-- Name: authorization_groups; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE authorization_groups (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('code_seq', 1, true);


--
-- Name: controlled_vocabularies; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE controlled_vocabularies (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_chosen_from_list boolean_char DEFAULT true NOT NULL,
    source_uri character varying(250)
);


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE controlled_vocabulary_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('controlled_vocabulary_id_seq', 100, true);


--
-- Name: controlled_vocabulary_terms; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE controlled_vocabulary_terms (
    id tech_id NOT NULL,
    code object_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    covo_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    label column_label,
    description description_2000,
    ordinal ordinal_int NOT NULL,
    CONSTRAINT cvte_ck CHECK (((ordinal)::bigint > 0))
);


--
-- Name: cvte_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cvte_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cvte_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cvte_id_seq', 100, true);


--
-- Name: data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data (
    id tech_id NOT NULL,
    code code,
    dsty_id tech_id NOT NULL,
    dast_id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    data_producer_code code,
    production_timestamp time_stamp,
    samp_id tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_placeholder boolean_char DEFAULT false,
    is_valid boolean_char DEFAULT true,
    modification_timestamp time_stamp DEFAULT now(),
    is_derived boolean_char NOT NULL,
    pers_id_registerer tech_id
);


--
-- Name: data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_id_seq', 1, false);


--
-- Name: data_set_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_properties (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL,
    dstpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT dspr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: data_set_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_property_id_seq', 1, false);


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_relationship_id_seq', 1, false);


--
-- Name: data_set_relationships; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_relationships (
    data_id_parent tech_id NOT NULL,
    data_id_child tech_id NOT NULL
);


--
-- Name: data_set_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_type_id_seq', 115, true);


--
-- Name: data_set_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_type_property_types (
    id tech_id NOT NULL,
    dsty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    ordinal ordinal_int NOT NULL,
    section description_2000,
    script_id tech_id
);


--
-- Name: data_set_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    main_ds_pattern character varying(300),
    main_ds_path character varying(1000)
);


--
-- Name: data_store_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_store_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_store_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_store_id_seq', 2, true);


--
-- Name: data_store_service_data_set_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_store_service_data_set_types (
    data_store_service_id tech_id NOT NULL,
    data_set_type_id tech_id NOT NULL
);


--
-- Name: data_store_services; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_store_services (
    id tech_id NOT NULL,
    key character varying(256) NOT NULL,
    label character varying(256) NOT NULL,
    kind data_store_service_kind NOT NULL,
    data_store_id tech_id NOT NULL,
    reporting_plugin_type data_store_service_reporting_plugin_type
);


--
-- Name: data_store_services_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_store_services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_store_services_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_store_services_id_seq', 4, true);


--
-- Name: data_stores; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_stores (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code code NOT NULL,
    download_url character varying(1024) NOT NULL,
    remote_url character varying(250) NOT NULL,
    session_token character varying(50) NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_archiver_configured boolean_char DEFAULT false NOT NULL
);


--
-- Name: data_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_type_id_seq', 10, true);


--
-- Name: data_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000 NOT NULL
);


--
-- Name: database_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE database_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: database_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('database_instance_id_seq', 1, true);


--
-- Name: database_instances; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE database_instances (
    id tech_id NOT NULL,
    code code NOT NULL,
    uuid code NOT NULL,
    is_original_source boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: database_version_logs; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE database_version_logs (
    db_version character varying(4) NOT NULL,
    module_name character varying(250),
    run_status character varying(10),
    run_status_timestamp timestamp without time zone,
    module_code bytea,
    run_exception bytea
);


--
-- Name: dstpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE dstpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: dstpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('dstpt_id_seq', 44, true);


--
-- Name: etpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE etpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: etpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('etpt_id_seq', 100, true);


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('event_id_seq', 1590, true);


--
-- Name: events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE events (
    id tech_id NOT NULL,
    event_type event_type NOT NULL,
    description description_2000,
    reason description_2000,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    entity_type character varying(80) NOT NULL,
    identifier character varying(250) NOT NULL,
    CONSTRAINT evnt_et_enum_ck CHECK (((entity_type)::text = ANY ((ARRAY['ATTACHMENT'::character varying, 'DATASET'::character varying, 'EXPERIMENT'::character varying, 'SPACE'::character varying, 'MATERIAL'::character varying, 'PROJECT'::character varying, 'PROPERTY_TYPE'::character varying, 'SAMPLE'::character varying, 'VOCABULARY'::character varying, 'AUTHORIZATION_GROUP'::character varying])::text[])))
);


--
-- Name: experiment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_id_seq', 2, true);


--
-- Name: experiment_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_properties (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT expr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: experiment_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_property_id_seq', 3, true);


--
-- Name: experiment_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiment_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_type_id_seq', 100, true);


--
-- Name: experiment_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_type_property_types (
    id tech_id NOT NULL,
    exty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    ordinal ordinal_int NOT NULL,
    section description_2000,
    script_id tech_id
);


--
-- Name: experiment_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: experiments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiments (
    id tech_id NOT NULL,
    perm_id code NOT NULL,
    code code NOT NULL,
    exty_id tech_id NOT NULL,
    mate_id_study_object tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    proj_id tech_id NOT NULL,
    inva_id tech_id,
    is_public boolean_char DEFAULT false NOT NULL
);


--
-- Name: external_data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE external_data (
    data_id tech_id NOT NULL,
    location character varying(1024) NOT NULL,
    ffty_id tech_id NOT NULL,
    loty_id tech_id NOT NULL,
    cvte_id_stor_fmt tech_id NOT NULL,
    is_complete boolean_char_or_unknown DEFAULT 'U'::bpchar NOT NULL,
    cvte_id_store tech_id,
    status archiving_status DEFAULT 'AVAILABLE'::character varying NOT NULL,
    share_id code,
    size ordinal_int,
    present_in_archive boolean_char DEFAULT false
);


--
-- Name: file_format_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE file_format_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: file_format_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('file_format_type_id_seq', 101, true);


--
-- Name: file_format_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE file_format_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL
);


--
-- Name: filter_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: filter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('filter_id_seq', 101, true);


--
-- Name: filters; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE filters (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression character varying(2000) NOT NULL,
    is_public boolean NOT NULL,
    grid_id character varying(200) NOT NULL
);


--
-- Name: grid_custom_columns; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE grid_custom_columns (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code character varying(200) NOT NULL,
    label column_label NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression grid_expression NOT NULL,
    is_public boolean NOT NULL,
    grid_id grid_id NOT NULL
);


--
-- Name: grid_custom_columns_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE grid_custom_columns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: grid_custom_columns_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('grid_custom_columns_id_seq', 101, true);


--
-- Name: invalidation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE invalidation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: invalidation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('invalidation_id_seq', 1, false);


--
-- Name: invalidations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE invalidations (
    id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    reason description_2000
);


--
-- Name: locator_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE locator_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: locator_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('locator_type_id_seq', 1, true);


--
-- Name: locator_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000
);


--
-- Name: material_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_id_seq', 1588, true);


--
-- Name: material_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_properties (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    mtpt_id tech_id NOT NULL,
    value text_value,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    pers_id_registerer tech_id NOT NULL,
    cvte_id tech_id,
    mate_prop_id tech_id,
    CONSTRAINT mapr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: material_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: material_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_property_id_seq', 3176, true);


--
-- Name: material_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: material_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_type_id_seq', 100, true);


--
-- Name: material_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_type_property_types (
    id tech_id NOT NULL,
    maty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    ordinal ordinal_int NOT NULL,
    section description_2000,
    script_id tech_id
);


--
-- Name: material_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: materials; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE materials (
    id tech_id NOT NULL,
    code code NOT NULL,
    maty_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    dbin_id tech_id NOT NULL
);


--
-- Name: mtpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mtpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mtpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('mtpt_id_seq', 103, true);


--
-- Name: perm_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE perm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: perm_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('perm_id_seq', 3, true);


--
-- Name: person_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: person_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('person_id_seq', 4, true);


--
-- Name: persons; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE persons (
    id tech_id NOT NULL,
    first_name character varying(30),
    last_name character varying(30),
    user_id user_id NOT NULL,
    email object_name,
    dbin_id tech_id NOT NULL,
    space_id tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id,
    display_settings file
);


--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('project_id_seq', 1, true);


--
-- Name: projects; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projects (
    id tech_id NOT NULL,
    code code NOT NULL,
    space_id tech_id NOT NULL,
    pers_id_leader tech_id,
    description description_2000,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: property_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE property_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: property_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('property_type_id_seq', 111, true);


--
-- Name: property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE property_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000 NOT NULL,
    label column_label NOT NULL,
    daty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    covo_id tech_id,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL,
    maty_prop_id tech_id,
    schema text_value,
    transformation text_value
);


--
-- Name: queries; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE queries (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    expression character varying(2000) NOT NULL,
    is_public boolean NOT NULL,
    query_type query_type NOT NULL,
    db_key code DEFAULT '1'::character varying NOT NULL,
    entity_type_code code
);


--
-- Name: query_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE query_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: query_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('query_id_seq', 101, true);


--
-- Name: relationship_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE relationship_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: relationship_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('relationship_type_id_seq', 2, true);


--
-- Name: relationship_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE relationship_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    label column_label,
    parent_label column_label,
    child_label column_label,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL
);


--
-- Name: role_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE role_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: role_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('role_assignment_id_seq', 1, true);


--
-- Name: role_assignments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE role_assignments (
    id tech_id NOT NULL,
    role_code authorization_role NOT NULL,
    space_id tech_id,
    dbin_id tech_id,
    pers_id_grantee tech_id,
    ag_id_grantee tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    CONSTRAINT roas_ag_pers_arc_ck CHECK ((((ag_id_grantee IS NOT NULL) AND (pers_id_grantee IS NULL)) OR ((ag_id_grantee IS NULL) AND (pers_id_grantee IS NOT NULL)))),
    CONSTRAINT roas_dbin_space_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (space_id IS NULL)) OR ((dbin_id IS NULL) AND (space_id IS NOT NULL))))
);


--
-- Name: sample_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_id_seq', 1, true);


--
-- Name: sample_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_properties (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value text_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT sapr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: sample_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_property_id_seq', 1, false);


--
-- Name: sample_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_relationship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_relationship_id_seq', 1, false);


--
-- Name: sample_relationships; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_relationships (
    id tech_id NOT NULL,
    sample_id_parent tech_id NOT NULL,
    relationship_id tech_id NOT NULL,
    sample_id_child tech_id NOT NULL
);


--
-- Name: sample_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_type_id_seq', 100, true);


--
-- Name: sample_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_type_property_types (
    id tech_id NOT NULL,
    saty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_displayed boolean_char DEFAULT true NOT NULL,
    ordinal ordinal_int NOT NULL,
    section description_2000,
    script_id tech_id
);


--
-- Name: sample_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_2000,
    dbin_id tech_id NOT NULL,
    is_listable boolean_char DEFAULT true NOT NULL,
    generated_from_depth integer DEFAULT 0 NOT NULL,
    part_of_depth integer DEFAULT 0 NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_auto_generated_code boolean_char DEFAULT false NOT NULL,
    generated_code_prefix code DEFAULT 'S'::character varying NOT NULL,
    is_subcode_unique boolean_char DEFAULT false NOT NULL
);


--
-- Name: samples; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE samples (
    id tech_id NOT NULL,
    perm_id code NOT NULL,
    code code NOT NULL,
    expe_id tech_id,
    saty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    pers_id_registerer tech_id NOT NULL,
    inva_id tech_id,
    dbin_id tech_id,
    space_id tech_id,
    samp_id_part_of tech_id,
    CONSTRAINT samp_dbin_space_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (space_id IS NULL)) OR ((dbin_id IS NULL) AND (space_id IS NOT NULL))))
);


--
-- Name: script_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE script_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: script_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('script_id_seq', 1, false);


--
-- Name: scripts; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE scripts (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    name character varying(200) NOT NULL,
    description description_2000,
    script text_value NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    entity_kind entity_kind,
    script_type script_type NOT NULL
);


--
-- Name: space_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE space_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: space_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('space_id_seq', 1, true);


--
-- Name: spaces; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE spaces (
    id tech_id NOT NULL,
    code code NOT NULL,
    dbin_id tech_id NOT NULL,
    description description_2000,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);


--
-- Name: stpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE stpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: stpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('stpt_id_seq', 101, true);


--
-- Data for Name: attachment_contents; Type: TABLE DATA; Schema: public; Owner: -
--

COPY attachment_contents (id, value) FROM stdin;
\.


--
-- Data for Name: attachments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY attachments (id, expe_id, samp_id, proj_id, exac_id, file_name, registration_timestamp, version, pers_id_registerer, title, description) FROM stdin;
\.


--
-- Data for Name: authorization_group_persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY authorization_group_persons (ag_id, pers_id) FROM stdin;
\.


--
-- Data for Name: authorization_groups; Type: TABLE DATA; Schema: public; Owner: -
--

COPY authorization_groups (id, dbin_id, code, description, registration_timestamp, pers_id_registerer, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: controlled_vocabularies; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabularies (id, code, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, dbin_id, modification_timestamp, is_chosen_from_list, source_uri) FROM stdin;
1	STORAGE_FORMAT	The on-disk storage format of a data set	2010-05-10 17:57:14.310868+02	1	t	t	1	2010-05-10 17:57:14.310868+02	t	\N
2	PLATE_GEOMETRY	The geometry or dimensions of a plate	2008-06-17 16:38:30.723292+02	1	t	t	1	2009-11-27 16:02:26.451046+01	t	\N
3	MICROSCOPE	Microscope used in an experiment.	2009-11-29 23:55:18.978884+01	1	f	f	1	2009-12-17 01:50:54.68+01	t	\N
\.


--
-- Data for Name: controlled_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabulary_terms (id, code, registration_timestamp, covo_id, pers_id_registerer, label, description, ordinal) FROM stdin;
1	PROPRIETARY	2010-05-10 17:57:14.310868+02	1	1	\N	\N	1
2	BDS_DIRECTORY	2010-05-10 17:57:14.310868+02	1	1	\N	\N	2
3	384_WELLS_16X24	2008-06-17 16:38:30.723292+02	2	1	384 Wells, 16x24	\N	1
4	96_WELLS_8X12	2008-06-17 16:38:31.101031+02	2	1	96 Wells, 8x12	\N	2
5	1536_WELLS_32X48	2008-06-17 16:38:31.101031+02	2	1	1536 Wells, 32x48	\N	3
6	BD_PATHWAY_855	2009-11-29 23:55:18.978884+01	3	1	\N	\N	1
7	MD_IMAGEXPRESS_MICROLIVE	2009-11-29 23:55:18.978884+01	3	1	\N	\N	2
8	MD_IMAGEXPRESS_MICRO_2	2009-11-29 23:55:18.978884+01	3	1	\N	\N	3
\.


--
-- Data for Name: data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data (id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, is_placeholder, is_valid, modification_timestamp, is_derived, pers_id_registerer) FROM stdin;
\.


--
-- Data for Name: data_set_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_properties (id, ds_id, dstpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: data_set_relationships; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_relationships (data_id_parent, data_id_child) FROM stdin;
\.


--
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id) FROM stdin;
1	102	108	f	f	3	2011-03-21 17:26:11.110561+01	2	\N	\N
2	102	104	f	f	3	2011-03-21 17:26:11.222937+01	1	\N	\N
17	105	105	f	f	3	2011-03-21 17:26:12.139357+01	7	\N	\N
18	105	106	f	f	3	2011-03-21 17:26:12.186943+01	6	\N	\N
19	105	107	f	f	3	2011-03-21 17:26:12.238947+01	5	\N	\N
20	105	108	f	f	3	2011-03-21 17:26:12.288735+01	4	\N	\N
21	105	109	f	f	3	2011-03-21 17:26:12.326605+01	3	\N	\N
22	105	110	f	f	3	2011-03-21 17:26:12.364225+01	2	\N	\N
23	105	111	f	f	3	2011-03-21 17:26:12.402836+01	1	\N	\N
36	114	110	f	f	3	2011-03-21 17:26:12.956691+01	2	\N	\N
31	114	105	f	f	3	2011-03-21 17:26:12.752998+01	7	\N	\N
32	114	106	f	f	3	2011-03-21 17:26:12.789538+01	6	\N	\N
33	114	107	f	f	3	2011-03-21 17:26:12.826936+01	5	\N	\N
3	103	105	f	f	3	2011-03-21 17:26:11.341491+01	7	\N	\N
4	103	106	f	f	3	2011-03-21 17:26:11.380395+01	6	\N	\N
5	103	107	f	f	3	2011-03-21 17:26:11.431101+01	5	\N	\N
6	103	108	f	f	3	2011-03-21 17:26:11.478735+01	4	\N	\N
7	103	109	f	f	3	2011-03-21 17:26:11.526622+01	3	\N	\N
8	103	110	f	f	3	2011-03-21 17:26:11.575038+01	2	\N	\N
9	103	111	f	f	3	2011-03-21 17:26:11.631348+01	1	\N	\N
34	114	108	f	f	3	2011-03-21 17:26:12.882289+01	4	\N	\N
35	114	109	f	f	3	2011-03-21 17:26:12.916932+01	3	\N	\N
37	114	111	f	f	3	2011-03-21 17:26:12.997449+01	1	\N	\N
24	113	105	f	f	3	2011-03-21 17:26:12.451077+01	7	\N	\N
25	113	106	f	f	3	2011-03-21 17:26:12.500247+01	6	\N	\N
10	104	105	f	f	3	2011-03-21 17:26:11.764041+01	7	\N	\N
11	104	106	f	f	3	2011-03-21 17:26:11.808166+01	6	\N	\N
12	104	107	f	f	3	2011-03-21 17:26:11.867637+01	5	\N	\N
13	104	108	f	f	3	2011-03-21 17:26:11.917843+01	4	\N	\N
14	104	109	f	f	3	2011-03-21 17:26:11.961496+01	3	\N	\N
15	104	110	f	f	3	2011-03-21 17:26:12.005426+01	2	\N	\N
16	104	111	f	f	3	2011-03-21 17:26:12.048597+01	1	\N	\N
26	113	107	f	f	3	2011-03-21 17:26:12.537417+01	5	\N	\N
27	113	108	f	f	3	2011-03-21 17:26:12.574531+01	4	\N	\N
28	113	109	f	f	3	2011-03-21 17:26:12.629951+01	3	\N	\N
29	113	110	f	f	3	2011-03-21 17:26:12.666348+01	2	\N	\N
30	113	111	f	f	3	2011-03-21 17:26:12.705713+01	1	\N	\N
38	115	105	f	f	3	2011-03-21 17:26:13.04653+01	7	\N	\N
39	115	106	f	f	3	2011-03-21 17:26:13.088932+01	6	\N	\N
40	115	107	f	f	3	2011-03-21 17:26:13.120735+01	5	\N	\N
41	115	108	f	f	3	2011-03-21 17:26:13.161247+01	4	\N	\N
42	115	109	f	f	3	2011-03-21 17:26:13.201593+01	3	\N	\N
43	115	110	f	f	3	2011-03-21 17:26:13.233222+01	2	\N	\N
44	115	111	f	f	3	2011-03-21 17:26:13.275769+01	1	\N	\N
\.


--
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) FROM stdin;
101	UNKNOWN	Unknown	1	2011-03-21 17:22:23.155774+01	\N	\N
102	HCS_IMAGE_RAW	Raw High Content Screening Images	1	2011-03-21 17:22:23.186545+01	\N	\N
103	HCS_IMAGE_OVERVIEW	Overview High Content Screening Images. Generated from raw images.	1	2011-03-21 17:22:23.187436+01	\N	\N
104	HCS_IMAGE_SEGMENTATION	HCS Segmentation Images (overlays).	1	2011-03-21 17:22:23.188406+01	\N	\N
105	HCS_ANALYSIS_WELL_FEATURES	HCS image analysis well feature vectors.	1	2011-03-21 17:22:23.18922+01	\N	\N
106	HCS_ANALYSIS_CELL_SEGMENTATION	HCS image analysis cell segmentation	1	2011-03-21 17:22:23.190036+01	\N	\N
107	HCS_ANALYSIS_CELL_FEATURES	HCS image analysis cell feature vectors	1	2011-03-21 17:22:23.190936+01	\N	\N
108	HCS_ANALYSIS_CELL_CLASS	HCS image analysis cell classification	1	2011-03-21 17:22:23.191951+01	\N	\N
113	HCS_ANALYSIS_WELL_QUALITY	\N	1	2011-03-21 17:26:12.44098+01	\N	\N
114	HCS_ANALYSIS_CELL_FEATURES_CSV	\N	1	2011-03-21 17:26:12.743694+01	\N	\N
115	HCS_ANALYSIS_CELL_FEATURES_MAT	\N	1	2011-03-21 17:26:13.036708+01	\N	\N
\.


--
-- Data for Name: data_store_service_data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_store_service_data_set_types (data_store_service_id, data_set_type_id) FROM stdin;
\.


--
-- Data for Name: data_store_services; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_store_services (id, key, label, kind, data_store_id, reporting_plugin_type) FROM stdin;
\.


--
-- Data for Name: data_stores; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_stores (id, dbin_id, code, download_url, remote_url, session_token, registration_timestamp, modification_timestamp, is_archiver_configured) FROM stdin;
1	1	STANDARD				2010-05-10 17:57:14.310868+02	2010-05-10 17:57:14.310868+02	f
\.


--
-- Data for Name: data_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_types (id, code, description) FROM stdin;
1	VARCHAR	Short text
2	MULTILINE_VARCHAR	Long text
3	INTEGER	Integer number
4	REAL	Real number, i.e. an inexact, variable-precision numeric type
5	BOOLEAN	True or False
6	TIMESTAMP	Both date and time. Format: yyyy-mm-dd hh:mm:ss
7	CONTROLLEDVOCABULARY	Controlled Vocabulary
8	MATERIAL	Reference to a material
9	HYPERLINK	Address of a web page
10	XML	XML document
\.


--
-- Data for Name: database_instances; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_instances (id, code, uuid, is_original_source, registration_timestamp) FROM stdin;
1	DEMO	678243C3-BD97-42E4-B04B-34DA0C43564D	t	2010-05-10 17:57:14.310868+02
\.


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
060	./sql/postgresql/migration/migration-059-060.sql	SUCCESS	2010-10-29 13:39:26.04	\\x2d2d204d6967726174696f6e2066726f6d2030353920746f203036300a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d202044726f702074726967676572206f6e20717565726965732e656e746974795f747970655f636f646520636f6c756d6e20746f20656e61626c652073746f72696e672061207265676578702074686572652e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a44524f5020545249474745522071756572795f656e746974795f747970655f636f64655f636865636b204f4e20717565726965733b0a44524f502046554e4354494f4e2071756572795f656e746974795f747970655f636f64655f636865636b28293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2044796e616d69632070726f706572746965730a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a2d2d204372656174652053435249505453207461626c650a0a435245415445205441424c4520534352495054532028494420544543485f4944204e4f54204e554c4c2c4442494e5f494420544543485f4944204e4f54204e554c4c2c4e414d4520564152434841522832303029204e4f54204e554c4c2c4445534352495054494f4e204445534352495054494f4e5f323030302c53435249505420544558545f56414c5545204e4f54204e554c4c2c524547495354524154494f4e5f54494d455354414d502054494d455f5354414d505f44464c204e4f54204e554c4c2044454641554c542043555252454e545f54494d455354414d502c504552535f49445f5245474953544552455220544543485f4944204e4f54204e554c4c293b0a4352454154452053455155454e4345205343524950545f49445f5345513b0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f504b205052494d415259204b4559284944293b0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f554b20554e49515545284e414d452c4442494e5f4944293b0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f4442494e5f464b20464f524549474e204b455920284442494e5f494429205245464552454e4345532044415441424153455f494e5354414e434553284944293b0a414c544552205441424c4520534352495054532041444420434f4e53545241494e5420534352495f504552535f464b20464f524549474e204b45592028504552535f49445f5245474953544552455229205245464552454e43455320504552534f4e53284944293b0a43524541544520494e444558205343524950545f504552535f464b5f49204f4e20534352495054532028504552535f49445f52454749535445524552293b0a43524541544520494e444558205343524950545f4442494e5f464b5f49204f4e205343524950545320284442494e5f4944293b0a0a2d2d204164642049535f44594e414d494320636f6c756d6e20746f202a5f50524f50455254595f5459504553207461626c6573200a0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4c554d4e2049535f44594e414d494320424f4f4c45414e204e4f54204e554c4c2044454641554c542046414c53453b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4c554d4e2049535f44594e414d494320424f4f4c45414e204e4f54204e554c4c2044454641554c542046414c53453b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4c554d4e2049535f44594e414d494320424f4f4c45414e204e4f54204e554c4c2044454641554c542046414c53453b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4c554d4e2049535f44594e414d494320424f4f4c45414e204e4f54204e554c4c2044454641554c542046414c53453b0a0a2d2d20416464205343524950545f494420636f6c756d6e20746f202a5f50524f50455254595f5459504553207461626c6573200a0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4c554d4e205343524950545f494420544543485f49443b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4c554d4e205343524950545f494420544543485f49443b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4c554d4e205343524950545f494420544543485f49443b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4c554d4e205343524950545f494420544543485f49443b0a0a2d2d204d616b65205343524950545f4944207265666572656e636520534352495054530a0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f5343524950545f464b20464f524549474e204b455920285343524950545f494429205245464552454e4345532053435249505453284944293b0a0a2d2d20436865636b205343524950545f49442069732066696c6c6564207768656e2049535f44594e414d494320697320545255450a0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532041444420434f4e53545241494e54204d5450545f434b20434845434b20282849535f44594e414d4943204953205452554520414e44205343524950545f4944204953204e4f54204e554c4c29204f52202849535f44594e414d49432049532046414c534520414e44205343524950545f4944204953204e554c4c29293b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420535450545f434b20434845434b20282849535f44594e414d4943204953205452554520414e44205343524950545f4944204953204e4f54204e554c4c29204f52202849535f44594e414d49432049532046414c534520414e44205343524950545f4944204953204e554c4c29293b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e5420455450545f434b20434845434b20282849535f44594e414d4943204953205452554520414e44205343524950545f4944204953204e4f54204e554c4c29204f52202849535f44594e414d49432049532046414c534520414e44205343524950545f4944204953204e554c4c29293b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532041444420434f4e53545241494e542044535450545f434b20434845434b20282849535f44594e414d4943204953205452554520414e44205343524950545f4944204953204e4f54204e554c4c29204f52202849535f44594e414d49432049532046414c534520414e44205343524950545f4944204953204e554c4c29293b0a0a	\N
061	../openbis/source//sql/postgresql/migration/migration-060-061.sql	SUCCESS	2011-03-21 17:23:56.541	\\x2d2d204d6967726174696f6e2066726f6d2030363020746f203036310a0a43524541544520444f4d41494e20454e544954595f4b494e4420415320564152434841522834302920434845434b202856414c554520494e20282753414d504c45272c20274558504552494d454e54272c2027444154415f534554272c20274d4154455249414c2729293b0a0a414c544552205441424c4520534352495054532041444420434f4c554d4e20454e544954595f4b494e4420454e544954595f4b494e443b0a	\N
062	../openbis/source//sql/postgresql/migration/migration-061-062.sql	SUCCESS	2011-03-21 17:23:56.658	\\x0a435245415445204f52205245504c4143452046554e4354494f4e2073616d706c655f636f64655f756e697175656e6573735f636865636b28292052455455524e5320747269676765720a202020204c414e475541474520706c706773716c0a2020202041532024240a4445434c4152450a202020636f756e7465722020494e54454745523b0a424547494e0a20204c4f434b205441424c452073616d706c657320494e204558434c5553495645204d4f44453b0a20200a092020494620284e45572e73616d705f69645f706172745f6f66206973204e554c4c29205448454e0a09092020494620284e45572e6462696e5f6964206973206e6f74204e554c4c29205448454e0a090909202053454c45435420636f756e74282a2920696e746f20636f756e7465722046524f4d2073616d706c6573200a0909202020202020776865726520696420213d204e45572e696420616e6420636f6465203d204e45572e636f646520616e642073616d705f69645f706172745f6f66206973204e554c4c20616e64206462696e5f6964203d204e45572e6462696e5f69643b0a202020202020202049462028636f756e746572203e203029205448454e0a090909092020524149534520455843455054494f4e2027496e736572742f557064617465206f662053616d706c652028436f64653a202529206661696c6564206265636175736520646174616261736520696e7374616e63652073616d706c652077697468207468652073616d6520636f646520616c7265616479206578697374732e272c204e45572e636f64653b0a2020202020202020454e442049463b0a09092020454c53494620284e45572e73706163655f6964206973206e6f74204e554c4c29205448454e0a090909202053454c45435420636f756e74282a2920696e746f20636f756e7465722046524f4d2073616d706c6573200a090909092020776865726520696420213d204e45572e696420616e6420636f6465203d204e45572e636f646520616e642073616d705f69645f706172745f6f66206973204e554c4c20616e642073706163655f6964203d204e45572e73706163655f69643b0a090909202049462028636f756e746572203e203029205448454e0a090909092020524149534520455843455054494f4e2027496e736572742f557064617465206f662053616d706c652028436f64653a202529206661696c656420626563617573652073706163652073616d706c652077697468207468652073616d6520636f646520616c7265616479206578697374732e272c204e45572e636f64653b0a0909092020454e442049463b0a202020202020454e442049463b0a20202020454c53450a09092020494620284e45572e6462696e5f6964206973206e6f74204e554c4c29205448454e0a090909202053454c45435420636f756e74282a2920696e746f20636f756e7465722046524f4d2073616d706c6573200a090909092020776865726520696420213d204e45572e696420616e6420636f6465203d204e45572e636f646520616e642073616d705f69645f706172745f6f66203d204e45572e73616d705f69645f706172745f6f6620616e64206462696e5f6964203d204e45572e6462696e5f69643b0a090909202049462028636f756e746572203e203029205448454e0a090909092020524149534520455843455054494f4e2027496e736572742f557064617465206f662053616d706c652028436f64653a202529206661696c6564206265636175736520646174616261736520696e7374616e63652073616d706c652077697468207468652073616d6520636f646520616e64206265696e67207468652070617274206f66207468652073616d6520636f6e7461696e657220616c7265616479206578697374732e272c204e45572e636f64653b0a0909092020454e442049463b0a09092020454c53494620284e45572e73706163655f6964206973206e6f74204e554c4c29205448454e0a090909202053454c45435420636f756e74282a2920696e746f20636f756e7465722046524f4d2073616d706c6573200a090909092020776865726520696420213d204e45572e696420616e6420636f6465203d204e45572e636f646520616e642073616d705f69645f706172745f6f66203d204e45572e73616d705f69645f706172745f6f6620616e642073706163655f6964203d204e45572e73706163655f69643b0a090909202049462028636f756e746572203e203029205448454e0a090909092020524149534520455843455054494f4e2027496e736572742f557064617465206f662053616d706c652028436f64653a202529206661696c656420626563617573652073706163652073616d706c652077697468207468652073616d6520636f646520616e64206265696e67207468652070617274206f66207468652073616d6520636f6e7461696e657220616c7265616479206578697374732e272c204e45572e636f64653b0a0909092020454e442049463b0a09092020454e442049463b0a2020202020454e442049463b2020200a20200a202052455455524e204e45573b0a454e443b0a24243b0a0a0a435245415445204f52205245504c4143452046554e4354494f4e2073616d706c655f737562636f64655f756e697175656e6573735f636865636b28292052455455524e5320747269676765720a202020204c414e475541474520706c706773716c0a2020202041532024240a4445434c4152450a202020636f756e7465722020494e54454745523b0a202020756e697175655f737562636f64652020424f4f4c45414e5f434841523b0a424547494e0a20204c4f434b205441424c452073616d706c657320494e204558434c5553495645204d4f44453b0a20200a202053454c4543542069735f737562636f64655f756e6971756520696e746f20756e697175655f737562636f64652046524f4d2073616d706c655f7479706573205748455245206964203d204e45572e736174795f69643b0a20200a202049462028756e697175655f737562636f646529205448454e0a20202020494620284e45572e6462696e5f6964206973206e6f74204e554c4c29205448454e0a09090953454c45435420636f756e74282a2920696e746f20636f756e7465722046524f4d2073616d706c6573200a09090909776865726520696420213d204e45572e696420616e6420636f6465203d204e45572e636f646520616e6420736174795f6964203d204e45572e736174795f696420616e64206462696e5f6964203d204e45572e6462696e5f69643b0a09090949462028636f756e746572203e203029205448454e0a09090909524149534520455843455054494f4e2027496e736572742f557064617465206f662053616d706c652028436f64653a202529206661696c6564206265636175736520646174616261736520696e7374616e63652073616d706c65206f66207468652073616d6520747970652077697468207468652073616d6520737562636f646520616c7265616479206578697374732e272c204e45572e636f64653b0a090909454e442049463b0a0909454c53494620284e45572e73706163655f6964206973206e6f74204e554c4c29205448454e0a09090953454c45435420636f756e74282a2920696e746f20636f756e7465722046524f4d2073616d706c6573200a09090909776865726520696420213d204e45572e696420616e6420636f6465203d204e45572e636f646520616e6420736174795f6964203d204e45572e736174795f696420616e642073706163655f6964203d204e45572e73706163655f69643b0a09090949462028636f756e746572203e203029205448454e0a09090909524149534520455843455054494f4e2027496e736572742f557064617465206f662053616d706c652028436f64653a202529206661696c656420626563617573652073706163652073616d706c65206f66207468652073616d6520747970652077697468207468652073616d6520737562636f646520616c7265616479206578697374732e272c204e45572e636f64653b0a090909454e442049463b0a0909454e442049463b0a2020454e442049463b0a20200a202052455455524e204e45573b0a454e443b0a24243b0a0a414c544552205441424c452067726f7570732052454e414d4520544f207370616365733b0a0a414c5445522053455155454e43452067726f75705f69645f7365712052454e414d4520544f2073706163655f69645f7365713b0a0a414c544552205441424c4520706572736f6e730952454e414d452067726f755f696420544f2073706163655f69643b0a414c544552205441424c452070726f6a656374732052454e414d452067726f755f696420544f2073706163655f69643b0a414c544552205441424c4520726f6c655f61737369676e6d656e74732052454e414d452067726f755f696420544f2073706163655f69643b0a414c544552205441424c452073616d706c65730952454e414d452067726f755f696420544f2073706163655f69643b0a0a2d2d206d696772617465204556454e5453207461626c650a0a414c544552205441424c45206576656e74730a0944524f5020434f4e53545241494e542065766e745f65745f656e756d5f636b3b0a0a757064617465206576656e74732073657420656e746974795f74797065203d202753504143452720776865726520656e746974795f74797065203d202747524f5550273b0a0a414c544552205441424c45206576656e74732041444420434f4e53545241494e542065766e745f65745f656e756d5f636b20434845434b200a0928656e746974795f7479706520494e2028274154544143484d454e54272c202744415441534554272c20274558504552494d454e54272c20275350414345272c20274d4154455249414c272c202750524f4a454354272c202750524f50455254595f54595045272c202753414d504c45272c2027564f434142554c415259272c2027415554484f52495a4154494f4e5f47524f55502729293b200a0a414c544552205441424c4520706572736f6e730a0944524f5020434f4e53545241494e5420706572735f67726f755f666b3b0a0a414c544552205441424c4520706572736f6e730a0941444420434f4e53545241494e5420706572735f73706163655f666b20464f524549474e204b4559202873706163655f696429205245464552454e43455320737061636573286964293b0a0a414c544552205441424c452070726f6a656374730a0944524f5020434f4e53545241494e542070726f6a5f626b5f756b3b0a0a414c544552205441424c452070726f6a656374730a0944524f5020434f4e53545241494e542070726f6a5f67726f755f666b3b0a0a414c544552205441424c452070726f6a656374730a0941444420434f4e53545241494e542070726f6a5f626b5f756b20554e495155452028636f64652c2073706163655f6964293b0a0a414c544552205441424c452070726f6a656374730a0941444420434f4e53545241494e542070726f6a5f73706163655f666b20464f524549474e204b4559202873706163655f696429205245464552454e43455320737061636573286964293b0a0a414c544552205441424c4520726f6c655f61737369676e6d656e74730a0944524f5020434f4e53545241494e5420726f61735f6462696e5f67726f755f6172635f636b3b0a0a414c544552205441424c4520726f6c655f61737369676e6d656e74730a0944524f5020434f4e53545241494e5420726f61735f61675f67726f75705f626b5f756b3b0a0a414c544552205441424c4520726f6c655f61737369676e6d656e74730a0944524f5020434f4e53545241494e5420726f61735f70655f67726f75705f626b5f756b3b0a0a414c544552205441424c4520726f6c655f61737369676e6d656e74730a0944524f5020434f4e53545241494e5420726f61735f67726f755f666b3b0a0a414c544552205441424c4520726f6c655f61737369676e6d656e74730a0941444420434f4e53545241494e5420726f61735f6462696e5f73706163655f6172635f636b20434845434b20282828286462696e5f6964204953204e4f54204e554c4c2920414e44202873706163655f6964204953204e554c4c2929204f522028286462696e5f6964204953204e554c4c2920414e44202873706163655f6964204953204e4f54204e554c4c292929293b0a0a414c544552205441424c4520726f6c655f61737369676e6d656e74730a0941444420434f4e53545241494e5420726f61735f61675f73706163655f626b5f756b20554e49515545202861675f69645f6772616e7465652c20726f6c655f636f64652c2073706163655f6964293b0a0a414c544552205441424c4520726f6c655f61737369676e6d656e74730a0941444420434f4e53545241494e5420726f61735f70655f73706163655f626b5f756b20554e495155452028706572735f69645f6772616e7465652c20726f6c655f636f64652c2073706163655f6964293b0a0a414c544552205441424c4520726f6c655f61737369676e6d656e74730a0941444420434f4e53545241494e5420726f61735f73706163655f666b20464f524549474e204b4559202873706163655f696429205245464552454e43455320737061636573286964293b0a0a414c544552205441424c452073616d706c65730a0944524f5020434f4e53545241494e542073616d705f6462696e5f67726f755f6172635f636b3b0a0a414c544552205441424c452073616d706c65730a0944524f5020434f4e53545241494e542073616d705f67726f755f666b3b0a0a414c544552205441424c452073616d706c65730a0941444420434f4e53545241494e542073616d705f6462696e5f73706163655f6172635f636b20434845434b20282828286462696e5f6964204953204e4f54204e554c4c2920414e44202873706163655f6964204953204e554c4c2929204f522028286462696e5f6964204953204e554c4c2920414e44202873706163655f6964204953204e4f54204e554c4c292929293b0a0a414c544552205441424c452073616d706c65730a0941444420434f4e53545241494e542073616d705f73706163655f666b20464f524549474e204b4559202873706163655f696429205245464552454e43455320737061636573286964293b0a0a414c544552205441424c45207370616365730a202044524f5020434f4e53545241494e542067726f755f626b5f756b2c0a202041444420434f4e53545241494e542073706163655f626b5f756b20554e495155452028636f64652c206462696e5f6964293b0a0a414c544552205441424c45207370616365730a202044524f5020434f4e53545241494e542067726f755f6462696e5f666b2c0a202041444420434f4e53545241494e542073706163655f6462696e5f666b20464f524549474e204b455920286462696e5f696429205245464552454e4345532064617461626173655f696e7374616e636573286964293b0a0a414c544552205441424c45207370616365730a202044524f5020434f4e53545241494e542067726f755f706572735f666b5f726567697374657265722c0a202041444420434f4e53545241494e542073706163655f706572735f666b5f7265676973746572657220464f524549474e204b45592028706572735f69645f7265676973746572657229205245464552454e43455320706572736f6e73286964293b0a0a414c54455220494e44455820706572735f67726f755f666b5f692052454e414d4520544f20706572735f73706163655f666b5f693b0a0a414c54455220494e4445582070726f6a5f67726f755f666b5f692052454e414d4520544f2070726f6a5f73706163655f666b5f693b0a0a414c54455220494e44455820726f61735f67726f755f666b5f692052454e414d4520544f20726f61735f73706163655f666b5f693b0a0a414c54455220494e4445582067726f755f706b2052454e414d4520544f2073706163655f706b3b0a0a414c54455220494e4445582067726f755f6462696e5f666b5f692052454e414d4520544f2073706163655f6462696e5f666b5f693b0a0a414c54455220494e4445582067726f755f706572735f726567697374657265645f62795f666b5f692052454e414d4520544f2073706163655f706572735f726567697374657265645f62795f666b5f693b0a0a	\N
063	../openbis/source//sql/postgresql/migration/migration-062-063.sql	SUCCESS	2011-03-21 17:23:56.691	\\x2d2d204d6967726174696f6e2066726f6d2030363220746f203036330a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2053637265656e696e67207370656369666963206d6967726174696f6e2e204e6f7468696e672077696c6c20626520706572666f726d6564206f6e206f70656e42495320646174616261736573200a2d2d20776869636820617265206e6f742073637265656e696e672073706563696669632e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a2d2d20636f6e6e6563742077656c6c7320746f206578706572696d656e7473206f6620746865697220706c617465730a5550444154452073616d706c6573200a0953455420657870655f6964203d2073632e657870655f69640a0946524f4d2073616d706c65732073632c2073616d706c655f7479706573207363740a0957484552452073616d706c65732e657870655f6964204953204e554c4c0a09414e442073632e6964203d2073616d706c65732e73616d705f69645f706172745f6f660a09414e44207363742e6964203d2073632e736174795f696420414e44207363742e636f6465203d2027504c415445273b0a0a	\N
064	../openbis/source//sql/postgresql/migration/migration-063-064.sql	SUCCESS	2011-03-21 17:23:56.731	\\x2d2d204d6967726174696f6e2066726f6d2030363320746f203036340a0a2d2d20416464205343524950545f5459504520636f6c756d6e20746f20534352495054530a43524541544520444f4d41494e207363726970745f7479706520415320564152434841522834302920434845434b202856414c554520494e20282744594e414d49435f50524f5045525459272c20274d414e414745445f50524f50455254592729293b0a414c544552205441424c4520736372697074732041444420434f4c554d4e207363726970745f74797065207363726970745f747970653b0a555044415445207363726970747320534554207363726970745f74797065203d202744594e414d49435f50524f5045525459273b0a414c544552205441424c45205343524950545320414c54455220434f4c554d4e205343524950545f5459504520534554204e4f54204e554c4c3b200a0a2d2d2052656d6f766520726564756e64616e742049535f44594e414d494320636f6c756d6e2066726f6d202a5f50524f50455254595f5459504553207461626c6573200a0a414c544552205441424c45204d4154455249414c5f545950455f50524f50455254595f54595045532044524f5020434f4c554d4e2049535f44594e414d49433b0a414c544552205441424c452053414d504c455f545950455f50524f50455254595f54595045532044524f5020434f4c554d4e2049535f44594e414d49433b0a414c544552205441424c45204558504552494d454e545f545950455f50524f50455254595f54595045532044524f5020434f4c554d4e2049535f44594e414d49433b0a414c544552205441424c4520444154415f5345545f545950455f50524f50455254595f54595045532044524f5020434f4c554d4e2049535f44594e414d49433b0a	\N
065	../openbis/source//sql/postgresql/migration/migration-064-065.sql	SUCCESS	2011-03-21 17:23:56.778	\\x2d2d204d6967726174696f6e2066726f6d2030363420746f203036350a0a2d2d2044656c657465206f727068616e6564206174746163686d656e745f636f6e74656e74732028736565204c4d532d313933362920200a44454c4554452046524f4d206174746163686d656e745f636f6e74656e7473205748455245204e4f5420455849535453200a202020202853454c4543542069642046524f4d206174746163686d656e7473205748455245206174746163686d656e745f636f6e74656e74732e6964203d206174746163686d656e74732e657861635f6964290a	\N
066	../openbis/source//sql/postgresql/migration/migration-065-066.sql	SUCCESS	2011-03-21 17:23:56.897	\\x2d2d204d6967726174696f6e2066726f6d2030363520746f203036360a0a0a414c544552205441424c452045585445524e414c5f444154412041444420434f4c554d4e2053484152455f494420434f44453b0a414c544552205441424c452045585445524e414c5f444154412041444420434f4c554d4e2053495a45204f5244494e414c5f494e543b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d205265646f2070617274206f66206d6967726174696f6e2066726f6d2030353620746f203035370a2d2d0a2d2d20496e74726f64756374696f6e206f662061206e657720646174612074797065202d20584d4c2c2077617320646f6e6520696e206d6967726174696f6e2062757420646174612e73716c207761736e277420757064617465642e0a2d2d204164642074686520584d4c2064617461207479706520697420696620697420646f65736e27742065786973742e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e20696e736572745f786d6c5f646174615f747970655f69665f6e6f745f65786973747328292052455455524e5320766f69642041532024240a4445434c4152450a0965786973747320626f6f6c3b0a424547494e0a2020202053454c454354207472756520494e544f206578697374730a2020202046524f4d20646174615f747970657320574845524520636f6465203d2027584d4c273b0a202020200a20202020494620657869737473204953204e554c4c207468656e0a202020202020202d2d20584d4c2064617461207479706520646f65736e2774206578697374202d20696e736572742069740a20202020202020494e5345525420494e544f20646174615f7479706573202869642c20636f64652c206465736372697074696f6e29200a202020202020202020202020202056414c55455320286e65787476616c2827646174615f747970655f69645f73657127292c2027584d4c272c2027584d4c20646f63756d656e7427293b0a20202020454e442049463b090a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a0a53454c45435420696e736572745f786d6c5f646174615f747970655f69665f6e6f745f65786973747328293b0a44524f502046554e4354494f4e20696e736572745f786d6c5f646174615f747970655f69665f6e6f745f65786973747328293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d205265646f2070617274206f66206d6967726174696f6e2066726f6d2030363320746f203036340a2d2d0a2d2d20446f6d61696e205343524950545f54595045206372656174656420696e206d6967726174696f6e2068617320646966666572656e742076616c756573207468616e20746865206f6e6520696e20646f6d61696e2e73716c2e0a2d2d20416c7465722074686520646f6d61696e2e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a414c54455220444f4d41494e207363726970745f747970652044524f5020434f4e53545241494e54207363726970745f747970655f636865636b3b0a414c54455220444f4d41494e207363726970745f747970652041444420434f4e53545241494e54207363726970745f747970655f636865636b20434845434b202856414c554520494e20282744594e414d49435f50524f5045525459272c20274d414e414745445f50524f50455254592729293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d205468697320697320612073637265656e696e67207370656369666963206d6967726174696f6e2e204e6f7468696e672077696c6c20626520706572666f726d6564206f6e206f70656e42495320646174616261736573200a2d2d20776869636820617265206e6f742073637265656e696e672073706563696669632e0a2d2d200a2d2d2054686973206d6967726174696f6e20666f722065616368206578697374696e6720636f6e6e656374696f6e206265747765656e206f6c69676f2077656c6c2c206f6c69676f206d6174657269616c20616e642067656e65206d6174657269616c0a2d2d206372656174657320612064697265637420636f6e6e656374696f6e206265747765656e207468652077656c6c20616e64207468652067656e652e200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a200a435245415445204f52205245504c4143452046554e4354494f4e20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e54286e65775f636f646520746578742c206e65775f6465736372697074696f6e2074657874292052455455524e5320766f69642041532024240a4445434c4152450a0965786973747320626f6f6c3b0a424547494e0a090a0973656c656374207472756520696e746f206578697374732066726f6d20646174615f7365745f747970657309776865726520636f6465203d206e65775f636f64653b0a090a09696620657869737473204953204e4f54204e554c4c207468656e200a090972657475726e3b0a09656e642069663b2020200a090a09696e7365727420696e746f20646174615f7365745f7479706573280a090969642c200a0909636f64652c206465736372697074696f6e2c200a09096462696e5f6964290a0976616c756573280a0909096e65787476616c2827646174615f7365745f747970655f69645f73657127292c200a0909096e65775f636f64652c0a0909096e65775f6465736372697074696f6e2c0a0909092873656c6563742069642066726f6d2064617461626173655f696e7374616e6365732077686572652069735f6f726967696e616c5f736f75726365203d20275427290a0909293b0a09090a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445204f52205245504c4143452046554e4354494f4e204352454154455f4843535f444154415345545f545950455328292052455455524e5320766f69642041532024240a4445434c4152450a096863735f696d6167655f646174617365745f65786973747320626f6f6c3b0a09756e6b6e6f776e5f66696c655f666f726d61745f65786973747320626f6f6c3b0a424547494e0a090a0973656c6563742074727565200a09696e746f206863735f696d6167655f646174617365745f6578697374730a0966726f6d20646174615f7365745f7479706573200a09776865726520636f6465203d20274843535f494d414745273b0a090a096966206863735f696d6167655f646174617365745f657869737473204953204e554c4c207468656e200a09092d2d20736b6970206d6967726174696f6e206966207468657265206973206e6f74204843535f494d414745206461746173657420747970650a090972657475726e3b0a09656e642069663b2020200a090a092d2d20696e7365727420756e6b6e6f776e2066696c6520666f726d6174206966206974206973206e6f74207965742070726573656e74202d2d200a0973656c656374207472756520696e746f20756e6b6e6f776e5f66696c655f666f726d61745f6578697374732066726f6d2066696c655f666f726d61745f747970657320776865726520636f6465203d2027554e4b4e4f574e273b0a09696620756e6b6e6f776e5f66696c655f666f726d61745f657869737473204953204e554c4c207468656e200a090909696e7365727420696e746f2066696c655f666f726d61745f7479706573280a0909090969642c200a09090909636f64652c200a090909096465736372697074696f6e2c0a090909096462696e5f696429200a09090976616c756573280a090909096e65787476616c282766696c655f666f726d61745f747970655f69645f73657127292c200a0909090927554e4b4e4f574e272c200a0909090927556e6b6e6f776e2066696c6520666f726d6174272c0a090909092873656c6563742069642066726f6d2064617461626173655f696e7374616e6365732077686572652069735f6f726967696e616c5f736f75726365203d20275427290a090909293b090a09656e642069663b2020200a090a09504552464f524d20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e54280a090909274843535f494d4147455f524157272c090a09090927526177204869676820436f6e74656e742053637265656e696e6720496d6167657327293b0a09504552464f524d20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e54280a090909274843535f494d4147455f4f56455256494557272c0a090909274f76657276696577204869676820436f6e74656e742053637265656e696e6720496d616765732e2047656e6572617465642066726f6d2072617720696d616765732e27293b0a09504552464f524d20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e54280a090909274843535f494d4147455f5345474d454e544154494f4e272c0a09090927484353205365676d656e746174696f6e20496d6167657320286f7665726c6179732927293b0a09504552464f524d20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e54280a090909274843535f414e414c595349535f57454c4c5f4645415455524553272c0a0909092748435320696d61676520616e616c797369732077656c6c206665617475726520766563746f72732e27293b0a09504552464f524d20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e54280a090909274843535f414e414c595349535f43454c4c5f5345474d454e544154494f4e272c0a0909092748435320696d61676520616e616c797369732063656c6c207365676d656e746174696f6e27293b0a09504552464f524d20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e5428090a090909274843535f414e414c595349535f43454c4c5f4645415455524553272c0a0909092748435320696d61676520616e616c797369732063656c6c206665617475726520766563746f727327293b0a09504552464f524d20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e54280a090909274843535f414e414c595349535f43454c4c5f434c415353272c0a0909092748435320696d61676520616e616c797369732063656c6c20636c617373696669636174696f6e27293b0a0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a73656c656374204352454154455f4843535f444154415345545f545950455328293b0a64726f702066756e6374696f6e204352454154455f4843535f444154415345545f545950455328293b0a64726f702066756e6374696f6e20494e534552545f444154415345545f545950455f49465f4e4f545f50524553454e5428746578742c2074657874293b0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d3d3d3d3d3d3d3d0a	\N
067	../openbis/source//sql/postgresql/migration/migration-066-067.sql	SUCCESS	2011-03-21 17:23:56.984	\\x0a2d2d204d6967726174696f6e2066726f6d2030363620746f203036370a414c544552205441424c452045585445524e414c5f444154412041444420434f4c554d4e2050524553454e545f494e5f4152434849564520424f4f4c45414e5f434841522044454641554c54202746414c5345273b0a7570646174652045585445524e414c5f44415441207365742050524553454e545f494e5f415243484956453d275452554527207768657265207374617475733d27415243484956454427200a	\N
068	../openbis/source//sql/postgresql/migration/migration-067-068.sql	SUCCESS	2011-03-21 17:23:57.029	\\x2d2d204d6967726174696f6e2066726f6d2030363720746f203036380a414c54455220444f4d41494e20415243484956494e475f5354415455532044524f5020434f4e53545241494e5420415243484956494e475f5354415455535f434845434b3b0a414c54455220444f4d41494e20415243484956494e475f5354415455532041444420434f4e53545241494e5420415243484956494e475f5354415455535f434845434b20434845434b202856414c554520494e2028274c4f434b4544272c2027415641494c41424c45272c20274152434849564544272c2027415243484956455f50454e44494e47272c2027554e415243484956455f50454e44494e47272c20274241434b55505f50454e44494e472729293b0a0a0a	\N
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY events (id, event_type, description, reason, pers_id_registerer, registration_timestamp, entity_type, identifier) FROM stdin;
\.


--
-- Data for Name: experiment_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_properties (id, expe_id, etpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
3	2	1	Example experiment	\N	\N	3	2010-05-10 19:15:50.404503+02	2010-05-10 19:15:50.616+02
\.


--
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id) FROM stdin;
1	1	1	t	t	1	2008-06-17 16:38:49.023295+02	1	\N	\N
2	2	1	t	t	1	2008-06-17 16:38:49.301922+02	1	\N	\N
5	1	13	f	f	1	2009-12-17 10:54:01.261178+01	2	\N	\N
\.


--
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
2	COMPOUND_HCS	Compound High Content Screening	1	2009-11-27 16:02:26.451046+01
1	SIRNA_HCS	Small Interfering RNA High Content Screening	1	2009-11-27 16:02:26.451046+01
\.


--
-- Data for Name: experiments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiments (id, perm_id, code, exty_id, mate_id_study_object, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, inva_id, is_public) FROM stdin;
2	20100510191550585-2	E1	1	\N	3	2010-05-10 19:15:50.404503+02	2010-05-10 19:15:50.609+02	1	\N	f
\.


--
-- Data for Name: external_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY external_data (data_id, location, ffty_id, loty_id, cvte_id_stor_fmt, is_complete, cvte_id_store, status, share_id, size, present_in_archive) FROM stdin;
\.


--
-- Data for Name: file_format_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY file_format_types (id, code, description, dbin_id) FROM stdin;
1	HDF5	Hierarchical Data Format File, version 5	1
2	PROPRIETARY	Proprietary Format File	1
3	SRF	Sequence Read Format File	1
4	TIFF	TIFF File	1
5	TSV	Tab Separated Values File	1
6	XML	XML File	1
7	PNG	\N	1
8	CSV	files with values separated by comma or semicolon	1
9	JPG	\N	1
101	UNKNOWN	Unknown file format	1
\.


--
-- Data for Name: filters; Type: TABLE DATA; Schema: public; Owner: -
--

COPY filters (id, dbin_id, name, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, grid_id) FROM stdin;
9	1	Infection index	\N	2009-12-16 21:19:19.012657+01	1	2009-12-16 22:41:56.863+01	float(row.col('colIndex_5')) > float(${threshold})	t	data-set-reporting-gridplate-image-analysis-merger
8	1	Total number of cells	\N	2009-12-16 21:18:05.161964+01	1	2009-12-16 22:42:31.707+01	int(row.col('colIndex_3')) > int(${threshold})	t	data-set-reporting-gridplate-image-analysis-merger
7	1	Number of infected cells	\N	2009-12-16 21:17:40.765083+01	1	2009-12-16 22:42:46.052+01	int(row.col('colIndex_4')) > int(${threshold})	t	data-set-reporting-gridplate-image-analysis-merger
\.


--
-- Data for Name: grid_custom_columns; Type: TABLE DATA; Schema: public; Owner: -
--

COPY grid_custom_columns (id, dbin_id, code, label, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, grid_id) FROM stdin;
\.


--
-- Data for Name: invalidations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY invalidations (id, pers_id_registerer, registration_timestamp, reason) FROM stdin;
\.


--
-- Data for Name: locator_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY locator_types (id, code, description) FROM stdin;
1	RELATIVE_LOCATION	Relative Location
\.


--
-- Data for Name: material_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_properties (id, mate_id, mtpt_id, value, registration_timestamp, modification_timestamp, pers_id_registerer, cvte_id, mate_prop_id) FROM stdin;
\.


--
-- Data for Name: material_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_type_property_types (id, maty_id, prty_id, is_mandatory, is_managed_internally, registration_timestamp, pers_id_registerer, ordinal, section, script_id) FROM stdin;
10	4	7	t	f	2009-11-27 16:02:45.060699+01	1	4	\N	\N
9	5	1	f	f	2008-02-28 13:03:03.358532+01	1	1	\N	\N
8	4	1	f	f	2008-02-28 13:03:03.358532+01	1	3	\N	\N
6	4	3	t	f	2008-02-28 13:03:03.358532+01	1	1	\N	\N
5	3	1	f	f	2008-02-28 13:03:03.358532+01	1	2	\N	\N
3	7	1	t	f	2008-02-28 13:03:03.358532+01	1	1	\N	\N
2	6	1	t	f	2008-02-28 13:03:03.358532+01	1	1	\N	\N
1	1	1	t	f	2008-02-28 13:03:03.358532+01	1	1	\N	\N
102	4	101	f	f	2010-05-10 19:12:07.600654+02	3	5	\N	\N
103	3	103	f	f	2010-10-29 13:39:25.611453+02	1	4	\N	\N
\.


--
-- Data for Name: material_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	VIRUS	Virus	1	2009-11-27 16:02:26.451046+01
2	CELL_LINE	Cell Line or Cell Culture. The growing of cells under controlled conditions.	1	2009-11-27 16:02:26.451046+01
3	GENE	Gene	1	2009-11-27 16:02:26.451046+01
5	CONTROL	Control of a control layout	1	2009-11-27 16:02:26.451046+01
6	BACTERIUM	Bacterium	1	2009-11-27 16:02:26.451046+01
7	COMPOUND	Compound	1	2009-11-27 16:02:26.451046+01
4	SIRNA	Oligo nucleotide	1	2009-11-27 16:02:26.451046+01
\.


--
-- Data for Name: materials; Type: TABLE DATA; Schema: public; Owner: -
--

COPY materials (id, code, maty_id, pers_id_registerer, registration_timestamp, modification_timestamp, dbin_id) FROM stdin;
\.


--
-- Data for Name: persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY persons (id, first_name, last_name, user_id, email, dbin_id, space_id, registration_timestamp, pers_id_registerer, display_settings) FROM stdin;
1		System User	system		1	\N	2010-05-10 17:57:14.310868+02	\N	\N
2	Claude	Levi-Strauss	etlserver	franz-josef.elmer@systemsx.ch	1	\N	2010-05-10 17:57:26.252547+02	1	\\xaced00057372004163682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e446973706c617953657474696e677300000000000000010200055a0029646973706c6179437573746f6d436f6c756d6e446562756767696e674572726f724d657373616765735a001575736557696c64636172645365617263684d6f64654c000e636f6c756d6e53657474696e677374000f4c6a6176612f7574696c2f4d61703b4c001d7265616c4e756d626572466f726d6174696e67506172616d65746572737400514c63682f73797374656d73782f636973642f6f70656e6269732f67656e657269632f7368617265642f62617369632f64746f2f5265616c4e756d626572466f726d6174696e67506172616d65746572733b4c000f73656374696f6e53657474696e677371007e000178700000737200176a6176612e7574696c2e4c696e6b6564486173684d617034c04e5c106cc0fb0200015a000b6163636573734f72646572787200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000078007372004f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e5265616c4e756d626572466f726d6174696e67506172616d657465727300000000000000010200035a0010666f726d6174696e67456e61626c6564490009707265636973696f6e5a000a736369656e746966696378700100000004007371007e00053f4000000000000c7708000000100000000078
4	Claude	Lévi-Strauss	admi	franz-josef.elmer@systemsx.ch	1	\N	2011-03-21 17:26:53.55376+01	1	\\xaced00057372004163682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e446973706c617953657474696e6773000000000000000102000b5a0009646562756767696e675a001669676e6f72654c617374486973746f7279546f6b656e5a001575736557696c64636172645365617263684d6f64654c000e636f6c756d6e53657474696e677374000f4c6a6176612f7574696c2f4d61703b4c001064726f70446f776e53657474696e677371007e00014c00166c617374486973746f7279546f6b656e4f724e756c6c7400124c6a6176612f6c616e672f537472696e673b4c001670616e656c436f6c6c617073656453657474696e677371007e00014c001170616e656c53697a6553657474696e677371007e00014c001d7265616c4e756d626572466f726d6174696e67506172616d65746572737400514c63682f73797374656d73782f636973642f6f70656e6269732f67656e657269632f7368617265642f62617369632f64746f2f5265616c4e756d626572466f726d6174696e67506172616d65746572733b4c000b74616253657474696e677371007e00014c001a746563686e6f6c6f6779537065636966696353657474696e677371007e00017870000000737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c77080000001000000000787371007e00053f4000000000000c7708000000100000000078707371007e00053f4000000000000c77080000001000000000787371007e00053f4000000000000c77080000001000000000787372004f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e5265616c4e756d626572466f726d6174696e67506172616d657465727300000000000000010200035a0010666f726d6174696e67456e61626c6564490009707265636973696f6e5a000a736369656e746966696378700100000004007371007e00053f4000000000000c77080000001000000000787371007e00053f4000000000000c7708000000100000000078
3	Gunter	Kapuściński	admin	franz-josef.elmer@systemsx.ch	1	1	2010-05-10 18:23:44.96025+02	1	\\xaced00057372004163682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e446973706c617953657474696e6773000000000000000102000b5a0009646562756767696e675a001669676e6f72654c617374486973746f7279546f6b656e5a001575736557696c64636172645365617263684d6f64654c000e636f6c756d6e53657474696e677374000f4c6a6176612f7574696c2f4d61703b4c001064726f70446f776e53657474696e677371007e00014c00166c617374486973746f7279546f6b656e4f724e756c6c7400124c6a6176612f6c616e672f537472696e673b4c001670616e656c436f6c6c617073656453657474696e677371007e00014c001170616e656c53697a6553657474696e677371007e00014c001d7265616c4e756d626572466f726d6174696e67506172616d65746572737400514c63682f73797374656d73782f636973642f6f70656e6269732f67656e657269632f7368617265642f62617369632f64746f2f5265616c4e756d626572466f726d6174696e67506172616d65746572733b4c000b74616253657474696e677371007e00014c001a746563686e6f6c6f6779537065636966696353657474696e677371007e00017870000000737200176a6176612e7574696c2e4c696e6b6564486173684d617034c04e5c106cc0fb0200015a000b6163636573734f72646572787200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000874001470726f6a6563742d62726f777365722d67726964737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000577040000000a7372003f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e436f6c756d6e53657474696e6700000000000000010200045a000968617346696c7465725a000668696464656e49000577696474684c0008636f6c756d6e494471007e00027870010000000096740006504552534f4e7371007e000b010000000096740013415554484f52495a4154494f4e5f47524f55507371007e000b01000000009674000547524f55507371007e000b0100000000f1740004524f4c457371007e000b01000000009674001144415441424153455f494e5354414e43457874002a6578706572696d656e742d64657461696c732d677269642d444154415f5345542d5349524e415f4843537371007e0009000000157704000000197371007e000b010000000096740004434f44457371007e000b00010000006474000653414d504c457371007e000b0000000000c874001153414d504c455f4944454e5449464945527371007e000b00010000009674000b53414d504c455f545950457371007e000b00010000006474000a4558504552494d454e547371007e000b0001000000647400154558504552494d454e545f4944454e5449464945527371007e000b00010000007874000f4558504552494d454e545f545950457371007e000b00010000009674000750524f4a4543547371007e000b0000000000c8740011524547495354524154494f4e5f444154457371007e000b00010000009674000a49535f494e56414c49447371007e000b00010000009674000b534f555243455f545950457371007e000b00010000009674000b49535f434f4d504c4554457371007e000b0001000000967400084c4f434154494f4e7371007e000b0001000000c87400065354415455537371007e000b01000000009674001046494c455f464f524d41545f545950457371007e000b00000000009674000d444154415f5345545f545950457371007e000b0001000000c874000f50524f44554354494f4e5f444154457371007e000b000100000096740012444154415f50524f44554345525f434f44457371007e000b00010000009674000f444154415f53544f52455f434f44457371007e000b0001000000967400075045524d5f49447371007e000b00010000009674001153484f575f44455441494c535f4c494e4b78740019706c7567696e2d7461736b732d62726f777365722d677269647371007e00090000000277040000000a7371007e000b0000000001137400056c6162656c7371007e000b0000000000aa74000e646174615f7365745f74797065737874002e6578706572696d656e742d64657461696c732d677269642d53414d504c452d28616c6c292d5349524e415f4843537371007e00090000000f7704000000107371007e000b00010000009671007e00157371007e000b00010000009671007e00117371007e000b01000000009671007e00197371007e000b000100000096740007535542434f44457371007e000b00010000009671007e001d7371007e000b00010000009671007e001f7371007e000b00010000009674001249535f494e5354414e43455f53414d504c457371007e000b00010000009671007e002b7371007e000b00010000009671007e00217371007e000b0001000000c871007e00237371007e000b00010000009671007e00277371007e000b00000000009674000b5245474953545241544f527371007e000b00000000012c71007e00297371007e000b00010000009671007e003f7371007e000b00010000009671007e004178740022656e746974792d62726f777365722d677269642d4d4154455249414c2d4f4c49474f7371007e00090000000977040000000a7371007e000b01000000009671007e00197371007e000b00010000009674000d4d4154455249414c5f545950457371007e000b00010000009671007e00157371007e000b00010000009671007e00587371007e000b0001000000c871007e00297371007e000b01000000013474002170726f70657274792d555345522d4e55434c454f544944455f53455155454e43457371007e000b00010000007874001970726f70657274792d555345522d4445534352495054494f4e7371007e000b01000000007874001a70726f70657274792d555345522d494e48494249544f525f4f467371007e000b00010000007874001870726f70657274792d555345522d4c4942524152595f494478740021656e746974792d62726f777365722d677269642d4d4154455249414c2d47454e457371007e00090000000777040000000a7371007e000b01000000009671007e00197371007e000b00010000009671007e00607371007e000b00010000009671007e00157371007e000b00010000009671007e00587371007e000b01000000032171007e00677371007e000b00000000012871007e00297371007e000b00010000007871007e006b7874002273616d706c652d64657461696c732d677269642d444154415f5345542d504c4154457371007e0009000000157704000000197371007e000b01000000009671007e00197371007e000b00010000006471007e001b7371007e000b0001000000c871007e001d7371007e000b00010000009671007e001f7371007e000b00010000006471007e00217371007e000b00010000006471007e00237371007e000b00010000007871007e00257371007e000b00010000009671007e00277371007e000b0000000000c871007e00297371007e000b00010000009671007e002b7371007e000b00010000009671007e002d7371007e000b00010000009671007e002f7371007e000b00010000009671007e00317371007e000b0001000000c871007e00337371007e000b01000000009671007e00357371007e000b00000000009671007e00377371007e000b0001000000c871007e00397371007e000b00010000009671007e003b7371007e000b00010000009671007e003d7371007e000b00010000009671007e003f7371007e000b00010000009671007e00417874002673616d706c652d64657461696c732d677269642d53414d504c452d28616c6c292d504c4154457371007e00090000000f7704000000107371007e000b00010000009671007e00157371007e000b00010000009671007e00117371007e000b01000000009671007e00197371007e000b00010000009671007e004e7371007e000b00010000009671007e001d7371007e000b00010000009671007e001f7371007e000b00010000009671007e00527371007e000b00010000009671007e002b7371007e000b00010000009671007e00217371007e000b0001000000c871007e00237371007e000b00010000009671007e00277371007e000b00010000009671007e00587371007e000b00010000012c71007e00297371007e000b00010000009671007e003f7371007e000b00010000009671007e0041787800707070707372004f63682e73797374656d73782e636973642e6f70656e6269732e67656e657269632e7368617265642e62617369632e64746f2e5265616c4e756d626572466f726d6174696e67506172616d657465727300000000000000010200035a0010666f726d6174696e67456e61626c6564490009707265636973696f6e5a000a736369656e746966696378700100000004007070
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projects (id, code, space_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
1	TEST-PROJECT	1	\N	\N	3	2010-05-10 18:38:39.168462+02	2010-05-10 19:15:50.619+02
\.


--
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) FROM stdin;
1	DESCRIPTION	A Description	Description	1	2010-05-10 17:57:14.310868+02	1	\N	f	f	1	\N	\N	\N
3	NUCLEOTIDE_SEQUENCE	A sequence of nucleotides	Nucleotide Sequence	1	2008-02-28 13:03:03.358532+01	1	\N	f	f	1	\N	\N	\N
4	REFSEQ	NCBI Reference Sequence code, applicable to sequences of type: DNA, RNA, protein	RefSeq	1	2008-02-28 13:03:03.358532+01	1	\N	f	f	1	\N	\N	\N
13	MICROSCOPE	 	Microscope	7	2009-11-29 23:57:05.85618+01	1	3	f	f	1	\N	\N	\N
7	INHIBITOR_OF	Inhibitor Of	Inhibitor Of	8	2009-11-27 16:02:45.060699+01	1	\N	f	f	1	3	\N	\N
12	CONTROL	Control	Control	8	2009-11-29 23:56:37.355313+01	1	\N	f	f	1	5	\N	\N
6	PLATE_GEOMETRY	Plate Geometry	Plate Geometry	7	2008-06-17 16:38:30.723292+02	1	2	t	t	1	\N	\N	\N
15	NUMBER_OF_CHANNEL	 	Channels	3	2009-12-17 10:56:17.239319+01	1	\N	f	f	1	\N	\N	\N
101	LIBRARY_ID	ID in the library	Library ID	1	2010-05-10 19:11:46.382687+02	3	\N	f	f	1	\N	\N	\N
102	GENE	Inhibited gene	Gene	8	2010-10-29 13:39:25.49557+02	1	\N	f	f	1	3	\N	\N
103	GENE_SYMBOLS	Gene symbols	Gene symbols	1	2010-10-29 13:39:25.611453+02	1	\N	f	f	1	\N	\N	\N
11	SIRNA	Oligo	siRNA	8	2009-11-29 23:56:19.39967+01	1	\N	f	f	1	4	\N	\N
104	INSTRUMENT.ID	 	instrument.id	1	2011-03-21 17:26:10.565193+01	3	\N	f	f	1	\N	\N	\N
105	IBRAIN2.PROCESS.ID	 	ibrain2.process.id	3	2011-03-21 17:26:10.669498+01	3	\N	f	f	1	\N	\N	\N
106	IBRAIN2.PARENT.DATASET.ID	 	ibrain2.parent.dataset.id	3	2011-03-21 17:26:10.684883+01	3	\N	f	f	1	\N	\N	\N
107	IBRAIN2.WORKFLOW.ID	 	ibrain2.workflow.id	3	2011-03-21 17:26:10.702225+01	3	\N	f	f	1	\N	\N	\N
108	IBRAIN2.DATASET.ID	 	ibrain2.dataset.id	3	2011-03-21 17:26:10.733316+01	3	\N	f	f	1	\N	\N	\N
109	IBRAIN2.MODULE.ORDER	 	ibrain2.module.order	3	2011-03-21 17:26:10.74988+01	3	\N	f	f	1	\N	\N	\N
110	IBRAIN2.WORKFLOW.NAME	 	ibrain2.workflow.name	1	2011-03-21 17:26:10.766973+01	3	\N	f	f	1	\N	\N	\N
111	IBRAIN2.WORKFLOW.AUTHOR	 	ibrain2.workflow.author	1	2011-03-21 17:26:10.785663+01	3	\N	f	f	1	\N	\N	\N
\.


--
-- Data for Name: queries; Type: TABLE DATA; Schema: public; Owner: -
--

COPY queries (id, dbin_id, name, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, query_type, db_key, entity_type_code) FROM stdin;
\.


--
-- Data for Name: relationship_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY relationship_types (id, code, label, parent_label, child_label, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, dbin_id) FROM stdin;
1	PARENT_CHILD	Parent - Child	Parent	Child	Parent - Child relationship	2010-10-29 13:39:25.49557+02	1	t	t	1
2	PLATE_CONTROL_LAYOUT	Plate - Control Layout	Plate	Control Layout	Plate - Control Layout relationship	2010-10-29 13:39:25.49557+02	1	t	t	1
\.


--
-- Data for Name: role_assignments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY role_assignments (id, role_code, space_id, dbin_id, pers_id_grantee, ag_id_grantee, pers_id_registerer, registration_timestamp) FROM stdin;
2	ETL_SERVER	\N	1	2	\N	1	2010-05-10 18:26:28.944973+02
1	ADMIN	\N	1	3	\N	1	2010-05-10 17:57:26.252547+02
\.


--
-- Data for Name: sample_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_properties (id, samp_id, stpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: sample_relationships; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_relationships (id, sample_id_parent, relationship_id, sample_id_child) FROM stdin;
\.


--
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed, ordinal, section, script_id) FROM stdin;
8	7	11	f	f	1	2009-11-29 23:57:38.268212+01	t	1	\N	\N
9	8	12	f	f	1	2009-11-29 23:57:49.098187+01	t	1	\N	\N
10	3	6	f	f	1	2009-11-30 01:28:20.972263+01	t	1	\N	\N
101	7	102	f	f	1	2010-10-29 13:39:25.49557+02	t	2	\N	\N
\.


--
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique) FROM stdin;
8	CONTROL_WELL	\N	1	f	0	1	2009-11-27 19:42:25.791288+01	f	C	f
9	LIBRARY	\N	1	f	0	0	2009-11-27 19:42:25.791288+01	f	L	f
3	PLATE	Cell Plate	1	t	1	0	2009-11-27 16:02:26.451046+01	f	S	f
7	SIRNA_WELL	\N	1	f	0	1	2009-11-27 19:42:03.483115+01	f	O	f
\.


--
-- Data for Name: samples; Type: TABLE DATA; Schema: public; Owner: -
--

COPY samples (id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, inva_id, dbin_id, space_id, samp_id_part_of) FROM stdin;
\.


--
-- Data for Name: scripts; Type: TABLE DATA; Schema: public; Owner: -
--

COPY scripts (id, dbin_id, name, description, script, registration_timestamp, pers_id_registerer, entity_kind, script_type) FROM stdin;
\.


--
-- Data for Name: spaces; Type: TABLE DATA; Schema: public; Owner: -
--

COPY spaces (id, code, dbin_id, description, registration_timestamp, pers_id_registerer) FROM stdin;
1	TEST	1	\N	2010-05-10 18:28:21.87885+02	3
\.


--
-- Name: ag_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_bk_uk UNIQUE (code, dbin_id);


--
-- Name: ag_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pk PRIMARY KEY (id);


--
-- Name: agp_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pk PRIMARY KEY (pers_id, ag_id);


--
-- Name: atta_expe_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_bk_uk UNIQUE (expe_id, file_name, version);


--
-- Name: atta_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pk PRIMARY KEY (id);


--
-- Name: atta_proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_bk_uk UNIQUE (proj_id, file_name, version);


--
-- Name: atta_samp_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_bk_uk UNIQUE (samp_id, file_name, version);


--
-- Name: covo_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_internal_namespace, dbin_id);


--
-- Name: covo_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pk PRIMARY KEY (id);


--
-- Name: cvte_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_bk_uk UNIQUE (code, covo_id);


--
-- Name: cvte_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pk PRIMARY KEY (id);


--
-- Name: dast_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_bk_uk UNIQUE (code, dbin_id);


--
-- Name: dast_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_pk PRIMARY KEY (id);


--
-- Name: data_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_bk_uk UNIQUE (code);


--
-- Name: data_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_pk PRIMARY KEY (id);


--
-- Name: daty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_bk_uk UNIQUE (code);


--
-- Name: daty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_pk PRIMARY KEY (id);


--
-- Name: dbin_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_bk_uk UNIQUE (code);


--
-- Name: dbin_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_pk PRIMARY KEY (id);


--
-- Name: dbin_uuid_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_uuid_uk UNIQUE (uuid);


--
-- Name: dspr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_bk_uk UNIQUE (ds_id, dstpt_id);


--
-- Name: dspr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pk PRIMARY KEY (id);


--
-- Name: dsre_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent);


--
-- Name: dssdst_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_bk_uk UNIQUE (data_store_service_id, data_set_type_id);


--
-- Name: dsse_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_bk_uk UNIQUE (key, data_store_id);


--
-- Name: dsse_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_pk PRIMARY KEY (id);


--
-- Name: dstpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_bk_uk UNIQUE (dsty_id, prty_id);


--
-- Name: dstpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pk PRIMARY KEY (id);


--
-- Name: dsty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: dsty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_pk PRIMARY KEY (id);


--
-- Name: etpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_bk_uk UNIQUE (exty_id, prty_id);


--
-- Name: etpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pk PRIMARY KEY (id);


--
-- Name: evnt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pk PRIMARY KEY (id);


--
-- Name: exac_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachment_contents
    ADD CONSTRAINT exac_pk PRIMARY KEY (id);


--
-- Name: exda_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_bk_uk UNIQUE (location, loty_id);


--
-- Name: exda_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_pk PRIMARY KEY (data_id);


--
-- Name: expe_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);


--
-- Name: expe_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pi_uk UNIQUE (perm_id);


--
-- Name: expe_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pk PRIMARY KEY (id);


--
-- Name: expr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_bk_uk UNIQUE (expe_id, etpt_id);


--
-- Name: expr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pk PRIMARY KEY (id);


--
-- Name: exty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: exty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_pk PRIMARY KEY (id);


--
-- Name: ffty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: ffty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_pk PRIMARY KEY (id);


--
-- Name: filt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_bk_uk UNIQUE (name, dbin_id, grid_id);


--
-- Name: filt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pk PRIMARY KEY (id);


--
-- Name: grid_custom_columns_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_bk_uk UNIQUE (code, dbin_id, grid_id);


--
-- Name: grid_custom_columns_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pk PRIMARY KEY (id);


--
-- Name: inva_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pk PRIMARY KEY (id);


--
-- Name: loty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_bk_uk UNIQUE (code);


--
-- Name: loty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_pk PRIMARY KEY (id);


--
-- Name: mapr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_bk_uk UNIQUE (mate_id, mtpt_id);


--
-- Name: mapr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pk PRIMARY KEY (id);


--
-- Name: mate_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code, maty_id, dbin_id);


--
-- Name: mate_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);


--
-- Name: maty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: maty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);


--
-- Name: mtpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_bk_uk UNIQUE (maty_id, prty_id);


--
-- Name: mtpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pk PRIMARY KEY (id);


--
-- Name: pers_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_bk_uk UNIQUE (dbin_id, user_id);


--
-- Name: pers_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pk PRIMARY KEY (id);


--
-- Name: proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, space_id);


--
-- Name: proj_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);


--
-- Name: prty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_internal_namespace, dbin_id);


--
-- Name: prty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pk PRIMARY KEY (id);


--
-- Name: quer_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_bk_uk UNIQUE (name, dbin_id);


--
-- Name: quer_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_pk PRIMARY KEY (id);


--
-- Name: rety_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relationship_types
    ADD CONSTRAINT rety_pk PRIMARY KEY (id);


--
-- Name: rety_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relationship_types
    ADD CONSTRAINT rety_uk UNIQUE (code, dbin_id);


--
-- Name: roas_ag_instance_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_instance_bk_uk UNIQUE (ag_id_grantee, role_code, dbin_id);


--
-- Name: roas_ag_space_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_space_bk_uk UNIQUE (ag_id_grantee, role_code, space_id);


--
-- Name: roas_pe_instance_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pe_instance_bk_uk UNIQUE (pers_id_grantee, role_code, dbin_id);


--
-- Name: roas_pe_space_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pe_space_bk_uk UNIQUE (pers_id_grantee, role_code, space_id);


--
-- Name: roas_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pk PRIMARY KEY (id);


--
-- Name: samp_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pi_uk UNIQUE (perm_id);


--
-- Name: samp_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pk PRIMARY KEY (id);


--
-- Name: sapr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_bk_uk UNIQUE (samp_id, stpt_id);


--
-- Name: sapr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pk PRIMARY KEY (id);


--
-- Name: sare_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_relationships
    ADD CONSTRAINT sare_bk_uk UNIQUE (sample_id_child, sample_id_parent, relationship_id);


--
-- Name: sare_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_relationships
    ADD CONSTRAINT sare_pk PRIMARY KEY (id);


--
-- Name: saty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: saty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);


--
-- Name: scri_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_pk PRIMARY KEY (id);


--
-- Name: scri_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_uk UNIQUE (name, dbin_id);


--
-- Name: space_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_bk_uk UNIQUE (code, dbin_id);


--
-- Name: space_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_pk PRIMARY KEY (id);


--
-- Name: stpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_bk_uk UNIQUE (saty_id, prty_id);


--
-- Name: stpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pk PRIMARY KEY (id);


--
-- Name: atta_exac_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_exac_fk_i ON attachments USING btree (exac_id);


--
-- Name: atta_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_expe_fk_i ON attachments USING btree (expe_id);


--
-- Name: atta_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_pers_fk_i ON attachments USING btree (pers_id_registerer);


--
-- Name: atta_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_proj_fk_i ON attachments USING btree (proj_id);


--
-- Name: atta_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_samp_fk_i ON attachments USING btree (samp_id);


--
-- Name: covo_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX covo_pers_fk_i ON controlled_vocabularies USING btree (pers_id_registerer);


--
-- Name: cvte_covo_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cvte_covo_fk_i ON controlled_vocabulary_terms USING btree (covo_id);


--
-- Name: cvte_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cvte_pers_fk_i ON controlled_vocabulary_terms USING btree (pers_id_registerer);


--
-- Name: dast_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dast_dbin_fk_i ON data_stores USING btree (dbin_id);


--
-- Name: data_dsty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_dsty_fk_i ON data USING btree (dsty_id);


--
-- Name: data_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_expe_fk_i ON data USING btree (expe_id);


--
-- Name: data_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_samp_fk_i ON data USING btree (samp_id);


--
-- Name: dspr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_cvte_fk_i ON data_set_properties USING btree (cvte_id);


--
-- Name: dspr_ds_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_ds_fk_i ON data_set_properties USING btree (ds_id);


--
-- Name: dspr_dstpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_dstpt_fk_i ON data_set_properties USING btree (dstpt_id);


--
-- Name: dspr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_pers_fk_i ON data_set_properties USING btree (pers_id_registerer);


--
-- Name: dsre_data_fk_i_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_child ON data_set_relationships USING btree (data_id_child);


--
-- Name: dsre_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_parent ON data_set_relationships USING btree (data_id_parent);


--
-- Name: dssdst_ds_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dssdst_ds_fk_i ON data_store_service_data_set_types USING btree (data_store_service_id);


--
-- Name: dssdst_dst_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dssdst_dst_fk_i ON data_store_service_data_set_types USING btree (data_set_type_id);


--
-- Name: dsse_ds_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsse_ds_fk_i ON data_store_services USING btree (data_store_id);


--
-- Name: dstpt_dsty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_dsty_fk_i ON data_set_type_property_types USING btree (dsty_id);


--
-- Name: dstpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_pers_fk_i ON data_set_type_property_types USING btree (pers_id_registerer);


--
-- Name: dstpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_prty_fk_i ON data_set_type_property_types USING btree (prty_id);


--
-- Name: etpt_exty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_exty_fk_i ON experiment_type_property_types USING btree (exty_id);


--
-- Name: etpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_pers_fk_i ON experiment_type_property_types USING btree (pers_id_registerer);


--
-- Name: etpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_prty_fk_i ON experiment_type_property_types USING btree (prty_id);


--
-- Name: evnt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX evnt_pers_fk_i ON events USING btree (pers_id_registerer);


--
-- Name: exda_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_cvte_fk_i ON external_data USING btree (cvte_id_stor_fmt);


--
-- Name: exda_cvte_stored_on_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_cvte_stored_on_fk_i ON external_data USING btree (cvte_id_store);


--
-- Name: exda_ffty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_ffty_fk_i ON external_data USING btree (ffty_id);


--
-- Name: exda_loty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_loty_fk_i ON external_data USING btree (loty_id);


--
-- Name: expe_exty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_exty_fk_i ON experiments USING btree (exty_id);


--
-- Name: expe_inva_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_inva_fk_i ON experiments USING btree (inva_id);


--
-- Name: expe_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_mate_fk_i ON experiments USING btree (mate_id_study_object);


--
-- Name: expe_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_pers_fk_i ON experiments USING btree (pers_id_registerer);


--
-- Name: expe_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_proj_fk_i ON experiments USING btree (proj_id);


--
-- Name: expr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_cvte_fk_i ON experiment_properties USING btree (cvte_id);


--
-- Name: expr_etpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_etpt_fk_i ON experiment_properties USING btree (etpt_id);


--
-- Name: expr_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_expe_fk_i ON experiment_properties USING btree (expe_id);


--
-- Name: expr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_pers_fk_i ON experiment_properties USING btree (pers_id_registerer);


--
-- Name: filt_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX filt_dbin_fk_i ON filters USING btree (dbin_id);


--
-- Name: filt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX filt_pers_fk_i ON filters USING btree (pers_id_registerer);


--
-- Name: grid_custom_columns_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grid_custom_columns_dbin_fk_i ON grid_custom_columns USING btree (dbin_id);


--
-- Name: grid_custom_columns_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grid_custom_columns_pers_fk_i ON grid_custom_columns USING btree (pers_id_registerer);


--
-- Name: inva_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inva_pers_fk_i ON invalidations USING btree (pers_id_registerer);


--
-- Name: mapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_cvte_fk_i ON material_properties USING btree (cvte_id);


--
-- Name: mapr_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_mate_fk_i ON material_properties USING btree (mate_id);


--
-- Name: mapr_mtpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_mtpt_fk_i ON material_properties USING btree (mtpt_id);


--
-- Name: mapr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_pers_fk_i ON material_properties USING btree (pers_id_registerer);


--
-- Name: mate_maty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_maty_fk_i ON materials USING btree (maty_id);


--
-- Name: mate_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_pers_fk_i ON materials USING btree (pers_id_registerer);


--
-- Name: mtpt_maty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_maty_fk_i ON material_type_property_types USING btree (maty_id);


--
-- Name: mtpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_pers_fk_i ON material_type_property_types USING btree (pers_id_registerer);


--
-- Name: mtpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_prty_fk_i ON material_type_property_types USING btree (prty_id);


--
-- Name: pers_space_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pers_space_fk_i ON persons USING btree (space_id);


--
-- Name: proj_pers_fk_i_leader; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_pers_fk_i_leader ON projects USING btree (pers_id_leader);


--
-- Name: proj_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_pers_fk_i_registerer ON projects USING btree (pers_id_registerer);


--
-- Name: proj_space_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_space_fk_i ON projects USING btree (space_id);


--
-- Name: prty_covo_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_covo_fk_i ON property_types USING btree (covo_id);


--
-- Name: prty_daty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_daty_fk_i ON property_types USING btree (daty_id);


--
-- Name: prty_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_pers_fk_i ON property_types USING btree (pers_id_registerer);


--
-- Name: roas_ag_fk_i_grantee; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_ag_fk_i_grantee ON role_assignments USING btree (ag_id_grantee);


--
-- Name: roas_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_dbin_fk_i ON role_assignments USING btree (dbin_id);


--
-- Name: roas_pers_fk_i_grantee; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_pers_fk_i_grantee ON role_assignments USING btree (pers_id_grantee);


--
-- Name: roas_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_pers_fk_i_registerer ON role_assignments USING btree (pers_id_registerer);


--
-- Name: roas_space_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_space_fk_i ON role_assignments USING btree (space_id);


--
-- Name: samp_code_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_code_i ON samples USING btree (code);


--
-- Name: samp_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_expe_fk_i ON samples USING btree (expe_id);


--
-- Name: samp_inva_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_inva_fk_i ON samples USING btree (inva_id);


--
-- Name: samp_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_pers_fk_i ON samples USING btree (pers_id_registerer);


--
-- Name: samp_samp_fk_i_part_of; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_part_of ON samples USING btree (samp_id_part_of);


--
-- Name: samp_saty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_saty_fk_i ON samples USING btree (saty_id);


--
-- Name: sapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_cvte_fk_i ON sample_properties USING btree (cvte_id);


--
-- Name: sapr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_pers_fk_i ON sample_properties USING btree (pers_id_registerer);


--
-- Name: sapr_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_samp_fk_i ON sample_properties USING btree (samp_id);


--
-- Name: sapr_stpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_stpt_fk_i ON sample_properties USING btree (stpt_id);


--
-- Name: sare_data_fk_i_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sare_data_fk_i_child ON sample_relationships USING btree (sample_id_child);


--
-- Name: sare_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sare_data_fk_i_parent ON sample_relationships USING btree (sample_id_parent);


--
-- Name: sare_data_fk_i_relationship; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sare_data_fk_i_relationship ON sample_relationships USING btree (relationship_id);


--
-- Name: script_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX script_dbin_fk_i ON scripts USING btree (dbin_id);


--
-- Name: script_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX script_pers_fk_i ON scripts USING btree (pers_id_registerer);


--
-- Name: space_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX space_dbin_fk_i ON spaces USING btree (dbin_id);


--
-- Name: space_pers_registered_by_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX space_pers_registered_by_fk_i ON spaces USING btree (pers_id_registerer);


--
-- Name: stpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_pers_fk_i ON sample_type_property_types USING btree (pers_id_registerer);


--
-- Name: stpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_prty_fk_i ON sample_type_property_types USING btree (prty_id);


--
-- Name: stpt_saty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_saty_fk_i ON sample_type_property_types USING btree (saty_id);


--
-- Name: controlled_vocabulary_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER controlled_vocabulary_check BEFORE INSERT OR UPDATE ON property_types FOR EACH ROW EXECUTE PROCEDURE controlled_vocabulary_check();


--
-- Name: data_set_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON data_set_properties FOR EACH ROW EXECUTE PROCEDURE data_set_property_with_material_data_type_check();


--
-- Name: experiment_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON experiment_properties FOR EACH ROW EXECUTE PROCEDURE experiment_property_with_material_data_type_check();


--
-- Name: external_data_storage_format_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER external_data_storage_format_check BEFORE INSERT OR UPDATE ON external_data FOR EACH ROW EXECUTE PROCEDURE external_data_storage_format_check();


--
-- Name: material_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER material_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON material_properties FOR EACH ROW EXECUTE PROCEDURE material_property_with_material_data_type_check();


--
-- Name: sample_code_uniqueness_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_code_uniqueness_check BEFORE INSERT OR UPDATE ON samples FOR EACH ROW EXECUTE PROCEDURE sample_code_uniqueness_check();


--
-- Name: sample_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON sample_properties FOR EACH ROW EXECUTE PROCEDURE sample_property_with_material_data_type_check();


--
-- Name: sample_subcode_uniqueness_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_subcode_uniqueness_check BEFORE INSERT OR UPDATE ON samples FOR EACH ROW EXECUTE PROCEDURE sample_subcode_uniqueness_check();


--
-- Name: ag_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: ag_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: agp_ag_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_ag_fk FOREIGN KEY (ag_id) REFERENCES authorization_groups(id);


--
-- Name: agp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pers_fk FOREIGN KEY (pers_id) REFERENCES persons(id);


--
-- Name: atta_cont_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);


--
-- Name: atta_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: atta_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: atta_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);


--
-- Name: atta_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: covo_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: covo_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: cvte_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);


--
-- Name: cvte_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: dast_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: data_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);


--
-- Name: data_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);


--
-- Name: data_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: data_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: data_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: dspr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: dspr_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_ds_fk FOREIGN KEY (ds_id) REFERENCES data(id);


--
-- Name: dspr_dstpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES data_set_type_property_types(id) ON DELETE CASCADE;


--
-- Name: dspr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: dspr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: dsre_data_fk_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_child FOREIGN KEY (data_id_child) REFERENCES data(id);


--
-- Name: dsre_data_fk_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_parent FOREIGN KEY (data_id_parent) REFERENCES data(id);


--
-- Name: dssdst_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_ds_fk FOREIGN KEY (data_store_service_id) REFERENCES data_store_services(id) ON DELETE CASCADE;


--
-- Name: dssdst_dst_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_dst_fk FOREIGN KEY (data_set_type_id) REFERENCES data_set_types(id) ON DELETE CASCADE;


--
-- Name: dsse_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_ds_fk FOREIGN KEY (data_store_id) REFERENCES data_stores(id) ON DELETE CASCADE;


--
-- Name: dstpt_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id) ON DELETE CASCADE;


--
-- Name: dstpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: dstpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: dstpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);


--
-- Name: dsty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: etpt_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id) ON DELETE CASCADE;


--
-- Name: etpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: etpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: etpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);


--
-- Name: evnt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: exda_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_fk FOREIGN KEY (cvte_id_stor_fmt) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: exda_cvte_stored_on_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_stored_on_fk FOREIGN KEY (cvte_id_store) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: exda_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (data_id) REFERENCES data(id);


--
-- Name: exda_ffty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_ffty_fk FOREIGN KEY (ffty_id) REFERENCES file_format_types(id);


--
-- Name: exda_loty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_loty_fk FOREIGN KEY (loty_id) REFERENCES locator_types(id);


--
-- Name: expe_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);


--
-- Name: expe_inva_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);


--
-- Name: expe_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_mate_fk FOREIGN KEY (mate_id_study_object) REFERENCES materials(id);


--
-- Name: expe_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: expe_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);


--
-- Name: expr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: expr_etpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_etpt_fk FOREIGN KEY (etpt_id) REFERENCES experiment_type_property_types(id) ON DELETE CASCADE;


--
-- Name: expr_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: expr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: expr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: exty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: ffty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: filt_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: filt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: grid_custom_columns_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: grid_custom_columns_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: inva_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: mapr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: mapr_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);


--
-- Name: mapr_mtpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES material_type_property_types(id) ON DELETE CASCADE;


--
-- Name: mapr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mate_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: mate_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);


--
-- Name: mate_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: maty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: mtpt_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id) ON DELETE CASCADE;


--
-- Name: mtpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mtpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: mtpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);


--
-- Name: pers_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: pers_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: pers_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);


--
-- Name: proj_pers_fk_leader; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES persons(id);


--
-- Name: proj_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: proj_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);


--
-- Name: prty_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);


--
-- Name: prty_daty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_daty_fk FOREIGN KEY (daty_id) REFERENCES data_types(id);


--
-- Name: prty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: prty_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES material_types(id) ON DELETE CASCADE;


--
-- Name: prty_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: quer_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: quer_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: roas_ag_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_fk_grantee FOREIGN KEY (ag_id_grantee) REFERENCES authorization_groups(id);


--
-- Name: roas_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: roas_pers_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_grantee FOREIGN KEY (pers_id_grantee) REFERENCES persons(id);


--
-- Name: roas_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: roas_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);


--
-- Name: samp_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: samp_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: samp_inva_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);


--
-- Name: samp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: samp_samp_fk_part_of; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_part_of FOREIGN KEY (samp_id_part_of) REFERENCES samples(id);


--
-- Name: samp_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);


--
-- Name: samp_space_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);


--
-- Name: sapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: sapr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: sapr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: sapr_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: sapr_stpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;


--
-- Name: sare_data_fk_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships
    ADD CONSTRAINT sare_data_fk_child FOREIGN KEY (sample_id_child) REFERENCES samples(id) ON DELETE CASCADE;


--
-- Name: sare_data_fk_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships
    ADD CONSTRAINT sare_data_fk_parent FOREIGN KEY (sample_id_parent) REFERENCES samples(id) ON DELETE CASCADE;


--
-- Name: sare_data_fk_relationship; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_relationships
    ADD CONSTRAINT sare_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES relationship_types(id);


--
-- Name: saty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: scri_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: scri_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: space_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: space_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: stpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: stpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: stpt_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id) ON DELETE CASCADE;


--
-- Name: stpt_script_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: attachment_content_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE attachment_content_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE attachment_content_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE attachment_content_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE attachment_content_id_seq TO openbis_readonly;


--
-- Name: attachment_contents; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE attachment_contents FROM PUBLIC;
REVOKE ALL ON TABLE attachment_contents FROM tpylak;
GRANT ALL ON TABLE attachment_contents TO tpylak;
GRANT SELECT ON TABLE attachment_contents TO openbis_readonly;


--
-- Name: attachment_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE attachment_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE attachment_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE attachment_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE attachment_id_seq TO openbis_readonly;


--
-- Name: attachments; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE attachments FROM PUBLIC;
REVOKE ALL ON TABLE attachments FROM tpylak;
GRANT ALL ON TABLE attachments TO tpylak;
GRANT SELECT ON TABLE attachments TO openbis_readonly;


--
-- Name: authorization_group_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE authorization_group_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE authorization_group_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE authorization_group_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE authorization_group_id_seq TO openbis_readonly;


--
-- Name: authorization_group_persons; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE authorization_group_persons FROM PUBLIC;
REVOKE ALL ON TABLE authorization_group_persons FROM tpylak;
GRANT ALL ON TABLE authorization_group_persons TO tpylak;
GRANT SELECT ON TABLE authorization_group_persons TO openbis_readonly;


--
-- Name: authorization_groups; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE authorization_groups FROM PUBLIC;
REVOKE ALL ON TABLE authorization_groups FROM tpylak;
GRANT ALL ON TABLE authorization_groups TO tpylak;
GRANT SELECT ON TABLE authorization_groups TO openbis_readonly;


--
-- Name: code_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE code_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE code_seq FROM tpylak;
GRANT ALL ON SEQUENCE code_seq TO tpylak;
GRANT SELECT ON SEQUENCE code_seq TO openbis_readonly;


--
-- Name: controlled_vocabularies; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE controlled_vocabularies FROM PUBLIC;
REVOKE ALL ON TABLE controlled_vocabularies FROM tpylak;
GRANT ALL ON TABLE controlled_vocabularies TO tpylak;
GRANT SELECT ON TABLE controlled_vocabularies TO openbis_readonly;


--
-- Name: controlled_vocabulary_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE controlled_vocabulary_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE controlled_vocabulary_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE controlled_vocabulary_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE controlled_vocabulary_id_seq TO openbis_readonly;


--
-- Name: controlled_vocabulary_terms; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE controlled_vocabulary_terms FROM PUBLIC;
REVOKE ALL ON TABLE controlled_vocabulary_terms FROM tpylak;
GRANT ALL ON TABLE controlled_vocabulary_terms TO tpylak;
GRANT SELECT ON TABLE controlled_vocabulary_terms TO openbis_readonly;


--
-- Name: cvte_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE cvte_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE cvte_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE cvte_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE cvte_id_seq TO openbis_readonly;


--
-- Name: data; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data FROM PUBLIC;
REVOKE ALL ON TABLE data FROM tpylak;
GRANT ALL ON TABLE data TO tpylak;
GRANT SELECT ON TABLE data TO openbis_readonly;


--
-- Name: data_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE data_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE data_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE data_id_seq TO openbis_readonly;


--
-- Name: data_set_properties; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data_set_properties FROM PUBLIC;
REVOKE ALL ON TABLE data_set_properties FROM tpylak;
GRANT ALL ON TABLE data_set_properties TO tpylak;
GRANT SELECT ON TABLE data_set_properties TO openbis_readonly;


--
-- Name: data_set_property_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE data_set_property_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_set_property_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE data_set_property_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE data_set_property_id_seq TO openbis_readonly;


--
-- Name: data_set_relationship_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE data_set_relationship_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_set_relationship_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE data_set_relationship_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE data_set_relationship_id_seq TO openbis_readonly;


--
-- Name: data_set_relationships; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data_set_relationships FROM PUBLIC;
REVOKE ALL ON TABLE data_set_relationships FROM tpylak;
GRANT ALL ON TABLE data_set_relationships TO tpylak;
GRANT SELECT ON TABLE data_set_relationships TO openbis_readonly;


--
-- Name: data_set_type_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE data_set_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_set_type_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE data_set_type_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE data_set_type_id_seq TO openbis_readonly;


--
-- Name: data_set_type_property_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data_set_type_property_types FROM PUBLIC;
REVOKE ALL ON TABLE data_set_type_property_types FROM tpylak;
GRANT ALL ON TABLE data_set_type_property_types TO tpylak;
GRANT SELECT ON TABLE data_set_type_property_types TO openbis_readonly;


--
-- Name: data_set_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data_set_types FROM PUBLIC;
REVOKE ALL ON TABLE data_set_types FROM tpylak;
GRANT ALL ON TABLE data_set_types TO tpylak;
GRANT SELECT ON TABLE data_set_types TO openbis_readonly;


--
-- Name: data_store_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE data_store_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_store_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE data_store_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE data_store_id_seq TO openbis_readonly;


--
-- Name: data_store_service_data_set_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data_store_service_data_set_types FROM PUBLIC;
REVOKE ALL ON TABLE data_store_service_data_set_types FROM tpylak;
GRANT ALL ON TABLE data_store_service_data_set_types TO tpylak;
GRANT SELECT ON TABLE data_store_service_data_set_types TO openbis_readonly;


--
-- Name: data_store_services; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data_store_services FROM PUBLIC;
REVOKE ALL ON TABLE data_store_services FROM tpylak;
GRANT ALL ON TABLE data_store_services TO tpylak;
GRANT SELECT ON TABLE data_store_services TO openbis_readonly;


--
-- Name: data_store_services_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE data_store_services_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_store_services_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE data_store_services_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE data_store_services_id_seq TO openbis_readonly;


--
-- Name: data_stores; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data_stores FROM PUBLIC;
REVOKE ALL ON TABLE data_stores FROM tpylak;
GRANT ALL ON TABLE data_stores TO tpylak;
GRANT SELECT ON TABLE data_stores TO openbis_readonly;


--
-- Name: data_type_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE data_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_type_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE data_type_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE data_type_id_seq TO openbis_readonly;


--
-- Name: data_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE data_types FROM PUBLIC;
REVOKE ALL ON TABLE data_types FROM tpylak;
GRANT ALL ON TABLE data_types TO tpylak;
GRANT SELECT ON TABLE data_types TO openbis_readonly;


--
-- Name: database_instance_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE database_instance_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE database_instance_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE database_instance_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE database_instance_id_seq TO openbis_readonly;


--
-- Name: database_instances; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE database_instances FROM PUBLIC;
REVOKE ALL ON TABLE database_instances FROM tpylak;
GRANT ALL ON TABLE database_instances TO tpylak;
GRANT SELECT ON TABLE database_instances TO openbis_readonly;


--
-- Name: database_version_logs; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE database_version_logs FROM PUBLIC;
REVOKE ALL ON TABLE database_version_logs FROM tpylak;
GRANT ALL ON TABLE database_version_logs TO tpylak;
GRANT SELECT ON TABLE database_version_logs TO openbis_readonly;


--
-- Name: dstpt_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE dstpt_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE dstpt_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE dstpt_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE dstpt_id_seq TO openbis_readonly;


--
-- Name: etpt_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE etpt_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE etpt_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE etpt_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE etpt_id_seq TO openbis_readonly;


--
-- Name: event_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE event_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE event_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE event_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE event_id_seq TO openbis_readonly;


--
-- Name: events; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE events FROM PUBLIC;
REVOKE ALL ON TABLE events FROM tpylak;
GRANT ALL ON TABLE events TO tpylak;
GRANT SELECT ON TABLE events TO openbis_readonly;


--
-- Name: experiment_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE experiment_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE experiment_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE experiment_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE experiment_id_seq TO openbis_readonly;


--
-- Name: experiment_properties; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE experiment_properties FROM PUBLIC;
REVOKE ALL ON TABLE experiment_properties FROM tpylak;
GRANT ALL ON TABLE experiment_properties TO tpylak;
GRANT SELECT ON TABLE experiment_properties TO openbis_readonly;


--
-- Name: experiment_property_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE experiment_property_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE experiment_property_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE experiment_property_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE experiment_property_id_seq TO openbis_readonly;


--
-- Name: experiment_type_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE experiment_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE experiment_type_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE experiment_type_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE experiment_type_id_seq TO openbis_readonly;


--
-- Name: experiment_type_property_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE experiment_type_property_types FROM PUBLIC;
REVOKE ALL ON TABLE experiment_type_property_types FROM tpylak;
GRANT ALL ON TABLE experiment_type_property_types TO tpylak;
GRANT SELECT ON TABLE experiment_type_property_types TO openbis_readonly;


--
-- Name: experiment_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE experiment_types FROM PUBLIC;
REVOKE ALL ON TABLE experiment_types FROM tpylak;
GRANT ALL ON TABLE experiment_types TO tpylak;
GRANT SELECT ON TABLE experiment_types TO openbis_readonly;


--
-- Name: experiments; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE experiments FROM PUBLIC;
REVOKE ALL ON TABLE experiments FROM tpylak;
GRANT ALL ON TABLE experiments TO tpylak;
GRANT SELECT ON TABLE experiments TO openbis_readonly;


--
-- Name: external_data; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE external_data FROM PUBLIC;
REVOKE ALL ON TABLE external_data FROM tpylak;
GRANT ALL ON TABLE external_data TO tpylak;
GRANT SELECT ON TABLE external_data TO openbis_readonly;


--
-- Name: file_format_type_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE file_format_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE file_format_type_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE file_format_type_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE file_format_type_id_seq TO openbis_readonly;


--
-- Name: file_format_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE file_format_types FROM PUBLIC;
REVOKE ALL ON TABLE file_format_types FROM tpylak;
GRANT ALL ON TABLE file_format_types TO tpylak;
GRANT SELECT ON TABLE file_format_types TO openbis_readonly;


--
-- Name: filter_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE filter_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE filter_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE filter_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE filter_id_seq TO openbis_readonly;


--
-- Name: filters; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE filters FROM PUBLIC;
REVOKE ALL ON TABLE filters FROM tpylak;
GRANT ALL ON TABLE filters TO tpylak;
GRANT SELECT ON TABLE filters TO openbis_readonly;


--
-- Name: invalidation_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE invalidation_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE invalidation_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE invalidation_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE invalidation_id_seq TO openbis_readonly;


--
-- Name: invalidations; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE invalidations FROM PUBLIC;
REVOKE ALL ON TABLE invalidations FROM tpylak;
GRANT ALL ON TABLE invalidations TO tpylak;
GRANT SELECT ON TABLE invalidations TO openbis_readonly;


--
-- Name: locator_type_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE locator_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE locator_type_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE locator_type_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE locator_type_id_seq TO openbis_readonly;


--
-- Name: locator_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE locator_types FROM PUBLIC;
REVOKE ALL ON TABLE locator_types FROM tpylak;
GRANT ALL ON TABLE locator_types TO tpylak;
GRANT SELECT ON TABLE locator_types TO openbis_readonly;


--
-- Name: material_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE material_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE material_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE material_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE material_id_seq TO openbis_readonly;


--
-- Name: material_properties; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE material_properties FROM PUBLIC;
REVOKE ALL ON TABLE material_properties FROM tpylak;
GRANT ALL ON TABLE material_properties TO tpylak;
GRANT SELECT ON TABLE material_properties TO openbis_readonly;


--
-- Name: material_property_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE material_property_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE material_property_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE material_property_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE material_property_id_seq TO openbis_readonly;


--
-- Name: material_type_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE material_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE material_type_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE material_type_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE material_type_id_seq TO openbis_readonly;


--
-- Name: material_type_property_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE material_type_property_types FROM PUBLIC;
REVOKE ALL ON TABLE material_type_property_types FROM tpylak;
GRANT ALL ON TABLE material_type_property_types TO tpylak;
GRANT SELECT ON TABLE material_type_property_types TO openbis_readonly;


--
-- Name: material_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE material_types FROM PUBLIC;
REVOKE ALL ON TABLE material_types FROM tpylak;
GRANT ALL ON TABLE material_types TO tpylak;
GRANT SELECT ON TABLE material_types TO openbis_readonly;


--
-- Name: materials; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE materials FROM PUBLIC;
REVOKE ALL ON TABLE materials FROM tpylak;
GRANT ALL ON TABLE materials TO tpylak;
GRANT SELECT ON TABLE materials TO openbis_readonly;


--
-- Name: mtpt_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE mtpt_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE mtpt_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE mtpt_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE mtpt_id_seq TO openbis_readonly;


--
-- Name: perm_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE perm_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE perm_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE perm_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE perm_id_seq TO openbis_readonly;


--
-- Name: person_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE person_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE person_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE person_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE person_id_seq TO openbis_readonly;


--
-- Name: persons; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE persons FROM PUBLIC;
REVOKE ALL ON TABLE persons FROM tpylak;
GRANT ALL ON TABLE persons TO tpylak;
GRANT SELECT ON TABLE persons TO openbis_readonly;


--
-- Name: project_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE project_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE project_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE project_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE project_id_seq TO openbis_readonly;


--
-- Name: projects; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE projects FROM PUBLIC;
REVOKE ALL ON TABLE projects FROM tpylak;
GRANT ALL ON TABLE projects TO tpylak;
GRANT SELECT ON TABLE projects TO openbis_readonly;


--
-- Name: property_type_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE property_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE property_type_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE property_type_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE property_type_id_seq TO openbis_readonly;


--
-- Name: property_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE property_types FROM PUBLIC;
REVOKE ALL ON TABLE property_types FROM tpylak;
GRANT ALL ON TABLE property_types TO tpylak;
GRANT SELECT ON TABLE property_types TO openbis_readonly;


--
-- Name: queries; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE queries FROM PUBLIC;
REVOKE ALL ON TABLE queries FROM tpylak;
GRANT ALL ON TABLE queries TO tpylak;
GRANT SELECT ON TABLE queries TO openbis_readonly;


--
-- Name: query_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE query_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE query_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE query_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE query_id_seq TO openbis_readonly;


--
-- Name: role_assignment_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE role_assignment_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE role_assignment_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE role_assignment_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE role_assignment_id_seq TO openbis_readonly;


--
-- Name: role_assignments; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE role_assignments FROM PUBLIC;
REVOKE ALL ON TABLE role_assignments FROM tpylak;
GRANT ALL ON TABLE role_assignments TO tpylak;
GRANT SELECT ON TABLE role_assignments TO openbis_readonly;


--
-- Name: sample_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE sample_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE sample_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE sample_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE sample_id_seq TO openbis_readonly;


--
-- Name: sample_properties; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE sample_properties FROM PUBLIC;
REVOKE ALL ON TABLE sample_properties FROM tpylak;
GRANT ALL ON TABLE sample_properties TO tpylak;
GRANT SELECT ON TABLE sample_properties TO openbis_readonly;


--
-- Name: sample_property_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE sample_property_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE sample_property_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE sample_property_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE sample_property_id_seq TO openbis_readonly;


--
-- Name: sample_type_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE sample_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE sample_type_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE sample_type_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE sample_type_id_seq TO openbis_readonly;


--
-- Name: sample_type_property_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE sample_type_property_types FROM PUBLIC;
REVOKE ALL ON TABLE sample_type_property_types FROM tpylak;
GRANT ALL ON TABLE sample_type_property_types TO tpylak;
GRANT SELECT ON TABLE sample_type_property_types TO openbis_readonly;


--
-- Name: sample_types; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE sample_types FROM PUBLIC;
REVOKE ALL ON TABLE sample_types FROM tpylak;
GRANT ALL ON TABLE sample_types TO tpylak;
GRANT SELECT ON TABLE sample_types TO openbis_readonly;


--
-- Name: samples; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE samples FROM PUBLIC;
REVOKE ALL ON TABLE samples FROM tpylak;
GRANT ALL ON TABLE samples TO tpylak;
GRANT SELECT ON TABLE samples TO openbis_readonly;


--
-- Name: scripts; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE scripts FROM PUBLIC;
REVOKE ALL ON TABLE scripts FROM tpylak;
GRANT ALL ON TABLE scripts TO tpylak;
GRANT SELECT ON TABLE scripts TO openbis_readonly;


--
-- Name: space_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE space_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE space_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE space_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE space_id_seq TO openbis_readonly;


--
-- Name: spaces; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON TABLE spaces FROM PUBLIC;
REVOKE ALL ON TABLE spaces FROM tpylak;
GRANT ALL ON TABLE spaces TO tpylak;
GRANT SELECT ON TABLE spaces TO openbis_readonly;


--
-- Name: stpt_id_seq; Type: ACL; Schema: public; Owner: -
--

REVOKE ALL ON SEQUENCE stpt_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE stpt_id_seq FROM tpylak;
GRANT ALL ON SEQUENCE stpt_id_seq TO tpylak;
GRANT SELECT ON SEQUENCE stpt_id_seq TO openbis_readonly;


--
-- PostgreSQL database dump complete
--

