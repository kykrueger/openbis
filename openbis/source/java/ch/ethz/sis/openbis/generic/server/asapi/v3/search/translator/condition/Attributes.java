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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

import java.util.HashMap;
import java.util.Map;

public class Attributes
{
    // SearchFieldType.ATTRIBUTE
    // "registration_date"
    // "code"
    // "codes" (collection case)
    // "identifier"
    // "perm id"
    // "ids" (find out what kind of ids are first)
    // "id"
    // "description"
    // "any"
    // "name"
    // "modification_date"

    public static final Map<String, String> ATTRIBUTE_ID_TO_COLUMN_NAME = new HashMap<>();

    static {
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("perm id", ColumnNames.PERM_ID_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.REGISTRATION_DATE, ColumnNames.REGISTRATION_TIMESTAMP_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.CODE, ColumnNames.CODE_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("codes", ColumnNames.CODE_COLUMN);
//        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.IDENTIFIER, ?);
//        ATTRIBUTE_ID_TO_COLUMN_NAME.put("ids", ?);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.ID, ColumnNames.ID_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.FILE_DESCRIPTION, ColumnNames.DESCRIPTION_COLUMN);
//        ATTRIBUTE_ID_TO_COLUMN_NAME.put("any", ?);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.FILE_NAME, ColumnNames.FILE_NAME_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.MODIFICATION_DATE, ColumnNames.MODIFICATION_TIMESTAMP_COLUMN);
    }

    private Attributes()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

}
