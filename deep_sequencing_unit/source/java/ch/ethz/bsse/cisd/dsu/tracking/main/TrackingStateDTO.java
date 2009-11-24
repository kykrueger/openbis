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

package ch.ethz.bsse.cisd.dsu.tracking.main;

import ch.systemsx.cisd.common.annotation.BeanProperty;

public class TrackingStateDTO
{
    private int lastSeenSequencingSampleId;

    private int lastSeenFlowLaneSampleId;

    private int lastSeenDatasetId;

    public int getLastSeenSequencingSampleId()
    {
        return lastSeenSequencingSampleId;
    }

    @BeanProperty(label = "lastSeenSequencingSampleId")
    public void setLastSeenSequencingSampleId(int lastSeenSequencingSampleId)
    {
        this.lastSeenSequencingSampleId = lastSeenSequencingSampleId;
    }

    public int getLastSeenFlowLaneSampleId()
    {
        return lastSeenFlowLaneSampleId;
    }

    @BeanProperty(label = "lastSeenFlowLaneSampleId")
    public void setLastSeenFlowLaneSampleId(int lastSeenFlowLaneSampleId)
    {
        this.lastSeenFlowLaneSampleId = lastSeenFlowLaneSampleId;
    }

    public int getLastSeenDatasetId()
    {
        return lastSeenDatasetId;
    }

    @BeanProperty(label = "lastSeenDatasetId")
    public void setLastSeenDatasetId(int lastSeenDatasetId)
    {
        this.lastSeenDatasetId = lastSeenDatasetId;
    }
}