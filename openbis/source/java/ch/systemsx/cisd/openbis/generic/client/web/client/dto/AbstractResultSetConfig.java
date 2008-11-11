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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

/**
 * An <code>abstract</code> {@link IResultSetConfig} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractResultSetConfig<K> implements IResultSetConfig<K>
{
    private int limit = -1;

    private int offset = 0;

    private SortInfo sortInfo = new SortInfo();

    /**
     * The result set key.
     * <p>
     * Could be <code>null</code> if unknown.
     * </p>
     */
    private K resultSetKey;

    public final void setLimit(final int limit)
    {
        this.limit = limit;
    }

    public final void setOffset(final int offset)
    {
        this.offset = offset;
    }

    public final void setSortInfo(final SortInfo sortInfo)
    {
        this.sortInfo = sortInfo;
    }

    public final void setResultSetKey(final K resultSetKey)
    {
        this.resultSetKey = resultSetKey;
    }

    //
    // IResultSetConfig
    //

    public final int getLimit()
    {
        return limit;
    }

    public final int getOffset()
    {
        return offset;
    }

    public final SortInfo getSortInfo()
    {
        return sortInfo;
    }

    public final K getResultSetKey()
    {
        return resultSetKey;
    }
}
