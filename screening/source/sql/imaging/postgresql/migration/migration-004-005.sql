-- Migration from 004 to 005

CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;
ALTER TABLE DATA_SETS ADD COLUMN IS_MULTIDIMENSIONAL BOOLEAN_CHAR NOT NULL;

-- allows to select one spot of the container quicker
CREATE INDEX SPOTS_COORDS_IDX ON SPOTS(CONT_ID, X, Y);

ALTER TABLE SPOTS DROP COLUMN PERM_ID;