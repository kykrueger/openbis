/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public class DataSetAttributeProvider extends AbstractEntityAttributeProvider
{

    @Override
    public IAttributeSearchFieldKind getAttribute(ISearchCriteria criteria)
    {
        if (criteria instanceof CodeSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.CODE;
        } else if (criteria instanceof PermIdSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.CODE;
        } else if (criteria instanceof RegistrationDateSearchCriteria)
        {
            RegistrationDateSearchCriteria dateCriteria = (RegistrationDateSearchCriteria) criteria;
            return getDateAttribute(dateCriteria.getFieldValue(), DataSetAttributeSearchFieldKind.REGISTRATION_DATE,
                    DataSetAttributeSearchFieldKind.REGISTRATION_DATE_UNTIL, DataSetAttributeSearchFieldKind.REGISTRATION_DATE_FROM);
        } else if (criteria instanceof ModificationDateSearchCriteria)
        {
            ModificationDateSearchCriteria dateCriteria = (ModificationDateSearchCriteria) criteria;
            return getDateAttribute(dateCriteria.getFieldValue(), DataSetAttributeSearchFieldKind.MODIFICATION_DATE,
                    DataSetAttributeSearchFieldKind.MODIFICATION_DATE_UNTIL, DataSetAttributeSearchFieldKind.MODIFICATION_DATE_FROM);
        } else
        {
            throw new IllegalArgumentException("Unknown attribute criteria: " + criteria);
        }
    }

}