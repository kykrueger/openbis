-- drop existing definitions
DROP TABLE IF EXISTS OPERATION_EXECUTIONS;
DROP DOMAIN IF EXISTS OPERATION_EXECUTION_STATE;

-- availability domain
CREATE DOMAIN OPERATION_EXECUTION_STATE AS VARCHAR(40) CHECK (VALUE IN ('NEW', 'SCHEDULED', 'RUNNING', 'FINISHED', 'FAILED'));
CREATE DOMAIN OPERATION_EXECUTION_AVAILABILITY AS VARCHAR(40) CHECK (VALUE IN ('AVAILABLE','DELETE_PENDING','DELETED','TIME_OUT_PENDING','TIMED_OUT'));

-- table
CREATE TABLE OPERATION_EXECUTIONS (
	ID TECH_ID NOT NULL, 
	CODE CODE NOT NULL,
	STATE OPERATION_EXECUTION_STATE NOT NULL DEFAULT 'NEW',
	OWNER TECH_ID NOT NULL,
	DESCRIPTION TEXT_VALUE,
	AVAILABILITY OPERATION_EXECUTION_AVAILABILITY NOT NULL DEFAULT 'AVAILABLE',
	AVAILABILITY_TIME BIGINT NOT NULL DEFAULT 1,
	SUMMARY_OPERATIONS TEXT_VALUE,
	SUMMARY_PROGRESS TEXT_VALUE,
	SUMMARY_ERROR TEXT_VALUE,
	SUMMARY_RESULTS TEXT_VALUE,
	SUMMARY_AVAILABILITY OPERATION_EXECUTION_AVAILABILITY NOT NULL DEFAULT 'AVAILABLE',
	SUMMARY_AVAILABILITY_TIME BIGINT NOT NULL DEFAULT 1,
	DETAILS_PATH VARCHAR(1000),
	DETAILS_AVAILABILITY OPERATION_EXECUTION_AVAILABILITY NOT NULL DEFAULT 'AVAILABLE',
	DETAILS_AVAILABILITY_TIME BIGINT NOT NULL DEFAULT 1,
	CREATION_DATE TIME_STAMP_DFL NOT NULL, 
	START_DATE TIME_STAMP, 
	FINISH_DATE TIME_STAMP
);

-- pk
ALTER TABLE OPERATION_EXECUTIONS ADD CONSTRAINT OPERATION_EXECUTIONS_PK PRIMARY KEY(ID);

-- fk
ALTER TABLE OPERATION_EXECUTIONS ADD CONSTRAINT OPERATION_EXECUTIONS_OWNER_FK FOREIGN KEY (OWNER) REFERENCES PERSONS(ID) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

-- code unique constraint
ALTER TABLE OPERATION_EXECUTIONS ADD CONSTRAINT OPERATION_EXECUTIONS_CODE_UK UNIQUE (CODE);

-- code index
CREATE INDEX OPERATION_EXECUTIONS_CODE_I ON OPERATION_EXECUTIONS (CODE);

-- owner index
CREATE INDEX OPERATION_EXECUTIONS_OWNER_I ON OPERATION_EXECUTIONS (OWNER);

-- availabilty index 
CREATE INDEX OPERATION_EXECUTIONS_AVAILABILITY_I ON OPERATION_EXECUTIONS (AVAILABILITY);

-- summary_availability
CREATE INDEX OPERATION_EXECUTIONS_SUMMARY_AVAILABILITY_I ON OPERATION_EXECUTIONS (SUMMARY_AVAILABILITY);

-- details_availability
CREATE INDEX OPERATION_EXECUTIONS_DETAILS_AVAILABILITY_I ON OPERATION_EXECUTIONS (DETAILS_AVAILABILITY);

-- checks
ALTER TABLE OPERATION_EXECUTIONS ADD CONSTRAINT OPERATION_EXECUTIONS_STATE_START_DATE_CHECK CHECK (
	(STATE IN ('NEW','SCHEDULED') AND START_DATE IS NULL) OR 
	(STATE IN ('RUNNING','FINISHED','FAILED') AND START_DATE IS NOT NULL)
);

ALTER TABLE OPERATION_EXECUTIONS ADD CONSTRAINT OPERATION_EXECUTIONS_STATE_FINISH_DATE_CHECK CHECK (
	(STATE IN ('NEW','SCHEDULED','RUNNING') AND FINISH_DATE IS NULL) OR 
	(STATE IN ('FINISHED','FAILED') AND FINISH_DATE IS NOT NULL)
);

-- grant
GRANT SELECT ON TABLE OPERATION_EXECUTIONS TO GROUP OPENBIS_READONLY;
