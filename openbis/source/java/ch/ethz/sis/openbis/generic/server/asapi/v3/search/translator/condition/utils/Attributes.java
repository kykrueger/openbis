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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.AUTHORIZATION_GROUPS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.CONTROLLED_VOCABULARY_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.CONTROLLED_VOCABULARY_TERM_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_SET_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_STORES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.EXPERIMENTS_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.EXPERIMENT_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.FILE_FORMAT_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.GRID_CUSTOM_COLUMNS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.LOCATOR_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.MATERIALS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.MATERIAL_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.OPERATION_EXECUTIONS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROJECTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROPERTY_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.RELATIONSHIP_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLE_TYPES_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SEMANTIC_ANNOTATIONS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SPACES_TABLE;

public class Attributes
{
    private static final String PERM_ID_ATTRIBUTE = "perm id";

    private static final Map<String, String> ATTRIBUTE_ID_TO_COLUMN_NAME = new HashMap<>();

    private static final Map<String, String> ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME = new HashMap<>();

    static {
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(PERM_ID_ATTRIBUTE, ColumnNames.PERM_ID_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.CODE, ColumnNames.CODE_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("codes", ColumnNames.CODE_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.ID, ColumnNames.ID_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.FILE_DESCRIPTION, ColumnNames.DESCRIPTION_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.MODIFICATION_DATE, ColumnNames.MODIFICATION_TIMESTAMP_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.REGISTRATION_DATE, ColumnNames.REGISTRATION_TIMESTAMP_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("userIds", ColumnNames.USER_COLUMN);

        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(CONTROLLED_VOCABULARY_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(CONTROLLED_VOCABULARY_TERM_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(DATA_SET_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(DATA_STORES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(DATA_ALL_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(DATA_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(GRID_CUSTOM_COLUMNS_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(EXPERIMENT_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(FILE_FORMAT_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(SPACES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(LOCATOR_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(MATERIAL_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(MATERIALS_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(PROPERTY_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(SAMPLE_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(RELATIONSHIP_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(AUTHORIZATION_GROUPS_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(OPERATION_EXECUTIONS_TABLE, ColumnNames.CODE_COLUMN);

        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(EXPERIMENTS_ALL_TABLE, ColumnNames.PERM_ID_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(PROJECTS_TABLE, ColumnNames.PERM_ID_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(SAMPLES_ALL_TABLE, ColumnNames.PERM_ID_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(SEMANTIC_ANNOTATIONS_TABLE, ColumnNames.PERM_ID_COLUMN);
    }

    private Attributes()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String getColumnName(final String attributeId, final String tableName, final String defaultValue)
    {
        return attributeId.equals(PERM_ID_ATTRIBUTE) ?
                ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.getOrDefault(tableName, defaultValue) :
                ATTRIBUTE_ID_TO_COLUMN_NAME.getOrDefault(attributeId, defaultValue);
    }

}
