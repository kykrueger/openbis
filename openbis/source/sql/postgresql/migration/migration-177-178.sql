-- Freezing checks for deletion
-- from space ----------------
CREATE OR REPLACE FUNCTION RAISE_DELETE_FROM_SPACE_EXCEPTION() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because space % is frozen.', TG_ARGV[0], 
        (select code from spaces where id = old.space_id);
END;
$$ LANGUAGE 'plpgsql';

-- Project from space deletion
DROP TRIGGER IF EXISTS DELETE_PROJECT_FROM_SPACE_CHECK ON PROJECTS;
CREATE TRIGGER DELETE_PROJECT_FROM_SPACE_CHECK AFTER DELETE ON PROJECTS
    FOR EACH ROW WHEN (OLD.SPACE_FROZEN)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SPACE_EXCEPTION('PROJECT');

-- Sample from space deleting
DROP TRIGGER IF EXISTS TRASH_SAMPLE_FROM_SPACE_CHECK ON SAMPLES_ALL;
CREATE TRIGGER TRASH_SAMPLE_FROM_SPACE_CHECK AFTER UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.space_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SPACE_EXCEPTION('SAMPLE');

DROP TRIGGER IF EXISTS DELETE_SAMPLE_FROM_SPACE_CHECK ON SAMPLES_ALL;
CREATE TRIGGER DELETE_SAMPLE_FROM_SPACE_CHECK AFTER DELETE ON SAMPLES_ALL
    FOR EACH ROW WHEN (OLD.space_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SPACE_EXCEPTION('SAMPLE');

-- from project -------------------
CREATE OR REPLACE FUNCTION RAISE_DELETE_FROM_PROJECT_EXCEPTION() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because project % is frozen.', TG_ARGV[0], 
        (select code from projects where id = old.proj_id);
END;
$$ LANGUAGE 'plpgsql';

-- Experiment from project deletion
DROP TRIGGER IF EXISTS TRASH_EXPERIMENT_FROM_PROJECT_CHECK ON EXPERIMENTS_ALL;
CREATE TRIGGER TRASH_EXPERIMENT_FROM_PROJECT_CHECK AFTER UPDATE ON EXPERIMENTS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.proj_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_PROJECT_EXCEPTION('EXPERIMENT');

DROP TRIGGER IF EXISTS DELETE_EXPERIMENT_FROM_PROJECT_CHECK ON EXPERIMENTS_ALL;
CREATE TRIGGER DELETE_EXPERIMENT_FROM_PROJECT_CHECK AFTER DELETE ON EXPERIMENTS_ALL
    FOR EACH ROW WHEN (OLD.proj_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_PROJECT_EXCEPTION('EXPERIMENT');

-- Sample from project deletion
DROP TRIGGER IF EXISTS TRASH_SAMPLE_FROM_PROJECT_CHECK ON SAMPLES_ALL;
CREATE TRIGGER TRASH_SAMPLE_FROM_PROJECT_CHECK AFTER UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.proj_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_PROJECT_EXCEPTION('SAMPLE');

DROP TRIGGER IF EXISTS DELETE_SAMPLE_FROM_PROJECT_CHECK ON SAMPLES_ALL;
CREATE TRIGGER DELETE_SAMPLE_FROM_PROJECT_CHECK AFTER DELETE ON SAMPLES_ALL
    FOR EACH ROW WHEN (OLD.proj_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_PROJECT_EXCEPTION('SAMPLE');

-- from experiment ---------------
CREATE OR REPLACE FUNCTION RAISE_DELETE_FROM_EXPERIMENT_EXCEPTION() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Operation DELETE % is not allowed because experiment % is frozen.', TG_ARGV[0], 
        (select code from experiments_all where id = old.expe_id);
END;
$$ LANGUAGE 'plpgsql';

-- Sample from experiment deletion
DROP TRIGGER IF EXISTS TRASH_SAMPLE_FROM_EXPERIMENT_CHECK ON SAMPLES_ALL;
CREATE TRIGGER TRASH_SAMPLE_FROM_EXPERIMENT_CHECK AFTER UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.expe_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_EXPERIMENT_EXCEPTION('SAMPLE');

DROP TRIGGER IF EXISTS DELETE_SAMPLE_FROM_EXPERIMENT_CHECK ON SAMPLES_ALL;
CREATE TRIGGER DELETE_SAMPLE_FROM_EXPERIMENT_CHECK AFTER DELETE ON SAMPLES_ALL
    FOR EACH ROW WHEN (OLD.expe_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_EXPERIMENT_EXCEPTION('SAMPLE');

-- Data set from experiment deletion
DROP TRIGGER IF EXISTS TRASH_DATA_SET_FROM_EXPERIMENT_CHECK ON DATA_ALL;
CREATE TRIGGER TRASH_DATA_SET_FROM_EXPERIMENT_CHECK AFTER UPDATE ON DATA_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.expe_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_EXPERIMENT_EXCEPTION('DATA SET');

DROP TRIGGER IF EXISTS DELETE_DATA_SET_FROM_EXPERIMENT_CHECK ON DATA_ALL;
CREATE TRIGGER DELETE_DATA_SET_FROM_EXPERIMENT_CHECK AFTER DELETE ON DATA_ALL
    FOR EACH ROW WHEN (OLD.expe_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_EXPERIMENT_EXCEPTION('DATA SET');

-- from sample ---------------
CREATE OR REPLACE FUNCTION RAISE_DELETE_FROM_SAMPLE_EXCEPTION() RETURNS trigger AS $$
DECLARE
    samp_id TECH_ID;
BEGIN
    IF (TG_ARGV[0] = 'SAMPLE CHILD') THEN
        samp_id = old.sample_id_parent;
    ELSEIF (TG_ARGV[0] = 'SAMPLE PARENT') THEN
        samp_id = old.sample_id_child;
    ELSEIF (TG_ARGV[0] = 'SAMPLE COMPONENT') THEN
        samp_id = old.samp_id_part_of;
    ELSE
        samp_id = old.samp_id;
    END IF;
    RAISE EXCEPTION 'Operation DELETE % is not allowed because sample % is frozen.', TG_ARGV[0], 
        (select code from samples_all where id = samp_id);
END;
$$ LANGUAGE 'plpgsql';

-- Sample from container deletion
DROP TRIGGER IF EXISTS TRASH_SAMPLE_FROM_CONTAINER_CHECK ON SAMPLES_ALL;
CREATE TRIGGER TRASH_SAMPLE_FROM_CONTAINER_CHECK AFTER UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.cont_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SAMPLE_EXCEPTION('SAMPLE COMPONENT');

DROP TRIGGER IF EXISTS DELETE_SAMPLE_FROM_CONTAINER_CHECK ON SAMPLES_ALL;
CREATE TRIGGER DELETE_SAMPLE_FROM_CONTAINER_CHECK AFTER DELETE ON SAMPLES_ALL
    FOR EACH ROW WHEN (OLD.cont_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SAMPLE_EXCEPTION('SAMPLE COMPONENT');

-- Sample from parent deletion
DROP TRIGGER IF EXISTS TRASH_SAMPLE_FROM_PARENT_CHECK ON SAMPLE_RELATIONSHIPS_ALL;
CREATE TRIGGER TRASH_SAMPLE_FROM_PARENT_CHECK AFTER UPDATE ON SAMPLE_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.parent_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SAMPLE_EXCEPTION('SAMPLE CHILD');

-- Sample from child deletion
DROP TRIGGER IF EXISTS TRASH_SAMPLE_FROM_CHILD_CHECK ON SAMPLE_RELATIONSHIPS_ALL;
CREATE TRIGGER TRASH_SAMPLE_FROM_CHILD_CHECK AFTER UPDATE ON SAMPLE_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.child_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SAMPLE_EXCEPTION('SAMPLE PARENT');

-- Data set from sample deletion
DROP TRIGGER IF EXISTS TRASH_DATA_SET_FROM_SAMPLE_CHECK ON DATA_ALL;
CREATE TRIGGER TRASH_DATA_SET_FROM_SAMPLE_CHECK AFTER UPDATE ON DATA_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.samp_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SAMPLE_EXCEPTION('DATA SET');

DROP TRIGGER IF EXISTS DELETE_DATA_SET_FROM_SAMPLE_CHECK ON DATA_ALL;
CREATE TRIGGER DELETE_DATA_SET_FROM_SAMPLE_CHECK AFTER DELETE ON DATA_ALL
    FOR EACH ROW WHEN (OLD.samp_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_SAMPLE_EXCEPTION('DATA SET');

-- from data set ---------------
CREATE OR REPLACE FUNCTION RAISE_DELETE_FROM_DATA_SET_EXCEPTION() RETURNS trigger AS $$
DECLARE
    data_id TECH_ID;
BEGIN
    IF (TG_ARGV[0] = 'DATA SET CHILD') THEN
        data_id = old.data_id_parent;
    ELSEIF (TG_ARGV[0] = 'DATA SET PARENT') THEN
        data_id = old.data_id_child;
    ELSEIF (TG_ARGV[0] = 'DATA SET COMPONENT') THEN
        data_id = old.data_id_parent;
    ELSEIF (TG_ARGV[0] = 'DATA SET CONTAINER') THEN
        data_id = old.data_id_child;
    END IF;
    RAISE EXCEPTION 'Operation DELETE % is not allowed because data set % is frozen.', TG_ARGV[0], 
        (select code from data_all where id = data_id);
END;
$$ LANGUAGE 'plpgsql';

-- Data set from parent deletion
DROP TRIGGER IF EXISTS TRASH_DATA_SET_FROM_PARENT_CHECK ON DATA_SET_RELATIONSHIPS_ALL;
CREATE TRIGGER TRASH_DATA_SET_FROM_PARENT_CHECK AFTER UPDATE ON DATA_SET_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.parent_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_DATA_SET_EXCEPTION('DATA SET CHILD');

-- Data set from child deletion
DROP TRIGGER IF EXISTS TRASH_DATA_SET_FROM_CHILD_CHECK ON DATA_SET_RELATIONSHIPS_ALL;
CREATE TRIGGER TRASH_DATA_SET_FROM_CHILD_CHECK AFTER UPDATE ON DATA_SET_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.child_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_DATA_SET_EXCEPTION('DATA SET PARENT');

-- Data set from container deletion
DROP TRIGGER IF EXISTS TRASH_DATA_SET_FROM_CONTAINER_CHECK ON DATA_SET_RELATIONSHIPS_ALL;
CREATE TRIGGER TRASH_DATA_SET_FROM_CONTAINER_CHECK AFTER UPDATE ON DATA_SET_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.cont_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_DATA_SET_EXCEPTION('DATA SET COMPONENT');

-- Data set from component deletion
DROP TRIGGER IF EXISTS TRASH_DATA_SET_FROM_COMPONENT_CHECK ON DATA_SET_RELATIONSHIPS_ALL;
CREATE TRIGGER TRASH_DATA_SET_FROM_COMPONENT_CHECK AFTER UPDATE ON DATA_SET_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.comp_frozen)
    EXECUTE PROCEDURE RAISE_DELETE_FROM_DATA_SET_EXCEPTION('DATA SET CONTAINER');

