/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

import net.lemnik.eodsql.TypeMapper;

/**
 * A class for mapping <code>Set&lt;Long&gt;</code> to {@link java.sql.Array}.
 * 
 * @author Bernd Rinn
 */
public class LongSetMapper implements TypeMapper<Set<Long>>
{

    @Override
    public Set<Long> get(ResultSet results, int column) throws SQLException
    {
        return new LongOpenHashSet((long[]) results.getArray(column).getArray());
    }

    @Override
    public void set(PreparedStatement statement, int column, Set<Long> obj) throws SQLException
    {
        if (obj != null)
        {
            statement.setArray(column, new SimpleSQLLongArray(obj));
        } else
        {
            statement.setNull(column, Types.ARRAY);
        }
    }

    @Override
    public void set(ResultSet results, int column, Set<Long> obj) throws SQLException
    {
        results.updateArray(column, new SimpleSQLLongArray(obj));
    }
}
