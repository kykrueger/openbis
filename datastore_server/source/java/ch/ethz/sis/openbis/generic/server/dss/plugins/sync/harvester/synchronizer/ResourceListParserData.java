/*
 * Copyright 2016 ETH Zuerich, SIS
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
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.MultiKeyMap;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;

/**
 * @author Ganime Betul Akin
 */
public class ResourceListParserData
{
    // Introduced to store the timestamp of any still-running transaction on the data source at the time of
    // retrieving the resource list.
    private Date resourceListTimestamp;

    private Set<String> harvesterSpaceList = new HashSet<>();

    private MasterData masterData = new MasterData();

    private Map<String, IncomingProject> projectsToProcess = new HashMap<String, IncomingProject>();

    private Map<String, IncomingExperiment> experimentsToProcess = new HashMap<String, IncomingExperiment>();

    private Map<String, IncomingSample> samplesToProcess = new HashMap<String, IncomingSample>();

    private Map<String, IncomingDataSet> dataSetsToProcess = new HashMap<String, IncomingDataSet>();

    private MultiKeyMap<String, MaterialWithLastModificationDate> materialsToProcess = new MultiKeyMap<String, MaterialWithLastModificationDate>();

    public MasterData getMasterData()
    {
        return masterData;
    }

    public Date getResourceListTimestamp()
    {
        return resourceListTimestamp;
    }

    public void setResourceListTimestamp(Date resourceListTimestamp)
    {
        this.resourceListTimestamp = resourceListTimestamp;
    }

    public Set<String> getHarvesterSpaceList()
    {
        return harvesterSpaceList;
    }

    public Map<String, IncomingProject> getProjectsToProcess()
    {
        return projectsToProcess;
    }

    public Map<String, IncomingExperiment> getExperimentsToProcess()
    {
        return experimentsToProcess;
    }

    public Map<String, IncomingSample> getSamplesToProcess()
    {
        return samplesToProcess;
    }

    public Map<String, IncomingDataSet> getDataSetsToProcess()
    {
        return dataSetsToProcess;
    }

    public MultiKeyMap<String, MaterialWithLastModificationDate> getMaterialsToProcess()
    {
        return materialsToProcess;
    }

    public Map<String, IncomingDataSet> filterPhysicalDataSetsByLastModificationDate(Date lastSyncDate, Set<String> dataSetsCodesToRetry,
            Set<String> blackListedDataSetCodes)
    {
        Map<String, IncomingDataSet> dsMap = new HashMap<String, IncomingDataSet>();
        for (String permId : dataSetsToProcess.keySet())
        {
            IncomingDataSet ds = dataSetsToProcess.get(permId);
            String dataSetCode = ds.getDataSet().getCode();
            if (ds.getKind() == DataSetKind.PHYSICAL
                    && (ds.lastModificationDate.after(lastSyncDate) == true || dataSetsCodesToRetry.contains(dataSetCode)) == true)
            {
                if (blackListedDataSetCodes.contains(dataSetCode) == false)
                {
                    dsMap.put(permId, ds);
                }
            }
        }
        return dsMap;
    }

    public List<IncomingEntity<?>> filterAttachmentHoldersByLastModificationDate(Date lastSyncTimestamp, Set<String> attachmentHoldersToRetry)
    {
        List<IncomingEntity<?>> attachmentHoldersToProcess = new ArrayList<IncomingEntity<?>>();
        // projects
        for (IncomingProject incomingProject : projectsToProcess.values())
        {
            if (syncAttachments(lastSyncTimestamp, attachmentHoldersToRetry, incomingProject) == true)
            {
                attachmentHoldersToProcess.add(incomingProject);
            }
        }
        // experiments
        for (IncomingExperiment incomingExperiment : experimentsToProcess.values())
        {
            if (syncAttachments(lastSyncTimestamp, attachmentHoldersToRetry, incomingExperiment) == true)
            {
                attachmentHoldersToProcess.add(incomingExperiment);
            }
        }
        // samples
        for (IncomingSample incomingSample : samplesToProcess.values())
        {
            if (syncAttachments(lastSyncTimestamp, attachmentHoldersToRetry, incomingSample) == true)
            {
                attachmentHoldersToProcess.add(incomingSample);
            }
        }
        return attachmentHoldersToProcess;
    }

    /**
     * Since we have no way of knowing if only the attachments have been in some way modified (more of a problem if the attachment has been deleted),
     * we need to process all modified entities and also retry any that have failed before. When the attachment holders are processed later, we check
     * if the entity has attachments to only query attachments for entities that has been marked as having attachments in the XML resource list.
     */
    private boolean syncAttachments(Date lastSyncTimestamp, Set<String> attachmentHoldersToRetry, IncomingEntity<?> incomingEntity)
    {
        Identifier<?> entity = incomingEntity.getEntity();
        if (incomingEntity.getLastModificationDate().after(lastSyncTimestamp)
                || attachmentHoldersToRetry.contains(incomingEntity.getEntityKind().getLabel() + "-" + entity.getPermID()))
        {
            return true;
        }
        return false;
    }

    public Map<String, IncomingDataSet> filterContainerDataSets()
    {
        Map<String, IncomingDataSet> dsMap = new HashMap<String, IncomingDataSet>();
        for (String permId : dataSetsToProcess.keySet())
        {
            IncomingDataSet ds = dataSetsToProcess.get(permId);
            if (ds.getKind() == DataSetKind.CONTAINER)
            {
                dsMap.put(permId, ds);
            }
        }
        return dsMap;
    }
}