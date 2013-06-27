/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author Jakub Straszewski
 */
public class SessionsUpdateInterceptor extends EmptyInterceptor
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private OpenBisSessionManager openBisSessionManager;

    private IDAOFactory daoFactory;

    private boolean rolesAssignmentsChanged;

    public SessionsUpdateInterceptor(OpenBisSessionManager openBisSessionManager, IDAOFactory daoFactory)
    {
        this.openBisSessionManager = openBisSessionManager;
        this.daoFactory = daoFactory;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
    {
        checkEntity(entity);
        return false;
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {
        checkEntity(entity);
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {
        checkEntity(entity);
        return false;
    }

    private void checkEntity(Object entity)
    {
        if (entity instanceof RoleAssignmentPE)
        {
            rolesAssignmentsChanged = true;
        }
    }

    @Override
    public void afterTransactionCompletion(Transaction tx)
    {
        if (rolesAssignmentsChanged && tx.wasCommitted())
        {
            openBisSessionManager.updateAllSessions(daoFactory);
        }
    }

}
