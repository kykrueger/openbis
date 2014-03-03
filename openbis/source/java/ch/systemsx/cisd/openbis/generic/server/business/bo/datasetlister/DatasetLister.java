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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.lang.time.DateUtils;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.AbstractLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreTranslator;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
{ DatasetRecord.class, DatasetRelationRecord.class, DataStoreRecord.class,
        DatasetCodeWithShareIdRecord.class, IDatasetListingQuery.class })
public class DatasetLister extends AbstractLister implements IDatasetLister
{
    public static final EnumSet<DataSetFetchOption> SUPPORTED_DATASET_FETCH_OPTIONS = EnumSet.of(
            DataSetFetchOption.BASIC, DataSetFetchOption.EXPERIMENT, DataSetFetchOption.SAMPLE,
            DataSetFetchOption.PROPERTIES, DataSetFetchOption.CHILDREN, DataSetFetchOption.PARENTS,
            DataSetFetchOption.PROPERTIES_OF_PARENTS, DataSetFetchOption.PROPERTIES_OF_CHILDREN,
            DataSetFetchOption.CONTAINER, DataSetFetchOption.CONTAINED,
            DataSetFetchOption.METAPROJECTS);

    public static final EnumSet<DataSetFetchOption> DEFAULT_DATASET_FETCH_OPTIONS = EnumSet.of(
            DataSetFetchOption.BASIC, DataSetFetchOption.EXPERIMENT, DataSetFetchOption.SAMPLE,
            DataSetFetchOption.PROPERTIES, DataSetFetchOption.PARENTS,
            DataSetFetchOption.CONTAINER, DataSetFetchOption.CONTAINED,
            DataSetFetchOption.METAPROJECTS);

    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    private final String baseIndexURL;

    private final Long userId;

    //
    // Working interfaces
    //

    private final IDatasetListingQuery query;

    private final IEntityPropertiesEnricher propertiesEnricher;

    private final SecondaryEntityDAO referencedEntityDAO;

    //
    // Working data structures
    //

    private final Long2ObjectMap<DataSetType> dataSetTypes =
            new Long2ObjectOpenHashMap<DataSetType>();

    private final Long2ObjectMap<DataStore> dataStores = new Long2ObjectOpenHashMap<DataStore>();

    private final Map<Long, FileFormatType> fileFormatTypes = new HashMap<Long, FileFormatType>();

    private final Map<Long, LocatorType> locatorTypes = new HashMap<Long, LocatorType>();

    private final Long2ObjectMap<ExternalDataManagementSystem> externalDataManagementSystems =
            new Long2ObjectOpenHashMap<ExternalDataManagementSystem>();

    public static IDatasetLister create(IDAOFactory daoFactory, String baseIndexURL, Long userId)
    {
        DatasetListerDAO dao = DatasetListerDAO.create(daoFactory);
        SecondaryEntityDAO referencedEntityDAO = SecondaryEntityDAO.create(daoFactory);

        return create(dao, referencedEntityDAO, baseIndexURL, userId);
    }

