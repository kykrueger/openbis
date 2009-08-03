/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicDataSetUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Piotr Buczek
 */
public class DataSetUpdatesDTO extends BasicDataSetUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String parentIdentifierOrNull;

    private SampleIdentifier sampleIdentifier;

    public String getParentIdentifierOrNull()
    {
        return parentIdentifierOrNull;
    }

    public void setParentIdentifierOrNull(String parentIdentifierOrNull)
    {
        this.parentIdentifierOrNull = parentIdentifierOrNull;
    }

    public SampleIdentifier getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(SampleIdentifier sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

}