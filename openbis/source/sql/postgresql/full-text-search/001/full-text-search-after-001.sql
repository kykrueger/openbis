-- Enabling triggers

-- Samples

ALTER TABLE samples_all
    ENABLE TRIGGER ALL;

ALTER TABLE sample_properties
    ENABLE TRIGGER ALL;

-- Experiments

ALTER TABLE experiments_all
    ENABLE TRIGGER ALL;

ALTER TABLE experiment_properties
    ENABLE TRIGGER ALL;

-- Data sets

ALTER TABLE data_all
    ENABLE TRIGGER ALL;

ALTER TABLE data_set_properties
    ENABLE TRIGGER ALL;

-- Projects

ALTER TABLE projects
    ENABLE TRIGGER ALL;
