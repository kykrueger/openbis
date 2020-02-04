/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;

import java.util.*;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

/**
 * Extension of enum {@link EntityKind} to contain extra information about tables related to the entities which can
 * have parent-child relationships.<p/>
 *
 * This enum also contains table information for non-entity search criteria (not related to MATERIAL, EXPERIMENT, SAMPLE, DATA_SET).
 *
 * @author Viktor Kovtun
 */
public enum TableMapper
{

    SAMPLE(SAMPLES_VIEW, SAMPLE_TYPE_COLUMN, PROPERTY_TYPES_TABLE, DATA_TYPE_COLUMN, SAMPLE_TYPES_TABLE,
            SAMPLE_TYPE_PROPERTY_TYPE_TABLE, SAMPLE_TYPE_COLUMN, PROPERTY_TYPE_COLUMN, SAMPLE_PROPERTIES_TABLE, SAMPLE_COLUMN,
            SAMPLE_TYPE_PROPERTY_TYPE_COLUMN, SAMPLE_RELATIONSHIPS_ALL_TABLE, PARENT_SAMPLE_COLUMN, CHILD_SAMPLE_COLUMN, DATA_VIEW,
            SAMPLE_COLUMN, SAMPLE_COLUMN),

    SAMPLE_TYPE(SAMPLE_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null),

    EXPERIMENT(EXPERIMENTS_VIEW, EXPERIMENT_TYPE_COLUMN, PROPERTY_TYPES_TABLE, DATA_TYPE_COLUMN, EXPERIMENT_TYPES_TABLE,
            EXPERIMENT_TYPE_PROPERTY_TYPE_TABLE, EXPERIMENT_TYPE_COLUMN, PROPERTY_TYPE_COLUMN, EXPERIMENT_PROPERTIES_TABLE, EXPERIMENT_COLUMN,
            EXPERIMENT_TYPE_PROPERTY_TYPE_COLUMN, null, null, null, DATA_VIEW, EXPERIMENT_COLUMN, EXPERIMENT_COLUMN),

