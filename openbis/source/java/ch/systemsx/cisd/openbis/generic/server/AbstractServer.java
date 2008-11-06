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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.spring.IInvocationLoggerFactory;
import ch.systemsx.cisd.common.spring.LogInterceptor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * An <i>abstract</i> {@link IServer} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractServer<T extends IServer> implements IServer,
        IInvocationLoggerFactory<T>, FactoryBean
{
    @Resource(name = ComponentNames.SESSION_MANAGER)
    private ISessionManager<Session> sessionManager;

    @Resource(name = ComponentNames.DAO_FACTORY)
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.LOG_INTERCEPTOR)
    private LogInterceptor logInterceptor;

    @Resource(name = ResourceNames.GENERIC_BUSINESS_OBJECT_FACTORY)
    private IGenericBusinessObjectFactory businessObjectFactory;

    private ProxyFactory proxyFactory;

    protected AbstractServer()
    {
    }

    private final ProxyFactory getProxyFactory()
    {
        if (proxyFactory == null)
        {
            proxyFactory = new ProxyFactory();
            proxyFactory.setTarget(this);
            proxyFactory.setInterfaces(new Class[]
                { getProxyInterface() });
            proxyFactory.addAdvice(logInterceptor);
        }
        return proxyFactory;
    }

    protected AbstractServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory, IGenericBusinessObjectFactory boFactory)
    {
        this.sessionManager = sessionManager;
        this.daoFactory = daoFactory;
        this.businessObjectFactory = boFactory;
    }

    protected final PersonPE createPerson(final Principal principal, final PersonPE registrator)
    {
        final PersonPE person = new PersonPE();
        person.setUserId(principal.getUserId());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setEmail(principal.getEmail());
        person.setRegistrator(registrator);
        daoFactory.getPersonDAO().createPerson(person);
        return person;
    }

    protected final IGenericBusinessObjectFactory getBusinessObjectFactory()
    {
        return businessObjectFactory;
    }

    protected final ISessionManager<Session> getSessionManager()
    {
        return sessionManager;
    }

    protected final IDAOFactory getDAOFactory()
    {
        return daoFactory;
    }

    protected abstract Class<T> getProxyInterface();

    //
    // FactoryBean
    //

    public final Object getObject() throws Exception
    {
        return getProxyFactory().getProxy();
    }

    @SuppressWarnings("unchecked")
    public final Class getObjectType()
    {
        return getClass();
    }

    public final boolean isSingleton()
    {
        return true;
    }

    //
    // IServer
    //

    public final IAuthSession getSession(final String sessionToken)
    {
        return sessionManager.getSession(sessionToken);
    }

    public final int getVersion()
    {
        return 1;
    }

    public final void logout(final String sessionToken)
    {
        sessionManager.closeSession(sessionToken);
    }

    public final Session tryToAuthenticate(final String user, final String password)
    {
        final String sessionToken = sessionManager.tryToOpenSession(user, password);
        if (sessionToken == null)
        {
            return null;
        }
        final Session session = sessionManager.getSession(sessionToken);
        final List<PersonPE> persons = daoFactory.getPersonDAO().listPersons();
        final boolean isFirstLoggedUser = persons.size() == 1;
        final PersonPE registrator = persons.get(0);
        PersonPE personPE = daoFactory.getPersonDAO().tryFindPersonByUserId(user);
        if (personPE == null)
        {
            personPE = createPerson(session.getPrincipal(), registrator);
        } else
        {
            HibernateUtils.initialize(personPE.getRoleAssignments());
        }
        if (session.tryGetPerson() == null)
        {
            session.setPerson(personPE);
        }
        if (isFirstLoggedUser)
        {
            final RoleAssignmentPE roleAssignmentPE = new RoleAssignmentPE();
            final PersonPE person = session.tryGetPerson();
            roleAssignmentPE.setPerson(person);
            roleAssignmentPE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
            roleAssignmentPE.setRegistrator(registrator);
            roleAssignmentPE.setRole(RoleCode.ADMIN);
            daoFactory.getRoleAssignmentDAO().createRoleAssignment(roleAssignmentPE);
            person.setRoleAssignments(Collections.singletonList(roleAssignmentPE));
        }
        return session;
    }

}
