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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.ObjectIdentifier;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Sample identifier.
 * 
 * @author pkupczyk
 */
@JsonObject("dto.id.sample.SampleIdentifier")
public class SampleIdentifier extends ObjectIdentifier implements ISampleId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param identifier Sample identifier, e.g. "/MY_SPACE/MY_SAMPLE" (space sample) or "/MY_SAMPLE" (shared sample)
     */
    public SampleIdentifier(String identifier)
    {
        super(identifier);
    }

    public SampleIdentifier(String spaceCodeOrNull, String sampleCode)
    {
        this(spaceCodeOrNull == null ? "/" + sampleCode : "/" + spaceCodeOrNull + "/" + sampleCode);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private SampleIdentifier()
    {
        super();
    }

}
