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
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinByExperiment implements IsSerializable, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    private String uniprotID;
    
    private String description;
    
    private List<ProteinSequence> sequences = new ArrayList<ProteinSequence>();

    public final String getUniprotID()
    {
        return uniprotID;
    }

    public final void setUniprotID(String uniprotID)
    {
        this.uniprotID = uniprotID;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    public final List<ProteinSequence> getSequences()
    {
        return sequences;
    }
    
    public final void addSequence(ProteinSequence proteinSequence)
    {
        this.sequences.add(proteinSequence);
    }
    
    
}