    EXPERIMENT_TYPE(EXPERIMENT_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    DATA_SET(DATA_VIEW, DATA_SET_TYPE_COLUMN, PROPERTY_TYPES_TABLE, DATA_TYPE_COLUMN, DATA_SET_TYPES_TABLE, DATA_SET_TYPE_PROPERTY_TYPE_TABLE,
            DATA_SET_TYPE_COLUMN, PROPERTY_TYPE_COLUMN, DATA_SET_PROPERTIES_TABLE, DATA_SET_COLUMN, DATA_SET_TYPE_PROPERTY_TYPE_COLUMN,
            DATA_SET_RELATIONSHIPS_ALL_TABLE, DATA_PARENT_COLUMN, DATA_CHILD_COLUMN, DATA_VIEW, ID_COLUMN, DATA_ID_COLUMN),

    DATA_SET_TYPE(DATA_SET_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    PERSON(PERSONS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    PROJECT(PROJECTS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    SPACE(SPACES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    TAG(METAPROJECTS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    SEMANTIC_ANNOTATION(SEMANTIC_ANNOTATIONS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    PROPERTY_TYPE(PROPERTY_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    SAMPLE_PROPERTY_ASSIGNMENT(SAMPLE_TYPE_PROPERTY_TYPE_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null),

    CONTENT_COPIES(CONTENT_COPIES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    EXTERNAL_DATA(EXTERNAL_DATA_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),

    EXTERNAL_DMS(EXTERNAL_DATA_MANAGEMENT_SYSTEMS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    static
    {
        initSampleFieldToSQLTypeMap();
        initSampleSQLTypeToFieldsMap();

        initSampleTypeFieldToSQLTypeMap();
        initSampleTypeSQLTypeToFieldsMap();

        initPersonFieldToSQLTypeMap();
        initPersonSQLTypeToFieldsMap();

        initExperimentFieldToSQLTypeMap();
        initExperimentSQLTypeToFieldsMap();

        initExperimentTypeFieldToSQLTypeMap();
        initExperimentTypeSQLTypeToFieldsMap();

        initProjectFieldToSQLTypeMap();
        initProjectSQLTypeToFieldsMap();

        initSpaceFieldToSQLTypeMap();
        initSpaceSQLTypeToFieldsMap();

        initDataSetFieldToSQLTypeMap();
        initDataSetSQLTypeToFieldsMap();

        initDataSetTypeFieldToSQLTypeMap();
        initDataSetTypeSQLTypeToFieldsMap();
    }


    /*
     * Entities Table
     */
    private String entitiesTable;

    private String entitiesTableEntityTypeIdField;

    private String attributeTypesTableDataTypeIdField;


    /*
     * Common Attribute definition Tables
     */
    private String attributeTypesTable;

    /*
     * Entity Attribute definition Tables
     */
    private String entityTypesTable;

    private String entityTypesAttributeTypesTable;

    private String entityTypesAttributeTypesTableEntityTypeIdField; //saty

    private String entityTypesAttributeTypesTableAttributeTypeIdField; //prty


    /*
     * Entity Values Tables
     */
    private String valuesTable;

    private String valuesTableEntityIdField;

    private String valuesTableEntityTypeAttributeTypeIdField;


    /*
     * Relationships Tables
     */
    private String relationshipsTable;

    private String relationshipsTableParentIdField;

    private String relationshipsTableChildIdField;


    /*
     * Files tables
     */
    private String dataTable;

    private String dataTableEntityIdField;

    /*
     * Other
     */

    private String metaprojectAssignmentsEntityIdField;

    private Map<String, PSQLTypes> fieldToSQLTypeMap = new HashMap<>();

    private Map<PSQLTypes, Set<String>> sqlTypeToFieldsMap = new HashMap<>();

    TableMapper(final String entitiesTable, final String entitiesTableEntityTypeIdField, final String attributeTypesTable,
            final String attributeTypesTableDataTypeIdField, final String entityTypesTable, final String entityTypesAttributeTypesTable,
            final String entityTypesAttributeTypesTableEntityTypeIdField, final String entityTypesAttributeTypesTableAttributeTypeIdField,
            final String valuesTable, final String valuesTableEntityIdField, final String valuesTableEntityTypeAttributeTypeIdField,
            final String relationshipsTable, final String relationshipsTableParentIdField, final String relationshipsTableChildIdField,
            final String dataTable, final String dataTableEntityIdField, final String metaprojectAssignmentsEntityIdField)
    {
        this.entitiesTable = entitiesTable;
        this.entitiesTableEntityTypeIdField = entitiesTableEntityTypeIdField;
        this.attributeTypesTable = attributeTypesTable;
        this.attributeTypesTableDataTypeIdField = attributeTypesTableDataTypeIdField;
        this.entityTypesTable = entityTypesTable;
        this.entityTypesAttributeTypesTable = entityTypesAttributeTypesTable;
        this.entityTypesAttributeTypesTableEntityTypeIdField = entityTypesAttributeTypesTableEntityTypeIdField;
        this.entityTypesAttributeTypesTableAttributeTypeIdField = entityTypesAttributeTypesTableAttributeTypeIdField;
        this.valuesTable = valuesTable;
        this.valuesTableEntityIdField = valuesTableEntityIdField;
        this.valuesTableEntityTypeAttributeTypeIdField = valuesTableEntityTypeAttributeTypeIdField;
        this.relationshipsTable = relationshipsTable;
        this.relationshipsTableParentIdField = relationshipsTableParentIdField;
        this.relationshipsTableChildIdField = relationshipsTableChildIdField;
        this.dataTable = dataTable;
        this.dataTableEntityIdField = dataTableEntityIdField;
        this.metaprojectAssignmentsEntityIdField = metaprojectAssignmentsEntityIdField;
    }

    public static TableMapper toEntityMapper(final EntityKind entityKind, final boolean isEntityType)
    {
        // By convention entity mapper values corresponding to types should end with "_TYPE"
        return TableMapper.valueOf(entityKind.name() + (isEntityType ? "_TYPE" : ""));
    }

    public String getEntitiesTable()
    {
        return entitiesTable;
    }

    public String getEntitiesTableEntityTypeIdField()
    {
        return entitiesTableEntityTypeIdField;
    }

    public String getAttributeTypesTable()
    {
        return attributeTypesTable;
    }

    public String getAttributeTypesTableDataTypeIdField()
    {
        return attributeTypesTableDataTypeIdField;
    }

    public String getEntityTypesTable()
    {
        return entityTypesTable;
    }

    public String getEntityTypesAttributeTypesTable()
    {
        return entityTypesAttributeTypesTable;
    }

    public String getEntityTypesAttributeTypesTableEntityTypeIdField()
    {
        return entityTypesAttributeTypesTableEntityTypeIdField;
    }

    public String getEntityTypesAttributeTypesTableAttributeTypeIdField()
    {
        return entityTypesAttributeTypesTableAttributeTypeIdField;
    }

    public String getValuesTable()
    {
        return valuesTable;
    }

    public String getValuesTableEntityIdField()
    {
        return valuesTableEntityIdField;
    }

    public String getValuesTableEntityTypeAttributeTypeIdField()
    {
        return valuesTableEntityTypeAttributeTypeIdField;
    }

    public String getRelationshipsTable()
    {
        return relationshipsTable;
    }

    public String getRelationshipsTableParentIdField()
    {
        return relationshipsTableParentIdField;
    }

    public String getRelationshipsTableChildIdField()
    {
        return relationshipsTableChildIdField;
    }

    public String getDataTable()
    {
        return dataTable;
    }

    public String getDataTableEntityIdField()
    {
        return dataTableEntityIdField;
    }

    public Map<String, PSQLTypes> getFieldToSQLTypeMap()
    {
        return fieldToSQLTypeMap;
    }

    public String getMetaprojectAssignmentsEntityIdField()
    {
        return metaprojectAssignmentsEntityIdField;
    }

    private static void initSampleFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = SAMPLE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(EXPERIMENT_COLUMN, PSQLTypes.INT8);
        fields.put(SAMPLE_TYPE_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_REGISTERER_COLUMN, PSQLTypes.INT8);
        fields.put(SPACE_COLUMN, PSQLTypes.INT8);
        fields.put(PART_OF_SAMPLE_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_MODIFIER_COLUMN, PSQLTypes.INT8);
        fields.put(PROJECT_COLUMN, PSQLTypes.INT8);
    }

    private static void initSampleTypeFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = SAMPLE_TYPE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(DESCRIPTION_COLUMN, PSQLTypes.VARCHAR);
        fields.put(GENERATED_CODE_PREFIX, PSQLTypes.VARCHAR);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(GENERATED_FROM_DEPTH, PSQLTypes.INT4);
        fields.put(PART_OF_DEPTH, PSQLTypes.INT4);
        fields.put(VALIDATION_SCRIPT_ID_COLUMN, PSQLTypes.INT8);
        fields.put(IS_AUTO_GENERATED_CODE, PSQLTypes.BOOLEAN);
        fields.put(IS_SUBCODE_UNIQUE, PSQLTypes.BOOLEAN);
        fields.put(IS_LISTABLE, PSQLTypes.BOOLEAN);
        fields.put(SHOW_PARENT_METADATA, PSQLTypes.BOOLEAN);
    }

    private static void initExperimentFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = EXPERIMENT.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(EXPERIMENT_TYPE_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_REGISTERER_COLUMN, PSQLTypes.INT8);
        fields.put(PROJECT_COLUMN, PSQLTypes.INT8);
        fields.put(DELETION_COLUMN, PSQLTypes.INT8);
        fields.put(ORIGINAL_DELETION_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_MODIFIER_COLUMN, PSQLTypes.INT8);
        fields.put(IS_PUBLIC, PSQLTypes.BOOLEAN);
    }

    private static void initExperimentTypeFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = EXPERIMENT_TYPE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(DESCRIPTION_COLUMN, PSQLTypes.VARCHAR);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(VALIDATION_SCRIPT_ID_COLUMN, PSQLTypes.INT8);
    }

    private static void initPersonFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = PERSON.fieldToSQLTypeMap;
        fields.put(FIRST_NAME_COLUMN, PSQLTypes.VARCHAR);
        fields.put(LAST_NAME_COLUMN, PSQLTypes.VARCHAR);
        fields.put(USER_COLUMN, PSQLTypes.VARCHAR);
        fields.put(EMAIL_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(PERSON_IS_ACTIVE_COLUMN, PSQLTypes.BOOLEAN);
    }

    private static void initSampleSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = SAMPLE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN, MODIFICATION_TIMESTAMP_COLUMN)));
        map.put(PSQLTypes.INT8, new HashSet<>(Arrays.asList(EXPERIMENT_COLUMN, SAMPLE_TYPE_COLUMN, PERSON_REGISTERER_COLUMN, SPACE_COLUMN,
                PART_OF_SAMPLE_COLUMN, PERSON_MODIFIER_COLUMN, PROJECT_COLUMN)));
    }

    private static void initSampleTypeSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = SAMPLE_TYPE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, DESCRIPTION_COLUMN, GENERATED_CODE_PREFIX)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(MODIFICATION_TIMESTAMP_COLUMN)));
        map.put(PSQLTypes.INT4, new HashSet<>(Arrays.asList(GENERATED_FROM_DEPTH, PART_OF_DEPTH)));
        map.put(PSQLTypes.INT8, new HashSet<>(Arrays.asList(VALIDATION_SCRIPT_ID_COLUMN)));
        map.put(PSQLTypes.BOOLEAN, new HashSet<>(Arrays.asList(IS_AUTO_GENERATED_CODE, IS_SUBCODE_UNIQUE, IS_LISTABLE, SHOW_PARENT_METADATA)));
    }

    private static void initPersonSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = PERSON.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(FIRST_NAME_COLUMN, LAST_NAME_COLUMN, USER_COLUMN, EMAIL_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
        map.put(PSQLTypes.BOOLEAN, new HashSet<>(Arrays.asList(PERSON_IS_ACTIVE_COLUMN)));
    }

    private static void initExperimentSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = EXPERIMENT.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(GENERATED_CODE_PREFIX, CODE_COLUMN, DESCRIPTION_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN, MODIFICATION_TIMESTAMP_COLUMN)));
        map.put(PSQLTypes.INT8, new HashSet<>(Arrays.asList(EXPERIMENT_TYPE_COLUMN, PERSON_REGISTERER_COLUMN, PROJECT_COLUMN, DELETION_COLUMN,
                ORIGINAL_DELETION_COLUMN)));
        map.put(PSQLTypes.BOOLEAN, new HashSet<>(Arrays.asList(IS_PUBLIC)));
    }

    private static void initExperimentTypeSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = SAMPLE_TYPE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, DESCRIPTION_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(MODIFICATION_TIMESTAMP_COLUMN)));
        map.put(PSQLTypes.INT8, new HashSet<>(Arrays.asList(VALIDATION_SCRIPT_ID_COLUMN)));
    }

    private static void initProjectFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = PROJECT.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(DESCRIPTION_COLUMN, PSQLTypes.VARCHAR);
        fields.put(SPACE_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_LEADER_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_REGISTERER_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_MODIFIER_COLUMN, PSQLTypes.INT8);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
    }

    private static void initProjectSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = PROJECT.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, DESCRIPTION_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN, MODIFICATION_TIMESTAMP_COLUMN)));
        map.put(PSQLTypes.INT8, new HashSet<>(Arrays.asList(SPACE_COLUMN, PERSON_LEADER_COLUMN, PERSON_REGISTERER_COLUMN, PERSON_MODIFIER_COLUMN)));
    }

    private static void initSpaceFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = SPACE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(DESCRIPTION_COLUMN, PSQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(PERSON_REGISTERER_COLUMN, PSQLTypes.INT8);
    }

    private static void initSpaceSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = SPACE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, DESCRIPTION_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
        map.put(PSQLTypes.INT8, new HashSet<>(Arrays.asList(PERSON_REGISTERER_COLUMN)));
    }

    private static void initDataSetFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = DATA_SET.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(DATA_SET_KIND_COLUMN, PSQLTypes.VARCHAR);
        fields.put(DATA_PRODUCER_CODE_COLUMN, PSQLTypes.VARCHAR);

        fields.put(PRODUCTION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(ACCESS_TIMESTAMP, PSQLTypes.TIMESTAMP_WITH_TZ);

        fields.put(DATA_SET_TYPE_COLUMN, PSQLTypes.INT8);
        fields.put(DATA_STORE_COLUMN, PSQLTypes.INT8);
        fields.put(EXPERIMENT_COLUMN, PSQLTypes.INT8);
        fields.put(SAMPLE_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_REGISTERER_COLUMN, PSQLTypes.INT8);
        fields.put(PERSON_MODIFIER_COLUMN, PSQLTypes.INT8);
        fields.put(DELETION_COLUMN, PSQLTypes.INT8);
        fields.put(ORIGINAL_DELETION_COLUMN, PSQLTypes.INT8);

        fields.put(IS_DERIVED, PSQLTypes.BOOLEAN);
    }

    private static void initDataSetSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = DATA_SET.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, DATA_SET_KIND_COLUMN, DATA_PRODUCER_CODE_COLUMN)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(PRODUCTION_TIMESTAMP_COLUMN, REGISTRATION_TIMESTAMP_COLUMN,
                MODIFICATION_TIMESTAMP_COLUMN, ACCESS_TIMESTAMP)));
        map.put(PSQLTypes.INT8, new HashSet<>(Arrays.asList(DATA_SET_TYPE_COLUMN, DATA_STORE_COLUMN, EXPERIMENT_COLUMN, SAMPLE_COLUMN,
                PERSON_REGISTERER_COLUMN, PERSON_MODIFIER_COLUMN, DELETION_COLUMN, ORIGINAL_DELETION_COLUMN)));
        map.put(PSQLTypes.BOOLEAN, new HashSet<>(Arrays.asList(IS_DERIVED)));
    }

    private static void initDataSetTypeFieldToSQLTypeMap()
    {
        final Map<String, PSQLTypes> fields = DATA_SET_TYPE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, PSQLTypes.VARCHAR);
        fields.put(DESCRIPTION_COLUMN, PSQLTypes.VARCHAR);
        fields.put(MAIN_DS_PATTERN, PSQLTypes.VARCHAR);
        fields.put(MAIN_DS_PATH, PSQLTypes.VARCHAR);

        fields.put(MODIFICATION_TIMESTAMP_COLUMN, PSQLTypes.TIMESTAMP_WITH_TZ);

        fields.put(VALIDATION_SCRIPT_ID_COLUMN, PSQLTypes.INT8);

        fields.put(DELETION_DISALLOW, PSQLTypes.BOOLEAN);
    }

    private static void initDataSetTypeSQLTypeToFieldsMap()
    {
        final Map<PSQLTypes, Set<String>> map = DATA_SET_TYPE.sqlTypeToFieldsMap;
        map.put(PSQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, DESCRIPTION_COLUMN, MAIN_DS_PATTERN, MAIN_DS_PATH)));
        map.put(PSQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(MODIFICATION_TIMESTAMP_COLUMN)));
        map.put(PSQLTypes.INT8, new HashSet<>(Arrays.asList(VALIDATION_SCRIPT_ID_COLUMN)));
        map.put(PSQLTypes.BOOLEAN, new HashSet<>(Arrays.asList(DELETION_DISALLOW)));
    }

}
