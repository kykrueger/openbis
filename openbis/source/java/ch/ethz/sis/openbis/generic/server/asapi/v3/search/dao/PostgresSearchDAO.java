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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SelectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DISTINCT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.UNNEST;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

public class PostgresSearchDAO implements ISQLSearchDAO
{
    private static final String[] POSTGRES_TYPES = new String[] {"INTEGER", "REAL", "BOOLEAN", "TIMESTAMP", "XML"};

    private ISQLExecutor sqlExecutor;

    private Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?, ?>> criteriaToManagerMap;

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
        return result.stream().map(
                stringLongMap -> (Long) stringLongMap.get(ID_COLUMN)
        ).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findChildIDs(final TableMapper tableMapper, final Set<Long> parentIdSet)
    {
        final String query = SELECT + SP + DISTINCT + SP + tableMapper.getRelationshipsTableChildIdField() + NL +
                FROM + SP + tableMapper.getRelationshipsTable() + NL +
                WHERE + SP + tableMapper.getRelationshipsTableParentIdField() + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP;
        final List<Object> args = Collections.singletonList(parentIdSet.toArray(new Long[0]));
        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(query, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(tableMapper.getRelationshipsTableChildIdField()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findParentIDs(final TableMapper tableMapper, final Set<Long> childIdSet)
    {
        final String query = SELECT + SP + DISTINCT + SP + tableMapper.getRelationshipsTableParentIdField() + NL +
                FROM + SP + tableMapper.getRelationshipsTable() + NL +
                WHERE + SP + tableMapper.getRelationshipsTableChildIdField() + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP;
        final List<Object> args = Collections.singletonList(childIdSet.toArray(new Long[0]));
        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(query, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(tableMapper.getRelationshipsTableParentIdField()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> sortIDs(final Long userId, final TableMapper tableMapper, final Set<Long> filteredIDs, final SortOptions<?> sortOptions)
    {
        final Translator.OrderTranslationVo orderTranslationVo = new Translator.OrderTranslationVo();
        orderTranslationVo.setUserId(userId);
        orderTranslationVo.setTableMapper(tableMapper);
        orderTranslationVo.setIDs(filteredIDs);
        orderTranslationVo.setSortOptions(sortOptions);
        orderTranslationVo.setTypesToFilter(POSTGRES_TYPES);

        // TODO: make the following query execute only when there is search by property.
        final SelectQuery dataTypesQuery = Translator.translateToSearchTypeQuery(orderTranslationVo);
        final List<Map<String, Object>> dataTypesQueryResultList = sqlExecutor.execute(dataTypesQuery.getQuery(), dataTypesQuery.getArgs());
        final Map<String, String> typeByPropertyName = dataTypesQueryResultList.stream().collect(Collectors.toMap(
                (valueByColumnName) -> (String) valueByColumnName.get(Translator.PROPERTY_CODE_ALIAS),
                (valueByColumnName) -> (String) valueByColumnName.get(Translator.TYPE_CODE_ALIAS)));

        orderTranslationVo.setDataTypeByPropertyName(typeByPropertyName);

        final SelectQuery orderQuery = Translator.translateToOrderQuery(orderTranslationVo);
        final List<Map<String, Object>> orderQueryResultList = sqlExecutor.execute(orderQuery.getQuery(), orderQuery.getArgs());
        return orderQueryResultList.stream().map((valueByColumnName) -> (Long) valueByColumnName.get(ID_COLUMN))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setCriteriaToManagerMap(
            final Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?, ?>> criteriaToManagerMap)
    {
        this.criteriaToManagerMap = criteriaToManagerMap;
    }

}
