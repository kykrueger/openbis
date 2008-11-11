/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.List;

/**
 * A default {@link IResultSet} implementation.
 * 
 * @author Christian Ribeaud
 */
final class DefaultResultSet<K, T> implements IResultSet<K, T>
{
    private final K resultSetKey;

    private final List<T> list;

    private final int totalLength;

    DefaultResultSet(final K resultSetKey, final List<T> list, final int totalLength)
    {
        assert resultSetKey != null : "Unspecified result set key";
        assert list != null : "Unspecified list.";
        assert totalLength > -1 : "Total length must be >= 0.";
        this.resultSetKey = resultSetKey;
        this.list = list;
        this.totalLength = totalLength;
    }

    //
    // IResult
    //

    public final K getResultSetKey()
    {
        return resultSetKey;
    }

    public final List<T> getList()
    {
        return list;
    }

    public final int getTotalLength()
    {
        return totalLength;
    }

}
