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

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CHILD_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CONTAINER_FROZEN_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.DELETION_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.EXPERIMENT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.EXPERIMENT_FROZEN_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.FROZEN_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.FROZEN_FOR_CHILDREN_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.FROZEN_FOR_COMPONENT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.FROZEN_FOR_DATA_SET_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.FROZEN_FOR_PARENTS_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ORIGINAL_DELETION_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PARENT_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERM_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_MODIFIER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_REGISTERER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_FROZEN_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROPERTY_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_FROZEN_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.VERSION_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROPERTY_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_PROPERTIES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_RELATIONSHIPS_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_TYPE_PROPERTY_TYPE_TABLE;

/**
 * Extension of enum {@link EntityKind} to contain extra information about tables related to the entities which can
 * have parent-child relationships.
 *
 * @author Viktor Kovtun
 */
public enum EntityMapper
{

    SAMPLE(SAMPLES_ALL_TABLE, ID_COLUMN, SAMPLE_TYPE_COLUMN,
            PROPERTY_TYPES_TABLE, ID_COLUMN,
            SAMPLE_TYPES_TABLE, ID_COLUMN,
            SAMPLE_TYPE_PROPERTY_TYPE_TABLE, ID_COLUMN, PROPERTY_TYPE_COLUMN, SAMPLE_TYPE_COLUMN,
            SAMPLE_PROPERTIES_TABLE, ID_COLUMN, SAMPLE_COLUMN, SAMPLE_TYPE_PROPERTY_TYPE_COLUMN,
            SAMPLE_RELATIONSHIPS_ALL_TABLE, ID_COLUMN, PARENT_SAMPLE_COLUMN, CHILD_SAMPLE_COLUMN,
            DATA_ALL_TABLE, ID_COLUMN, SAMPLE_COLUMN,
            new String[] { ID_COLUMN, PERM_ID_COLUMN, CODE_COLUMN, EXPERIMENT_COLUMN, SAMPLE_TYPE_COLUMN, REGISTRATION_TIMESTAMP_COLUMN,
                    MODIFICATION_TIMESTAMP_COLUMN, PERSON_REGISTERER_COLUMN, DELETION_COLUMN, ORIGINAL_DELETION_COLUMN, SPACE_COLUMN,
                    PART_OF_SAMPLE_COLUMN, PERSON_MODIFIER_COLUMN, VERSION_COLUMN, PROJECT_COLUMN, FROZEN_COLUMN, FROZEN_FOR_COMPONENT_COLUMN,
                    FROZEN_FOR_CHILDREN_COLUMN, FROZEN_FOR_PARENTS_COLUMN, FROZEN_FOR_DATA_SET_COLUMN, SPACE_FROZEN_COLUMN, PROJECT_FROZEN_COLUMN,
                    EXPERIMENT_FROZEN_COLUMN, CONTAINER_FROZEN_COLUMN });

    /*
     * Entities Table
     */
    private String entitiesTable;

    private String entitiesTableIdField;

    private String entitiesTableEntityTypeIdField;

    /*
     * Common Attribute definition Tables
     */
    private String attributeTypesTable;

    private String attributeTypesTableIdField;

    /*
     * Entity Attribute definition Tables
     */
    private String entityTypesTable;

    private String entityTypesTableIdField;

    private String entityTypesAttributeTypesTable;

    private String entityTypesAttributeTypesTableIdField;

    private String entityTypesAttributeTypesTableEntityTypeIdField; //saty

    private String entityTypesAttributeTypesTableAttributeTypeIdField; //prty

    /*
     * Entity Values Tables
     */
    private String valuesTable;

    private String valuesTableIdField;

    private String valuesTableEntityIdField;

    private String valuesTableEntityTypeAttributeTypeIdField;

    /*
     * Relationships Tables
     */
    private String relationshipsTable;

    private String relationshipsTableIdField;

    private String relationshipsTableParentIdField;

    private String relationshipsTableChildIdField;

    /*
     * Files tables
     */
    private String dataTable;

