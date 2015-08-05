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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relation;

/**
 * @author pkupczyk
 */
public abstract class ObjectBaseRelation<RECORD extends ObjectBaseRecord> implements Relation
{

    private Collection<Long> objectIds;

    private Map<Long, RECORD> recordMap = new HashMap<Long, RECORD>();

    public ObjectBaseRelation(Collection<Long> objectIds)
    {
        this.objectIds = objectIds;
    }

    @Override
    public void load()
    {
        List<RECORD> records = load(new LongOpenHashSet(objectIds));

        for (RECORD record : records)
        {
            recordMap.put(record.id, record);
        }
    }

    protected abstract List<RECORD> load(@SuppressWarnings("hiding")
    LongOpenHashSet objectIds);

    public RECORD getRecord(Long materialId)
    {
        return recordMap.get(materialId);
    }

}
