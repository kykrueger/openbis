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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.mappers;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;

public enum LogicalOperatorsMapper
{
    AND("AND"),

    OR("OR");

    private String value;

    LogicalOperatorsMapper(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }

    public static LogicalOperatorsMapper toLogicalOperatorsMapper(final SearchOperator operator)
    {
        return LogicalOperatorsMapper.valueOf(operator.name());
    }

}
