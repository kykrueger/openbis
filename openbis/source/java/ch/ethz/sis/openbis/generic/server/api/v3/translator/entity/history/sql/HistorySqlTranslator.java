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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.sql.IPersonSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.PropertyHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.history.HistoryEntryFetchOptions;

/**
 * @author pkupczyk
 */
public abstract class HistorySqlTranslator extends AbstractCachingTranslator<Long, ObjectHolder<List<HistoryEntry>>, HistoryEntryFetchOptions>
        implements IHistorySqlTranslator
{

    @Autowired
    private IPersonSqlTranslator personTranslator;

    protected abstract List<? extends HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds);

    protected abstract List<? extends HistoryRelationshipRecord> loadRelationshipHistory(Collection<Long> entityIds);

    @Override
    protected ObjectHolder<List<HistoryEntry>> createObject(TranslationContext context, Long entityId, HistoryEntryFetchOptions fetchOptions)
    {
        return new ObjectHolder<List<HistoryEntry>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> entityIds, HistoryEntryFetchOptions fetchOptions)
    {
        List<? extends HistoryPropertyRecord> properties = loadPropertyHistory(entityIds);
        List<? extends HistoryRelationshipRecord> relationships = loadRelationshipHistory(entityIds);

        Map<Long, Person> authorMap = new HashMap<>();

        if (fetchOptions.hasAuthor())
        {
            Set<Long> authorIds = new HashSet<Long>();
            if (properties != null)
            {
                for (HistoryPropertyRecord property : properties)
                {
                    if (property.authorId != null)
                    {
                        authorIds.add(property.authorId);
                    }
                }
            }
            if (relationships != null)
            {
                for (HistoryRelationshipRecord relationship : relationships)
                {
                    if (relationship.authorId != null)
                    {
                        authorIds.add(relationship.authorId);
                    }
                }
            }
            authorMap = personTranslator.translate(context, authorIds, fetchOptions.withAuthor());
        }

        Map<Long, List<HistoryEntry>> entriesMap = new HashMap<Long, List<HistoryEntry>>();

        if (properties != null)
        {
            createPropertyEntries(entriesMap, properties, authorMap, fetchOptions);
        }
        if (relationships != null)
        {
            createRelationshipEntries(entriesMap, relationships, authorMap, fetchOptions);
        }

        for (Long entityId : entityIds)
        {
            if (false == entriesMap.containsKey(entityId))
            {
                entriesMap.put(entityId, Collections.<HistoryEntry> emptyList());
            }
        }

        return entriesMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateObject(TranslationContext context, Long entityId, ObjectHolder<List<HistoryEntry>> result, Object relations,
            HistoryEntryFetchOptions fetchOptions)
    {
        Map<Long, List<HistoryEntry>> entriesMap = (Map<Long, List<HistoryEntry>>) relations;
        result.setObject(entriesMap.get(entityId));
    }

    private void createPropertyEntries(Map<Long, List<HistoryEntry>> entriesMap, List<? extends HistoryPropertyRecord> records,
            Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        for (HistoryPropertyRecord record : records)
        {
            List<HistoryEntry> entries = entriesMap.get(record.entityId);

            if (entries == null)
            {
                entries = new LinkedList<HistoryEntry>();
                entriesMap.put(record.entityId, entries);
            }

            entries.add(createPropertyEntry(record, authorMap, fetchOptions));
        }
    }

    protected PropertyHistoryEntry createPropertyEntry(HistoryPropertyRecord record, Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        PropertyHistoryEntry entry = new PropertyHistoryEntry();
        entry.setFetchOptions(new HistoryEntryFetchOptions());
        entry.setValidFrom(record.validFrom);
        entry.setValidTo(record.validTo);
        entry.setPropertyName(record.propertyCode);

        if (record.propertyValue != null)
        {
            entry.setPropertyValue(record.propertyValue);
        } else if (record.vocabularyPropertyValue != null)
        {
            entry.setPropertyValue(record.vocabularyPropertyValue);
        } else if (record.materialPropertyValue != null)
        {
            entry.setPropertyValue(record.materialPropertyValue);
        } else
        {
            throw new IllegalArgumentException("Unexpected property history entry with all values null");
        }

        if (fetchOptions.hasAuthor())
        {
            entry.setAuthor(authorMap.get(record.authorId));
            entry.getFetchOptions().withAuthorUsing(fetchOptions.withAuthor());
        }
        return entry;
    }

    private void createRelationshipEntries(Map<Long, List<HistoryEntry>> entriesMap, List<? extends HistoryRelationshipRecord> records,
            Map<Long, Person> authorMap, HistoryEntryFetchOptions fetchOptions)
    {
        for (HistoryRelationshipRecord record : records)
        {
            List<HistoryEntry> entries = entriesMap.get(record.entityId);

            if (entries == null)
            {
                entries = new LinkedList<HistoryEntry>();
                entriesMap.put(record.entityId, entries);
            }

            entries.add(createRelationshipEntry(record, authorMap, fetchOptions));
        }
    }

    protected RelationHistoryEntry createRelationshipEntry(HistoryRelationshipRecord record, Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        RelationHistoryEntry entry = new RelationHistoryEntry();
        entry.setFetchOptions(new HistoryEntryFetchOptions());
        entry.setValidFrom(record.validFrom);
        entry.setValidTo(record.validTo);

        if (fetchOptions.hasAuthor())
        {
            entry.setAuthor(authorMap.get(record.authorId));
            entry.getFetchOptions().withAuthorUsing(fetchOptions.withAuthor());
        }

        return entry;
    }

}
