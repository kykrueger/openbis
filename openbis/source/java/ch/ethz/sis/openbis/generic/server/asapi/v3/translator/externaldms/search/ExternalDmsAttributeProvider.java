/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.externaldms.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.AddressSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.LabelSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public class ExternalDmsAttributeProvider implements IObjectAttributeProvider
{

    @Override
    public IAttributeSearchFieldKind getAttribute(ISearchCriteria criteria)
    {
        if (criteria instanceof CodeSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.EXTERNAL_DMS_CODE;
        } else if (criteria instanceof LabelSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.EXTERNAL_DMS_LABEL;
        } else if (criteria instanceof AddressSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.EXTERNAL_DMS_ADDRESS;
        } else if (criteria instanceof ExternalDmsTypeSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.EXTERNAL_DMS_TYPE;
        } else
        {
            throw new IllegalArgumentException("Unknown attribute criteria: " + criteria);
        }
    }

}
