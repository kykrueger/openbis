-- Migration from 057 to 058

ALTER TABLE DATA ADD COLUMN PERS_ID_REGISTERER TECH_ID;
ALTER TABLE DATA ADD CONSTRAINT DATA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
