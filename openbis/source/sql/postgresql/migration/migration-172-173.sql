----------------------------------------
-- Tables, indices and constrains
----------------------------------------

--
-- SPACES
--
ALTER TABLE SPACES ADD COLUMN FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

CREATE INDEX SPACE_IDFRZ_PK_I ON SPACES (id, frozen);

ALTER TABLE SPACES ADD CONSTRAINT SPACE_IDFRZ_UK UNIQUE(ID, FROZEN);

--
-- PROJECTS
--
ALTER TABLE PROJECTS ADD COLUMN FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE PROJECTS ADD COLUMN SPACE_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

CREATE INDEX PROJ_IDFRZ_PK_I ON PROJECTS (ID, FROZEN);

ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_IDFRZ_UK UNIQUE(ID, FROZEN);
ALTER TABLE PROJECTS DROP CONSTRAINT PROJ_SPACE_FK;
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_SPACE_FK FOREIGN KEY (SPACE_ID, SPACE_FROZEN) REFERENCES SPACES(ID, FROZEN) ON UPDATE CASCADE;

ALTER TABLE ATTACHMENTS ADD COLUMN PROJ_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

ALTER TABLE ATTACHMENTS DROP CONSTRAINT ATTA_PROJ_FK;
ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_PROJ_FK FOREIGN KEY (PROJ_ID, PROJ_FROZEN) REFERENCES PROJECTS(ID, FROZEN) ON UPDATE CASCADE;

--
-- EXPERIMENTS
--
ALTER TABLE EXPERIMENTS_ALL ADD COLUMN FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE EXPERIMENTS_ALL ADD COLUMN PROJ_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

CREATE INDEX EXPE_IDFRZ_PK_I ON EXPERIMENTS_ALL (id, frozen);

ALTER TABLE EXPERIMENTS_ALL ADD CONSTRAINT EXPE_IDFRZ_UK UNIQUE(ID, FROZEN);
ALTER TABLE EXPERIMENTS_ALL DROP CONSTRAINT EXPE_PROJ_FK;
ALTER TABLE EXPERIMENTS_ALL ADD CONSTRAINT EXPE_PROJ_FK FOREIGN KEY (PROJ_ID, PROJ_FROZEN) REFERENCES PROJECTS(ID, FROZEN) ON UPDATE CASCADE;

ALTER TABLE EXPERIMENT_PROPERTIES ADD COLUMN EXPE_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_EXPE_FK;
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_EXPE_FK FOREIGN KEY (EXPE_ID, EXPE_FROZEN) REFERENCES EXPERIMENTS_ALL(ID, FROZEN) ON UPDATE CASCADE;

ALTER TABLE ATTACHMENTS ADD COLUMN EXPE_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

ALTER TABLE ATTACHMENTS DROP CONSTRAINT ATTA_EXPE_FK;
ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_EXPE_FK FOREIGN KEY (EXPE_ID, EXPE_FROZEN) REFERENCES EXPERIMENTS_ALL(ID, FROZEN) ON UPDATE CASCADE;

--
-- SAMPLES
--
ALTER TABLE SAMPLES_ALL ADD COLUMN FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE SAMPLES_ALL ADD COLUMN SPACE_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE SAMPLES_ALL ADD COLUMN PROJ_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE SAMPLES_ALL ADD COLUMN EXPE_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE SAMPLES_ALL ADD COLUMN CONT_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

CREATE INDEX SAMP_IDFRZ_PK_I ON SAMPLES_ALL (id, frozen);

ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_IDFRZ_UK UNIQUE(ID, FROZEN);
ALTER TABLE SAMPLES_ALL DROP CONSTRAINT SAMP_SAMP_FK_PART_OF;
ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_SAMP_FK_PART_OF FOREIGN KEY (SAMP_ID_PART_OF, CONT_FROZEN) REFERENCES SAMPLES_ALL(ID, FROZEN) ON UPDATE CASCADE;
ALTER TABLE SAMPLES_ALL DROP CONSTRAINT SAMP_EXPE_FK;
ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_EXPE_FK FOREIGN KEY (EXPE_ID, EXPE_FROZEN) REFERENCES EXPERIMENTS_ALL(ID, FROZEN) ON UPDATE CASCADE;
ALTER TABLE SAMPLES_ALL DROP CONSTRAINT SAMP_PROJ_FK;
ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_PROJ_FK FOREIGN KEY (PROJ_ID, PROJ_FROZEN) REFERENCES PROJECTS(ID, FROZEN) ON UPDATE CASCADE;
ALTER TABLE SAMPLES_ALL DROP CONSTRAINT SAMP_SPACE_FK;
ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_SPACE_FK FOREIGN KEY (SPACE_ID, SPACE_FROZEN) REFERENCES SPACES(ID, FROZEN) ON UPDATE CASCADE;

