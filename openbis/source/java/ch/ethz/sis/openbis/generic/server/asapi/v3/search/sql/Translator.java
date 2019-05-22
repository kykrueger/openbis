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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.mappers.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.mappers.LogicalOperatorsMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Translator
{

    private static final String SELECT = "SELECT";

    private static final String DISTINCT = "DISTINCT";

    private static final String FROM = "FROM";

    private static final String WHERE = "WHERE";

    private static final String LEFT = "LEFT";

    private static final String JOIN = "JOIN";

    private static final String ON = "ON";

    private static final String SPACE = " ";

    private static final String COMMA = ",";

    private static final String PERIOD = ".";

    private static final String EQ = "=";

    private static final String NEW_LINE = "\n";

    private static final String ASTERISK = "*";

    public static TranslatorResult translate(final EntityKind entityKind, final List<ISearchCriteria> criteria,
            final SearchOperator operator)
    {
        if (criteria == null && criteria.isEmpty())
        {
            throw new IllegalArgumentException("Empty or null criteria provided.");
        }

        final EntityMapper dbEntityKind = EntityMapper.toEntityMapper(entityKind);

        final List<String> aliases = new ArrayList<>();

        if (criteria.size() == 1)
        {
            final ISearchCriteria criterion = criteria.get(0);
            if (criteria instanceof AbstractEntitySearchCriteria<?>)
            {
                final AbstractEntitySearchCriteria<?> entitySearchCriterion = (AbstractEntitySearchCriteria<?>) criterion;
                if (entitySearchCriterion.getCriteria().isEmpty())
                {
                    aliases.add(ASTERISK);
                }
            }
        }

        final StringBuilder builder = new StringBuilder();
        select(builder, dbEntityKind, aliases);
        from(builder, dbEntityKind, criteria);
        where(builder, dbEntityKind, criteria, operator);
        return new TranslatorResult(builder.toString(), Collections.emptyList());
    }

    private static void select(final StringBuilder builder, final EntityMapper dbEntityKind, final List<String> aliasesPresentInOrderBy)
    {
        builder.append(SELECT).append(SPACE).append(DISTINCT).append(SPACE).append(dbEntityKind.getEntitiesTableIdField());
        aliasesPresentInOrderBy.forEach(alias -> builder.append(SPACE).append(COMMA).append(alias));
        builder.append(NEW_LINE);
    }

    private static void from(final StringBuilder builder, final EntityMapper entityMapper, final List<ISearchCriteria> criteria)
    {
        final String entitiesTableName = entityMapper.getEntitiesTable();
        builder.append(FROM).append(SPACE).append(entitiesTableName).append(NEW_LINE);
        criteria.forEach(criterion ->
                {
                    if (criterion instanceof AbstractEntitySearchCriteria<?>)
                    {
                        final AbstractEntitySearchCriteria<?> entitySearchCriterion = (AbstractEntitySearchCriteria<?>) criterion;
                        if (entitySearchCriterion.getCriteria().isEmpty()) {

                        }
                    }

                    if (criterion instanceof SampleSearchCriteria)
                    {
                        final SampleSearchCriteria sampleSearchCriterion = (SampleSearchCriteria) criterion;


                    }

//                    if(isAliasPresentInWhere) {
//
//                    }
//                    builder.append(LEFT).append(SPACE).append(JOIN).append(SPACE).append().append(SPACE).append(entitiesTableName).append(SPACE).append(ON)
//                            .append(SPACE).append(entitiesTableName).append(PERIOD).append().append(EQ).append().append(PERIOD).append();
                });
    }

    private static void where(final StringBuilder builder, final EntityMapper entityMapper, final List<ISearchCriteria> criteria,
            final SearchOperator operator)
    {
        final String logicalOperator = LogicalOperatorsMapper.toLogicalOperatorsMapper(operator).toString();
        builder.append(WHERE).append(SPACE);
        criteria.forEach(criterion ->
                {
                    if (criterion instanceof AbstractEntitySearchCriteria<?>)
                    {
                        final AbstractEntitySearchCriteria<?> entitySearchCriterion = (AbstractEntitySearchCriteria<?>) criterion;
//                        builder.append().append(SPACE).append(logicalOperator);
                    }
                    if (criterion instanceof SampleSearchCriteria)
                    {
                        final SampleSearchCriteria sampleSearchCriterion = (SampleSearchCriteria) criterion;


                    }
                });
        builder.setLength(builder.length() - logicalOperator.length() - SPACE.length());
    }

    public static class TranslatorResult
    {

        private String sqlQuery;

        private List<Object> args;

        public TranslatorResult(String sqlQuery, List<Object> args)
        {
            this.sqlQuery = sqlQuery;
            this.args = args;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            TranslatorResult that = (TranslatorResult) o;

            if (!sqlQuery.equals(that.sqlQuery))
            {
                return false;
            }
            return args.equals(that.args);
        }

        @Override
        public int hashCode()
        {
            int result = sqlQuery.hashCode();
            result = 31 * result + args.hashCode();
            return result;
        }

    }

    public static class TranslatorAlias
    {

        private String table;

        private String tableAlias; // table + "_" + <alias_idx>

        private ISearchCriteria reasonForAlias;

    }

}
