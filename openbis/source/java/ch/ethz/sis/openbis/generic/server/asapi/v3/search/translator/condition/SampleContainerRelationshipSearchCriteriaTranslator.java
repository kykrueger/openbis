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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.RELATIONSHIP_TYPES_TABLE;

public class SampleContainerRelationshipSearchCriteriaTranslator implements IConditionTranslator<SampleContainerSearchCriteria>
{
    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final SampleContainerSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        return TranslatorUtils.getRelationshipsJoinInformationMap(tableMapper, aliasFactory);
    }

    @Override
    public void translate(final SampleContainerSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases, final Map<String, String> dataTypeByPropertyName)
    {
        final Map<String, JoinInformation> joinInformationByTable = aliases.get(criterion);
        sqlBuilder.append(joinInformationByTable.get(RELATIONSHIP_TYPES_TABLE).getSubTableAlias()).append(PERIOD).append(CODE_COLUMN).append(SP).append(EQ).append(SP).
                append(QU);
        args.add(IGetRelationshipIdExecutor.RelationshipType.CONTAINER_COMPONENT.toString());
    }

}
