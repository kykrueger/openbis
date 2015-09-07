/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.fetchoptions.sort.SortOptions")
public abstract class SortOptions<OBJECT> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private transient Map<String, Comparator<OBJECT>> comparators;

    private List<Sorting> sortings = new LinkedList<>();

    protected void addComparators(Map<String, Comparator<OBJECT>> map)
    {
    }

    public final Comparator<OBJECT> getComparator(String field)
    {
        if (comparators == null)
        {
            Map<String, Comparator<OBJECT>> map = new HashMap<String, Comparator<OBJECT>>();
            addComparators(map);
            comparators = map;
        }

        return comparators.get(field);
    }

    protected SortOrder getOrCreateSorting(String field)
    {
        SortOrder order = getSorting(field);
        if (order == null)
        {
            order = new SortOrder();
            sortings.add(new Sorting(field, order));
        }
        return order;
    }

    protected SortOrder getSorting(String field)
    {
        for (Sorting sorting : sortings)
        {
            if (field.equals(sorting.getField()))
            {
                return sorting.getOrder();
            }
        }
        return null;
    }

    public List<Sorting> getSortings()
    {
        return sortings;
    }

}
