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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.CriteriaMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.RELATIONSHIP_TYPES_TABLE;

public class PostgresSearchDAO implements ISQLSearchDAO
{
    private static final String[] POSTGRES_TYPES = new String[] {"INTEGER", "REAL", "BOOLEAN", "TIMESTAMP", "XML"};

    private ISQLExecutor sqlExecutor;

    public PostgresSearchDAO(final ISQLExecutor sqlExecutor)
    {
        this.sqlExecutor = sqlExecutor;
    }

    public Set<Long> queryDBWithNonRecursiveCriteria(final Long userId, final AbstractCompositeSearchCriteria criterion,
            final TableMapper tableMapper, final String idsColumnName, final AuthorisationInformation authorisationInformation)
    {
        final Collection<ISearchCriteria> criteria = criterion.getCriteria();
        final SearchOperator operator = criterion.getOperator();

        final String finalIdColumnName = (idsColumnName == null) ? ID_COLUMN : idsColumnName;

        final TranslationVo translationVo = new TranslationVo();
        translationVo.setUserId(userId);
        translationVo.setTableMapper(tableMapper);
        translationVo.setParentCriterion(criterion);
        translationVo.setCriteria(criteria);
        translationVo.setOperator(operator);
        translationVo.setIdColumnName(finalIdColumnName);
        translationVo.setAuthorisationInformation(authorisationInformation);

        final boolean containsProperties = criteria.stream().anyMatch(
                (subcriterion) -> subcriterion instanceof AbstractFieldSearchCriteria &&
                        ((AbstractFieldSearchCriteria) subcriterion).getFieldType().equals(SearchFieldType.PROPERTY));
        updateWithDataTypes(translationVo, containsProperties);

        final SelectQuery selectQuery = SearchCriteriaTranslator.translate(translationVo);
        final List<Map<String, Object>> result = sqlExecutor.execute(selectQuery.getQuery(), selectQuery.getArgs());
        return result.stream().map(stringLongMap -> (Long) stringLongMap.get(finalIdColumnName))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Map<String, Object>> queryDBWithNonRecursiveCriteria(final Long userId, final GlobalSearchCriteria criterion,
            final TableMapper tableMapper, final String idsColumnName,
            final AuthorisationInformation authorisationInformation, final boolean useHeadline)
    {
        final Collection<ISearchCriteria> criteria = criterion.getCriteria();
        final SearchOperator operator = criterion.getOperator();

        final String finalIdColumnName = (idsColumnName == null) ? ID_COLUMN : idsColumnName;

        final TranslationVo translationVo = new TranslationVo();
        translationVo.setUserId(userId);
        translationVo.setTableMapper(tableMapper);
        translationVo.setParentCriterion(criterion);
        translationVo.setCriteria(criteria);
        translationVo.setOperator(operator);
        translationVo.setIdColumnName(finalIdColumnName);
        translationVo.setAuthorisationInformation(authorisationInformation);
        translationVo.setUseHeadline(useHeadline);

        final SelectQuery selectQuery = GlobalSearchCriteriaTranslator.translate(translationVo);
        final List<Map<String, Object>> result = sqlExecutor.execute(selectQuery.getQuery(), selectQuery.getArgs());

        return new LinkedHashSet<>(result);
    }

    @Override
    public Set<Long> findChildIDs(final TableMapper tableMapper, final Set<Long> parentIdSet,
            final IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        final String rel = "rel";
        final String child = "child";

        final String sql = SELECT + SP + DISTINCT + SP + child + PERIOD + ID_COLUMN + NL +
                FROM + SP + tableMapper.getRelationshipsTable() + SP + rel + NL +
                INNER_JOIN + SP + tableMapper.getEntitiesTable() + SP + child + SP +
                ON + SP + rel + PERIOD + tableMapper.getRelationshipsTableChildIdField() + SP + EQ + SP + child +
                        PERIOD + ID_COLUMN + NL +
                WHERE + SP + tableMapper.getRelationshipsTableParentIdField() + SP + IN + SP + LP +
                SELECT + SP + UNNEST + LP + QU + RP + RP + SP +
                AND + SP + RELATIONSHIP_COLUMN + SP + EQ + SP + LP +
                    SELECT + SP + ID_COLUMN + SP +
                    FROM + SP + RELATIONSHIP_TYPES_TABLE + SP +
                    WHERE + SP + CODE_COLUMN + SP + EQ + SP + QU +
                RP;

        final List<Object> args = new ArrayList<>(2);
        args.add(parentIdSet.toArray(new Long[0]));
        args.add(relationshipType.toString());

        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(sql, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(ID_COLUMN)).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findParentIDs(final TableMapper tableMapper, final Set<Long> childIdSet,
            final IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        final String rel = "rel";
        final String parent = "parent";

        final String sql = SELECT + SP + DISTINCT + SP + parent + PERIOD + ID_COLUMN + NL +
                FROM + SP + tableMapper.getRelationshipsTable() + SP + rel + NL +
                INNER_JOIN + SP + tableMapper.getEntitiesTable() + SP + parent + SP +
                ON + SP + rel + PERIOD + tableMapper.getRelationshipsTableParentIdField() + SP + EQ + SP + parent + PERIOD + ID_COLUMN + NL +
                WHERE + SP + tableMapper.getRelationshipsTableChildIdField() + SP + IN + SP + LP +
                SELECT + SP + UNNEST + LP + QU + RP + RP + SP +
                AND + SP + RELATIONSHIP_COLUMN + SP + EQ + SP + LP +
                    SELECT + SP + ID_COLUMN + SP +
                    FROM + SP + RELATIONSHIP_TYPES_TABLE + SP +
                    WHERE + SP + CODE_COLUMN + SP + EQ + SP + QU +
                RP;
        final List<Object> args = new ArrayList<>(2);
        args.add(childIdSet.toArray(new Long[0]));
        args.add(relationshipType.toString());

        final List<Map<String, Object>> queryResultList = sqlExecutor.execute(sql, args);
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(ID_COLUMN)).collect(Collectors.toSet());
    }

    @Override
    public List<Long> sortIDs(final TableMapper tableMapper, final Collection<Long> filteredIDs, final SortOptions<?> sortOptions)
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
                .collect(Collectors.toList());
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

    @Autowired
    public void setApplicationContext(final ApplicationContext applicationContext)
    {
        CriteriaMapper.initCriteriaToManagerMap(applicationContext);
    }

}
