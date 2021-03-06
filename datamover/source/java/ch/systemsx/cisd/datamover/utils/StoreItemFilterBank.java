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

package ch.systemsx.cisd.datamover.utils;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.IStoreItemFilter;
import ch.systemsx.cisd.common.filesystem.StoreItem;

/**
 * A filter bank of {@link IStoreItemFilter} instances. A {@link StoreItem} tries to passes the filters of the filter bank in the order the filters
 * have been added (with {@link #add(IStoreItemFilter)}). If the item does not pass a filter (i.e. {@link IStoreItemFilter#accept(StoreItem)} return
 * <code>false</code>) it does not pass the filter bank. Note, that in this case the other filter are not checked.
 * 
 * @author Franz-Josef Elmer
 */
public class StoreItemFilterBank implements IStoreItemFilter
{
    private final List<IStoreItemFilter> filters = new ArrayList<IStoreItemFilter>();

    /**
     * Adds the specified filter.
     */
    public void add(IStoreItemFilter filter)
    {
        filters.add(filter);
    }

    /**
     * Accepts the specified item if it accepted by all filters.
     */
    @Override
    public boolean accept(StoreItem item)
    {
        for (IStoreItemFilter filter : filters)
        {
            if (filter.accept(item) == false)
            {
                return false;
            }
        }
        return true;
    }

}
