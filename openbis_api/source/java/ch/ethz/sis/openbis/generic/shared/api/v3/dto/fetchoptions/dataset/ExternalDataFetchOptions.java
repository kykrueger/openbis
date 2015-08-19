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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.FileFormatTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.LocatorTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyTermFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("dto.fetchoptions.dataset.ExternalDataFetchOptions")
public class ExternalDataFetchOptions extends FetchOptions<ExternalData> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private VocabularyTermFetchOptions storageFormat;

    @JsonProperty
    private FileFormatTypeFetchOptions fileFormatType;

    @JsonProperty
    private LocatorTypeFetchOptions locatorType;

    @JsonProperty
    private ExternalDataSortOptions sort;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public VocabularyTermFetchOptions withStorageFormat()
    {
        if (storageFormat == null)
        {
            storageFormat = new VocabularyTermFetchOptions();
        }
        return storageFormat;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public VocabularyTermFetchOptions withStorageFormatUsing(VocabularyTermFetchOptions fetchOptions)
    {
        return storageFormat = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasStorageFormat()
    {
        return storageFormat != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public FileFormatTypeFetchOptions withFileFormatType()
    {
        if (fileFormatType == null)
        {
            fileFormatType = new FileFormatTypeFetchOptions();
        }
        return fileFormatType;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public FileFormatTypeFetchOptions withFileFormatTypeUsing(FileFormatTypeFetchOptions fetchOptions)
    {
        return fileFormatType = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasFileFormatType()
    {
        return fileFormatType != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public LocatorTypeFetchOptions withLocatorType()
    {
        if (locatorType == null)
        {
            locatorType = new LocatorTypeFetchOptions();
        }
        return locatorType;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public LocatorTypeFetchOptions withLocatorTypeUsing(LocatorTypeFetchOptions fetchOptions)
    {
        return locatorType = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasLocatorType()
    {
        return locatorType != null;
    }

    @Override
    public ExternalDataSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new ExternalDataSortOptions();
        }
        return sort;
    }

    @Override
    public ExternalDataSortOptions getSortBy()
    {
        return sort;
    }
}
