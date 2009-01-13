/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;

/**
 * Criteria for listing <i>samples</i>.
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
// TODO 2009-01-12, Tomasz Pylak: remove inheritance from DefaultResultSetConfig by creating a new
// type which uses composition. Move ListSampleCriteria to the client.shared package. Only then
// deletion of ListSampleCriteriaDTO will be possible.
public final class ListSampleCriteria extends DefaultResultSetConfig<String> implements
        IsSerializable
{
    private SampleType sampleType;

    private String groupCode;

    private boolean includeGroup;

    private boolean includeInstance;

    private String containerIdentifier;

    private String experimentIdentifier;

    public final String getContainerIdentifier()
    {
        return containerIdentifier;
    }

    public final void setContainerIdentifier(final String containerIdentifier)
    {
        this.containerIdentifier = containerIdentifier;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    public void setGroupCode(final String groupCode)
    {
        this.groupCode = groupCode;
    }

    public boolean isIncludeGroup()
    {
        return includeGroup;
    }

    public void setIncludeGroup(final boolean includeGroup)
    {
        this.includeGroup = includeGroup;
    }

    public boolean isIncludeInstance()
    {
        return includeInstance;
    }

    public void setIncludeInstance(final boolean includeInstance)
    {
        this.includeInstance = includeInstance;
    }

    public String getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    public void setExperimentIdentifier(final String experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }
}
