-- Migration from 070 to 071
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD COLUMN IS_OFFICIAL BOOLEAN_CHAR NOT NULL DEFAULT 'T';
