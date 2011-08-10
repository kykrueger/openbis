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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Implementation of {@link IDataDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class DataDAO extends AbstractGenericEntityWithPropertiesDAO<DataPE> implements IDataDAO
{
    private final static Class<DataPE> ENTITY_CLASS = DataPE.class;

    private final static Class<ExternalDataPE> EXTERNAL_DATA_ENTITY_CLASS = ExternalDataPE.class;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataDAO.class);

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    private static final String EXTERNAL_DATA_TABLE_NAME = EXTERNAL_DATA_ENTITY_CLASS
            .getSimpleName();

    DataDAO(final PersistencyResources persistencyResources,
            final DatabaseInstancePE databaseInstance)
    {
        super(persistencyResources, databaseInstance, ENTITY_CLASS);
    }

    //
    // IExternalDataDAO
    //

    public boolean hasDataSet(SamplePE sample) throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExternalDataPE.class);
        criteria.add(Restrictions.eq("sampleInternal", sample));
        criteria.setProjection(Projections.rowCount());
        Integer count = (Integer) getHibernateTemplate().findByCriteria(criteria).get(0);
        return count > 0;
    }

    public final List<DataPE> listRelatedDataSets(final IEntityInformationHolder entity)
            throws DataAccessException
    {
        assert entity != null : "Unspecified entity.";

        final String entityName = entity.getEntityKind().toString().toLowerCase();
        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.sampleInternal " + "left join fetch e.parents "
                        + "left join fetch e.dataSetProperties " + "where e.%sInternal.id = ?",
                        TABLE_NAME, entityName);
        final List<DataPE> list = cast(getHibernateTemplate().find(query, toArray(entity.getId())));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for [entity=%s].",
                    list.size(), entity));
        }
        return list;
    }

    public final List<DataPE> listDataSets(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample.";

        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.parents " + "left join fetch e.dataSetProperties "
                        + "where e.sampleInternal = ?", TABLE_NAME);
        final List<DataPE> list = cast(getHibernateTemplate().find(query, toArray(sample)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for [sample=%s].",
                    list.size(), sample));
        }
        return list;
    }

    public final List<DataPE> listExternalData(final DataStorePE dataStore)
            throws DataAccessException
    {
        assert dataStore != null : "Unspecified data store.";
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExternalDataPE.class);
        criteria.add(Restrictions.eq("dataStore", dataStore));
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d data set(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), dataStore, list.size()));
        }
        return list;
    }

    public final List<DataPE> listDataSets(final ExperimentPE experiment)
            throws DataAccessException
    {
        assert experiment != null : "Unspecified experiment.";

        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.parents " + "left join fetch e.dataSetProperties "
                        + "where e.experimentInternal = ?", TABLE_NAME);
        final List<DataPE> list = cast(getHibernateTemplate().find(query, toArray(experiment)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d external data have been found for [experiment=%s].", list.size(),
                    experiment));
        }
        return list;
    }

    private void distinct(List<DataPE> list)
    {
        Set<DataPE> set = new TreeSet<DataPE>(list);
        list.clear();
        list.addAll(set);
    }

    public DataPE tryToFindDataSetByCode(String dataSetCode)
    {
        assert dataSetCode != null : "Unspecified data set code.";

        final String mangledCode = CodeConverter.tryToDatabase(dataSetCode);
        final Criterion codeEq = Restrictions.eq("code", mangledCode);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeEq);
        criteria.setFetchMode("dataSetType", FetchMode.JOIN);
        criteria.setFetchMode("dataStore", FetchMode.JOIN);
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final DataPE entity = tryFindEntity(list, "data set");

        if (operationLog.isDebugEnabled())
        {
            String methodName = MethodUtils.getCurrentMethod().getName();
            operationLog.debug(String.format("%s(%s): '%s'.", methodName, dataSetCode, entity));
        }
        return entity;
    }

    public List<DeletedDataPE> tryToFindDeletedDataSetsByCodes(Collection<String> dataSetCodes)
    {
        assert dataSetCodes != null : "Unspecified collection";

        if (dataSetCodes.size() == 0)
        {
            return Collections.emptyList();
        }

        final Criterion codeIn = Restrictions.in("code", dataSetCodes);

        final DetachedCriteria criteria = DetachedCriteria.forClass(DeletedDataPE.class);
        criteria.add(codeIn);
        criteria.setFetchMode("dataStore", FetchMode.SELECT);
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        final List<DeletedDataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Found '%s' data sets for codes '%s'.", list.size(),
                    dataSetCodes));
        }
        return list;
    }

    public List<DataPE> tryToFindFullDataSetsByCodes(Collection<String> dataSetCodes,
            boolean withPropertyTypes, boolean lockForUpdate)
    {
        assert dataSetCodes != null : "Unspecified collection";

        if (dataSetCodes.size() == 0)
        {
            return Collections.emptyList();
        }

        final Criterion codeIn = Restrictions.in("code", dataSetCodes);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeIn);
        criteria.setFetchMode("dataSetType", FetchMode.SELECT);
        criteria.setFetchMode("dataStore", FetchMode.SELECT);
        criteria.setFetchMode("experimentInternal", FetchMode.SELECT);
        criteria.setFetchMode("sampleInternal", FetchMode.SELECT);
        criteria.setFetchMode("fileFormat", FetchMode.SELECT);
        if (withPropertyTypes)
        {
            criteria.setFetchMode("dataSetType.dataSetTypePropertyTypesInternal", FetchMode.JOIN);
        }
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (lockForUpdate)
        {
            criteria.setLockMode(LockMode.UPGRADE);
        }
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Found '%s' data sets for codes '%s'.", list.size(),
                    dataSetCodes));
        }
        return list;
    }

    public DataPE tryToFindFullDataSetByCode(String dataSetCode, boolean withPropertyTypes,
            boolean lockForUpdate)
    {
        assert dataSetCode != null : "Unspecified data set code";

        final String mangledCode = CodeConverter.tryToDatabase(dataSetCode);
        final Criterion codeEq = Restrictions.eq("code", mangledCode);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeEq);
        criteria.setFetchMode("dataSetType", FetchMode.SELECT);
        criteria.setFetchMode("dataStore", FetchMode.SELECT);
        criteria.setFetchMode("experimentInternal", FetchMode.SELECT);
        criteria.setFetchMode("sampleInternal", FetchMode.SELECT);
        criteria.setFetchMode("fileFormat", FetchMode.SELECT);
        if (withPropertyTypes)
        {
            criteria.setFetchMode("dataSetType.dataSetTypePropertyTypesInternal", FetchMode.JOIN);
        }
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (lockForUpdate)
        {
            criteria.setLockMode(LockMode.UPGRADE);
        }
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final DataPE entity = tryFindEntity(list, "data set");

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Data Set '%s' found for data set code '%s'.", entity,
                    dataSetCode));
        }
        return entity;
    }

    public void updateDataSetStatuses(final List<String> dataSetCodes,
            final DataSetArchivingStatus status)
    {
        assert dataSetCodes != null : "Unspecified data set codes";
        assert status != null : "Unspecified code";

        if (dataSetCodes.size() == 0)
        {
            return;
        }

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        int updatedRows = (Integer) hibernateTemplate.execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    // NOTE: 'VERSIONED' makes modification time modified too
                    return session
                            .createQuery(
                                    "UPDATE VERSIONED " + EXTERNAL_DATA_TABLE_NAME
                                            + " SET status = :status WHERE code IN (:codes) ")
                            .setParameter("status", status).setParameterList("codes", dataSetCodes)
                            .executeUpdate();
                }
            });
        hibernateTemplate.flush();
        if (updatedRows != dataSetCodes.size())
        {
            throw UserFailureException.fromTemplate("Update of %s data set statuses to %s failed.",
                    dataSetCodes.size(), status);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATED: %s data set statuses to '%s'.",
                    dataSetCodes.size(), status));
        }
    }

    public void updateDataSetStatuses(final List<String> dataSetCodes,
            final DataSetArchivingStatus status, final boolean presentInArchive)
    {
        assert dataSetCodes != null : "Unspecified data set codes";
        assert status != null : "Unspecified code";

        if (dataSetCodes.size() == 0)
        {
            return;
        }

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        int updatedRows = (Integer) hibernateTemplate.execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    // NOTE: 'VERSIONED' makes modification time modified too
                    return session
                            .createQuery(
                                    "UPDATE VERSIONED "
                                            + EXTERNAL_DATA_TABLE_NAME
                                            + " SET status = :status, presentInArchive = :presentInArchive"
                                            + " WHERE code IN (:codes) ")
                            .setParameter("status", status)
                            .setParameter("presentInArchive", presentInArchive)
                            .setParameterList("codes", dataSetCodes).executeUpdate();
                }
            });
        hibernateTemplate.flush();
        if (updatedRows != dataSetCodes.size())
        {
            throw UserFailureException.fromTemplate(
                    "Update of %s data set statuses to '%s' and presentInArchive to '%s' failed.",
                    dataSetCodes.size(), status, presentInArchive);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "UPDATED: %s data set statuses to '%s' and presentInArchive flag to '%s'.",
                    dataSetCodes.size(), status, presentInArchive));
        }
    }

    public void createDataSet(DataPE dataset)
    {
        assert dataset != null : "Unspecified data set.";

        dataset.setCode(CodeConverter.tryToDatabase(dataset.getCode()));
        if (false == dataset.isPlaceholder())
        {
            validatePE(dataset);
        }

        final HibernateTemplate template = getHibernateTemplate();
        template.save(dataset);
        template.flush();
        scheduleDynamicPropertiesEvaluation(Collections.singletonList(dataset));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: data set '%s'.", dataset));
        }
    }

    public void updateDataSet(DataPE data)
    {
        assert data != null : "Given external data can not be null.";
        validatePE(data);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        data.setCode(CodeConverter.tryToDatabase(data.getCode()));
        Long id = HibernateUtils.getId(data);
        final DataPE loaded = (DataPE) hibernateTemplate.load(ENTITY_CLASS, id);
        // This just means that we do not have any entry in 'EXTERNAL_DATA' table for this id. It
        // might happen when we work with placeholder data.
        if (loaded.isPlaceholder() && data instanceof ExternalDataPE)
        {
            ExternalDataPE externalData = (ExternalDataPE) data;
            String shareId = externalData.getShareId();
            String location = externalData.getLocation();
            Long size = externalData.getSize();
            Long locatorTypeID = externalData.getLocatorType().getId();
            Long fileFormatTypeID = externalData.getFileFormatType().getId();
            char complete = externalData.getComplete().name().charAt(0);
            Long storageFormatTermID = externalData.getStorageFormatVocabularyTerm().getId();
            if (size == null)
            {
                executeUpdate(
                        "insert into "
                                + TableNames.EXTERNAL_DATA_TABLE
                                + " (data_id, share_id, location, loty_id, ffty_id, is_complete, cvte_id_stor_fmt) "
                                + "values (?, ?, ?, ?, ?, ?, ?)", id, shareId, location,
                        locatorTypeID, fileFormatTypeID, complete, storageFormatTermID);
            } else
            {
                executeUpdate(
                        "insert into "
                                + TableNames.EXTERNAL_DATA_TABLE
                                + " (data_id, share_id, location, size, loty_id, ffty_id, is_complete, cvte_id_stor_fmt) "
                                + "values (?, ?, ?, ?, ?, ?, ?, ?)", id, shareId, location, size,
                        locatorTypeID, fileFormatTypeID, complete, storageFormatTermID);
            }
            hibernateTemplate.evict(loaded);
        }
        hibernateTemplate.update(data);
        hibernateTemplate.flush();
        scheduleDynamicPropertiesEvaluation(Collections.singletonList(data));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: external data '%s'.", data));
        }
    }

    @Override
    public void delete(DataPE entity) throws DataAccessException
    {
        assert entity != null : "entity unspecified";
        // Remove children & components and flush changes before deletion.
        // Otherwise constraint violation exception will be thrown.
        List<DataPE> children = new ArrayList<DataPE>(entity.getChildren());
        for (DataPE child : children)
        {
            child.removeParent(entity);
        }
        if (entity.isContainer())
        {
            List<DataPE> components = new ArrayList<DataPE>(entity.getContainedDataSets());
            for (DataPE component : components)
            {
                entity.removeComponent(component);
            }
        }
        flush();
        super.delete(entity);
    }

    public void delete(final List<TechId> dataIds, final PersonPE registrator, final String reason)
            throws DataAccessException
    {
        // NOTE: we use DATA_ALL_TABLE, not DELETED_DATA_VIEW because we still want to be
        // able to directly delete data without going to trash (trash may be disabled)
        final String dataTable = TableNames.DATA_ALL_TABLE;

        // For data sets we load codes, which are logically the same as perm ids.
        // We load them ordered by ID because locations need to be loaded in the same order.
        final String sqlSelectPermIds = createSelectCodesOrderByIdSQL(dataTable);
        final String sqlDeleteProperties =
                SQLBuilder.createDeletePropertiesSQL(TableNames.DATA_SET_PROPERTIES_TABLE,
                        ColumnNames.DATA_SET_COLUMN);
        // FIXME
        final String sqlSelectAttachmentContentIds =
                SQLBuilder.createSelectAttachmentContentIdsSQL(ColumnNames.DATA_SET_COLUMN);
        final String sqlDeleteAttachmentContents = SQLBuilder.createDeleteAttachmentContentsSQL();
        final String sqlDeleteAttachments = SQLBuilder.createDeleteAttachmentsSQL();
        final String sqlDeleteDataSets = SQLBuilder.createDeleteEnitiesSQL(dataTable);
        final String sqlInsertEvent = SQLBuilder.createInsertEventSQL();
        // data set specific queries
        // FIXME use locations in events
        // final String sqlSelectLocationsOrderById = createSelectLocationsOrderByIdSQL(dataTable);
        final String sqlDeleteChildrenConnections = createDeleteChildrenConnectionsSQL();
        final String sqlDeleteParentConnections = createDeleteParentConnectionsSQL();
        final String sqlDeleteComponentConnections = createDeleteComponentConnectionsSQL();

        executePermanentDeleteAction(EntityKind.DATA_SET, dataIds, registrator, reason,
                sqlSelectPermIds, sqlDeleteProperties, sqlSelectAttachmentContentIds,
                sqlDeleteAttachmentContents, sqlDeleteAttachments, sqlDeleteDataSets,
                sqlInsertEvent,
                // sqlSelectLocationsOrderById,
                sqlDeleteChildrenConnections, sqlDeleteParentConnections,
                sqlDeleteComponentConnections);
    }

    // @Override
    // protected boolean debugDeletion()
    // {
    // return true;
    // }

    protected static String createSelectCodesOrderByIdSQL(final String dataSetsTable)
    {
        return "SELECT code FROM " + dataSetsTable + " WHERE id " + SQLBuilder.inEntityIds()
                + " ORDER BY id";
    }

    protected static String createSelectLocationsOrderByIdSQL(final String dataSetsTable)
    {
        return "SELECT ed.location FROM " + dataSetsTable + " d "
                + "LEFT OUTER JOIN external_data ed ON (d.id = ed.data_id) WHERE id "
                + SQLBuilder.inEntityIds() + " ORDER BY id";
    }

    private static String createDeleteChildrenConnectionsSQL()
    {
        return "DELETE FROM " + TableNames.DATA_SET_RELATIONSHIPS_TABLE + " WHERE data_id_parent "
                + SQLBuilder.inEntityIds();
    }

    private static String createDeleteParentConnectionsSQL()
    {
        return "DELETE FROM " + TableNames.DATA_SET_RELATIONSHIPS_TABLE + " WHERE data_id_child "
                + SQLBuilder.inEntityIds();
    }

    private static String createDeleteComponentConnectionsSQL()
    {
        return "UPDATE " + TableNames.DATA_ALL_TABLE + " SET ctnr_id = NULL WHERE ctnr_id "
                + SQLBuilder.inEntityIds();
    }

    @SuppressWarnings("unchecked")
    public Set<TechId> findParentIds(final Collection<TechId> dataSetIds)
    {
        // Native SQL query is used to be able to query on 'many-to-many association table -
        // - 'data_set_relationships' without join with 'data' table involved in this association.
        // Drawback of this solution is that instead of list of Long values we get list of
        // BigIntegers and so a transformation is needed.

        final String query =
                "select data_id_parent from data_set_relationships where data_id_child in (:ids)";
        final List<? extends Number> results =
                (List<? extends Number>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        //
                        // HibernateCallback
                        //

                        public final Object doInHibernate(final Session session)
                        {
                            // we could remove this transformation if we choose to pass Long values
                            final List<Long> longIds = TechId.asLongs(dataSetIds);
                            return session.createSQLQuery(query).setParameterList("ids", longIds)
                                    .list();
                        }
                    });
        return transformNumbers2TechIdSet(results);
    }

    // data set relationship helper methods

    public List<DataPE> listByCode(Set<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return new ArrayList<DataPE>();
        }
        final DetachedCriteria criteria = DetachedCriteria.forClass(DataPE.class);
        criteria.add(Restrictions.in("code", values));
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d data set(s) have been found.", list.size()));
        }
        return list;
    }

    public void updateDataSets(List<DataPE> dataSets)
    {
        assert dataSets != null : "Data sets not defined";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        for (DataPE data : dataSets)
        {
            validatePE(data);
            data.setCode(CodeConverter.tryToDatabase(data.getCode()));
            hibernateTemplate.saveOrUpdate(data);
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: %d data sets.", dataSets.size()));
        }

        flushWithSqlExceptionHandling(getHibernateTemplate());
        scheduleDynamicPropertiesEvaluation(dataSets);

        // if session is not cleared registration of many samples slows down after each batch
        hibernateTemplate.clear();
    }

    @Override
    public final void validateAndSaveUpdatedEntity(DataPE entity) throws DataAccessException
    {
        super.validateAndSaveUpdatedEntity(entity);
        scheduleDynamicPropertiesEvaluation(Arrays.asList(entity));
    }

    public List<TechId> listDataSetIdsBySampleIds(final Collection<TechId> samples)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(DataPE.class);
        final List<Long> longIds = TechId.asLongs(samples);
        criteria.setProjection(Projections.id());
        criteria.add(Restrictions.in("sampleInternal.id", longIds));
        final List<Long> results = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .info(String.format("found %s data sets for given samples", results.size()));
        }
        return transformNumbers2TechIdList(results);
    }

    public List<TechId> listDataSetIdsByExperimentIds(final Collection<TechId> experiments)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(DataPE.class);
        final List<Long> longIds = TechId.asLongs(experiments);
        criteria.setProjection(Projections.id());
        criteria.add(Restrictions.in("experimentInternal.id", longIds));
        final List<Long> results = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.info(String.format("found %s data sets for given experiments",
                    results.size()));
        }
        return transformNumbers2TechIdList(results);
    }

    @Override
    Logger getLogger()
    {
        return operationLog;
    }

}
