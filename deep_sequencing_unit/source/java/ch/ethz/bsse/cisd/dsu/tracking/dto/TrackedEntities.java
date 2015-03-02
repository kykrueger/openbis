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

package ch.ethz.bsse.cisd.dsu.tracking.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Simple encapsulation of list of entities that are tracked.
 * 
 * @author Piotr Buczek
 * @author Manuel Kohler
 */
public class TrackedEntities
{

    private final List<Sample> sequencingSamplesToBeProcessed;

    private final List<Sample> sequencingSamplesProcessed;

    private final List<AbstractExternalData> dataSets;

    public TrackedEntities(List<Sample> sequencingSamplesToBeProcessed,
            List<Sample> sequencingSamplesProcessed, List<AbstractExternalData> dataSets)
    {
        this.sequencingSamplesToBeProcessed = sequencingSamplesToBeProcessed;
        this.sequencingSamplesProcessed = sequencingSamplesProcessed;
        this.dataSets = dataSets;
    }

    // setting these to null, is that OK?
    public TrackedEntities(List<AbstractExternalData> dataSets)
    {
        this.dataSets = dataSets;
        this.sequencingSamplesProcessed = null;
        this.sequencingSamplesToBeProcessed = null;
    }

    public List<Sample> getSequencingSamplesToBeProcessed()
    {
        return sequencingSamplesToBeProcessed;
    }

    public List<Sample> getSequencingSamplesProcessed()
    {
        return sequencingSamplesProcessed;
    }

    public List<AbstractExternalData> getDataSets()
    {
        return dataSets;
    }
}