ALTER TABLE SAMPLE_PROPERTIES ADD COLUMN SAMP_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

ALTER TABLE SAMPLE_PROPERTIES DROP CONSTRAINT SAPR_SAMP_FK;
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_SAMP_FK FOREIGN KEY (SAMP_ID, SAMP_FROZEN) REFERENCES SAMPLES_ALL(ID, FROZEN) ON UPDATE CASCADE;

ALTER TABLE ATTACHMENTS ADD COLUMN SAMP_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

ALTER TABLE ATTACHMENTS DROP CONSTRAINT ATTA_SAMP_FK;
ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_SAMP_FK FOREIGN KEY (SAMP_ID, SAMP_FROZEN) REFERENCES SAMPLES_ALL(ID, FROZEN) ON UPDATE CASCADE;

ALTER TABLE SAMPLE_RELATIONSHIPS_ALL ADD COLUMN PARENT_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE SAMPLE_RELATIONSHIPS_ALL ADD COLUMN CHILD_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

ALTER TABLE sample_relationships_all DROP CONSTRAINT sare_data_fk_child;
ALTER TABLE sample_relationships_all ADD CONSTRAINT sare_data_fk_child FOREIGN KEY (sample_id_child, CHILD_FROZEN) REFERENCES SAMPLES_ALL(id, frozen) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE sample_relationships_all DROP CONSTRAINT sare_data_fk_parent;
ALTER TABLE sample_relationships_all ADD CONSTRAINT sare_data_fk_parent FOREIGN KEY (sample_id_parent, PARENT_FROZEN) REFERENCES SAMPLES_ALL(id, frozen) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- DATA SETS
--
ALTER TABLE DATA_ALL ADD COLUMN FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE DATA_ALL ADD COLUMN EXPE_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE DATA_ALL ADD COLUMN SAMP_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

CREATE INDEX DATA_IDFRZ_PK_I ON DATA_ALL (id, frozen);

ALTER TABLE DATA_ALL ADD CONSTRAINT DATA_IDFRZ_UK UNIQUE(ID, FROZEN);
ALTER TABLE DATA_ALL DROP CONSTRAINT DATA_EXPE_FK;
ALTER TABLE DATA_ALL ADD CONSTRAINT DATA_EXPE_FK FOREIGN KEY (EXPE_ID, EXPE_FROZEN) REFERENCES EXPERIMENTS_ALL(ID, FROZEN) ON UPDATE CASCADE;
ALTER TABLE DATA_ALL DROP CONSTRAINT DATA_SAMP_FK;
ALTER TABLE DATA_ALL ADD CONSTRAINT DATA_SAMP_FK FOREIGN KEY (SAMP_ID, SAMP_FROZEN) REFERENCES SAMPLES_ALL(ID, FROZEN) ON UPDATE CASCADE;

ALTER TABLE DATA_SET_PROPERTIES ADD COLUMN DASE_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

ALTER TABLE DATA_SET_PROPERTIES DROP CONSTRAINT DSPR_DS_FK;
ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_DS_FK FOREIGN KEY (DS_ID, DASE_FROZEN) REFERENCES DATA_ALL(ID, FROZEN) ON UPDATE CASCADE;

ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD COLUMN PARENT_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD COLUMN CHILD_FROZEN BOOLEAN_CHAR NOT NULL DEFAULT 'F';

