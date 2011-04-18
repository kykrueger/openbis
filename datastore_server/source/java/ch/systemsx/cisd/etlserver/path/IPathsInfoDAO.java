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

package ch.systemsx.cisd.etlserver.path;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

/**
 * Data Access Object for feeding pathinfo database.
 *
 * @author Franz-Josef Elmer
 */
public interface IPathsInfoDAO extends TransactionQuery
{
    @Select("insert into data_sets (code, location) values (?{1}, ?{2}) returning id")
    public long createDataSet(String code, String location);
    
    @Select("insert into data_set_files (dase_id, parent_id, relative_path, file_name, "
            + "size_in_bytes, is_directory) values (?{1}, ?{2}, ?{3}, ?{4}, ?{5}, ?{6}) returning id")
    public long createDataSetFile(long dataSetId, Long parentId, String relativePath,
            String fileName, long sizeInBytes, boolean directory);
    
}
