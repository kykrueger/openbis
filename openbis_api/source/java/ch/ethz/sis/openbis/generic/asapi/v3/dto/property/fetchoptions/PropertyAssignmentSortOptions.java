/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOrder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.property.fetchoptions.PropertyAssignmentSortOptions")
public class PropertyAssignmentSortOptions extends SortOptions<PropertyAssignment>
{
    private static final long serialVersionUID = 1L;
    
    @JsonIgnore
    public static final String CODE = "CODE";

    @JsonIgnore
    public static final String LABEL = "LABEL";
    
    public SortOrder code()
    {
        return getOrCreateSorting(CODE);
    }

    public SortOrder getCode()
    {
        return getSorting(CODE);
    }
    
    public SortOrder label()
    {
        return getOrCreateSorting(LABEL);
    }
    
    public SortOrder getLabel()
    {
        return getSorting(LABEL);
    }

}