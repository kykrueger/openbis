/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.ObjectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Identifies a data set by tech id.
 * 
 * @author pkupczyk
 */
@JsonObject("DataSetTechIdId")
public class DataSetTechIdId extends ObjectTechIdId implements IDataSetId
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /**
     * @param techId Data set tech id.
     */
    public DataSetTechIdId(Long techId)
    {
        super(techId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private DataSetTechIdId()
    {
        super();
    }

}