    static IDatasetLister create(DatasetListerDAO dao, SecondaryEntityDAO referencedEntityDAO,
            String baseIndexURL, Long userId)
    {
        IDatasetListingQuery query = dao.getQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new DatasetLister(dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), query,
                propertiesEnricher, referencedEntityDAO, baseIndexURL, userId);
    }

    // For unit tests
    DatasetLister(final long databaseInstanceId, final DatabaseInstance databaseInstance,
            final IDatasetListingQuery query, IEntityPropertiesEnricher propertiesEnricher,
            SecondaryEntityDAO referencedEntityDAO, String baseIndexURL, Long userId)
    {
        super(referencedEntityDAO);
        assert databaseInstance != null;
        assert query != null;

        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.propertiesEnricher = propertiesEnricher;
        this.referencedEntityDAO = referencedEntityDAO;
        this.baseIndexURL = baseIndexURL;
        this.userId = userId;
    }

    private void checkFetchOptions(EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        EnumSet<DataSetFetchOption> work = EnumSet.copyOf(datasetFetchOptions);
        work.removeAll(SUPPORTED_DATASET_FETCH_OPTIONS);
        if (work.isEmpty() == false)
        {
            throw new IllegalArgumentException("Currently only " + SUPPORTED_DATASET_FETCH_OPTIONS
                    + " fetch options are supported by this method");
        }
    }

    @Override
    public List<AbstractExternalData> listBySampleTechId(TechId sampleId,
            boolean showOnlyDirectlyConnected)
    {
        if (showOnlyDirectlyConnected)
        {
            return enrichDatasets(query.getDatasetsForSample(sampleId.getId()));
        } else
        {
            // get all descendands of the sample
            LongSet sampleIds = referencedEntityDAO.getSampleDescendantIdsAndSelf(sampleId.getId());
            // get directly connected datasets, then go layer by layer into children datasets
            LongSet results = new LongOpenHashSet();
            LongSet currentLayer = new LongOpenHashSet(query.getDatasetIdsForSamples(sampleIds));
            LongSet nextLayer;
            while (currentLayer.isEmpty() == false)
            {
                nextLayer = new LongOpenHashSet(query.getDatasetChildrenIds(currentLayer));
                results.addAll(currentLayer);
                nextLayer.removeAll(results); // don't go twice through the same dataset
                currentLayer = nextLayer;
            }
            return listByDatasetIds(results);
        }
    }

    @Override
    public List<AbstractExternalData> listBySampleIds(Collection<Long> sampleIds)
    {
        return listBySampleIds(sampleIds, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    @Override
    public List<AbstractExternalData> listBySampleIds(Collection<Long> sampleIds,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        LongSet ids = new LongOpenHashSet();
        for (Long id : sampleIds)
        {
            ids.add(id);
        }
        return enrichDatasets(query.getDatasetsForSamples(ids), datasetFetchOptions);
    }

    @Override
    public List<AbstractExternalData> listByExperimentTechId(TechId experimentId,
            boolean showOnlyDirectlyConnected)
    {
        DataIterator<DatasetRecord> dataSets;
        if (showOnlyDirectlyConnected)
        {
            dataSets = query.getDatasetsForExperiment(experimentId.getId());
        } else
        {
            dataSets = query.getDataSetsForExperimentAndDescendents(experimentId.getId());
        }
        return enrichDatasets(dataSets);
    }

    @Override
    public List<AbstractExternalData> listByMetaprojectId(Long metaprojectId)
    {
        DataIterator<DatasetRecord> dataSets = query.getDatasetsForMetaproject(metaprojectId);
        return enrichDatasets(dataSets);
    }

    @Override
    public Map<Long, Set<Long>> listParentIds(Collection<Long> dataSetIDs)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : dataSetIDs)
        {
            ids.add(id);
        }
        DataIterator<DatasetRelationRecord> relationships = query.listParentDataSetIds(ids);
        Map<Long, Set<Long>> map = new LinkedHashMap<Long, Set<Long>>();
        for (DatasetRelationRecord relationship : relationships)
        {
            Set<Long> parents = map.get(relationship.data_id_child);
            if (parents == null)
            {
                parents = new LinkedHashSet<Long>();
                map.put(relationship.data_id_child, parents);
            }
            parents.add(relationship.data_id_parent);
        }
        return map;
    }

    @Override
    public Map<Long, Set<Long>> listChildrenIds(Collection<Long> dataSetIDs)
    {
        LongOpenHashSet ids = new LongOpenHashSet();
        for (Long id : dataSetIDs)
        {
            ids.add(id);
        }
        DataIterator<DatasetRelationRecord> relationships = query.listChildrenDataSetIds(ids);
        Map<Long, Set<Long>> map = new LinkedHashMap<Long, Set<Long>>();
        for (DatasetRelationRecord relationship : relationships)
        {
            Set<Long> children = map.get(relationship.data_id_parent);
            if (children == null)
            {
                children = new LinkedHashSet<Long>();
                map.put(relationship.data_id_parent, children);
            }
            children.add(relationship.data_id_child);
        }
        return map;
    }

    @Override
    public Map<Sample, List<AbstractExternalData>> listAllDataSetsFor(List<Sample> samples)
    {
        TableMap<Long, Sample> samplesByID =
                new TableMap<Long, Sample>(samples, new IKeyExtractor<Long, Sample>()
                    {
                        @Override
                        public Long getKey(Sample e)
                        {
                            return e.getId();
                        }
                    });
        Map<Sample, List<AbstractExternalData>> result =
                new HashMap<Sample, List<AbstractExternalData>>();
        Set<Long> sampleIDs = new HashSet<Long>();
        for (Sample sample : samples)
        {
            result.put(sample, new ArrayList<AbstractExternalData>());
            sampleIDs.add(sample.getId());
        }
        List<AbstractExternalData> rootDataSets = listBySampleIds(sampleIDs);
        addChildren(rootDataSets);
        for (AbstractExternalData dataSet : rootDataSets)
        {
            Sample sample = samplesByID.tryGet(dataSet.getSample().getId());
            assert sample != null;
            result.get(sample).add(dataSet);
        }
        return result;
    }

    private void addChildren(List<AbstractExternalData> dataSets)
    {
        Map<Long, AbstractExternalData> dataSetsByID = new HashMap<Long, AbstractExternalData>();
        for (AbstractExternalData dataSet : dataSets)
        {
            dataSetsByID.put(dataSet.getId(), dataSet);
        }
        Map<Long, Set<Long>> childrenIDs = listChildrenIds(dataSetsByID.keySet());
        Set<Entry<Long, Set<Long>>> entrySet = childrenIDs.entrySet();
        Map<Long, Set<Long>> child2ParentsMap = new HashMap<Long, Set<Long>>();
        Set<Long> childIDs = new HashSet<Long>();
        for (Entry<Long, Set<Long>> entry : entrySet)
        {
            Set<Long> value = entry.getValue();
            Long parentID = entry.getKey();
            for (Long childId : value)
            {
                childIDs.add(childId);
                Set<Long> parents = child2ParentsMap.get(childId);
                if (parents == null)
                {
                    parents = new HashSet<Long>(1);
                    child2ParentsMap.put(childId, parents);
                }
                parents.add(parentID);
            }
        }
        if (childIDs.isEmpty() == false)
        {
            List<AbstractExternalData> children = listByDatasetIds(childIDs);
            for (AbstractExternalData child : children)
            {
                Set<Long> parentIDs = child2ParentsMap.get(child.getId());
                for (Long parentID : parentIDs)
                {
                    AbstractExternalData dataSet = dataSetsByID.get(parentID);
                    Collection<AbstractExternalData> childList = dataSet.getChildren();
                    if (childList == null)
                    {
                        childList = new ArrayList<AbstractExternalData>(1);
                        dataSet.setChildren(childList);
                    }
                    childList.add(child);
                }
            }
            addChildren(children);
        }
    }

    @Override
    public List<AbstractExternalData> listByChildTechId(TechId childDatasetId)
    {
        return enrichDatasets(query.getParentDatasetsForChild(childDatasetId.getId()));
    }

    @Override
    public List<AbstractExternalData> listByContainerTechId(TechId containerDatasetId)
    {
        return enrichDatasets(query.getContainedDatasetsForContainer(containerDatasetId.getId()));
    }

    @Override
    public List<AbstractExternalData> listByParentTechIds(Collection<Long> parentDatasetIds)
    {
        DataIterator<DatasetRecord> childrenDataSets =
                query.getChildDatasetsForParents(new LongOpenHashSet(parentDatasetIds));
        return enrichDatasets(childrenDataSets);
    }

    @Override
    public List<AbstractExternalData> listByDatasetIds(Collection<Long> datasetIds)
    {
        return listByDatasetIds(datasetIds, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    @Override
    public List<AbstractExternalData> listByDatasetIds(Collection<Long> datasetIds,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        return enrichDatasets(query.getDatasets(new LongOpenHashSet(datasetIds)),
                datasetFetchOptions);
    }

    @Override
    public List<AbstractExternalData> listByDatasetCode(Collection<String> datasetCodes)
    {
        return listByDatasetCode(datasetCodes, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    @Override
    public List<AbstractExternalData> listByDatasetCode(Collection<String> datasetCodes,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        String[] codes = datasetCodes.toArray(new String[datasetCodes.size()]);
        DataIterator<DatasetRecord> datasets = query.getDatasets(codes);
        return enrichDatasets(datasets, datasetFetchOptions);
    }

    @Override
    public List<AbstractExternalData> listByDataStore(long dataStoreID)
    {
        return listByDataStore(dataStoreID, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    @Override
    public List<AbstractExternalData> listByDataStore(long dataStoreID,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        return enrichDatasets(query.getDatasetsByDataStoreId(dataStoreID), datasetFetchOptions);
    }

    @Override
    public List<AbstractExternalData> listByDataStore(long dataStoreID, int limit,
            EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        List<AbstractExternalData> data = null;
        int multiplier = 1;
        int lastSize = 0;

        // Given limit is not respected, if registration timestamp of all the returned data sets
        // would be the same. In this case we make sure we return (at least) all the data sets
        // with the same registration timestamp, no matter how many of them there are.
        while (data == null
                || (data.get(0).getRegistrationDate().equals(data.get(data.size() - 1)
                        .getRegistrationDate())) && lastSize != data.size())
        {
            lastSize = data != null ? data.size() : 0;
            data =
                    orderByDate(enrichDatasets(
                            handleDegenerateRegistrationTimestamp(
                                    query.getDatasetsByDataStoreId(dataStoreID, limit * multiplier),
                                    dataStoreID), datasetFetchOptions));
            multiplier = multiplier << 1;
        }

        return data;
    }

    @Override
    public List<AbstractExternalData> listByDataStore(long dataStoreID, Date youngerThan,
            int limit, EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        return orderByDate(enrichDatasets(
                handleDegenerateRegistrationTimestamp(
                        query.getDatasetsByDataStoreId(dataStoreID, youngerThan, limit),
                        dataStoreID), datasetFetchOptions));
    }

    @Override
    public List<AbstractExternalData> listByDataStoreWithUnknownSize(long dataStoreID, EnumSet<DataSetFetchOption> datasetFetchOptions)
    {
        checkFetchOptions(datasetFetchOptions);
        return orderByDate(enrichDatasets(
                handleDegenerateRegistrationTimestamp(
                        query.getDatasetsByDataStoreIdWithUnknownSize(dataStoreID),
                        dataStoreID), datasetFetchOptions));
    }

    private Iterable<DatasetRecord> handleDegenerateRegistrationTimestamp(List<DatasetRecord> list,
            long dataStoreID)
    {
        if (list.isEmpty())
        {
            return list;
        }
        final Date youngestDate = list.get(list.size() - 1).registration_timestamp;
        final List<DatasetRecord> youngestRecords =
                query.getDatasetsByDataStoreId(dataStoreID, youngestDate);
        // Check for degenerate case of multiple datasets having exactly regDate = youngestDate.
        if (youngestRecords.size() > 1)
        {
            final Long2ObjectMap<DatasetRecord> datasetMap =
                    new Long2ObjectOpenHashMap<DatasetRecord>(list.size() + youngestRecords.size());
            for (DatasetRecord dr : list)
            {
                datasetMap.put(dr.id, dr);
            }
            for (DatasetRecord dr : youngestRecords)
            {
                datasetMap.put(dr.id, dr);
            }
            return datasetMap.values();
        } else
        {
            return list;
        }
    }

    private List<AbstractExternalData> orderByDate(List<AbstractExternalData> list)
    {
        Collections.sort(list, new Comparator<AbstractExternalData>()
            {
                @Override
                public int compare(AbstractExternalData o1, AbstractExternalData o2)
                {
                    return o1.getRegistrationDate().compareTo(o2.getRegistrationDate());
                }
            });
        return list;
    }

    @Override
    public List<DataSetShareId> listAllDataSetShareIdsByDataStore(long dataStoreID)
    {
        List<DataSetShareId> results = new ArrayList<DataSetShareId>();

        DataIterator<DatasetCodeWithShareIdRecord> records =
                query.getAllDatasetsWithShareIdsByDataStoreId(dataStoreID);
        for (DatasetCodeWithShareIdRecord record : records)
        {
            DataSetShareId dataSetShareId = new DataSetShareId();
            dataSetShareId.setDataSetCode(record.code);
            dataSetShareId.setShareId(record.share_id);
            results.add(dataSetShareId);
        }
        return results;
    }

    @Override
    public List<AbstractExternalData> listByTrackingCriteria(TrackingDataSetCriteria criteria)
    {
        DataIterator<DatasetRecord> dataSets;
        String sampleType = criteria.getConnectedSampleTypeCode();
        long lastSeenDataSetId = criteria.getLastSeenDataSetId();
        if (sampleType == null)
        {
            dataSets = query.getNewDataSets(lastSeenDataSetId);
        } else
        {
            Long sampleTypeId = referencedEntityDAO.getSampleTypeIdForSampleTypeCode(sampleType);
            dataSets = query.getNewDataSetsForSampleType(sampleTypeId, lastSeenDataSetId);
        }
        if (criteria.shouldResultBeEnriched())
        {
            return enrichDatasets(dataSets);
        }
        loadSmallConnectedTables();
        List<DatasetRecord> datasetRecords = asList(dataSets);
        Long2ObjectMap<AbstractExternalData> datasetMap = createPrimaryDatasets(datasetRecords);
        return asList(datasetMap);
    }

    @Override
    public List<AbstractExternalData> listByArchiverCriteria(String dataStoreCode,
            ArchiverDataSetCriteria criteria)
    {
        loadSmallConnectedTables();
        final Long dataStoreId = extractDataStoreId(dataStoreCode);
        final Date lastRegistrationDate = extractLastRegistrationDate(criteria);
        final String dataSetTypeCodeOrNull = criteria.tryGetDataSetTypeCode();
        final boolean presentInArchive = criteria.isPresentInArchive();
        if (dataSetTypeCodeOrNull == null)
        {
            return enrichDatasets(query.getAvailableExtDatasRegisteredBefore(dataStoreId,
                    lastRegistrationDate, presentInArchive));
        } else
        {
            Long dataSetTypeId = extractDataSetTypeId(dataSetTypeCodeOrNull);
            return enrichDatasets(query.getAvailableExtDatasRegisteredBeforeWithDataSetType(
                    dataStoreId, lastRegistrationDate, presentInArchive, dataSetTypeId));
        }
    }

    private Date extractLastRegistrationDate(ArchiverDataSetCriteria criteria)
    {
        return DateUtils.addDays(new Date(), -criteria.getOlderThan());
    }

    private Long extractDataStoreId(String dataStoreCode)
    {
        for (Entry<Long, DataStore> entry : dataStores.entrySet())
        {
            if (dataStoreCode.equalsIgnoreCase(entry.getValue().getCode()))
            {
                return entry.getKey();
            }
        }
        throw new UserFailureException("Data store '" + dataStoreCode + "' unknown.");
    }

    private Long extractDataSetTypeId(String dataSetTypeCode)
    {
        for (Entry<Long, DataSetType> entry : dataSetTypes.entrySet())
        {
            if (dataSetTypeCode.equalsIgnoreCase(entry.getValue().getCode()))
            {
                return entry.getKey();
            }
        }
        throw new UserFailureException("Data Set type '" + dataSetTypeCode + "' unknown.");
    }

    private List<AbstractExternalData> enrichDatasets(Iterable<DatasetRecord> datasets)
    {
        return enrichDatasets(datasets, DEFAULT_DATASET_FETCH_OPTIONS);
    }

    private List<AbstractExternalData> enrichDatasets(Iterable<DatasetRecord> datasets,
            EnumSet<DataSetFetchOption> fetchOptions)
    {
        loadSmallConnectedTables();
        List<DatasetRecord> datasetRecords = asList(datasets);
        final Long2ObjectMap<AbstractExternalData> datasetMap =
                createPrimaryDatasets(datasetRecords);
        if (fetchOptions.contains(DataSetFetchOption.EXPERIMENT))
        {
            enrichWithExperiments(datasetMap);
        }
        filterDatasetsWithNullExperiments(datasetMap);
        if (fetchOptions.contains(DataSetFetchOption.PROPERTIES)
                || fetchOptions.contains(DataSetFetchOption.PROPERTIES_OF_PROPERTIES))
        {
            enrichWithProperties(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.SAMPLE))
        {
            enrichWithSamples(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.CONTAINER))
        {
            enrichWithContainers(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.CONTAINED))
        {
            enrichWithContainedDataSets(datasetMap);
        }
        if (fetchOptions.contains(DataSetFetchOption.PARENTS))
        {
            enrichWithParents(datasetMap,
                    fetchOptions.contains(DataSetFetchOption.PROPERTIES_OF_PARENTS));
        }
        if (fetchOptions.contains(DataSetFetchOption.CHILDREN))
        {
            enrichWithChildren(datasetMap,
                    fetchOptions.contains(DataSetFetchOption.PROPERTIES_OF_CHILDREN));
        }
        if (this.userId != null && fetchOptions.contains(DataSetFetchOption.METAPROJECTS))
        {
            enrichWithMetaProjects(datasetMap);
        }
        return asList(datasetMap);
    }

    private void enrichWithMetaProjects(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet set = new LongOpenHashSet();
        set.addAll(datasetMap.keySet());

        for (MetaProjectWithEntityId metaProject : query.getMetaprojects(set, userId))
        {
            Metaproject mp = new Metaproject();
            mp.setId(metaProject.id);
            mp.setCreationDate(metaProject.creation_date);
            mp.setDescription(metaProject.description);
            mp.setIdentifier("/" + metaProject.owner_name + "/" + metaProject.name);
            mp.setName(metaProject.name);
            mp.setOwnerId(metaProject.owner_name);
            mp.setPrivate(metaProject.is_private);

            AbstractExternalData data = datasetMap.get(metaProject.entity_id);

            if (data != null)
            {
                Collection<Metaproject> mps = data.getMetaprojects();
                if (mps == null)
                {
                    mps = new HashSet<Metaproject>();
                    data.setMetaprojects(mps);
                }
                mps.add(mp);
            }
        }
    }

    // assumes that the connection to the sample has been already established and sample has the
    // id set.
    private void enrichWithSamples(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet ids = extractSampleIds(datasetMap);
        Long2ObjectMap<Sample> samples = referencedEntityDAO.getSamples(ids);
        for (AbstractExternalData dataset : datasetMap.values())
        {
            if (dataset.getSample() != null)
            {
                long sampleId = dataset.getSample().getId();
                Sample sample = samples.get(sampleId);
                dataset.setSample(sample);
            }
        }
    }

    private static LongSet extractSampleIds(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet ids = new LongOpenHashSet();
        for (AbstractExternalData dataset : datasetMap.values())
        {
            if (dataset.getSample() != null)
            {
                long sampleId = dataset.getSample().getId();
                ids.add(sampleId);
            }
        }
        return ids;
    }

    // assumes that the connection to experiment has been already established and experiment has the
    // id set.
    private void enrichWithExperiments(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        Long2ObjectMap<Experiment> experimentMap = new Long2ObjectOpenHashMap<Experiment>();

        for (AbstractExternalData dataset : datasetMap.values())
        {
            long experimentId = dataset.getExperiment().getId();
            Experiment experiment = experimentMap.get(experimentId);
            // null value is put if experiment is from different db instance
            if (experimentMap.containsKey(experimentId) == false)
            {
                experiment = referencedEntityDAO.tryGetExperiment(experimentId);
                experimentMap.put(experimentId, experiment);
            }
            dataset.setExperiment(experiment);
        }
    }

    private void filterDatasetsWithNullExperiments(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        LongSet datasetsToRemove = new LongOpenHashSet();
        for (AbstractExternalData dataset : datasetMap.values())
        {
            if (dataset.getExperiment() == null)
            {
                datasetsToRemove.add(dataset.getId());
            }
        }
        for (Long datasetId : datasetsToRemove)
        {
            datasetMap.remove(datasetId);
        }
    }

    private static <T> List<T> asList(Iterable<T> items)
    {
        List<T> result = new ArrayList<T>();
        for (T item : items)
        {
            result.add(item);
        }
        return result;
    }

    private void enrichWithProperties(final Long2ObjectMap<AbstractExternalData> resultMap)
    {
        propertiesEnricher.enrich(resultMap.keySet(), new IEntityPropertiesHolderResolver()
            {
                @Override
                public AbstractExternalData get(long id)
                {
                    return resultMap.get(id);
                }
            });
    }

    private void enrichWithParents(Long2ObjectMap<AbstractExternalData> datasetMap,
            boolean withProperties)
    {
        Map<Long, Set<Long>> parentIdsMap = listParentIds(datasetMap.keySet());
        Set<Long> allParentIds = new HashSet<Long>();

        for (Set<Long> parentIds : parentIdsMap.values())
        {
            allParentIds.addAll(parentIds);
        }

        DataIterator<DatasetRecord> parentIterator =
                query.getDatasets(new LongOpenHashSet(allParentIds));

        if (parentIterator != null)
        {
            Long2ObjectMap<AbstractExternalData> parentDatasetMap =
                    withProperties ? new Long2ObjectOpenHashMap<AbstractExternalData>() : null;
            Long2ObjectMap<AbstractExternalData> parentMap = createPrimaryDatasets(parentIterator);

            for (Entry<Long, Set<Long>> parentIdsEntry : parentIdsMap.entrySet())
            {
                Long datasetId = parentIdsEntry.getKey();
                Set<Long> parentIds = parentIdsEntry.getValue();

                AbstractExternalData dataset = datasetMap.get(datasetId);
                List<AbstractExternalData> parents = new ArrayList<AbstractExternalData>();

                for (Long parentId : parentIds)
                {
                    AbstractExternalData parent = parentMap.get(parentId);
                    if (parent != null)
                    {
                        parents.add(parent);
                        if (parentDatasetMap != null)
                        {
                            parentDatasetMap.put(parentId, parent);
                        }
                    }
                }

                dataset.setParents(parents);
            }
            if (parentDatasetMap != null)
            {
                enrichWithProperties(parentDatasetMap);
            }
        }
    }

    private void enrichWithChildren(Long2ObjectMap<AbstractExternalData> datasetMap,
            boolean withProperties)
    {
        Map<Long, Set<Long>> childrenIdsMap = listChildrenIds(datasetMap.keySet());
        Set<Long> allChildrenIds = new HashSet<Long>();

        for (Set<Long> childrenIds : childrenIdsMap.values())
        {
            allChildrenIds.addAll(childrenIds);
        }

        DataIterator<DatasetRecord> childrenIterator =
                query.getDatasets(new LongOpenHashSet(allChildrenIds));

        if (childrenIterator != null)
        {
            Long2ObjectMap<AbstractExternalData> childrenDatasetMap =
                    withProperties ? new Long2ObjectOpenHashMap<AbstractExternalData>() : null;
            Long2ObjectMap<AbstractExternalData> childrenMap =
                    createPrimaryDatasets(childrenIterator);

            for (Entry<Long, Set<Long>> childrenIdsEntry : childrenIdsMap.entrySet())
            {
                Long datasetId = childrenIdsEntry.getKey();
                Set<Long> childrenIds = childrenIdsEntry.getValue();

                AbstractExternalData dataset = datasetMap.get(datasetId);
                List<AbstractExternalData> children = new ArrayList<AbstractExternalData>();

                for (Long childId : childrenIds)
                {
                    AbstractExternalData child = childrenMap.get(childId);
                    if (child != null)
                    {
                        children.add(child);
                        if (childrenDatasetMap != null)
                        {
                            childrenDatasetMap.put(childId, child);
                        }
                    }
                }
                dataset.setChildren(children);
            }
            if (childrenDatasetMap != null)
            {
                enrichWithProperties(childrenDatasetMap);
            }
        }
    }

    private void enrichWithContainers(Long2ObjectMap<AbstractExternalData> datasetMap)
    {

        Set<Long> containersNotLoaded = new HashSet<Long>();
        for (AbstractExternalData dataSet : datasetMap.values())
        {
            ContainerDataSet containerOrNull = dataSet.tryGetContainer();
            Long containerId = (containerOrNull != null) ? containerOrNull.getId() : null;
            if (containerId != null)
            {
                ContainerDataSet loadedContainer = (ContainerDataSet) datasetMap.get(containerId);
                if (loadedContainer != null)
                {
                    dataSet.setContainer(loadedContainer);
                } else
                {
                    containersNotLoaded.add(containerId);
                }
            }
        }

        if (false == containersNotLoaded.isEmpty())
        {
            // load the unavailable container data sets with an additional query
            List<AbstractExternalData> containersSecondPass = listByDatasetIds(containersNotLoaded);
            TableMap<Long, AbstractExternalData> secondPassMap =
                    new TableMap<Long, AbstractExternalData>(containersSecondPass,
                            KeyExtractorFactory.<AbstractExternalData> createIdKeyExtractor());
            for (AbstractExternalData dataSet : datasetMap.values())
            {
                ContainerDataSet containerOrNull = dataSet.tryGetContainer();
                Long containerId = (containerOrNull != null) ? containerOrNull.getId() : null;
                ContainerDataSet newlyLoaded = (ContainerDataSet) secondPassMap.tryGet(containerId);
                if (newlyLoaded != null)
                {
                    dataSet.setContainer(newlyLoaded);
                }
            }
        }
    }

    private void enrichWithContainedDataSets(Long2ObjectMap<AbstractExternalData> datasetMap)
    {
        Long2ObjectMap<AbstractExternalData> fullContextMap =
                new Long2ObjectOpenHashMap<AbstractExternalData>(datasetMap);
        LongSet containerIDs = new LongOpenHashSet();
        for (AbstractExternalData dataSet : datasetMap.values())
        {
            if (dataSet.isContainer())
            {
                containerIDs.add(dataSet.getId());
            }
            ContainerDataSet containerOrNull = dataSet.tryGetContainer();
            Long containerId = (containerOrNull != null) ? containerOrNull.getId() : null;
            if (containerId != null && false == fullContextMap.containsKey(containerId))
            {
                containerIDs.add(containerId);
                fullContextMap.put(containerId, containerOrNull);
            }
        }

        if (containerIDs.isEmpty())
        {
            return;
        }

        List<Long> containedDataSetIDs = asList(query.getContainedDataSetIds(containerIDs));
        LongSet notYetLoadedChilren = new LongOpenHashSet();

        for (Long containedDataSetID : containedDataSetIDs)
        {
            if (false == fullContextMap.containsKey(containedDataSetID))
            {
                notYetLoadedChilren.add(containedDataSetID);
            }
        }

        if (false == notYetLoadedChilren.isEmpty())
        {
            Long2ObjectMap<AbstractExternalData> childrenSecondPass =
                    createPrimaryDatasets(asList(query.getDatasets(notYetLoadedChilren)));
            fullContextMap.putAll(childrenSecondPass);
        }

        for (Long id : containedDataSetIDs)
        {
            AbstractExternalData contained = fullContextMap.get(id);
            Long containerId = contained.tryGetContainer().getId();
            ContainerDataSet container = fullContextMap.get(containerId).tryGetAsContainerDataSet();
            // set container to the child
            contained.setContainer(container);
            // add the child to the container
            List<AbstractExternalData> containedDataSets = container.getContainedDataSets();
            containedDataSets.add(contained);
            container.setContainedDataSets(containedDataSets);
        }
    }

    private static <T> List<T> asList(Long2ObjectMap<T> items)
    {
        List<T> result = new ArrayList<T>();
        result.addAll(items.values());
        return result;
    }

    private Long2ObjectMap<AbstractExternalData> createPrimaryDatasets(
            Iterable<DatasetRecord> records)
    {
        Long2ObjectMap<AbstractExternalData> datasets =
                new Long2ObjectOpenHashMap<AbstractExternalData>();
        for (DatasetRecord record : records)
        {
            DataSetType dsType = dataSetTypes.get(record.dsty_id);
            AbstractExternalData dataSetOrNull = null;
            if (record.is_placeholder)
            {
                // placeholder data sets are filtered out
            } else if (dsType.getDataSetKind() == DataSetKind.CONTAINER)
            {
                dataSetOrNull = convertToContainerDataSet(record);
            } else if (dsType.getDataSetKind() == DataSetKind.LINK)
            {
                dataSetOrNull = convertToLinkDataSet(record);
            } else if (record.location != null)
            {
                dataSetOrNull = convertToDataSet(record);
            }

            if (dataSetOrNull != null)
            {
                enrichWithDeletion(dataSetOrNull, record);
                datasets.put(record.id, dataSetOrNull);
            }
        }
        return datasets;
    }

    // NOTE: this just marks the data set as invalid without loading any details
    private void enrichWithDeletion(final AbstractExternalData dataSet, DatasetRecord row)
    {
        if (row.del_id != null)
        {
            final Deletion deletion = new Deletion();
            dataSet.setDeletion(deletion);
        }
    }

    private PhysicalDataSet convertToDataSet(DatasetRecord record)
    {
        PhysicalDataSet dataSet = new PhysicalDataSet();
        convertStandardAttributes(dataSet, record);

        dataSet.setComplete(resolve(record.is_complete));
        dataSet.setStatus(record.status == null ? null : DataSetArchivingStatus
                .valueOf(record.status));
        dataSet.setPresentInArchive(record.present_in_archive == null ? false
                : record.present_in_archive);
        dataSet.setSpeedHint(record.speed_hint == null ? Constants.DEFAULT_SPEED_HINT
                : record.speed_hint);
        dataSet.setShareId(record.share_id);
        dataSet.setFileFormatType(record.ffty_id == null ? null : fileFormatTypes
                .get(record.ffty_id));
        dataSet.setStorageConfirmation(record.storage_confirmation == null ? false
                : record.storage_confirmation);
        dataSet.setLocation(record.location);
        dataSet.setSize(record.size);
        dataSet.setLocatorType(record.loty_id == null ? null : locatorTypes.get(record.loty_id));
        return dataSet;
    }

    private ContainerDataSet convertToContainerDataSet(DatasetRecord record)
    {
        ContainerDataSet containerDataSet = new ContainerDataSet();
        convertStandardAttributes(containerDataSet, record);
        return containerDataSet;
    }

    private LinkDataSet convertToLinkDataSet(DatasetRecord record)
    {
        LinkDataSet linkDataSet = new LinkDataSet();

        convertStandardAttributes(linkDataSet, record);
        linkDataSet.setExternalDataManagementSystem(externalDataManagementSystems
                .get(record.edms_id));
        linkDataSet.setExternalCode(record.external_code);

        return linkDataSet;
    }

    private void convertStandardAttributes(AbstractExternalData dataSet, DatasetRecord record)
    {
        dataSet.setCode(record.code);
        dataSet.setVersion(record.version);
        dataSet.setDataSetType(dataSetTypes.get(record.dsty_id));
        dataSet.setId(record.id);
        dataSet.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.DATA_SET,
                record.code));
        dataSet.setId(record.id);
        dataSet.setDataProducerCode(record.data_producer_code);
        dataSet.setDataStore(dataStores.get(record.dast_id));
        dataSet.setDerived(record.is_derived);
        dataSet.setOrderInContainer(record.ctnr_order);
        dataSet.setProductionDate(record.production_timestamp);
        dataSet.setRegistrationDate(record.registration_timestamp);
        dataSet.setRegistrator(getOrCreateActor(record.pers_id_registerer));
        dataSet.setModificationDate(record.modification_timestamp);
        dataSet.setModifier(getOrCreateActor(record.pers_id_modifier));
        dataSet.setDataSetProperties(new ArrayList<IEntityProperty>());

        if (record.ctnr_id != null)
        {
            ContainerDataSet container = new ContainerDataSet();
            container.setId(record.ctnr_id);
            dataSet.setContainer(container);
        }

        if (record.samp_id != null)
        {
            Sample sample = new Sample();
            sample.setId(record.samp_id);
            dataSet.setSample(sample);
        }

        Experiment experiment = new Experiment();
        experiment.setId(record.expe_id);
        dataSet.setExperiment(experiment);
    }

    private Boolean resolve(String booleanRepresentative)
    {
        if (booleanRepresentative == null)
        {
            return null;
        }
        return BooleanOrUnknown.tryToResolve(BooleanOrUnknown.valueOf(booleanRepresentative));
    }

    private void loadSmallConnectedTables()
    {
        dataSetTypes.clear();
        for (DataSetTypeRecord code : query.getDatasetTypes(databaseInstanceId))
        {
            dataSetTypes.put(code.id, createDataSetType(code));
        }

        fileFormatTypes.clear();
        for (CodeRecord code : query.getFileFormatTypes(databaseInstanceId))
        {
            fileFormatTypes.put(code.id, createFileFormatType(code));
        }

        locatorTypes.clear();
        for (CodeRecord code : query.getLocatorTypes())
        {
            locatorTypes.put(code.id, createLocatorType(code));
        }

        dataStores.clear();
        for (DataStoreRecord code : query.getDataStores(databaseInstanceId))
        {
            dataStores.put(code.id, createDataStore(code));
        }

        externalDataManagementSystems.clear();
        for (ExternalDataManagementSystemRecord edms : query
                .getExternalDataManagementSystems(databaseInstanceId))
        {
            externalDataManagementSystems.put(edms.id, createExternalDataManagementSystem(edms));
        }
    }

    private static void setCode(Code<?> codeHolder, CodeRecord codeRecord)
    {
        codeHolder.setCode(codeRecord.code);
    }

    private static DataStore createDataStore(DataStoreRecord codeRecord)
    {
        DataStore result = new DataStore();
        setCode(result, codeRecord);
        result.setHostUrl(codeRecord.download_url);
        String downloadUrl = DataStoreTranslator.translateDownloadUrl(codeRecord.download_url);
        result.setDownloadUrl(downloadUrl);
        return result;
    }

    private static ExternalDataManagementSystem createExternalDataManagementSystem(
            ExternalDataManagementSystemRecord edmsRecord)
    {
        ExternalDataManagementSystem result = new ExternalDataManagementSystem();

        result.setId(edmsRecord.id);
        result.setCode(edmsRecord.code);
        result.setLabel(edmsRecord.label);
        result.setUrlTemplate(edmsRecord.url_template);
        result.setOpenBIS(edmsRecord.is_openbis);

        return result;
    }

    private static LocatorType createLocatorType(CodeRecord codeRecord)
    {
        LocatorType result = new LocatorType();
        setCode(result, codeRecord);
        return result;
    }

    private static FileFormatType createFileFormatType(CodeRecord codeRecord)
    {
        FileFormatType result = new FileFormatType();
        setCode(result, codeRecord);
        return result;
    }

    private DataSetType createDataSetType(DataSetTypeRecord record)
    {
        DataSetType result = new DataSetType();
        setCode(result, record);
        result.setDatabaseInstance(databaseInstance);
        result.setDataSetKind(DataSetKind.valueOf(record.data_set_kind));
        return result;
    }

    @Override
    public Map<Long, GenericEntityPropertyRecord> fetchProperties(List<Long> ids,
            String propertyTypeCode)
    {
        DataIterator<GenericEntityPropertyRecord> queryResult =
                query.getEntityPropertyGenericValues(new LongOpenHashSet(ids), propertyTypeCode);

        Map<Long, GenericEntityPropertyRecord> result =
                new HashMap<Long, GenericEntityPropertyRecord>();
        for (GenericEntityPropertyRecord record : queryResult)
        {
            result.put(record.entity_id, record);
        }

        return result;
    }

    @Override
    public IDatasetLocationNode listLocationsByDatasetCode(String datasetCode)
    {
        TableMap<Long, DatasetLocationNodeRecord> records = loadRawLocationData(datasetCode);

        Map<Long, DatasetLocationNode> nodeMap = new HashMap<Long, DatasetLocationNode>();
        DatasetLocationNode rootNode = null;
        for (DatasetLocationNodeRecord record : records)
        {
            DatasetLocation location = new DatasetLocation();
            location.setDatasetCode(record.code);
            location.setDataSetLocation(record.location);
            location.setDataStoreCode(record.data_store_code);
            location.setDataStoreUrl(record.data_store_url);
            DatasetLocationNode node = new DatasetLocationNode(location);
            if (datasetCode.equals(record.code))
            {
                rootNode = node;
            }
            nodeMap.put(record.id, node);
        }

        linkContainedData(records, nodeMap);

        return rootNode;
    }

    private void linkContainedData(TableMap<Long, DatasetLocationNodeRecord> records,
            Map<Long, DatasetLocationNode> nodeMap)
    {
        Set<Entry<Long, DatasetLocationNode>> entrySet = nodeMap.entrySet();
        for (Entry<Long, DatasetLocationNode> entry : entrySet)
        {
            Long id = entry.getKey();
            DatasetLocationNode node = entry.getValue();
            Long containerId = records.tryGet(id).ctnr_id;
            if (containerId != null)
            {
                DatasetLocationNode containerNode = nodeMap.get(containerId);
                if (containerNode != null)
                {
                    containerNode.addContained(node);
                }
            }
        }
    }

    private TableMap<Long, DatasetLocationNodeRecord> loadRawLocationData(String datasetCode)
    {
        DataIterator<DatasetLocationNodeRecord> queryResult =
                query.listLocationsByDatasetCode(datasetCode);
        TableMap<Long, DatasetLocationNodeRecord> records =
                new TableMap<Long, DatasetLocationNodeRecord>(queryResult,
                        new IKeyExtractor<Long, DatasetLocationNodeRecord>()
                            {
                                @Override
                                public Long getKey(DatasetLocationNodeRecord r)
                                {
                                    return r.id;
                                }
                            });
        queryResult.close();
        return records;
    }

    @Override
    public List<String> listContainedCodes(String datasetCode)
    {
        List<String> result = new LinkedList<String>();
        DataIterator<String> queryResult = query.getContainedDataSetCodes(datasetCode);
        for (String containedCode : queryResult)
        {
            result.add(containedCode);
        }
        queryResult.close();
        return result;
    }

}
