/**************************************************************************/
/* Schema Version: 006                                                    */
/* Author: Bernd Rinn, 2010, CISD                                         */
/**************************************************************************/

/* ---------------------------------------------------------------------- */
/* Domains                                                                */
/* ---------------------------------------------------------------------- */

CREATE DOMAIN TECH_ID AS BIGINT;

CREATE DOMAIN CODE AS VARCHAR(40);

CREATE DOMAIN PROPERTY AS CHARACTER VARYING(1000);

CREATE DOMAIN INTEGER AS INTEGER;

CREATE DOMAIN REAL AS DOUBLE PRECISION;

/* ---------------------------------------------------------------------- */
/* Add table "DATA_SETS"                                                */
/* ---------------------------------------------------------------------- */

CREATE TABLE DATA_SETS (
    ID BIGSERIAL  NOT NULL,
    PERM_ID CODE NOT NULL,
    UPLOADER_EMAIL PROPERTY NOT NULL,
    EXP_CODE CODE NOT NULL,
    EXP_PERM_ID CODE NOT NULL,
    CONSTRAINT DATA_PK PRIMARY KEY (ID),
    CONSTRAINT DATA_BK_UK UNIQUE (PERM_ID)
);


/* ---------------------------------------------------------------------- */
/* Add table "TIME_SERIES"                                                */
/* ---------------------------------------------------------------------- */

CREATE SEQUENCE TIME_SERIES_VALUE_GROUP_ID_SEQ;

CREATE TABLE TIME_SERIES (
    ID BIGSERIAL  NOT NULL,
    DASE_ID TECH_ID NOT NULL,
    ROW_INDEX INTEGER NOT NULL,
    COLUMN_INDEX INTEGER NOT NULL,
    VALUE_GROUP_ID TECH_ID NOT NULL,
    
    IDENTIFIER PROPERTY NOT NULL,
    IDENTIFIER_TYPE PROPERTY NOT NULL,
    IDENTIFIER_HUMAN_READABLE PROPERTY,
    BSB_ID PROPERTY,
    CONFIDENCE_LEVEL PROPERTY,
    CONTROLLED_GENE PROPERTY,
    NUMBER_OF_REPLICATES INTEGER,
    
    EXPERIMENT_TYPE PROPERTY NOT NULL,
    CULTIVATION_METHOD PROPERTY NOT NULL,
    BIOLOGICAL_REPLICATES PROPERTY NOT NULL,
    TIME_POINT INTEGER NOT NULL,
    TIME_POINT_TYPE PROPERTY NOT NULL,
    TECHNICAL_REPLICATES PROPERTY NOT NULL,
    CELL_LOCATION PROPERTY NOT NULL,
    DATA_SET_TYPE PROPERTY NOT NULL,
    VALUE_TYPE PROPERTY NOT NULL,
    UNIT PROPERTY NOT NULL,
    SCALE PROPERTY NOT NULL,
    VALUE REAL,
    
    CONSTRAINT TISE_PK PRIMARY KEY (ID),
    CONSTRAINT TISE_DASE_FK FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE
);

/* ---------------------------------------------------------------------- */
/* Add indices                                                            */
/* ---------------------------------------------------------------------- */

CREATE INDEX TISE_DASE_FK_I ON TIME_SERIES (DASE_ID);
CREATE INDEX TISE_VALUE_GROUP_ID_I ON TIME_SERIES (VALUE_GROUP_ID);
CREATE INDEX TISE_IDENTIFIER_I ON TIME_SERIES (IDENTIFIER);
CREATE INDEX TISE_EXPERIMENT_TYPE_I ON TIME_SERIES (EXPERIMENT_TYPE);
CREATE INDEX TISE_CONTROLLED_GENE_ID ON TIME_SERIES (CONTROLLED_GENE);
CREATE INDEX TISE_CULTIVATION_METHOD_I ON TIME_SERIES (CULTIVATION_METHOD);
CREATE INDEX TISE_BIOLOGICAL_REPLICATES_I ON TIME_SERIES (BIOLOGICAL_REPLICATES);
CREATE INDEX TISE_TIME_POINT_I ON TIME_SERIES (TIME_POINT);
CREATE INDEX TISE_TIME_POINT_TYPE_I ON TIME_SERIES (TIME_POINT_TYPE);
CREATE INDEX TISE_TECHNICAL_REPLICATES_I ON TIME_SERIES (TECHNICAL_REPLICATES);
CREATE INDEX TISE_CELL_LOCATION_I ON TIME_SERIES (CELL_LOCATION);
CREATE INDEX TISE_DATA_SET_TYPE_I ON TIME_SERIES (DATA_SET_TYPE);
CREATE INDEX TISE_VALUE_TYPE_I ON TIME_SERIES (VALUE_TYPE);
CREATE INDEX TISE_UNIT_I ON TIME_SERIES (UNIT);
CREATE INDEX TISE_SCALE_I ON TIME_SERIES (SCALE);

