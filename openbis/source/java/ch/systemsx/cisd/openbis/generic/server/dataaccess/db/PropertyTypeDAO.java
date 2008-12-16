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
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * <i>Data Access Object</i> implementation for {@link PropertyTypePE}.
 * 
 * @author Christian Ribeaud
 */
final class PropertyTypeDAO extends AbstractDAO implements IPropertyTypeDAO
{

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PropertyTypeDAO.class);

    PropertyTypeDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    //
    // IPropertyTypeDAO
    //

    public final PropertyTypePE tryFindPropertyTypeByCode(final String code)
            throws DataAccessException
    {
        assert code != null : "Unspecified property type code";

        final List<PropertyTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("select pt from %s pt where pt.simpleCode = ? "
                                + "and pt.databaseInstance = ? and pt.internalNamespace = ?",
                                PropertyTypePE.class.getSimpleName()),
                        toArray(CodeConverter.tryToDatabase(code), getDatabaseInstance(),
                                CodeConverter.isInternalNamespace(code))));
        final PropertyTypePE entity = tryFindEntity(list, "property type");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), code, entity));
        }
        return entity;
    }

    public List<PropertyTypePE> listAllPropertyTypes()
    {
        final List<PropertyTypePE> list =
                cast(getHibernateTemplate().loadAll(PropertyTypePE.class));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d property types(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    public final List<PropertyTypePE> listPropertyTypes() throws DataAccessException
    {
        final List<PropertyTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s v where v.managedInternally = false"
                                + " and v.databaseInstance = ?", PropertyTypePE.class
                                .getSimpleName()), toArray(getDatabaseInstance())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d property types(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    public final List<DataTypePE> listDataTypes() throws DataAccessException
    {
        final List<DataTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s dt", DataTypePE.class.getSimpleName())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d data types(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    public final DataTypePE tryFindDataTypeByCode(final String code) throws DataAccessException
    {
        assert code != null : "Unspecified property type code";

        final List<DataTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s dt where dt.code = ? and dt.databaseInstance = ?",
                                PropertyTypePE.class.getSimpleName()),
                        toArray(CodeConverter.tryToDatabase(code), getDatabaseInstance())));
        final DataTypePE entity = tryFindEntity(list, "data type");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), code, entity));
        }
        return entity;
    }

    public final void createPropertyType(final PropertyTypePE propertyType)
            throws DataAccessException
    {
        assert propertyType != null : "Unspecified property type.";
        validatePE(propertyType);

        propertyType.setSimpleCode(CodeConverter.tryToDatabase(propertyType.getSimpleCode()));
        final HibernateTemplate template = getHibernateTemplate();
        template.save(propertyType);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: property type '%s'.", propertyType));
        }
    }

}
