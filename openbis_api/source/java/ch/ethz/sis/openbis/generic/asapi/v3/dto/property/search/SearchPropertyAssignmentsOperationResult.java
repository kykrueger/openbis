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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.property.search.SearchPropertyAssignmentsOperationResult")
public class SearchPropertyAssignmentsOperationResult extends SearchObjectsOperationResult<PropertyAssignment>
{

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private SearchPropertyAssignmentsOperationResult()
    {
    }

    public SearchPropertyAssignmentsOperationResult(SearchResult<PropertyAssignment> searchResult)
    {
        super(searchResult);
    }

}
