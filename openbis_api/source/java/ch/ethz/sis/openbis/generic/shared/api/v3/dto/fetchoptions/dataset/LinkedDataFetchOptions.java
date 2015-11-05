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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.LinkedData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.externaldms.ExternalDmsFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("dto.fetchoptions.dataset.LinkedDataFetchOptions")
public class LinkedDataFetchOptions extends FetchOptions<LinkedData> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExternalDmsFetchOptions externalDms;

    @JsonProperty
    private LinkedDataSortOptions sort;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public ExternalDmsFetchOptions withExternalDms()
    {
        if (externalDms == null)
        {
            externalDms = new ExternalDmsFetchOptions();
        }
        return externalDms;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public ExternalDmsFetchOptions withExternalDmsUsing(ExternalDmsFetchOptions fetchOptions)
    {
        return externalDms = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasExternalDms()
    {
        return externalDms != null;
    }

    @Override
    public LinkedDataSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new LinkedDataSortOptions();
        }
        return sort;
    }

    @Override
    public LinkedDataSortOptions getSortBy()
    {
        return sort;
    }
}