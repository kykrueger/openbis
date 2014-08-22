/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.xoai;

import java.util.LinkedList;
import java.util.List;

import com.lyncode.xoai.dataprovider.handlers.results.ListSetsResult;
import com.lyncode.xoai.dataprovider.model.Set;
import com.lyncode.xoai.dataprovider.repository.SetRepository;

/**
 * @author pkupczyk
 */
public class SimpleSetRepository implements SetRepository
{

    private List<Set> sets = new LinkedList<Set>();

    public void addSet(Set set)
    {
        sets.add(set);
    }

    @Override
    public boolean supportSets()
    {
        return sets.isEmpty() == false;
    }

    @Override
    public ListSetsResult retrieveSets(int offset, int length)
    {
        return new ListSetsResult(offset + length < this.sets.size(), this.sets.subList(offset, Math.min(offset + length, sets.size())));
    }

    @Override
    public boolean exists(String setSpec)
    {
        for (Set s : this.sets)
            if (s.getSpec().equals(setSpec))
                return true;

        return false;
    }

}
