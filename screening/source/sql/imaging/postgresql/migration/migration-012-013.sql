-- Migration from 012 to 013

ALTER TABLE IMAGES ADD COLUMN IMAGE_ID CODE;

UPDATE IMAGES SET IMAGE_ID = '0-' || PAGE || '-0-0' WHERE PAGE IS NOT NULL;  
           
ALTER TABLE IMAGES DROP COLUMN PAGE;
