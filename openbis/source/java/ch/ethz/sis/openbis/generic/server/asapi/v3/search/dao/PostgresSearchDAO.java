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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.ModifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.RegistratorSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.OrderTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SelectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.TranslationVo;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor.RelationshipType.PARENT_CHILD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DISTINCT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
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
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.RELATIONSHIP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.RELATIONSHIP_TYPES_TABLE;

public class PostgresSearchDAO implements ISQLSearchDAO
{
    private static final String[] POSTGRES_TYPES = new String[] {"INTEGER", "REAL", "BOOLEAN", "TIMESTAMP", "XML"};

    private ISQLExecutor sqlExecutor;

    private Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?, ?>> criteriaToManagerMap = new HashMap<>();

    public PostgresSearchDAO(final ISQLExecutor sqlExecutor)
    {
        this.sqlExecutor = sqlExecutor;
    }

    public Set<Long> queryDBWithNonRecursiveCriteria(final Long userId, final TableMapper tableMapper,
            final Collection<ISearchCriteria> criteria, final SearchOperator operator)
    {
        final TranslationVo translationVo = new TranslationVo();
        translationVo.setUserId(userId);
        translationVo.setTableMapper(tableMapper);
        translationVo.setCriteria(criteria);
        translationVo.setOperator(operator);
        translationVo.setCriteriaToManagerMap(criteriaToManagerMap);

        final boolean containsProperties = criteria.stream().anyMatch(
                (criterion) -> criterion instanceof AbstractFieldSearchCriteria &&
                        ((AbstractFieldSearchCriteria) criterion).getFieldType().equals(SearchFieldType.PROPERTY));
        updateWithDataTypes(translationVo, containsProperties);

        final SelectQuery selectQuery = CriteriaTranslator.translate(translationVo);
        final List<Map<String, Object>> result = sqlExecutor.execute(selectQuery.getQuery(), selectQuery.getArgs());
        return result.stream().map(
                stringLongMap -> (Long) stringLongMap.get(ID_COLUMN)
        ).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findChildIDs(final TableMapper tableMapper, final Set<Long> parentIdSet)
    {
        final StringBuilder sqlBuilder = new StringBuilder(SELECT).append(SP).append(DISTINCT).append(SP).
                append(tableMapper.getRelationshipsTableChildIdField()).append(NL).
                append(FROM).append(SP).append(tableMapper.getRelationshipsTable()).append(NL).
                append(WHERE).append(SP).append(tableMapper.getRelationshipsTableParentIdField()).append(SP).append(IN).append(SP).append(LP).
                append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP).
                append(RP);
        final List<Object> args = Collections.singletonList(parentIdSet.toArray(new Long[0]));

        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(sqlBuilder.toString(), args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(tableMapper.getRelationshipsTableChildIdField()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findParentIDs(final TableMapper tableMapper, final Set<Long> childIdSet)
    {
        final StringBuilder sqlBuilder = new StringBuilder(SELECT).append(SP).append(DISTINCT).append(SP).
                append(tableMapper.getRelationshipsTableParentIdField()).append(NL).
                append(FROM).append(SP).append(tableMapper.getRelationshipsTable()).append(NL).
                append(WHERE).append(SP).append(tableMapper.getRelationshipsTableChildIdField()).append(SP).append(IN).append(SP).append(LP).
                append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP).
                append(RP);

        final List<Object> args = new ArrayList<>(2);
        args.add(childIdSet.toArray(new Long[0]));

        appendRelationshipId(sqlBuilder, args);

        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(sqlBuilder.toString(), args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(tableMapper.getRelationshipsTableParentIdField()))
                .collect(Collectors.toSet());
    }

    private void appendRelationshipId(final StringBuilder sqlBuilder, final List<Object> args)
    {
        sqlBuilder.append(SP).append(AND).append(SP).append(RELATIONSHIP_COLUMN).append(SP).append(EQ).append(SP).append(LP);
        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP).
                append(FROM).append(SP).append(RELATIONSHIP_TYPES_TABLE).append(SP).
                append(WHERE).append(SP).append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
        sqlBuilder.append(RP);
        args.add(PARENT_CHILD.toString());
    }

    @Override
    public Set<Long> sortIDs(final TableMapper tableMapper, final Set<Long> filteredIDs, final SortOptions<?> sortOptions)
    {
        final TranslationVo translationVo = new TranslationVo();
        translationVo.setTableMapper(tableMapper);
        translationVo.setIds(filteredIDs);
        translationVo.setSortOptions(sortOptions);

        final boolean containsProperties = sortOptions.getSortings().stream().anyMatch(
                (sorting) -> TranslatorUtils.isPropertySearchFieldName(sorting.getField()));

        updateWithDataTypes(translationVo, containsProperties);

        final SelectQuery orderQuery = OrderTranslator.translateToOrderQuery(translationVo);
        final List<Map<String, Object>> orderQueryResultList = sqlExecutor.execute(orderQuery.getQuery(), orderQuery.getArgs());
        return orderQueryResultList.stream().map((valueByColumnName) -> (Long) valueByColumnName.get(ID_COLUMN))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void updateWithDataTypes(final TranslationVo translationVo, final boolean containsProperties)
    {
        translationVo.setTypesToFilter(POSTGRES_TYPES);
        final Map<String, String> typeByPropertyName;
        if (containsProperties)
        {
            // Making property types query only when it is needed.
            final SelectQuery dataTypesQuery = OrderTranslator.translateToSearchTypeQuery(translationVo);
            final List<Map<String, Object>> dataTypesQueryResultList = sqlExecutor.execute(dataTypesQuery.getQuery(), dataTypesQuery.getArgs());
            typeByPropertyName = dataTypesQueryResultList.stream().collect(Collectors.toMap(
                    (valueByColumnName) -> (String) valueByColumnName.get(OrderTranslator.PROPERTY_CODE_ALIAS),
                    (valueByColumnName) -> (String) valueByColumnName.get(OrderTranslator.TYPE_CODE_ALIAS)));
        } else
        {
            typeByPropertyName = Collections.emptyMap();
        }

        translationVo.setDataTypeByPropertyName(typeByPropertyName);
    }

    @SuppressWarnings("unchecked")
    @Autowired
    public void setApplicationContext(final ApplicationContext applicationContext)
    {
        criteriaToManagerMap.put(DataSetSearchCriteria.class, applicationContext.getBean("data-set-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(DataSetTypeSearchCriteria.class, applicationContext.getBean("data-set-type-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(ExperimentSearchCriteria.class, applicationContext.getBean("experiment-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(ExperimentTypeSearchCriteria.class, applicationContext.getBean("experiment-type-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(SampleSearchCriteria.class, applicationContext.getBean("sample-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(SampleTypeSearchCriteria.class, applicationContext.getBean("sample-type-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(SampleContainerSearchCriteria.class, applicationContext.getBean("sample-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(RegistratorSearchCriteria.class, applicationContext.getBean("person-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(ModifierSearchCriteria.class, applicationContext.getBean("person-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(ProjectSearchCriteria.class, applicationContext.getBean("project-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(SpaceSearchCriteria.class, applicationContext.getBean("space-search-manager", ISearchManager.class));
        criteriaToManagerMap.put(TagSearchCriteria.class, applicationContext.getBean("tag-search-manager", ISearchManager.class));
    }

}
