-- a little workaround as it is not possible to alter domain type directly

CREATE DOMAIN TEMP_CODE AS VARCHAR(40);
ALTER TABLE DATA_SETS ALTER COLUMN CODE TYPE TEMP_CODE;
DROP DOMAIN CODE;
CREATE DOMAIN CODE AS VARCHAR(60);
ALTER TABLE DATA_SETS ALTER COLUMN CODE TYPE CODE;
DROP DOMAIN TEMP_CODE;