/* ---------------------------------------------------------------------- */
/* Add table "CHIP_CHIP_DATA"                                             */
/* ---------------------------------------------------------------------- */

CREATE SEQUENCE CHIP_CHIP_VALUE_GROUP_ID_SEQ;

CREATE TABLE CHIP_CHIP_DATA (
    ID BIGSERIAL  NOT NULL,
    DASE_ID TECH_ID NOT NULL,
    ROW_INDEX INTEGER NOT NULL,
    
    BSU_IDENTIFIER PROPERTY NOT NULL,
    GENE_NAME PROPERTY,
    GENE_FUNCTION PROPERTY,
    ARRAY_DESIGN PROPERTY,
    MICROARRAY_ID INTEGER,
    
    EXPERIMENT_TYPE PROPERTY NOT NULL,
    CULTIVATION_METHOD PROPERTY NOT NULL,
    BIOLOGICAL_REPLICATES PROPERTY NOT NULL,
    TECHNICAL_REPLICATES PROPERTY NOT NULL,
    CELL_LOCATION PROPERTY NOT NULL,
    GROWTH_PHASE PROPERTY  NOT NULL,
    GENOTYPE PROPERTY  NOT NULL,
    
    CHIP_PEAK_POSITION_VALUE INTEGER,
    CHIP_PEAK_POSITION_SCALE PROPERTY NOT NULL,
    CHIP_LOCAL_HEIGHT_VALUE REAL,
    CHIP_LOCAL_HEIGHT_SCALE PROPERTY NOT NULL,
    CHIP_SCORE_VALUE REAL,
    CHIP_SCORE_SCALE PROPERTY NOT NULL,
    
    INTERGENIC BOOLEAN NOT NULL,
    NEARBY_GENE_NAMES PROPERTY,
    NEARBY_GENE_IDS PROPERTY,
    DISTANCES_FROM_START PROPERTY,
    
    CONSTRAINT CHCH_PK PRIMARY KEY (ID),
    CONSTRAINT CHCH_DASE_FK FOREIGN KEY (DASE_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE
);

CREATE INDEX CHCH_EXPERIMENT_TYPE_I ON CHIP_CHIP_DATA (EXPERIMENT_TYPE);
CREATE INDEX CHCH_CULTIVATION_METHOD_I ON CHIP_CHIP_DATA (CULTIVATION_METHOD);
CREATE INDEX CHCH_BIOLOGICAL_REPLICATES_I ON CHIP_CHIP_DATA (BIOLOGICAL_REPLICATES);
CREATE INDEX CHCH_TECHNICAL_REPLICATES_I ON CHIP_CHIP_DATA (TECHNICAL_REPLICATES);
CREATE INDEX CHCH_CELL_LOCATION_I ON CHIP_CHIP_DATA (CELL_LOCATION);
CREATE INDEX CHCH_GROWTH_PHASE_I ON CHIP_CHIP_DATA (GROWTH_PHASE);
CREATE INDEX CHCH_GENOTYPE_I ON CHIP_CHIP_DATA (GENOTYPE);

/* ---------------------------------------------------------------------- */
/* Add table "EVENTS"                                                     */
/* ---------------------------------------------------------------------- */

CREATE TABLE EVENTS (
    LAST_SEEN_DELETION_EVENT_ID BIGINT  NOT NULL
);


