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
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.FileFormatType;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
 */
@JsonObject("dto.dataset.fetchoptions.FileFormatTypeFetchOptions")
public class FileFormatTypeFetchOptions extends FetchOptions<FileFormatType> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private FileFormatTypeSortOptions sort;

    @Override
    public FileFormatTypeSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new FileFormatTypeSortOptions();
        }
        return sort;
    }

    @Override
    public FileFormatTypeSortOptions getSortBy()
    {
        return sort;
    }
}