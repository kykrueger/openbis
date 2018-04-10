/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.plugin.search.SearchPluginsOperationResult")
public class SearchPluginsOperationResult extends SearchObjectsOperationResult<Plugin>
{

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private SearchPluginsOperationResult()
    {
    }

    public SearchPluginsOperationResult(SearchResult<Plugin> searchResult)
    {
        super(searchResult);
    }

}
