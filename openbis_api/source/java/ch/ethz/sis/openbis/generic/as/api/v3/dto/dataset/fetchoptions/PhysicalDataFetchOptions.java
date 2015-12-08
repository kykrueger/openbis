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
package ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.PhysicalData;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
 */
@JsonObject("dto.dataset.fetchoptions.PhysicalDataFetchOptions")
public class PhysicalDataFetchOptions extends FetchOptions<PhysicalData> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private StorageFormatFetchOptions storageFormat;

    @JsonProperty
    private FileFormatTypeFetchOptions fileFormatType;

    @JsonProperty
    private LocatorTypeFetchOptions locatorType;

    @JsonProperty
    private PhysicalDataSortOptions sort;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public StorageFormatFetchOptions withStorageFormat()
    {
        if (storageFormat == null)
        {
            storageFormat = new StorageFormatFetchOptions();
        }
        return storageFormat;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public StorageFormatFetchOptions withStorageFormatUsing(StorageFormatFetchOptions fetchOptions)
    {
        return storageFormat = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasStorageFormat()
    {
        return storageFormat != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public FileFormatTypeFetchOptions withFileFormatType()
    {
        if (fileFormatType == null)
        {
            fileFormatType = new FileFormatTypeFetchOptions();
        }
        return fileFormatType;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public FileFormatTypeFetchOptions withFileFormatTypeUsing(FileFormatTypeFetchOptions fetchOptions)
    {
        return fileFormatType = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasFileFormatType()
    {
        return fileFormatType != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public LocatorTypeFetchOptions withLocatorType()
    {
        if (locatorType == null)
        {
            locatorType = new LocatorTypeFetchOptions();
        }
        return locatorType;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public LocatorTypeFetchOptions withLocatorTypeUsing(LocatorTypeFetchOptions fetchOptions)
    {
        return locatorType = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasLocatorType()
    {
        return locatorType != null;
    }

    @Override
    public PhysicalDataSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new PhysicalDataSortOptions();
        }
        return sort;
    }

    @Override
    public PhysicalDataSortOptions getSortBy()
    {
        return sort;
    }
}