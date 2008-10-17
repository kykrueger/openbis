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
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.GenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericServer extends AbstractServer<IGenericServer> implements IGenericServer
{
    private final IGenericBusinessObjectFactory boFactory;

    private final IAuthenticationService authenticationService;

    public GenericServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory)
    {
        this(authenticationService, sessionManager, daoFactory, new GenericBusinessObjectFactory(
                daoFactory));
    }

    @Private
    GenericServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final IGenericBusinessObjectFactory boFactory)
    {
        super(sessionManager, daoFactory);
        this.authenticationService = authenticationService;
        this.boFactory = boFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final IGenericServer createLogger(final boolean invocationSuccessful)
    {
        return new GenericServerLogger(getSessionManager(), invocationSuccessful);
    }

    //
    // IGenericServer
    //

    @Transactional
    public final List<GroupPE> listGroups(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final DatabaseInstancePE databaseInstance =
                GroupIdentifierHelper.getDatabaseInstance(identifier, getDAOFactory());
        final List<GroupPE> groups = getDAOFactory().getGroupDAO().listGroups(databaseInstance);
        final Long homeGroupID = session.tryGetHomeGroupId();
        for (final GroupPE group : groups)
        {
            group.setHome(homeGroupID != null && homeGroupID.equals(group.getId()));
        }
        Collections.sort(groups);
        return groups;
    }

    @Transactional
    public final void registerGroup(final String sessionToken, final String groupCode,
            final String descriptionOrNull, final String groupLeaderOrNull)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IGroupBO groupBO = boFactory.createGroupBO(session);
        groupBO.define(groupCode, descriptionOrNull, groupLeaderOrNull);
        groupBO.save();
    }

    @Transactional
    public final void registerPerson(final String sessionToken, final String userID)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userID);
        if (person != null)
        {
            throw UserFailureException.fromTemplate("Person '%s' already exists.", userID);
        }
        final String applicationToken = authenticationService.authenticateApplication();
        if (applicationToken == null)
        {
            throw new EnvironmentFailureException("Authentication service cannot be accessed.");
        }
        try
        {
            final Principal principal =
                    authenticationService.getPrincipal(applicationToken, userID);
            createPerson(principal, session.tryGetPerson());
        } catch (final IllegalArgumentException e)
        {
            throw new UserFailureException("Person '" + userID
                    + "' unknown by the authentication service.");
        }
    }

    @Transactional
    public final List<RoleAssignmentPE> listRoles(final String sessionToken)
    {
        getSessionManager().getSession(sessionToken);
        return getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
    }

    @Transactional
    public final void registerGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setUserId(person);
        newRoleAssignment.setGroupIdentifier(groupIdentifier);
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = boFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    @Transactional
    public final void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setUserId(person);
        newRoleAssignment.setDatabaseInstanceIdentifier(new DatabaseInstanceIdentifier(
                DatabaseInstanceIdentifier.HOME));
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = boFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    @Transactional
    public final void deleteGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {

        final Session session = getSessionManager().getSession(sessionToken);

        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindGroupRoleAssignment(roleCode,
                        groupIdentifier.getGroupCode(), person);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given role does not exist.");
        }
        final PersonPE personPE = session.tryGetPerson();
        if (roleAssignment.getPerson().compareTo(personPE) == 0
                && roleAssignment.getRole().compareTo(RoleCode.ADMIN) == 0)
        {
            boolean isInstanceAdmin = false;
            if (personPE != null && personPE.getRoleAssignments() != null)
            {
                for (final RoleAssignmentPE ra : personPE.getRoleAssignments())
                {
                    if (ra.getDatabaseInstance() != null && ra.getRole().equals(RoleCode.ADMIN))
                    {
                        isInstanceAdmin = true;
                    }
                }
            }
            if (isInstanceAdmin == false)
            {
                throw new UserFailureException(
                        "For safety reason you cannot give away your own group admin power. Ask instance admin to do that for you.");
            }
        }
        getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    @Transactional
    public final void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {

        final Session session = getSessionManager().getSession(sessionToken);
        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindInstanceRoleAssignment(roleCode,
                        person);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given role does not exist.");
        }
        if (roleAssignment.getPerson().compareTo(session.tryGetPerson()) == 0
                && roleAssignment.getRole().compareTo(RoleCode.ADMIN) == 0
                && roleAssignment.getDatabaseInstance() != null)
        {
            throw new UserFailureException(
                    "For safety reason you cannot give away your own omnipotence. Ask another instance admin to do that for you.");
        }
        getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    @Transactional
    public final List<PersonPE> listPersons(final String sessionToken)
    {
        getSessionManager().getSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();
        Collections.sort(persons);
        return persons;
    }

    @Transactional
    public final List<SampleTypePE> listSampleTypes(final String sessionToken)
    {
        getSessionManager().getSession(sessionToken);
        return getDAOFactory().getSampleTypeDAO().listSampleTypes(true);
    }

    @Transactional
    public final List<SamplePE> listSamples(final String sessionToken,
            final List<SampleOwnerIdentifier> ownerIdentifiers, final SampleTypePE sampleType)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final List<SamplePE> samples =
                boFactory.createSampleBO(session).listSamples(sampleType, ownerIdentifiers);
        return samples;
    }

    @Transactional
    public Map<SampleIdentifier, List<SamplePropertyPE>> listSamplesProperties(final String sessionToken,
            final List<SampleIdentifier> sampleIdentifiers, final List<PropertyTypePE> propertyCodes)
    {
        getSessionManager().getSession(sessionToken);
        return getDAOFactory().getSamplePropertyDAO().listSampleProperties(sampleIdentifiers,
                propertyCodes);

    }
}
