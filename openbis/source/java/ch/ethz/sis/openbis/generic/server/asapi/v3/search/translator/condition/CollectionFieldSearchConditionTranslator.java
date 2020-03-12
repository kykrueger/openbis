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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CollectionFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdsSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.Attributes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;

public class CollectionFieldSearchConditionTranslator implements IConditionTranslator<CollectionFieldSearchCriteria<?>>
{

    private static final Map<Class, Object[]> arrayCasting = new HashMap<>();
    static {
        arrayCasting.put(CodesSearchCriteria.class, new String[0]);
        arrayCasting.put(IdsSearchCriteria.class, new Long[0]);
        arrayCasting.put(UserIdsSearchCriteria.class, new String[0]);
    }

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final CollectionFieldSearchCriteria<?> criterion, final TableMapper tableMapper,
                                                              final IAliasFactory aliasFactory) {
        return null;
    }

    @Override
    public void translate(final CollectionFieldSearchCriteria<?> criterion, final TableMapper tableMapper, final List<Object> args,
                          final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final Map<String, String> dataTypeByPropertyName)
    {
        if (!arrayCasting.containsKey(criterion.getClass())) {
            throw new RuntimeException("Unsupported CollectionFieldSearchCriteria, this is a core error, contact the development team.");
        }

        switch (criterion.getFieldType()) {
            case ATTRIBUTE:
            {
                final Object fieldName = Attributes.getColumnName(criterion.getFieldName(), tableMapper.getEntitiesTable(), criterion.getFieldName());
                final Collection<?> fieldValue = criterion.getFieldValue();

                sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName).append(SP).append(IN).append(SP).append(LP).
                        append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP).
                        append(RP);
                args.add(fieldValue.toArray(arrayCasting.get(criterion.getClass())));
                break;
            }

            case PROPERTY:
            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

}
