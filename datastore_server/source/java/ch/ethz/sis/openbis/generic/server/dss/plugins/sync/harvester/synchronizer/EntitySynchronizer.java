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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.EntityRetriever;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.ResourceListParserData.Connection;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.ResourceListParserData.DataSetWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.ResourceListParserData.ExperimentWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.ResourceListParserData.MaterialWithLastModificationDate;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.ResourceListParserData.ProjectWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.ResourceListParserData.SampleWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.datasourceconnector.DataSourceConnector;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.datasourceconnector.IDataSourceConnector;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.INameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.PrefixBasedNameTranslator;
import ch.ethz.sis.openbis.generic.shared.entitygraph.EntityGraph;
import ch.ethz.sis.openbis.generic.shared.entitygraph.Node;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.ConversionUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationException;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataTransactionErrors;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Ganime Betul Akin
 */
public class EntitySynchronizer
{
    private final String dataStoreCode;

    private final File storeRoot;

    private final IEncapsulatedOpenBISService service;

    private final DataSetProcessingContext context;

    private final Date lastSyncTimestamp;

    private final Set<String> dataSetsCodesToRetry;

    private final SyncConfig config;

    private final Logger operationLog;

    private final Set<String> blackListedDataSetCodes;

    private final MasterDataRegistrationTransaction masterDataRegistrationTransaction;

    public EntitySynchronizer(IEncapsulatedOpenBISService service, String dataStoreCode, File storeRoot, Date lastSyncTimestamp,
            Set<String> dataSetsCodesToRetry, Set<String> blackListedDataSetCodes, DataSetProcessingContext context,
            SyncConfig config, Logger operationLog)
    {
        this.service = service;
        this.dataStoreCode = dataStoreCode;
        this.storeRoot = storeRoot;
        this.lastSyncTimestamp = lastSyncTimestamp;
        this.dataSetsCodesToRetry = dataSetsCodesToRetry;
        this.blackListedDataSetCodes = blackListedDataSetCodes;
        this.context = context;
        this.config = config;
        this.operationLog = operationLog;
        this.masterDataRegistrationTransaction = getMasterDataRegistrationTransaction();
    }

    public Date syncronizeEntities() throws Exception
    {
        DataSourceConnector dataSourceConnector = new DataSourceConnector(config.getDataSourceURI(), config.getAuthenticationCredentials());
        return syncronizeEntities(dataSourceConnector);
    }

    public Date syncronizeEntities(IDataSourceConnector dataSourceConnector) throws Exception
    {
        // retrieve the document from the data source
        operationLog.info("Retrieving the resource list..");
        Document doc = dataSourceConnector.getResourceListAsXMLDoc(Arrays.asList(ArrayUtils.EMPTY_STRING_ARRAY));

        // Parse the resource list: This sends back all projects,
        // experiments, samples and data sets contained in the XML together with their last modification date to be used for filtering
        operationLog.info("parsing the resource list xml document");
        String dataSourcePrefix = config.getDataSourceAlias();
        INameTranslator nameTranslator = null;
        if (dataSourcePrefix != null && dataSourcePrefix.trim().equals("") == false)
        {
            nameTranslator = new PrefixBasedNameTranslator(dataSourcePrefix);
        }

        ResourceListParser parser = ResourceListParser.create(nameTranslator, dataStoreCode, masterDataRegistrationTransaction); // ,
                                                                                                                                 // lastSyncTimestamp
        ResourceListParserData data = parser.parseResourceListDocument(doc);

        processDeletions(data);

        operationLog.info("registering master data");
        // registerMasterData();

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();

        for (String spaceCode : data.getHarvesterSpaceList())
        {
            Space space = service.tryGetSpace(new SpaceIdentifier(spaceCode));
            if (space == null)
            {
                builder.space(new NewSpace(spaceCode, "Synchronized from: " + config.getDataSourceURI(), null));
            }
        }

        processMetaData(data, builder);

        operationLog.info("Registering meta data...");
        AtomicEntityOperationResult operationResult = service.performEntityOperations(builder.getDetails());
        operationLog.info("entity operation result: " + operationResult);

        // register physical data sets without any hierarchy
        // Note that container/component and parent/child relationships are established post-reg.
        // setParentDataSetsOnTheChildren(data);
        Map<String, DataSetWithConnections> physicalDSMap =
                data.filterPhysicalDataSetsByLastModificationDate(lastSyncTimestamp, dataSetsCodesToRetry);
        operationLog.info("Registering data sets...");
        List<String> notRegisteredDataSets = registerPhysicalDataSets(physicalDSMap);
        operationLog.info((physicalDSMap.keySet().size() - notRegisteredDataSets.size()) + " data set(s) have been successfully registered. "
                + notRegisteredDataSets.size()
                + " data set(s) FAILED to register ");

        // link physical data sets registered above to container data sets
        // and set parent/child relationships
        operationLog.info("start linking/un-linking container and component data sets");
        establishDataSetRelationships(data.getDataSetsToProcess(), notRegisteredDataSets, physicalDSMap);

        return data.getResourceListTimestamp();
    }

