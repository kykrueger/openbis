/*
 * Copyright 2007 ETH Zuerich, CISD
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

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * Data access object for {@link SampleTypePE}. <br>
 * Note: this class has been copied from old lims project.
 * 
 * @author Christian Ribeaud
 */
final class SampleTypeDAO extends AbstractTypeDAO<SampleTypePE> implements ISampleTypeDAO
{
    @Private
    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SampleTypeDAO.class);

    SampleTypeDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    //
    // AbstractTypeDAO
    //

    @Override
    final Class<SampleTypePE> getEntityClass()
    {
        return SampleTypePE.class;
    }

    //
    // ISampleTypeDAO
    //

    public final List<SampleTypePE> listSampleTypes(final boolean onlyListable)
            throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.eq("databaseInstance", getDatabaseInstance()));
        criteria.setFetchMode("sampleTypePropertyTypesInternal", FetchMode.JOIN);
        criteria.setFetchMode(
                "sampleTypePropertyTypesInternal.propertyTypeInternal.vocabulary.vocabularyTerms",
                FetchMode.JOIN);
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (onlyListable)
        {
            criteria.add(Restrictions.eq("listable", true));
        }
        final List<SampleTypePE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d sample type(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), onlyListable, list.size()));
        }
        return list;
    }

    public final SampleTypePE tryFindSampleTypeByExample(final SampleTypePE sampleType)
            throws DataAccessException
    {
        assert sampleType != null : "Unspecified sample type.";

        final List<SampleTypePE> list = cast(getHibernateTemplate().findByExample(sampleType));
        final SampleTypePE result = tryFindEntity(list, "sample type");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): Sample type '%s' found.", MethodUtils
                    .getCurrentMethod().getName(), sampleType, result));
        }
        return result;
    }

    public final SampleTypePE tryFindSampleTypeByCode(final String code) throws DataAccessException
    {
        return tryFindTypeByCode(code);
    }

}