ALTER TABLE DATA_SET_RELATIONSHIPS_ALL DROP CONSTRAINT DSRE_DATA_FK_CHILD;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD CONSTRAINT DSRE_DATA_FK_CHILD FOREIGN KEY (DATA_ID_CHILD, CHILD_FROZEN) REFERENCES DATA_ALL(ID, FROZEN) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL DROP CONSTRAINT DSRE_DATA_FK_PARENT;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD CONSTRAINT DSRE_DATA_FK_PARENT FOREIGN KEY (DATA_ID_PARENT, PARENT_FROZEN) REFERENCES DATA_ALL(ID, FROZEN) ON DELETE CASCADE ON UPDATE CASCADE;

---------------------------
-- Views
---------------------------
-- Experiments
DROP VIEW experiments;
CREATE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, proj_frozen, del_id, orig_del, is_public, version, frozen 
       FROM experiments_all 
      WHERE del_id IS NULL;

CREATE OR REPLACE RULE experiment_insert AS
  ON INSERT TO experiments DO INSTEAD 
     INSERT INTO experiments_all (
       id,
       frozen,
       code, 
       del_id,
       orig_del,
       exty_id, 
       is_public,
       modification_timestamp,
       perm_id,
       pers_id_registerer, 
       pers_id_modifier, 
       proj_id,
       proj_frozen,
       registration_timestamp,
       version
     ) VALUES (
       NEW.id,
       NEW.frozen,
       NEW.code, 
       NEW.del_id,
       NEW.orig_del,
       NEW.exty_id, 
       NEW.is_public,
       NEW.modification_timestamp,
       NEW.perm_id,
       NEW.pers_id_registerer, 
       NEW.pers_id_modifier, 
       NEW.proj_id,
       NEW.proj_frozen,
       NEW.registration_timestamp,
       NEW.version
     );
     
CREATE OR REPLACE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD 
       UPDATE experiments_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              exty_id = NEW.exty_id,
              is_public = NEW.is_public,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              proj_id = NEW.proj_id,
              proj_frozen = NEW.proj_frozen,
              registration_timestamp = NEW.registration_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiment_delete AS
    ON DELETE TO experiments DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;

-- Samples
DROP VIEW SAMPLES;
CREATE VIEW samples AS
     SELECT id, perm_id, code, proj_id, proj_frozen, expe_id, expe_frozen, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, space_frozen, samp_id_part_of, cont_frozen, version, frozen 
       FROM samples_all 
      WHERE del_id IS NULL;

CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD 
       INSERT INTO samples_all (
         id,
         frozen,
         code, 
         del_id,
         orig_del,
         expe_id,
         expe_frozen,
         proj_id,
         proj_frozen,
         modification_timestamp,
         perm_id,
         pers_id_registerer, 
         pers_id_modifier, 
         registration_timestamp, 
         samp_id_part_of,
         cont_frozen,
         saty_id, 
         space_id,
         space_frozen,
         version
       ) VALUES (
         NEW.id,
         NEW.frozen,
         NEW.code, 
         NEW.del_id,
         NEW.orig_del,
         NEW.expe_id,
         NEW.expe_frozen,
         NEW.proj_id,
         NEW.proj_frozen,
         NEW.modification_timestamp,
         NEW.perm_id,
         NEW.pers_id_registerer, 
         NEW.pers_id_modifier, 
         NEW.registration_timestamp, 
         NEW.samp_id_part_of,
         NEW.cont_frozen,
         NEW.saty_id, 
         NEW.space_id,
         NEW.space_frozen,
         NEW.version
       );
     
CREATE OR REPLACE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD 
       UPDATE samples_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              expe_frozen = NEW.expe_frozen,
              proj_id = NEW.proj_id,
              proj_frozen = NEW.proj_frozen,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              registration_timestamp = NEW.registration_timestamp,
              samp_id_part_of = NEW.samp_id_part_of,
              cont_frozen = NEW.cont_frozen,
              saty_id = NEW.saty_id,
              space_id = NEW.space_id,
              space_frozen = NEW.space_frozen,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_delete AS
    ON DELETE TO samples DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;
              
-- Sample relationships
DROP VIEW sample_relationships;
CREATE VIEW sample_relationships AS
   SELECT id, sample_id_parent, parent_frozen, relationship_id, sample_id_child, child_frozen, del_id, pers_id_author, registration_timestamp, modification_timestamp
   FROM sample_relationships_all
   WHERE del_id IS NULL;