    private void establishDataSetRelationships(Map<String, DataSetWithConnections> dataSetsToProcess,
            List<String> notRegisteredDataSets, Map<String, DataSetWithConnections> physicalDSMap)
    {
        // set parent and container data set codes before everything else
        // container and physical data sets can both be parents/children of each other
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        Map<String, NewExternalData> datasetsToUpdate = new HashMap<String, NewExternalData>();
        Map<String, Set<String>> dsToParents = new HashMap<String, Set<String>>();
        Map<String, Set<String>> dsToContained = new HashMap<String, Set<String>>();
        for (DataSetWithConnections dsWithConn : dataSetsToProcess.values())
        {
            for (Connection conn : dsWithConn.getConnections())
            {
                NewExternalData dataSet = dsWithConn.getDataSet();
                if (dataSetsToProcess.containsKey(conn.getToPermId()) && conn.getType().equals("Child"))
                {
                    if (notRegisteredDataSets.contains(dataSet.getCode()) == false)
                    {
                        NewExternalData childDataSet = dataSetsToProcess.get(conn.getToPermId()).getDataSet();
                        List<String> parentDataSetCodes = childDataSet.getParentDataSetCodes();
                        parentDataSetCodes.add(dataSet.getCode());
                        dsToParents.put(childDataSet.getCode(), new HashSet<String>(parentDataSetCodes));
                    }
                }
                else if (dataSetsToProcess.containsKey(conn.getToPermId()) && conn.getType().equals("Component"))
                {
                    NewExternalData componentDataSet = dataSetsToProcess.get(conn.getToPermId()).getDataSet();
                    if (notRegisteredDataSets.contains(componentDataSet.getCode()) == false)
                    {
                        NewContainerDataSet containerDataSet = (NewContainerDataSet) dataSet;
                        List<String> containedDataSetCodes = containerDataSet.getContainedDataSetCodes();
                        containedDataSetCodes.add(componentDataSet.getCode());
                        dsToContained.put(dataSet.getCode(), new HashSet<String>(containedDataSetCodes));
                    }
                }
            }
        }
        // go through all the data sets, decide what needs to be updated
        for (DataSetWithConnections dsWithConn : dataSetsToProcess.values())
        {
            NewExternalData dataSet = (NewExternalData) dsWithConn.getDataSet();

            if (dsWithConn.getLastModificationDate().after(lastSyncTimestamp)
                    || dataSetsCodesToRetry.contains(dataSet.getCode()) == true
                    || isParentModified(dsToParents, dataSet))
            {
                if (physicalDSMap.containsKey(dataSet.getCode()) == false && service.tryGetDataSet(dataSet.getCode()) == null)
                {
                    builder.dataSet(dataSet);
                }
                else
                {
                    datasetsToUpdate.put(dataSet.getCode(), dataSet);
                }
            }
        }

        // go thru to-be-updated DS list and establish/break relations
        for (NewExternalData dataSet : datasetsToUpdate.values())
        {
            // if the DS could not have been registered for some reason,
            // skip this.
            AbstractExternalData dsInHarvester = service.tryGetDataSet(dataSet.getCode());
            if (dsInHarvester == null)
            {
                continue;
            }
            DataSetBatchUpdatesDTO dsBatchUpdatesDTO = createDataSetBatchUpdateDTO(dataSet, dsInHarvester);
            if (dataSet instanceof NewContainerDataSet)
            {
                NewContainerDataSet containerDS = (NewContainerDataSet) dataSet;
                if (dsToContained.containsKey(containerDS.getCode()))
                {
                    dsBatchUpdatesDTO.setModifiedContainedDatasetCodesOrNull(dsToContained.get(dataSet.getCode()).toArray(new
                            String[containerDS.getContainedDataSetCodes().size()]));
                }
                else
                {
                    dsBatchUpdatesDTO.setModifiedContainedDatasetCodesOrNull(new String[0]);
                }
                dsBatchUpdatesDTO.getDetails().setContainerUpdateRequested(true);
            }
            if (dsToParents.containsKey(dataSet.getCode()))
            {
                dsBatchUpdatesDTO.setModifiedParentDatasetCodesOrNull(dsToParents.get(dataSet.getCode()).toArray(
                        new String[dataSet.getParentDataSetCodes().size()]));
                // TODO should this always be true or should we flag the ones that require parent update. Same for container
            }
            else
            {
                dsBatchUpdatesDTO.setModifiedParentDatasetCodesOrNull(new String[0]);
            }
            dsBatchUpdatesDTO.getDetails().setParentsUpdateRequested(true);
            SampleIdentifier sampleIdentifier = dataSet.getSampleIdentifierOrNull();
            if (sampleIdentifier != null)
            {
                Sample sampleWithExperiment = service.tryGetSampleWithExperiment(sampleIdentifier);
                dsBatchUpdatesDTO.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sampleWithExperiment.getIdentifier()));
                dsBatchUpdatesDTO.getDetails().setSampleUpdateRequested(true);
            }
            else
            {
                dsBatchUpdatesDTO.setSampleIdentifierOrNull(null);
                dsBatchUpdatesDTO.getDetails().setSampleUpdateRequested(true);
            }

