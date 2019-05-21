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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.mappers.EntityMapper;

import java.util.Collections;
import java.util.List;

public class Translator
{

    private static final String SELECT = "SELECT";

    private static final String DISTINCT = "DISTINCT";

    private static final String FROM = "FROM";

    private static final String LEFT = "LEFT";

    private static final String JOIN = "JOIN";

    private static final String ON = "ON";

    private static final String SPACE = " ";

    private static final String COMMA = ",";

    private static final String PERIOD = ".";

    private static final String EQ = "=";

    private static final String NEW_LINE = "\n";

    public static class TranslatorResult
    {
        private String sqlQuery;

        private List<Object> args;
    }

    public static class TranslatorAlias {
        private String table;
        private String tableAlias; // table + "_" + <alias_idx>
        private ISearchCriteria reasonForAlias;
    }

    public static TranslatorResult translate(final EntityKind entityKind, final List<ISearchCriteria> criteria,
            final SearchOperator operator)
    {
        final EntityMapper dbEntityKind = EntityMapper.toDBEntityKind(entityKind);

        StringBuilder builder = new StringBuilder();
        select(builder, dbEntityKind, Collections.emptyList());
        from(builder, dbEntityKind, criteria);
        where(builder, dbEntityKind, criteria);
        return new TranslatorResult();
    }

    private static void select(final StringBuilder builder, final EntityMapper dbEntityKind, final List<String> aliasesPresentInOrderBy)
    {
        builder.append(SELECT).append(SPACE).append(DISTINCT).append(SPACE).append(dbEntityKind.getEntitiesTableIdField());
        for (String alias : aliasesPresentInOrderBy)
        {
            builder.append(SPACE).append(COMMA).append(alias);
        }
        builder.append(NEW_LINE);
    }

    private static void from(final StringBuilder builder, final EntityMapper dbEntityKind, final List<ISearchCriteria> criteria)
    {
        final String entitiesTableName = dbEntityKind.getEntitiesTable();
        builder.append(FROM).append(SPACE).append(entitiesTableName).append(NEW_LINE);
        for (final ISearchCriteria criterion : criteria)
        {
//            if(isAliasPresentInWhere) {
//
//            }
//            builder.append(LEFT).append(SPACE).append(JOIN).append(SPACE).append().append(SPACE).append(entitiesTableName).append(SPACE).append(ON)
//                    .append(SPACE).append(entitiesTableName).append(PERIOD).append().append(EQ).append().append(PERIOD).append();
        }
    }

    private static void where(final StringBuilder builder, EntityMapper dbEntityKind, List<ISearchCriteria> criteria)
    {

    }

}
