/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.ICreationIdHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.CreationId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.entity.dataset.DataSetCreation")
public class DataSetCreation implements Serializable, ICreationIdHolder
{
    private static final long serialVersionUID = 1L;

    private IEntityTypeId typeId;

    private IExperimentId experimentId;

    private ISampleId sampleId;

    private String code;

    private boolean measured;

    private ExternalDataCreation externalData;

    private List<? extends ITagId> tagIds;

    private Map<String, String> properties = new HashMap<String, String>();

    private List<? extends IDataSetId> containerIds;

    private List<? extends IDataSetId> containedIds;

    private List<? extends IDataSetId> parentIds;

    private List<? extends IDataSetId> childIds;

    private CreationId creationId;

    public IEntityTypeId getTypeId()
    {
        return typeId;
    }

    public void setTypeId(IEntityTypeId typeId)
    {
        this.typeId = typeId;
    }

    public IExperimentId getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
    }

    public ISampleId getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(ISampleId sampleId)
    {
        this.sampleId = sampleId;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public boolean isMeasured()
    {
        return measured;
    }

    public void setMeasured(boolean measured)
    {
        this.measured = measured;
    }

    public ExternalDataCreation getExternalData()
    {
        return externalData;
    }

    public void setExternalData(ExternalDataCreation externalData)
    {
        this.externalData = externalData;
    }

    public List<? extends ITagId> getTagIds()
    {
        return tagIds;
    }

    public void setTagIds(List<? extends ITagId> tagIds)
    {
        this.tagIds = tagIds;
    }

    public List<? extends IDataSetId> getContainerIds()
    {
        return containerIds;
    }

    public void setContainerIds(List<? extends IDataSetId> containerIds)
    {
        this.containerIds = containerIds;
    }

    public List<? extends IDataSetId> getContainedIds()
    {
        return containedIds;
    }

    public void setContainedIds(List<? extends IDataSetId> containedIds)
    {
        this.containedIds = containedIds;
    }

    public List<? extends IDataSetId> getParentIds()
    {
        return parentIds;
    }

    public void setParentIds(List<? extends IDataSetId> parentIds)
    {
        this.parentIds = parentIds;
    }

    public List<? extends IDataSetId> getChildIds()
    {
        return childIds;
    }

    public void setChildIds(List<? extends IDataSetId> childIds)
    {
        this.childIds = childIds;
    }

    // NOTE: does this construct work well with JSON?
    public void setProperty(String key, String value)
    {
        this.properties.put(key, value);
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    @Override
    public CreationId getCreationId()
    {
        return creationId;
    }

    public void setCreationId(CreationId creationId)
    {
        this.creationId = creationId;
    }

}
