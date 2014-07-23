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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Sample perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("SamplePermId")
public class SamplePermId extends ObjectPermId implements ISampleId
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /**
     * @param permId Sample perm id, e.g. "201108050937246-1031".
     */
    public SamplePermId(String permId)
    {
        super(permId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private SamplePermId()
    {
        super();
    }

}
