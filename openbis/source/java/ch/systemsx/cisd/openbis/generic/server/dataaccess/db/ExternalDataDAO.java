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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Implementation of {@link IExternalDataDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class ExternalDataDAO extends AbstractGenericEntityDAO<ExternalDataPE> implements
        IExternalDataDAO
{
    private static final String EXTERNAL_DATA_UPDATE_TEMPLATE =
            "insert into %s (data_id, location, loty_id, ffty_id, is_complete, cvte_id_stor_fmt) "
                    + "values (%d, '%s', %d, %d, '%c', %d)";

    private final static Class<ExternalDataPE> ENTITY_CLASS = ExternalDataPE.class;

    private final static Class<DataPE> ENTITY_SUPER_CLASS = DataPE.class;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ExternalDataDAO.class);

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    ExternalDataDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ENTITY_CLASS);
    }

    //
    // IExternalDataDAO
    //

    public final List<ExternalDataPE> listExternalData(final SamplePE sample)
            throws DataAccessException
    {
        assert sample != null : "Unspecified sample.";

        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.parents " + "left join fetch e.dataSetProperties "
                        + "where e.sampleInternal = ?", TABLE_NAME);
        final List<ExternalDataPE> list = cast(getHibernateTemplate().find(query, toArray(sample)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for [sample=%s].",
                    list.size(), sample));
        }
        return list;
    }

    public final List<ExternalDataPE> listExternalData(final ExperimentPE experiment)
            throws DataAccessException
    {
        assert experiment != null : "Unspecified experiment.";

        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.parents " + "left join fetch e.dataSetProperties "
                        + "where e.experimentInternal = ?", TABLE_NAME);
        final List<ExternalDataPE> list =
                cast(getHibernateTemplate().find(query, toArray(experiment)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d external data have been found for [experiment=%e].", list.size(),
                    experiment));
        }
        return list;
    }

    private void distinct(List<ExternalDataPE> list)
    {
        Set<ExternalDataPE> set = new TreeSet<ExternalDataPE>(list);
        list.clear();
        list.addAll(set);
    }

    public DataPE tryToFindDataSetByCode(String dataSetCode)
    {
        assert dataSetCode != null : "Unspecified data set code.";

        String name = ENTITY_SUPER_CLASS.getSimpleName();
        String hql = String.format("select e from %s e where e.code = ?", name);
        String normalizedCode = CodeConverter.tryToDatabase(dataSetCode);
        final List<DataPE> list = cast(getHibernateTemplate().find(hql, toArray(normalizedCode)));
        final DataPE entity = tryFindEntity(list, "data set");
        if (operationLog.isDebugEnabled())
        {
            String methodName = MethodUtils.getCurrentMethod().getName();
            operationLog.debug(String.format("%s(%s): '%s'.", methodName, dataSetCode, entity));
        }
        return entity;
    }

    public ExternalDataPE tryToFindFullDataSetByCode(String dataSetCode)
    {
        assert dataSetCode != null : "Unspecified data set code";

        final String mangledCode = CodeConverter.tryToDatabase(dataSetCode);
        final Criterion codeEq = Restrictions.eq("code", mangledCode);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeEq);
        criteria.setFetchMode("dataSetType.dataSetTypePropertyTypesInternal", FetchMode.JOIN);
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        final List<ExternalDataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final ExternalDataPE entity = tryFindEntity(list, "data set");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("External data '%s' found for data set code '%s'.",
                    entity, dataSetCode));
        }
        return entity;
    }

    public void createDataSet(DataPE dataset)
    {
        assert dataset != null : "Unspecified data set.";

        dataset.setCode(CodeConverter.tryToDatabase(dataset.getCode()));
        final HibernateTemplate template = getHibernateTemplate();
        template.save(dataset);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: data set '%s'.", dataset));
        }
    }

    public void updateDataSet(ExternalDataPE externalData)
    {
        assert externalData != null : "Given external data can not be null.";
        validatePE(externalData);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        externalData.setCode(CodeConverter.tryToDatabase(externalData.getCode()));
        Long id = HibernateUtils.getId(externalData);
        final DataPE loaded = (DataPE) hibernateTemplate.load(ENTITY_CLASS, id);
        // This just means that we do not have any entry in 'EXTERNAL_DATA' table for this id. It
        // might happen when we work with placeholder data.
        if (loaded instanceof ExternalDataPE == false)
        {
            String location = externalData.getLocation();
            Long locatorTypeID = externalData.getLocatorType().getId();
            Long fileFormatTypeID = externalData.getFileFormatType().getId();
            char complete = externalData.getComplete().name().charAt(0);
            Long storageFormatTermID = externalData.getStorageFormatVocabularyTerm().getId();
            final String sql =
                    String.format(EXTERNAL_DATA_UPDATE_TEMPLATE, TableNames.EXTERNAL_DATA_TABLE,
                            id, location, locatorTypeID, fileFormatTypeID, complete,
                            storageFormatTermID);
            executeUpdate(sql);
            hibernateTemplate.evict(loaded);
        }
        hibernateTemplate.update(externalData);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: external data '%s'.", externalData));
        }
    }

    @Override
    public void delete(ExternalDataPE entity) throws DataAccessException
    {
        assert entity != null : "entity unspecified";
        // TODO 2009-06-09, Piotr Buczek: remove this check if we change many2many -> one2many
        if (entity.getChildren().size() > 0)
        {
            throw new DataIntegrityViolationException(
                    "Entity cannot be deleted because children datasets are connected.");
        }
        super.delete(entity);
    }

}
