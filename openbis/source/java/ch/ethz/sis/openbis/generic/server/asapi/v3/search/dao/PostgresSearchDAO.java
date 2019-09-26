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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SelectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;

public class PostgresSearchDAO implements ISQLSearchDAO
{
    private ISQLExecutor sqlExecutor;

    public PostgresSearchDAO(ISQLExecutor sqlExecutor)
    {
        this.sqlExecutor = sqlExecutor;
    }

    public Set<Long> queryDBWithNonRecursiveCriteria(final EntityKind entityKind, final Collection<ISearchCriteria> criteria,
            final SearchOperator operator, final boolean entityTypeSearch)
    {
        final EntityMapper entityMapper = EntityMapper.toEntityMapper(entityKind, entityTypeSearch);
        final SelectQuery selectQuery = Translator.translate(entityMapper, criteria, operator);
        final List<Map<String, Object>> result = sqlExecutor.execute(selectQuery.getQuery(), selectQuery.getArgs());
        return result.stream().map(stringLongMap -> (Long) stringLongMap.get(entityMapper.getEntitiesTableIdField())).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findChildIDs(final EntityKind entityKind, final Set<Long> parentIdSet)
    {
        final EntityMapper entityMapper = EntityMapper.toEntityMapper(entityKind, false);
        final String query = "SELECT DISTINCT " + entityMapper.getRelationshipsTableChildIdField() + "\n" +
                "FROM " + entityMapper.getRelationshipsTable() + "\n" +
                "WHERE " + entityMapper.getRelationshipsTableParentIdField() + " IN (?)";
        return executeSetSearchQuery(entityMapper, query, Collections.singletonList(parentIdSet.toArray()));
    }

    @Override
    public Set<Long> findParentIDs(final EntityKind entityKind, final Set<Long> childIdSet)
    {
        final EntityMapper entityMapper = EntityMapper.toEntityMapper(entityKind, false);
        final String query = "SELECT DISTINCT " + entityMapper.getRelationshipsTableParentIdField() + "\n" +
                "FROM " + entityMapper.getRelationshipsTable() + "\n" +
                "WHERE " + entityMapper.getRelationshipsTableChildIdField() + " IN (?)";
        return executeSetSearchQuery(entityMapper, query, Collections.singletonList(childIdSet.toArray()));
    }

    private Set<Long> executeSetSearchQuery(final EntityMapper entityMapper, final String query, final List<Object> args)
    {
        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(query, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(entityMapper.getRelationshipsTableChildIdField()))
                .collect(Collectors.toSet());
    }

}
