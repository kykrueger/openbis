-- Migration from 114 to 115

ALTER DOMAIN DATA_SET_KIND DROP CONSTRAINT data_set_kind_check;
UPDATE DATA_SET_TYPES SET DATA_SET_KIND = 'PHYSICAL' WHERE DATA_SET_KIND = 'EXTERNAL';
ALTER TABLE DATA_SET_TYPES ALTER COLUMN DATA_SET_KIND SET DEFAULT 'PHYSICAL';
ALTER DOMAIN DATA_SET_KIND ADD CHECK (VALUE IN ('PHYSICAL', 'LINK', 'CONTAINER'));
