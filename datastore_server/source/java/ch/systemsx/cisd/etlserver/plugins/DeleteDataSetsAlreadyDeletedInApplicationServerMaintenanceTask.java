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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Maintenance task that removes data sets that have been deleted in AS from the DSS shares.
 * 
 * @author pkupczyk
 */
public class DeleteDataSetsAlreadyDeletedInApplicationServerMaintenanceTask extends
        AbstractDataSetDeletionPostProcessingMaintenanceTask
{

    private static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";

    private static final String LAST_SEEN_DATA_SET_FILE_DEFAULT =
            "delete-datasets-already-deleted-from-application-server.txt";

    private File lastSeenDataSetFile;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        super.setUp(pluginName, properties);

        String lastSeenDataSetFileProperty =
                properties.getProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY,
                        LAST_SEEN_DATA_SET_FILE_DEFAULT);

        lastSeenDataSetFile = new File(lastSeenDataSetFileProperty);
    }

    @Override
    protected Long getLastSeenEventId()
    {
        if (lastSeenDataSetFile.exists())
        {
            String lastSeenEventId = FileUtilities.loadToString(lastSeenDataSetFile).trim();
            try
            {
                return Long.valueOf(lastSeenEventId);
            } catch (NumberFormatException e)
            {
                operationLog
                        .error("Couldn't get the last seen data set from file: "
                                + lastSeenDataSetFile.getAbsolutePath()
                                + " because the contents of that file cannot be parsed to a long value. "
                                + " As there is no last seen data set available all data sets deletions will be taken into consideration.");
                return null;
            }
        } else
        {
            return null;
        }
    }

    @Override
    protected void updateLastSeenEventId(Long eventId)
    {
        FileUtilities.writeToFile(lastSeenDataSetFile, String.valueOf(eventId));
    }

    @Override
    protected void execute(List<DeletedDataSet> datasets)
    {
        operationLog.info("Got " + datasets.size() + " deletions to process");

        deleteKnownDatasets(datasets);
        deleteUnknownDatasets(datasets);
    }

    private void deleteKnownDatasets(List<DeletedDataSet> datasets)
    {
        List<IDatasetLocation> locations = new ArrayList<IDatasetLocation>();

        for (DeletedDataSet dataset : datasets)
        {
            if (isKnownDataset(dataset))
            {
                operationLog.info("Is going to delete a known data set: " + dataset.getCode());

                DatasetLocation location = new DatasetLocation();
                location.setDatasetCode(dataset.getCode());
                location.setDataSetLocation(dataset.getLocationOrNull());
                locations.add(location);
            }
        }

        if (!locations.isEmpty())
        {
            getService().getDataSetDeleter().scheduleDeletionOfDataSets(locations);
        }
    }

    private void deleteUnknownDatasets(List<DeletedDataSet> datasets)
    {
        ISimpleLogger logger = new Log4jSimpleLogger(operationLog);

        for (DeletedDataSet dataset : datasets)
        {
            if (isUnknownDatasets(dataset))
            {
                operationLog.info("Is going to delete an unknown data set: " + dataset.getCode());

                File datasetDir =
                        getDirectoryProvider().getDataSetDirectory(dataset.getShareIdOrNull(),
                                dataset.getLocationOrNull());
                SegmentedStoreUtils.deleteDataSetInstantly(dataset.getCode(), datasetDir, logger);
            }
        }
    }

    private boolean isKnownDataset(DeletedDataSet dataset)
    {
        String dataStoreCode = getConfigProvider().getDataStoreCode();

        boolean isDataStoreMatching = dataStoreCode.equals(dataset.getDatastoreCodeOrNull());
        boolean isKnown = getShareIdManager().isKnown(dataset.getCode());
        boolean isLocationSpecified = dataset.getLocationOrNull() != null;

        return isDataStoreMatching && isKnown && isLocationSpecified;
    }

    private boolean isUnknownDatasets(DeletedDataSet dataset)
    {
        String dataStoreCode = getConfigProvider().getDataStoreCode();

        boolean isDataStoreMatching = dataStoreCode.equals(dataset.getDatastoreCodeOrNull());
        boolean isKnown = getShareIdManager().isKnown(dataset.getCode());
        boolean isLocationSpecified = dataset.getLocationOrNull() != null;
        boolean isShareIdSpecified = dataset.getShareIdOrNull() != null;

        return isDataStoreMatching && !isKnown && isLocationSpecified && isShareIdSpecified;
    }

    private IDataStoreServiceInternal getService()
    {
        return ServiceProvider.getDataStoreService();
    }

    private IConfigProvider getConfigProvider()
    {
        return ServiceProvider.getConfigProvider();
    }

    private IShareIdManager getShareIdManager()
    {
        return ServiceProvider.getShareIdManager();
    }

    private IDataSetDirectoryProvider getDirectoryProvider()
    {
        return new DataSetDirectoryProvider(getConfigProvider().getStoreRoot(), getShareIdManager());
    }

}
