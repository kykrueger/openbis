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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CHILD_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.DATA_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.DESCRIPTION_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.EMAIL_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.EXPERIMENT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.FIRST_NAME_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.GENERATED_CODE_PREFIX;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.GENERATED_FROM_DEPTH;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.IS_AUTO_GENERATED_CODE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.IS_LISTABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.IS_SUBCODE_UNIQUE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.LAST_NAME_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PARENT_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_DEPTH;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERM_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_IS_ACTIVE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_MODIFIER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_REGISTERER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROPERTY_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SHOW_PARENT_METADATA;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.USER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.VALIDATION_SCRIPT_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PERSONS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROPERTY_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_PROPERTIES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_RELATIONSHIPS_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_TYPE_PROPERTY_TYPE_TABLE;

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

    SAMPLE(SAMPLES_ALL_TABLE, SAMPLE_TYPE_COLUMN, PROPERTY_TYPES_TABLE, DATA_TYPE_COLUMN, SAMPLE_TYPES_TABLE,
            SAMPLE_TYPE_PROPERTY_TYPE_TABLE, SAMPLE_TYPE_COLUMN, PROPERTY_TYPE_COLUMN, SAMPLE_PROPERTIES_TABLE, SAMPLE_COLUMN,
            SAMPLE_TYPE_PROPERTY_TYPE_COLUMN, SAMPLE_RELATIONSHIPS_ALL_TABLE, PARENT_SAMPLE_COLUMN, CHILD_SAMPLE_COLUMN, DATA_ALL_TABLE,
            SAMPLE_COLUMN),

    SAMPLE_TYPE(SAMPLE_TYPES_TABLE, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null),

    PERSON(PERSONS_TABLE, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null);

    static
    {
        initSampleFieldToSQLTypeMap();
        initSampleSQLTypeToFieldsMap();
        initSampleTypeFieldToSQLTypeMap();
        initSampleTypeSQLTypeToFieldsMap();
        initPersonFieldToSQLTypeMap();
        initPersonSQLTypeToFieldsMap();
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
    private Map<String, SQLTypes> fieldToSQLTypeMap = new HashMap<>();

    private Map<SQLTypes, Set<String>> sqlTypeToFieldsMap = new HashMap<>();


    TableMapper(final String entitiesTable, final String entitiesTableEntityTypeIdField, final String attributeTypesTable,
            final String attributeTypesTableDataTypeIdField, final String entityTypesTable, final String entityTypesAttributeTypesTable,
            final String entityTypesAttributeTypesTableEntityTypeIdField, final String entityTypesAttributeTypesTableAttributeTypeIdField,
            final String valuesTable, final String valuesTableEntityIdField, final String valuesTableEntityTypeAttributeTypeIdField,
            final String relationshipsTable, final String relationshipsTableParentIdField, final String relationshipsTableChildIdField,
            final String dataTable, final String dataTableEntityIdField)
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

    public Map<String, SQLTypes> getFieldToSQLTypeMap()
    {
        return fieldToSQLTypeMap;
    }

    private static void initSampleFieldToSQLTypeMap()
    {
        final Map<String, SQLTypes> fields = SAMPLE.fieldToSQLTypeMap;
        fields.put(PERM_ID_COLUMN, SQLTypes.VARCHAR);
        fields.put(CODE_COLUMN, SQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, SQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, SQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(EXPERIMENT_COLUMN, SQLTypes.INT8);
        fields.put(SAMPLE_TYPE_COLUMN, SQLTypes.INT8);
        fields.put(PERSON_REGISTERER_COLUMN, SQLTypes.INT8);
        fields.put(SPACE_COLUMN, SQLTypes.INT8);
        fields.put(PART_OF_SAMPLE_COLUMN, SQLTypes.INT8);
        fields.put(PERSON_MODIFIER_COLUMN, SQLTypes.INT8);
        fields.put(PROJECT_COLUMN, SQLTypes.INT8);
    }

    private static void initSampleTypeFieldToSQLTypeMap()
    {
        final Map<String, SQLTypes> fields = SAMPLE_TYPE.fieldToSQLTypeMap;
        fields.put(CODE_COLUMN, SQLTypes.VARCHAR);
        fields.put(DESCRIPTION_COLUMN, SQLTypes.VARCHAR);
        fields.put(GENERATED_CODE_PREFIX, SQLTypes.VARCHAR);
        fields.put(MODIFICATION_TIMESTAMP_COLUMN, SQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(GENERATED_FROM_DEPTH, SQLTypes.INT4);
        fields.put(PART_OF_DEPTH, SQLTypes.INT4);
        fields.put(VALIDATION_SCRIPT_ID_COLUMN, SQLTypes.INT8);
        fields.put(IS_AUTO_GENERATED_CODE, SQLTypes.BOOLEAN);
        fields.put(IS_SUBCODE_UNIQUE, SQLTypes.BOOLEAN);
        fields.put(IS_LISTABLE, SQLTypes.BOOLEAN);
        fields.put(SHOW_PARENT_METADATA, SQLTypes.BOOLEAN);
    }

    private static void initPersonFieldToSQLTypeMap()
    {
        final Map<String, SQLTypes> fields = PERSON.fieldToSQLTypeMap;
        fields.put(FIRST_NAME_COLUMN, SQLTypes.VARCHAR);
        fields.put(LAST_NAME_COLUMN, SQLTypes.VARCHAR);
        fields.put(USER_COLUMN, SQLTypes.VARCHAR);
        fields.put(EMAIL_COLUMN, SQLTypes.VARCHAR);
        fields.put(REGISTRATION_TIMESTAMP_COLUMN, SQLTypes.TIMESTAMP_WITH_TZ);
        fields.put(PERSON_IS_ACTIVE_COLUMN, SQLTypes.BOOLEAN);
    }

    private static void initSampleTypeSQLTypeToFieldsMap()
    {
        final Map<SQLTypes, Set<String>> map = SAMPLE_TYPE.sqlTypeToFieldsMap;
        map.put(SQLTypes.VARCHAR, new HashSet<>(Arrays.asList(CODE_COLUMN, DESCRIPTION_COLUMN, GENERATED_CODE_PREFIX)));
        map.put(SQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(MODIFICATION_TIMESTAMP_COLUMN)));
        map.put(SQLTypes.INT4, new HashSet<>(Arrays.asList(GENERATED_FROM_DEPTH, PART_OF_DEPTH)));
        map.put(SQLTypes.INT8, new HashSet<>(Arrays.asList(VALIDATION_SCRIPT_ID_COLUMN)));
        map.put(SQLTypes.BOOLEAN, new HashSet<>(Arrays.asList(IS_AUTO_GENERATED_CODE, IS_SUBCODE_UNIQUE, IS_LISTABLE, SHOW_PARENT_METADATA)));
    }

    private static void initSampleSQLTypeToFieldsMap()
    {
        final Map<SQLTypes, Set<String>> map = SAMPLE.sqlTypeToFieldsMap;
        map.put(SQLTypes.VARCHAR, new HashSet<>(Arrays.asList(PERM_ID_COLUMN, CODE_COLUMN)));
        map.put(SQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN, MODIFICATION_TIMESTAMP_COLUMN)));
        map.put(SQLTypes.INT8, new HashSet<>(Arrays.asList(EXPERIMENT_COLUMN, SAMPLE_TYPE_COLUMN, PERSON_REGISTERER_COLUMN, SPACE_COLUMN,
                PART_OF_SAMPLE_COLUMN, PERSON_MODIFIER_COLUMN, PROJECT_COLUMN)));
    }

    private static void initPersonSQLTypeToFieldsMap()
    {
        final Map<SQLTypes, Set<String>> map = PERSON.sqlTypeToFieldsMap;
        map.put(SQLTypes.VARCHAR, new HashSet<>(Arrays.asList(FIRST_NAME_COLUMN, LAST_NAME_COLUMN, USER_COLUMN, EMAIL_COLUMN)));
        map.put(SQLTypes.TIMESTAMP_WITH_TZ, new HashSet<>(Arrays.asList(REGISTRATION_TIMESTAMP_COLUMN)));
        map.put(SQLTypes.BOOLEAN, new HashSet<>(Arrays.asList(PERSON_IS_ACTIVE_COLUMN)));
    }

}
