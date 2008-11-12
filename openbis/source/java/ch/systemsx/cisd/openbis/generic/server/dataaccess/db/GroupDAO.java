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
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * <i>Data Access Object</i> implementation for {@link GroupPE}.
 * 
 * @author Christian Ribeaud
 */
final class GroupDAO extends AbstractDAO implements IGroupDAO
{

    private final static Class<GroupPE> ENTITY_CLASS = GroupPE.class;

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     * </p>
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GroupDAO.class);

    GroupDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    //
    // IGroupDAO
    //

    public final GroupPE tryFindGroupByCodeAndDatabaseInstanceId(final String groupCode,
            final long databaseInstanceId) throws DataAccessException
    {
        assert groupCode != null : "Unspecified group code";

        final List<GroupPE> list =
                cast(getHibernateTemplate().find(
                        String.format("select g from %s g where g.code = ? "
                                + "and g.databaseInstance.id = ?", ENTITY_CLASS.getSimpleName()),
                        new Object[]
                            { CodeConverter.tryToDatabase(groupCode), databaseInstanceId }));
        final GroupPE entity = tryFindEntity(list, "group");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("tryFindGroupByCodeAndDatabaseInstanceId(" + groupCode + ", "
                    + databaseInstanceId + "): '" + entity + "'");
        }
        return entity;
    }

    public final List<GroupPE> listGroups() throws DataAccessException
    {
        final List<GroupPE> list = cast(getHibernateTemplate().loadAll(ENTITY_CLASS));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("listGroups(): " + list.size() + " group(s) have been found.");
        }
        return list;
    }

    public final List<GroupPE> listGroups(final long databaseInstanceId) throws DataAccessException
    {
        final List<GroupPE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s g where g.databaseInstance.id = ?", ENTITY_CLASS
                                .getSimpleName()), new Object[]
                            { databaseInstanceId }));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("listGroups(" + databaseInstanceId + "): " + list.size()
                    + " group(s) have been found.");
        }
        return list;
    }

    public List<GroupPE> listGroups(DatabaseInstancePE databaseInstance) throws DataAccessException
    {
        assert databaseInstance != null : "Unspecified database instance.";

        DetachedCriteria criteria = DetachedCriteria.forClass(GroupPE.class);
        criteria = criteria.add(Restrictions.eq("databaseInstance", databaseInstance));
        List<GroupPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("listGroups(" + databaseInstance.getCode() + "): " + list.size()
                    + " group(s) have been found.");
        }
        return list;
    }

    public final GroupPE getGroupById(final long groupId) throws DataAccessException
    {
        final GroupPE group = (GroupPE) getHibernateTemplate().load(ENTITY_CLASS, groupId);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("getGroupById(" + groupId + "): '" + group + "'.");
        }
        return group;
    }

    public final void createGroup(final GroupPE group) throws DataAccessException
    {
        assert group != null : "Unspecified group";
        validatePE(group);

        assert group.getDatabaseInstance().isOriginalSource() : "registration on a non home database is not allowed";
        group.setCode(CodeConverter.tryToDatabase(group.getCode()));
        final HibernateTemplate template = getHibernateTemplate();
        template.save(group);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: group '%s'.", group));
        }
    }
}
