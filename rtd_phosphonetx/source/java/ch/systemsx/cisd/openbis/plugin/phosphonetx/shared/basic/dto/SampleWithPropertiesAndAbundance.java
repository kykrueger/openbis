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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SampleWithPropertiesAndAbundance implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
 
    private String identifier;
    
    private TechId id;
    
    private String sampleTypeCode;
    
    private List<SampleProperty> properties;
    
    private double abundance;

    public final String getIdentifier()
    {
        return identifier;
    }

    public final void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public final TechId getId()
    {
        return id;
    }

    public final void setId(TechId id)
    {
        this.id = id;
    }

    public final String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    public final void setSampleTypeCode(String sampleTypeCode)
    {
        this.sampleTypeCode = sampleTypeCode;
    }

    public final List<SampleProperty> getProperties()
    {
        return properties;
    }

    public final void setProperties(List<SampleProperty> properties)
    {
        this.properties = properties;
    }

    public final double getAbundance()
    {
        return abundance;
    }

    public final void setAbundance(double abundance)
    {
        this.abundance = abundance;
    }

}
