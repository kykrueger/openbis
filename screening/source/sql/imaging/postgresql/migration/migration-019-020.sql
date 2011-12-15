-- Migration from 019 to 020

ALTER TABLE IMAGE_ZOOM_LEVELS ADD COLUMN COLOR_DEPTH INTEGER;
ALTER TABLE IMAGE_ZOOM_LEVELS ADD COLUMN FILE_TYPE VARCHAR(20);
ALTER TABLE IMAGE_ZOOM_LEVELS ADD COLUMN IMAGE_TRANSFORMATION_ID TECH_ID;

ALTER TABLE IMAGE_ZOOM_LEVELS ADD CONSTRAINT FK_IMAGE_ZOOM_LEVELS_2 FOREIGN KEY (IMAGE_TRANSFORMATION_ID) REFERENCES IMAGE_TRANSFORMATIONS (ID);