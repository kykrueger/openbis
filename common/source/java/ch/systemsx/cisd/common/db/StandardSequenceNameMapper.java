/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.db;

import java.util.Map;

/**
 * Standard sequence name mapper which replaces the last character of the table name by <code>_id_seq</code>. This
 * generic rules applies only for sequence names not found in the map provided as constructor argument.
 * 
 * @author Franz-Josef Elmer
 */
public class StandardSequenceNameMapper implements ISequenceNameMapper
{
    private final Map<String, String> nonstandardMaping;

    /**
     * Creates an instance for the specified map of sequence name which are not map by the above mentioned rule.
     */
    public StandardSequenceNameMapper(final Map<String, String> nonstandardMaping)
    {
        this.nonstandardMaping = nonstandardMaping;
    }

    //
    // ISequenceNameMapper
    //

    public final String map(String tableName)
    {
        String tableNameInLowerCase = tableName.toLowerCase();
        String sequenceName = nonstandardMaping.get(tableNameInLowerCase);
        if (sequenceName == null)
        {
            sequenceName = tableNameInLowerCase.substring(0, tableNameInLowerCase.length() - 1) + "_id_seq";
        }
        return sequenceName;
    }

}
