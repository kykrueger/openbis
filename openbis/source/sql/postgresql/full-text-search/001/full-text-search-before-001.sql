-- Disabling triggers

-- Samples

ALTER TABLE samples_all
    DISABLE TRIGGER ALL;

ALTER TABLE sample_properties
    DISABLE TRIGGER ALL;

-- Experiments

ALTER TABLE experiments_all
    DISABLE TRIGGER ALL;

ALTER TABLE experiment_properties
    DISABLE TRIGGER ALL;

-- Data sets

ALTER TABLE data_all
    DISABLE TRIGGER ALL;

ALTER TABLE data_set_properties
    DISABLE TRIGGER ALL;

-- Projects

ALTER TABLE projects
    DISABLE TRIGGER ALL;