            ExperimentIdentifier expIdentifier = dataSet.getExperimentIdentifierOrNull();
            if (expIdentifier != null)
            {
                Experiment experiment = service.tryGetExperiment(expIdentifier);
                dsBatchUpdatesDTO.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(experiment.getIdentifier()));
                dsBatchUpdatesDTO.getDetails().setExperimentUpdateRequested(true);
            }
            else
            {
                dsBatchUpdatesDTO.setExperimentIdentifierOrNull(null);
                dsBatchUpdatesDTO.getDetails().setExperimentUpdateRequested(true);
            }
            builder.dataSetUpdate(dsBatchUpdatesDTO);
        }
        AtomicEntityOperationResult operationResult = service.performEntityOperations(builder.getDetails());
        operationLog.info("entity operation result: " + operationResult);
    }

    private boolean isParentModified(Map<String, Set<String>> dsToParents, NewExternalData dataSet)
    {
        Set<String> parents = dsToParents.get(dataSet.getCode());
        if (parents == null)
        {
            return false;
        }
        for (String parentDSCode : parents)
        {
            if (dataSetsCodesToRetry.contains(parentDSCode))
            {
                return true;
            }
        }
        return false;
    }

    private List<String> registerPhysicalDataSets(Map<String, DataSetWithConnections> physicalDSMap) throws IOException
    {
        List<DataSetWithConnections> dsList = new ArrayList<DataSetWithConnections>(physicalDSMap.values());
        List<String> notRegisteredDataSetCodes = Collections.synchronizedList(new ArrayList<String>());

        // This parallelization is possible because each DS is registered without dependencies
        // and the dependencies are established later on in the sync process.
        ParallelizedExecutor.process(dsList, new DataSetRegistrationTaskExecutor(notRegisteredDataSetCodes), 0.5, 10, "register data sets", 0, false);

        // backup the current not synced data set codes file, delete the original file
        saveNotSyncedDataSetsFile(notRegisteredDataSetCodes);

        return notRegisteredDataSetCodes;
    }

    private void saveNotSyncedDataSetsFile(List<String> notRegisteredDataSetCodes) throws IOException
    {
        File notSyncedDataSetsFile = new File(config.getNotSyncedDataSetsFileName());
        if (notSyncedDataSetsFile.exists())
        {
            backupAndResetNotSyncedDataSetsFile(notSyncedDataSetsFile);
        }

        for (String dsCode : notRegisteredDataSetCodes)
        {
            FileUtilities.appendToFile(notSyncedDataSetsFile, dsCode, true);
        }
        // append the blacklisted codes to the end of the file
        for (String dsCode : blackListedDataSetCodes)
        {
            FileUtilities.appendToFile(notSyncedDataSetsFile, dsCode, true);
        }
    }

    private void backupAndResetNotSyncedDataSetsFile(File notSyncedDataSetsFile) throws IOException
    {
        File backupLastSyncTimeStampFile = new File(config.getNotSyncedDataSetsFileName() + ".bk");
        FileUtils.copyFile(notSyncedDataSetsFile, backupLastSyncTimeStampFile);
        FileUtils.writeStringToFile(notSyncedDataSetsFile, "");
    }

    private void processMetaData(ResourceListParserData data, AtomicEntityOperationDetailsBuilder builder)
    {
        processProjects(data, builder);

        processExperiments(data, builder);

        processSamples(data, builder);

        processMaterials(data, builder);
    }

    private void registerMasterData()
    {
        masterDataRegistrationTransaction.commit();
        MasterDataTransactionErrors transactionErrors = masterDataRegistrationTransaction.getTransactionErrors();
        if (false == transactionErrors.getErrors().isEmpty())
        {
            MasterDataRegistrationException masterDataRegistrationException =
                    new MasterDataRegistrationException("Master data synchronization finished with errors:",
                            Collections
                                    .<MasterDataTransactionErrors> singletonList(transactionErrors));
            operationLog.info("Master data synchronizatio finished with errors");
            masterDataRegistrationException.logErrors(new Log4jSimpleLogger(operationLog));
        }
    }

    private MasterDataRegistrationTransaction getMasterDataRegistrationTransaction()
    {
        String openBisServerUrl = ServiceProvider.getConfigProvider().getOpenBisServerUrl();
        String sessionToken = ServiceProvider.getOpenBISService().getSessionToken();
        EncapsulatedCommonServer encapsulatedCommonServer = ServiceFinderUtils.getEncapsulatedCommonServer(sessionToken, openBisServerUrl);
        return new MasterDataRegistrationTransaction(encapsulatedCommonServer);
    }

    private void processDeletions(ResourceListParserData data) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        operationLog.info("Processing deletions");
        String sessionToken = ServiceProvider.getOpenBISService().getSessionToken();
        EntityRetriever entityRetriever =
                EntityRetriever.createWithSessionToken(ServiceProvider.getV3ApplicationService(), sessionToken);

        Set<String> incomingProjectPermIds = data.getProjectsToProcess().keySet();
        Set<String> incomingExperimentPermIds = data.getExperimentsToProcess().keySet();
        Set<String> incomingSamplePermIds = data.getSamplesToProcess().keySet();
        Set<String> incomingDataSetCodes = data.getDataSetsToProcess().keySet();
        Set<String> incomingMaterialCodes = data.getMaterialsToProcess().keySet();

        // find projects, experiments, samples and data sets to be deleted
        List<ProjectPermId> projectPermIds = new ArrayList<ProjectPermId>();
        List<ExperimentPermId> experimentPermIds = new ArrayList<ExperimentPermId>();
        List<SamplePermId> samplePermIds = new ArrayList<SamplePermId>();
        List<DataSetPermId> dsPermIds = new ArrayList<DataSetPermId>();
        List<MaterialPermId> matPermIds = new ArrayList<MaterialPermId>();

        Set<PhysicalDataSet> physicalDataSetsDelete = new HashSet<PhysicalDataSet>();
        // first find out the entities to be deleted
        for (String harvesterSpaceId : data.getHarvesterSpaceList())
        {
            EntityGraph<Node<?>> harvesterEntityGraph = entityRetriever.getEntityGraph(harvesterSpaceId);
            List<Node<?>> entities = harvesterEntityGraph.getNodes();
            for (Node<?> entity : entities)
            {
                if (entity.getEntityKind().equals("PROJECT"))
                {
                    if (incomingProjectPermIds.contains(entity.getPermId()) == false)
                    {
                        projectPermIds.add(new ProjectPermId(entity.getPermId()));
                    }
                }
                else if (entity.getEntityKind().equals("EXPERIMENT"))
                {
                    if (incomingExperimentPermIds.contains(entity.getPermId()) == false)
                    {
                        experimentPermIds.add(new ExperimentPermId(entity.getPermId()));
                    }
                    else
                    {
                        String typeCodeOrNull = entity.getTypeCodeOrNull();
                        NewExperiment exp = data.getExperimentsToProcess().get(entity.getPermId()).getExperiment();
                        if (typeCodeOrNull.equals(exp.getExperimentTypeCode()) == false)
                        {
                            experimentPermIds.add(new ExperimentPermId(entity.getPermId()));
                        }
                    }
                }
                else if (entity.getEntityKind().equals("SAMPLE"))
                {
                    if (incomingSamplePermIds.contains(entity.getPermId()) == false)
                    {
                        samplePermIds.add(new SamplePermId(entity.getPermId()));
                    }
                    else
                    {
                        String typeCodeOrNull = entity.getTypeCodeOrNull();
                        NewSample smp = data.getSamplesToProcess().get(entity.getPermId()).getSample();
                        if (typeCodeOrNull.equals(smp.getSampleType().getCode()) == false)
                        {
                            samplePermIds.add(new SamplePermId(entity.getPermId()));
                        }
                    }
                }
                else if (entity.getEntityKind().equals("DATA_SET"))
                {
                    if (incomingDataSetCodes.contains(entity.getPermId()) == false)
                    {
                        dsPermIds.add(new DataSetPermId(entity.getPermId()));
                    }
                    else
                    {
                        boolean sameDS = true;
                        // if (ds.getKind() == DataSetKind.PHYSICAL && ds.lastModificationDate.after(lastSyncDate))
                        String typeCodeOrNull = entity.getTypeCodeOrNull();

                        DataSetWithConnections dsWithConns = data.getDataSetsToProcess().get(entity.getPermId());
                        NewExternalData ds = dsWithConns.getDataSet();
                        if (typeCodeOrNull.equals(ds.getDataSetType().getCode()) == false)
                        {
                            sameDS = false;
                        }
                        else
                        {
                            if (dsWithConns.getKind() == DataSetKind.PHYSICAL && dsWithConns.getLastModificationDate().after(lastSyncTimestamp))
                            {
                                PhysicalDataSet physicalDS = service.tryGetDataSet(entity.getPermId()).tryGetAsDataSet();
                                sameDS = deepCompareDataSets(entity.getPermId());
                                if (sameDS == false)
                                    physicalDataSetsDelete.add(physicalDS);
                            }
                        }
                        if (sameDS == false)
                        {
                            dsPermIds.add(new DataSetPermId(entity.getPermId()));
                        }
                    }
                }
            }
        }

        List<ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material> materials = entityRetriever.fetchMaterials();

        for (ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material material : materials)
        {
            if (incomingMaterialCodes.contains(material.getCode()) == false)
            {
                matPermIds.add(new MaterialPermId(material.getCode(), material.getType().getCode()));
            }
        }

        IApplicationServerApi v3Api = ServiceProvider.getV3ApplicationService();

        // delete data sets
        DataSetDeletionOptions dsDeletionOpts = new DataSetDeletionOptions();
        dsDeletionOpts.setReason("sync data set deletions"); // TODO maybe mention data source space id in the reason

        IDeletionId dsDeletionId =
                v3Api.deleteDataSets(sessionToken, dsPermIds, dsDeletionOpts);

        // delete samples
        SampleDeletionOptions sampleDeletionOptions = new SampleDeletionOptions();
        sampleDeletionOptions.setReason("sync sample deletions");
        IDeletionId smpDeletionId = v3Api.deleteSamples(sessionToken, samplePermIds, sampleDeletionOptions);

        // delete experiments
        ExperimentDeletionOptions expDeletionOpts = new ExperimentDeletionOptions();
        expDeletionOpts.setReason("sync experiment deletions");
        IDeletionId expDeletionId = v3Api.deleteExperiments(sessionToken, experimentPermIds, expDeletionOpts);

        // delete projects
        ProjectDeletionOptions prjDeletionOpts = new ProjectDeletionOptions();
        prjDeletionOpts.setReason("Sync projects");
        v3Api.deleteProjects(sessionToken, projectPermIds, prjDeletionOpts);

        // delete materials
        MaterialDeletionOptions matDeletionOptions = new MaterialDeletionOptions();
        matDeletionOptions.setReason("sync materials");
        v3Api.deleteMaterials(sessionToken, matPermIds, matDeletionOptions);

        // confirm deletions
        ArrayList<IDeletionId> deletionIds = new ArrayList<IDeletionId>();

        StringBuffer summary = new StringBuffer();
        if (projectPermIds.size() > 0)
        {
            summary.append(projectPermIds.size() + " projects,");
        }
        if (matPermIds.size() > 0)
        {
            summary.append(matPermIds.size() + " materials,");
        }
        if (expDeletionId != null)
        {
            deletionIds.add(expDeletionId);
            summary.append(experimentPermIds.size() + " experiments,");
        }
        if (smpDeletionId != null)
        {
            deletionIds.add(smpDeletionId);
            summary.append(samplePermIds.size() + " samples,");
        }
        if (dsDeletionId != null)
        {
            deletionIds.add(dsDeletionId);
            summary.append(dsPermIds.size() + " data sets");
        }
        v3Api.confirmDeletions(sessionToken, deletionIds); // Arrays.asList(expDeletionId, dsDeletionId, smpDeletionId)

        if (summary.length() > 0)
        {
            operationLog.info(summary.substring(0, summary.length() - 1) + " have been deleted:");
        }
        for (PhysicalDataSet physicalDS : physicalDataSetsDelete)
        {
            operationLog.info("Is going to delete the location: " + physicalDS.getLocation());
            File datasetDir =
                    getDirectoryProvider().getDataSetDirectory(physicalDS);
            SegmentedStoreUtils.deleteDataSetInstantly(physicalDS.getCode(), datasetDir, new Log4jSimpleLogger(operationLog));
        }
    }

    private void processExperiments(ResourceListParserData data,
            AtomicEntityOperationDetailsBuilder builder)
    {
        // process experiments
        Map<String, ExperimentWithConnections> experimentsToProcess = data.getExperimentsToProcess();
        for (ExperimentWithConnections exp : experimentsToProcess.values())
        {
            NewExperiment newIncomingExp = exp.getExperiment();
            if (exp.getLastModificationDate().after(lastSyncTimestamp))
            {
                Experiment experiment = null;
                try
                {
                    experiment = service.tryGetExperimentByPermId(newIncomingExp.getPermID());
                } catch (Exception e)
                {
                    // doing nothing because when the experiment with the perm id not found
                    // an exception will be thrown. Seems to be the same with entity kinds
                }

                if (experiment == null)
                {
                    // ADD EXPERIMENT
                    builder.experiment(newIncomingExp);
                }
                else
                {
                    // UPDATE EXPERIMENT
                    ExperimentUpdatesDTO expUpdate = createExperimentUpdateDTOs(newIncomingExp, experiment);
                    builder.experimentUpdate(expUpdate);
                }
            }
            handleExperimentConnections(data, exp, newIncomingExp);
        }
    }

    private void handleExperimentConnections(ResourceListParserData data, ExperimentWithConnections exp, NewExperiment newIncomingExp)
    {
        Map<String, SampleWithConnections> samplesToProcess = data.getSamplesToProcess();
        Map<String, DataSetWithConnections> dataSetsToProcess = data.getDataSetsToProcess();
        for (Connection conn : exp.getConnections())
        {
            if (samplesToProcess.containsKey(conn.getToPermId()))
            {
                SampleWithConnections sample = samplesToProcess.get(conn.getToPermId());
                NewSample newSample = sample.getSample();
                newSample.setExperimentIdentifier(newIncomingExp.getIdentifier());
            }
            if (dataSetsToProcess.containsKey(conn.getToPermId()))
            {
                NewExternalData externalData = dataSetsToProcess.get(conn.getToPermId()).getDataSet();
                externalData.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(newIncomingExp.getIdentifier()));
            }
        }
    }

    private ExperimentUpdatesDTO createExperimentUpdateDTOs(NewExperiment newIncomingExp, Experiment experiment)
    {
        ExperimentUpdatesDTO expUpdate = new ExperimentUpdatesDTO();
        expUpdate.setProjectIdentifier(ExperimentIdentifierFactory.parse(newIncomingExp.getIdentifier()));
        expUpdate.setVersion(experiment.getVersion());
        expUpdate.setProperties(Arrays.asList(newIncomingExp.getProperties()));
        expUpdate.setExperimentId(TechId.create(experiment));
        // TODO attachments
        expUpdate.setAttachments(Collections.<NewAttachment> emptyList());
        return expUpdate;
    }

    private void processMaterials(ResourceListParserData data, AtomicEntityOperationDetailsBuilder builder)
    {
        // process materials
        Map<String, MaterialWithLastModificationDate> materialsToProcess = data.getMaterialsToProcess();
        for (MaterialWithLastModificationDate newMaterialWithType : materialsToProcess.values())
        {
            NewMaterialWithType newIncomingMaterial = newMaterialWithType.getMaterial();
            if (newMaterialWithType.getLastModificationDate().after(lastSyncTimestamp))
            {
                Material material = service.tryGetMaterial(new MaterialIdentifier(newIncomingMaterial.getCode(), newIncomingMaterial.getType()));
                if (material == null)
                {
                    builder.material(newIncomingMaterial);
                }
                else
                {
                    MaterialUpdateDTO update =
                            new MaterialUpdateDTO(TechId.create(material), Arrays.asList(newIncomingMaterial.getProperties()),
                                    material.getModificationDate());
                    builder.materialUpdate(update);
                }
            }
        }
    }

    private void processProjects(ResourceListParserData data, AtomicEntityOperationDetailsBuilder builder)
    {
        Map<String, ProjectWithConnections> projectsToProcess = data.getProjectsToProcess();
        for (ProjectWithConnections prj : projectsToProcess.values())
        {
            NewProject incomingProject = prj.getProject();
            if (prj.getLastModificationDate().after(lastSyncTimestamp))
            {
                Project project = null;
                try
                {
                    project = service.tryGetProjectByPermId(incomingProject.getPermID());
                } catch (Exception e)
                {
                    // TODO doing nothing because when the project with the perm is not found
                    // an exception will be thrown. See bug report SSDM-4108
                }

                if (project == null)
                {
                    // ADD PROJECT
                    builder.project(incomingProject);
                }
                else
                {
                    // UPDATE PROJECT
                    builder.projectUpdate(createProjectUpdateDTO(incomingProject, project));
                }
            }
            // handleProjectConnections(data, prj);
        }
    }

    private void handleProjectConnections(ResourceListParserData data, ProjectWithConnections prj)
    {
        Map<String, ExperimentWithConnections> experimentsToProcess = data.getExperimentsToProcess();
        for (Connection conn : prj.getConnections())
        {
            String connectedExpPermId = conn.getToPermId();
            // TODO we need to do the same check for samples to support project samples
            if (experimentsToProcess.containsKey(connectedExpPermId))
            {
                // the project is connected to an experiment
                ExperimentWithConnections exp = experimentsToProcess.get(connectedExpPermId);
                NewExperiment newExp = exp.getExperiment();
                Experiment experiment = service.tryGetExperimentByPermId(connectedExpPermId);
                // check if our local graph has the same connection
                if (service.tryGetExperiment(ExperimentIdentifierFactory.parse(newExp.getIdentifier())) == null)
                {
                    // add new edge
                    String oldIdentifier = newExp.getIdentifier();
                    int index = oldIdentifier.lastIndexOf('/');
                    String expCode = oldIdentifier.substring(index + 1);
                    newExp.setIdentifier(prj.getProject().getIdentifier() + "/" + expCode);
                    // add new experiment node
                }
            }
            else
            {
                // This means the XML contains the connection but not the connected entity.
                // This is an unlikely scenario.
                operationLog.info("Connected experiment with permid : " + connectedExpPermId + " is missing");
            }
        }
    }

    private ProjectUpdatesDTO createProjectUpdateDTO(NewProject incomingProject, Project project)
    {
        ProjectUpdatesDTO prjUpdate = new ProjectUpdatesDTO();
        prjUpdate.setVersion(project.getVersion());
        prjUpdate.setTechId(TechId.create(project));
        prjUpdate.setDescription(incomingProject.getDescription());
        // TODO attachments????
        prjUpdate.setAttachments(Collections.<NewAttachment> emptyList());
        ProjectIdentifier projectIdentifier = ProjectIdentifierFactory.parse(incomingProject.getIdentifier());
        prjUpdate.setSpaceCode(projectIdentifier.getSpaceCode());
        return prjUpdate;
    }

    private void processSamples(ResourceListParserData data, AtomicEntityOperationDetailsBuilder builder)
    {
        // process samples
        Map<String, SampleWithConnections> samplesToProcess = data.getSamplesToProcess();
        Map<SampleIdentifier, NewSample> samplesToUpdate = new HashMap<SampleIdentifier, NewSample>();
        Set<String> sampleWithUpdatedParents = new HashSet<String>();
        for (SampleWithConnections sample : samplesToProcess.values())
        {
            NewSample incomingSample = sample.getSample();
            if (sample.getLastModificationDate().after(lastSyncTimestamp))
            {
                SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(incomingSample);
                Sample sampleWithExperiment = null;
                try
                {
                    sampleWithExperiment = service.tryGetSampleByPermId(incomingSample.getPermID());
                } catch (Exception e)
                {
                    // doing nothing because when the sample with the perm is not found
                    // an exception will be thrown. See the same issue for projects
                }
                if (sampleWithExperiment == null)
                {
                    // ADD SAMPLE
                    builder.sample(incomingSample);
                }
                else
                {
                    // defer creation of sample update objects until all samples have been gone through;
                    samplesToUpdate.put(sampleIdentifier, incomingSample);
                    List<Sample> childSamples = getChildSamples(sampleWithExperiment);
                    for (Sample child : childSamples)
                    {
                        String childSampleIdentifier = child.getIdentifier();// edgeNodePair.getNode().getIdentifier();
                        SampleWithConnections childSampleWithConns = findChildInSamplesToProcess(childSampleIdentifier, samplesToProcess);
                        if (childSampleWithConns == null)
                        {
                            // TODO Handle sample delete
                        }
                        else
                        {
                            // the childSample will appear in the incoming samples list anyway
                            // but we want to make sure its parent modification is handled
                            NewSample childSample = childSampleWithConns.getSample();
                            sampleWithUpdatedParents.add(childSample.getIdentifier());
                        }
                    }
                }
            }
            for (Connection conn : sample.getConnections())
            {
                if (conn.getType().equals("Component"))
                {
                    NewSample containedSample = samplesToProcess.get(conn.getToPermId()).getSample();
                    containedSample.setContainerIdentifier(incomingSample.getIdentifier());
                }
                else if (conn.getType().equals("Child"))
                {
                    NewSample childSample = samplesToProcess.get(conn.getToPermId()).getSample();
                    String[] parents = childSample.getParentsOrNull();
                    List<String> parentIds = null;
                    if (parents == null)
                    {
                        parentIds = new ArrayList<String>();
                    }
                    else
                    {
                        parentIds = new ArrayList<String>(Arrays.asList(parents));
                    }
                    parentIds.add(incomingSample.getIdentifier());
                    childSample.setParentsOrNull(parentIds.toArray(new String[parentIds.size()]));
                }
                // TODO how about Connection Type
                // else if (conn.getType().equals("Connection")) // TODO not sure if this guarantees that we have a dataset in the toPermId
                // {
                // NewExternalData externalData = dataSetsToCreate.get(conn.getToPermId()).getDataSet();
                // externalData.setSampleIdentifierOrNull(new SampleIdentifier(newSmp.getIdentifier()));
                // }
            }
        }

        // create sample update dtos for the samples that need to be updated
        for (SampleIdentifier sampleIdentifier : samplesToUpdate.keySet())
        {
            NewSample newSmp = samplesToUpdate.get(sampleIdentifier);
            Sample sampleWithExperiment = service.tryGetSampleByPermId(newSmp.getPermID());

            TechId sampleId = TechId.create(sampleWithExperiment);
            ExperimentIdentifier experimentIdentifier = getExperimentIdentifier(newSmp);
            ProjectIdentifier projectIdentifier = getProjectIdentifier(newSmp);
            String[] modifiedParentIds = newSmp.getParentsOrNull();
            if (modifiedParentIds == null)
            {
                if (sampleWithUpdatedParents.contains(newSmp.getIdentifier()))
                {
                    modifiedParentIds = new String[0];
                }
            }
            String containerIdentifier = getContainerIdentifier(newSmp);

            SampleUpdatesDTO updates =
                    new SampleUpdatesDTO(sampleId, Arrays.asList(newSmp.getProperties()), experimentIdentifier, 
                            projectIdentifier, Collections.<NewAttachment> emptyList(), 
                            sampleWithExperiment.getVersion(), sampleIdentifier, containerIdentifier,
                            modifiedParentIds);
            builder.sampleUpdate(updates);
        }
    }

    private String getContainerIdentifier(NewSample newSmp)
    {
        String containerIdentifier = newSmp.getContainerIdentifier();
        return containerIdentifier == null ? null : containerIdentifier;
    }

    private ExperimentIdentifier getExperimentIdentifier(NewSample newSmp)
    {
        String expIdentifier = newSmp.getExperimentIdentifier();
        if (expIdentifier == null)
        {
            return null;
        }
        return ExperimentIdentifierFactory.parse(expIdentifier);
    }

    private ProjectIdentifier getProjectIdentifier(NewSample sample)
    {
        String projectIdentifier = sample.getProjectIdentifier();
        if (projectIdentifier == null)
        {
            return null;
        }
        return ProjectIdentifierFactory.parse(projectIdentifier);
    }
    private List<Sample> getChildSamples(Sample sampleWithExperiment)
    {
        ListSampleCriteria criteria = ListSampleCriteria.createForParent(new TechId(sampleWithExperiment.getId()));
        return service.listSamples(criteria);
    }

    private SampleWithConnections findChildInSamplesToProcess(String childSampleIdentifier, Map<String, SampleWithConnections> samplesToProcess)
    {
        for (SampleWithConnections sample : samplesToProcess.values())
        {
            if (sample.getSample().getIdentifier().equals(childSampleIdentifier))
            {
                return sample;
            }
        }
        return null;
    }

    private final class DataSetRegistrationTaskExecutor implements ITaskExecutor<DataSetWithConnections>
    {

        private List<String> notRegisteredDataSetCodes;

        public DataSetRegistrationTaskExecutor(List<String> notRegisteredDataSetCodes)
        {
            this.notRegisteredDataSetCodes = notRegisteredDataSetCodes;
        }

        @Override
        public Status execute(DataSetWithConnections dataSet)
        {
            System.out.println("start " + dataSet.getDataSet().getCode());

            Properties props = setProperties();

            DataSetRegistrationIngestionService ingestionService =
                    new DataSetRegistrationIngestionService(props, storeRoot, notRegisteredDataSetCodes, dataSet.getDataSet(), operationLog);
            TableModel resultTable = ingestionService.createAggregationReport(new HashMap<String, Object>(), context);
            if (resultTable != null)
            {
                List<TableModelColumnHeader> headers = resultTable.getHeader();
                String[] stringArray = new String[headers.size()];
                for (int i = 0; i < stringArray.length; i++)
                {
                    if (headers.get(i).getTitle().startsWith("Error"))
                    {
                        String message = resultTable.getRows().get(0).getValues().toString();
                        notRegisteredDataSetCodes.add(dataSet.getDataSet().getCode());
                        operationLog.error(message);
                        return Status.createError(message);
                    }
                }
            }
            return Status.OK;
        }

        private Properties setProperties()
        {
            Properties props = new Properties();
            props.setProperty("user", EntitySynchronizer.this.config.getUser());
            props.setProperty("pass", EntitySynchronizer.this.config.getPassword());
            props.setProperty("as-url", EntitySynchronizer.this.config.getDataSourceOpenbisURL());
            props.setProperty("dss-url", EntitySynchronizer.this.config.getDataSourceDSSURL());
            props.setProperty("harvester-temp-dir", EntitySynchronizer.this.config.getHarvesterTempDir());
            props.setProperty("do-not-create-original-dir", "true");
            return props;
        }
    }

    private boolean deepCompareDataSets(String dataSetCode)
            throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        // get the file nodes in the incoming DS by querying the data source openbis
        String asUrl = config.getDataSourceOpenbisURL();
        String dssUrl = config.getDataSourceDSSURL();

        DSSFileUtils dssFileUtils = DSSFileUtils.create(asUrl, dssUrl);
        String sessionToken = dssFileUtils.login(config.getUser(), config.getPassword());

        DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
        criteria.withDataSet().withCode().thatEquals(dataSetCode);
        SearchResult<DataSetFile> result = dssFileUtils.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());

        // get the file nodes in the harvester openbis
        IDataStoreServerApi dssharvester = (IDataStoreServerApi) ServiceProvider.getDssServiceV3().getService();
        SearchResult<DataSetFile> resultHarvester =
                dssharvester.searchFiles(ServiceProvider.getOpenBISService().getSessionToken(), criteria, new DataSetFileFetchOptions());
        if (result.getTotalCount() != resultHarvester.getTotalCount())
        {
            return false;
        }
        List<DataSetFile> dsNodes = result.getObjects();
        List<DataSetFile> harvesterNodes = resultHarvester.getObjects();
        sortFileNodes(dsNodes);
        sortFileNodes(harvesterNodes);
        return calculateHash(dsNodes).equals(calculateHash(harvesterNodes));
    }

    private void sortFileNodes(List<DataSetFile> nodes)
    {
        Collections.sort(nodes, new Comparator<DataSetFile>()
            {

                @Override
                public int compare(DataSetFile dsFile1, DataSetFile dsFile2)
                {
                    return dsFile1.getPath().compareTo(dsFile2.getPath());
                }
            });
    }

    private String calculateHash(List<DataSetFile> nodes) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        for (DataSetFile dataSetFile : nodes)
        {
            sb.append(dataSetFile.getPath());
            sb.append(dataSetFile.getChecksumCRC32());
            sb.append(dataSetFile.getFileLength());
        }
        byte[] digest = MessageDigest.getInstance("MD5").digest(new String(sb).getBytes("UTF-8"));
        return new String(Hex.encodeHex(digest));
    }

    private DataSetBatchUpdatesDTO createDataSetBatchUpdateDTO(NewExternalData childDS, AbstractExternalData dsInHarvester)
    {
        ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetUpdatable updateUpdatable = new
                ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetUpdatable(dsInHarvester, service);
        DataSetBatchUpdatesDTO dsBatchUpdatesDTO = ConversionUtils.convertToDataSetBatchUpdatesDTO(updateUpdatable);
        dsBatchUpdatesDTO.setDatasetId(TechId.create(dsInHarvester));
        List<IEntityProperty> entityProperties = new ArrayList<IEntityProperty>();
        for (NewProperty prop : childDS.getDataSetProperties())
        {
            String propertyCode = prop.getPropertyCode();
            String value = prop.getValue();
            entityProperties.add(new PropertyBuilder(propertyCode).value(value).getProperty());
        }
        dsBatchUpdatesDTO.setProperties(entityProperties);
        return dsBatchUpdatesDTO;
    }

    private IDataSetDirectoryProvider getDirectoryProvider()
    {
        return new DataSetDirectoryProvider(getConfigProvider().getStoreRoot(), getShareIdManager());
    }

    private IConfigProvider getConfigProvider()
    {
        return ServiceProvider.getConfigProvider();
    }

    private IShareIdManager getShareIdManager()
    {
        return ServiceProvider.getShareIdManager();
    }

}