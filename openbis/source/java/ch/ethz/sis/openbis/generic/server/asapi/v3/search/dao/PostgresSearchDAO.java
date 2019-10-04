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
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SelectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

public class PostgresSearchDAO implements ISQLSearchDAO
{
    private ISQLExecutor sqlExecutor;

    private Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?>> criteriaToManagerMap;

    public PostgresSearchDAO(ISQLExecutor sqlExecutor)
    {
        this.sqlExecutor = sqlExecutor;
    }

    public Set<Long> queryDBWithNonRecursiveCriteria(final Long userId, final TableMapper tableMapper,
            final Collection<ISearchCriteria> criteria, final SearchOperator operator)
    {
        final Translator.TranslationVo translationVo = new Translator.TranslationVo();
        translationVo.setUserId(userId);
        translationVo.setTableMapper(tableMapper);
        translationVo.setCriteria(criteria);
        translationVo.setOperator(operator);
        translationVo.setCriteriaToManagerMap(criteriaToManagerMap);

        final SelectQuery selectQuery = Translator.translate(translationVo);
        final List<Map<String, Object>> result = sqlExecutor.execute(selectQuery.getQuery(), selectQuery.getArgs());
        return result.stream().map(stringLongMap -> (Long) stringLongMap.get(ID_COLUMN)).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findChildIDs(final TableMapper tableMapper, final Set<Long> parentIdSet)
    {
        final String query = "SELECT DISTINCT " + tableMapper.getRelationshipsTableChildIdField() + "\n" +
                "FROM " + tableMapper.getRelationshipsTable() + "\n" +
                "WHERE " + tableMapper.getRelationshipsTableParentIdField() + " IN (?)";
        return executeSetSearchQuery(tableMapper, query, Collections.singletonList(parentIdSet.toArray()));
    }

    @Override
    public Set<Long> findParentIDs(final TableMapper tableMapper, final Set<Long> childIdSet)
    {
        final String query = "SELECT DISTINCT " + tableMapper.getRelationshipsTableParentIdField() + "\n" +
                "FROM " + tableMapper.getRelationshipsTable() + "\n" +
                "WHERE " + tableMapper.getRelationshipsTableChildIdField() + " IN (?)";
        return executeSetSearchQuery(tableMapper, query, Collections.singletonList(childIdSet.toArray()));
    }

    private Set<Long> executeSetSearchQuery(final TableMapper tableMapper, final String query, final List<Object> args)
    {
        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(query, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(tableMapper.getRelationshipsTableChildIdField()))
                .collect(Collectors.toSet());
    }

    public void setCriteriaToManagerMap(
            final Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?>> criteriaToManagerMap)
    {
        this.criteriaToManagerMap = criteriaToManagerMap;
    }

}