    private String dataTableIdField;

    private String dataTableEntityIdField;

    private String[] allFields;

    EntityMapper(String entitiesTable, String entitiesTableIdField, String entitiesTableEntityTypeIdField, String attributeTypesTable,
            String attributeTypesTableIdField, String entityTypesTable, String entityTypesTableIdField, String entityTypesAttributeTypesTable,
            String entityTypesAttributeTypesTableIdField, String entityTypesAttributeTypesTableEntityTypeIdField, String entityTypesAttributeTypesTableAttributeTypeIdField,
            String valuesTable, String valuesTableIdField, String valuesTableEntityIdField,
            String valuesTableEntityTypeAttributeTypeIdField, String relationshipsTable, String relationshipsTableIdField,
            String relationshipsTableParentIdField, String relationshipsTableChildIdField, String dataTable, String dataTableIdField,
            String dataTableEntityIdField, final String[] allFields)
    {
        this.entitiesTable = entitiesTable;
        this.entitiesTableIdField = entitiesTableIdField;
        this.entitiesTableEntityTypeIdField = entitiesTableEntityTypeIdField;
        this.attributeTypesTable = attributeTypesTable;
        this.attributeTypesTableIdField = attributeTypesTableIdField;
        this.entityTypesTable = entityTypesTable;
        this.entityTypesTableIdField = entityTypesTableIdField;
        this.entityTypesAttributeTypesTable = entityTypesAttributeTypesTable;
        this.entityTypesAttributeTypesTableIdField = entityTypesAttributeTypesTableIdField;
        this.entityTypesAttributeTypesTableEntityTypeIdField = entityTypesAttributeTypesTableEntityTypeIdField;
        this.entityTypesAttributeTypesTableAttributeTypeIdField = entityTypesAttributeTypesTableAttributeTypeIdField;
        this.valuesTable = valuesTable;
        this.valuesTableIdField = valuesTableIdField;
        this.valuesTableEntityIdField = valuesTableEntityIdField;
        this.valuesTableEntityTypeAttributeTypeIdField = valuesTableEntityTypeAttributeTypeIdField;
        this.relationshipsTable = relationshipsTable;
        this.relationshipsTableIdField = relationshipsTableIdField;
        this.relationshipsTableParentIdField = relationshipsTableParentIdField;
        this.relationshipsTableChildIdField = relationshipsTableChildIdField;
        this.dataTable = dataTable;
        this.dataTableIdField = dataTableIdField;
        this.dataTableEntityIdField = dataTableEntityIdField;
        this.allFields = allFields;
    }

    public static EntityMapper toEntityMapper(final EntityKind entityKind)
    {
        return EntityMapper.valueOf(entityKind.name());
    }

    public String getEntitiesTable()
    {
        return entitiesTable;
    }

    public String getEntitiesTableIdField()
    {
        return entitiesTableIdField;
    }

    public String getEntitiesTableEntityTypeIdField()
    {
        return entitiesTableEntityTypeIdField;
    }

    public String getAttributeTypesTable()
    {
        return attributeTypesTable;
    }

    public String getAttributeTypesTableIdField()
    {
        return attributeTypesTableIdField;
    }

    public String getEntityTypesTable()
    {
        return entityTypesTable;
    }

    public String getEntityTypesTableIdField()
    {
        return entityTypesTableIdField;
    }

    public String getEntityTypesAttributeTypesTable()
    {
        return entityTypesAttributeTypesTable;
    }

    public String getEntityTypesAttributeTypesTableIdField()
    {
        return entityTypesAttributeTypesTableIdField;
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

    public String getValuesTableIdField()
    {
        return valuesTableIdField;
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

    public String getRelationshipsTableIdField()
    {
        return relationshipsTableIdField;
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

    public String getDataTableIdField()
    {
        return dataTableIdField;
    }

    public String getDataTableEntityIdField()
    {
        return dataTableEntityIdField;
    }

    public String[] getAllFields()
    {
        return allFields;
    }

}
