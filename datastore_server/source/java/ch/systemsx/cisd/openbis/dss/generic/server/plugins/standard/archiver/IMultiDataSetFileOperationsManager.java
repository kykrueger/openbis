/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Jakub Straszewski
 */
public interface IMultiDataSetFileOperationsManager
{
    String generateContainerPath(List<DatasetDescription> dataSets);

    Status createContainerInStage(String containerPath, List<DatasetDescription> datasetDescriptions);

    Status copyToFinalDestination(String containerLocalPath);

    Status deleteContainerFromFinalDestination(String containerLocalPath);

    Status deleteContainerFromStage(String containerPath);

    /**
     * Get's the content of archived content in final destination.
     */
    IHierarchicalContent getContainerAsHierarchicalContent(String containerPath);

    Status restoreDataSetsFromContainerInFinalDestination(String containerPath,
            List<? extends IDatasetLocation> dataSetLocations);
}