CREATE OR REPLACE RULE sample_relationships_insert AS
    ON INSERT TO sample_relationships DO INSTEAD 
       INSERT INTO sample_relationships_all (
         id, 
         sample_id_parent,
         parent_frozen,
         relationship_id,
         sample_id_child,
         child_frozen,
         pers_id_author,
         registration_timestamp,
         modification_timestamp
       ) VALUES (
         NEW.id, 
         NEW.sample_id_parent,
         NEW.parent_frozen,
         NEW.relationship_id,
         NEW.sample_id_child,
         NEW.child_frozen,
         NEW.pers_id_author,
         NEW.registration_timestamp,
         NEW.modification_timestamp
       );
       
CREATE OR REPLACE RULE sample_relationships_update AS
    ON UPDATE TO sample_relationships DO INSTEAD 
       UPDATE sample_relationships_all
          SET 
             sample_id_parent = NEW.sample_id_parent,
             parent_frozen = NEW.parent_frozen,
             relationship_id = NEW.relationship_id,
             sample_id_child = NEW.sample_id_child,
             child_frozen = NEW.child_frozen,
             del_id = NEW.del_id,
             pers_id_author = NEW.pers_id_author,
             registration_timestamp = NEW.registration_timestamp,
             modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;

CREATE OR REPLACE RULE sample_relationships_delete AS
    ON DELETE TO sample_relationships DO INSTEAD
       DELETE FROM sample_relationships_all
              WHERE id = OLD.id;

-- Data sets
DROP VIEW data;
CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, expe_frozen, data_producer_code, production_timestamp, samp_id, samp_frozen, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind, frozen 
       FROM data_all 
      WHERE del_id IS NULL;

CREATE OR REPLACE RULE data_insert AS
  ON INSERT TO data DO INSTEAD 
     INSERT INTO data_all (
       id,
       frozen,
       code, 
       del_id,
       orig_del,
       expe_id,
       expe_frozen,
       dast_id,
       data_producer_code,
       dsty_id,
       is_derived,
       is_valid,
       modification_timestamp,
       access_timestamp,
       pers_id_registerer,
       pers_id_modifier,
       production_timestamp,
       registration_timestamp,
       samp_id,
       samp_frozen,
       version,
       data_set_kind
     ) VALUES (
       NEW.id,
       NEW.frozen,
       NEW.code, 
       NEW.del_id, 
       NEW.orig_del,
       NEW.expe_id,
       NEW.expe_frozen,
       NEW.dast_id,
       NEW.data_producer_code,
       NEW.dsty_id,
       NEW.is_derived, 
       NEW.is_valid,
       NEW.modification_timestamp,
       NEW.access_timestamp,
       NEW.pers_id_registerer,
       NEW.pers_id_modifier,
       NEW.production_timestamp,
       NEW.registration_timestamp,
       NEW.samp_id,
       NEW.samp_frozen,
       NEW.version,
       NEW.data_set_kind
     );
     
CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD 
       UPDATE data_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              expe_frozen = NEW.expe_frozen,
              dast_id = NEW.dast_id,
              data_producer_code = NEW.data_producer_code,
              dsty_id = NEW.dsty_id,
              is_derived = NEW.is_derived,
              is_valid = NEW.is_valid,
              modification_timestamp = NEW.modification_timestamp,
              access_timestamp = NEW.access_timestamp,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              production_timestamp = NEW.production_timestamp,
              registration_timestamp = NEW.registration_timestamp,
              samp_id = NEW.samp_id,
              samp_frozen = NEW.samp_frozen,
              version = NEW.version,
              data_set_kind = NEW.data_set_kind
       WHERE id = NEW.id;
              
CREATE OR REPLACE RULE data_all AS
    ON DELETE TO data DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;
              
-- Data set relationships

DROP VIEW data_set_relationships;
CREATE VIEW data_set_relationships AS
   SELECT data_id_parent, parent_frozen, data_id_child, child_frozen, relationship_id, ordinal, del_id, pers_id_author, registration_timestamp, modification_timestamp
   FROM data_set_relationships_all 
   WHERE del_id IS NULL;

