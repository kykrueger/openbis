-- Sample relationships

DELETE FROM sample_relationships_all
WHERE id=10001;

DELETE FROM sample_relationships_all
WHERE id=10002;

DELETE FROM sample_relationships_all
WHERE id=10003;

DELETE FROM relationship_types
WHERE id=10001;

-- Tables related to samples

DELETE FROM property_types
WHERE id=4002;

DELETE FROM sample_properties
WHERE id=1001;

DELETE FROM sample_type_property_types
WHERE id=2001;

DELETE FROM sample_types
WHERE id=3001;


DELETE FROM property_types
WHERE id=4001;

DELETE FROM sample_properties
WHERE id=1002;

DELETE FROM sample_type_property_types
WHERE id=2002;

DELETE FROM sample_types
WHERE id=3002;


DELETE FROM property_types
WHERE id=4003;

DELETE FROM sample_properties
WHERE id=1003;

DELETE FROM sample_type_property_types
WHERE id=2003;

DELETE FROM sample_types
WHERE id=3003;


DELETE FROM property_types
WHERE id=4004;

DELETE FROM sample_properties
WHERE id=1004;

DELETE FROM sample_type_property_types
WHERE id=2004;

DELETE FROM sample_types
WHERE id=3004;


DELETE FROM property_types
WHERE id=4005;

DELETE FROM sample_properties
WHERE id=1005;

DELETE FROM sample_type_property_types
WHERE id=2005;

DELETE FROM sample_types
WHERE id=3005;

-- Sample types

DELETE FROM sample_types
WHERE id=3101;

DELETE FROM sample_types
WHERE id=3102;

-- Samples

DELETE FROM samples_all
WHERE id=1006;

DELETE FROM samples_all
WHERE id=1005;

DELETE FROM samples_all
WHERE id=1004;

DELETE FROM samples_all
WHERE id=1003;

DELETE FROM samples_all
WHERE id=1002;

DELETE FROM samples_all
WHERE id=1001;


DELETE FROM experiments_all
WHERE id=10003;

DELETE FROM experiment_types
WHERE id=10004;

DELETE FROM projects
WHERE id=10002;


-- Spaces

DELETE FROM spaces
WHERE id=10000;

DELETE FROM spaces
WHERE id=10001;

-- Persons

DELETE FROM persons
WHERE id=101;

DELETE FROM persons
WHERE id=102;