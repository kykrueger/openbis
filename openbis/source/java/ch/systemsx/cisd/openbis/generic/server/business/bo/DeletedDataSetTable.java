package ch.systemsx.cisd.openbis.generic.server.business.bo;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * BO for handling {@link DataPE}.
 * 
 * @author Piotr Buczek
 */
public final class DeletedDataSetTable extends AbstractDataSetBusinessObject implements
        IDeletedDataSetTable
{

    // private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
    // DeletedDataSetTable.class);

    private static void assertDatasetsAreDeletable(List<DeletedDataPE> datasets)
    {
        List<String> notDeletableDatasets = new ArrayList<String>();
        for (DeletedDataPE dataSet : datasets)
        {
            if (dataSet.isDeletable() == false)
            {
                notDeletableDatasets.add(dataSet.getCode());
            }
        }
        if (notDeletableDatasets.isEmpty() == false)
        {
            throw UserFailureException.fromTemplate(
                    "Deletion failed because the following data sets are required "
                            + "by a background process (their status is pending): %s. ",
                    CollectionUtils.abbreviate(notDeletableDatasets, 10));
        }
    }

    private final IDataStoreServiceFactory dssFactory;

    private List<DeletedDataPE> deletedDataSets;

    public DeletedDataSetTable(final IDAOFactory daoFactory, IDataStoreServiceFactory dssFactory,
            final Session session)
    {
        super(daoFactory, session);
        this.dssFactory = dssFactory;
    }

    //
    // IExternalDataTable
    //

    public void loadByDataSetCodes(List<String> dataSetCodes)
    {
        IDataDAO dataDAO = getDataDAO();

        deletedDataSets = new ArrayList<DeletedDataPE>();
        deletedDataSets.addAll(dataDAO.tryToFindDeletedDataSetsByCodes(dataSetCodes));
    }

    public void permanentlyDeleteLoadedDataSets(String reason)
    {
        assertDatasetsAreDeletable(deletedDataSets);

        Map<DataStorePE, List<DeletedDataPE>> allToBeDeleted = groupDataSetsByDataStores();
        Map<DataStorePE, List<DeletedExternalDataPE>> availableDatasets =
                filterAvailableDatasets(allToBeDeleted);

        assertDataSetsAreKnown(availableDatasets);
        for (Map.Entry<DataStorePE, List<DeletedDataPE>> entry : allToBeDeleted.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<DeletedDataPE> allDataSets = entry.getValue();
            deleteLocallyFromDB(reason, allDataSets);
            deleteRemotelyFromDataStore(availableDatasets, dataStore);
        }
    }

    private void deleteLocallyFromDB(String reason, List<DeletedDataPE> dataSetsToDelete)
    {
        deleteByTechIds(TechId.createList(dataSetsToDelete), reason);
    }

    private void deleteByTechIds(List<TechId> dataSetIds, String reason)
            throws UserFailureException
    {
        try
        {
            getSessionFactory().getCurrentSession().flush();
            getSessionFactory().getCurrentSession().clear();
            getDataDAO().delete(dataSetIds, session.tryGetPerson(), reason);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Data Set", EntityKind.DATA_SET);
        }
    }

    private void deleteRemotelyFromDataStore(
            Map<DataStorePE, List<DeletedExternalDataPE>> availableDatasets, DataStorePE dataStore)
    {
        List<DeletedExternalDataPE> availableDatasetsInStore = availableDatasets.get(dataStore);
        deleteDataSets(dataStore, createDatasetDescriptions(availableDatasetsInStore));
    }

    private Map<DataStorePE, List<DeletedExternalDataPE>> filterAvailableDatasets(
            Map<DataStorePE, List<DeletedDataPE>> map)
    {
        Map<DataStorePE, List<DeletedExternalDataPE>> result =
                new HashMap<DataStorePE, List<DeletedExternalDataPE>>();
        for (Map.Entry<DataStorePE, List<DeletedDataPE>> entry : map.entrySet())
        {
            ArrayList<DeletedExternalDataPE> available = new ArrayList<DeletedExternalDataPE>();
            for (DeletedDataPE data : entry.getValue())
            {
                DeletedExternalDataPE externalData = data.tryAsExternalData();
                if (externalData != null && externalData.isAvailable())
                {
                    available.add(externalData);
                }
            }
            result.put(entry.getKey(), available);
        }
        return result;
    }

    private void assertDataSetsAreKnown(Map<DataStorePE, List<DeletedExternalDataPE>> map)
    {
        // Set<String> knownLocations = new LinkedHashSet<String>();
        List<String> unknownDataSets = new ArrayList<String>();
        for (Map.Entry<DataStorePE, List<DeletedExternalDataPE>> entry : map.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<DeletedExternalDataPE> externalDataSets = entry.getValue();
            Set<String> knownLocations =
                    getKnownDataSets(dataStore, createDatasetDescriptions(externalDataSets));
            for (DeletedExternalDataPE dataSet : externalDataSets)
            {
                if (knownLocations.contains(dataSet.getLocation()) == false)
                {
                    unknownDataSets.add(dataSet.getCode());
                }
            }
        }
        if (unknownDataSets.isEmpty() == false)
        {
            throw new UserFailureException(
                    "The following data sets are unknown by Data Store Servers they were registered in. "
                            + "May be the responsible Data Store Servers are not running.\n"
                            + unknownDataSets);
        }
    }

    /**
     * groups all deleted data sets (both virtual and non-virtual) by data stores
     * 
     * @param deletedDataSets
     */
    private Map<DataStorePE, List<DeletedDataPE>> groupDataSetsByDataStores()
    {
        Map<DataStorePE, List<DeletedDataPE>> map =
                new LinkedHashMap<DataStorePE, List<DeletedDataPE>>();
        for (DeletedDataPE dataSet : deletedDataSets)
        {
            DataStorePE dataStore = dataSet.getDataStore();
            List<DeletedDataPE> list = map.get(dataStore);
            if (list == null)
            {
                list = new ArrayList<DeletedDataPE>();
                map.put(dataStore, list);
            }
            list.add(dataSet);
        }
        return map;
    }

    // TODO old, not modified, should be extracted

    private void deleteDataSets(DataStorePE dataStore, List<DatasetDescription> list)
    {
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            // Nothing to delete on dummy data store
            return;
        }
        String sessionToken = dataStore.getSessionToken();
        service.deleteDataSets(sessionToken, list);
    }

    // null if DSS URL has not been specified
    private IDataStoreService tryGetDataStoreService(DataStorePE dataStore)
    {
        String remoteURL = dataStore.getRemoteUrl();
        if (StringUtils.isBlank(remoteURL))
        {
            return null;
        }
        return dssFactory.create(remoteURL);
    }

    private Set<String> getKnownDataSets(DataStorePE dataStore,
            List<DatasetDescription> dataSetDescriptions)
    {
        String remoteURL = dataStore.getRemoteUrl();
        if (StringUtils.isBlank(remoteURL))
        {
            // Assuming dummy data store "knows" all locations
            Set<String> locations = new HashSet<String>();
            for (DatasetDescription dataSet : dataSetDescriptions)
            {
                locations.add(dataSet.getDataSetLocation());
            }
            return locations;
        }
        IDataStoreService service = dssFactory.create(remoteURL);
        String sessionToken = dataStore.getSessionToken();
        return new HashSet<String>(service.getKnownDataSets(sessionToken, dataSetDescriptions));
    }

    private List<DatasetDescription> createDatasetDescriptions(List<DeletedExternalDataPE> datasets)
    {
        List<DatasetDescription> result = new ArrayList<DatasetDescription>();
        for (DeletedExternalDataPE dataset : datasets)
        {
            result.add(createDatasetDescription(dataset));
        }
        return result;
    }

    private DatasetDescription createDatasetDescription(DeletedExternalDataPE dataSet)
    {
        assert dataSet != null;

        // TODO we don't need most of fields in DatasetDescription to delete a data set
        // use IDatasetLocation instead
        
        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode(dataSet.getCode());

        DeletedExternalDataPE externalData = dataSet;
        description.setDataSetLocation(externalData.getLocation());
        description.setDataSetSize(externalData.getSize());
        description.setSpeedHint(externalData.getSpeedHint());

        DataSetTypePE dataSetType = dataSet.getDataSetType();
        description.setMainDataSetPath(dataSetType.getMainDataSetPath());
        description.setMainDataSetPattern(dataSetType.getMainDataSetPattern());
        description.setDatasetTypeCode(dataSetType.getCode());

        return description;
    }

}