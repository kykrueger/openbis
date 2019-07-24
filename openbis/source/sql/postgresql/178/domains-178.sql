-- Creating domains

CREATE DOMAIN AUTHORIZATION_ROLE AS VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'POWER_USER', 'USER', 'OBSERVER', 'ETL_SERVER'));
CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;
CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) DEFAULT 'U' CHECK (VALUE IN ('F', 'T', 'U'));
CREATE DOMAIN CODE AS VARCHAR(100);
CREATE DOMAIN COLUMN_LABEL AS VARCHAR(128);
CREATE DOMAIN DATA_STORE_SERVICE_KIND AS VARCHAR(40) CHECK (VALUE IN ('PROCESSING', 'QUERIES'));
CREATE DOMAIN DATA_STORE_SERVICE_REPORTING_PLUGIN_TYPE AS VARCHAR(40) CHECK (VALUE IN ('TABLE_MODEL', 'DSS_LINK', 'AGGREGATION_TABLE_MODEL'));
CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE IN ('DELETION', 'MOVEMENT', 'FREEZING'));
CREATE DOMAIN FILE AS BYTEA;
CREATE DOMAIN FILE_NAME AS VARCHAR(255);
CREATE DOMAIN TEXT_VALUE AS TEXT;
CREATE DOMAIN OBJECT_NAME AS VARCHAR(50);
CREATE DOMAIN REAL_VALUE AS REAL;
CREATE DOMAIN TECH_ID AS BIGINT;
CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;
CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE DOMAIN USER_ID AS VARCHAR(50);
CREATE DOMAIN TITLE_100 AS VARCHAR(100);
CREATE DOMAIN GRID_EXPRESSION AS VARCHAR(2000);
CREATE DOMAIN GRID_ID AS VARCHAR(200);
CREATE DOMAIN ORDINAL_INT AS BIGINT CHECK (VALUE > 0);
CREATE DOMAIN DESCRIPTION_2000 AS VARCHAR(2000);
CREATE DOMAIN ARCHIVING_STATUS AS VARCHAR(100) CHECK (VALUE IN ('LOCKED', 'AVAILABLE', 'ARCHIVED', 'ARCHIVE_PENDING', 'UNARCHIVE_PENDING', 'BACKUP_PENDING'));
CREATE DOMAIN QUERY_TYPE AS VARCHAR(40) CHECK (VALUE IN ('GENERIC', 'EXPERIMENT', 'SAMPLE', 'DATA_SET', 'MATERIAL'));
CREATE DOMAIN ENTITY_KIND AS VARCHAR(40) CHECK (VALUE IN ('SAMPLE', 'EXPERIMENT', 'DATA_SET', 'MATERIAL'));
CREATE DOMAIN SCRIPT_TYPE AS VARCHAR(40) CHECK (VALUE IN ('DYNAMIC_PROPERTY', 'MANAGED_PROPERTY', 'ENTITY_VALIDATION'));
CREATE DOMAIN IDENTIFIER AS VARCHAR(200);
CREATE DOMAIN DATA_SET_KIND AS VARCHAR(40) CHECK (VALUE IN ('PHYSICAL', 'LINK', 'CONTAINER'));
CREATE DOMAIN PLUGIN_TYPE AS VARCHAR(40) CHECK (VALUE IN ('JYTHON', 'PREDEPLOYED'));
CREATE DOMAIN OPERATION_EXECUTION_STATE AS VARCHAR(40) CHECK (VALUE IN ('NEW', 'SCHEDULED', 'RUNNING', 'FINISHED', 'FAILED'));
CREATE DOMAIN OPERATION_EXECUTION_AVAILABILITY AS VARCHAR(40) CHECK (VALUE IN ('AVAILABLE','DELETE_PENDING','DELETED','TIME_OUT_PENDING','TIMED_OUT'));
CREATE DOMAIN EDMS_ADDRESS_TYPE AS TEXT CHECK (VALUE IN ('OPENBIS', 'URL', 'FILE_SYSTEM'));
CREATE DOMAIN LOCATION_TYPE AS TEXT CHECK (VALUE IN ('OPENBIS', 'URL', 'FILE_SYSTEM_PLAIN', 'FILE_SYSTEM_GIT'));