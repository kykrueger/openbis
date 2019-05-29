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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateObjectValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateEarlierThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateLaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectEarlierThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectLaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;

import java.text.ParseException;
import java.util.List;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.DATE_FORMAT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.GE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.LE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.QU;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;

public class DateFieldConditionTranslator implements IConditionTranslator<DateFieldSearchCriteria>
{

    @Override
    public void translate(final DateFieldSearchCriteria criterion, final List<Object> args,
            final StringBuilder sqlBuilder)
    {
        final Object fieldName = criterion.getFieldName();
        final Object fieldValue = criterion.getFieldValue();

        if (criterion instanceof RegistrationDateSearchCriteria)
        {
            sqlBuilder.append(REGISTRATION_TIMESTAMP_COLUMN);
        } else if (criterion instanceof ModificationDateSearchCriteria)
        {
            sqlBuilder.append(MODIFICATION_TIMESTAMP_COLUMN);
        } else
        {
            sqlBuilder.append(fieldName);
        }

        if (fieldValue instanceof DateEqualToValue || fieldValue instanceof DateObjectEqualToValue)
        {
            sqlBuilder.append(EQ).append(QU);
        } else if (fieldValue instanceof DateEarlierThanOrEqualToValue ||
                fieldValue instanceof DateObjectEarlierThanOrEqualToValue)
        {
            sqlBuilder.append(LE).append(QU);
        } else if (fieldValue instanceof DateLaterThanOrEqualToValue ||
                fieldValue instanceof DateObjectLaterThanOrEqualToValue)
        {
            sqlBuilder.append(GE).append(QU);
        } else
        {
            throw new IllegalArgumentException("Unsupported field value: " + fieldValue.getClass().getSimpleName());
        }

        if (fieldValue instanceof AbstractDateValue)
        {
            // String type date value.
            final String dateString = ((AbstractDateValue) fieldValue).getValue();
            try
            {
                args.add(DATE_FORMAT.parse(dateString));
            } catch (ParseException e)
            {
                throw new IllegalArgumentException("Illegal date [dateString='" + dateString + "']", e);
            }
        } else
        {
            // Date type date value.
            args.add(((AbstractDateObjectValue) fieldValue).getValue());
        }
    }

}