CREATE OR REPLACE RULE data_set_relationships_insert AS
    ON INSERT TO data_set_relationships DO INSTEAD 
       INSERT INTO data_set_relationships_all (
         data_id_parent,
         parent_frozen,
         data_id_child,
         child_frozen,
         pers_id_author,
         relationship_id,
         ordinal,
         registration_timestamp,
         modification_timestamp
       ) VALUES (
         NEW.data_id_parent,
         NEW.parent_frozen,
         NEW.data_id_child,
         NEW.child_frozen,
         NEW.pers_id_author,
         NEW.relationship_id,
         NEW.ordinal,
         NEW.registration_timestamp,
         NEW.modification_timestamp
       );

CREATE OR REPLACE RULE data_set_relationships_update AS
    ON UPDATE TO data_set_relationships DO INSTEAD 
       UPDATE data_set_relationships_all
          SET 
            data_id_parent = NEW.data_id_parent,
            parent_frozen = NEW.parent_frozen,
            data_id_child = NEW.data_id_child,
            child_frozen = NEW.child_frozen,
            del_id = NEW.del_id,
            relationship_id = NEW.relationship_id,
            ordinal = NEW.ordinal,
            pers_id_author = NEW.pers_id_author,
            registration_timestamp = NEW.registration_timestamp,
            modification_timestamp = NEW.modification_timestamp
          WHERE data_id_parent = NEW.data_id_parent and data_id_child = NEW.data_id_child 
                and relationship_id = NEW.relationship_id;

CREATE OR REPLACE RULE data_set_relationships_delete AS
    ON DELETE TO data_set_relationships DO INSTEAD
       DELETE FROM data_set_relationships_all
              WHERE data_id_parent = OLD.data_id_parent and data_id_child = OLD.data_id_child
                    and relationship_id = OLD.relationship_id;

---------------------------
-- Triggers for freezing
---------------------------

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Operation % is not allowed because % % is frozen.', TG_ARGV[0], TG_ARGV[1], OLD.code;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SPACE_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    space_id   TECH_ID;
    operation  TEXT;
BEGIN
    IF (NEW.space_id IS NOT NULL AND NEW.space_frozen) THEN
        space_id = NEW.space_id;
        operation = 'SET SPACE';
    ELSEIF (OLD.space_id IS NOT NULL AND OLD.space_frozen) THEN
        space_id = OLD.space_id;
        operation = 'REMOVE SPACE';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and space % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from spaces where id = space_id);
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_PROJECT_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    project_id   TECH_ID;
    operation    TEXT;
BEGIN
    IF (NEW.proj_id IS NOT NULL AND NEW.proj_frozen) THEN
        project_id = NEW.proj_id;
        operation = 'SET PROJECT';
    ELSEIF (OLD.proj_id IS NOT NULL AND OLD.proj_frozen) THEN
        project_id = OLD.proj_id;
        operation = 'REMOVE PROJECT';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and project % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from projects where id = project_id);
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_EXPERIMENT_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    experiment_id   TECH_ID;
    operation       TEXT;
BEGIN
    IF (NEW.expe_id IS NOT NULL AND NEW.expe_frozen) THEN
        experiment_id = NEW.expe_id;
        operation = 'SET EXPERIMENT';
    ELSEIF (OLD.expe_id IS NOT NULL AND OLD.expe_frozen) THEN
        experiment_id = OLD.expe_id;
        operation = 'REMOVE EXPERIMENT';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and experiment % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from experiments_all where id = experiment_id);
END;
$$ LANGUAGE 'plpgsql';

-- Projects --------------------
-- Project deleting

DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_DELETE ON PROJECTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON PROJECTS
    FOR EACH ROW WHEN (OLD.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('DELETE', 'project');

-- Project update
DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_UPDATE ON PROJECTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_UPDATE BEFORE UPDATE ON PROJECTS
    FOR EACH ROW WHEN (OLD.frozen AND NEW.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('UPDATE', 'project');

-- Project attachment inserting, updating and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_PROJECT() RETURNS trigger AS $$
DECLARE
    project_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        project_id = OLD.proj_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        project_id = NEW.proj_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because project % is frozen.', TG_OP, TG_ARGV[0],
        (select code from projects where id = project_id);
END;
$$ LANGUAGE 'plpgsql';
DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_INSERT_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_INSERT_ATTACHMENT BEFORE INSERT ON ATTACHMENTS
    FOR EACH ROW WHEN (NEW.PROJ_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT('ATTACHMENT');

DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_UPDATE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_UPDATE_ATTACHMENT BEFORE UPDATE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.PROJ_FROZEN AND NEW.PROJ_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT('ATTACHMENT');

DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_DELETE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_DELETE_ATTACHMENT BEFORE DELETE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.PROJ_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT('ATTACHMENT');

-- Project space relationship
DROP TRIGGER IF EXISTS PROJECT_SPACE_RELATIONSHIP_FROZEN_CHECK ON PROJECTS;
CREATE TRIGGER PROJECT_SPACE_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON PROJECTS
    FOR EACH ROW WHEN (NEW.space_id <> OLD.space_id AND (NEW.SPACE_FROZEN OR OLD.SPACE_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SPACE_RELATIONSHIP('project');

-- Experiments --------------------
-- Experiment trashing and deleting

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_TRASH ON EXPERIMENTS_ALL;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_TRASH BEFORE UPDATE ON EXPERIMENTS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.frozen) 
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('TRASH', 'experiment');
    
DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_DELETE ON EXPERIMENTS_ALL;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON EXPERIMENTS_ALL
    FOR EACH ROW WHEN (OLD.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('DELETE', 'experiment');

-- Experiment property inserting, updating and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_EXPERIMENT() RETURNS trigger AS $$
DECLARE
    experiment_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        experiment_id = OLD.expe_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        experiment_id = NEW.expe_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because experiment % is frozen.', TG_OP, TG_ARGV[0],
        (select code from experiments_all where id = experiment_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_INSERT_PROPERTY ON EXPERIMENT_PROPERTIES;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_INSERT_PROPERTY BEFORE INSERT ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY ON EXPERIMENT_PROPERTIES;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN AND NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_DELETE_PROPERTY ON EXPERIMENT_PROPERTIES;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_DELETE_PROPERTY BEFORE DELETE ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

-- Experiment attachment inserting, updating and deleting
DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_INSERT_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_INSERT_ATTACHMENT BEFORE INSERT ON ATTACHMENTS
    FOR EACH ROW WHEN (NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('ATTACHMENT');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_UPDATE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_UPDATE_ATTACHMENT BEFORE UPDATE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN AND NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('ATTACHMENT');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_DELETE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_DELETE_ATTACHMENT BEFORE DELETE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('ATTACHMENT');

-- Experiment project relationship
DROP TRIGGER IF EXISTS EXPERIMENT_PROJECT_RELATIONSHIP_FROZEN_CHECK ON EXPERIMENTS_ALL;
CREATE TRIGGER EXPERIMENT_PROJECT_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON EXPERIMENTS_ALL
    FOR EACH ROW WHEN (NEW.proj_id <> OLD.proj_id AND (NEW.PROJ_FROZEN OR OLD.PROJ_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT_RELATIONSHIP('experiment');

-- Samples --------------------
-- Sample trashing and deleting

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_TRASH ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_TRASH BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.frozen) 
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('TRASH', 'sample');
    
DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_DELETE ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON SAMPLES_ALL
    FOR EACH ROW WHEN (OLD.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('DELETE', 'sample');

-- Sample property inserting, updating and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SAMPLE() RETURNS trigger AS $$
DECLARE
    sample_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        sample_id = OLD.samp_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        sample_id = NEW.samp_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because sample % is frozen.', TG_OP, TG_ARGV[0],
        (select code from samples_all where id = sample_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_INSERT_PROPERTY ON SAMPLE_PROPERTIES;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_INSERT_PROPERTY BEFORE INSERT ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY ON SAMPLE_PROPERTIES;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN AND NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_DELETE_PROPERTY ON SAMPLE_PROPERTIES;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_DELETE_PROPERTY BEFORE DELETE ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

-- Sample attachment inserting, updating and deleting
DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_INSERT_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_INSERT_ATTACHMENT BEFORE INSERT ON ATTACHMENTS
    FOR EACH ROW WHEN (NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('ATTACHMENT');

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_UPDATE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_UPDATE_ATTACHMENT BEFORE UPDATE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN AND NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('ATTACHMENT');

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_DELETE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_DELETE_ATTACHMENT BEFORE DELETE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('ATTACHMENT');

-- Sample container setting and removing
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SAMPLE_CONTAINER_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    sample_id   TECH_ID;
    operation   TEXT;
BEGIN
    IF (NEW.samp_id_part_of IS NOT NULL AND NEW.CONT_FROZEN) THEN
        sample_id = NEW.samp_id_part_of;
        operation = 'SET CONTAINER';
    ELSEIF (OLD.samp_id_part_of IS NOT NULL AND OLD.CONT_FROZEN) THEN
        sample_id = OLD.samp_id_part_of;
        operation = 'REMOVE CONTAINER';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because samples % and % are frozen.', operation, NEW.code,
        (select code from samples_all where id = sample_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_SET_CONTAINER ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_SET_CONTAINER BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (
        (NEW.samp_id_part_of <> OLD.samp_id_part_of
         OR (NEW.samp_id_part_of IS NOT NULL AND OLD.samp_id_part_of IS NULL)
         OR (NEW.samp_id_part_of IS NULL AND OLD.samp_id_part_of IS NOT NULL))
        AND (NEW.CONT_FROZEN OR OLD.CONT_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE_CONTAINER_RELATIONSHIP();

-- Sample parent-child relationship inserting and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SAMPLE_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    parent_id   TECH_ID;
    child_id    TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        parent_id = OLD.sample_id_parent;
        child_id = OLD.sample_id_child;
    ELSEIF (TG_OP = 'INSERT') THEN
        parent_id = NEW.sample_id_parent;
        child_id = NEW.sample_id_child;
    END IF;
    RAISE EXCEPTION 'Operation % is not allowed because samples % and % are frozen.', TG_OP, 
        (select code from samples_all where id = parent_id),
        (select code from samples_all where id = child_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_INSERT ON SAMPLE_RELATIONSHIPS_ALL;
CREATE TRIGGER SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_INSERT BEFORE INSERT ON SAMPLE_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.PARENT_FROZEN AND NEW.CHILD_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE_RELATIONSHIP();

DROP TRIGGER IF EXISTS SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_DELETE ON SAMPLE_RELATIONSHIPS_ALL;
CREATE TRIGGER SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON SAMPLE_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (OLD.PARENT_FROZEN AND OLD.CHILD_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE_RELATIONSHIP();

-- Sample experiment relationship
DROP TRIGGER IF EXISTS SAMPLE_EXPERIMENT_RELATIONSHIP_FROZEN_CHECK ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_EXPERIMENT_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (
        (NEW.expe_id <> OLD.expe_id
         OR (NEW.expe_id IS NOT NULL AND OLD.expe_id IS NULL)
         OR (NEW.expe_id IS NULL AND OLD.expe_id IS NOT NULL))
        AND (NEW.EXPE_FROZEN OR OLD.EXPE_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT_RELATIONSHIP('sample');

-- Sample project relationship
DROP TRIGGER IF EXISTS SAMPLE_PROJECT_RELATIONSHIP_FROZEN_CHECK ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_PROJECT_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (
        (NEW.proj_id <> OLD.proj_id
         OR (NEW.proj_id IS NOT NULL AND OLD.proj_id IS NULL)
         OR (NEW.proj_id IS NULL AND OLD.proj_id IS NOT NULL))
        AND (NEW.PROJ_FROZEN OR OLD.PROJ_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT_RELATIONSHIP('sample');

-- Sample space relationship
DROP TRIGGER IF EXISTS SAMPLE_SPACE_RELATIONSHIP_FROZEN_CHECK ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_SPACE_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (
        (NEW.space_id <> OLD.space_id
         OR (NEW.space_id IS NOT NULL AND OLD.space_id IS NULL)
         OR (NEW.space_id IS NULL AND OLD.space_id IS NOT NULL))
        AND (NEW.SPACE_FROZEN OR OLD.SPACE_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SPACE_RELATIONSHIP('sample');

-- Data Set --------------------
-- Data set trashing and deleting

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_TRASH ON DATA_ALL;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_TRASH BEFORE UPDATE ON DATA_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.frozen) 
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('TRASH', 'data set');
    
DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_DELETE ON DATA_ALL;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON DATA_ALL
    FOR EACH ROW WHEN (OLD.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('DELETE', 'data set');

-- Data set property inserting, updating and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_DATA_SET() RETURNS trigger AS $$
DECLARE
    ds_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        ds_id = OLD.ds_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        ds_id = NEW.ds_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because data set % is frozen.', TG_OP, TG_ARGV[0],
        (select code from data_all where id = ds_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_INSERT_PROPERTY ON DATA_SET_PROPERTIES;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_INSERT_PROPERTY BEFORE INSERT ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (NEW.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY ON DATA_SET_PROPERTIES;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (OLD.DASE_FROZEN AND NEW.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_DELETE_PROPERTY ON DATA_SET_PROPERTIES;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_DELETE_PROPERTY BEFORE DELETE ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (OLD.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

-- Data set parent-child relationship inserting and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_DATA_SET_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    parent_id   TECH_ID;
    child_id    TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        parent_id = OLD.data_id_parent;
        child_id = OLD.data_id_child;
    ELSEIF (TG_OP = 'INSERT') THEN
        parent_id = NEW.data_id_parent;
        child_id = NEW.data_id_child;
    END IF;
    RAISE EXCEPTION 'Operation % is not allowed because data sets % and % are frozen.', TG_OP, 
        (select code from data_all where id = parent_id),
        (select code from data_all where id = child_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS DATA_SET_RELATIONSHIP_FROZEN_CHECK_ON_INSERT ON DATA_SET_RELATIONSHIPS_ALL;
CREATE TRIGGER DATA_SET_RELATIONSHIP_FROZEN_CHECK_ON_INSERT BEFORE INSERT ON DATA_SET_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.PARENT_FROZEN AND NEW.CHILD_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET_RELATIONSHIP();

DROP TRIGGER IF EXISTS DATA_SET_RELATIONSHIP_FROZEN_CHECK_ON_DELETE ON DATA_SET_RELATIONSHIPS_ALL;
CREATE TRIGGER DATA_SET_RELATIONSHIP_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON DATA_SET_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (OLD.PARENT_FROZEN AND OLD.CHILD_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET_RELATIONSHIP();

-- Data set experiment relationship
DROP TRIGGER IF EXISTS DATA_SET_EXPERIMENT_RELATIONSHIP_FROZEN_CHECK_ON_UPDATE ON DATA_ALL;
CREATE TRIGGER DATA_SET_EXPERIMENT_RELATIONSHIP_FROZEN_CHECK_ON_UPDATE BEFORE UPDATE ON DATA_ALL
    FOR EACH ROW WHEN (
        (NEW.EXPE_ID <> OLD.EXPE_ID
         OR (NEW.EXPE_ID IS NOT NULL AND OLD.EXPE_ID IS NULL)
         OR (NEW.EXPE_ID IS NULL AND OLD.EXPE_ID IS NOT NULL))
        AND (NEW.EXPE_FROZEN OR OLD.EXPE_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT_RELATIONSHIP('data set');

-- Data set sample relationship
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_DATA_SET_SAMPLE_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    sample_id   TECH_ID;
    operation   TEXT;
BEGIN
    IF (NEW.samp_id IS NOT NULL AND NEW.samp_frozen) THEN
        sample_id = NEW.samp_id;
        operation = 'SET SAMPLE';
    ELSEIF (OLD.samp_id IS NOT NULL AND OLD.samp_frozen) THEN
        sample_id = OLD.samp_id;
        operation = 'REMOVE SAMPLE';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because data set % and sample % are frozen.', operation, NEW.code,
        (select code from samples_all where id = sample_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS DATA_SET_SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_UPDATE ON DATA_ALL;
CREATE TRIGGER DATA_SET_SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_UPDATE BEFORE UPDATE ON DATA_ALL
    FOR EACH ROW WHEN (
        (NEW.SAMP_ID <> OLD.SAMP_ID
         OR (NEW.SAMP_ID IS NOT NULL AND OLD.SAMP_ID IS NULL)
         OR (NEW.SAMP_ID IS NULL AND OLD.SAMP_ID IS NOT NULL))
        AND (NEW.SAMP_FROZEN OR OLD.SAMP_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET_SAMPLE_RELATIONSHIP();
